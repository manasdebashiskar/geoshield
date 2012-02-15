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

import ch.supsi.ist.geoshield.data.DataManager;
import ch.supsi.ist.geoshield.data.ServicesUrls;
import ch.supsi.ist.geoshield.utils.ProxyUtility;
import ch.supsi.ist.geoshield.parser.OGCParser;
import ch.supsi.ist.geoshield.shields.CacheFilterUtils;
import ch.supsi.ist.geoshield.utils.Utility;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */
public class WMSProxy extends HttpServlet {

    private DataManager dm;
    private String stringProxyHost;

    /*
    @Override
    public void init(ServletConfig config) throws ServletException {
    super.init(config);
    //config.getServletContext().setAttribute("name", "Faisal Khan");
    }*/
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        dm = CacheFilterUtils.getDataManagerCached(request);
        ServicesUrls sur;
        try {

            sur = dm.getServicesUrlsByPathIdSrv(request.getPathInfo(), "WMS");

            stringProxyHost = sur.getUrlSur();

            PostMethod postMethod = new PostMethod(stringProxyHost);
            System.out.println("Server: " + stringProxyHost);
                        
            //ProxyUtility.setProxyRequestHeaders(request, postMethod, stringProxyHost);

            // Check if BASIC authentication is needed
            if ((sur.getPswSur() != null && !sur.getPswSur().equalsIgnoreCase(""))
                    || (sur.getUsrSur() != null && !sur.getUsrSur().equalsIgnoreCase(""))) {
                System.out.println("Setting Authorization..");
                postMethod.setRequestHeader(new Header("Authorization", "Basic " + new String(
                        Base64.encodeBase64((sur.getUsrSur() + ":" + sur.getPswSur()).getBytes()))));
            }

            if (ServletFileUpload.isMultipartContent(request)) {
                throw new ServletException("MultipartContent is not yet handled by GeoShield");
                
            } else {

                Enumeration paramNames = request.getParameterNames();
                List<NameValuePair> nvp = new ArrayList<NameValuePair>();
                while (paramNames.hasMoreElements()) {
                    String paramName = (String) paramNames.nextElement();
                    String[] paramValues = request.getParameterValues(paramName);
                    for (int i = 0; i < paramValues.length; i++) {
                        System.out.println("paramName: " + paramName + " = " + paramValues[i]);
                        nvp.add(new NameValuePair(paramName, paramValues[i]));
                    }
                }
                NameValuePair[] data = new NameValuePair[nvp.size()];
                for (int i = 0; i < data.length; i++) {
                    data[i] = nvp.get(i);
                }
                postMethod.setRequestBody(data);

                /*
                NameValuePair[] p = postMethod.getParameters();
                //System.out.println(" -  - - - - - getParameters - -- - - - - - - -");
                for (int i = 0; i < p.length; i++) {
                NameValuePair n = p[i];
                //System.out.println(n.getName() + ": " + n.getValue());
                }
                 */

                // Execute the request
                postMethod.setFollowRedirects(false);
                HttpClient httpClient = new HttpClient();

                int status = httpClient.executeMethod(postMethod);
                                
                response.setStatus(status);

                // Setting response headers
                ProxyUtility.setProxyResponseHeaders(response, postMethod);

                Header[] hs = postMethod.getResponseHeaders();
                request.getSession().setAttribute(OGCParser.ENCRES, null);
                String encoding = "";
                for (int i = 0; i < hs.length; i++) {
                    Header h = hs[i];
                    if (h.getName().equalsIgnoreCase("Content-Encoding")) {
                        request.getSession().setAttribute(OGCParser.ENCRES, h.getValue());
                        encoding = h.getValue();
                        break;
                    }
                }

                // Store response only if a GetCapabilities request has been made
                if (Utility.getHttpParam("REQUEST", request).equalsIgnoreCase("GETCAPABILITIES")) {

                    InputStream bodyStream = null;

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
                    request.setAttribute(OGCParser.BYTRES, by);

                } else {
                    InputStream inputStreamProxyResponse = postMethod.getResponseBodyAsStream();
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStreamProxyResponse);
                    OutputStream outputStreamClientResponse = response.getOutputStream();
                    int intNextByte;

                    //System.out.print("\nCopying response: ");
                    StringBuffer sb = new StringBuffer();
                    int cnt = 0;
                    while ((intNextByte = bufferedInputStream.read()) != -1) {
                        cnt++;
                        outputStreamClientResponse.write(intNextByte);
                    }

                    //System.out.println(" - > BYTE READED: " + cnt);
                    outputStreamClientResponse.flush();
                    outputStreamClientResponse.close();

                }

            }

        } catch (Exception ex) {
            Logger.getLogger(WMSProxy.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServletException(ex.getMessage());
        }
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
