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

import ch.supsi.ist.geoshield.utils.xml.FilterTransformer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.v1_1.OGCConfiguration;
import org.geotools.wfs.v1_1.WFSConfiguration;
import org.opengis.filter.*;
import org.opengis.filter.spatial.BBOX;
import org.xml.sax.SAXException;

public class TestFilter2 {

    public static void main(String[] args) {
        //try {

        org.geotools.xml.Configuration configuration =
                new OGCConfiguration();
        org.geotools.xml.Parser parser =
                new org.geotools.xml.Parser(configuration);
        FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);

        //create the encoder with the filter 1.0 configuration
        org.geotools.xml.Configuration conf2 = new org.geotools.filter.v1_1.OGCConfiguration();
        org.geotools.xml.Encoder encoder = new org.geotools.xml.Encoder(configuration);



        String xml =
                "<ogc:Filter xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\">"
                + "<ogc:BBOX>"
                + "<ogc:PropertyName>the_geom</ogc:PropertyName>"
                + "<gml:Envelope>"
                + "<gml:lowerCorner>0 0</gml:lowerCorner>"
                + "<gml:upperCorner>10 10</gml:upperCorner>"
                + "</gml:Envelope>"
                + "</ogc:BBOX>"
                + "</ogc:Filter>";
        
        Filter f;
        try {
            BBOX bb = filterFactory.bbox("the_geom", 0, 0, 10, 10, "4326");
            encoder.setIndenting(true);
            encoder.setIndentSize(2);
            encoder.encode( bb, org.geotools.filter.v1_1.OGC.Filter, System.out );

            f = (Filter) parser.parse(new ByteArrayInputStream(
                    xml.getBytes()));
            System.out.println(bb);
            System.out.println(f);

            FilterTransformer ft = new FilterTransformer();
            ft.setOmitXMLDeclaration(true);
            //ft.setEncoding(??);
            ft.setNamespaceDeclarationEnabled(true);
            ft.setIndentation(0);
            try {
                ft.setIndentation(2);
                System.out.println(ft.transform(bb));
                System.out.println(ft.transform(f));
            } catch (TransformerException ex) {
                System.err.println("TransformerException: " + ex.getMessageAndLocation());
            }

        } catch (IOException ex) {
            Logger.getLogger(TestFilter2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(TestFilter2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(TestFilter2.class.getName()).log(Level.SEVERE, null, ex);
        }


        /*
        Filter result = CQL.toFilter( "CROSSES(the_geom, LINESTRING(1 2, 10 15))" );
        System.out.println("Filter: " + result.toString());
        FilterTransformer transform = new FilterTransformer();
        transform.setIndentation(2);
        com.vividsolutions.jts.geom.Envelope c;
        try {
        String xml = transform.transform(result);
        System.out.println(xml);
        } catch (TransformerException ex) {
        Logger.getLogger(TestFilter2.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Filter result2 = CQL.toFilter( "INTERSECTS(ATTR1, BBOX(10.0,20.0,30.0,40.0))" );


        Filter result2 = CQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)");
        System.out.println("Filter2: " + result2.getClass().getName());
        System.out.println("Filter2: " + result2.toString());
        try {
        String xml = transform.transform(result2);
        System.out.println(xml);
        } catch (TransformerException ex) {
        Logger.getLogger(TestFilter2.class.getName()).log(Level.SEVERE, null, ex);
        }*/


        /*
        Filter result3 = CQL.toFilter( "CONTAINS(the_geom, POLYGON ((10 20, 10 40, 30 40, 30 20, 10 20)) )" );

        System.out.println("Filter3: " + result3.toString());
        try {
        String xml = transform.transform(result3);
        System.out.println(xml);
        } catch (TransformerException ex) {
        Logger.getLogger(TestFilter2.class.getName()).log(Level.SEVERE, null, ex);
        }*/


        /*
        String cqlx = "x=y";
        Filter filterx = CQL.toFilter(cqlx);
        System.out.println("filter: " + filterx.getClass());
        System.out.println("        " + filterx.toString());
        FilterTransformer transform = new FilterTransformer();
        transform.setIndentation(2);
        try {
        String xml = transform.transform(filterx);
        System.out.println(xml);
        } catch (TransformerException ex) {
        Logger.getLogger(TestFilter2.class.getName()).log(Level.SEVERE, null, ex);
        }
        //String cql = "INTERSECT(the_geom,POINT(717724,83464))";
        //IntersectsImpl filter = (IntersectsImpl) CQL.toFilter(cql);
        String cql = "BBOX(the_geom,707724.972896,82464.552999,732146.795356,113847.736066)";
        Filter filter = CQL.toFilter(cql);
        //BBOXImpl filter = (BBOXImpl) CQL.toFilter(cql);

        try {
        String xml = transform.transform(filter);
        System.out.println(xml);
        } catch (TransformerException ex) {
        Logger.getLogger(TestFilter2.class.getName()).log(Level.SEVERE, null, ex);
        }


        System.out.println("filter: " + filter.getClass());

        //String cql2 = "INTERSECT(the_geom,POINT(717724,83464)) " +
        String cql2 = "BBOX(the_geom,707724.972896,82464.552999,732146.795356,113847.736066) "
        + "and param = 'ciao'";
        AndImpl filter2 = (AndImpl) CQL.toFilter(cql2);

        System.out.println("filter2: " + filter2.getClass());

        for (Iterator it = filter2.getFilterIterator(); it.hasNext();) {
        Object object = it.next();
        System.out.println(" -" + object.getClass());
        }

        System.out.println();
        WmsAuth wmsau = new WmsAuth();
        List<Filter> ret = new LinkedList<Filter>();
        ret.add(filter);
        ret.add(filter2);
        List<Filter> test = wmsau.getSpatialFilter(ret);
        System.out.println("Testing WmsAuth: extracting only spatial operator.");
        for (Iterator<Filter> it = test.iterator(); it.hasNext();) {
        Filter filter1 = it.next();
        System.out.println(" -" + filter1.getClass() + ": " + filter1.toString());
        }

        System.out.println();

        // Creazione della feature type 1
        SimpleFeatureType schema = DataUtilities.createType("test", "the_geom:Point,param:String");
        System.out.println(DataUtilities.spec(schema));

        // Creazione della feature type 2
        SimpleFeatureType schema2 = DataUtilities.createType("test2", "the_geom:Point");
        System.out.println(DataUtilities.spec(schema2));

        GeometryFactory gf = new GeometryFactory();

        // Creo la feature dallo tipo 1
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(schema);
        featureBuilder.add(gf.createPoint(new Coordinate(717724, 83464)));
        featureBuilder.add("ciao");
        SimpleFeature feature = featureBuilder.buildFeature("test.1");

        // Creo la feature dallo tipo 2
        SimpleFeatureBuilder featureBuilder2 = new SimpleFeatureBuilder(schema2);
        featureBuilder2.add(gf.createPoint(new Coordinate(717724, 83464)));
        SimpleFeature feature2 = featureBuilder2.buildFeature("test.2");

        System.out.println("\nEvaluating feature: " + feature.getID());
        System.out.println("Evaluate f1: " + filter.evaluate(feature));
        System.out.println("Evaluate f2: " + filter2.evaluate(feature));

        System.out.println("\nEvaluating feature: " + feature2.getID());
        System.out.println("Evaluate f1: " + filter.evaluate(feature2));
        System.out.println("Evaluate f2: " + filter2.evaluate(feature2));
         */
        // @todo Extract IntersectsImpl from AndImpl
/* catch (SchemaException ex) {
        Logger.getLogger(TestFilter2.class.getName()).log(Level.SEVERE, null, ex);
        }*/ /*catch (CQLException ex) {
        Logger.getLogger(TestFilter2.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
}
