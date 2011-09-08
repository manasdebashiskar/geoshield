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

import ch.supsi.ist.geoshield.data.DataManager;
import ch.supsi.ist.geoshield.data.Groups;
import ch.supsi.ist.geoshield.data.Layers;
import ch.supsi.ist.geoshield.data.LayersPermissions;
import ch.supsi.ist.geoshield.data.Offerings;
import ch.supsi.ist.geoshield.data.Users;
import ch.supsi.ist.geoshield.exception.ServiceException;
import ch.supsi.ist.geoshield.sos.SOSUtils;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author milan
 */
public class TestDataManager {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        DataManager dm = new DataManager();
        Users usr = dm.getUser(1);
        System.out.println("User: " + usr.getNameUsr());
        List<Groups> grps = usr.getGroups();
        //System.out.println("Groups: " + grps.size());

        for (Iterator<Groups> it = grps.iterator(); it.hasNext();) {
            Groups g = it.next();
            System.out.println(" - " + g.getNameGrp());
            List<LayersPermissions> lp = g.getLayersPermissionsCollection();
            for (Iterator<LayersPermissions> pIt = lp.iterator(); pIt.hasNext();) {
                LayersPermissions lap = pIt.next();
                //System.out.println("    - " + lap.getIdLpr().toString());
                Layers lay = lap.getIdLayFk();
                System.out.println("     - " + lay.getNameLay());
            }
        }

        try {
            List<Offerings> offs = SOSUtils.getOfferings(usr);
            System.out.println("\nOFFERINGS:");
            for (Iterator<Offerings> it = offs.iterator(); it.hasNext();) {
                Offerings offerings = it.next();
                System.out.println(" > " + offerings.getNameOff());
            }
        } catch (ServiceException ex) {
            Logger.getLogger(TestDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }


        dm.close();
    }
}
