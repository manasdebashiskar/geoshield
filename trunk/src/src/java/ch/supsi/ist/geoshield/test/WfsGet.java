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

package ch.supsi.ist.geoshield.test;

import ch.supsi.ist.geoshield.utils.xml.XmlUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import net.opengis.wfs.GetFeatureType;
import net.opengis.wfs.QueryType;
import net.opengis.wfs.impl.QueryTypeImpl;
import net.opengis.wfs.impl.WfsFactoryImpl;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.impl.EDataTypeImpl;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTS;
import org.geotools.wfs.v1_1.WFS;
import org.geotools.xml.Configuration;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.Envelope;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author milan
 */
public class WfsGet {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String getCapabilities = "http://localhost:8080/geoserver/wfs?REQUEST=GetCapabilities";
            Map connectionParameters = new HashMap();
            connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities);
            WfsFactoryImpl wfs = new WfsFactoryImpl();
            GetFeatureType gf = wfs.createGetFeatureType();
            gf.setVersion("1.1.0");


            Filter filter = CQL.toFilter("NAME like '%land'");
            /*
            query.s
            wfs.crea
            wfs.createResultTypeTypeFromString(new EDataTypeImpl(), "");*/

            QName typeName = new QName("http://www.geotools.org/test", "lay", "test");
            QName typeName2 = new QName("http://www.geotools.org/test", "lay2", "test");

            List t = new ArrayList(1);
            t.add(typeName);
            t.add(typeName2);

            QueryType query = wfs.createQueryType();
            query.setTypeName(t);
            
            gf.getQuery().add(query);

            for (int i = 0; i < gf.getQuery().size(); i++) {
                QueryType qry = (QueryType) gf.getQuery().get(i);
                System.out.println("Query: " + qry.getPropertyName());
            }

            //.add(new DefaultQuery("sitinet:laghi"));

            Configuration config = new org.geotools.wfs.v1_1.WFSConfiguration();
            org.geotools.xml.Encoder encoder = new org.geotools.xml.Encoder(config);
            encoder.setIndenting(true);
            encoder.setIndentSize(2);
            Document d = encoder.encodeAsDOM(gf, WFS.GetFeature);

            d.getDocumentElement().setAttribute("xmlns:test", "http://www.geotools.org/test");

            System.out.println(XmlUtils.xmlToString(d));

        } catch (CQLException ex) {
            Logger.getLogger(WfsGet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WfsGet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(WfsGet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(WfsGet.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
