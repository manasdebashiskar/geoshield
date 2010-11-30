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

package ch.supsi.ist.geoshield.auth;

import ch.supsi.ist.geoshield.data.Layers;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.filter.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import javax.servlet.http.HttpServletResponse;
import org.geotools.filter.AndImpl;
import org.geotools.filter.GeometryFilterImpl;
import org.geotools.filter.OrImpl;

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */
public class WmsAuth {

    public synchronized boolean checkGetFeatureInfo(double[] coord, List<Filter> sysFlts, List<Layers> layers) {
        try {
            GeometryFactory gf = new GeometryFactory();
            //System.out.println(">>>>>>>>>>>>>>>>>>>>>>>><<<<");
            List<Layers> layers2Remove = new ArrayList<Layers>(0);

            for (int i = 0; i < layers.size(); i++) {
                Layers layer = layers.get(i);
                //System.out.println("Layer to check: " + layer.getNameLay());
                Filter filter = sysFlts.get(i);
                SimpleFeatureType schema = DataUtilities.createType(
                        "test",
                        layer.getGeomLay() + ":Point");
                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(schema);
                featureBuilder.add(gf.createPoint(new Coordinate(coord[0], coord[1])));
                SimpleFeature feature = featureBuilder.buildFeature("test.1");
                List<Filter> fltsCheck = this.getSpatialFilter(filter);
                for (Iterator<Filter> it2 = fltsCheck.iterator(); it2.hasNext();) {
                    GeometryFilterImpl filter2 = (GeometryFilterImpl) it2.next();
                    //System.out.println(" - Checking spatial filter: " + filter2.getClass());
                    //System.out.println("                            " + filter2.toString());
                    if (!filter2.evaluate(feature)) {
                        // @todo forse è meglio lanciare un'eccezione in cui si specifica dove
                        // non si ha i permessi necessari
                        //System.out.println("Layer to remove: " + layer.getNameLay());
                        layers2Remove.add(layer);

                    }
                }
            }
            if (layers2Remove.size() > 0) {
                layers.removeAll(layers2Remove);
                return false;
            } else {
                return true;
            }
        } catch (SchemaException ex) {
            Logger.getLogger(WmsAuth.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    public static synchronized void postProcessGetCapabilities(
            HttpURLConnection conn, HttpServletResponse res, String srvUrl, String realSrvUrl) {
        //System.out.println("Modifying: " + realSrvUrl + " with " + srvUrl);
        String s = "";
        PrintWriter out = null;
        BufferedReader inp = null;
        try {
            inp = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            out = res.getWriter();
            //System.out.println("Reading lines..");
            while ((s = inp.readLine()) != null) {
                s = s.replaceAll(realSrvUrl + "/GetLegendGraphic\\?", srvUrl + "?SERVICE=WMS&amp;REQUEST=GetLegendGraphic&amp;");
                s = s.replaceAll(realSrvUrl, srvUrl);
                out.print(s);
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        } finally {
            if (out != null) {
                out.close();
            }
            if (inp != null) {
                try {
                    inp.close();
                } catch (IOException ex) {
                    Logger.getLogger(WmsAuth.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public synchronized List<Filter> getSpatialFilter(Filter filter) {
        List<Filter> ret = new LinkedList<Filter>();
        if (filter instanceof AndImpl) {
            List<Filter> tmp = new LinkedList<Filter>();
            for (Iterator it2 = ((AndImpl) filter).getFilterIterator(); it2.hasNext();) {
                tmp.add((Filter) it2.next());
            }
            ret.addAll(getSpatialFilter(tmp));
        } else if (filter instanceof OrImpl) {
            List<Filter> tmp = new LinkedList<Filter>();
            for (Iterator it2 = ((OrImpl) filter).getFilterIterator(); it2.hasNext();) {
                tmp.add((Filter) it2.next());
            }
            ret.addAll(getSpatialFilter(tmp));
        } else if (filter instanceof GeometryFilterImpl) {
            ret.add(filter);
        }
        return ret;
    }

    public synchronized List<Filter> getSpatialFilter(List<Filter> flts) {
        List<Filter> ret = new LinkedList<Filter>();
        for (Iterator<Filter> it = flts.iterator(); it.hasNext();) {
            ret.addAll(this.getSpatialFilter(it.next()));
        }
        return ret;
    }
}
