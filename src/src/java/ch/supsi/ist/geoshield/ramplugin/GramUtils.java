/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.supsi.ist.geoshield.ramplugin;

import ch.supsi.ist.geoshield.auth.FilterAuth;
import ch.supsi.ist.geoshield.data.DataManager;
import ch.supsi.ist.geoshield.data.GroupsUsers;
import ch.supsi.ist.geoshield.data.Layers;
import ch.supsi.ist.geoshield.data.LayersPermissions;
import ch.supsi.ist.geoshield.data.Requests;
import ch.supsi.ist.geoshield.data.Users;
import ch.supsi.ist.geoshield.exception.ServiceException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.filter.text.cql2.CQL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.filter.Filter;

/**
 *
 * @author milan
 */
public class GramUtils {

    public static String getSyncJson(String gramUrl) throws ServiceException {
        
        
        JSONObject ret = new JSONObject();

        DataManager dm = new DataManager();

        List<Layers> laysList = dm.getLayersByUrl(gramUrl);
        List<Users> usersList = dm.getUsersBySurSrv(gramUrl);
        FilterAuth fau = new FilterAuth();

        try {

            System.out.println("User: " + usersList.size());
            for (Iterator<Users> it = usersList.iterator(); it.hasNext();) {
                
                JSONObject usrJson = new JSONObject();
                
                Users usr = it.next();
                usrJson.put("password", usr.getPswUsr());
                
                // Groups 
                JSONObject groupsJson = new JSONObject();
                for (Iterator<GroupsUsers> it1 = usr.getGroupsUsers().iterator(); it1.hasNext();) {
                    GroupsUsers gus = it1.next();
                    groupsJson.put(gus.getIdGrpFk().getNameGrp(),gus.getExpirationGus());
                }
                usrJson.put("groups", groupsJson);
                
                // WMS Syncronization
                JSONArray requestsJson = new JSONArray();
                List<Requests> reqsList = dm.getRequestsBySurUsrSrv(gramUrl, usr.getNameUsr(), "WMS");
                for (Iterator<Requests> it1 = reqsList.iterator(); it1.hasNext();) {
                    Requests req = it1.next();
                    requestsJson.put(req.getNameReq());
                    //System.out.println("Request: " + req.getNameReq());
                }
                usrJson.put("requests", requestsJson);
                
                JSONObject filterJson = new JSONObject();
                for (Iterator<Layers> layIter = laysList.iterator(); layIter.hasNext();) {
                    Layers lay = layIter.next();
                    Filter filter = fau.getFilter(usr, lay, dm);
                    if (filter == Filter.INCLUDE) {
                        filterJson.put(lay.getNameLay(), "INCLUDE");
                    } else if (filter == Filter.EXCLUDE) {
                        //filterJson.put(lay.getNameLay(), "EXCLUDE");
                        continue;
                    } else {
                        filterJson.put(lay.getNameLay(), CQL.toCQL(filter));
                    }
                }
                usrJson.put("filter", filterJson);
                
                ret.put(usr.getNameUsr(),usrJson);
            }
            System.out.println(ret.toString(4));

        } catch (JSONException ex) {
            Logger.getLogger(GramUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret.toString();
    }

    public static void main(String[] args) {
        GramUtils g = new GramUtils();
        try {
            g.getSyncJson("http://geoservice.ist.supsi.ch/geoserver/wms");
        } catch (ServiceException ex) {
            Logger.getLogger(GramUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
