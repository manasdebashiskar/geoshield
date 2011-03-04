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

package ch.supsi.ist.geoshield.wms;

import ch.supsi.ist.geoshield.auth.AuthorityManager;
import ch.supsi.ist.geoshield.auth.FilterAuth;
import ch.supsi.ist.geoshield.auth.WmsAuth;
import ch.supsi.ist.geoshield.data.DataManager;
import ch.supsi.ist.geoshield.data.Layers;
import ch.supsi.ist.geoshield.data.Requests;
import ch.supsi.ist.geoshield.data.Services;
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
import java.util.ArrayList;
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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */
public class WMSShield implements Filter {

    private DataManager dm = null;
    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured. 
    private FilterConfig filterConfig = null;
    private Users usr = null;

    public WMSShield() {
    }

    private synchronized void doBeforeProcessing(RequestWrapper request, ResponseWrapper response)
            throws IOException, ServletException, ServiceException, UserException {

        dm = Utility.getDmSession(request);

        Throwable problem = null;
        try {

            // ************************************************************
            // Looking for service instance
            // ************************************************************
            String service = Utility.getHttpParam("SERVICE", request);
            if (service == null) {
                // @todo set the exception in a throwable object that will be thrown after dataManager is closed
                throw new ServiceException("Parameter name 'Service' is mandatory.");
            }
            Services srv = dm.getServiceByName(service);
            String path = request.getPathInfo();

            // ************************************************************
            // Check user request
            // ************************************************************
            String requestParam = Utility.getHttpParam("REQUEST", request);
            if (requestParam == null) {
                throw new ServiceException("Parameter name 'Request' is mandatory.");
            }

            AuthorityManager am = new AuthorityManager();

            ServicesUrls sur = dm.getServicesUrlsByPathIdSrv(request.getPathInfo(), "WMS");
            usr = (Users) request.getSession().getAttribute("user");

            Requests reqs = dm.getRequestByNameReqNameSrv(requestParam, service);
            if (!am.checkUsrAuthOnSrvSurReq(usr, sur, reqs, dm)) {
                throw new ServiceException("User " + usr.getNameUsr() + " is not authorized to make"
                        + " REQUEST '" + request + "' on SERVICE '" + service + "' for the given "
                        + " PATH '" + path + "'.", ServiceException.SERVICE_AUTHENTICATION);
            }

            if (!requestParam.equalsIgnoreCase("GETCAPABILITIES")) {


                // ************************************************************
                // Get requested layers
                // ************************************************************
                //System.out.println("GETTING PARAM LAYER:");
                String layers = Utility.getHttpParam("LAYERS", request);
                // System.out.println(" > Layers: " + layers);
                if (layers == null) {
                    layers = Utility.getHttpParam("LAYER", request);
                    //System.out.println(" > Layer: " + layers);
                    if (layers == null) {
                        throw new ServiceException("Parameter name 'Layers' is mandatory.");
                    }
                }

                // Load the requested layer for the given path/server
                List<Layers> lays = dm.getLayers(path, layers.split(","), "WMS");

                // ************************************************************
                // Looking for user
                // ************************************************************
                usr = null;

                if (path.equalsIgnoreCase("/public")) {
                    usr = (Users) request.getSession().getAttribute("publicUser");
                } else {
                    usr = (Users) request.getSession().getAttribute("user");
                }

                // ************************************************************
                // Check user permission on requested layers
                // ************************************************************
                boolean authOk = am.checkUsrAuthOnLayers(usr, lays);

                if (!authOk) {
                    throw new ch.supsi.ist.geoshield.exception.ServiceException(
                            "User not authorized to access one or some layers requested.",
                            ServiceException.SERVICE_AUTHENTICATION);
                }

                if (!requestParam.equalsIgnoreCase("GetLegendGraphic")) {
                    // ************************************************************
                    // Looking for user's filter(s)
                    // ************************************************************
                    List<org.opengis.filter.Filter> filters = dm.getUserFilters(request);

                    // @todo check for user's ExcludeFilter
                    // FilterAuth.checkExcludeFilters(layers,filters,request);

                    // *************************************************************
                    // APPLY USER LIMITATION
                    // *************************************************************
                    FilterAuth fau = new FilterAuth();

                    if (requestParam.equalsIgnoreCase("GETMAP")) {

                        String xmlOgc = "";

                        //List<org.opengis.filter.Filter> sysFlts = fau.getFilters(user, lays);

                        if (filters == null) {
                            // User hasn't set filters
                            xmlOgc = fau.getXmlStringFilters(fau.getFilters(usr, lays, dm));
                        } else if (filters != null) {
                            xmlOgc = fau.getXmlStringFilters(
                                    fau.mergeUserAndSystemFilters(filters, fau.getFilters(usr, lays, dm)));
                            // Get a new list merging the user filter list with the system filter list

                        }
                        //xmlOgc = StringEscapeUtils.escapeHtml(xmlOgc);
                        request.removeParameter("CQL_FILTER");
                        String[] oneDimArray = {xmlOgc};

                        request.setParameter("FILTER", oneDimArray);

                    } else if (requestParam.equalsIgnoreCase("GETFEATUREINFO")) {

                        // @todo rewrite this !!!
                        WmsAuth wmsAu = new WmsAuth();
                        double x = Double.parseDouble(Utility.getHttpParam("x", request));
                        double y = Double.parseDouble(Utility.getHttpParam("y", request));
                        int h = Integer.parseInt(Utility.getHttpParam("HEIGHT", request));
                        int w = Integer.parseInt(Utility.getHttpParam("WIDTH", request));
                        String bbox = Utility.getHttpParam("BBOX", request);

                        boolean prm = wmsAu.checkGetFeatureInfo(
                                Utility.pixToGeo(x, y, h, w, bbox),
                                fau.getFilters(usr, lays, dm),
                                lays);

                        if (!prm) {
                            throw new ch.supsi.ist.geoshield.exception.ServiceException(
                                    "User not authorized to make GETFEATUREINFO because of filters..",
                                    ServiceException.SERVICE_AUTHENTICATION);
                        }

                    } else {
                        // @todo throw new exception ??
                    }
                }
            }
        } catch (ch.supsi.ist.geoshield.exception.ServiceException serviceException) {
            problem = serviceException;
        } finally {
            // Closing the connection o database
            //dm.close();
            if (problem != null) {
                throw (ch.supsi.ist.geoshield.exception.ServiceException) problem;
            }
        }
    }

    private void doAfterProcessing(RequestWrapper request, ResponseWrapper response)
            throws IOException, ServletException, ServiceException, UserException {

        if (Utility.getHttpParam("REQUEST", request).equalsIgnoreCase("GETCAPABILITIES")) {

            byte[] by = (byte[]) request.getSession().getAttribute(OGCParser.BYTRES);
            request.getSession().setAttribute(OGCParser.BYTRES, null);

            String charset = "UTF-8";
            String body = new String(by, charset);
            //System.out.println(body);

            Document doc;
            try {
                doc = XmlUtils.buildDocument(body);
            } catch (Exception ex) {
                throw new ServiceException(ex.getMessage());
            }
            Element root = doc.getDocumentElement();
            if (root == null) {
                throw new ServiceException("Document has no root element");
            }
            String tag = root.getLocalName();

            NodeList gNl = doc.getElementsByTagName("OnlineResource");
            for (int i = 0; i < gNl.getLength(); i++) {
                Element object = (Element) gNl.item(i);

                String href = object.getAttribute("xlink:href");
                if (href.indexOf("?") != -1) {
                    object.setAttribute("xlink:href", request.getRequestURL().toString()
                            + "?" + href.split("\\?")[1]);
                } else {
                    object.setAttribute("xlink:href", request.getRequestURL().toString());
                }
            }

            // Filtering Capability
            gNl = doc.getElementsByTagName("Capability");
            Element capa = (Element) gNl.item(0);

            //   Filtering requests
            gNl = capa.getElementsByTagName("Request");
            Element reqs = (Element) gNl.item(0);


            //   Filter request permitted on requested service
            ServicesUrls sur = dm.getServicesUrlsByPathIdSrv(request.getPathInfo(), "WMS");
            usr = (Users) request.getSession().getAttribute("user");

            Map<String, Requests> reqsAvailable = dm.getRequestByUsersServiceUrl(usr, sur);
            List<Node> reqsToRemove = new ArrayList<Node>(0);

            for (int i = 0; i < reqs.getChildNodes().getLength(); i++) {
                Node n = reqs.getChildNodes().item(i);
                if (n instanceof org.apache.xerces.dom.DeferredElementNSImpl) {
                    String r = n.getNodeName();
                    if (!reqsAvailable.containsKey(r.toUpperCase())) {
                        reqsToRemove.add(n);
                    }
                }
            }
            for (Iterator<Node> it = reqsToRemove.iterator(); it.hasNext();) {
                reqs.removeChild(it.next());
            }

            //   Filtering getMap Formats
            gNl = reqs.getElementsByTagName("GetMap");
            Element gm = (Element) gNl.item(0);


            gNl = gm.getElementsByTagName("Format");
            List<Element> frmToRemove = new ArrayList<Element>(0);
            for (int i = 0; i < gNl.getLength(); i++) {
                Element frm = (Element) gNl.item(i);
                //System.out.println("frm.getTextContent(): " + frm.getTextContent() + " -> " + frm.getTextContent().indexOf("image"));
                if (frm.getTextContent().indexOf("image") == -1) {
                    //gm.removeChild(frm);
                    frmToRemove.add(frm);
                }
            }
            for (Iterator<Element> it = frmToRemove.iterator(); it.hasNext();) {
                gm.removeChild(it.next());
            }


            //   Filtering getMap Formats
            gNl = capa.getElementsByTagName("GetMap");

            //   Filtering Layer Layers
            gNl = capa.getChildNodes();
            Element lay = null;
            for (int i = 0; i < gNl.getLength(); i++) {
                Node n = gNl.item(i);
                if (n.getNodeName().equalsIgnoreCase("Layer")) {
                    lay = (Element) n;
                    break;
                }
            }
            // Controllo permessi utente
            AuthorityManager am = new AuthorityManager();
            List<Layers> lays = sur.getLayersCollection();//dm.getLayers(request.getPathInfo());
            List<Layers> layPermit = new ArrayList<Layers>(0);

            for (Iterator<Layers> it = lays.iterator(); it.hasNext();) {
                Layers l = it.next();
                if (am.checkUsrVisibilityOnLayer(usr, l)) {
                    layPermit.add(l);
                }
            }

            gNl = lay.getElementsByTagName("Layer");
            List<Element> layToRemove = new ArrayList<Element>(0);

            for (int i = 0; i < gNl.getLength(); i++) {
                Element element = (Element) gNl.item(i);
                String name = element.getElementsByTagName("Name").item(0).getTextContent();
                boolean toRem = true;
                for (Iterator<Layers> it = layPermit.iterator(); it.hasNext();) {
                    Layers layers = it.next();
                    if (name.equalsIgnoreCase(layers.getNameLay())) {
                        toRem = false;
                        layPermit.remove(layers);
                        break;
                    }
                }
                if (toRem) {
                    layToRemove.add(element);
                } else {
                    // Change getLegendGraphic layer definition
                    NodeList rl = element.getElementsByTagName("OnlineResource");
                    for (int f = 0; f < rl.getLength(); f++) {
                        Element or = (Element) rl.item(f);
                        or.setAttribute("xlink:href", request.getRequestURL().toString()
                                + "?" + "request=GetLegendGraphic&format=image/png&width=20&height=20&layer=" + name);
                    }
                }
            }

            for (Iterator<Element> it = layToRemove.iterator(); it.hasNext();) {
                lay.removeChild(it.next());
            }

            request.getSession().setAttribute(OGCParser.BYTRES, XmlUtils.xmlToString(doc).getBytes());
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

        RequestWrapper wrappedRequest = new RequestWrapper((HttpServletRequest) request);
        ResponseWrapper wrappedResponse = new ResponseWrapper((HttpServletResponse) response);

        Throwable problem = null;

        UserException uex = (UserException) wrappedRequest.getSession().getAttribute("problem");

        if (uex instanceof UserException && uex.getCode().equals(UserException.INVALID_USER_OR_PASSWORD)) {
            sendProcessingError(uex, response);
        } else {
            try {
                doBeforeProcessing(wrappedRequest, wrappedResponse);
                chain.doFilter(wrappedRequest, wrappedResponse);
                doAfterProcessing(wrappedRequest, wrappedResponse);
            } catch (ServiceException t) {
                wrappedResponse.sendError(ResponseWrapper.SC_NOT_FOUND, t.getMessage());
                response.getWriter().close();
            } catch (UserException t) {
                if (((UserException) t).getCode().equals(UserException.USER_EXPIRED)) {
                    wrappedResponse.sendError(ResponseWrapper.SC_FORBIDDEN);
                    response.getWriter().close();
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
        }
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("WmsShield()");
        }

        StringBuffer sb = new StringBuffer("WmsShield(");
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
