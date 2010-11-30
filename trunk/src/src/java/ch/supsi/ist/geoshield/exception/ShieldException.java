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

package ch.supsi.ist.geoshield.exception;

/**
 * @author Milan Antonovic, Massimiliano Cannata - Istituto Scienze della Terra, SUPSI
 */
public class ShieldException extends Exception {

	protected String code = "";
    protected String locator = null;
    
    /**
     * Creates a new instance of <code>ShieldException</code> without detail message.
     */
    public ShieldException() {
    }


    /**
     * Constructs an instance of <code>ShieldException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ShieldException(String msg) {
        super(msg);
    }

    public ShieldException(String msg, String code) {
    	super(msg);
    	this.code = code;
    }
    /**
     * Passes the message to the parent, or the code if the message is null.
     *
     * @param msg Message
     * @param code Error Code
     * @param locator Error Location
     */
    public ShieldException(String msg, String code, String locator) {
    	super((msg == null) ? code : msg);
        this.code = code;
        this.locator = locator;
    }

    /**
     * @return String the error code, such as 404-Not Found
     */
    public String getCode() {
        return code;
    }

    /**
     * @return String the location of the error, useful for parse errors
     */
    public String getLocator() {
        return locator;
    }
}
