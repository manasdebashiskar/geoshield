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

import ch.supsi.ist.geoshield.data.DataManager;
import com.vividsolutions.jts.geom.Coordinate;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.geotools.data.DataUtilities;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.AndImpl;
import org.geotools.filter.FilterTransformer;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.SQLEncoderException;
import org.geotools.filter.SQLEncoderPostgis;
import org.geotools.filter.spatial.IntersectsImpl;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.geotools.geometry.GeometryBuilder;
import org.geotools.geometry.GeometryFactoryFinder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.*;
import org.opengis.filter.expression.*;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.geometry.coordinate.GeometryFactory;
import org.opengis.geometry.primitive.Point;
import org.opengis.geometry.primitive.PrimitiveFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author milan
 */
public class TestFilter {

    /**
     * @param args the command line arguments
     */
    @SuppressWarnings("empty-statement")
    public static void main(String[] args) {
        try {
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

            // TODO code application logic here
            //String ret = "INTERSECTS(the_geom,POINT(48.44 -123.37))";
            String ret = "INTERSECTS(the_geom,POINT(48.44 -123.37)) and (id>2*4) and name='pippo' and startDate > '2010-10-01'";
            String cql = " and attName = 5";
            Filter filter = CQL.toFilter(ret);

            System.out.println(">" + filter.toString() + "<");
            System.out.println("*************************");

            SQLEncoderPostgis encoder = new SQLEncoderPostgis();


            try {

                SimpleFeatureType road3 = DataUtilities.createType(
                        "test.road",
                        "id:Integer,the_geom:Point:srid=4326,name:String,startDate:Date");

                encoder.setFeatureType(road3);

                System.out.println("Feature: " + road3.toString());

            } catch (SchemaException ex) {
                System.out.println("Ex: " + ex.toString());
                Logger.getLogger(TestFilter.class.getName()).log(Level.SEVERE, null, ex);
            }




            encoder.setLooseBbox(true);
            encoder.setSRID(2356);
            encoder.setSupportsGEOS(true);

            String out;
            try {
                out = encoder.encode(filter);
                System.out.println("SQL: " + out);
            } catch (SQLEncoderException ex) {
                Logger.getLogger(TestFilter.class.getName()).log(Level.SEVERE, null, ex);
            }






            /*
            class Optimization extends DuplicatingFilterVisitor {

            private Optimization() {
            throw new UnsupportedOperationException("Not yet implemented");
            }

            private Optimization(FilterFactory2 ff) {
            }

            class PName extends DuplicatingFilterVisitor {

            @Override
            public Object visit(PropertyName expression, Object data) {
            System.out.println("PropertyName: " + expression.getPropertyName());
            return null;
            }
            };

            @Override
            public Object visit(Intersects f, Object extraData) {
            //System.out.println("Expression1: "+filter.accept(new PName(), null));
            //System.out.print("PropertyName: "+filter.getExpression1().accept(new PName(), null));
            System.out.println("ClassName: " + f.getClass().getName());
            System.out.println("Expression2: " + f.getExpression2().accept(new PName(), null));
            //filter.getExpression2().
            if (f instanceof AndImpl) {
            System.out.println("AndImpl!!!");
            return f;
            }
            if (f instanceof IntersectsImpl) {
            return f;
            }
            return null;
            }

            @Override
            public Object visit(Equals filter, Object extraData) {
            System.out.println("ClassName heheh: " + filter.getClass().getName());
            return null;
            }

            @Override
            public Object visit(And filter, Object extraData) {
            System.out.println("ClassName heheh: " + filter.getClass().getName());
            //System.out.println("Expression2: " + filter.;
            //filter.getExpression2().

            return null;
            }
            }

            class Interseptor extends DuplicatingFilterVisitor {

            class PName extends DuplicatingFilterVisitor {

            public Object visit(Expression expression, Object data) {
            System.out.println("PropertyName: " + expression.toString());
            return null;
            }
            };

            @Override
            public Object visit(Intersects f, Object extraData) {
            //System.out.println("Expression1: "+filter.accept(new PName(), null));
            //System.out.print("PropertyName: "+filter.getExpression1().accept(new PName(), null));
            System.out.println("1-ClassName: " + f.getClass().getName());
            System.out.println("2-Expression1: " + f.getExpression1().accept(new PName(), null));
            System.out.println("3-Expression2: " + f.getExpression2().accept(new PName(), null));
            //filter.getExpression2().
            if (f instanceof AndImpl) {
            System.out.println("AndImpl!!!");
            return f;
            }
            if (f instanceof IntersectsImpl) {
            return f;
            }
            return null;
            }
            }


             */
            /*
            Object o = filter.accept(new Interseptor(), null);
            System.out.println(o);
             */
            //IntersectsImpl im = (IntersectsImpl)filter.accept(new Optimization(), null);
            //System.out.println("ClassName: " + im.getClass().getName());
            //System.out.println("ClassName: " + im.toString());

            try {
                //PrimitiveFactory geomFactory = GeometryFactoryFinder.getPrimitiveFactory(null);

                com.vividsolutions.jts.geom.GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

                Coordinate coord = new Coordinate(1, 1);
                com.vividsolutions.jts.geom.Point point = geometryFactory.createPoint(coord);


                SimpleFeatureType schema = DataUtilities.createType("Flag", "the_geom:Point");
                SimpleFeatureBuilder build = new SimpleFeatureBuilder(schema);

                //add the attributes
                double dArray[] = {48.44, -123.37};
                build.add(point);
                //build.add(5);

                SimpleFeature feature = build.buildFeature(null);
                //System.out.println(feature.getDefaultGeometryProperty().getName());

                System.out.println("Evaluating: " + filter.evaluate(feature));
                //System.out.println("Evaluating: " + im.evaluate(feature));
                //;

            } catch (SchemaException ex) {
                Logger.getLogger(TestFilter.class.getName()).log(Level.SEVERE, null, ex);
            }


            /*
            try {
            GeometryBuilder builder = new  GeometryBuilder(DefaultGeographicCRS.WGS84);

            org.opengis.geometry.

            Point point = builder.createPoint(48.44, -123.37);

            // Creating schema
            SimpleFeatureType schema = DataUtilities.createType("Flag", "the_geom:Point");
            System.out.println( DataUtilities.spec( schema ) );

            // Getting builder
            SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(schema);

            //add the attributes
            featureBuilder.add(point);

            //build the feature
            SimpleFeature feature = featureBuilder.buildFeature("Flag.12");
            System.out.println(feature.toString());
            } catch (SchemaException ex) {
            Logger.getLogger(TestFilter.class.getName()).log(Level.SEVERE, null, ex);
            }
             */
            return;
            /*
            Configuration configuration = new org.geotools.filter.v1_0.OGCConfiguration();
            Parser parser = new Parser(configuration);


            String xmlFilter = "<ogc:Filter xmlns:ogc='http://www.opengis.net/ogc' ";
            xmlFilter += "  xmlns:gml='http://www.opengis.net/gml'>";
            xmlFilter += "  <ogc:PropertyIsEqualTo>";
            xmlFilter += "    <ogc:PropertyName>attName</ogc:PropertyName>";
            xmlFilter += "    <ogc:Literal>5</ogc:Literal>";
            xmlFilter += "  </ogc:PropertyIsEqualTo>";
            xmlFilter += "</ogc:Filter>";


            ByteArrayInputStream xml = new ByteArrayInputStream(xmlFilter.getBytes("UTF-8"));
            Filter filter2 = (Filter) parser.parse(xml);


            System.out.println("*************************");
            System.out.println(">" + filter2.toString() + "<");
            System.out.println("*************************");


            Encoder encoder = new Encoder(configuration);

            FilterTransformer ft = new FilterTransformer();
            ft.setOmitXMLDeclaration(true);
            ft.setIndentation(2);
            System.out.println("*************************");
            System.out.println(ft.transform(filter));

            System.out.println("*************************");
            System.out.println(ft.transform(filter2));
            System.out.println("*************************");



            List<Filter> list = new LinkedList<Filter>();
            list.add(filter);
            list.add(filter2);

            And and = ff.and(list);

            System.out.println("*************************");
            System.out.println(ft.transform(and));
            System.out.println("*************************");
             */
            /*
            StringBuffer str = new StringBuffer();
            str.append("(");
            str.append(ft.transform(filter));
            str.append(")(");
            str.append(ft.transform(filter2));
            str.append(")");

            System.out.println(str);
             */


            //DataManager dm = new DataManager();

            //dm.decodeFilter(str.toString());

            /*} catch (TransformerException ex) {
            Logger.getLogger(TestFilter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
            Logger.getLogger(TestFilter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
            Logger.getLogger(TestFilter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException ex) {
            Logger.getLogger(TestFilter.class.getName()).log(Level.SEVERE, null, ex);
             */
        } catch (CQLException ex) {
            Logger.getLogger(TestFilter.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
}
