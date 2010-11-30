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

package ch.supsi.ist.geoshield.shields.app;

import ch.supsi.ist.geoshield.auth.AuthorityManager;
import ch.supsi.ist.geoshield.data.Applications;
import ch.supsi.ist.geoshield.data.DataManager;
import ch.supsi.ist.geoshield.data.Users;
import ch.supsi.ist.geoshield.exception.AppsException;
import ch.supsi.ist.geoshield.exception.ServiceException;
import ch.supsi.ist.geoshield.exception.UserException;
import ch.supsi.ist.geoshield.utils.Utility;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.persistence.NoResultException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */

public class AppsShield implements Filter {

    private static final boolean debug = true;
    private static final int loginAttemp = 2;

    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured. 
    private FilterConfig filterConfig = null;

    public AppsShield() {
    }

    private void doBeforeProcessing(RequestWrapper req, ResponseWrapper response)
            throws IOException, ServletException, UserException, AppsException, ServiceException {

        if (debug) {
            System.out.println("\n********************* FILTER: AppsShield (Before processing) *****************");
        }

        String vpath = Utility.getVpath(req);

        if (debug) {
            System.out.println(" Virtual path: " + vpath);
        }

        DataManager dm = new DataManager();

        Applications aps = null;
        try {
            aps = dm.getApplicationByVpath(vpath);
            req.getSession().setAttribute("apps", aps);
            HttpSession session = null;
            dm.close();
            if (!aps.getIsPublicAps()) {

                Users usr = null;
                try {
                    // Check if User param exist in session object
                    usr = (Users) req.getSession().getAttribute("user");
                } catch (IllegalStateException e) {
                    System.err.println(e.toString());
                    usr = null;
                }
                if (usr == null) {

                    if (req.getSession().isNew()) {
                        System.out.println("Nuova sessione!");
                        session = req.getSession(true);
                    } else {
                        System.out.println("Sessione esistente!");
                        session = req.getSession();
                    }

                    // Try to validate user against authorization header passed (if passed)
                    String auth = req.getHeader("Authorization");
                    AuthorityManager am = new AuthorityManager();
                    usr = am.WWWAuthenticate(req);
                    am.close();
                    if (usr == null) {
                        if (session.getAttribute("login") == null) {
                            session.setAttribute("login", new Integer(1));
                        }
                        if (debug) {
                            System.out.println(" - Attempt " + session.getAttribute("login") + "/" + loginAttemp);
                        }
                        if (((Integer) session.getAttribute("login")) < loginAttemp) {
                            session.setAttribute("login",
                                    ((Integer) session.getAttribute("login")) + 1);
                            throw new UserException(
                                    "Sorry! invalid user-name or password.",
                                    UserException.LOGIN_ATTEMPT_FAILED);
                        } else {
                            session.setAttribute("login", new Integer(1));
                            throw new UserException(
                                    "Sorry! invalid user-name or password.",
                                    UserException.INVALID_USER_OR_PASSWORD);
                        }
                    } else {
                        session.setAttribute("user", usr);
                        session.setAttribute("login", new Integer(1));
                    }

                }
                if (!usr.getIsActiveUsr()) {
                    response.sendRedirect("/istShield/error/userExpired.jsp");
                    return;
                }


                try {
                    Calendar lur = (Calendar) req.getSession().getAttribute("lastUserRefresh");
                    if (lur == null) {
                        session.setAttribute("lastUserRefresh", Calendar.getInstance());
                    } else {
                        Calendar next = Calendar.getInstance();
                        //next.setTimeInMillis(lur.getTimeInMillis()+60000); // 1 minuto
                        next.setTimeInMillis(lur.getTimeInMillis() + 10000); // 1 minuto
                        System.out.println(" > lastUserRefresh: " + lur.getTime().toString());
                        System.out.println(" > next: " + next.getTime().toString());
                        if (lur.after(next)) {
                            if (debug) {
                                System.out.println("Refreshing Users object");
                            }
                            dm.refresh(usr);
                            session.setAttribute("lastUserRefresh", Calendar.getInstance());
                        } else {
                            System.out.println("Leaving cached Users object");
                        }
                    }
                } catch (IllegalStateException e) {
                    System.err.println(e.toString());
                }

                AuthorityManager am = new AuthorityManager();
                // Check user permissions on requested app
                if (debug) {
                    System.out.print("Checking users permission on application '" + aps.getNameAps() + "' requested: ");
                }
                if (am.checkUsrAuthOnApp(usr, aps)) {
                    if (debug) {
                        System.out.println(" [OK]");
                    }
                } else {
                    if (debug) {
                        System.out.println(" [ERROR]");
                    }
                    throw new AppsException("User '" + usr.getLastNameUsr() +
                            " " + usr.getNameUsr() + "' is not authorized to access '" + vpath + "' not found",
                            AppsException.USER_NOT_AUTHORIZED);
                }
            }

            if (debug) {
                System.out.println("\n User is requesting:");
                System.out.println(" - name: " + aps.getNameAps());
                System.out.println(" - desc: " + aps.getDescAps());
                System.out.println(" - host: " + aps.getHostAps());
                System.out.println(" - port: " + String.valueOf(aps.getPortAps()));
                System.out.println(" - path: " + aps.getPathAps());
                System.out.println(" - publ: " + aps.getIsPublicAps());
            }

        } catch (NoResultException noResultException) {
            dm.close();
            System.out.println(" Application not found..");
            throw new AppsException("Application " + vpath + " not found",
                    AppsException.APPS_NOT_FOUND);
        }

    }

    private void doAfterProcessing(RequestWrapper request, ResponseWrapper response)
            throws IOException, ServletException {
        if (debug) {
            log("AppsShield:DoAfterProcessing");
        }
    }

    /**
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        if (debug) {
            log("AppsShield:doFilter()");
        }

        // Create wrappers for the request and response objects.
        // Using these, you can extend the capabilities of the
        // request and response, for example, allow setting parameters
        // on the request before sending the request to the rest of the filter chain,
        // or keep track of the cookies that are set on the response.
        //
        // Caveat: some servers do not handle wrappers very well for forward or
        // include requests.
        RequestWrapper wrappedRequest = new RequestWrapper((HttpServletRequest) request);
        ResponseWrapper wrappedResponse = new ResponseWrapper((HttpServletResponse) response);
        try {
            doBeforeProcessing(wrappedRequest, wrappedResponse);
            chain.doFilter(wrappedRequest, wrappedResponse);
        } catch (UserException t) {
            if (debug) {
                System.out.println(" - " + t.getMessage());
            }
            if (((UserException) t).getCode().equals(UserException.INVALID_USER_OR_PASSWORD)) {
                if (debug) {
                    System.out.println(" - Logins attempt expired.");
                }
                wrappedResponse.sendError(ResponseWrapper.SC_FORBIDDEN);
                response.getWriter().close();
            } else if (((UserException) t).getCode().equals(UserException.USER_EXPIRED)) {
                if (debug) {
                    System.out.println(" - User expired.");
                }
                wrappedResponse.sendError(ResponseWrapper.SC_FORBIDDEN);
                response.getWriter().close();
            } else if (((UserException) t).getCode().equals(UserException.LOGIN_ATTEMPT_FAILED)) {
                if (debug) {
                    System.out.println(" - Try again.");
                }
                wrappedResponse.setHeader("WWW-Authenticate", "BASIC realm=\"Istituto Scienze della Terra\"");
                wrappedResponse.sendError(ResponseWrapper.SC_UNAUTHORIZED);
                response.getWriter().close();
            }
        } catch (AppsException t) {
            if (((AppsException) t).getCode().equals(AppsException.APPS_NOT_FOUND)) {
                if (debug) {
                    System.out.println(" - " + t.getMessage());
                }
                wrappedResponse.sendError(ResponseWrapper.SC_NOT_FOUND);
                response.getWriter().close();
            } else if (((AppsException) t).getCode().equals(AppsException.USER_NOT_AUTHORIZED)) {
                // TODO REDIRECT
                if (debug) {
                    System.out.println(" - " + t.getMessage());
                }
                wrappedResponse.sendError(ResponseWrapper.SC_NOT_FOUND);
                response.getWriter().close();
            }

        } catch (Throwable t) {
            if (debug) {
                System.out.println(" - " + t.getMessage());
            }
            t.printStackTrace();
            if (t instanceof ServletException) {
                throw (ServletException) t;
            }
            if (t instanceof IOException) {
                throw (IOException) t;
            }
            sendProcessingError(t, response);
        }
    }

    /**
     * Return the filter configuration object for this filter.
     */
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /**
     * Destroy method for this filter 
     */
    public void destroy() {
    }

    /**
     * Init method for this filter 
     */
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            if (debug) {
                log("AppsShield: Initializing filter");
            }
        }
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("AppsShield()");
        }
        StringBuffer sb = new StringBuffer("AppsShield(");
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
        filterConfig.getServletContext().log(msg);
    }

    /**
     * This request wrapper class extends the support class HttpServletRequestWrapper,
     * which implements all the methods in the HttpServletRequest interface, as
     * delegations to the wrapped request. 
     * You only need to override the methods that you need to change.
     * You can get access to the wrapped request using the method getRequest()
     */
    class RequestWrapper extends HttpServletRequestWrapper {

        public RequestWrapper(HttpServletRequest request) {
            super(request);
        }

        // You might, for example, wish to add a setParameter() method. To do this
        // you must also override the getParameter, getParameterValues, getParameterMap,
        // and getParameterNames methods.
        protected Hashtable localParams = null;

        public void setParameter(String name, String[] values) {
            if (debug) {
                System.out.println("AppsShield::setParameter(" + name + "=" + values + ")" + " localParams = " + localParams);
            }

            if (localParams == null) {
                localParams = new Hashtable();
                // Copy the parameters from the underlying request.
                Map wrappedParams = getRequest().getParameterMap();
                Set keySet = wrappedParams.keySet();
                for (Iterator it = keySet.iterator(); it.hasNext();) {
                    Object key = it.next();
                    Object value = wrappedParams.get(key);
                    localParams.put(key, value);
                }
            }
            localParams.put(name, values);
        }

        @Override
        public String getParameter(String name) {
            if (debug) {
                System.out.println("AppsShield::getParameter(" + name + ") localParams = " + localParams);
            }
            if (localParams == null) {
                return getRequest().getParameter(name);
            }
            Object val = localParams.get(name);
            if (val instanceof String) {
                return (String) val;
            }
            if (val instanceof String[]) {
                String[] values = (String[]) val;
                return values[0];
            }
            return (val == null ? null : val.toString());
        }

        @Override
        public String[] getParameterValues(String name) {
            if (debug) {
                System.out.println("AppsShield::getParameterValues(" + name + ") localParams = " + localParams);
            }
            if (localParams == null) {
                return getRequest().getParameterValues(name);
            }
            return (String[]) localParams.get(name);
        }

        @Override
        public Enumeration getParameterNames() {
            if (debug) {
                System.out.println("AppsShield::getParameterNames() localParams = " + localParams);
            }
            if (localParams == null) {
                return getRequest().getParameterNames();
            }
            return localParams.keys();
        }

        @Override
        public Map getParameterMap() {
            if (debug) {
                System.out.println("AppsShield::getParameterMap() localParams = " + localParams);
            }
            if (localParams == null) {
                return getRequest().getParameterMap();
            }
            return localParams;
        }
    }

    /**
     * This response wrapper class extends the support class HttpServletResponseWrapper,
     * which implements all the methods in the HttpServletResponse interface, as
     * delegations to the wrapped response. 
     * You only need to override the methods that you need to change.
     * You can get access to the wrapped response using the method getResponse()
     */
    class ResponseWrapper extends HttpServletResponseWrapper {

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        // You might, for example, wish to know what cookies were set on the response
        // as it went throught the filter chain. Since HttpServletRequest doesn't
        // have a get cookies method, we will need to store them locally as they
        // are being set.
	/*
        protected Vector cookies = null;

        // Create a new method that doesn't exist in HttpServletResponse
        public Enumeration getCookies() {
        if (cookies == null)
        cookies = new Vector();
        return cookies.elements();
        }

        // Override this method from HttpServletResponse to keep track
        // of cookies locally as well as in the wrapped response.
        public void addCookie (Cookie cookie) {
        if (cookies == null)
        cookies = new Vector();
        cookies.add(cookie);
        ((HttpServletResponse)getResponse()).addCookie(cookie);
        }
         */
    }
}
