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
import flexjson.JSON;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;
import javax.persistence.NoResultException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opengis.filter.Filter;
import org.geotools.filter.text.cql2.CQL;
import org.json.JSONObject;

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
            
            //List<ServicesUrls> surs = dm.getServicesUrls();
            
            // Intercept GeoServer's question
            String question = req.getParameter("question");
            
            System.out.println("GRAM QUESTION: " + question);

            if (question == null) {
                out.print("question unknown");

            } else if (question.equalsIgnoreCase("USEREXIST")) {
                
                dm.recreate();
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

            } else if (question.equalsIgnoreCase("SYNCHRONIZE")) {
                String url = req.getParameter("url");
                out.print(GramUtils.getSyncJson(url));
            } else if (question.equalsIgnoreCase("GETFILTERS")) {

                JSONObject json = new JSONObject();

                String user = req.getParameter("user");
                String password = req.getParameter("password");
                String layer = req.getParameter("layer");
                String request = req.getParameter("request");
                String service = req.getParameter("service");
                String url = req.getParameter("url");

                System.out.println(">>> GETFILTERS:");
                System.out.println("user: " + user);
                System.out.println("password: " + password);
                System.out.println("layer: " + layer);
                System.out.println("request: " + request);
                System.out.println("service: " + service);
                System.out.println("url: " + url);

                try {
                    Users usr = dm.getAuthUser(user + ":" + password);
                    if (!usr.getIsActiveUsr().booleanValue()) {
                        json.put("user", "inactive");
                    } else {
                        json.put("user", "true");
                        try {
                            ServicesUrls sur = dm.getServicesUrlsByUrlIdSrv(url, service);
                            Requests reqs = dm.getRequestByNameReqNameSrv(request, service);

                            if (am.checkUsrAuthOnSrvSurReq(usr, sur, reqs, dm)) {
                                json.put("service", "granted");

                                System.out.println("ACCESS GRANTED");

                                if (request.equalsIgnoreCase("GETCAPABILITIES")) {
                                    json.put("request", "granted");
                                } else {

                                    json.put("request", "granted");

                                    String[] layers = layer.split(",");

                                    List<Layers> lays = dm.getLayersByUrlAndNameAndService(url, layers, "WMS");

                                    FilterAuth fau = new FilterAuth();


                                    JSONObject filters = new JSONObject();
                                    for (Iterator<Layers> it = lays.iterator(); it.hasNext();) {
                                        Layers l = it.next();
                                        Filter f = fau.getFilter(usr, l, dm);

                                        System.out.println("Filter: " + f.toString());
                                        System.out.println("CQL: " + CQL.toCQL(f));

                                        String filter = "";

                                        if (f.toString().equalsIgnoreCase("Filter.INCLUDE")) {
                                            filter = "INCLUDE";
                                        } else if (f.toString().equalsIgnoreCase("Filter.EXCLUDE")) {
                                            filter = "EXCLUDE";
                                        } else if (CQL.toCQL(f).equalsIgnoreCase("mb = ma")) {
                                            filter = "EXCLUDE";
                                        } else if (CQL.toCQL(f).equalsIgnoreCase("fid=-1")) {
                                            filter = "EXCLUDE";
                                        } else {
                                            filter = CQL.toCQL(f);
                                        }
                                        filters.put(l.getNameLay(), filter);

                                    }
                                    json.put("filters", filters);
                                }
                            } else {
                                json.put("request", "denied");
                            }
                        } catch (ServiceException ex) {
                            json.put("service", "denied");
                        }
                    }
                } catch (NoResultException e) {
                    json.put("user", "false");
                }

                System.out.println(json.toString(4));
                out.print(json.toString(4));

            } else if (question.equalsIgnoreCase("GETFILTER")) {

                //dm.recreate();

                String user = req.getParameter("user");
                String password = req.getParameter("password");
                String layer = req.getParameter("layer");
                String request = req.getParameter("request");
                String service = req.getParameter("service");
                String url = req.getParameter("url");

                System.out.println(">>> Serlet:");
                System.out.println("user: " + user);
                System.out.println("password: " + password);
                System.out.println("layer: " + layer);
                System.out.println("request: " + request);
                System.out.println("service: " + service);
                System.out.println("url: " + url);
                System.out.println("ServletPath: " + req.getServletPath());
                System.out.println("ContextPath: " + req.getContextPath());


                ServicesUrls sur = dm.getServicesUrlsByUrlIdSrv(url, service);

                System.out.println("user: " + user);
                System.out.println("password: " + password);

                Users usr = dm.getAuthUser(user + ":" + password);

                Requests reqs = dm.getRequestByNameReqNameSrv(request, service);
                System.out.println("Request: " + reqs.getNameReq());

                if (am.checkUsrAuthOnSrvSurReq(usr, sur, reqs, dm)) {
                    System.out.println("ACCESS GRANTED");

                    if (request.equalsIgnoreCase("GETCAPABILITIES")) {
                        out.print("INCLUDE");
                    } else {

                        String[] layers = {
                            layer
                        };

                        List<Layers> lays = dm.getLayersByUrlAndNameAndService(url, layers, "WMS");

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
                        } else if (CQL.toCQL(f).equalsIgnoreCase("fid=-1")) {
                            out.print("EXCLUDE");
                        } else {
                            out.print(CQL.toCQL(f));
                        }
                    }
                } else {
                    System.out.println("ACCESS DENIED");
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
