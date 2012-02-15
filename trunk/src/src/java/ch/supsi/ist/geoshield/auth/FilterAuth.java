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

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

import ch.supsi.ist.geoshield.data.DataManager;
import ch.supsi.ist.geoshield.data.Groups;
import ch.supsi.ist.geoshield.data.Layers;
import ch.supsi.ist.geoshield.data.LayersPermissions;
import ch.supsi.ist.geoshield.data.Users;
import ch.supsi.ist.geoshield.exception.ServiceException;
//import ch.supsi.ist.geoshield.test.FilterTransformer;
import org.geotools.filter.FilterTransformer;


import ch.supsi.ist.geoshield.utils.FiltersUtils;
import ch.supsi.ist.geoshield.utils.Utility;
import ch.supsi.ist.geoshield.utils.xml.XmlUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.NoResultException;
import javax.xml.transform.TransformerException;

// GEOTOOLS --------------------------------------------------------------------
import org.opengis.filter.*;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.factory.CommonFactoryFinder;
import org.xml.sax.SAXException;

/**
 *
 * @author Milan Antonovic, Massimiliano Cannata
 */
public class FilterAuth {

    public void checkExcludeFilters(List<Layers> layers, List<Filter> filters, HttpServletRequest request) {
        if (filters.size() == 1) {
        } else if (filters.size() > 1) {
        }
    }

    /**
     * Retrive all the filters setted up for the given user and the given layer.
     * If the user belong to different groups that have access to the given map
     * then this method will merge the filters in an Or combination.
     *
     * @param usr The Users object to check
     * @param sur The Layers object to check
     *
     * @return Filter obj.
     *
     * @author Milan Antonovic
     */
    @Deprecated
    public synchronized Filter getFilter(Users usr, Layers lay)
            throws ServiceException {
        List<Groups> grps = usr.getGroups();
        List<Filter> fils = new LinkedList<Filter>();
        DataManager dm = new DataManager();
        try {
            boolean incl = false;
            boolean extcl = false;
            boolean spatial = false;
            boolean other = false;
            for (Iterator<Groups> it = grps.iterator(); it.hasNext();) {
                Groups grp = it.next();
                LayersPermissions layPrm = null;
                try {
                    layPrm = dm.getLayersPermissionsByGrpLay(grp, lay);
                    if (FiltersUtils.getPriorityFilter(layPrm.getFilterLpr()) == 1) { // EXCLUDE
                        extcl = true;
                    } else if (FiltersUtils.getPriorityFilter(layPrm.getFilterLpr()) == 4) { // INCLUDE
                        incl = true;
                        break;
                    } else if (FiltersUtils.getPriorityFilter(layPrm.getFilterLpr()) == 2) { // SPATIAL
                        spatial = true;
                    } else {
                        other = true;
                    }
                } catch (NoResultException noResultException) {
                    it.remove();
                    //System.out.println("Failed for: group=" + grp.getNameGrp() + " layer=" + lay.getNameLay());
                    continue;
                }
            }
            if (incl) {
                fils.add(CQL.toFilter("INCLUDE"));
                //System.out.println("Adding only filter INCLUDE");
            } else if (extcl && !(spatial || other)) {
                fils.add(CQL.toFilter("mb=ma"));
                //System.out.println("Adding only filter EXCLUDE");
            } else {
                for (Iterator<Groups> it = grps.iterator(); it.hasNext();) {
                    Groups grp = it.next();
                    LayersPermissions layPrm = null;
                    try {
                        layPrm = dm.getLayersPermissionsByGrpLay(grp, lay);
                        fils.add(CQL.toFilter(layPrm.getFilterLpr()));
                        //System.out.println("   > " + CQL.toFilter(layPrm.getFilterLpr()).toString());
                        //}
                    } catch (NoResultException noResultException) {
                        //System.out.println("Failed for: group=" + grp.getNameGrp() + " layer=" + lay.getNameLay());
                        continue;
                    }
                }
            }
        } catch (CQLException ex) {
            throw new ServiceException("Error getting users filter.");
        }/* catch (NoResultException nre){
        throw new ServiceException("Error getting users filter.");
        }*/ finally {
            dm.close();
        }
        if (fils.size() == 1) {
            return fils.get(0);
        } else if (fils.size() > 1) {
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
            return ff.or(fils);
        } else {
            throw new ServiceException("Error getting users filter.");
        }
    }

    public synchronized Filter getFilter(Users usr, Layers lay, DataManager dm)
            throws ServiceException {
        List<Groups> grps = usr.getGroups();
        List<Filter> fils = new LinkedList<Filter>();
        try {
            boolean incl = false;
            boolean extcl = false;
            boolean spatial = false;
            boolean other = false;
            for (Iterator<Groups> it = grps.iterator(); it.hasNext();) {
                Groups grp = it.next();
                LayersPermissions layPrm = null;
                try {
                    layPrm = dm.getLayersPermissionsByGrpLay(grp, lay);
                    int priorityFilter = FiltersUtils.getPriorityFilter(layPrm.getFilterLpr());
                    if (priorityFilter == 1) { // EXCLUDE
                        extcl = true;
                    } else if (priorityFilter == 4) { // INCLUDE
                        incl = true;
                        break;
                    } else if (priorityFilter == 2) { // SPATIAL
                        spatial = true;
                        fils.add(CQL.toFilter(layPrm.getFilterLpr()));
                    } else {
                        other = true;
                        fils.add(CQL.toFilter(layPrm.getFilterLpr()));
                    }
                } catch (NoResultException noResultException) {
                    it.remove();
                    extcl = true;
                    continue;
                }
            }
            if (incl) {
                // INCLUDE WIN OVER ALL FILTERS
                fils = new LinkedList<Filter>();
                fils.add(CQL.toFilter("INCLUDE"));

            } else if (extcl && !(spatial || other)) {
                fils = new LinkedList<Filter>();
                fils.add(CQL.toFilter("fid=-1"));
            }
        } catch (CQLException ex) {
            throw new ServiceException("Error getting users filter.");
        }
        if (fils.size() == 1) {
            return fils.get(0);
        } else if (fils.size() > 1) {
            FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
            return ff.or(fils);
        } else {
            throw new ServiceException("Error getting users filter.");
        }
    }

    /**
     * Retrive all the filters setted up for the given user and the given layers
     * list.
     * If the user belong to different groups that have access to the given map
     * then this method will merge the filters in an Or combination for each
     * layer given in a Filter List.
     *
     * @param usr The Users object to check
     * @param lays The List&gt;Layers&lt; object to check
     *
     * @return List&gt;Filter&lt; obj.
     *
     * @author Milan Antonovic
     */
    public synchronized List<Filter> getFilters(Users usr, List<Layers> lays)
            throws ServiceException {
        List<Filter> fils = new LinkedList<Filter>();
        for (Iterator<Layers> it = lays.iterator(); it.hasNext();) {
            Layers lay = it.next();
            fils.add(this.getFilter(usr, lay));
        }
        if (fils.size() != lays.size()) {
            throw new ServiceException("Sorry: filters number does not match "
                    + "layers number, maybe there is an error in the DB data.");
        }
        return fils;
    }

    public synchronized List<Filter> getFilters(Users usr, List<Layers> lays, DataManager dm)
            throws ServiceException {
        List<Filter> fils = new LinkedList<Filter>();
        for (Iterator<Layers> it = lays.iterator(); it.hasNext();) {
            Layers lay = it.next();
            fils.add(this.getFilter(usr, lay, dm));
        }
        if (fils.size() != lays.size()) {
            throw new ServiceException("Sorry: filters number does not match "
                    + "layers number, maybe there is an error in the DB data.");
        }
        return fils;
    }

    public synchronized List<Filter> mergeUserAndSystemFilters(List<Filter> usrflts, List<Filter> sysflts) {
        //System.out.println("Merging...");
        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        // User has just one filter set. So i merge the users filter with each of the system filters
        List<Filter> ret = new ArrayList<Filter>();
        
        //System.out.println(" - User filter size: " + usrflts.size());
        //System.out.println(" - System filter size: " + sysflts.size());

        if (sysflts.size() == usrflts.size()) {
            for (int i = 0; i < sysflts.size(); i++) {
                //System.out.println("instance of:" + sysflts.get(i).getClass().getName());
                if (sysflts.get(i) instanceof ExcludeFilter) {
                    //System.out.println("sysflts instanceof ExcludeFilter");
                    ret.add(sysflts.get(i));
                } else if (usrflts.get(i) instanceof ExcludeFilter) {
                    //System.out.println("usrflts instanceof ExcludeFilter");
                    ret.add(usrflts.get(i));
                } else if (usrflts.get(i) instanceof IncludeFilter) {
                    //System.out.println("usrflts instanceof IncludeFilter");
                    ret.add(sysflts.get(i));
                } else {
                    //System.out.println("ELSE...");
                    //System.out.println(" > Merging: " + sysflts.get(i).getClass().getName() + " and " +
                            //usrflts.get(i).getClass().getName());
                    ret.add(ff.and(sysflts.get(i), usrflts.get(i)));
                }
            }
        } else if (usrflts.size() == 1) {
            for (Iterator<Filter> it = sysflts.iterator(); it.hasNext();) {
                Filter filter = it.next();
                ret.add(ff.and(filter, usrflts.get(0)));
            }
        } else {
            ff = null;
            return sysflts;
        }
        ff = null;
        return ret;
    }

    public synchronized String getXmlStringFilters(List<Filter> fils)
            throws ServiceException {
        StringBuffer ret = null;
        //List<Filter> fils = this.getFilters(usr, lays);
        if (fils.size() > 1) {
            ret = new StringBuffer();
            for (Iterator<Filter> it = fils.iterator(); it.hasNext();) {
                Filter filter = it.next();
                ret.append("(");
                String tmp = this.filterToXmlString(filter);
                if (tmp == null || tmp.equals("")) {
                    ret.append(" ");
                } else {
                    ret.append(tmp);
                }
                ret.append(")");
            }
        } else if (fils.size() == 1) {
            ret = new StringBuffer();
            String tmp = this.filterToXmlString(fils.get(0));
            if (tmp == null || tmp.equals("")) {
                ret.append(" ");
            } else {
                ret.append(tmp);
            }
        } else {
            throw new ServiceException("getXmlStringFilters parsing error");
        }
        return ret.toString();
    }

    public synchronized String filterToXmlString(Filter filter) throws ServiceException {
        try {
            if (filter instanceof IncludeFilter) {
                return null;
            }
            org.geotools.xml.Configuration configuration = new org.geotools.filter.v1_1.OGCConfiguration();
            org.geotools.xml.Encoder encoder = new org.geotools.xml.Encoder(configuration);
            encoder.setIndenting(true);
            encoder.setIndentSize(2);


            return XmlUtils.xmlToString(encoder.encodeAsDOM(filter, org.geotools.filter.v1_1.OGC.Filter));
            //return encoder.encode(filter, org.geotools.filter.v1_1.OGC.Filter, System.out);

        } catch (SAXException ex) {
            Logger.getLogger(FilterAuth.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServiceException(ex.getMessage());
        } catch (TransformerException ex) {
            Logger.getLogger(FilterAuth.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServiceException(ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(FilterAuth.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServiceException(ex.getMessage());
        }
    }
}
