/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.supsi.ist.geoshield.sos;

import ch.supsi.ist.geoshield.data.Groups;
import ch.supsi.ist.geoshield.data.Offerings;
import ch.supsi.ist.geoshield.data.OfferingsPermissions;
import ch.supsi.ist.geoshield.data.ServicesUrls;
import ch.supsi.ist.geoshield.data.Users;
import ch.supsi.ist.geoshield.exception.ServiceException;
import ch.supsi.ist.geoshield.parser.OGCParser;
import ch.supsi.ist.geoshield.utils.ProxyUtility;
import ch.supsi.ist.geoshield.utils.Utility;
import ch.supsi.ist.geoshield.utils.xml.XmlUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author milan
 */
public class SOSUtils {

    private static final String NS_OWS = "http://www.opengis.net/ows/1.1";
    private static final String NS_SOS = "http://www.opengis.net/sos/1.0";
    private static final String NS_GML = "http://www.opengis.net/gml";

    public static synchronized List<Offerings> getOfferings(Users user)
            throws ServiceException {
        List<Offerings> ret = new ArrayList<Offerings>();
        if (user == null) {
            throw new ServiceException("User is null");
        }
        List<Groups> groups = user.getGroups();
        for (Iterator<Groups> it = groups.iterator(); it.hasNext();) {
            Groups g = it.next();
            List<OfferingsPermissions> ops = g.getOfferingsPermissions();
            if (ops != null && !ops.isEmpty()) {
                for (Iterator<OfferingsPermissions> opsIter = ops.iterator(); opsIter.hasNext();) {
                    OfferingsPermissions offeringsPermissions = opsIter.next();
                    ret.add(offeringsPermissions.getIdOffFk());
                }
            }
        }
        return ret;
    }

    public static synchronized HashSet<String> getOfferingsSet(Users user)
            throws ServiceException {
        List<Offerings> offs = getOfferings(user);
        HashSet<String> ret = new HashSet<String>();
        for (Iterator<Offerings> it = offs.iterator(); it.hasNext();) {
            Offerings o = it.next();
            System.out.println(" -> " + o.getNameOff());
            ret.add(o.getNameOff().toUpperCase());
        }
        return ret;
    }

    public static synchronized HashMap<String, HashSet<String>> getInfo(Document doc, List<Offerings> offs) {
        HashMap<String, HashSet<String>> ret = new HashMap<String, HashSet<String>>();
        //System.out.println("GETTING INFO:");
        ret.put("procs", new HashSet<String>());
        ret.put("fois", new HashSet<String>());
        ret.put("obsProps", new HashSet<String>());
        ret.put("offs", new HashSet<String>());

        NodeList gNl = doc.getElementsByTagNameNS(NS_SOS, "ObservationOffering");
        for (int i = 0; i < gNl.getLength(); i++) {

            Element observationOffering = (Element) gNl.item(i);
            String gmlId = observationOffering.getAttribute("gml:id");
            
            //System.out.println("gmlId: " + gmlId);

            for (Iterator<Offerings> it = offs.iterator(); it.hasNext();) {
                Offerings off = it.next();

                ret.get("offs").add(off.getNameOff());

                if (gmlId.equalsIgnoreCase(off.getNameOff())) {

                    // Adding also the long name
                    ret.get("offs").add(observationOffering.getElementsByTagNameNS(NS_GML, "name").item(0).getTextContent());

                    NodeList tmp = observationOffering.getElementsByTagNameNS(NS_SOS, "procedure");
                    for (int c = 0; c < tmp.getLength(); c++) {
                        Element procedure = (Element) tmp.item(c);
                        ret.get("procs").add(procedure.getAttribute("xlink:href"));
                        if(procedure.getAttribute("xlink:href").indexOf(":")>0){
                            String [] ps = procedure.getAttribute("xlink:href").split(":");
                            ret.get("procs").add(ps[ps.length-1]);
                        }
                    }
                    
                    tmp = observationOffering.getElementsByTagNameNS(NS_SOS, "featureOfInterest");
                    for (int c = 0; c < tmp.getLength(); c++) {
                        Element featureOfInterest = (Element) tmp.item(c);
                        ret.get("fois").add(featureOfInterest.getAttribute("xlink:href"));
                        if(featureOfInterest.getAttribute("xlink:href").indexOf(":")>0){
                            String [] ps = featureOfInterest.getAttribute("xlink:href").split(":");
                            ret.get("fois").add(ps[ps.length-1]);
                        }
                    }

                    tmp = observationOffering.getElementsByTagNameNS(NS_SOS, "observedProperty");
                    for (int c = 0; c < tmp.getLength(); c++) {
                        Element observedProperty = (Element) tmp.item(c);
                        ret.get("obsProps").add(observedProperty.getAttribute("xlink:href"));
                    }
                }
            }
        }
        /*
        for (Iterator<String> it = ret.get("fois").iterator(); it.hasNext();) {
            String p = it.next();
            System.out.println(" > " + p);
        }
        System.out.println(" >>> " + ret.get("fois").contains("CANDOGLIA_MERGOZZO"));
        */
        return ret;
    }

    public static synchronized String get2sos(String sosUrl, NameValuePair[] nvp, String auth) {
        try {
            //System.out.println(" sosUrl: " + sosUrl);
            GetMethod postMethod = new GetMethod(sosUrl);
            if (auth != null) {
                postMethod.setRequestHeader(new Header("Authorization", "Basic " + auth));
            }
            postMethod.setQueryString(nvp);
            postMethod.setFollowRedirects(false);
            HttpClient httpClient = new HttpClient();
            int status = httpClient.executeMethod(postMethod);
            //System.out.println(" STATUS: " + status);
            return postMethod.getResponseBodyAsString();
        } catch (HttpException ex) {
            Logger.getLogger(SOSUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SOSUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static synchronized Document getGetCapabilitiesDocument(ServicesUrls sur) throws ServiceException {
        NameValuePair[] nvp = {
            new NameValuePair("request", "getCapabilities"),
            new NameValuePair("section", "contents"),
            new NameValuePair("service", "SOS"),
            new NameValuePair("version", "1.0.0")
        };

        // Check if BASIC authentication is needed
        String authentication = null;
        if ((sur.getPswSur() != null && !sur.getPswSur().equalsIgnoreCase(""))
                || (sur.getUsrSur() != null && !sur.getUsrSur().equalsIgnoreCase(""))) {
            byte[] b = org.apache.commons.codec.binary.Base64.encodeBase64((sur.getUsrSur() + ":" + sur.getPswSur()).getBytes());
            authentication = b.toString();
        }

        String body = SOSUtils.get2sos(sur.getUrlSur(), nvp, authentication);
        //System.out.println("Body: " + body);         
        Document doc;
        try {
            doc = XmlUtils.buildDocument(body);
        } catch (Exception ex) {
            throw new ServiceException(ex.getMessage());
        }
        Element root = doc.getDocumentElement();
        if (root == null) {
            throw new ServiceException("Document has no root element");
        }
        return doc;

    }
}
