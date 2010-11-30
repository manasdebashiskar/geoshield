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

import ch.supsi.ist.geoshield.data.Groups;
import ch.supsi.ist.geoshield.data.Layers;
import ch.supsi.ist.geoshield.data.LayersPermissions;
import ch.supsi.ist.geoshield.data.ServicesUrls;
import ch.supsi.ist.geoshield.data.Users;
import ch.supsi.ist.geoshield.exception.ServiceException;
import ch.supsi.ist.geoshield.utils.Utility;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */
public class GeoShield implements Filter {

    private static final boolean debug = false;
    private static final boolean debugMillisec = false;
    private static final String GS_SERVICE = "GEOSHIELD";

    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured. 
    private FilterConfig filterConfig = null;

    public GeoShield() {
    }

    private void doBeforeProcessing(RequestWrapper request, ResponseWrapper response)
            throws IOException, ServletException, ServiceException {
        if (debug) {
            System.out.println("GeoShield (BEFORE-PROCESSING): ");
        }

        //DataManager dm = new DataManager();

        Throwable problem = null;
        try {
            // ************************************************************
            // Looking for service instance
            // ************************************************************
            String service = Utility.getHttpParam("SERVICE", request);
            if (service == null) {
                // @todo set the exception in a throwable object that will be thrown after dataManager is closed
                return;
                //throw new ServiceException("Parameter name 'Service' is mandatory.");
            }
            if (debug) {
                System.out.println("Looking for SERVICE parameter: " + service);
            }

            // ************************************************************
            // EXECUTE THIS FILTER ONLY WITH WMS SERVICES !!!!!!!!!!!!!!!!!
            // ************************************************************
            if (service.equalsIgnoreCase(GS_SERVICE)) {

                // ************************************************************
                // Check user request
                // ************************************************************
                String requestParam = Utility.getHttpParam("REQUEST", request);
                if (requestParam == null) {
                    throw new ServiceException("Parameter name 'Request' is mandatory.");
                }

                // Geoshild options check
                if (requestParam.equalsIgnoreCase("GSGETLAYERS")) {
                    // Return JSON user's layers list
                    Map<ServicesUrls, List<Layers>> surList = new HashMap<ServicesUrls, List<Layers>>();

                    //Set<ServicesUrls> surList = new HashSet<ServicesUrls>();
                    List<Layers> layList = new ArrayList<Layers>();

                    //System.out.println("Looking 4 user.. ");
                    Users usr = (Users) request.getSession().getAttribute("user");
                    //System.out.println("Found user: " + usr.getNameUsr());
                    List<Groups> grList = usr.getGroups();
                    for (Iterator<Groups> it = grList.iterator(); it.hasNext();) {
                        Groups grp = it.next();
                        for (Iterator<LayersPermissions> it2 = grp.getLayersPermissionsCollection().iterator(); it2.hasNext();) {
                            LayersPermissions lpr = it2.next();
                            //layList.add(lpr.getIdLayFk());

                            ServicesUrls sur = lpr.getIdLayFk().getIdSurFk();
                            if (!surList.containsKey(sur)) {
                                surList.put(sur, new ArrayList<Layers>());
                            }
                            surList.get(sur).add(lpr.getIdLayFk());



                        //surList.add(lpr.getIdLayFk().getIdSurFk());
                        }
                    }
                    String json = "{usrPrm:[";
                    json += "\n";
                    for (Iterator<ServicesUrls> it = surList.keySet().iterator(); it.hasNext();) {
                        ServicesUrls sur = it.next();
                        json += " {";
                        json += "\n";
                        json += "  surl: \"" + sur.getPathSur() + "\",";
                        json += "\n";
                        json += "  service: \"" + sur.getIdSrvFk().getNameSrv() + "\",";
                        json += "\n";
                        json += "  layers: [";
                        json += "\n";
                        for (Iterator<Layers> layIt = surList.get(sur).iterator(); layIt.hasNext();) {
                            Layers lay = layIt.next();
                            if (lay.getIdSurFk().equals(sur)) {
                                json += "   {";
                                json += "\n";
                                json += "    name: \"" + lay.getNameLay() + "\"";
                                json += "\n";
                                json += "   }";
                                if (layIt.hasNext()) {
                                    json += ",";
                                }
                                json += "\n";
                            }
                        }
                        json += "  ]";
                        json += "\n";
                        json += " }";
                        if (it.hasNext()) {
                            json += ",";
                        }
                        json += "\n";
                    }
                    json += "]}";
                    //System.out.println("*************************************\n" + json);
                    PrintWriter out = response.getWriter();
                    out.print(json);
                    out.close();
                } else if (requestParam.equalsIgnoreCase("LOGIN")) {
                    Users usr = (Users) request.getSession().getAttribute("user");
                    //System.out.println("Found user: " + usr.getNameUsr());
                    PrintWriter out = response.getWriter();
                    if (usr != null) {
                        out.print("User ok!");
                    } else {
                        out.print("User Ko!");
                    }
                    out.close();
                }
                throw new ch.supsi.ist.geoshield.exception.ServiceException();
            }
        } catch (ch.supsi.ist.geoshield.exception.ServiceException serviceException) {
            if (debug) {
                log("WmsShield: launching exception.");
            }
            problem = serviceException;
        } finally {
            // Closing the connection o database
            //dm.close();
            if (problem != null) {
                throw (ch.supsi.ist.geoshield.exception.ServiceException) problem;
            }
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
            System.out.println("\n**********************************************************");
            System.out.println("ServiceAuthority: checking user's autorization on service.");
            System.out.println("**********************************************************");
        }

        RequestWrapper wrappedRequest = new RequestWrapper((HttpServletRequest) request);
        ResponseWrapper wrappedResponse = new ResponseWrapper((HttpServletResponse) response);



        Throwable problem = null;

        // @todo handle exception with OWS exception Schema see (http://schemas.opengis.net/ows/)
        long milli = 0;
        try {
            milli = Utility.getMillis();
            doBeforeProcessing(wrappedRequest, wrappedResponse);
            if (debugMillisec) {
                milli = Utility.getMillis() - milli;
                System.out.println(" GeoShield >>> " + milli);
            }
            chain.doFilter(wrappedRequest, wrappedResponse);
        } catch (ServiceException t) {
            if (debug) {
                System.out.println(" - Error: " + t.getMessage());
            }
            //if (t.getCode().equals(t.INVALID_SERVICE_PARAMETER)) {
            wrappedResponse.sendError(ResponseWrapper.SC_NOT_FOUND, t.getMessage());
            response.getWriter().close();
        } catch (Throwable t) {
            problem = t;
            t.printStackTrace();
        }
        //doAfterProcessing(wrappedRequest, wrappedResponse);

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
                log("GeoShield: Initializing filter");
            }
        }
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("GeoShield()");
        }
        StringBuffer sb = new StringBuffer("GeoShield(");
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
