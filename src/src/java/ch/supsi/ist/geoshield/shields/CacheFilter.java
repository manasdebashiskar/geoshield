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

import ch.supsi.ist.geoshield.data.DataManager;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Milan P. Antonovic - Istituto Scienze della Terra, SUPSI
 */
public class CacheFilter implements Filter {

    // Attribute names used to store objects interface the request
    public static final String GEOSHIELD_CACHE = "GEOSHIELD_CACHE";
    public static final String GEOSHIELD_CACHE_RESYNC_TIMEOUT = "CACHE_RESYNC";
    public static final String GEOSHIELD_CACHE_LAST_RESYNC = "LAST_CACHE_RESYNC";
    public static final String GEOSHIELD_DATAMANAGER = "GEOSHIELD_DATAMANAGER";
    
    public static final String GEOSHIELD_REALM = "GEOSHIELD_REALM";
    
    private static final boolean debug = true;
    private Long resyncTimeOut;
    private Long lastResyncTime;
    private FilterConfig filterConfig = null;
    private boolean resynNeeded = false;
    private String realm = "GeoShield";
    /*
     * geoshieldCache structure:
     * {
     *      user: {
     *          service: 
     *      }
     * 
     */
    private HashMap<String, Map<String, Object>> geoshieldCache;
    private DataManager dm;

    public CacheFilter() {
    }

    private void doBeforeProcessing(RequestWrapper request, ServletResponse response)
            throws IOException, ServletException {
        request.setAttribute(GEOSHIELD_CACHE, this.geoshieldCache);
        request.setAttribute(GEOSHIELD_CACHE_RESYNC_TIMEOUT, this.resyncTimeOut);
        request.setAttribute(GEOSHIELD_CACHE_LAST_RESYNC, this.lastResyncTime);
        request.setAttribute(GEOSHIELD_REALM, this.realm);
        synchronized (this) {
            if (CacheFilterUtils.resyncNeeded(request) && !resynNeeded) {
                this.dm.recreate();
                System.out.println(" > Recreating DataManager");
                resynNeeded = true;
            }
            request.setAttribute(GEOSHIELD_DATAMANAGER, this.dm);
        }
    }

    private void doAfterProcessing(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        /*Logger.getLogger(CacheFilter.class.getName()).log(
        Level.INFO,
        "DoAfterProcessing");*/
        if (resynNeeded) {
            resynNeeded = false;
            this.lastResyncTime = new Long(Calendar.getInstance().getTimeInMillis());
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {
        RequestWrapper wrappedRequest = new RequestWrapper((HttpServletRequest) request);
        Throwable problem = null;
        try {
            doBeforeProcessing(wrappedRequest, response);
            chain.doFilter(request, response);
            doAfterProcessing(wrappedRequest, response);
        } catch (Throwable t) {
            problem = t;
            t.printStackTrace();
        }

        //doAfterProcessing(request, response);

        if (problem != null) {
            if (problem instanceof ServletException) {
                throw (ServletException) problem;
            }
            if (problem instanceof IOException) {
                throw (IOException) problem;
            }
            sendProcessingError(problem, response);
        }

    }

    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    @Override
    public void destroy() {
        if (this.dm != null) {
            this.dm.closeIt();
        }
        this.geoshieldCache = null;
        this.dm = null;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if (filterConfig != null) {

            Logger.getLogger(CacheFilter.class.getName()).log(
                    Level.INFO,
                    "CacheFilter: initializing cacheFilter");

            this.resyncTimeOut = Long.decode(this.filterConfig.getInitParameter(GEOSHIELD_CACHE_RESYNC_TIMEOUT));
            
            //GEOSHIELD_REALM
            
            realm = filterConfig.getServletContext().getInitParameter(GEOSHIELD_REALM);
            
        }
        this.geoshieldCache = new HashMap<String, Map<String, Object>>();
        this.lastResyncTime = new Long(Calendar.getInstance().getTimeInMillis());
        this.dm = new DataManager();
    }

    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("CacheFilter()");
        }
        StringBuffer sb = new StringBuffer("CacheFilter(");
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
