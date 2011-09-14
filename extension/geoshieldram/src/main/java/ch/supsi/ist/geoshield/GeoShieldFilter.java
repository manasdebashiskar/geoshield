/**
 * Copyright (c) 2010 Istituto Scienze della Terra - SUPSI
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Istituto Scienze della Terra - SUPSI nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE ISTITUTO SCIENZE DELLA TERRA - SUPSI BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ch.supsi.ist.geoshield;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.NameValuePair;
import org.geoserver.security.DataAccessLimits;
import org.geoserver.security.impl.GeoserverUserDao;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;

/**
 * @author Milan P. Antonovic - Istituto Scienze della Terra, SUPSI
 */
public class GeoShieldFilter implements Filter {

    private static final boolean debug = true;
    public static final String GEOSHIELD_URL = "GEOSHIELD_URL";
    public static final String GEOSHIELD_AUTH = "GEOSHIELD_AUTH";
    public static final String GEOSHIELD_USER = "GEOSHIELD_USER";
    public static final String GEOSHIELD_ACCESS_LIMITS = "GEOSHIELD_ACCESS_LIMITS";
    public static final String GEOSHIELD_LIMITS_RESYNC = "GEOSHIELD_LIMITS_RESYNC";
    
    // Cached object
    private String geoshieldUser;
    private String geoshieldUrl;
    private HashMap<String, Long> resync;
    private Long resyncTimeOut;
    private HashMap<String, Map<String, DataAccessLimits>> geoshieldAccessLimits;
    private int counter = 0;
    
    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured. 
    private FilterConfig filterConfig = null;

    public GeoShieldFilter() {
    }

    private void doBeforeProcessing(RequestWrapper request, ResponseWrapper response)
            throws IOException, ServletException, UserException {

        counter++;
        System.out.println("\n---------------- doBeforeProcessing " + counter);
        
        request.setAttribute(GEOSHIELD_URL, geoshieldUrl);

        // The service attribute is used to discriminate web-administrations 
        // request from resourse access request.
        String service = request.getParameter("service");
        if (service == null) {
            String[] pathInfo = request.getPathInfo().substring(1).split("/");
            service = pathInfo[pathInfo.length - 1];
        }

        if (service != null && (service.equalsIgnoreCase("WMS")
                || service.equalsIgnoreCase("WFS")
                || service.equalsIgnoreCase("WCS")
                || service.equalsIgnoreCase("OWS"))) {

            System.out.println(" > GSFilter intrusion activated..");

            // Reading the authorization header to get user and password
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null) {

                // Decoding authorization header
                byte[] b = Base64.decodeBase64(authHeader.substring(6).getBytes());
                String userDecoded = new String(b);

                // Caching GeoShield USER
                request.setAttribute(GEOSHIELD_AUTH, userDecoded);

                String[] userDecodedArr = userDecoded.split(":");

                if (userDecodedArr.length == 2) {

                    String user = userDecodedArr[0];
                    String password = userDecodedArr[1];

                    GeoserverUserDao gsd = new GeoserverUserDao();
                    try {
                        UserDetails ud = gsd.loadUserByUsername(user);
                        System.out.println(" > User found: " + ud.getUsername());
                        if (ud.getPassword().equals(password)) {
                            System.out.println("   > Skipping..");
                            request.setAutorizationHeader(authHeader);
                            return;
                        }
                    } catch (UsernameNotFoundException e) {
                        System.out.println(" > User NOT found: " + user);
                    }


                    // Check 4 resincronization 
                    Long now = new Long(Calendar.getInstance().getTimeInMillis());
                    if (this.resync.containsKey(userDecoded)) {
                        Long lastHit = resync.get(userDecoded);
                        Long tmp = (lastHit + (this.resyncTimeOut*1000));
                        if (tmp.compareTo(now) < 0) {
                            this.resync.put(userDecoded, now);

                            System.out.println(" > Resyncing:");

                            Connector.checkUser(geoshieldUrl, user, password);
                            //authUser(request, user, password);

                            // it will be rebuilt interface the GeoshieldResourceAccessManager class
                            this.geoshieldAccessLimits.remove(userDecoded);
                            
                        }
                    } else {
                        this.resync.put(userDecoded, now);
                        Connector.checkUser(geoshieldUrl, user, password);
                    }

                    copy2request(request, userDecoded);


                    String dummyUser = null;

                    // Extracting GeoServer user to pass internal authetication
                    // A GeoShield user must exist !!
                    UserDetails ud = gsd.loadUserByUsername(geoshieldUser);
                    dummyUser = new String(Base64.encodeBase64(
                            (ud.getUsername() + ":" + ud.getPassword()).getBytes()));

                    if (dummyUser == null) {
                        // @todo Notify wrong configuration, a GeoShield dummy user must exist!!
                        throw new UserException(
                                "Sorry! invalid user-name or password.",
                                UserException.LOGIN_ATTEMPT_FAILED);
                    } else {
                        // Overwrite authorization header with the dummy GeoShield user
                        request.setAutorizationHeader("Basic " + dummyUser);
                    }

                } else {
                    throw new UserException(
                            "Sorry! invalid user-name or password.",
                            UserException.LOGIN_ATTEMPT_FAILED);
                }
            } else {
                throw new UserException(
                        "Sorry! invalid user-name or password.",
                        UserException.LOGIN_ATTEMPT_FAILED);
            }
        }
        /*else if (!pathInfo[0].equalsIgnoreCase("web")) {
        request.setAutorizationHeader("Basic YWRtaW46Z2Vvc2VydmVy");
        }*/

    }

    private void doAfterProcessing(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        if (debug) {
            log("GeoShieldFilter:DoAfterProcessing");
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        ResponseWrapper wrappedResponse = new ResponseWrapper((HttpServletResponse) response);
        RequestWrapper wrappedRequest = new RequestWrapper((HttpServletRequest) request);

        try {
            doBeforeProcessing(wrappedRequest, wrappedResponse);
            chain.doFilter(wrappedRequest, wrappedResponse);
            //doAfterProcessing(wrappedRequest, wrappedResponse);
        } catch (UserException t) {

            if (((UserException) t).getCode().equals(UserException.INVALID_USER_OR_PASSWORD)) {

                System.out.println(" > " + UserException.INVALID_USER_OR_PASSWORD);
                wrappedResponse.sendError(ResponseWrapper.SC_FORBIDDEN);
                response.getWriter().close();

            } else if (((UserException) t).getCode().equals(UserException.LOGIN_ATTEMPT_FAILED)) {

                System.out.println(" > " + UserException.LOGIN_ATTEMPT_FAILED);
                wrappedResponse.setHeader("WWW-Authenticate", "BASIC realm=\"GeoShield Realm\"");
                wrappedResponse.sendError(ResponseWrapper.SC_UNAUTHORIZED);
                response.getWriter().close();

            } else {

                System.out.println(" > BOOOO???");

            }

        } catch (Throwable t) {
            //t.printStackTrace();
            if (t instanceof ServletException) {
                throw (ServletException) t;
            }
            if (t instanceof IOException) {
                throw (IOException) t;
            }
            sendProcessingError(t, response);
        }

    }

    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    public void destroy() {
    }

    private void authUser(RequestWrapper request, String user, String password) throws UserException {
        if (Connector.checkUser(geoshieldUrl, user, password)) {

            String dummyUser = null;

            // Extracting GeoServer user to pass internal authetication
            // A GeoShield user must exist !!
            GeoserverUserDao gsd = new GeoserverUserDao();
            UserDetails ud = gsd.loadUserByUsername(geoshieldUser);
            dummyUser = new String(Base64.encodeBase64(
                    (ud.getUsername() + ":" + ud.getPassword()).getBytes()));

            if (dummyUser == null) {
                // @todo Notify wrong configuration, a GeoShield dummy user must exist!!
                throw new UserException(
                        "Sorry! invalid user-name or password.",
                        UserException.LOGIN_ATTEMPT_FAILED);
            } else {
                // Overwrite authorization header with the dummy GeoShield user
                request.setAutorizationHeader("Basic " + dummyUser);
            }
        } else {
            throw new UserException(
                    "Sorry! invalid user-name or password.",
                    UserException.LOGIN_ATTEMPT_FAILED);
        }
    }

    public void copy2request(HttpServletRequest request, String user) {
        if (geoshieldAccessLimits.containsKey(user)) {
            request.setAttribute(GEOSHIELD_ACCESS_LIMITS, geoshieldAccessLimits.get(user));
        } else {
            HashMap<String, DataAccessLimits> tmp = new HashMap<String, DataAccessLimits>();
            geoshieldAccessLimits.put(user, tmp);
            request.setAttribute(GEOSHIELD_ACCESS_LIMITS, tmp);
        }

    }

    public void init(FilterConfig filterConfig) {

        this.resync = new HashMap<String, Long>();
        this.geoshieldAccessLimits = new HashMap<String, Map<String, DataAccessLimits>>();

        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            if (debug) {
                log("GeoShieldFilter: Initializing filter");
            }
            this.filterConfig.getServletContext().setAttribute(
                    GEOSHIELD_ACCESS_LIMITS,
                    new HashMap<String, Map<String, DataAccessLimits>>());
            this.geoshieldUser = this.filterConfig.getInitParameter(GEOSHIELD_USER);
            this.geoshieldUrl = this.filterConfig.getInitParameter(GEOSHIELD_URL);
            this.resyncTimeOut = Long.decode(this.filterConfig.getInitParameter(GEOSHIELD_LIMITS_RESYNC));
        }
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("GeoShieldFilter()");
        }
        StringBuffer sb = new StringBuffer("GeoShieldFilter(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());
    }

    private void sendProcessingError(Throwable t, ServletResponse response) {
        String stackTrace = getStackTrace(t);

        if (stackTrace != null && !stackTrace.equals("")) {
            try {
                response.setContentType("text/html");
                PrintStream ps = new PrintStream(response.getOutputStream());
                PrintWriter pw = new PrintWriter(ps);
                pw.print("<html>\n<head>\n<title>Error</title>\n</head>\n<body>\n"); //NOI18N

                // PENDING! Localize this for next official release
                pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");
                pw.print(stackTrace);
                pw.print("</pre></body>\n</html>"); //NOI18N
                pw.close();
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        } else {
            try {
                PrintStream ps = new PrintStream(response.getOutputStream());
                t.printStackTrace(ps);
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        }
    }

    public static String getStackTrace(Throwable t) {
        String stackTrace = null;
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            sw.close();
            stackTrace = sw.getBuffer().toString();
        } catch (Exception ex) {
        }
        return stackTrace;
    }

    public void log(String msg) {
        System.out.println(msg);
    }

    class ResponseWrapper extends HttpServletResponseWrapper {

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }
    }

    class RequestWrapper extends HttpServletRequestWrapper {

        private Map headerMap;
        private HashMap params = new HashMap();
        private String authorization;

        public RequestWrapper(HttpServletRequest request) {
            super(request);
            headerMap = new HashMap();
            authorization = null;
        }

        public void setAutorizationHeader(String value) {
            authorization = value;
        }

        @Override
        public Enumeration getHeaderNames() {

            HttpServletRequest request = (HttpServletRequest) getRequest();

            List list = new ArrayList();

            boolean hasAuth = false;

            for (Enumeration e = request.getHeaderNames(); e.hasMoreElements();) {
                String h = e.nextElement().toString();
                if (h.equalsIgnoreCase("Authorization")) {
                    hasAuth = true;
                }
                //System.out.println("  > " + h);
                list.add(h);
            }

            for (Iterator i = headerMap.keySet().iterator(); i.hasNext();) {
                list.add(i.next());
            }

            if (!hasAuth && authorization != null) {
                list.add("Authorization");
            }

            return Collections.enumeration(list);
        }

        @Override
        public String getHeader(String name) {
            if (name.equalsIgnoreCase("Authorization") && authorization != null) {
                return authorization;
            }

            Object value;
            String ret = null;

            if ((value = headerMap.get("" + name)) != null) {
                ret = value.toString();
                return ret;
            } else {
                ret = ((HttpServletRequest) getRequest()).getHeader(name);
                return ret;
            }

        }
    }
}
