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

package ch.supsi.ist.geoshield.utils;

// GEOTOOLS --------------------------------------------------------------------
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.opengis.filter.*;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.AndImpl;
import org.geotools.filter.FilterTransformer;
import org.geotools.filter.GeometryFilterImpl;
import org.geotools.filter.OrImpl;

/**
 *
 * @author Milan Antonovic - milan.antonovic@supsi.ch
 */
public class FiltersUtils {

    public static int getPriorityFilter(String cqlFilter) throws CQLException {
        int ret = 0;
        if (cqlFilter.equalsIgnoreCase("INCLUDE")) {
            ret = 4;
        } else if (cqlFilter.equalsIgnoreCase("EXCLUDE")) {
            ret = 1;
        } else {
            List<Filter> f = getSpatialFilter(CQL.toFilter(cqlFilter));
            if (f.size() > 0) {
                ret = 2;
            }else{
                ret = 3;
            }
        }
        return ret;
    }

    public static synchronized List<Filter> getSpatialFilter(Filter filter) {
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

    public static synchronized List<Filter> getSpatialFilter(List<Filter> flts) {
        List<Filter> ret = new LinkedList<Filter>();
        for (Iterator<Filter> it = flts.iterator(); it.hasNext();) {
            ret.addAll(getSpatialFilter(it.next()));
        }
        return ret;
    }

    public static void main(String[] args) throws Exception {
        int p = 0;
        String cql = "INCLUDE";
        p = FiltersUtils.getPriorityFilter(cql);
        System.out.println(cql+"  PRIORITY="+p);

        cql = "EXCLUDE";
        p = FiltersUtils.getPriorityFilter(cql);
        System.out.println(cql+"  PRIORITY="+p);

        cql = "val=4";
        p = FiltersUtils.getPriorityFilter(cql);
        System.out.println(cql+"  PRIORITY="+p);

        cql = "BBOX(the_geom, 147.15,-43, 147.5,-42.75)";
        p = FiltersUtils.getPriorityFilter(cql);
        System.out.println(cql+"  PRIORITY="+p);

        cql = "val=4 AND BBOX(the_geom, 147.15,-43, 147.5,-42.75)";
        p = FiltersUtils.getPriorityFilter(cql);
        System.out.println(cql+"  PRIORITY="+p);

    }
}
