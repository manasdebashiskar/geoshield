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
import ch.supsi.ist.geoshield.data.Layers;
import ch.supsi.ist.geoshield.data.LayersPermissions;
import ch.supsi.ist.geoshield.data.ServicesUrls;
import ch.supsi.ist.geoshield.exception.ServiceException;
import ch.supsi.ist.geoshield.shields.CacheFilter;
import ch.supsi.ist.geoshield.utils.Utility;
import flexjson.JSONSerializer;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import javax.persistence.NoResultException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */
public class LayersManagerCtr extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        //DataManager dm = new DataManager();
        DataManager dm = (DataManager)request.getAttribute(
                CacheFilter.GEOSHIELD_DATAMANAGER);
        //System.out.println("***************************************************");
        //System.out.println("LayersManagerCtr:");
        try {
            String req = Utility.getHttpParam("REQUEST", request);
            //System.out.println("Request: " + req);
            String filter = Utility.getHttpParam("FILTER", request);
            //System.out.println("Filter: " + filter);
            String[] filterArr = null;
            if (filter != null) {
                filterArr = filter.split(";");
            }
            if (req.equalsIgnoreCase("layers")) {
                try {
                    dm.recreate();
                    List<Layers> lays = dm.getLayers();
                    if (filterArr != null) {
                        for (Iterator<Layers> it = lays.iterator(); it.hasNext();) {
                            Layers lay = it.next();
                            if (lay.getIdSurFk().getIdSur() != Integer.parseInt(filterArr[1])) {
                                //System.out.println("removing for " + lay.getNameLay());
                                it.remove();
                            }
                        }
                    }
                    JSONSerializer json = new JSONSerializer();
                    //System.out.println("JSON:\n" + json.exclude("*.class").prettyPrint("layers", lays));
                    out.println(json.exclude("*.class").serialize("layers", lays));
                } catch (Exception e) {
                    out.println(e.toString());
                }
            } else if (req.equalsIgnoreCase("insertLayer")) {
                // check if idSpr exist error!!
                String idLay = Utility.getHttpParam("idLay", request);

                String idSurFk = Utility.getHttpParam("idSur", request);
                String nameLay = Utility.getHttpParam("nameLay", request);
                String geomLay = Utility.getHttpParam("geomLay", request);
                String nsLay = Utility.getHttpParam("nsLay", request);
                String nsUrlLay = Utility.getHttpParam("nsUrlLay", request);

                // Check mandatory input params

                Layers lay = null;
                try {
                    ServicesUrls sur = dm.getServicesUrls(Integer.parseInt(idSurFk));
                    lay = null;
                    lay = new Layers();
                    lay.setNameLay(nameLay);
                    lay.setGeomLay(geomLay);
                    lay.setNsLay(nsLay);
                    lay.setNsUrlLay(nsUrlLay);
                    lay.setIdSurFk(sur);
                    try {
                        dm.persist(lay);
                        out.print("{success: true, message: 'Layer \"" + lay.getNameLay() + "\" added.'}");
                    } catch (ServiceException ex) {
                        System.out.println("Service ex:\n" + ex.getMessage());
                        out.print("{success: false, error: 'Database error!'}");
                    }
                } catch (NumberFormatException numberFormatException) {
                    out.print("{success: false, error: 'Parameter idSurFk not number!'}");
                } catch (NoResultException noResultException) {
                    out.print("{success: false, error: 'ServicesUrls not exist!'}");
                } catch (Exception noResultException) {
                    out.print("{success: false, error: 'ServicesUrls not exist!'}");
                }

            } else if (req.equalsIgnoreCase("updateLayer")) {
                String idLay = Utility.getHttpParam("idLay", request);
                String idSurFk = Utility.getHttpParam("idSur", request);
                String nameLay = Utility.getHttpParam("nameLay", request);
                String geomLay = Utility.getHttpParam("geomLay", request);
                String nsLay = Utility.getHttpParam("nsLay", request);
                String nsUrlLay = Utility.getHttpParam("nsUrlLay", request);
                Layers lay = null;
                try {
                    ServicesUrls sur = dm.getServicesUrls(Integer.parseInt(idSurFk));
                    lay = dm.getLayersById(Integer.parseInt(idLay));
                    lay.setNameLay(nameLay);
                    lay.setGeomLay(geomLay);
                    lay.setNsLay(nsLay);
                    lay.setNsUrlLay(nsUrlLay);
                    lay.setIdSurFk(sur);
                    try {
                        dm.persist(lay);
                        out.print("{success: true, message: 'Layer \"" + lay.getNameLay() + "\" updated.'}");
                    } catch (ServiceException ex) {
                        System.out.println("Service ex:\n" + ex.getMessage());
                        out.print("{success: false, error: 'Database error!'}");
                    }
                } catch (NumberFormatException numberFormatException) {
                    out.print("{success: false, error: 'Parameter id not number!'}");
                } catch (NoResultException noResultException) {
                    out.print("{success: false, error: 'ServicesUrls not exist!'}");
                } catch (Exception noResultException) {
                    out.print("{success: false, error: 'ServicesUrls not exist!'}");
                }
            } else if (req.equalsIgnoreCase("deleteLayer")) {
                String idLay = Utility.getHttpParam("idLay", request);
                Layers lay = null;
                try {
                    lay = dm.getLayersById(Integer.parseInt(idLay));
                    //System.out.println("Found Layer to remove: " + lay.getNameLay());
                    try {
                        String nm = lay.getNameLay();
                        dm.remove(lay);
                        out.print("{success: true, message: 'Layer \"" + nm + "\" removed.'}");
                    } catch (ServiceException ex) {
                        out.print("{success: false, error: 'Database error!'}");
                    }
                } catch (NoResultException noResultException) {
                    out.print("{success: false, " +
                            "error: 'Layer with id \"" + idLay + "\" does not exist.'}");
                }
            } else if (req.equalsIgnoreCase("layersPermissions")) {
                try {
                    dm.recreate();
                    List<LayersPermissions> lprs = dm.getLayersPermissions();
                    if (filterArr != null) {
                        for (Iterator<LayersPermissions> it = lprs.iterator(); it.hasNext();) {
                            LayersPermissions lpr = it.next();
                            if (lpr.getIdLpr() != Integer.parseInt(filterArr[1])) {
                                //System.out.println("removing for " + lpr.getIdLpr());
                                it.remove();
                            }
                        }
                    }
                    JSONSerializer json = new JSONSerializer();
                    //System.out.println("JSON:\n" + json.exclude("*.class").prettyPrint("layers", lprs));
                    out.println(json.exclude("*.class").serialize("layers", lprs));
                } catch (Exception e) {
                    out.println(e.toString());
                }
            } else if (req.equalsIgnoreCase("setLayersPermissions")) {
                String idLpr = Utility.getHttpParam("idLpr", request);
                // check if idLpr not exist error!!
                String filterLpr = Utility.getHttpParam("filterLpr", request);
                if (filterLpr == null || filterLpr.equalsIgnoreCase("")) {
                    filterLpr = "INCLUDE";
                }
                try {
                    try {
                        LayersPermissions lpr = dm.getLayersPermissionsById(Integer.parseInt(idLpr));
                        lpr.setFilterLpr(filterLpr);
                        try {
                            dm.persist(lpr);
                            out.print("{success: true, message: 'CQL Filter updated!'}");
                        } catch (ServiceException serviceException) {
                            out.print("{success: false, error: 'Error saving new CQL filter'}");
                        }
                    } catch (NumberFormatException numberFormatException) {
                    } catch (NoResultException noResultException) {
                        out.print("{success: false, error: 'Layers Permissions does not exist!'}");
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                    out.print("{success: false, error: '" + e.getClass().getSimpleName() + "'}");

                }

            } else if (req.equalsIgnoreCase("checkLayersPermissions")) {
                //System.out.println("*******************************************");
                //System.out.println("addRemLayersPermissions:");

                //String idLpr = Utility.getHttpParam("idLpr", request);
                String idGrp = Utility.getHttpParam("idGrp", request);
                String idLay = Utility.getHttpParam("idLay", request);
                // check if idLpr not exist error!!
                String checked = Utility.getHttpParam("checked", request);
                if (checked == null || checked.equalsIgnoreCase("")) {
                    out.print("{success: false, error: 'Parameter checked not set correctly'}");
                } else {
                    //System.out.println("*******************************************");
                    //System.out.println("Params: ");
                    ////System.out.println("idLpr: " + idLpr);
                    //System.out.println("idGrp: " + idGrp);
                    //System.out.println("idLay: " + idLay);
                    //System.out.println("checked: " + checked);
                    LayersPermissions lpr = null;
                    try {
                        try {
                            dm.recreate();
                            lpr = dm.getLayersPermissionsByIdGrpIdLay(
                                    Integer.parseInt(idGrp), Integer.parseInt(idLay));
                            //System.out.println("LayersPermissions exist: " + lpr.getIdLpr());
                            if (!Boolean.parseBoolean(checked)) {
                                dm.remove(lpr);
                                out.print("{success: true, message: 'Permission on removed.'}");
                            } else {
                                out.print("{success: false, error: 'Database mismatch, please reload data!'}");
                            }
                        } catch (NumberFormatException numberFormatException) {
                        } catch (NoResultException noResultException) {
                            //System.out.println("LayersPermissions NOT exist.");
                            if (Boolean.parseBoolean(checked)) {
                                lpr = new LayersPermissions();
                                lpr.setIdGrpFk(dm.getGroup(Integer.parseInt(idGrp)));
                                lpr.setIdLayFk(dm.getLayersById(Integer.parseInt(idLay)));
                                lpr.setFilterLpr("");
                                dm.persist(lpr);
                                out.print("{success: true, message: 'Permission on added.'}");
                            } else {
                                out.print("{success: false, error: 'Database mismatch, please reload data!'}");
                            }
                        }
                    } catch (ServiceException ex) {
                        out.print("{success: false, error: 'Database error!'}");
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                        out.print("{success: false, error: 'Database error!'}");
                    }
                }
            } else {
                out.print("{success: false, error: 'Request parameter unknown!'}");
            }
        } finally {
            dm.close();
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

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
