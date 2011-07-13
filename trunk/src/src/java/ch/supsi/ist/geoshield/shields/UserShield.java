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
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */

public class UserShield implements Filter {

    private static final int loginAttemp = 3;
    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured. 
    private FilterConfig filterConfig = null;

    public UserShield() {
    }

    private void doBeforeProcessing(RequestWrapper req, ResponseWrapper res)
            throws IOException, ServletException, UserException {

        // @todo check if channel is secured
        // request.isSecure();
        /*System.out.println("\n--------------------------------");
        System.out.println("Headers summary:");
        Enumeration h = req.getHeaderNames();
        while (h.hasMoreElements()) {
            String object = (String)h.nextElement();
            System.out.println(object + " " + req.getHeader(object));
        }
        System.out.println("--------------------------------\n");*/
        
        String path = req.getPathInfo();

        // If asked a public url it is not needed to authenticate
        if (path.equalsIgnoreCase("/public")) {
            Users usr = null;
            try {
                usr = (Users) req.getSession().getAttribute("publicUser");
            } catch (IllegalStateException e) {
                System.err.println(e.toString());
                usr = null;
            }
            if (usr == null) {
                DataManager dm = new DataManager();
                usr = dm.getUser("public");
                req.getSession().setAttribute("publicUser", usr);
                dm.close();
            }
        } else {

            Users usr = null;

            // Refresh user on request after one minute
            try {
                Long milli = null;
                milli = (Long) req.getSession().getAttribute("refreshUser");
                if (milli == null) {
                    req.getSession().setAttribute("refreshUser", new Long(Utility.getMillis()));
                } else {
                    if ((new Long(Utility.getMillis()) - milli) < 60000) {
                        usr = (Users) req.getSession().getAttribute("user");
                    }else{
                        req.getSession().setAttribute("refreshUser", new Long(Utility.getMillis()));
                    }
                }
            } catch (IllegalStateException e) {
                System.err.println(e.toString());
            }

            if (usr == null) {
                HttpSession session = null;

                if (req.getSession().isNew()) {
                    session = req.getSession(true);
                } else {
                    session = req.getSession();
                }

                try {
                    if (req.getSession().getAttribute("datamanager") == null) {
                        req.getSession().setAttribute("datamanager", new DataManager());
                    }else{
                        DataManager dm = (DataManager)req.getSession().getAttribute("datamanager");
                        if(!dm.isOpen()){
                            dm.recreate();
                            System.out.println("Recreation of new entity manager..");
                        }
                    }
                } catch (IllegalStateException e) {
                    System.err.println(e.toString());
                }

                //String auth = req.getHeader("Authorization");
                AuthorityManager am = new AuthorityManager();
                usr = am.WWWAuthenticate(req);

                am.close();
                if (usr == null) {
                    System.out.println("User is NULL");
                    if (session.getAttribute("login") == null) {
                        session.setAttribute("login", new Integer(1));
                    }
                    if (((Integer) session.getAttribute("login")) < UserShield.loginAttemp) {
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
                    System.out.println("User is " + usr.getLastNameUsr());
                    session.setAttribute("user", usr);
                    session.setAttribute("login", new Integer(1));
                }
            }
            // User exist but is no more active
            if (!usr.getIsActiveUsr().booleanValue()) {
                req.getSession().invalidate();
                res.sendRedirect("/istShield/error/userExpired.jsp");
                return;
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
                wrappedResponse.setHeader("WWW-Authenticate", "BASIC realm=\"GeoShield: Istituto Scienze della Terra SUPSI\"");
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
    }

    /**
     * Init method for this filter 
     */
    @Override
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
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
