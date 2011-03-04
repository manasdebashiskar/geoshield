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

import ch.supsi.ist.geoshield.parser.OGCParser;
import ch.supsi.ist.geoshield.data.DataManager;
import ch.supsi.ist.geoshield.data.Layers;
import ch.supsi.ist.geoshield.data.ServicesUrls;
import ch.supsi.ist.geoshield.exception.ServiceException;
import ch.supsi.ist.geoshield.shields.RequestWrapper;
import ch.supsi.ist.geoshield.shields.ResponseWrapper;
import ch.supsi.ist.geoshield.wfs.WFSParser;
import ch.supsi.ist.geoshield.utils.Utility;
import ch.supsi.ist.geoshield.utils.xml.XmlUtils;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import net.opengis.wfs.FeatureCollectionType;
import net.opengis.wfs.QueryType;
import net.opengis.wfs.WFSCapabilitiesType;
import net.opengis.wfs.impl.GetFeatureTypeImpl;
import org.geotools.wfs.v1_1.WFS;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */

public class WfsButler implements Filter {

    private FilterConfig filterConfig = null;
    private DataManager dm = null;
    private ServicesUrls sur = null;
    private String path = null;
    private OGCParser parser = null;

    public WfsButler() {
    }

    private void doBeforeProcessing(RequestWrapper request, ResponseWrapper response)
            throws IOException, ServletException, ServiceException {
            System.out.println("WfsButler..");
        dm = Utility.getDmSession(request);
        if (request.getMethod().equalsIgnoreCase("GET")) {
            this.handleGetRequest(request, response);
            //throw new ServiceException("GET method is not supported");
        } else if (request.getMethod().equalsIgnoreCase("POST")) {
            this.handlePostRequest(request, response);
        }
    }

    private void handleGetRequest(RequestWrapper request, ResponseWrapper response) throws IOException, ServiceException {

        parser = new WFSParser();
        Object o = parser.parseGet(request);
        request.getSession().setAttribute(OGCParser.OBJREQ, o);
    }

    private void handlePostRequest(RequestWrapper request, ResponseWrapper response) throws IOException, ServiceException {

        parser = new WFSParser();
        Object o = parser.parsePost(request);
        request.getSession().setAttribute(OGCParser.OBJREQ, o);


    }

    private void doAfterProcessing(RequestWrapper request, ResponseWrapper response)
            throws IOException, ServletException, ServiceException {

        Object obj = request.getSession().getAttribute(OGCParser.OBJRES);
        byte[] byts = null;
        if (obj == null) {
            byts = (byte[]) request.getSession().getAttribute(OGCParser.BYTRES);
        } else {

            org.geotools.xml.Encoder encoder =
                    new org.geotools.xml.Encoder(
                    new org.geotools.wfs.v1_1.WFSConfiguration());

            // @TODO commentare questo
            /*
            encoder.setIndenting(true);
            encoder.setIndentSize(2);*/

            String toSend;
            try {
                if (obj instanceof WFSCapabilitiesType) {
                    Document d = encoder.encodeAsDOM(obj, WFS.WFS_Capabilities);
                    d.getDocumentElement().setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
                    toSend = XmlUtils.xmlToString(d);

                } else if (obj instanceof FeatureCollectionType) {

                    GetFeatureTypeImpl req = (GetFeatureTypeImpl) request.getSession().getAttribute(OGCParser.OBJREQ);
                    List<Layers> lays = null;
                    for (int i = 0; i < req.getQuery().size(); i++) {
                        QueryType query = (QueryType) req.getQuery().get(i);
                        List tyns = query.getTypeName();
                        String[] ls = new String[tyns.size()];

                        // @todo controllare sta roba..
                        // E' possibile che si mettono piu' featureType in una query?
                        for (Iterator it = tyns.iterator(); it.hasNext();) {
                            javax.xml.namespace.QName qn = (javax.xml.namespace.QName) it.next();
                            ls[i] = qn.getPrefix() + ":" + qn.getLocalPart();
                        }

                        // Loading layers object from database
                        lays = dm.getLayers(request.getPathInfo(), ls, "WFS");
                    }

                    // @todo migliorare questa parte..

                    for (Iterator<Layers> it = lays.iterator(); it.hasNext();) {
                        Layers lay = it.next();
                        encoder.getNamespaces().declarePrefix(lay.getNsLay(), lay.getNsUrlLay());
                    }

                    //encoder.setSchemaLocation("sitinet", "http://127.0.0.1:8080/geoserver/wfs?service=WFS&version=1.1.0&request=DescribeFeatureType&typeName=sitinet:laghi");

                    Document d = encoder.encodeAsDOM(obj, WFS.FeatureCollection);
                    d.getDocumentElement().setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");

                    for (Iterator<Layers> it = lays.iterator(); it.hasNext();) {
                        Layers lay = it.next();
                        d.getDocumentElement().setAttribute("xmlns:" + lay.getNsLay(), lay.getNsUrlLay());
                    }


                    //d.getDocumentElement().setAttribute("xmlns:sitinet", "http://www.ist.supsi.ch/sitinet");
                    //DataManager dm = Utility.getDmSession(request);

                    toSend = XmlUtils.xmlToString(d);

                }/* else if (obj instanceof DescribeFeatureTypeType) {
                Document d = encoder.encodeAsDOM(obj, WFS.DescribeFeatureType);
                d.getDocumentElement().setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
                toSend = XmlUtils.xmlToString(d);

                }*/ else {
                    throw new ServiceException("Response unknown.");
                }
            } catch (SAXException ex) {
                throw new ServiceException(ex.getMessage());
            } catch (TransformerException ex) {
                throw new ServiceException(ex.getMessage());
            }

            //OutputStream outputStreamClientResponse = null;
            byts = toSend.getBytes();

        }

        String encoding = (String) request.getSession().getAttribute(OGCParser.ENCRES);
/*
        System.out.println("byts.length: " + byts.length);
        System.out.println("encoding: " + encoding);
        System.out.println("Sending back..");
        //System.out.println("FeatureCollection toSend:\n" + toSend);
        System.out.print("FeatureCollection toSend:\n" + new String(byts));*/

        if (encoding.equalsIgnoreCase("gzip")) {

            // prepare a gzip stream
            ByteArrayOutputStream compressedContent = new ByteArrayOutputStream();
            GZIPOutputStream gzipstream = new GZIPOutputStream(compressedContent);
            byte[] bytes = byts;
            gzipstream.write(bytes);
            gzipstream.finish();

            // get the compressed content
            byts = compressedContent.toByteArray();
            response.setContentLength(byts.length);

        } else if (encoding.equalsIgnoreCase("deflate")) {

            ByteArrayOutputStream compressedContent = new ByteArrayOutputStream();
            DeflaterOutputStream defstream = new DeflaterOutputStream(compressedContent);

            defstream.write(byts);
            defstream.finish();

            // get the compressed content
            byts = compressedContent.toByteArray();
            response.setContentLength(byts.length);

        }

        BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
        out.write(byts);
        out.flush();
        out.close();

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
        try {
            doBeforeProcessing(wrappedRequest, wrappedResponse);
            chain.doFilter(wrappedRequest, wrappedResponse);
            doAfterProcessing(wrappedRequest, wrappedResponse);
        } catch (ServiceException t) {
            wrappedResponse.sendError(ResponseWrapper.SC_NOT_FOUND, t.getMessage());
            //response.getWriter().close();
            problem = t;
        } catch (Throwable t) {
            problem = t;
            //t.printStackTrace();
        }
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
            return ("RequestParser()");
        }
        StringBuffer sb = new StringBuffer("RequestParser(");
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
                //t.printStackTrace(ps);
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
