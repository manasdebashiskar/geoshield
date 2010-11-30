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

package ch.supsi.ist.geoshield.test;

import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.geotools.data.ows.Layer;
import org.geotools.data.wms.*;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.ows.ServiceException;

/**
 *http://localhost:8080/geoserver/wms?
 * bbox=-130,24,-66,50&
 * styles=population&
 * Format=image/png&
 * request=GetMap&
 * layers=topp:states&
 * width=550&
 * height=250&
 * srs=EPSG:4326
 *
 * @author milan
 */
public class test1 {

    public static void main(String[] args) {
        try {
            URL wmsUrl;
            wmsUrl = new URL("http://localhost:8080/geoserver/wms");
            WebMapServer wms = new WebMapServer(wmsUrl);
            GetMapRequest request = wms.createGetMapRequest();
            request.setVersion("1.1.1");
            request.setBBox("-130,24,-66,50");
            request.setFormat("image/png");
            request.addLayer(new Layer("topp:states"));
            request.setDimensions("550", "250");
            request.setSRS("EPSG:4326");
            
            URL url = request.getFinalURL();
            System.out.println(url.getHost());

            /*
            ImageIcon load = new ImageIcon(url);
            image = load.getImage();
            */

        } catch (MalformedURLException ex) {
            Logger.getLogger(test1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(test1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(test1.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
