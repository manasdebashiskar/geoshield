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

package ch.supsi.ist.geoshield.data;

import ch.supsi.ist.geoshield.exception.ServiceException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.filter.Filter;
import org.xml.sax.SAXException;

/**
 *
 * @author Milan Antonovic, Massimiliano Cannata
 */
public class DataManager {

    // CREATE  emf.createEntityManager(); on class initialization
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("GeoshieldPU");
    EntityManager entMan = emf.createEntityManager();

    // APPLICATION ----------------------------------------
    public synchronized Applications getApplication(int id_aps)
            throws NoResultException {
        Applications ret = entMan.find(Applications.class, id_aps);
        entMan.refresh(ret);
        return ret;
    }

    public synchronized Applications getApplicationByVpath(String vpath_aps)
            throws NoResultException {
        Query query = entMan.createNamedQuery("Applications.findByVpathAps");
        query.setParameter("vpathAps", vpath_aps);
        Applications ret = (Applications) query.getSingleResult();
        entMan.refresh(ret);
        return ret;
    }

    public synchronized GrpAps getGrpApsByGrpApp(Groups g, Applications app)
            throws NoResultException {
        Query query = entMan.createNamedQuery("GrpAps.findByGrpAndApp");
        query.setParameter("idAps", app);
        query.setParameter("idGrp", g);
        GrpAps ret = (GrpAps) query.getSingleResult();
        return ret;
    }

    // USERS ----------------------------------------
    public synchronized Users getUser(int id_usr)
            throws NoResultException {
        //System.out.println("getUser(" + id_usr + ")");
        Query query = entMan.createNamedQuery("Users.findByIdUsr");
        query.setParameter("idUsr", id_usr);
        Users ret = (Users) query.getSingleResult();
        entMan.refresh(ret);
        return ret;
        /*
        Users ret = entMan.find(Users.class, id_usr);
        entMan.refresh(ret);
        entMan.flush();
        return ret;
         * */
    }

    public synchronized Users getUser(String name)
            throws NoResultException {
        Query query = entMan.createNamedQuery("Users.findByNameUsr");
        query.setParameter("nameUsr", name);
        Users ret = (Users) query.getSingleResult();
        entMan.refresh(ret);
        return ret;
    }

    public synchronized List<Users> getUsers()
            throws NoResultException {
        Query query = entMan.createNamedQuery("Users.findAll");
        List<Users> ret = query.getResultList();
        return ret;
    }

    public synchronized Users getAuthUser(String AuthHeader)
            throws NoResultException {
        Query query = entMan.createNamedQuery("AuthHeader.findByAuhUsr");
        query.setParameter("auth", AuthHeader);
        AuthHeader ah = (AuthHeader) query.getSingleResult();
        Users ret = getUser(ah.getIdUsr());
        entMan.refresh(ret);
        return ret;
    }

    // GROUPS ----------------------------------------
    public synchronized Groups getGroup(String name)
            throws NoResultException {
        Query query = entMan.createNamedQuery("Groups.findByNameGrp");
        query.setParameter("nameGrp", name);
        Groups ret = (Groups) query.getSingleResult();
        return ret;
    }

    public synchronized Groups getGroup(int idGrp)
            throws NoResultException {
        Groups ret = entMan.find(Groups.class, idGrp);
        if (ret == null) {
            throw new NoResultException();
        }
        return ret;
    }

    public synchronized List<Groups> getGroups()
            throws NoResultException {
        Query query = entMan.createNamedQuery("Groups.findAll");
        List<Groups> ret = query.getResultList();
        return ret;
    }

    public synchronized List<Groups> getGroupsByUser(int idUsr)
            throws NoResultException {
        Query query = entMan.createNamedQuery("Groups.findAll");
        List<Groups> ret = query.getResultList();
        return ret;
    }

    // GROUPSUSERS ----------------------------------------
    public synchronized GroupsUsers getGroupUsersById(int idGus)
            throws NoResultException {
        GroupsUsers ret = entMan.find(GroupsUsers.class, idGus);
        if (ret == null) {
            throw new NoResultException();
        }
        return ret;
    }

    public synchronized GroupsUsers getGroupsUsersByIdGrpIdUsr(int idGrp, int idUsr)
            throws NoResultException {
        Query query = entMan.createNamedQuery("GroupsUsers.findByGrgUsr");
        query.setParameter("idGrp", this.getGroup(idGrp));
        query.setParameter("idUsr", this.getUser(idUsr));
        GroupsUsers ret = (GroupsUsers) query.getSingleResult();
        return ret;
    }

    public synchronized List<GroupsUsers> getGroupsUsers()
            throws NoResultException {
        Query query = entMan.createNamedQuery("GroupsUsers.findAll");
        List<GroupsUsers> ret = query.getResultList();
        return ret;
    }

    public synchronized List<GroupsUsers> getGroupsUsersByIdUsr(int idUsr) throws NoResultException {
        Query query = entMan.createNamedQuery("GroupsUsers.findByIdUsr");
        query.setParameter("idUsr", this.getUser(idUsr));
        List<GroupsUsers> ret = query.getResultList();
        return ret;
    }

    // LAYERS ----------------------------------------
    public synchronized Layers getLayersById(int idLay)
            throws NoResultException {
        return entMan.find(Layers.class, idLay);
    }

    public synchronized List<Layers> getLayers()
            throws NoResultException {
        Query query = entMan.createNamedQuery("Layers.findAll");
        List<Layers> ret = query.getResultList();
        return ret;
    }

    public synchronized List<Layers> getLayers(String path)
            throws ServiceException {
        List<Layers> ret = null;
        Query query = entMan.createNamedQuery("ServicesUrls.findByPathSur");
        query.setParameter("pathSur", path);
        try {
            ServicesUrls pu = (ServicesUrls) query.getSingleResult();
            entMan.refresh(pu);
            ret = pu.getLayersCollection();
        } catch (NoResultException e) {
            throw new ServiceException("Sorry, the requested path '" + path + "' "
                    + "' is not handled by this GSS.", ServiceException.INVALID_PARAMETER);
        }
        return ret;
    }
    /*
    public synchronized List<Layers> getLayers(String path, String[] layers)
            throws ServiceException {
        List<Layers> lays = new LinkedList<Layers>();
        for (int i = 0; i < layers.length; i++) {
            //System.out.println("Looking for: " + path + " - " + layers[i]);
            Query query = entMan.createNamedQuery("Layers.findByPathAndName");
            query.setParameter("pathSur", path);
            query.setParameter("nameLay", layers[i]);
            try {
                Layers lay = (Layers) query.getSingleResult();
                entMan.refresh(lay);
                lays.add(lay);
            } catch (NoResultException e) {
                throw new ServiceException("Sorry, the requested layer '" + layers[i] + "' for path '" + path
                        + "' is not handled by this GSS.", ServiceException.INVALID_PARAMETER);
            }
        }
        return lays;
    }*/

    public synchronized List<Layers> getLayers(String path, String[] layers, String service)
            throws ServiceException {
        List<Layers> lays = new LinkedList<Layers>();
        for (int i = 0; i < layers.length; i++) {
            //System.out.println("Looking for: " + path + " - " + layers[i]);
            Query query = entMan.createNamedQuery("Layers.findByPathAndNameAndService");
            query.setParameter("pathSur", path);
            query.setParameter("nameLay", layers[i]);
            query.setParameter("idSrvFk", this.getServiceByName(service));
            try {
                //Layers lay = (Layers) query.getSingleResult();
                //entMan.refresh(lay);
                //System.out.println("  >> layer found: " + lay.getNameLay());
                lays.add((Layers) query.getSingleResult());
            } catch (NoResultException e) {
                throw new ServiceException("Sorry, the requested layer '" + layers[i] + "' for path '" + path
                        + "' is not handled by this GSS.", ServiceException.INVALID_PARAMETER);
            }
        }
        this.refreshList(lays);
        return lays;
    }

    // SERVICES URLS ----------------------------------------
    public synchronized ServicesUrls getServicesUrls(int idSur)
            throws NoResultException {
        return entMan.find(ServicesUrls.class, idSur);
    }

    public synchronized List<ServicesUrls> getServicesUrls()
            throws NoResultException {
        Query query = entMan.createNamedQuery("ServicesUrls.findAll");
        List<ServicesUrls> ret = query.getResultList();
        return ret;
    }
/*
    public synchronized ServicesUrls getServicesUrlsByPath(String path) throws ServiceException {
        //EntityManager em = emf.createEntityManager();
        Query query = entMan.createNamedQuery("ServicesUrls.findByPathSur");
        query.setParameter("pathSur", path);
        ServicesUrls ret = null;
        try {
            ret = (ServicesUrls) query.getSingleResult();
            entMan.refresh(ret);
        } catch (NoResultException e) {
            throw new ServiceException("Sorry, the requested path '" + path + "' are "
                    + "not handled by this GSS.", ServiceException.INVALID_SERVER_URL);
        }
        return ret;
    }*/

    public synchronized ServicesUrls getServicesUrlsByPathIdSrv(String path, String service) throws ServiceException {
        //EntityManager em = emf.createEntityManager();
        Query query = entMan.createNamedQuery("ServicesUrls.findByPathSurIdSrv");
        query.setParameter("pathSur", path);
        query.setParameter("idSrvFk", this.getServiceByName(service));
        ServicesUrls ret = null;
        try {
            ret = (ServicesUrls) query.getSingleResult();
            entMan.refresh(ret);
        } catch (NoResultException e) {
            throw new ServiceException("Sorry, the requested path '" + path + "' are "
                    + "not handled by this GSS.", ServiceException.INVALID_SERVER_URL);
        }
        return ret;
    }

    // SERVICES ----------------------------------------
    public synchronized Services getServicesById(int idSrv)
            throws NoResultException {
        return entMan.find(Services.class, idSrv);
    }

    public synchronized List<Services> getServices()
            throws NoResultException {
        return entMan.createNamedQuery("Services.findAll").getResultList();
    }

    public synchronized Services getServiceByName(String name) throws ServiceException {
        Query query = entMan.createNamedQuery("Services.findByNameSrv");
        query.setParameter("nameSrv", name);
        Services ret = null;
        try {
            ret = (Services) query.getSingleResult();
        } catch (NoResultException e) {
            throw new ServiceException("Sorry, the requested SERVICE '" + name + "' is "
                    + "not handled here.", ServiceException.INVALID_SERVICE_PARAMETER);
        }
        return ret;
    }

    // SERVICES PERMISSIONS ----------------------------------------
    public synchronized List<ServicesPermissions> getServicesPermissions()
            throws NoResultException {
        return entMan.createNamedQuery("ServicesPermissions.findAll").getResultList();
    }

    public synchronized ServicesPermissions getServicesPermissionsById(int idSpr)
            throws NoResultException {
        return entMan.find(ServicesPermissions.class, idSpr);
    }

    public synchronized ServicesPermissions getServicePermissionBySurGrp(
            int idSurFk, int idGrpFk)
            throws NoResultException {
        //System.out.println("idSurFk: " + idSurFk);
        //System.out.println("idGrpFk: " + idGrpFk);
        //Groups g = entMan.find(Groups.class, idGrpFk);
        Groups g = this.getGroup(idGrpFk);
        /*Query query = entMan.createNamedQuery("Groups.findByIdGrp");
        query.setParameter("idGrp", idGrpFk);
        Groups g = (Groups) query.getSingleResult();*/
        //System.out.println("Found group: " + g.getNameGrp());


        //ServicesUrls s = entMan.find(ServicesUrls.class, idSurFk);
        ServicesUrls s = this.getServicesUrls(idSurFk);
        /*query = null;
        query = entMan.createNamedQuery("ServicesUrls.findByIdSur");
        query.setParameter("idSur", idSurFk);
        ServicesUrls s  = (ServicesUrls) query.getSingleResult();*/
        //System.out.println("Found serviceUrl: " + s.getPathSur());

        //query = null;
        Query query = entMan.createNamedQuery("ServicesPermissions.findBySurGrp");
        query.setParameter("idGrpFk", g);
        query.setParameter("idSurFk", s);
        ServicesPermissions ret = (ServicesPermissions) query.getSingleResult();
        return ret;
    }

    public synchronized ServicesPermissions getServicePermission(
            int idGrpFk, int idReqFk, int idSurFk)
            throws NoResultException {
        Groups g = entMan.find(Groups.class, idGrpFk);
        Requests r = entMan.find(Requests.class, idReqFk);
        ServicesUrls s = entMan.find(ServicesUrls.class, idSurFk);
        /*System.out.println("Group: " + g.getNameGrp());
        System.out.println("Requests: " + r.getNameReq());
        System.out.println("ServicesUrls: " + s.getUrlSur());*/
        Query query = entMan.createNamedQuery("ServicesPermissions.findByGrpReqSur");
        query.setParameter("idGrpFk", g);
        query.setParameter("idReqFk", r);
        query.setParameter("idSurFk", s);
        ServicesPermissions ret = (ServicesPermissions) query.getSingleResult();
        return ret;
    }

    // SprReq  -----------------------------------------
    public synchronized SprReq getSprReqByIdReqIdSpr(int idReq, int idSpr) throws NoResultException {//
        Query query = entMan.createNamedQuery("SprReq.findByIdReqIdSpr");
        query.setParameter("idReq", this.getRequestById(idReq));
        query.setParameter("idSpr", this.getServicesPermissionsById(idSpr));
        return (SprReq) query.getSingleResult();
    }

    // REQUESTS ----------------------------------------
    public synchronized List<Requests> getRequests()
            throws NoResultException {
        return entMan.createNamedQuery("Requests.findAll").getResultList();
    }

    public synchronized Requests getRequestById(int idReq) throws NoResultException {
        return entMan.find(Requests.class, idReq);
    }

    public synchronized Requests getRequestByName(String name) throws ServiceException {
        Query query = entMan.createNamedQuery("Requests.findByNameReq");
        query.setParameter("nameReq", name);
        Requests ret = null;
        try {
            ret = (Requests) query.getSingleResult();
        } catch (NoResultException e) {
            throw new ServiceException("Request " + name + " doesn't exist.");
        }
        return ret;
    }

    public synchronized Requests getRequestByNameReqNameSrv(String reqName, String srvName)
            throws ServiceException {
        Services srv = this.getServiceByName(srvName);
        Query query = entMan.createNamedQuery("Requests.findByNameReqidSrvFk");
        query.setParameter("nameReq", reqName);
        query.setParameter("idSrvFk", srv);
        Requests ret = null;
        try {
            ret = (Requests) query.getSingleResult();
        } catch (NoResultException e) {
            throw new ServiceException("Sorry, the requested SERVICE '" + reqName
                    + "' doesn't handle '" + reqName + "' REQUEST.",
                    ServiceException.NO_SUCH_OPERATION);
        }
        return ret;
    }

    public synchronized Map<String,Requests> getRequestByUsersServiceUrl(Users usr, ServicesUrls sur)
            throws ServiceException {
        Map<String,Requests> ret = new HashMap<String, Requests>();
        for (ListIterator<Groups> isG = usr.getGroups().listIterator(); isG.hasNext();) {
            Groups g = isG.next();
            ServicesPermissions sp = this.getServicePermissionBySurGrp(sur.getIdSur(), g.getIdGrp());
            for (ListIterator<SprReq> itSr =
                    sp.getSprReqCollection().listIterator(); itSr.hasNext();) {
                SprReq sr = itSr.next();
                ret.put(sr.getIdReqFk().getNameReq().toUpperCase(), sr.getIdReqFk());
            }
        }
        return ret;
    }

    // LAYERS PERMISSION ----------------------------------------
    public synchronized List<LayersPermissions> getLayersPermissions()
            throws NoResultException {
        return entMan.createNamedQuery("LayersPermissions.findAll").getResultList();
    }

    public synchronized LayersPermissions getLayersPermissionsById(int id_prm)
            throws NoResultException {
        return entMan.find(LayersPermissions.class, id_prm);
    }

    public synchronized LayersPermissions getLayersPermissionsByIdGrpIdLay(int idGrp, int idLay)
            throws NoResultException {
        Layers l = entMan.find(Layers.class, idLay);
        Groups g = entMan.find(Groups.class, idGrp);
        return getLayersPermissionsByGrpLay(g, l);
    }

    public synchronized LayersPermissions getLayersPermissionsByGrpLay(Groups g, Layers l)
            throws NoResultException {
        Query query = entMan.createNamedQuery("LayersPermissions.findByLayAndGrp");
        query.setParameter("idLay", l);
        query.setParameter("idGrp", g);
        LayersPermissions ret = (LayersPermissions) query.getSingleResult();
        return ret;
    }

    // FILTERS ----------------------------------------
    public synchronized List<Filter> getUserFilters(HttpServletRequest req) throws ServiceException {
        String filterStr = null;
        Enumeration<String> pm = req.getParameterNames();
        boolean cql = false;
        while (pm.hasMoreElements()) {
            String param = pm.nextElement();
            if (param.equalsIgnoreCase("FILTER")) {
                filterStr = req.getParameter(param);
                break;
            }
            if (param.equalsIgnoreCase("CQL_FILTER")) {
                cql = true;
                filterStr = req.getParameter(param);
                break;
            }
        }
        //System.out.println("Filter received: " + filterStr);
        if (filterStr == null || filterStr.equalsIgnoreCase("")) {
            return null;
        }
        if (cql) {
            //System.out.println("User CQL Filter: " + filterStr);
            return decodeCqlFilter(filterStr);
        } else {
            //System.out.println("User OGC Filter: " + filterStr);
            return decodeFilter(filterStr);
        }
    }

    public synchronized List<Filter> decodeCqlFilter(String filter) {
        if (filter == null || filter.equals("")) {
            return null;
        }
        String f = "";
        List<Filter> ret = new LinkedList<Filter>();
        try {
            f = URLDecoder.decode(filter, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            if (filter.indexOf(";") != -1) {
                //ret = org.geotools.filter.text.cql2.CQL.toFilterList(filter);

                for (StringTokenizer stringTokenizer = new StringTokenizer(f, ";"); stringTokenizer.hasMoreTokens();) {
                    String token = stringTokenizer.nextToken();
                    if (token.trim().equalsIgnoreCase("EXCLUDE")) {
                        token = "a=b";
                    }
                    ret.add(org.geotools.filter.text.cql2.CQL.toFilter(token));
                }

            } else {
                ret.add(org.geotools.filter.text.cql2.CQL.toFilter(filter));
            }
        } catch (CQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public synchronized List<Filter> decodeFilter(String filter) throws ServiceException {
        if (filter == null || filter.equals("")) {
            return null;
        }
        String f = "";
        List<Filter> ret = new LinkedList<Filter>();
        try {
            try {
                f = URLDecoder.decode(filter, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
            }

            org.geotools.xml.Configuration configuration =
                    new org.geotools.filter.v1_0.OGCConfiguration();
            org.geotools.xml.Parser parser =
                    new org.geotools.xml.Parser(configuration);
            //check if more then one layer
            if (f.startsWith("(")) {
                if (f.indexOf("()") != -1) {
                    // @todo copy exception from geoserver
                    throw new ServiceException();
                }
                // Remove first and last parantesis
                //System.out.println("Filter before: " + f);
                f = f.replaceAll("\\( \\)", "(?)");
                //System.out.println("User FILTER: " + f);
                f = f.substring(1);
                f = f.substring(0, (f.length() - 1));
                //System.out.println("Filter after: " + f);
                for (StringTokenizer stringTokenizer = new StringTokenizer(f, ")("); stringTokenizer.hasMoreTokens();) {
                    String token = stringTokenizer.nextToken();
                    //System.out.println("Token: " + token);
                    if (!token.equals("?")) {
                        java.io.ByteArrayInputStream xml =
                                new java.io.ByteArrayInputStream(token.getBytes("UTF-8"));
                        ret.add((Filter) parser.parse(xml));
                    } else {
                        ret.add(org.geotools.filter.text.cql2.CQL.toFilter("INCLUDE"));
                    }
                }
            } else {
                java.io.ByteArrayInputStream xml =
                        new java.io.ByteArrayInputStream(f.getBytes("UTF-8"));
                ret.add((Filter) parser.parse(xml));
            }
            /*String info = "Found " + ret.size() + " filter.";
            Logger.getLogger(DataManager.class.getName()).log(
            Level.INFO, info);*/
            // if more then one loop!

            // else decode one

            // else throw exception

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CQLException ex) {
            Logger.getLogger(DataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    // UTILS ------------------------------------
    public synchronized void persist(Object object) throws ServiceException {
        try {
            entMan.getTransaction().begin();
            entMan.persist(object);
            entMan.getTransaction().commit();
        } catch (Exception e) {
            //Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", e);
            if (entMan.getTransaction().isActive()) {
                entMan.getTransaction().rollback();
            }
            throw new ServiceException(e.getMessage());
        }
    }

    public synchronized void refresh(Object object) throws ServiceException {
        try {
            entMan.refresh(object);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    public synchronized void refreshList(List object) throws ServiceException {
        try {
            for (Iterator<Object> it = object.iterator(); it.hasNext();) {
                entMan.refresh(it.next());
            }
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    public synchronized void remove(Object object) throws ServiceException {
        try {
            entMan.getTransaction().begin();
            entMan.remove(object);
            entMan.getTransaction().commit();
            //System.out.println("Committing");
        } catch (Exception e) {
            //System.out.println("Exception");
            //Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", e);
            if (entMan.getTransaction().isActive()) {
                entMan.getTransaction().rollback();
            }
            //System.out.println(e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    public synchronized void removeUser(int idUsr) throws ServiceException {
        try {
            entMan.getTransaction().begin();
            entMan.remove(entMan.find(Users.class, idUsr));
            entMan.getTransaction().commit();
        } catch (Exception e) {
            //Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", e);
            if (entMan.getTransaction().isActive()) {
                entMan.getTransaction().rollback();
            }
            throw new ServiceException(e.getMessage());
        }
    }

    public void close() {

        if (entMan.isOpen()) {
            entMan.close();
        }
        if (emf.isOpen()) {
            emf.close();
        }
    }

    public boolean isOpen() {
        return entMan.isOpen() && emf.isOpen();
    }
}
