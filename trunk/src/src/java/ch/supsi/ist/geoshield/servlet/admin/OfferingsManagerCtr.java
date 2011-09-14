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
import ch.supsi.ist.geoshield.data.Offerings;
import ch.supsi.ist.geoshield.data.OfferingsPermissions;
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
 * @author Milan Antonovic - Istituto Scienze della Terra, SUPSI
 */

public class OfferingsManagerCtr extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        //DataManager dm = new DataManager();
        
        DataManager dm = (DataManager)request.getAttribute(
                CacheFilter.GEOSHIELD_DATAMANAGER);
        try {
            System.out.println("OfferingsManagerCtr: ");
            String req = Utility.getHttpParam("REQUEST", request);
            String filter = Utility.getHttpParam("FILTER", request);
            System.out.println(" > REQUEST: " + req);
            System.out.println(" > FILTER: " + filter);
            String[] filterArr = null;
            if (filter != null) {
                filterArr = filter.split(";");
            }
            if (req.equalsIgnoreCase("offerings")) {
                try {
                    dm.recreate();
                    List<Offerings> ofrs = dm.getOfferings();

                    if (filterArr != null) {
                        for (Iterator<Offerings> it = ofrs.iterator(); it.hasNext();) {
                            Offerings lay = it.next();
                            if (lay.getIdSurFk().getIdSur() != Integer.parseInt(filterArr[1])) {
                                //System.out.println("removing for " + lay.getNameLay());
                                it.remove();
                            }
                        }
                    }

                    JSONSerializer json = new JSONSerializer();
                    //System.out.println("JSON:\n" + json.exclude("*.class").prettyPrint("servicesUrls", srvUrls));
                    out.println(json.exclude("*.class").serialize("offerings", ofrs));

                } catch (Exception e) {
                    out.println(e.toString());
                } finally {
                    dm.close();
                }
            } else if(req.equalsIgnoreCase("checkOfferingsPermissions")){
                String idGrp = Utility.getHttpParam("idGrp", request);
                String idOff = Utility.getHttpParam("idOff", request);
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
                    OfferingsPermissions opr = null;
                    try {
                        try {
                            opr = dm.getOfferingsPermissionsByIdGrpIdLay(
                                    Integer.parseInt(idGrp), Integer.parseInt(idOff));
                            if (!Boolean.parseBoolean(checked)) {
                                dm.remove(opr);
                                out.print("{success: true, message: 'Permission removed.'}");
                            } else {
                                out.print("{success: false, error: 'Database mismatch, please reload data!'}");
                            }
                        } catch (NumberFormatException numberFormatException) {
                            // do somothing...
                            
                        } catch (NoResultException noResultException) {
                            //System.out.println("LayersPermissions NOT exist.");
                            if (Boolean.parseBoolean(checked)) {
                                opr = new OfferingsPermissions();
                                opr.setIdGrpFk(dm.getGroup(Integer.parseInt(idGrp)));
                                opr.setIdOffFk(dm.getOfferings(Integer.parseInt(idOff)));
                                dm.persist(opr);
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
            }else{
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
