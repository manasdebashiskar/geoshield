/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.supsi.ist.geoshield.ramplugin;

import ch.supsi.ist.geoshield.auth.AuthorityManager;
import ch.supsi.ist.geoshield.data.DataManager;
import ch.supsi.ist.geoshield.data.Layers;
import ch.supsi.ist.geoshield.data.Requests;
import ch.supsi.ist.geoshield.data.ServicesUrls;
import ch.supsi.ist.geoshield.data.Users;
import ch.supsi.ist.geoshield.exception.ServiceException;
import ch.supsi.ist.geoshield.exception.UserException;
import ch.supsi.ist.geoshield.utils.Utility;
import ch.supsi.ist.geoshield.auth.FilterAuth;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.NoResultException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opengis.filter.Filter;
import org.geotools.filter.text.cql2.CQL;

/**
 * @author Milan Antonovic - Istituto Scienze della Terra, SUPSI
 */
public class GeoshieldResourceAccessManagerCtr extends HttpServlet {

    private DataManager dm = null;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param req servlet request
     * @param res servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();
        try {

            AuthorityManager am = new AuthorityManager();
            dm = Utility.getDmSession(req);

            List<ServicesUrls> surs = dm.getServicesUrls();



            // Intercept GeoServer's question
            String question = req.getParameter("question");

            if (question == null) {
                out.print("question unknown");

            } else if (question.equalsIgnoreCase("USEREXIST")) {

                String user = req.getParameter("user");
                String password = req.getParameter("password");
                try {
                    Users usr = dm.getAuthUser(user + ":" + password);
                    if (!usr.getIsActiveUsr().booleanValue()) {
                        out.print("inactive");
                    } else {
                        out.print("true");
                    }
                } catch (NoResultException e) {
                    out.print("false");
                }

            } else if (question.equalsIgnoreCase("GETFILTER")) {


                String user = req.getParameter("user");
                String password = req.getParameter("password");
                String layer = req.getParameter("layer");
                String request = req.getParameter("request");
                String service = req.getParameter("service");
                String url = req.getParameter("url");

                /*System.out.println(">>> Serlet:");
                System.out.println("user: " + user);
                System.out.println("password: " + password);
                System.out.println("layer: " + layer);
                System.out.println("request: " + request);
                System.out.println("service: " + service);
                System.out.println("url: " + url);
                System.out.println("path: " + req.getServletPath());
                System.out.println("path: " + req.getContextPath());*/

                ServicesUrls sur = dm.getServicesUrlsByPathIdSrv("/map", service);
                /*System.out.println("ServicesUrls: " + sur);
                System.out.println("ServicesUrls: " + sur.getUrlSur());*/
                //Users usr = dm.getAuthUser(user+":"+password);
                Users usr = dm.getAuthUser(user + ":" + password);
                //System.out.println("USER: " + usr.getFirstNameUsr() + " " + usr.getLastNameUsr());

                Requests reqs = dm.getRequestByNameReqNameSrv(request, service);
                //System.out.println("Request: " + reqs.getNameReq());
                if (am.checkUsrAuthOnSrvSurReq(usr, sur, reqs, dm)) {
                    //System.out.println("ACCESS GRANTED");

                    if (request.equalsIgnoreCase("GETCAPABILITIES")) {
                        out.print("INCLUDE");
                    } else {

                        String[] layers = {
                            layer
                        };

                        List<Layers> lays = dm.getLayers("/map", layers, "WMS");

                        //System.out.println(lays);

                        FilterAuth fau = new FilterAuth();
                        Filter f = fau.getFilter(usr, lays.get(0), dm);

                        System.out.println("Filter: " + f.toString());
                        System.out.println("CQL: " + CQL.toCQL(f));


                        if (f.toString().equalsIgnoreCase("Filter.INCLUDE")) {
                            out.print("INCLUDE");
                        } else if (f.toString().equalsIgnoreCase("Filter.EXCLUDE")) {
                            out.print("EXCLUDE");
                        } else if (CQL.toCQL(f).equalsIgnoreCase("mb = ma")) {
                            out.print("EXCLUDE");
                        } else {
                            out.print(CQL.toCQL(f));
                        }
                    }
                } else {
                    //System.out.println("ACCESS DENIED");
                    out.print("EXCLUDE");
                }

            }
            //out.println("BBOX(the_geom,-113.42272,29.92247,-75.67387,49.99073) AND FEMALE > MALE");
            //out.println("BBOX(the_geom,-113.42272,29.92247,-75.67387,49.99073)");

            //System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n\n");

        } catch (UserException ex) {
            System.err.println("UserException Error: " + ex.toString());
            out.print("EXCLUDE");
            //Logger.getLogger(GeoshieldResourceAccessManagerCtr.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            System.err.println("ServiceException Error: " + ex.toString());
            out.print("EXCLUDE");
            //Logger.getLogger(GeoshieldResourceAccessManagerCtr.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            System.err.println("Exception Error: " + ex.toString());
            out.print("EXCLUDE");
            //Logger.getLogger(GeoshieldResourceAccessManagerCtr.class.getName()).log(Level.SEVERE, null, ex);
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
