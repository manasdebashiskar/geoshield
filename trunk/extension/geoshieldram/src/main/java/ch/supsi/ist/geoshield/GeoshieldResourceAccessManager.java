/**
 * Copyright (c) 2011 Istituto Scienze della Terra - SUPSI
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
package ch.supsi.ist.geoshield;

import java.util.HashMap;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.ows.Dispatcher;
import org.geoserver.ows.Request;
import org.geoserver.security.CatalogMode;
import org.geoserver.security.DataAccessLimits;
import org.geoserver.security.ResourceAccessManager;
import org.geoserver.security.VectorAccessLimits;
import org.geoserver.security.WorkspaceAccessLimits;
//import org.geotools.factory.CommonFactoryFinder;
import org.springframework.security.Authentication;
import org.opengis.filter.Filter;
import org.geotools.util.logging.Logging;
import org.springframework.security.userdetails.User;
//import org.opengis.filter.FilterFactory2;


import org.apache.commons.httpclient.NameValuePair;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;

/**
 * @author Milan Antonovic - Istituto Scienze della Terra, SUPSI
 */
public class GeoshieldResourceAccessManager implements ResourceAccessManager {
    //private static FilterFactory2 factory = CommonFactoryFinder.getFilterFactory2(null);

    protected static Logger LOGGER = Logging.getLogger(GeoshieldResourceAccessManager.class);

    @Override
    public WorkspaceAccessLimits getAccessLimits(Authentication a, WorkspaceInfo wi) {
        /*
        System.out.println(" >>>>>>>>>>>>>>>>>> getAccessLimits 1");
        System.out.println(" Authentication:");
        System.out.println(" - getCredentials: " + a.getCredentials().toString());
        System.out.println(" - getDetails: " + a.getDetails().toString());
        System.out.println(" - getPrincipal: " + a.getPrincipal().toString());
        System.out.println(" WorkspaceInfo:");
        System.out.println(" - getName: " + wi.getName());
        
        
        org.springframework.security.ui.WebAuthenticationDetails det =
        (org.springframework.security.ui.WebAuthenticationDetails) a.getDetails();
        
        
        org.springframework.security.userdetails.User user =
        (org.springframework.security.userdetails.User) a.getPrincipal();
        System.out.println(" USER:");
        System.out.println(" - " + user.getUsername());
        System.out.println(" - " + user.getPassword());
        
        boolean readable = false;
        boolean writable = false;
        
        GrantedAuthority[] ga = user.getAuthorities();
        System.out.println("  - grantedAuthority:");
        for (int i = 0; i < ga.length; i++) {
        GrantedAuthority grantedAuthority = ga[i];
        System.out.println("     - " + grantedAuthority.getAuthority());
        if (grantedAuthority.getAuthority().equalsIgnoreCase("ROLE_ADMINISTRATOR")) {
        System.out.println("     - R+W True!");
        readable = true;
        writable = true;
        break;
        }
        }
        
        readable = true;
        writable = true;
        //System.out.println(" <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
         */
        return new WorkspaceAccessLimits(CatalogMode.CHALLENGE, true, true);
        //return null;
    }

    @Override
    public DataAccessLimits getAccessLimits(Authentication a, LayerInfo li) {
        CatalogMode mode = CatalogMode.CHALLENGE;

        //System.out.println("\n\n>>>>>>>>>>>>>>> getAccessLimits <<<<< ");
        //System.out.println(" > Authorization: " + Dispatcher.REQUEST.get().getHttpRequest().getHeader("Authorization"));
        if (a.getPrincipal() instanceof User) {

            User user = (User) a.getPrincipal();

            //System.out.println(" > User logged in as " + user.getUsername());

            if (user.getUsername().equalsIgnoreCase("geoshield")) {

                ResourceInfo info = li.getResource();

                // Getting HttpRequest from Dispatcher
                Request req = Dispatcher.REQUEST.get();
                HttpServletRequest request = req.getHttpRequest();

                // Extracting attibutes from request, injected in the GeoShieldFilter
                String geoshieldAddress = (String) request.getAttribute(GeoShieldFilter.GEOSHIELD_URL);
                String geoshieldAuthorization = (String) request.getAttribute(GeoShieldFilter.GEOSHIELD_AUTH);
                HashMap<String, DataAccessLimits> limits =
                        (HashMap<String, DataAccessLimits>) request.getAttribute(GeoShieldFilter.GEOSHIELD_ACCESS_LIMITS);

                if (geoshieldAuthorization != null && geoshieldAddress != null) {

                    String[] geoshielduser = geoshieldAuthorization.split(":");
                    
                    /*
                    System.out.println("layer: " + info.getPrefixedName());
                    System.out.println("request: " + req.getRequest());
                    System.out.println("service: " + req.getService());
                    System.out.println("url: " + request.getRequestURL().toString());*/

                    String geoserverUrl = request.getRequestURL().toString();
                    String[] pathInfo = geoserverUrl.split("/");
                    String service = pathInfo[pathInfo.length - 1];
                    //System.out.println("service: " + service);
                    if (service.equalsIgnoreCase("OWS")) {
                        service = request.getParameter("service");
                        geoserverUrl = geoserverUrl.substring(0, geoserverUrl.length() - 3) + service;
                        //System.out.println("url modified: " + geoserverUrl);
                    }
                                        
                    if (limits.containsKey(info.getPrefixedName())) {
                        System.out.println(" > Loading from cache");
                        return limits.get(info.getPrefixedName());
                    } else {
                        System.out.println(" > Loading from GeoShield");
                        NameValuePair[] nvp = {
                            new NameValuePair("question", "GETFILTER"),
                            new NameValuePair("user", geoshielduser[0]),
                            new NameValuePair("password", geoshielduser[0]),
                            new NameValuePair("layer", info.getPrefixedName()),
                            new NameValuePair("request", req.getRequest()),
                            new NameValuePair("service", req.getService()),
                            new NameValuePair("url", geoserverUrl)
                        };

                        String gsFilter = Connector.post2geoshield(geoshieldAddress, nvp);
                        Filter readFilter;
                        try {
                            if (gsFilter.equalsIgnoreCase("EXCLUDE")) {
                                // If the filter is EXCLUDE geoserver does not 
                                // hide the layer but throws an exception (more investigation needed!)
                                readFilter = CQL.toFilter("fid=-1");
                            } else {
                                readFilter = CQL.toFilter(gsFilter);
                            }
                            DataAccessLimits dal = new VectorAccessLimits(mode, null, readFilter, null, readFilter);
                            limits.put(info.getPrefixedName(), dal);
                            return dal;
                        } catch (CQLException ex) {
                            System.err.println("CQLException Error: " + ex.toString());
                            //Logger.getLogger(GeoshieldResourceAccessManager.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    //return new VectorAccessLimits(mode, null, Filter.INCLUDE, null, Filter.INCLUDE);
                }
            } else if (user.getUsername().equalsIgnoreCase("admin")) {
                return null; //new VectorAccessLimits(mode, null, Filter.INCLUDE, null, Filter.INCLUDE);
            }
        } else {
            System.out.println(" > User NOT logged in");
            System.out.println(" > " + a.getPrincipal());
        }
        return new VectorAccessLimits(mode, null, Filter.EXCLUDE, null, Filter.EXCLUDE);
    }

    @Override
    public DataAccessLimits getAccessLimits(Authentication a, ResourceInfo ri) {
        /*System.out.println(" >>>>>>>>>>>>>>>>>> getAccessLimits 3");
        System.out.println(" ResourceInfo:");
        System.out.println("  - getName: " + ri.getName());
        
        //Catalog cat = ri.getCatalog();
        //cat.
        CatalogMode mode = CatalogMode.CHALLENGE;
        Filter readFilter = Filter.EXCLUDE;
        System.out.println(" <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
         */
        return new DataAccessLimits(CatalogMode.CHALLENGE, null);
        //return null;
    }
}
