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
package ch.supsi.ist.geoshield.sos;

import ch.supsi.ist.geoshield.auth.AuthorityManager;
import ch.supsi.ist.geoshield.auth.FilterAuth;
import ch.supsi.ist.geoshield.data.DataManager;
import ch.supsi.ist.geoshield.data.Layers;
import ch.supsi.ist.geoshield.data.Requests;
import ch.supsi.ist.geoshield.data.ServicesUrls;
import ch.supsi.ist.geoshield.data.Users;
import ch.supsi.ist.geoshield.exception.ServiceException;
import ch.supsi.ist.geoshield.exception.UserException;
import ch.supsi.ist.geoshield.parser.OGCParser;
import ch.supsi.ist.geoshield.shields.RequestWrapper;
import ch.supsi.ist.geoshield.shields.ResponseWrapper;
import ch.supsi.ist.geoshield.utils.Utility;
import ch.supsi.ist.geoshield.utils.xml.XmlUtils;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.wfs.DescribeFeatureTypeType;
import net.opengis.wfs.FeatureTypeListType;
import net.opengis.wfs.GetCapabilitiesType;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.QueryType;
import net.opengis.wfs.impl.FeatureCollectionTypeImpl;
import net.opengis.wfs.impl.FeatureTypeTypeImpl;
import net.opengis.wfs.impl.GetFeatureTypeImpl;
import net.opengis.wfs.impl.WFSCapabilitiesTypeImpl;
import org.eclipse.emf.common.util.EList;
//import org.geotools.wfs.WFS;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
//import org.geotools.wfs.v1_1.WFS;
import org.geotools.xml.Configuration;
import org.geotools.xml.DOMParser;
import org.opengis.feature.Feature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */
public class SOSShield implements Filter {

    private FilterConfig filterConfig = null;
    private Users usr = null;
    private List<Layers> lays = null;
    private List<org.opengis.filter.Filter> permissionFilter = null;

    public SOSShield() {
    }

    private synchronized void doBeforeProcessing(RequestWrapper request, ResponseWrapper response)
            throws IOException, ServletException, ServiceException, UserException {

        System.out.println(" > SOSShield: doBeforeProcessing");
        if (request.getMethod().equalsIgnoreCase("GET")) {
            this.handleGetRequest(request, response);
        } else if (request.getMethod().equalsIgnoreCase("POST")) {
            throw new ServiceException("POST method is not supported");

        }
    }

    private void handleGetRequest(RequestWrapper request, ResponseWrapper response) throws IOException, ServiceException, UserException {

        System.out.println(" > SOSShield");
        DataManager dm = Utility.getDmSession(request);
        AuthorityManager am = new AuthorityManager();

        String path = request.getPathInfo();

        // Controllo se il path è stato richiesto (path!=null)

        ServicesUrls sur = dm.getServicesUrlsByPathIdSrv(path, "SOS");
        usr = (Users) request.getSession().getAttribute("user");

        // Check access on serviceUrl for user

        HashMap<String, String> obj =
                (HashMap<String, String>) request.getSession().getAttribute(OGCParser.OBJREQ);

        Requests req = dm.getRequestByNameReqNameSrv(obj.get("REQUEST"), "SOS");
        if (!am.checkUsrAuthOnSrvSurReq(usr, sur, req)) {
            throw new ch.supsi.ist.geoshield.exception.ServiceException("User " + usr.getNameUsr() + " is not authorized to make"
                    + " REQUEST '" + request + "' on SERVICE 'sos' for the given "
                    + " PATH '" + path + "'.", ServiceException.SERVICE_AUTHENTICATION);
        }
        if (!obj.get("REQUEST").equalsIgnoreCase("GETCAPABILITIES")) {
            if (req.getNameReq().equalsIgnoreCase("GETOBSERVATION")) {
                // Check if user has access to the requested offering
                
            } else if (req.getNameReq().equalsIgnoreCase("DESCRIBESENSOR")) {
                // Check if user has access to the requested procedure/sensor
                
                // It will be necessary to make a getcapabilities (put it in session?)
                // to check if procedure is member of the offering that the user has access
                
            }
        }
        System.out.println("   > end");
    }

    private void doAfterProcessing(RequestWrapper request, ResponseWrapper response)
            throws IOException, ServletException, ServiceException, UserException {

        if (Utility.getHttpParam("REQUEST", request).equalsIgnoreCase("GETCAPABILITIES")) {
            // Vedi WMSShield.java
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
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        System.out.println(" > SOSShield: doFilter");
        long milli = 0;

        RequestWrapper wrappedRequest = new RequestWrapper((HttpServletRequest) request);
        ResponseWrapper wrappedResponse = new ResponseWrapper((HttpServletResponse) response);

        Throwable problem = null;

        UserException uex = (UserException) wrappedRequest.getSession().getAttribute("problem");

        if (uex instanceof UserException && uex.getCode().equals(UserException.INVALID_USER_OR_PASSWORD)) {
            sendProcessingError(uex, response);
        } else {
            try {
                milli = Utility.getMillis();
                doBeforeProcessing(wrappedRequest, wrappedResponse);
                chain.doFilter(wrappedRequest, wrappedResponse);
                milli = Utility.getMillis();
                doAfterProcessing(wrappedRequest, wrappedResponse);
            } catch (ServiceException t) {
                //problem = t;
                wrappedResponse.sendError(ResponseWrapper.SC_NOT_FOUND, t.getMessage());
                throw new ServletException(t.getMessage());
                //response.getWriter().close();

            } catch (UserException t) {
                if (((UserException) t).getCode().equals(UserException.USER_EXPIRED)) {
                    wrappedResponse.sendError(ResponseWrapper.SC_FORBIDDEN);
                    problem = t;
                    //response.getWriter().close();
                }
            } catch (Throwable t) {
                // If an exception is thrown somewhere down the filter chain,
                // we still want to execute our after processing, and then
                // rethrow the problem after that.
                problem = t;
                t.printStackTrace();
            }
        }

        // If there was a problem, we want to rethrow it if it is
        // a known type, otherwise log it.
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
    }

    @Override
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
        }
    }

    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("WfsShield()");
        }

        StringBuffer sb = new StringBuffer("WfsShield(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());

    }

    private void sendProcessingError(Throwable t, ServletResponse response) throws IOException {

        if (t instanceof UserException && ((UserException) t).getCode().equals(UserException.INVALID_USER_OR_PASSWORD)) {
            ResponseWrapper wrappedResponse = new ResponseWrapper((HttpServletResponse) response);
            wrappedResponse.sendError(ResponseWrapper.SC_FORBIDDEN);
            response.getWriter().close();
        }

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

    public static String getStackTrace(
            Throwable t) {
        String stackTrace = null;
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            sw.close();
            stackTrace =
                    sw.getBuffer().toString();
        } catch (Exception ex) {
        }
        return stackTrace;
    }

    public void log(String msg) {
        filterConfig.getServletContext().log(msg);
    }
}
