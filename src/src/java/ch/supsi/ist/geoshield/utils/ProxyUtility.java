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
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.Header;

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */

public class ProxyUtility {

    public static synchronized void setProxyRequestHeaders(
            HttpServletRequest httpServletRequest,
            HttpMethod httpMethodProxyRequest,
            String stringProxyHost) throws ServiceException {
        Enumeration headerNames = httpServletRequest.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String stringHeaderName = (String) headerNames.nextElement();
            if (stringHeaderName.equalsIgnoreCase("Content-Length")) {
                continue;
            }
            if (stringHeaderName.equalsIgnoreCase("Authorization")) {
                continue;
            }
            Enumeration headerValues = httpServletRequest.getHeaders(stringHeaderName);
            while (headerValues.hasMoreElements()) {
                String stringHeaderValue = (String) headerValues.nextElement();
                if (stringHeaderName.equalsIgnoreCase("Host")) {
                    stringHeaderValue = stringProxyHost;
                }
                Header header = new Header(stringHeaderName, stringHeaderValue);
                httpMethodProxyRequest.setRequestHeader(header);
            }
        }
    }

    public static synchronized void setProxyResponseHeaders(
            HttpServletResponse response,
            HttpMethod httpMethodProxyRequest) {
        Header[] headerArrayResponse = httpMethodProxyRequest.getResponseHeaders();
        for (Header header : headerArrayResponse) {
            String val = header.getValue();
            if (header.getName().equalsIgnoreCase("Transfer-Encoding")) {
                continue;
            } else {
                response.setHeader(header.getName(), val);
            }
        }
    }
}
