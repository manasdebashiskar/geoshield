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

import ch.supsi.ist.geoshield.data.DataManager;
import ch.supsi.ist.geoshield.data.Layers;
import ch.supsi.ist.geoshield.data.ServicesUrls;
import ch.supsi.ist.geoshield.exception.ServiceException;
import ch.supsi.ist.geoshield.utils.ProxyUtility;
import ch.supsi.ist.geoshield.parser.OGCParser;
import ch.supsi.ist.geoshield.utils.Utility;
import ch.supsi.ist.geoshield.utils.xml.XmlUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.QueryType;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.geotools.wfs.v1_1.WFS;
import org.geotools.xml.Configuration;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */

public class WFSProxy extends HttpServlet {

    // The host to which we are proxying requests
    private String stringProxyHost;
    private DataManager dm;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        dm = Utility.getDmSession(request);
        ServicesUrls sur;
        System.out.println("WFSProxy...");
        try {

            sur = dm.getServicesUrlsByPathIdSrv(request.getPathInfo(),"WFS");
            stringProxyHost = sur.getUrlSur();
            PostMethod postMethod = new PostMethod(stringProxyHost);

            // Setting request headers
            ProxyUtility.setProxyRequestHeaders(request, postMethod, stringProxyHost);

            // Check if BASIC authentication is needed
            if ((sur.getPswSur()!=null && !sur.getPswSur().equalsIgnoreCase("")) ||
                    (sur.getUsrSur()!=null && !sur.getUsrSur().equalsIgnoreCase(""))) {
                String aut = "";
                byte[] b = org.apache.commons.codec.binary.Base64.encodeBase64((sur.getUsrSur() + ":" + sur.getPswSur()).getBytes());
                System.out.println("Setting WFS Credential: " + sur.getUsrSur() + ":" + sur.getPswSur());
                //postMethod.setRequestHeader(new Header("Authorization", "Basic " + b.toString()));
            }

            if (ServletFileUpload.isMultipartContent(request)) {
                throw new ServletException("MultipartContent is not yet handled by GeoShield");
            } else {
                try {
                    Object obj = request.getSession().getAttribute(OGCParser.OBJREQ);

                    String toSend = "";

                    if (obj != null) {

                        Configuration config = new org.geotools.wfs.v1_1.WFSConfiguration();
                        org.geotools.xml.Encoder encoder = new org.geotools.xml.Encoder(config);

                        encoder.setIndenting(true);
                        encoder.setIndentSize(2);

                        //encoder.setSchemaLocation("http://www.openplans.org/topp", "topp");

                        // Encoding GetFeature
                        if (obj instanceof net.opengis.wfs.GetFeatureType) {
                            GetFeatureType gft = (GetFeatureType) obj;
                            Document d = encoder.encodeAsDOM(obj, WFS.GetFeature);
                            List<Layers> lays = null;
                            for (int i = 0; i < gft.getQuery().size(); i++) {
                                QueryType query = (QueryType) gft.getQuery().get(i);
                                List tyns = query.getTypeName();
                                String[] ls = new String[tyns.size()];

                                // @todo controllare sta roba..
                                // E' possibile che si mettono piu' featureType in una query?
                                for (Iterator it = tyns.iterator(); it.hasNext();) {
                                    javax.xml.namespace.QName qn = (javax.xml.namespace.QName) it.next();
                                    ls[i] = qn.getPrefix() + ":" + qn.getLocalPart();
                                }

                                // Loading layers object from database
                                lays = dm.getLayers(request.getPathInfo(), ls,"WFS");
                                for (Iterator<Layers> it = lays.iterator(); it.hasNext();) {
                                    Layers lay = it.next();
                                    d.getDocumentElement().setAttribute("xmlns:" + lay.getNsLay(), lay.getNsUrlLay());
                                }
                            }
                            toSend = XmlUtils.xmlToString(d);
                        } // Encoding DescribeFeatureType
                        else if (obj instanceof net.opengis.wfs.DescribeFeatureTypeType) {
                            toSend = XmlUtils.xmlToString(encoder.encodeAsDOM(obj, WFS.DescribeFeatureType));
                        } // Encoding GetCapabilities
                        else if (obj instanceof net.opengis.wfs.GetCapabilitiesType) {
                            toSend = XmlUtils.xmlToString(encoder.encodeAsDOM(obj, WFS.GetCapabilities));
                        }

                        //System.out.println("Sending: \n" + toSend);

                        postMethod.setRequestEntity(
                                new StringRequestEntity(toSend, "text/xml", "UTF-8"));


                        HttpClient httpClient = new HttpClient();
                        postMethod.setFollowRedirects(false);

                        // Execute the request
                        response.setStatus(httpClient.executeMethod(postMethod));

                        // Setting response headers
                        ProxyUtility.setProxyResponseHeaders(response, postMethod);

                        // Downloading request
                        InputStream bodyStream = null;

                        String encoding = postMethod.getResponseHeader(
                                "Content-Encoding").getValue().replaceFirst("^.*;", "").trim();

                        // Saving response encoding for later use
                        request.getSession().setAttribute(OGCParser.ENCRES, encoding);

                        //System.out.println("Encoding: " + encoding);

                        if (encoding.equalsIgnoreCase("gzip")) {
                            bodyStream = new GZIPInputStream(
                                    postMethod.getResponseBodyAsStream());

                        } else if (encoding.equalsIgnoreCase("deflate")) {
                            bodyStream = new DeflaterInputStream(
                                    postMethod.getResponseBodyAsStream());

                        } else {
                            bodyStream = new BufferedInputStream(
                                    postMethod.getResponseBodyAsStream());
                        }

                        // Changing the response from InputStream to Byte Array.
                        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                        byte[] buffer = new byte[4096];
                        int length;
                        while ((length = bodyStream.read(buffer)) > 0) {
                            outStream.write(buffer, 0, length);
                        }
                        byte[] by = outStream.toByteArray();

                        // Saving byte response into session memory (think something better..)
                        request.getSession().setAttribute(OGCParser.BYTRES, by);


                    } else {
                        throw new ServiceException("WFS response Object is null");
                    }

                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(WFSProxy.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(WFSProxy.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SAXException ex) {
                    Logger.getLogger(WFSProxy.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TransformerException ex) {
                    Logger.getLogger(WFSProxy.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (ServiceException ex) {
            Logger.getLogger(WFSProxy.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServletException(ex.getMessage());
        }
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "WFS Proxy";
    }// </editor-fold>
}
