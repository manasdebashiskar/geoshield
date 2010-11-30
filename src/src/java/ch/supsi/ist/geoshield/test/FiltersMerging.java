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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.filter.v1_1.OGCConfiguration;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.spatial.BBOX;

/**
 *
 * @author milan
 */
public class FiltersMerging {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
            org.geotools.xml.Configuration configuration = new OGCConfiguration();
            org.geotools.xml.Encoder encoder = new org.geotools.xml.Encoder(configuration);
            encoder.setIndenting(true);
            encoder.setIndentSize(2);
            
            Set<FeatureId> fids = new HashSet<FeatureId>();
            fids.add(ff.featureId("ROAD.1"));
            fids.add(ff.featureId("ROAD.2"));
            Filter filter = ff.id(fids);

            Filter filter2 = ff.less(ff.property( "AGE"), ff.literal( 12 ) );


            //encoder.encode(filter, org.geotools.filter.v1_1.OGC.Filter, System.out);

            BBOX bb = ff.bbox("the_geom", 0, 0, 10, 10, "4326");
            BBOX bb2 = ff.bbox("the_geom", 10, 10, 20, 20, "4326");

            encoder.encode( ff.or(filter2,bb), org.geotools.filter.v1_1.OGC.Filter, System.out);
            /*
            encoder.encode(bb, org.geotools.filter.v1_1.OGC.Filter, System.out);*/
        } catch (IOException ex) {
            Logger.getLogger(FiltersMerging.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
