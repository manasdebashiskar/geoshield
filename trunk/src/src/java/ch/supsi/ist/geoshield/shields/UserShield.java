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
package ch.supsi.ist.geoshield.shields;

import ch.supsi.ist.geoshield.auth.AuthorityManager;
import ch.supsi.ist.geoshield.data.DataManager;
import ch.supsi.ist.geoshield.data.Users;
import ch.supsi.ist.geoshield.exception.UserException;
import ch.supsi.ist.geoshield.utils.Utility;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Milan P. Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */
public class UserShield implements Filter {

    private static final int loginAttemp = 3;
    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured. 
    private FilterConfig filterConfig = null;
    private HashMap<String, Users> usersCache;

    public UserShield() {
    }

    private void doBeforeProcessing(RequestWrapper req, ResponseWrapper res)
            throws IOException, ServletException, UserException {

        // @todo check if channel is secured

        String path = req.getPathInfo();

        DataManager cdm = CacheFilterUtils.getDataManagerCached(req);

        // If asked a public url, authentication is not needed
        // @todo recode this public user access mess
        if (path.equalsIgnoreCase("/public")) {
            Users usr = null;
            try {
                usr = (Users) req.getSession().getAttribute("publicUser");
            } catch (IllegalStateException e) {
                System.err.println(e.toString());
                usr = null;
            }
            if (usr == null) {
                usr = cdm.getUser("public");
                req.setAttribute("publicUser", usr);
                req.getSession().setAttribute("publicUser", usr);
            }
        } else {
            // Reading the authorization header to get user and password
            String authHeader = req.getHeader("Authorization");
            Users usr = null;

            if (authHeader != null) {
                if (CacheFilterUtils.resyncNeeded(req)) {
                    
                    AuthorityManager am = new AuthorityManager();
                    usr = am.WWWAuthenticate(req);
                    usersCache.put(authHeader, usr);
                } else {
                    // Check if user exist interface filter cache
                    if (usersCache.containsKey(authHeader)) {
                        usr = usersCache.get(authHeader);
                    } else {
                        AuthorityManager am = new AuthorityManager();
                        usr = am.WWWAuthenticate(req);
                        usersCache.put(authHeader, usr);
                    }
                }
            }

            if (usr == null) {
                throw new UserException(
                        "Sorry! invalid user-name or password.",
                        UserException.LOGIN_ATTEMPT_FAILED);
            } else {
                if (!usr.getIsActiveUsr().booleanValue()) {
                    res.sendRedirect("/istShield/error/userExpired.jsp");
                } else {
                    req.setAttribute("user", usr);
                }
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        RequestWrapper wrappedRequest = new RequestWrapper((HttpServletRequest) request);
        ResponseWrapper wrappedResponse = new ResponseWrapper((HttpServletResponse) response);

        try {
            doBeforeProcessing(wrappedRequest, wrappedResponse);
            chain.doFilter(wrappedRequest, wrappedResponse);
        } catch (UserException t) {
            if (((UserException) t).getCode().equals(UserException.INVALID_USER_OR_PASSWORD)) {
                wrappedResponse.sendError(ResponseWrapper.SC_FORBIDDEN);
                response.getWriter().close();
            } else if (((UserException) t).getCode().equals(UserException.LOGIN_ATTEMPT_FAILED)) {
                wrappedResponse.setHeader("WWW-Authenticate", "BASIC realm=\"GeoShield Realm\"");
                wrappedResponse.sendError(ResponseWrapper.SC_UNAUTHORIZED);
                response.getWriter().close();
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
    @Override
    public void destroy() {
        this.usersCache = null;
    }

    /**
     * Init method for this filter 
     */
    @Override
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        this.usersCache = new HashMap<String, Users>();
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("UserAuthority()");
        }
        StringBuffer sb = new StringBuffer("UserAuthority(");
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
}
