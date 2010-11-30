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

package ch.supsi.ist.geoshield.utils;

import ch.supsi.ist.geoshield.exception.ServiceException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */

public class Utility {

    static String PACKAGE = "ch.supsi.ist.interceptor.data.";

    public static String getHttpParam(String parameter, javax.servlet.http.HttpServletRequest req) {
        String ret = null;
        java.util.Enumeration<String> pm = req.getParameterNames();
        while (pm.hasMoreElements()) {
            String param = pm.nextElement();
            if (param.equalsIgnoreCase(parameter)) {
                ret = req.getParameter(param);
                break;
            }
        }
        return ret;
    }

    public static void removeHttpParam(String parameter, javax.servlet.http.HttpServletRequest req) {
        String ret = null;
        java.util.Enumeration<String> pm = req.getParameterNames();
        while (pm.hasMoreElements()) {
            String param = pm.nextElement();
            if (param.equalsIgnoreCase(parameter)) {
                req.removeAttribute(param);
                break;
            }
        }
    }

    public static String getGeoShieldUrl(javax.servlet.http.HttpServletRequest req){
        // http://hostname.com:80/mywebapp/servlet/MyServlet/a/b;c=123?d=789
        
        String scheme = req.getScheme(); // http
        String serverName = req.getServerName(); // hostname.com
        int serverPort = req.getServerPort(); // 80
        String contextPath = req.getContextPath(); // /mywebapp
        //String servletPath = req.getServletPath(); // /servlet/MyServlet
        //String pathInfo = req.getPathInfo(); // /a/b;c=123
        //String queryString = req.getQueryString(); // d=789

        // Reconstruct original requesting URL
        //String url = scheme+"://"+serverName+":"+serverPort+contextPath+servletPath;

        return scheme+"://"+serverName+":"+serverPort+contextPath;
    }

    public static String getVpath(javax.servlet.http.HttpServletRequest req) {
        //System.out.println("\ngetVpath: ");
        //System.out.println(req.getRequestURI());
        //System.out.println(req.getRequestURI().replaceFirst("/apps/", ""));
        String[] arr = req.getRequestURI().replaceFirst("/istShield/apps/", "").split("/");
        /*for (int i = 0; i < arr.length; i++) {
        System.out.println(" - "+arr[i]);
        }*/
        String ret = null;
        if (arr.length > 0) {
            ret = arr[0];
        }
        return ret;
    }

    public static String getCompleteVpath(javax.servlet.http.HttpServletRequest req) {
        //System.out.println("\ngetVpath: ");
        //System.out.println(req.getRequestURI());
        //System.out.println(req.getRequestURI().replaceFirst("/apps/", ""));
        String[] arr = req.getRequestURI().split("/");
        /*for (int i = 0; i < arr.length; i++) {
        System.out.println(" "+ i + ". " +arr[i]);
        }*/
        String ret = "/";
        if (arr.length >= 3) {
            ret = "/" + arr[1] + "/" + arr[2] + "/" + arr[3] + "/";
            //System.out.println(" getCompleteVpath ------------ > Returning: " + ret);
        }
        return ret;
    }

    public static StringBuffer getText(HttpURLConnection conn) {
        try {
            System.out.println("Response code = " + conn.getResponseCode());
            String s = "";
            StringBuffer html = new StringBuffer();
            try {
                BufferedReader inp = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                while ((s = inp.readLine()) != null) {
                    s = s.replaceAll("http://istgeo.ist.supsi.ch:80/basemaps/wms/", "ciao");
                    System.out.println(s);
                    html.append(s);
                }
                inp.close();
                return html;
            } catch (Exception e) {
                System.out.println(e.getLocalizedMessage());
            }
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
        return new StringBuffer();
    }

    public static String getBinary(HttpURLConnection conn) {
        try {
            conn.setRequestMethod("POST");
            System.out.println("Response code = " + conn.getResponseCode());
            String s = "";
            String html = "";
            try {
                BufferedReader inp = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                while ((s = inp.readLine()) != null) {
                    html = html + s;
                }
                inp.close();
                return html;
            } catch (Exception e) {
            }
        } catch (IOException e) {
        }
        return "";
    }

    public static synchronized double[] pixToGeo(
            double x, double y, int height, int width, String bbox) {
        double[] ret = new double[2];

        // minX         minY         maxX          maxY
        //713402.476015,87913.814786,725613.387244,103605.40632
        String[] coords = bbox.split(",");
        double minX = Double.parseDouble(coords[0]);
        double minY = Double.parseDouble(coords[1]);
        double maxX = Double.parseDouble(coords[2]);
        double maxY = Double.parseDouble(coords[3]);

        double deltaX = maxX - minX;
        double deltaY = maxY - minY;

        double xRes = width / deltaX;
        double yRes = width / deltaY;

        ret[0] = x * xRes + minX;
        ret[1] = y * yRes + minY;
        //System.out.println("Coord: " + ret[0] + " " + ret[1]);
        return ret;
    }

    public static String arrayToString(String[] a, String separator) {
        StringBuffer result = new StringBuffer();
        if (a.length > 0) {
            result.append(a[0]);
            for (int i = 1; i < a.length; i++) {
                result.append(separator);
                result.append(a[i]);
            }
        }
        return result.toString();
    }

    public static Map<String, List<String>> getDatesToTrasform(String dbClassName)
            throws ServiceException {
        Map<String, List<String>> m = new HashMap<String, List<String>>();
        try {
            //Class c = Class.forName(PACKAGE + dbClassName);
            Class c = Class.forName(PACKAGE + dbClassName);
            java.lang.reflect.Field[] fields = c.getDeclaredFields();
            String format = "";
            for (int i = 0; i < fields.length; i++) {
                java.lang.reflect.Field field = fields[i];
                //System.out.println("Field: " + field.getName());
                javax.persistence.Temporal tt = field.getAnnotation(javax.persistence.Temporal.class);
                if (tt != null) {
                    if (tt.value() == javax.persistence.TemporalType.DATE) {
                        format = "dd/MM/yyyy";
                    } else if (tt.value() == javax.persistence.TemporalType.TIME) {
                        format = "HH:mm:ss";
                    } else if (tt.value() == javax.persistence.TemporalType.TIMESTAMP) {
                        format = "dd/MM/yyyy HH:mm:ss";
                    }
                    if (m.get(format) == null) {
                        m.put(format, new ArrayList<String>(0));
                    }
                    m.get(format).add(field.getName());
                }
            }
        } catch (Exception ex) {
            throw new ServiceException("Error: " + ex.getMessage());
        }
        return m;
    }

    public static long getMillis() {
        Calendar cal = Calendar.getInstance();
        return cal.getTimeInMillis();
    }

    public static ch.supsi.ist.geoshield.data.DataManager getDmSession(javax.servlet.http.HttpServletRequest req) throws IllegalStateException {
        ch.supsi.ist.geoshield.data.DataManager dm = (ch.supsi.ist.geoshield.data.DataManager) req.getSession().getAttribute("datamanager");
        if (dm == null || !dm.isOpen()) {
            req.getSession().setAttribute("datamanager", new ch.supsi.ist.geoshield.data.DataManager());
        }
        return (ch.supsi.ist.geoshield.data.DataManager) req.getSession().getAttribute("datamanager");

    }

    /**
     * This method will return the body content of an HTTP request as a String.
     *
     * @param  request     A valid HTTPServletRequest object.
     * @return             A String containing the body content of the request.
     * @throws IOException Catch-all exception.
     */
    public static String getBodyContent(javax.servlet.http.HttpServletRequest request)
            throws IOException {
        BufferedReader br = request.getReader();
        String nextLine = "";
        StringBuffer bodyContent = new StringBuffer();
        nextLine = br.readLine();
        while (nextLine != null) {
            bodyContent.append(nextLine);
            nextLine = br.readLine();
        }
        System.out.println(bodyContent.toString());
        return bodyContent.toString();

    } // End getBodyContent().
}
