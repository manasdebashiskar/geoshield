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
import ch.supsi.ist.geoshield.utils.Utility;
import flexjson.DateTransformer;
import flexjson.JSONSerializer;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
public class UserAccountCtr extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @PersistenceContext
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        DataManager dm = new DataManager();
        try {
            String req = Utility.getHttpParam("REQUEST", request);
            if (req.equalsIgnoreCase("users")) {
                try {
                    List<Users> usrs = dm.getUsers();
                    dm.refreshList(usrs);
                    JSONSerializer json = new JSONSerializer();
                    json = json.transform(new DateTransformer("dd/MM/yyyy"), "groupsUsers.expirationGus");

                    /*
                    Map<String, List<String>> map = Utility.getDatesToTrasform("GroupsUsers");
                    for (String key : map.keySet()){
                    List<String> df = map.get(key);
                    System.out.println("Key: " + key);
                    System.out.println(" - " + df.toArray(new String[0]));
                    System.out.println(" - " + df.toArray(new String[0])[0]);

                    json = json.transform(new DateTransformer(key), df.toArray(new String[0]) );
                    }
                     */


                    //System.out.println("JSON:\n" + json.exclude("*.class").prettyPrint("users", usrs));
                    out.println(json.exclude("*.class").serialize("users", usrs));

                } catch (Exception e) {
                    out.println(e.toString());
                } finally {
                    dm.close();
                }
            } else if (req.equalsIgnoreCase("groupUsers")) {
                try {
                    String idGus = Utility.getHttpParam("idUsr", request);
                    List<GroupsUsers> gus = null;
                    if (idGus != null && !idGus.equalsIgnoreCase("")) {
                        gus = dm.getGroupsUsersByIdUsr(Integer.parseInt(idGus));
                    } else {
                        gus = dm.getGroupsUsers();
                    }
                    JSONSerializer json = new JSONSerializer();
                    json = json.transform(new DateTransformer("dd/MM/yyyy"), "expirationGus");
                    out.println(json.exclude("*.class").exclude("idUsrFk").serialize("groupUsers", gus));


                } catch (Exception e) {
                    out.println(e.toString());
                } finally {
                    dm.close();
                }
            } else if (req.equalsIgnoreCase("updateGroupUser")) {
                try {
                    /*
                    expirationGus
                    idGus	13
                    invoiceGus
                     */
                    String idGus = Utility.getHttpParam("idGus", request);
                    String expirationGus = Utility.getHttpParam("expirationGus", request);
                    String invoiceGus = Utility.getHttpParam("invoiceGus", request);
                    GroupsUsers gus = null;
                    try {
                        gus = dm.getGroupUsersById(Integer.parseInt(idGus));
                        if (expirationGus == null || expirationGus.equalsIgnoreCase("")) {
                            java.util.Date d = null;
                            gus.setExpirationGus(d);
                        } else {
                            java.text.DateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy");
                            gus.setExpirationGus(df.parse(expirationGus));
                        }
                        gus.setInvoiceGus(invoiceGus);
                        try {
                            dm.persist(gus);
                            out.print("{success: true}");
                        } catch (ServiceException ex) {
                            out.print("{success: false, error: 'Database error!'}");
                        }
                    } catch (NoResultException noResultException) {
                        out.print("{success: false, error: 'Cannot find user with id \"" + idGus + "\"'}");
                    }
                    /*String idGus = Utility.getHttpParam("idUsr", request);
                    List<GroupsUsers> gus = null;
                    if (idGus != null && !idGus.equalsIgnoreCase("")) {
                    gus = dm.getGroupsUsersByIdUsr(Integer.parseInt(idGus));
                    } else {
                    gus = dm.getGroupsUsers();
                    }
                    JSONSerializer json = new JSONSerializer();
                    json = json.transform(new DateTransformer("dd/MM/yyyy HH:mm:ss"), "expirationGus");
                    out.println(json.exclude("*.class").exclude("idUsrFk").serialize("groupUsers", gus));

                     */
                } catch (Exception e) {
                    out.println(e.toString());
                    out.print("{success: false, error: 'Internal error'}");
                } finally {
                    dm.close();
                }
            } else if (req.equalsIgnoreCase("insertUser")) {
                String nameUsr = Utility.getHttpParam("nameUsr", request);
                String pswUsr = Utility.getHttpParam("pswUsr", request);
                String firstNameUsr = Utility.getHttpParam("firstNameUsr", request);
                String lastNameUsr = Utility.getHttpParam("lastNameUsr", request);
                String emailUsr = Utility.getHttpParam("emailUsr", request);
                String officeUsr = Utility.getHttpParam("officeUsr", request);
                String telUsr = Utility.getHttpParam("telUsr", request);
                String faxUsr = Utility.getHttpParam("faxUsr", request);
                String addressUsr = Utility.getHttpParam("addressUsr", request);
                String isActiveUsr = Utility.getHttpParam("isActiveUsr", request);
                Users usr = null;
                try {
                    usr = dm.getUser(nameUsr);
                    out.print("{success: false, "
                            + "error: 'User with name \"" + nameUsr + "\" already exist.'}");
                } catch (NoResultException noResultException) {
                    usr = new Users();
                    usr.setNameUsr(nameUsr);
                    usr.setPswUsr(pswUsr);
                    usr.setFirstNameUsr(firstNameUsr);
                    usr.setLastNameUsr(lastNameUsr);
                    usr.setEmailUsr(emailUsr);
                    usr.setOfficeUsr(officeUsr);
                    usr.setTelUsr(telUsr);
                    usr.setFaxUsr(faxUsr);
                    usr.setAddressUsr(addressUsr);
                    if (isActiveUsr==null) {
                        usr.setIsActiveUsr(Boolean.FALSE);
                    } else {
                        usr.setIsActiveUsr(Boolean.TRUE);
                    }
                    try {
                        dm.persist(usr);
                        out.print("{success: true}");
                    } catch (ServiceException ex) {
                        out.print("{success: false, error: 'Database error!'}");
                    }
                }
            } else if (req.equalsIgnoreCase("updateUser")) {
                String idUsr = Utility.getHttpParam("idUsr", request);
                String nameUsr = Utility.getHttpParam("nameUsr", request);
                String pswUsr = Utility.getHttpParam("pswUsr", request);
                String firstNameUsr = Utility.getHttpParam("firstNameUsr", request);
                String lastNameUsr = Utility.getHttpParam("lastNameUsr", request);
                String emailUsr = Utility.getHttpParam("emailUsr", request);
                String officeUsr = Utility.getHttpParam("officeUsr", request);
                String telUsr = Utility.getHttpParam("telUsr", request);
                String faxUsr = Utility.getHttpParam("faxUsr", request);
                String addressUsr = Utility.getHttpParam("addressUsr", request);
                String isActiveUsr = Utility.getHttpParam("isActiveUsr", request);
                Users usr = null;
                try {
                    usr = dm.getUser(Integer.parseInt(idUsr));
                    usr.setNameUsr(nameUsr);
                    usr.setPswUsr(pswUsr);
                    usr.setFirstNameUsr(firstNameUsr);
                    usr.setLastNameUsr(lastNameUsr);
                    usr.setEmailUsr(emailUsr);
                    usr.setOfficeUsr(officeUsr);
                    usr.setTelUsr(telUsr);
                    usr.setFaxUsr(faxUsr);
                    usr.setAddressUsr(addressUsr);
                    if (isActiveUsr==null) {
                        usr.setIsActiveUsr(Boolean.FALSE);
                    } else {
                        usr.setIsActiveUsr(Boolean.TRUE);
                    }
                    try {
                        dm.persist(usr);
                        out.print("{success: true}");
                    } catch (ServiceException ex) {
                        out.print("{success: false, error: 'Database error!'}");
                    }
                } catch (NoResultException noResultException) {
                    out.print("{success: false, error: 'Cannot find user with id \"" + idUsr + "\"'}");
                }
            } else if (req.equalsIgnoreCase("deleteUser")) {
                String idUsr = Utility.getHttpParam("idUsr", request);
                Users usr = null;
                try {
                    usr = dm.getUser(Integer.parseInt(idUsr));
                    //System.out.println("Found user to remove: " + usr.getNameUsr());
                    try {
                        dm.remove(usr);
                        out.print("{success: true}");
                    } catch (ServiceException ex) {
                        out.print("{success: false, error: 'Database error!'}");
                    }
                } catch (NoResultException noResultException) {
                    out.print("{success: false, "
                            + "error: 'User with id \"" + idUsr + "\" does not exist.'}");
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
