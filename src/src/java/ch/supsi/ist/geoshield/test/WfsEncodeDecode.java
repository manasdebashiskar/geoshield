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

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import net.opengis.wfs.QueryType;
import net.opengis.wfs.impl.FeatureCollectionTypeImpl;
import net.opengis.wfs.impl.GetFeatureTypeImpl;
import org.geotools.wfs.v1_1.WFS;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.xml.sax.InputSource;

/**
 *
 * @author milan
 */
public class WfsEncodeDecode {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //WfsEncodeDecode.decoEncoGetFeature();
        WfsEncodeDecode.decoEncoFeatureCollection();

    }

    public static void decoEncoFeatureCollection(){
        try {
            // TODO code application logic here

            String xml = "<wfs:FeatureCollection " +
                    "numberOfFeatures=\"1\" timeStamp=\"2010-09-03T11:30:46.365+02:00\" " +
                    "xsi:schemaLocation=\"http://www.openplans.org/topp http://localhost:80/geoserver/wfs?service=WFS&amp;version=1.1.0&amp;request=DescribeFeatureType&amp;typeName=topp%3Atasmania_cities http://www.opengis.net/wfs http://localhost:80/geoserver/schemas/wfs/1.1.0/wfs.xsd\" " +
                    "xmlns:ogc=\"http://www.opengis.net/ogc\" " +
                    "xmlns:tiger=\"http://www.census.gov\" " +
                    "xmlns:cite=\"http://www.opengeospatial.net/cite\" " +
                    "xmlns:nurc=\"http://www.nurc.nato.int\" " +
                    "xmlns:sde=\"http://geoserver.sf.net\" " +
                    "xmlns:wfs=\"http://www.opengis.net/wfs\" " +
                    "xmlns:topp=\"http://www.openplans.org/topp\" " +
                    "xmlns:it.geosolutions=\"http://www.geo-solutions.it\" " +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                    "xmlns:sf=\"http://www.openplans.org/spearfish\" " +
                    "xmlns:ows=\"http://www.opengis.net/ows\" " +
                    "xmlns:gml=\"http://www.opengis.net/gml\" " +
                    "xmlns:xlink=\"http://www.w3.org/1999/xlink\">" +
                    "<gml:featureMembers>" +
                    "   <topp:tasmania_cities gml:id=\"tasmania_cities.1\">" +
                    "       <topp:the_geom>" +
                    "           <gml:MultiPoint srsName=\"urn:x-ogc:def:crs:EPSG:4326\">" +
                    "               <gml:pointMember>" +
                    "                   <gml:Point>" +
                    "                       <gml:pos>-42.851001816890005 147.2910004483</gml:pos>" +
                    "                   </gml:Point>" +
                    "               </gml:pointMember>" +
                    "           </gml:MultiPoint>" +
                    "       </topp:the_geom>" +
                    "       <topp:CITY_NAME>Hobart</topp:CITY_NAME>" +
                    "       <topp:ADMIN_NAME>Tasmania</topp:ADMIN_NAME>" +
                    "       <topp:CNTRY_NAME>Australia</topp:CNTRY_NAME>" +
                    "       <topp:STATUS>Provincial capital</topp:STATUS>" +
                    "       <topp:POP_CLASS>100,000 to 250,000</topp:POP_CLASS>" +
                    "   </topp:tasmania_cities>" +
                    "</gml:featureMembers>" +
                    "</wfs:FeatureCollection>";

            Configuration config = new org.geotools.wfs.v1_1.WFSConfiguration();
            Parser parser = new Parser( config );

            FeatureCollectionTypeImpl parsed = ( FeatureCollectionTypeImpl) parser.parse(
                    new InputSource(new StringReader(xml)));

            System.out.println("Parsed: " + parsed);
            
            org.geotools.xml.Encoder encoder = new org.geotools.xml.Encoder(config);
            encoder.setIndenting(true);
            encoder.setIndentSize(2);
            encoder.getNamespaces().declarePrefix("topp", "http://www.openplans.org/topp");
            //encoder.setSchemaLocation("topp", "http://www.openplans.org/topp");

            encoder.encode( parsed, WFS.FeatureCollection, System.out );


        } catch (Exception ex) {
            Logger.getLogger(WfsEncodeDecode.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 

    public static void decoEncoGetFeature(){
        try {
            // TODO code application logic here

            String xml = "<wfs:GetFeature service=\"WFS\" version=\"1.1.0\" "
                    + "xmlns:topp=\"http://www.openplans.org/topp\" "
                    + "xmlns:wfs=\"http://www.opengis.net/wfs\" "
                    + "xmlns:ogc=\"http://www.opengis.net/ogc\" "
                    + "xmlns:gml=\"http://www.opengis.net/gml\" "
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                    + "xsi:schemaLocation=\"http://www.opengis.net/wfs "
                    + "http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\"> "
                    + "<wfs:Query typeName=\"topp:states\"> "
                    + "<ogc:Filter> "
                    + "<ogc:BBOX>"
                    + "<ogc:PropertyName>the_geom</ogc:PropertyName>"
                    + "<gml:Envelope>"
                    + "<gml:lowerCorner>0 0</gml:lowerCorner>"
                    + "<gml:upperCorner>10 10</gml:upperCorner>"
                    + "</gml:Envelope>"
                    + "</ogc:BBOX>"
                    //+ "<ogc:FeatureId fid=\"states.39\"/> "
                    + "</ogc:Filter> "
                    + "</wfs:Query> "
                    + "</wfs:GetFeature>";

            Configuration config = new org.geotools.wfs.v1_1.WFSConfiguration();
            Parser parser = new Parser( config );

            GetFeatureTypeImpl parsed = (GetFeatureTypeImpl) parser.parse(
                    new InputSource(new StringReader(xml)));

            System.out.println("Parsed: " + parsed);

            QueryType qt = (QueryType) parsed.getQuery().get(0);
            List tyns = qt.getTypeName();
            String[] ls = new String[tyns.size()];
            for (Iterator it = tyns.iterator(); it.hasNext();) {
                javax.xml.namespace.QName qn = (javax.xml.namespace.QName) it.next();
                System.out.println(qn.getLocalPart());
            }

            org.geotools.xml.Encoder encoder = new org.geotools.xml.Encoder(config);
            encoder.setIndenting(true);
            encoder.setIndentSize(2);
            //encoder.getNamespaces().declarePrefix("topp", "http://www.openplans.org/topp");
            //encoder.setSchemaLocation("topp", "http://www.openplans.org/topp");

            encoder.encode( parsed, WFS.GetFeature, System.out );


        } catch (Exception ex) {
            Logger.getLogger(WfsEncodeDecode.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
