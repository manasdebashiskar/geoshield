/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.supsi.ist.geoshield.admin;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.data.ResourceInfo;
import org.geotools.data.ows.Layer;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WMS1_1_0;
import org.geotools.data.wms.WebMapServer;
import org.geotools.ows.ServiceException;

/**
 *
 * @author milan
 */
public class WmsUtils {

    public static void getLayerList(String url) {

        WebMapServer wms;
        WMS1_1_0 wms2;
        try {
            wms = new WebMapServer(new URL(url));
            WMSCapabilities capabilities = wms.getCapabilities();

            List<org.geotools.data.ows.Layer> list = capabilities.getLayerList();

            System.out.println("Layers: ");

            for (Iterator<Layer> it = list.iterator(); it.hasNext();) {
                Layer l = it.next();

                String name = l.getName();

                System.out.println(" > " + name);
                System.out.println("   > " + l.getTitle());
                System.out.println("   > " + l.get_abstract());
                

                if (name != null && l.getLayerChildren().isEmpty()) {
                    ResourceInfo ri = wms.getInfo(l);
                    System.out.println("   > " + ri.getSchema());
                }
                System.out.println("");
            }



            if (capabilities.getRequest().getDescribeLayer() != null) {
                System.out.println(" > This server supports DescribeLayer requests!");
            }
            
            if (capabilities.getRequest().getGetCapabilities() != null) {
                System.out.println(" > This server supports GetCapabilities requests!");
            }
            
            if (capabilities.getRequest().getGetFeatureInfo() != null) {
                System.out.println(" > This server supports GetFeatureInfo requests!");
            }
            
            if (capabilities.getRequest().getGetLegendGraphic() != null) {
                System.out.println(" > This server supports GetLegendGraphic requests!");
            }
            
            if (capabilities.getRequest().getGetMap() != null) {
                System.out.println(" > This server supports GetMap requests!");
            }
            
            if (capabilities.getRequest().getGetStyles() != null) {
                System.out.println(" > This server supports GetStyles requests!");
            }
            
            if (capabilities.getRequest().getPutStyles() != null) {
                System.out.println(" > This server supports PutStyles requests!");
            }
            
            wms2 = new WMS1_1_0();
            //wms.
            

        } catch (IOException ex) {
            Logger.getLogger(WmsUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(WmsUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
    }

    // TEST
    public static void main(String[] args) {
        WmsUtils.getLayerList("http://vm-geoserver.local/geoserver/wms");
    }
}
