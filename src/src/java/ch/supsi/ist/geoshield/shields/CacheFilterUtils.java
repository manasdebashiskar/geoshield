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
package ch.supsi.ist.geoshield.shields;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Milan P. Antonovic - Istituto Scienze della Terra, SUPSI
 */
public class CacheFilterUtils {

    public static boolean resyncNeeded(javax.servlet.http.HttpServletRequest req) {
        //System.out.println("\n\n\\/\\/\\/\\/\\/\\/");
        Long now = new Long(Calendar.getInstance().getTimeInMillis());
        Long refresh = (Long) req.getAttribute(CacheFilter.GEOSHIELD_CACHE_RESYNC_TIMEOUT);
        //System.out.println(" > " + refresh);
        Long lastRefresh = (Long) req.getAttribute(CacheFilter.GEOSHIELD_CACHE_LAST_RESYNC);
        //System.out.println(" > " + lastRefresh);
        lastRefresh = lastRefresh + refresh;
        //System.out.println(" > nex: " + lastRefresh);
        //System.out.println(" > now: " + now);
        //System.out.println(" > com1: " + lastRefresh.compareTo(now));
        //System.out.println(" > com2: " + (lastRefresh-now));
        if (lastRefresh.compareTo(now) < 0) {
            //System.out.println("resync!");
            return true;
        } else {
            return false;
        }
    }

    public static ch.supsi.ist.geoshield.data.DataManager getDataManagerCached(
            javax.servlet.http.HttpServletRequest req) throws IllegalStateException {
        ch.supsi.ist.geoshield.data.DataManager dm =
                (ch.supsi.ist.geoshield.data.DataManager) req.getAttribute(CacheFilter.GEOSHIELD_DATAMANAGER);
        if (dm == null) {
            req.setAttribute(CacheFilter.GEOSHIELD_DATAMANAGER, new ch.supsi.ist.geoshield.data.DataManager());
        } else if (!dm.isOpen()) {
            dm.recreate();
        }/* else if(CacheFilterUtils.resyncNeeded(req)){
            dm.recreate();
        }*/
        return (ch.supsi.ist.geoshield.data.DataManager) req.getAttribute(CacheFilter.GEOSHIELD_DATAMANAGER);
    }

    public static Map<String, Object> getUserCache(String username, HttpServletRequest request) {

        // Extract cache from request
        HashMap<String, Map<String, Object>> cache =
                (HashMap<String, Map<String, Object>>) request.getAttribute(CacheFilter.GEOSHIELD_CACHE);

        return cache.get(username);
        /*
        Map<String, Object> ret = cache.get(username);
        if (ret == null) {
        ret = new HashMap<String, Object>();
        cache.put(username, ret);
        }
        return ret;*/

        /*String authHeader = request.getHeader("Authorization");
        if (authHeader == null) {
        return null;  // no auth
        }
        String userpassEncoded = null;
        
        if (authHeader.toUpperCase().startsWith("BASIC ")) {
        userpassEncoded = authHeader.substring(6);
        } else {
        return null;
        }
        byte[] b = org.apache.commons.codec.binary.Base64.decodeBase64(userpassEncoded.getBytes());
        String[] userDecodedArr = new String(b).split(":");
        
        if (userDecodedArr.length == 2) {
        Map<String, Object> ret = cache.get(userDecodedArr[0]);
        if (ret == null) {
        ret = new HashMap<String, Object>();
        cache.put(userDecodedArr[0], ret);
        }
        return ret;
        } else {
        return null;
        }
         */
    }
}
