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

import ch.supsi.ist.geoshield.exception.ServiceException;
import ch.supsi.ist.geoshield.parser.OGCParser;
import ch.supsi.ist.geoshield.shields.RequestWrapper;
import ch.supsi.ist.geoshield.utils.Utility;
import ch.supsi.ist.geoshield.utils.xml.XmlUtils;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.QueryType;
import net.opengis.wfs.impl.WfsFactoryImpl;
import org.geotools.data.DefaultQuery;
import org.geotools.xml.Configuration;
import org.geotools.xml.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */

public class WFSParser extends OGCParser {

    @Override
    public Object parseGet(RequestWrapper request) throws ServiceException {
        Object ret = null;

        String req = Utility.getHttpParam("REQUEST", request);
        WfsFactoryImpl wfs = new WfsFactoryImpl();

        if (req.equalsIgnoreCase("GETFEATURE")) {
            // request=GetFeature&version=1.1.0&typeName=sitinet:laghi&outputFormat=GML2
            GetFeatureType gf = wfs.createGetFeatureType();

            // Checking version
            String version = Utility.getHttpParam("VERSION", request);
            if (version == null) {
                throw new ServiceException("Parameter name 'Service' is mandatory.");
            } else if (!version.equalsIgnoreCase("1.1.0")) {
                throw new ServiceException("Geoshield support WFS version 1.1.0, " + version + " given.");
            }
            gf.setVersion(version);

            // Checking TypeName
            String typeName = Utility.getHttpParam("TYPENAME", request);
            if (typeName == null) {
                throw new ServiceException("Parameter name 'typeName' is mandatory.");
            }
            QueryType qt = wfs.createQueryType();
            gf.getQuery().add(new DefaultQuery("sitinet:laghi"));
            
            
        }
        return ret;
        //throw new ServiceException("GET method is not supported");
    }

    @Override
    public Object parsePost(RequestWrapper request) throws ServiceException {
        Map<String, String[]> postParams = (Map<String, String[]>) request.getParameterMap();
        if (postParams.size() > 0) {
            return this.parseGet(request);
        } else {
            try {
                String body = Utility.getBodyContent(request);
                // Parsing xml request
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
                String xmlRequest = root.getLocalName();
                // Check for WFS version if user isn't asking for GetCapabilities document
                if (!xmlRequest.equalsIgnoreCase("GetCapabilities")) {
                    String version = null;
                    NamedNodeMap nnm = root.getAttributes();
                    for (int j = 0; j < nnm.getLength(); j++) {
                        Node att = nnm.item(j);
                        if (att.getNodeName().equalsIgnoreCase("VERSION")) {
                            version = att.getNodeValue();
                        }
                    }
                    if (version == null) {
                        throw new ServiceException("Parameter name 'Service' is mandatory.");
                    } else if (!version.equalsIgnoreCase("1.1.0")) {
                        throw new ServiceException("Geoshield support WFS version 1.1.0, " + version + " given.");
                    }
                }
                Configuration config = new org.geotools.wfs.v1_1.WFSConfiguration();
                DOMParser parser = new DOMParser(config, doc);
                try {
                    return parser.parse();
                } catch (SAXException ex) {
                    throw new ServiceException(ex.getMessage());
                } catch (ParserConfigurationException ex) {
                    throw new ServiceException(ex.getMessage());
                }
            } catch (IOException ex) {
                Logger.getLogger(WFSParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        throw new ServiceException("Object unknown");
    }

    @Override
    public Object checkResponse(RequestWrapper request) throws ServiceException {
        Object obj = request.getSession().getAttribute(OBJREQ);
        if (obj instanceof GetFeatureType) {
            //System.out.println(" > GetFeatureType: response");
            GetFeatureType gft = (GetFeatureType) obj;

            boolean postProcess = false;
            String body = "";
            for (int i = 0; i < gft.getQuery().size(); i++) {
                QueryType query = (QueryType) gft.getQuery().get(i);
                if (query.getFilter() instanceof org.geotools.filter.FidFilterImpl) {
                    body = (String) request.getSession().getAttribute(OBJRES);
                    postProcess = true;
                    break;
                }
            }
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
