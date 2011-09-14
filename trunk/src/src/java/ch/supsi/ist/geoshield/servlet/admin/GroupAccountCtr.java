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
import ch.supsi.ist.geoshield.data.GroupsUsers;
import ch.supsi.ist.geoshield.data.Users;
import ch.supsi.ist.geoshield.exception.ServiceException;
import ch.supsi.ist.geoshield.shields.CacheFilter;
import ch.supsi.ist.geoshield.utils.Utility;
import flexjson.JSONSerializer;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */
public class GroupAccountCtr extends HttpServlet {

    @PersistenceContext
    protected synchronized void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            
        DataManager dm = (DataManager)request.getAttribute(
                CacheFilter.GEOSHIELD_DATAMANAGER);
            String req = Utility.getHttpParam("REQUEST", request);
            //System.out.println("GroupAccountCtr: " + req);
            if (req.equalsIgnoreCase("groups")) {
                try {
                    //System.out.println("Searching groups..");
                    List<Groups> grps = dm.getGroups();
                    JSONSerializer json = new JSONSerializer();
                    //System.out.println("JSON:\n" + json.exclude("*.class", "groups").prettyPrint("users", grps));
                    out.println(json.exclude("*.class", "groups").serialize("groups", grps));
                } catch (Exception e) {
                    out.println(e.toString());
                } finally {
                    dm.close();
                }
            } else if (req.equalsIgnoreCase("setUserGroup")) {
                String idGrp = Utility.getHttpParam("idGrp", request);
                String idUsr = Utility.getHttpParam("idUsr", request);
                String isMember = Utility.getHttpParam("isMember", request);

                //System.out.println("*******************************************");
                //System.out.println("Params: ");
                //System.out.println("idGrp: " + idGrp);
                //System.out.println("idUsr: " + idUsr);
                //System.out.println("isMember: " + isMember);
                // Check input params

                Groups grp = null;
                Users usr = null;
                GroupsUsers gus = null;

                try {
                    //System.out.println("\nChecking..");
                    // Check if User exist
                    usr = dm.getUser(Integer.parseInt(idUsr));
                    //System.out.println("User exist: " + usr.getNameUsr());
                    // Check if Group exist
                    grp = dm.getGroup(Integer.parseInt(idGrp));
                    //System.out.println("Group exist: " + grp.getNameGrp());
                    try {
                        // Check if isMember
                        gus = dm.getGroupsUsersByIdGrpIdUsr(grp.getIdGrp(), usr.getIdUsr());
                        //System.out.println("Membership exist: " + gus.getIdGus());
                        if (!Boolean.parseBoolean(isMember)) {
                            dm.remove(gus);
                            out.print("{success: true, message: 'Member \""+usr.getNameUsr()+"\" removed.'}");
                        } else {
                            out.print("{success: false, error: 'Database mismatch, please reload data!'}");
                        }
                    } catch (NoResultException noResultException) {
                        //System.out.println("Membership NOT exist.");
                        if (Boolean.parseBoolean(isMember)) {
                            gus = new GroupsUsers();
                            gus.setIdGrpFk(grp);
                            gus.setIdUsrFk(usr);
                            dm.persist(gus);
                            out.print("{success: true, message: 'Member \""+usr.getNameUsr()+"\" added.'}");
                        } else {
                            out.print("{success: false, error: 'Database mismatch, please reload data!'}");
                        }
                    }
                } catch (ServiceException ex) {
                    out.print("{success: false, error: 'Database error!'}");
                } catch (NoResultException noResultException) {
                    out.print("{success: false, " +
                            "error: 'Group or user with given Id are not found'}");
                } catch (Exception ex) {
                    //System.out.println(ex.getMessage());
                    out.print("{success: false, error: 'Database error!'}");
                }
            } else if (req.equalsIgnoreCase("insertGroup")) {
                String nameGrp = Utility.getHttpParam("nameGrp", request);
                Groups usr = null;
                try {
                    usr = dm.getGroup(nameGrp);
                    out.print("{success: false, " +
                            "error: 'Group with name \"" + nameGrp + "\" already exist.'}");
                } catch (NoResultException noResultException) {
                    usr = new Groups();
                    usr.setNameGrp(nameGrp);
                    try {
                        dm.persist(usr);
                        out.print("{success: true}");
                    } catch (ServiceException ex) {
                        out.print("{success: false, error: 'Database error!'}");
                    }
                }
            } else if (req.equalsIgnoreCase("deleteGroup")) {
                String idGrp = Utility.getHttpParam("idGrp", request);
                Groups grp = null;
                try {
                    grp = dm.getGroup(Integer.parseInt(idGrp));
                    String grpName = grp.getNameGrp();
                    try {
                        dm.remove(grp);
                        out.print("{success: true, message: 'Group "+grpName+" removed successfully'}");
                    } catch (ServiceException serviceException) {
                        out.print("{success: false, error: 'Database error, cannot delete!'}");
                    }
                } catch (NoResultException noResultException) {
                    out.print("{success: false, " +
                            "error: 'Group with id \"" + idGrp + "\" does not exist.'}");
                } catch (NumberFormatException numberFormatException) {
                    out.print("{success: false, error: 'idGrp is not a number!'}");
                }
            } else if (req.equalsIgnoreCase("updateGroup")) {
                String idGrp = Utility.getHttpParam("idGrp", request);
                String nameGrp = Utility.getHttpParam("nameGrp", request);
                Groups grp = null;
                try {
                    grp = dm.getGroup(Integer.parseInt(idGrp));
                    grp.setNameGrp(nameGrp);
                    try {
                        dm.persist(grp);
                        out.print("{success: true}");
                    } catch (ServiceException ex) {
                        out.print("{success: false, error: 'Database error!'}");
                    }
                } catch (NoResultException noResultException) {
                    out.print("{success: false, error: 'Cannot find user with id \"" + idGrp + "\"'}");
                }
            } else {
                out.print("{success: false, error: 'Request parameter unknown!'}");
            }
            dm.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
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

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
