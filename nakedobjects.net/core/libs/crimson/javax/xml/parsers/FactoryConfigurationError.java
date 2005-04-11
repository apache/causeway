/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The name "Apache Software Foundation" must not be used to endorse or
 *    promote products derived from this software without prior written
 *    permission. For written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999-2001, Sun Microsystems,
 * Inc., http://www.sun.com.  For more information on the Apache Software
 * Foundation, please see <http://www.apache.org/>.
 */

package javax.xml.parsers;

/**
 * Thrown when a problem with configuration with the Parser Factories
 * exists. This error will typically be thrown when the class of a
 * parser factory specified in the system properties cannot be found
 * or instantiated.
 *
 * @since JAXP 1.0
 * @version 1.0
 */

public class FactoryConfigurationError extends Error {

    private Exception exception;

    /**
     * Create a new <code>FactoryConfigurationError</code> with no
     * detail mesage.
     */

     public FactoryConfigurationError() {
         super();
    	 this.exception = null; 
     }

    /**
     * Create a new <code>FactoryConfigurationError</code> with
     * the <code>String </code> specified as an error message.
     *
     * @param msg The error message for the exception.
     */
    
    public FactoryConfigurationError(String msg) {
        super(msg);
        this.exception = null;
    }


    /**
     * Create a new <code>FactoryConfigurationError</code> with a
     * given <code>Exception</code> base cause of the error.
     *
     * @param e The exception to be encapsulated in a
     * FactoryConfigurationError.
     */
    
    public FactoryConfigurationError(Exception e) {
        super(e.toString());
        this.exception = e;
    }

    /**
     * Create a new <code>FactoryConfigurationError</code> with the
     * given <code>Exception</code> base cause and detail message.
     *
     * @param e The exception to be encapsulated in a
     * FactoryConfigurationError
     * @param msg The detail message.
     * @param e The exception to be wrapped in a FactoryConfigurationError
     */
    
    public FactoryConfigurationError(Exception e, String msg) {
        super(msg);
        this.exception = e;
    }


    /**
     * Return the message (if any) for this error . If there is no
     * message for the exception and there is an encapsulated
     * exception then the message of that exception, if it exists will be 
     * returned. Else the name of the encapsulated exception will be
     * returned.
     *
     * @return The error message.
     */
    
    public String getMessage () {
        String message = super.getMessage ();
  
        if (message == null && exception != null) {
            return exception.getMessage();
        }

        return message;
    }
  
    /**
     * Return the actual exception (if any) that caused this exception to
     * be raised.
     *
     * @return The encapsulated exception, or null if there is none.
     */
    
    public Exception getException () {
        return exception;
    }
}







