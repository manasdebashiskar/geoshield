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

package ch.supsi.ist.geoshield.wfs;

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

public class WFSShield implements Filter {

    private FilterConfig filterConfig = null;
    private Users usr = null;
    private List<Layers> lays = null;
    private List<org.opengis.filter.Filter> permissionFilter = null;

    public WFSShield() {
    }

    private synchronized void doBeforeProcessing(RequestWrapper request, ResponseWrapper response)
            throws IOException, ServletException, ServiceException, UserException {
        
        if (request.getMethod().equalsIgnoreCase("GET")) {
            //this.handleGetRequest(request, response);
            throw new ServiceException("GET method is not supported");

        } else if (request.getMethod().equalsIgnoreCase("POST")) {
            this.handlePostRequest(request, response);

        }
    }

    private void handlePostRequest(RequestWrapper request, ResponseWrapper response) throws IOException, ServiceException, UserException {

        Object obj = request.getSession().getAttribute(OGCParser.OBJREQ);
        String path = request.getPathInfo();

        usr = (Users) request.getSession().getAttribute("user");
        DataManager dm = Utility.getDmSession(request);

        AuthorityManager am = new AuthorityManager();

        Requests reqs = null;
        if (obj != null && !(obj instanceof net.opengis.wfs.GetCapabilitiesType)) {

            if (obj instanceof net.opengis.wfs.GetFeatureType) {
                // <editor-fold defaultstate="collapsed" desc="Permission Check 4 GetFeature request">

                // Check user permission on services and request
                reqs = dm.getRequestByNameReqNameSrv("GetFeature", "WFS");

                // Casting object
                GetFeatureTypeImpl parsed = (GetFeatureTypeImpl) obj;

                // Check user permission on feature
                FilterAuth fau = new FilterAuth();
                for (int i = 0; i < parsed.getQuery().size(); i++) {
                    QueryType query = (QueryType) parsed.getQuery().get(i);
                    List tyns = query.getTypeName();
                    String[] ls = new String[tyns.size()];

                    // @todo controllare sta roba..
                    // E' possibile che si mettono piu' featureType in una query?
                    for (Iterator it = tyns.iterator(); it.hasNext();) {
                        javax.xml.namespace.QName qn = (javax.xml.namespace.QName) it.next();
                        ls[i] = qn.getPrefix() + ":" + qn.getLocalPart();
                    }

                    // Loading layers object from database
                    lays = dm.getLayers(path, ls, "WFS");

                    // Checking authorization of the user for each layer
                    boolean authOk = am.checkUsrAuthOnLayers(usr, lays);
                    if (!authOk){
                        throw new ch.supsi.ist.geoshield.exception.ServiceException(
                                "User not authorized to access one or some layers requested.",
                                ServiceException.SERVICE_AUTHENTICATION);
                    }
                    org.opengis.filter.Filter clientFilter = query.getFilter();

                    this.permissionFilter = fau.getFilters(usr, lays, dm);

                    if (clientFilter == null || !(clientFilter instanceof org.geotools.filter.FidFilterImpl)) { // use onli gs filters
                        // Extracting client filter
                        List<org.opengis.filter.Filter> gsFilter = null;
                        if (clientFilter == null) {
                            // Only one filter is present for each query element
                            // @todo migliorare questa parte
                            if (!(this.permissionFilter.size() == 1
                                    && this.permissionFilter.get(0) instanceof org.opengis.filter.IncludeFilter)) {
                                gsFilter = this.permissionFilter;
                            }
                        } else { // User HAS set filters
                            List<org.opengis.filter.Filter> clientFilterList = new ArrayList<org.opengis.filter.Filter>();
                            clientFilterList.add(clientFilter);
                            gsFilter = fau.mergeUserAndSystemFilters(clientFilterList, this.permissionFilter);
                        }
                        // @todo milgiorare questa parte. Ci sono sempre dei filtri??
                        if (gsFilter != null) {
                            query.setFilter(gsFilter.get(0));
                        }

                    }
                }
                // </editor-fold>
            } else if (obj instanceof net.opengis.wfs.DescribeFeatureTypeType) {
                // <editor-fold defaultstate="collapsed" desc="Permission Check 4 DescribeFeatureType request">
                // Check user permission on services and request
                reqs = dm.getRequestByNameReqNameSrv("DescribeFeatureType", "WFS");
                DescribeFeatureTypeType parsed = (DescribeFeatureTypeType) obj;


                // ------------------------------------------------------------
                // Check user permission on feature ---------------------------
                // ------------------------------------------------------------
                // @todo return list of authorized feature is parsed.getTypeName().size() == 0
                if (parsed.getTypeName().size() > 0) {
                    String[] ls = new String[parsed.getTypeName().size()];
                    for (int i = 0; i < parsed.getTypeName().size(); i++) {
                        javax.xml.namespace.QName qn = (javax.xml.namespace.QName) parsed.getTypeName().get(i);
                        ls[i] = qn.getPrefix() + ":" + qn.getLocalPart();
                    }
                    lays = dm.getLayers(path, ls, "WFS");
                    boolean authOk = am.checkUsrAuthOnLayers(usr, lays);
                    if (!authOk) {
                        throw new ch.supsi.ist.geoshield.exception.ServiceException(
                                "User not authorized to access one or some layers requested.",
                                ServiceException.SERVICE_AUTHENTICATION);
                    }
                }
                // </editor-fold>
            }
            ServicesUrls sur = dm.getServicesUrlsByPathIdSrv(path, "WFS");
            if (!am.checkUsrAuthOnSrvSurReq(usr, sur, reqs)) {
                throw new ch.supsi.ist.geoshield.exception.ServiceException("User " + usr.getNameUsr() + " is not authorized to make"
                        + " REQUEST '" + request + "' on SERVICE 'WFS' for the given "
                        + " PATH '" + path + "'.", ServiceException.SERVICE_AUTHENTICATION);
            }
        }
    }

    private void handleGetRequest(RequestWrapper request, ResponseWrapper response) throws IOException, ServiceException {
    }

    private void doAfterProcessing(RequestWrapper request, ResponseWrapper response)
            throws IOException, ServletException, ServiceException, UserException {

        Object obj = request.getSession().getAttribute(OGCParser.OBJREQ);

        if (obj instanceof GetFeatureType) {

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
            String wfsResponse = root.getLocalName();

            Configuration config = new org.geotools.wfs.v1_1.WFSConfiguration();
            DOMParser parser = new DOMParser(config, doc);
            Object parsedObj;
            try {
                parsedObj = parser.parse();
                request.getSession().setAttribute(OGCParser.OBJRES, parsedObj);
            } catch (SAXException ex) {
                throw new ServiceException(ex.getMessage());
            } catch (ParserConfigurationException ex) {
                throw new ServiceException(ex.getMessage());
            }

            GetFeatureType gft = (GetFeatureType) obj;

            for (int i = 0; i < gft.getQuery().size(); i++) {
                QueryType query = (QueryType) gft.getQuery().get(i);
                
                // Check feature collection in postprocessing only if there is a Fid Filter
                if (query.getFilter() instanceof org.geotools.filter.FidFilterImpl) {
                    if (wfsResponse.equalsIgnoreCase("FeatureCollection")) {
                        FeatureCollectionTypeImpl parsed = (FeatureCollectionTypeImpl) parsedObj;
                        EList el = parsed.getFeature();
                        for (Iterator it = el.iterator(); it.hasNext();) {
                            DefaultFeatureCollection dfc = (DefaultFeatureCollection) it.next();
                            List<Feature> featureToRemove = new ArrayList<Feature>(0);
                            for (FeatureIterator it1 = dfc.features(); it1.hasNext();) {
                                Feature f = it1.next();
                                //System.out.println("Feature:" + f.getType().getName().getURI());
                                for (int v = 0; v < this.lays.size(); v++) {
                                    Layers lay = this.lays.get(v);
                                    org.opengis.filter.Filter fil = this.permissionFilter.get(v);
                                    //System.out.println(" > Comparing with: " + lay.getNameLay());
                                    if (f.getType().getName().getURI().indexOf(lay.getNameLay()) > -1) {
                                        //System.out.println("   > Evaluating with: " + fil.toString());
                                        boolean eval = fil.evaluate(f);
                                        //System.out.println("      > " + eval);
                                        if (!eval) {
                                            featureToRemove.add(f);
                                            //System.out.println("NumberOfFeatures: " + parsed.getNumberOfFeatures().intValue());
                                        }
                                        break;
                                    }

                                }
                            }
                            for (Iterator<Feature> itToRem = featureToRemove.iterator(); itToRem.hasNext();) {
                                //System.out.println("Removing: " + dfc.remove(itToRem.next()));
                                parsed.setNumberOfFeatures(parsed.getNumberOfFeatures().subtract(BigInteger.ONE));
                            }
                        }
                        for (Iterator it = el.iterator(); it.hasNext();) {
                            DefaultFeatureCollection dfc = (DefaultFeatureCollection) it.next();
                            for (FeatureIterator it1 = dfc.features(); it1.hasNext();) {
                                Feature f = it1.next();
                                //System.out.println(" >>>>>>>>>>< Feature:" + f.getType().getName().getURI());
                            }
                        }
                    }
                    break;
                }
            }

        } else if (obj instanceof GetCapabilitiesType) {

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
            String wfsResponse = root.getLocalName();

            NodeList gNl = doc.getElementsByTagNameNS("http://www.opengis.net/ows", "Get");
            for (int i = 0; i < gNl.getLength(); i++) {
                Element object = (Element) gNl.item(i);
                //System.out.println(" " + i + ". " + object.getAttribute("xlink:href"));
                object.setAttribute("xlink:href", request.getRequestURL().toString());
            }
            //System.out.println("Changing ows:Post");
            gNl = doc.getElementsByTagNameNS("http://www.opengis.net/ows", "Post");
            for (int i = 0; i < gNl.getLength(); i++) {
                Element object = (Element) gNl.item(i);
                //System.out.println(" " + i + ". " + object.getAttribute("xlink:href"));
                object.setAttribute("xlink:href", request.getRequestURL().toString());
            }

            Configuration config = new org.geotools.wfs.v1_1.WFSConfiguration();
            DOMParser parser = new DOMParser(config, doc);
            Object parsedObj;
            try {
                parsedObj = parser.parse();
                request.getSession().setAttribute(OGCParser.OBJRES, parsedObj);
            } catch (SAXException ex) {
                throw new ServiceException(ex.getMessage());
            } catch (ParserConfigurationException ex) {
                throw new ServiceException(ex.getMessage());
            }

            WFSCapabilitiesTypeImpl parsed = (WFSCapabilitiesTypeImpl) parsedObj;

            DataManager dm = Utility.getDmSession(request);
            AuthorityManager am = new AuthorityManager();
            this.lays = dm.getLayers(request.getPathInfo());
            List<Layers> layToRemove = new ArrayList<Layers>(0);
            for (Iterator<Layers> it = this.lays.iterator(); it.hasNext();) {
                Layers lay = it.next();
                if (!am.checkUsrVisibilityOnLayer(usr, lay)) {
                    layToRemove.add(lay);
                }
            }
            this.lays.removeAll(layToRemove);

            // CHECK LAYERS TO BE RETURNED (ONLY LAYER WITH EXISTING PERMISSION AND FILTER != EXCLUDE)
            FeatureTypeListType fList = parsed.getFeatureTypeList();
            EList el = fList.getFeatureType();
            List<FeatureTypeTypeImpl> toremove = new ArrayList<FeatureTypeTypeImpl>(0);

            for (Iterator it = el.iterator(); it.hasNext();) {
                FeatureTypeTypeImpl feat = (FeatureTypeTypeImpl) it.next();
                String layerName = feat.getName().getPrefix() + ":" + feat.getName().getLocalPart();
                //System.out.print(" Checking > " + layerName);
                boolean isToremove = true;
                for (int i = 0; i < this.lays.size(); i++) {
                    Layers lay = this.lays.get(i);
                    if (lay.getNameLay().equalsIgnoreCase(layerName)) {
                        //System.out.println(": show: ");
                        isToremove = false;
                        break;
                    }
                }
                if (isToremove) {
                    //System.out.println(": remove: ");
                    toremove.add(feat);
                }
            }
            el.removeAll(toremove);
        } else if (obj instanceof DescribeFeatureTypeType) {
            request.getSession().setAttribute(OGCParser.OBJRES, null);
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
