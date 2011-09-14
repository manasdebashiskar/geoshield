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
import ch.supsi.ist.geoshield.data.DataManager;
import ch.supsi.ist.geoshield.data.Offerings;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */
public class SOSShield implements Filter {

    private FilterConfig filterConfig = null;
    private List<org.opengis.filter.Filter> permissionFilter = null;
    private static final String NS_OWS = "http://www.opengis.net/ows/1.1";
    private static final String NS_SOS = "http://www.opengis.net/sos/1.0";
    private static final String NS_GML = "http://www.opengis.net/gml";

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

    private synchronized void handleGetRequest(RequestWrapper request, ResponseWrapper response)
            throws IOException, ServiceException, UserException {

        System.out.println(" > SOSShield");
        DataManager dm = Utility.getDmSession(request);
        AuthorityManager am = new AuthorityManager();

        String path = request.getPathInfo();

        // Controllo se il path è stato richiesto (path!=null)

        ServicesUrls sur = dm.getServicesUrlsByPathIdSrv(path, "SOS");
        Users usr = (Users) request.getAttribute("user");

        // Check access on serviceUrl for user

        HashMap<String, String> obj =
                (HashMap<String, String>) request.getAttribute(OGCParser.OBJREQ);
        //(HashMap<String, String>) request.getSession().getAttribute(OGCParser.OBJREQ);

        Requests req = dm.getRequestByNameReqNameSrv(obj.get("REQUEST"), "SOS");
        System.out.println(" > Request: " + req.getNameReq());
        System.out.println(" > Sur: " + sur.getUrlSur());

        try {
            if (!am.checkUsrAuthOnSrvSurReq(usr, sur, req)) {
                throw new ch.supsi.ist.geoshield.exception.ServiceException("User " + usr.getNameUsr() + " is not authorized to make"
                        + " REQUEST '" + req.getNameReq() + "' on SERVICE 'sos' for the given "
                        + " PATH '" + path + "'.", ServiceException.SERVICE_AUTHENTICATION);
            }
        } catch (NullPointerException e) {
        }


        List<Offerings> offs = SOSUtils.getOfferings(usr);
        HashSet<String> offerings = SOSUtils.getOfferingsSet(usr);

        if (!obj.get("REQUEST").equalsIgnoreCase("GETCAPABILITIES")) {

            // Ask for procedures that can be requested
            if (req.getNameReq().equalsIgnoreCase("GETOBSERVATION")) {
                // Check if user has access to the requested offering
                String offering = obj.get("OFFERING");

                System.out.println(" OFFERING: " + offering);

                if (offering != null && offerings.contains(offering)) {
                    System.out.println(" > GETOBSERVATION permitted.");
                } else if (offering != null) {
                    throw new ch.supsi.ist.geoshield.exception.ServiceException("User " + usr.getNameUsr() + " is not authorized to make"
                            + " REQUEST '" + req.getNameReq() + "' on SERVICE 'sos' for the given "
                            + " OFFERING '" + offering + "'.", ServiceException.INVALID_SERVICE_PARAMETER);
                } else {
                    throw new ch.supsi.ist.geoshield.exception.ServiceException("Offering parameter is mandatory.", ServiceException.INVALID_SERVICE_PARAMETER);
                }
            } else if (req.getNameReq().equalsIgnoreCase("DESCRIBESENSOR")) {
                HashMap<String, HashSet<String>> tmp = SOSUtils.getInfo(SOSUtils.getGetCapabilitiesDocument(sur), offs);
                if (!tmp.get("procs").contains(obj.get("PROCEDURE"))) {
                    throw new ch.supsi.ist.geoshield.exception.ServiceException("User " + usr.getNameUsr() + " is not authorized to make"
                            + " REQUEST '" + req.getNameReq() + "' on SERVICE 'sos' for the given "
                            + " PROCEDURE '" + obj.get("PROCEDURE") + "'.", ServiceException.INVALID_PARAMETER);

                }
            } else if (req.getNameReq().equalsIgnoreCase("GETFEATUREOFINTEREST")) {
                HashMap<String, HashSet<String>> tmp = SOSUtils.getInfo(SOSUtils.getGetCapabilitiesDocument(sur), offs);
                if (!tmp.get("fois").contains(Utility.getHttpParam("featureOfInterest", request))) {
                    throw new ch.supsi.ist.geoshield.exception.ServiceException("User " + usr.getNameUsr() + " is not authorized to make"
                            + " REQUEST '" + req.getNameReq() + "' on SERVICE 'sos' for the given "
                            + " FEATUREOFINTEREST '" + Utility.getHttpParam("featureOfInterest", request) + "'.", ServiceException.INVALID_PARAMETER);

                }
            }
        }
        System.out.println("   > end");
    }

    private synchronized void doAfterProcessing(RequestWrapper request, ResponseWrapper response)
            throws IOException, ServletException, ServiceException, UserException {

        if (Utility.getHttpParam("REQUEST", request).equalsIgnoreCase(
                "GETCAPABILITIES")) {

            byte[] by = (byte[]) request.getAttribute(OGCParser.BYTRES);
            request.setAttribute(OGCParser.BYTRES, null);

            String charset = "UTF-8";
            String body = new String(by, charset);
            System.out.println(body);

            Document doc;
            try {
                doc = XmlUtils.buildDocument(body);
            } catch (Exception ex) {
                System.out.println("ERROR Building document..");
                throw new ServiceException(ex.getMessage());
            }
            Element root = doc.getDocumentElement();
            if (root == null) {
                throw new ServiceException("Document has no root element");
            }

            String rootName = root.getLocalName();


            // Rewriting URL 
            // ----------------------------------------------------------------<
            NodeList gNl = doc.getElementsByTagNameNS(NS_OWS, "Get");
            for (int i = 0; i < gNl.getLength(); i++) {
                Element object = (Element) gNl.item(i);

                String href = object.getAttribute("xlink:href");
                if (href.indexOf("?") != -1) {
                    if (href.split("\\?").length == 1) {
                        object.setAttribute("xlink:href", request.getRequestURL().toString()
                                + "?");
                    } else if (href.split("\\?").length == 2) {
                        object.setAttribute("xlink:href", request.getRequestURL().toString()
                                + "?" + href.split("\\?")[1]);
                    }
                } else {
                    object.setAttribute("xlink:href", request.getRequestURL().toString());
                }
            }
            gNl = doc.getElementsByTagNameNS(NS_OWS, "Post");
            for (int i = 0; i < gNl.getLength(); i++) {
                Element object = (Element) gNl.item(i);

                String href = object.getAttribute("xlink:href");
                if (href.indexOf("?") != -1) {
                    if (href.split("\\?").length == 1) {
                        object.setAttribute("xlink:href", request.getRequestURL().toString()
                                + "?");
                    } else if (href.split("\\?").length == 2) {
                        object.setAttribute("xlink:href", request.getRequestURL().toString()
                                + "?" + href.split("\\?")[1]);
                    }
                } else {
                    object.setAttribute("xlink:href", request.getRequestURL().toString());
                }
            }


            // Procedures allowed
            HashSet<String> procs = new HashSet<String>();
            // Feature of interest allowed
            HashSet<String> fois = new HashSet<String>();
            // Observed properties allowed
            HashSet<String> obsProp = new HashSet<String>();
            // Observed properties allowed
            HashSet<String> offerings = new HashSet<String>();

            List<Element> toRemove = new ArrayList<Element>();

            Users usr = (Users) request.getAttribute("user");
            List<Offerings> offs = SOSUtils.getOfferings(usr);

            // Extracting offering/procedures
            // ----------------------------------------------------------------<
            gNl = doc.getElementsByTagNameNS(NS_SOS, "ObservationOffering");
            for (int i = 0; i < gNl.getLength(); i++) {

                Element observationOffering = (Element) gNl.item(i);
                String gmlId = observationOffering.getAttribute("gml:id");

                boolean toRemoveFlag = true;
                for (Iterator<Offerings> it = offs.iterator(); it.hasNext();) {
                    Offerings off = it.next();
                    offerings.add(off.getNameOff());
                    if (gmlId.equalsIgnoreCase(off.getNameOff())) {

                        // Adding also the long name
                        offerings.add(observationOffering.getElementsByTagNameNS(NS_GML, "name").item(0).getTextContent());

                        NodeList tmp = observationOffering.getElementsByTagNameNS(NS_SOS, "procedure");
                        //System.out.println("\nPROCEDURES: ");
                        for (int c = 0; c < tmp.getLength(); c++) {
                            Element procedure = (Element) tmp.item(c);
                            //System.out.println(" > " + procedure.getAttribute("xlink:href"));
                            procs.add(procedure.getAttribute("xlink:href"));
                        }

                        tmp = observationOffering.getElementsByTagNameNS(NS_SOS, "featureOfInterest");
                        //System.out.println("\nFOIS: ");
                        for (int c = 0; c < tmp.getLength(); c++) {
                            Element procedure = (Element) tmp.item(c);
                            //System.out.println(" > " + procedure.getAttribute("xlink:href"));
                            fois.add(procedure.getAttribute("xlink:href"));
                        }

                        tmp = observationOffering.getElementsByTagNameNS(NS_SOS, "observedProperty");
                        //System.out.println("\nOBSPROP: ");
                        for (int c = 0; c < tmp.getLength(); c++) {
                            Element procedure = (Element) tmp.item(c);
                            //System.out.println(" > " + procedure.getAttribute("xlink:href"));
                            obsProp.add(procedure.getAttribute("xlink:href"));
                        }

                        toRemoveFlag = false;
                    }
                }
                if (toRemoveFlag) {
                    toRemove.add(observationOffering);
                }
            }

            gNl = doc.getElementsByTagNameNS(NS_SOS, "ObservationOfferingList");
            if (gNl.getLength() == 1) {
                Element ObservationOfferingList = (Element) gNl.item(0);
                //Rimuovi quello che non serve!!!
                //System.out.println("OFFERINGS TOREMOVE:");
                if (toRemove.size() > 0) {
                    for (Iterator<Element> it = toRemove.iterator(); it.hasNext();) {
                        Element element = it.next();
                        ObservationOfferingList.removeChild(element);
                        //System.out.println(" > " + element.getAttribute("gml:id"));
                    }
                }
            }

            DataManager dm = Utility.getDmSession(request);
            AuthorityManager am = new AuthorityManager();
            ServicesUrls sur = dm.getServicesUrlsByPathIdSrv(request.getPathInfo(), "SOS");
            HashMap<String, String> obj =
                    (HashMap<String, String>) request.getAttribute(OGCParser.OBJREQ);
            Requests req = dm.getRequestByNameReqNameSrv(obj.get("REQUEST"), "SOS");

            gNl = doc.getElementsByTagNameNS(NS_OWS, "Operation");


            toRemove = new ArrayList<Element>();
            for (int i = 0; i < gNl.getLength(); i++) {
                Element param = (Element) gNl.item(i);
                String name = param.getAttribute("name");
                try {
                    if (!am.checkUsrAuthOnSrvSurReq(usr, sur,
                            dm.getRequestByNameReqNameSrv(name, "SOS"))) {
                        System.out.println(name + " > " + false);
                        toRemove.add(param);
                    } else {
                        System.out.println(name + " > " + true);
                    }
                } catch (ServiceException s) {
                    System.out.println(name + " > " + false);
                    toRemove.add(param);
                }
            }
            gNl = doc.getElementsByTagNameNS(NS_OWS, "OperationsMetadata");
            if (gNl.getLength() == 1) {
                Element OperationsMetadata = (Element) gNl.item(0);
                //Rimuovi quello che non serve!!!
                System.out.println("OperationsMetadata TOREMOVE:");
                if (toRemove.size() > 0) {
                    for (Iterator<Element> it = toRemove.iterator(); it.hasNext();) {
                        Element element = it.next();
                        OperationsMetadata.removeChild(element);
                        System.out.println(" > " + element.getAttribute("name"));
                    }
                }
            }


            gNl = doc.getElementsByTagNameNS(NS_OWS, "Parameter");

            for (int i = 0; i < gNl.getLength(); i++) {
                Element param = (Element) gNl.item(i);

                if (param.getAttribute("name").equalsIgnoreCase("procedure")) {

                    toRemove = new ArrayList<Element>();

                    NodeList tmp = param.getElementsByTagNameNS(NS_OWS, "Value");
                    for (int c = 0; c < tmp.getLength(); c++) {
                        Element val = (Element) tmp.item(c);
                        //System.out.println(val.getTextContent());
                        if (!procs.contains(val.getTextContent())) {
                            //System.out.println(" > Removing..");
                            toRemove.add(val);
                        }
                    }

                    tmp = param.getElementsByTagNameNS(NS_OWS, "AllowedValues");
                    if (toRemove.size() > 0) {
                        for (Iterator<Element> it = toRemove.iterator(); it.hasNext();) {
                            tmp.item(0).removeChild(it.next());
                        }
                    }

                } else if (param.getAttribute("name").equalsIgnoreCase("AssignedSensorId")) {

                    toRemove = new ArrayList<Element>();

                    NodeList tmp = param.getElementsByTagNameNS(NS_OWS, "Value");
                    for (int c = 0; c < tmp.getLength(); c++) {
                        Element val = (Element) tmp.item(c);
                        //System.out.println(val.getTextContent());
                        if (!procs.contains(val.getTextContent())) {
                            //System.out.println(" > Removing..");
                            toRemove.add(val);
                        }
                    }

                    tmp = param.getElementsByTagNameNS(NS_OWS, "AllowedValues");
                    if (toRemove.size() > 0) {
                        for (Iterator<Element> it = toRemove.iterator(); it.hasNext();) {
                            tmp.item(0).removeChild(it.next());
                        }
                    }

                } else if (param.getAttribute("name").equalsIgnoreCase("observedProperty")) {

                    toRemove = new ArrayList<Element>();

                    NodeList tmp = param.getElementsByTagNameNS(NS_OWS, "Value");
                    for (int c = 0; c < tmp.getLength(); c++) {
                        Element val = (Element) tmp.item(c);
                        //System.out.println(val.getTextContent());
                        if (!obsProp.contains(val.getTextContent())) {
                            //System.out.println(" > Removing..");
                            toRemove.add(val);
                        }
                    }

                    tmp = param.getElementsByTagNameNS(NS_OWS, "AllowedValues");
                    if (toRemove.size() > 0) {
                        for (Iterator<Element> it = toRemove.iterator(); it.hasNext();) {
                            tmp.item(0).removeChild(it.next());
                        }
                    }

                } else if (param.getAttribute("name").equalsIgnoreCase("featureOfInterest")) {

                    toRemove = new ArrayList<Element>();

                    NodeList tmp = param.getElementsByTagNameNS(NS_OWS, "Value");
                    for (int c = 0; c < tmp.getLength(); c++) {
                        Element val = (Element) tmp.item(c);
                        //System.out.println(val.getTextContent());
                        if (!fois.contains(val.getTextContent())) {
                            //System.out.println(" > Removing..");
                            toRemove.add(val);
                        }
                    }

                    tmp = param.getElementsByTagNameNS(NS_OWS, "AllowedValues");
                    if (toRemove.size() > 0) {
                        for (Iterator<Element> it = toRemove.iterator(); it.hasNext();) {
                            tmp.item(0).removeChild(it.next());
                        }
                    }

                } else if (param.getAttribute("name").equalsIgnoreCase("FeatureOfInterestId")) {

                    toRemove = new ArrayList<Element>();

                    NodeList tmp = param.getElementsByTagNameNS(NS_OWS, "Value");
                    for (int c = 0; c < tmp.getLength(); c++) {
                        Element val = (Element) tmp.item(c);
                        //System.out.println(val.getTextContent());
                        if (!fois.contains(val.getTextContent())) {
                            //System.out.println(" > Removing..");
                            toRemove.add(val);
                        }
                    }

                    tmp = param.getElementsByTagNameNS(NS_OWS, "AllowedValues");
                    if (toRemove.size() > 0) {
                        for (Iterator<Element> it = toRemove.iterator(); it.hasNext();) {
                            tmp.item(0).removeChild(it.next());
                        }
                    }

                } else if (param.getAttribute("name").equalsIgnoreCase("FeatureId")) {

                    toRemove = new ArrayList<Element>();

                    NodeList tmp = param.getElementsByTagNameNS(NS_OWS, "Value");
                    for (int c = 0; c < tmp.getLength(); c++) {
                        Element val = (Element) tmp.item(c);
                        //System.out.println(val.getTextContent());
                        if (!fois.contains(val.getTextContent())) {
                            //System.out.println(" > Removing..");
                            toRemove.add(val);
                        }
                    }

                    tmp = param.getElementsByTagNameNS(NS_OWS, "AllowedValues");
                    if (toRemove.size() > 0) {
                        for (Iterator<Element> it = toRemove.iterator(); it.hasNext();) {
                            tmp.item(0).removeChild(it.next());
                        }
                    }

                } else if (param.getAttribute("name").equalsIgnoreCase("offering")) {

                    toRemove = new ArrayList<Element>();

                    NodeList tmp = param.getElementsByTagNameNS(NS_OWS, "Value");
                    for (int c = 0; c < tmp.getLength(); c++) {
                        Element val = (Element) tmp.item(c);
                        //System.out.println(val.getTextContent());
                        if (!offerings.contains(val.getTextContent())) {
                            //System.out.println(" > Removing..");
                            toRemove.add(val);
                        }
                    }

                    tmp = param.getElementsByTagNameNS(NS_OWS, "AllowedValues");
                    if (toRemove.size() > 0) {
                        for (Iterator<Element> it = toRemove.iterator(); it.hasNext();) {
                            tmp.item(0).removeChild(it.next());
                        }
                    }

                }
            }


            request.setAttribute(OGCParser.BYTRES, XmlUtils.xmlToString(doc).getBytes());
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
