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
package ch.supsi.ist.geoshield.servlet.admin;

import ch.supsi.ist.geoshield.data.DataManager;
import ch.supsi.ist.geoshield.data.Groups;
import ch.supsi.ist.geoshield.data.Requests;
import ch.supsi.ist.geoshield.data.Services;
import ch.supsi.ist.geoshield.data.ServicesPermissions;
import ch.supsi.ist.geoshield.data.ServicesUrls;
import ch.supsi.ist.geoshield.data.SprReq;
import ch.supsi.ist.geoshield.exception.ServiceException;
import ch.supsi.ist.geoshield.shields.CacheFilter;
import ch.supsi.ist.geoshield.shields.CacheFilterUtils;
import ch.supsi.ist.geoshield.utils.Utility;
import flexjson.JSONSerializer;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */
public class ServicesManagerCtr extends HttpServlet {

    @PersistenceContext
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        //DataManager dm = new DataManager();

        try {
            String req = Utility.getHttpParam("REQUEST", request);
            String filter = Utility.getHttpParam("FILTER", request);
            String[] filterArr = null;
            if (filter != null) {
                filterArr = filter.split(";");
            }
            if (req.equalsIgnoreCase("servicesUrls")) {
                synchronized (this) {
                    System.out.println("\nSTART: servicesUrls" );
                    DataManager dm = new DataManager();
                    try {
                        List<ServicesUrls> srvUrls = dm.getServicesUrls();

                        if (filterArr != null) {
                            for (Iterator<ServicesUrls> it = srvUrls.iterator(); it.hasNext();) {
                                ServicesUrls sur = it.next();
                                if (filterArr[0].equalsIgnoreCase("idGrp")) {
                                    List<ServicesPermissions> lpr = sur.getSprList();
                                    boolean rem = true;
                                    for (Iterator<ServicesPermissions> itSpr = lpr.iterator(); itSpr.hasNext();) {
                                        ServicesPermissions spr = itSpr.next();
                                        if (spr.getIdGrpFk().getIdGrp() == Integer.parseInt(filterArr[1])) {
                                            rem = false;
                                            break;
                                        }
                                    }
                                    if (rem) {
                                        it.remove();
                                    }
                                } else {
                                    if (sur.getIdSrvFk().getIdSrv() != Integer.parseInt(filterArr[1])) {
                                        //System.out.println("removing idSrv " + sur.getPathSur());
                                        it.remove();
                                    }
                                }
                            }
                        }

                        JSONSerializer json = new JSONSerializer();
                        //System.out.println("JSON:\n" + json.exclude("*.class").prettyPrint("servicesUrls", srvUrls));
                        out.println(json.exclude("*.class").serialize("servicesUrls", srvUrls));

                    } catch (Exception e) {
                        out.println(e.toString());
                    }
                    dm.closeIt();
                    dm = null;
                    System.out.println("END: servicesUrls\n" );
                }
            } else if (req.equalsIgnoreCase("wmswfsSurls")) {
                synchronized (this) {
                    System.out.println("\nSTART: wmswfsSurls");
                    DataManager dm = new DataManager();
                    try {
                        List<ServicesUrls> srvUrls = dm.getServicesUrls();

                        for (Iterator<ServicesUrls> it = srvUrls.iterator(); it.hasNext();) {
                            ServicesUrls sur = it.next();
                            if (sur.getIdSrvFk().getNameSrv().equalsIgnoreCase("SOS")) {
                                it.remove();
                            }
                        }

                        if (filterArr != null) {
                            for (Iterator<ServicesUrls> it = srvUrls.iterator(); it.hasNext();) {
                                ServicesUrls sur = it.next();
                                if (filterArr[0].equalsIgnoreCase("idGrp")) {
                                    List<ServicesPermissions> lpr = sur.getSprList();
                                    boolean rem = true;
                                    for (Iterator<ServicesPermissions> itSpr = lpr.iterator(); itSpr.hasNext();) {
                                        ServicesPermissions spr = itSpr.next();
                                        if (spr.getIdGrpFk().getIdGrp() == Integer.parseInt(filterArr[1])) {
                                            rem = false;
                                            break;
                                        }
                                    }
                                    if (rem) {
                                        it.remove();
                                    }
                                } else {
                                    if (sur.getIdSrvFk().getIdSrv() != Integer.parseInt(filterArr[1])) {
                                        //System.out.println("removing idSrv " + sur.getPathSur());
                                        it.remove();
                                    }
                                }
                            }
                        }

                        JSONSerializer json = new JSONSerializer();
                        //System.out.println("JSON:\n" + json.exclude("*.class").prettyPrint("servicesUrls", srvUrls));
                        out.println(json.exclude("*.class").serialize("servicesUrls", srvUrls));

                    } catch (Exception e) {
                        out.println(e.toString());
                    }
                    dm.closeIt();
                    dm = null;
                    System.out.println("END: wmswfsSurls\n");
                }
            } else if (req.equalsIgnoreCase("sosSurls")) {
                synchronized (this) {
                    System.out.println("\nSTART: sosSurls");
                    DataManager dm = new DataManager();
                    try {
                        List<ServicesUrls> srvUrls = dm.getServicesUrls();
                        List<ServicesUrls> ret = new ArrayList<ServicesUrls>();
                        for (Iterator<ServicesUrls> it = srvUrls.iterator(); it.hasNext();) {
                            ServicesUrls sur = it.next();
                            if (sur.getIdSrvFk().getNameSrv().equalsIgnoreCase("SOS")) {
                                ret.add(sur);
                            }
                        }
                        JSONSerializer json = new JSONSerializer();
                        //System.out.println("JSON:\n" + json.exclude("*.class").prettyPrint("servicesUrls", srvUrls));
                        out.println(json.exclude("*.class").serialize("servicesUrls", ret));
                    } catch (Exception e) {
                        out.println(e.toString());
                    }
                    dm.closeIt();
                    dm = null;
                    System.out.println("END: sosSurls\n");
                }
            } else if (req.equalsIgnoreCase("services")) {
                synchronized (this) {
                    DataManager dm = new DataManager();
                    try {
                        List<Services> srv = dm.getServices();
                        JSONSerializer json = new JSONSerializer();
                        //System.out.println("JSON:\n" + json.exclude("*.class").prettyPrint("services", srv));
                        out.println(json.exclude("*.class").serialize("services", srv));
                    } catch (Exception e) {
                        out.println(e.toString());
                    }
                    dm.closeIt();
                    dm = null;
                }
            } else if (req.equalsIgnoreCase("servicesPermissions")) {
                synchronized (this) {
                    DataManager dm = new DataManager();
                    try {
                        List<ServicesPermissions> srvPrm = dm.getServicesPermissions();
                        JSONSerializer json = new JSONSerializer();
                        //System.out.println("JSON:\n" + json.exclude("*.class", "idReqFk.idSrvFk", "idSurFk.idSrvFk").prettyPrint("servicesPermissions", srvPrm));
                        out.println(json.exclude("*.class", "idSrvFk").serialize("servicesPermissions", srvPrm));
                    } catch (Exception e) {
                        out.println(e.toString());
                    }
                    dm.closeIt();
                    dm = null;
                }
            } else if (req.equalsIgnoreCase("setServicesPermissions")) {
                synchronized (this) {
                    DataManager dm = new DataManager();
                    String idGrp = Utility.getHttpParam("idGrp", request);
                    String idSur = Utility.getHttpParam("idSur", request);
                    String hasSpr = Utility.getHttpParam("hasSpr", request);

                    System.out.println("*******************************************");
                    System.out.println("Params: ");
                    System.out.println("idGrp: " + idGrp);
                    System.out.println("idSur: " + idSur);
                    System.out.println("hasSpr: " + hasSpr);
                    // Check input params

                    Groups grp = null;
                    ServicesUrls sur = null;
                    ServicesPermissions spr = null;

                    try {
                        //System.out.println("\nChecking..");

                        // Check if Group exist
                        grp = dm.getGroup(Integer.parseInt(idGrp));
                        //System.out.println("Group exist: " + grp.getNameGrp());

                        // Check if Sur exist
                        sur = dm.getServicesUrls(Integer.parseInt(idSur));
                        //System.out.println("Sur exist: " + sur.getPathSur());

                        try {
                            // Check if isMember
                            spr = dm.getServicePermissionBySurGrp(sur.getIdSur(), grp.getIdGrp());
                            System.out.println("Membership exist: " + spr.getIdSpr());
                            if (!Boolean.parseBoolean(hasSpr)) {
                                System.out.println("Removing.");
                                dm.remove(spr);
                                out.print("{success: true, message: 'Services Permissions removed.'}");
                            } else {
                                //System.out.println("Error.");
                                out.print("{success: false, error: 'Database mismatch, please reload data!'}");
                            }
                        } catch (NoResultException noResultException) {
                            System.out.println("Membership NOT exist.");
                            if (Boolean.parseBoolean(hasSpr)) {
                                spr = new ServicesPermissions();
                                spr.setIdGrpFk(grp);
                                spr.setIdSurFk(sur);
                                dm.persist(spr);
                                out.print("{success: true, message: 'Services Permissions "
                                        + "<br>for group \"" + grp.getNameGrp() + "\" added.'}");
                            } else {
                                out.print("{success: false, error: 'Database mismatch, please reload data!'}");
                            }
                        }
                    } catch (ServiceException ex) {
                        out.print("{success: false, error: 'Database error!'}");
                    } catch (NoResultException noResultException) {
                        out.print("{success: false, "
                                + "error: 'Group or user with given Id are not found'}");
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                        out.print("{success: false, error: 'Database error!'}");
                    }
                    dm.closeIt();
                    dm = null;
                }
            } else if (req.equalsIgnoreCase("setSprReq")) {
                synchronized (this) {
                    DataManager dm = new DataManager();
                    String idReq = Utility.getHttpParam("idReq", request);
                    String idSpr = Utility.getHttpParam("idSpr", request);
                    String hasSre = Utility.getHttpParam("hasSre", request);
                    
                    Requests reqs = null;
                    ServicesPermissions spr = null;
                    SprReq sre = null;

                    try {

                        // Check if Requests exist
                        //System.out.println("\nChecking Requests..");
                        reqs = dm.getRequestById(Integer.parseInt(idReq));
                        //System.out.println("Requests exist: " + reqs.getNameReq());

                        // Check if Spr exist
                        //System.out.println("\nChecking ServicesPermissions..");
                        spr = dm.getServicesPermissionsById(Integer.parseInt(idSpr));
                        //System.out.println("Spr exist: " + spr.getIdSpr());


                        try {
                            // Check if isMember
                            sre = dm.getSprReqByIdReqIdSpr(reqs.getIdReq(), spr.getIdSpr());
                            //System.out.println("SprReq exist:(" + hasSre + "):" + sre.getIdSre());
                            if (!Boolean.parseBoolean(hasSre)) {
                                dm.remove(sre);
                                out.print("{success: true, message: 'Services Request \"" + reqs.getNameReq() + "\" removed.'}");
                            } else {
                                out.print("{success: false, error: 'Services request already setted!'}");
                            }
                        } catch (NoResultException noResultException) {
                            //System.out.println("Membership NOT exist. (" + hasSre + ")");
                            if (Boolean.parseBoolean(hasSre)) {
                                sre = new SprReq();
                                sre.setIdReqFk(reqs);
                                sre.setIdSprFk(spr);
                                dm.persist(sre);
                                out.print("{success: true, message: 'Services Request \"" + reqs.getNameReq() + "\" added.'}");
                            } else {
                                out.print("{success: false, error: 'Services request is already deleted!'}");
                            }
                        }
                    } catch (ServiceException ex) {
                        out.print("{success: false, error: 'Database error!'}");
                    } catch (NoResultException noResultException) {
                        out.print("{success: false, "
                                + "error: 'Group or user with given Id are not found'}");
                    } catch (Exception ex) {
                        System.out.println("Error: " + ex.getClass().getCanonicalName() + "\n" + ex.getMessage());
                        out.print("{success: false, error: 'Database error!'}");
                    }
                    dm.closeIt();
                    dm = null;
                }
            } else if (req.equalsIgnoreCase("insertServiceUrl")) {
                synchronized (this) {
                    DataManager dm = new DataManager();
                    // check if idSpr exist error!!
                    String idSur = Utility.getHttpParam("idSur", request);

                    String idSrv = Utility.getHttpParam("idSrv", request);
                    String pathSur = Utility.getHttpParam("pathSur", request);
                    String urlSur = Utility.getHttpParam("urlSur", request);

                    // Check mandatory input params

                    ServicesUrls srvUrls = null;
                    try {
                        srvUrls = dm.getServicesUrlsByPathIdSrv(pathSur, dm.getServicesById(Integer.parseInt(idSrv)).getNameSrv());
                        out.print("{success: false, "
                                + "error: 'Service with path \"" + pathSur + "\" already exist.'}");
                    } catch (ServiceException sex) {
                        Services srv = null;
                        try {
                            srv = dm.getServicesById(Integer.parseInt(idSrv));
                            srvUrls = null;
                            srvUrls = new ServicesUrls();
                            srvUrls.setPathSur(pathSur);
                            srvUrls.setUrlSur(urlSur);
                            srvUrls.setIdSrvFk(srv);
                            try {
                                dm.persist(srvUrls);
                                out.print("{success: true, message: 'Service \"" + srvUrls.getUrlSur() + "\" added.'}");
                            } catch (ServiceException ex) {
                                System.out.println("Service ex:\n" + ex.getMessage());
                                out.print("{success: false, error: 'Database error!'}");
                            }
                        } catch (NumberFormatException numberFormatException) {
                            out.print("{success: false, error: 'Parameter idSrv not number!'}");
                        } catch (NoResultException noResultException) {
                            out.print("{success: false, error: 'Service type idSrv not exist!'}");
                        }
                    }
                    dm.closeIt();
                    dm = null;
                }
            } else if (req.equalsIgnoreCase("updateServicePermission")) {
                synchronized (this) {
                    DataManager dm = new DataManager();
                    String idSur = Utility.getHttpParam("idSur", request);
                    String idSrv = Utility.getHttpParam("idSrv", request);
                    String pathSur = Utility.getHttpParam("pathSur", request);
                    String urlSur = Utility.getHttpParam("urlSur", request);
                    ServicesUrls srvUrls = null;
                    try {
                        srvUrls = dm.getServicesUrls(Integer.parseInt(idSur));
                        try {
                            srvUrls.setIdSrvFk(dm.getServicesById(Integer.parseInt(idSrv)));
                            srvUrls.setPathSur(pathSur);
                            srvUrls.setUrlSur(urlSur);
                            dm.persist(srvUrls);
                            out.print("{success: true, message: 'Service \"" + srvUrls.getUrlSur() + "\" updated.'}");
                        } catch (ServiceException ex) {
                            out.print("{success: false, error: 'Database error!'}");
                        }
                    } catch (NoResultException noResultException) {
                        out.print("{success: false, "
                                + "error: 'User with id \"" + idSur + "\" does not exist.'}");
                    }
                    dm.closeIt();
                    dm = null;
                }
            } else if (req.equalsIgnoreCase("deleteService")) {
                synchronized (this) {
                    DataManager dm = new DataManager();
                    String idSur = Utility.getHttpParam("idSur", request);
                    ServicesUrls srvUrls = null;
                    try {
                        srvUrls = dm.getServicesUrls(Integer.parseInt(idSur));
                        try {
                            String url = srvUrls.getUrlSur();
                            dm.remove(srvUrls);
                            out.print("{success: true, message: 'Service \"" + url + "\" removed.'}");
                        } catch (ServiceException ex) {
                            out.print("{success: false, error: 'Database error!'}");
                        }
                    } catch (NoResultException noResultException) {
                        out.print("{success: false, "
                                + "error: 'User with id \"" + idSur + "\" does not exist.'}");
                    }
                    dm.closeIt();
                    dm = null;
                }
            } else if (req.equalsIgnoreCase("requests")) {
                synchronized (this) {
                    DataManager dm = new DataManager();
                    try {
                        List<Requests> reqs = dm.getRequests();
                        if (filterArr != null) {
                            for (Iterator<Requests> it = reqs.iterator(); it.hasNext();) {
                                Requests tmp = it.next();
                                if (tmp.getIdSrvFk().getIdSrv() != Integer.parseInt(filterArr[1])) {
                                    //System.out.println("removing for " + tmp.getIdSrvFk().getNameSrv());
                                    it.remove();
                                }
                            }
                        }
                        JSONSerializer json = new JSONSerializer();
                        out.println(json.include("sreList.idSprFk.idSpr").exclude("idSrvFk", "*.class", "sreList.*").serialize("requests", reqs));
                    } catch (Exception e) {
                        out.println(e.toString());
                    }
                    dm.closeIt();
                    dm = null;
                }
            } else {
                out.print("{success: false, error: 'Request parameter unknown!'}");
            }
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public void destroy() {
        Logger.getLogger(ServicesManagerCtr.class.getName()).log(Level.INFO, "LayersManagerCtr destroyed!");
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
