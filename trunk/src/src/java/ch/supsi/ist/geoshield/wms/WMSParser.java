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

package ch.supsi.ist.geoshield.wms;

import ch.supsi.ist.geoshield.exception.ServiceException;
import ch.supsi.ist.geoshield.parser.OGCParser;
import ch.supsi.ist.geoshield.shields.RequestWrapper;
import ch.supsi.ist.geoshield.utils.Utility;
import java.util.Map;

import java.util.HashMap;

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */

public class WMSParser extends OGCParser {

    //private URL server;
    //private WMSSpecification spec;
    //private static String _GCREQUEST = "getCapabilities";
    public static String _PRM_SERVICE = "SERVICE";
    public static String _PRM_REQUEST = "REQUEST";
    public static String _PRM_LAYERS = "LAYERS";
    public static String _PRM_LAYER = "LAYER";

    @Override
    public Object parseGet(RequestWrapper request) throws ServiceException {

        HashMap<String, String[]> kvp = (HashMap<String, String[]>) request.getParameterMap();
        HashMap<String, String> ret = new HashMap<String, String>();

        String usrReq = Utility.getHttpParam("REQUEST", request);
        if (usrReq == null) {
            throw new ServiceException("Parameter name 'REQUEST' is mandatory.");
        }

        for (String s : kvp.keySet()) {
            String[] tmp = kvp.get(s);
            if (tmp.length == 1) {
                ret.put(s.toUpperCase(), tmp[0]);
            } else {
                throw new ServiceException("Parameter name '" + s + "' can not be redundant.");
            }
        }
        
        if (ret.get(_PRM_SERVICE) == null) {
            // @todo set the exception in a throwable object that will be thrown after dataManager is closed
            throw new ServiceException("Parameter name '" + _PRM_SERVICE + "' is mandatory.");
        }
        if (ret.get(_PRM_REQUEST) == null) {
            throw new ServiceException("Parameter name '" + _PRM_REQUEST + "' is mandatory.");
        }

        if (!ret.get(_PRM_REQUEST).equalsIgnoreCase("GETCAPABILITIES")) {
            if (ret.get(_PRM_REQUEST).equalsIgnoreCase("GETLEGENDGRAPHIC")) {
                if (ret.get(_PRM_LAYER) == null) {
                    throw new ServiceException("Parameter name '" + _PRM_LAYER + "' is mandatory.");
                }
            } else {
                if (ret.get(_PRM_LAYERS) == null) {
                    throw new ServiceException("Parameter name '" + _PRM_LAYERS + "' is mandatory.");
                }
            }

        }

        return ret;
    }

    @Override
    public Object parsePost(RequestWrapper request) throws ServiceException {
        Map<String, String[]> postParams = (Map<String, String[]>) request.getParameterMap();
        for (String s : postParams.keySet()) {
            System.out.print(s + ": " + postParams.get(s));
        }
        if (postParams.size() > 0) {
            return this.parseGet(request);
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    @Override
    public Object checkResponse(RequestWrapper request) throws ServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
