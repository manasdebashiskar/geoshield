/**
 * Copyright (c) 2011 Istituto Scienze della Terra - SUPSI
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
package ch.supsi.ist.geoshield;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 *
 * @author Milan Antonovic - Institute of Earth Science SUPSI
 */
public class Connector {

    public static synchronized String post2geoshield(String geoshieldUrl, NameValuePair[] nvp) {
        try {
            //System.out.println("> Connector: " + geoshieldUrl+"/gram");
            // get geoshield address from config files
            PostMethod postMethod = new PostMethod(geoshieldUrl + "/gram");
            //PostMethod postMethod = new PostMethod("http://localhost:8084/geoshield/gram");
            postMethod.setRequestBody(nvp);
            postMethod.setFollowRedirects(false);
            HttpClient httpClient = new HttpClient();
            int status = httpClient.executeMethod(postMethod);
            return postMethod.getResponseBodyAsString();
        } catch (HttpException ex) {
            Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static synchronized boolean checkUser(String geoshieldUrl, String user, String password) throws UserException {
        NameValuePair[] nvp = {
            new NameValuePair("question", "USEREXIST"),
            new NameValuePair("user", user),
            new NameValuePair("password", password)
        };
        String checkUser = Connector.post2geoshield(geoshieldUrl, nvp);
        if (checkUser.equalsIgnoreCase("true")) {
            return true;
        } else {
            throw new UserException(
                    "Sorry! invalid user-name or password.",
                    UserException.LOGIN_ATTEMPT_FAILED);
        }

    }
}
