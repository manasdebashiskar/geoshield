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

import java.util.List;

import ch.supsi.ist.geoshield.data.*;
import ch.supsi.ist.geoshield.exception.ServiceException;
import ch.supsi.ist.geoshield.exception.UserException;
import ch.supsi.ist.geoshield.shields.CacheFilterUtils;
import ch.supsi.ist.geoshield.utils.Utility;
import java.util.Date;
import java.util.Iterator;
import javax.persistence.NoResultException;

/**
 *
 * @author Milan Antonovic, Massimiliano Cannata
 */

public class AuthorityManager {
    
    public void close() {
    }

    /**
     * Check if the given user belong at least to one group with permission on
     * the given layer.
     *
     * @param usr The Users object to check
     * @param sur The Layers object to check
     *
     * @return true if a user has permission otherwise false.
     *
     * @author Milan Antonovic
     */
    public synchronized boolean checkUsrAuthOnApp(Users user, Applications app)
            throws ServiceException, UserException {

        List<GroupsUsers> gusList = user.getGroupsUsers();
        Date now = new Date();
        boolean expired = false;
        String gName = "";
        for (Iterator<GroupsUsers> it = gusList.iterator(); it.hasNext();) {
            GroupsUsers gus = it.next();
            if (gus.getExpirationGus() == null || gus.getExpirationGus().compareTo(now) >= 0) {
                if (this.checkGrpAuthOnApp(gus.getIdGrpFk(), app)) {
                    return true;
                }
            } else {
                expired = true;
                gName = gus.getIdGrpFk().getNameGrp();
                //System.out.println("\n > access to application '"+app.getNameAps()+"' with Group '" + gName + "' expired for user '" + user.getNameUsr() + "'");
            }
        }
        if (expired) {
            throw new UserException(
                    "Sorry! the access to application '"+app.getNameAps()+"' from group '" + gName + "' is expired.",
                    UserException.USER_EXPIRED);
        }
        /*
        List<Groups> group = user.getGroups();
        for (Iterator<Groups> it = group.iterator(); it.hasNext();) {
        Groups groups = it.next();
        if (this.checkGrpAuthOnLayer(groups, layer)) {
        return true;
        }
        }
         */
        return false;

    /*
    List<Groups> group = user.getGroups();
    for (Iterator<Groups> it = group.iterator(); it.hasNext();) {
    Groups groups = it.next();
    if (this.checkGrpAuthOnApp(groups, app)) {
    return true;
    }
    }
    return false;
     */
    }

    public synchronized boolean checkGrpAuthOnApp(Groups group, Applications app)
            throws ServiceException {
        DataManager dm = new DataManager();
        try {
            dm.getGrpApsByGrpApp(group, app);
        } catch (NoResultException e) {
            dm.close();
            return false;
        }
        dm.close();
        return true;
    }

    /**
     * Check if the given params combination is authorized to access the services.
     * If the user belong to more than one group, this return true if one of his
     * groups has the permission to the services.
     *
     * @param usr The Users object to check
     * @param sur The ServicesUrls object to check
     * @param req The Requests object to check
     * 
     * @return true if a user-group has a permission on the requested service,
     * otherwise false.
     *
     * @author Milan Antonovic
     */
    @Deprecated
    public synchronized boolean checkUsrAuthOnSrvSurReq(
            Users usr, ServicesUrls sur, Requests req) throws UserException {
        
        DataManager dm = new DataManager();

        System.out.println("\nUser: " + usr.getNameUsr() + " has ");
        System.out.println("   > " + usr.getGroups().size() + " groups.");

        Date now = new Date();
        List<GroupsUsers> usrGrp = usr.getGroupsUsers();

        boolean expired = false;
        String gName = "";

        // Loop groups
        for (Iterator<GroupsUsers> it = usrGrp.iterator(); it.hasNext();) {
            GroupsUsers gus = it.next();
            // Check if expired
            if (gus.getExpirationGus() == null || gus.getExpirationGus().compareTo(now) >= 0) {

                int idGrp = gus.getIdGrpFk().getIdGrp();
                try {

                    ServicesPermissions sp = dm.getServicePermissionBySurGrp(
                            sur.getIdSur(), idGrp);
                    
                    for (Iterator<SprReq> grIt = sp.getSprReqCollection().iterator(); grIt.hasNext();) {
                        SprReq sre = grIt.next();
                        //System.out.println(" sre: " + sre.getIdReqFk().getNameReq());
                        if (sre.getIdReqFk().equals(req)) {
                            dm.close();
                            return true;
                        }
                    }
                    dm.close();
                } catch (NoResultException e) {
                    continue;
                //System.out.println(e.toString());
                }
            } else {
                expired = true;
                gName = gus.getIdGrpFk().getNameGrp();
            }
        }
        dm.close();
        if (expired) {
            throw new UserException(
                    "Sorry! the access to service '" + sur.getPathSur() + "' from group '" + gName + "' is expired.",
                    UserException.USER_EXPIRED);
        }
        return false;
    }


    public synchronized boolean checkUsrAuthOnSrvSurReq(
            Users usr, ServicesUrls sur, Requests req, DataManager dm) throws UserException {

        Date now = new Date();
        List<GroupsUsers> usrGrp = usr.getGroupsUsers();
        
        boolean expired = false;
        String gName = "";

        // Loop groups
        for (Iterator<GroupsUsers> it = usrGrp.iterator(); it.hasNext();) {
            GroupsUsers gus = it.next();
            // Check if expired
            if (gus.getExpirationGus() == null || gus.getExpirationGus().compareTo(now) >= 0) {

                int idGrp = gus.getIdGrpFk().getIdGrp();
                try {
                    ServicesPermissions sp = dm.getServicePermissionBySurGrp(
                            sur.getIdSur(), idGrp);
                    for (Iterator<SprReq> grIt = sp.getSprReqCollection().iterator(); grIt.hasNext();) {
                        SprReq sre = grIt.next();
                        if (sre.getIdReqFk().equals(req)) {
                            return true;
                        }
                    }
                } catch (NoResultException e) {
                    continue;
                }
            } else {
                expired = true;
                gName = gus.getIdGrpFk().getNameGrp();
            }
        }
        if (expired) {
            throw new UserException(
                    "Sorry! the access to service '" + sur.getPathSur() + "' from group '" + gName + "' is expired.",
                    UserException.USER_EXPIRED);
        }
        return false;
    }


    public Users WWWAuthenticate(javax.servlet.http.HttpServletRequest req /*String authHeader*/) throws IOException {
        //System.out.println("Received : " + authHeader);
        String authHeader = req.getHeader("Authorization");
        //System.out.println("authHeader: " + authHeader);
        if (authHeader == null) {
            return null;  // no auth
        }
        String userpassEncoded = null;
        
        if (authHeader.toUpperCase().startsWith("BASIC ")) {
            userpassEncoded = authHeader.substring(6);
        } else if (authHeader.toUpperCase().startsWith("BASICG ")) {
            userpassEncoded = authHeader.substring(7);
        } else {
            return null;
        }
        byte[] b = org.apache.commons.codec.binary.Base64.decodeBase64(userpassEncoded.getBytes());
        String userpassDecoded = new String(b);

        //DataManager dm = Utility.getDmSession(req);
        //DataManager dm = Utility.getDmSession(req);
        DataManager dm = CacheFilterUtils.getDataManagerCached(req);
        //System.out.println("Usint datamanger from session obj..");
        try {
            //System.out.println("userpassDecoded: " + userpassDecoded);
            Users user = dm.getAuthUser(userpassDecoded);
            //dm.close();
            return user;
        } catch (NoResultException e) {
            //dm.close();
            return null;
        }
    }


    /**
     * Check if the given user belong at least to one group with permission on
     * the all the given layers.
     *
     * @param usr The Users object to check
     * @param sur The Layers List object to check
     *
     * @return true if a user has permission otherwise false.
     *
     * @author Milan Antonovic
     */
    public synchronized boolean checkUsrAuthOnLayers(Users user, List<Layers> layers)
            throws ServiceException, UserException {
        boolean ret = true;
        for (Iterator<Layers> it = layers.iterator(); it.hasNext();) {
            Layers layer = it.next();
            //System.out.print("Checking " + layer.getNameLay() + ": ");
            boolean tmp = this.checkUsrAuthOnLayer(user, layer);
            //System.out.println(tmp);
            ret = ret && tmp;
        }
        //System.out.println("\nFinal: " + ret);
        return ret;
    }


    /**
     * Check if the given user belong at least to one group with permission on
     * the given layer.
     *
     * @param usr The Users object to check
     * @param sur The Layers object to check
     *
     * @return true if a user has permission otherwise false.
     *
     * @author Milan Antonovic
     */
    public synchronized boolean checkUsrVisibilityOnLayer(Users user, Layers layer)
            throws ServiceException, UserException {
        List<GroupsUsers> gusList = user.getGroupsUsers();
        Date now = new Date();
        boolean expired = false;
        String gName = "";
        for (Iterator<GroupsUsers> it = gusList.iterator(); it.hasNext();) {
            GroupsUsers gus = it.next();
            if (gus.getExpirationGus() == null || gus.getExpirationGus().compareTo(now) >= 0) {
                if (this.checkGrpVisOnLayer(gus.getIdGrpFk(), layer)) {
                    return true;
                }
            } else {
                expired = true;
                gName = gName + "|" + gus.getIdGrpFk().getNameGrp() ;
                //System.out.println("\n > Group '" + gName + "' expired for user '" + user.getNameUsr() + "'");
            }
        }
        if (expired) {
            throw new UserException(
                    "Sorry! the access to layers from group '" + gName + "|' is expired.",
                    UserException.USER_EXPIRED);
        }
        return false;
    }

    protected  synchronized boolean checkGrpVisOnLayer(Groups group, Layers layer)
            throws ServiceException {
        List<LayersPermissions> list = group.getLayersPermissionsCollection();
        //System.out.println(" Count: " + list.size());
        for (Iterator<LayersPermissions> it = list.iterator(); it.hasNext();) {
            LayersPermissions layersPermissions = it.next();
            if(layersPermissions.getIdLayFk().equals(layer) && !layersPermissions.getFilterLpr().equalsIgnoreCase("EXCLUDE")){
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the given user belong at least to one group with permission on
     * the given layer.
     *
     * @param usr The Users object to check
     * @param sur The Layers object to check
     *
     * @return true if a user has permission otherwise false.
     *
     * @author Milan Antonovic
     */
    public synchronized boolean checkUsrAuthOnLayer(Users user, Layers layer)
            throws ServiceException, UserException {
        List<GroupsUsers> gusList = user.getGroupsUsers();
        Date now = new Date();
        boolean expired = false;
        String gName = "";
        for (Iterator<GroupsUsers> it = gusList.iterator(); it.hasNext();) {
            GroupsUsers gus = it.next();
            if (gus.getExpirationGus() == null || gus.getExpirationGus().compareTo(now) >= 0) {
                if (this.checkGrpAuthOnLayer(gus.getIdGrpFk(), layer)) {
                    return true;
                }
            } else {
                expired = true;
                gName = gus.getIdGrpFk().getNameGrp();
                //System.out.println("\n > Group '" + gName + "' expired for user '" + user.getNameUsr() + "'");
            }
        }
        if (expired) {
            throw new UserException(
                    "Sorry! the access to layers from group '" + gName + "' is expired.",
                    UserException.USER_EXPIRED);
        }
        return false;
    }

    public synchronized boolean checkGrpAuthOnLayer(Groups group, Layers layer)
            throws ServiceException {
        List<LayersPermissions> list = group.getLayersPermissionsCollection();
        //System.out.println(" Count: " + list.size());
        for (Iterator<LayersPermissions> it = list.iterator(); it.hasNext();) {
            LayersPermissions layersPermissions = it.next();
            if(layersPermissions.getIdLayFk().equals(layer)){
                return true;
            }
        }
        return false;
        /*
        //group.
        DataManager dm = new DataManager();
        try {
            dm.getLayersPermissionsByGrpLay(group, layer);
        } catch (NoResultException e) {
            dm.close();
            return false;
        }
        dm.close();
        return true;
        */
    }
}
