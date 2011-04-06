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

package ch.supsi.ist.geoshield.servlet.apps;

import ch.supsi.ist.geoshield.auth.AuthorityManager;
import ch.supsi.ist.geoshield.data.Users;
import ch.supsi.ist.geoshield.exception.UserException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import javax.servlet.http.HttpSession;

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */
public class Login extends HttpServlet {

    private static final boolean debug = false;
    private static final int loginAttemp = 4;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Users usr = null;
        try {
            /*
            if (debug) {
            System.out.println(" - Authorization header: " + auth);
            }
             */
            try {
                usr = (Users) req.getSession().getAttribute("user");
            } catch (IllegalStateException e) {
                System.err.println(e.toString());
                usr = null;
            }
            if (usr == null) {
                HttpSession session = null;

                if (req.getSession().isNew()) {
                    System.out.println("Nuova sessione!");
                    session = req.getSession(true);
                } else {
                    System.out.println("Sessione esistente!");
                    session = req.getSession();
                }

                String auth = req.getHeader("Authorization");
                AuthorityManager am = new AuthorityManager();
                //usr = am.WWWAuthenticate(auth);
                usr = am.WWWAuthenticate(req);
                am.close();
                if (usr == null) {
                    if (session.getAttribute("login") == null) {
                        session.setAttribute("login", new Integer(1));
                    }
                    if (debug) {
                        //System.out.println(" - Attempt " + session.getAttribute("login") + "/" + this.loginAttemp);
                    }
                    if (((Integer) session.getAttribute("login")) < this.loginAttemp) {
                        session.setAttribute("login",
                                ((Integer) session.getAttribute("login")) + 1);
                        throw new UserException(
                                "Sorry! invalid user-name or password.",
                                UserException.LOGIN_ATTEMPT_FAILED);
                    } else {
                        session.setAttribute("login", new Integer(1));
                        throw new UserException(
                                "Sorry! invalid user-name or password.",
                                UserException.INVALID_USER_OR_PASSWORD);
                    }
                } else {
                    if (!usr.getIsActiveUsr()) {
                        throw new UserException(
                                "Sorry! invalid user-name or password.",
                                UserException.INVALID_USER_OR_PASSWORD);
                    } else {
                        session.setAttribute("user", usr);
                        session.setAttribute("login", new Integer(1));
                    }
                }
            } else {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                System.out.println(sdf.format(cal.getTime()) + " - " +
                        usr.getFirstNameUsr() + " " + usr.getLastNameUsr() + " logged in.");
                req.getSession().setAttribute("user", usr);
                req.getSession().setAttribute("login", new Integer(1));
                out.print("True");
            }

            if (!usr.getIsActiveUsr()) {
                throw new UserException(
                        "Sorry! invalid user-name or password.",
                        UserException.INVALID_USER_OR_PASSWORD);
            /*
            response.sendRedirect("/istShield/error/userExpired.jsp");
            return;
             * */
            }
        } catch (UserException t) {
            if (debug) {
                System.out.println(" - " + t.getMessage());
            }
            if (((UserException) t).getCode().equals(UserException.INVALID_USER_OR_PASSWORD)) {
                if (debug) {
                    System.out.println(" - Logins attempt expired.");
                }
                response.sendError(response.SC_FORBIDDEN);
                response.getWriter().close();
            } else if (((UserException) t).getCode().equals(UserException.LOGIN_ATTEMPT_FAILED)) {
                if (debug) {
                    System.out.println(" - Try again.");
                }
                response.setHeader("WWW-Authenticate", "BASIC realm=\"Istituto Scienze della Terra\"");
                response.sendError(response.SC_UNAUTHORIZED);
                response.getWriter().close();
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

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
