/*
 * $Id: DomEx.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
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
 * 4. The names "Crimson" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
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
 * originally based on software copyright (c) 1999, Sun Microsystems, Inc., 
 * http://www.sun.com.  For more information on the Apache Software 
 * Foundation, please see <http://www.apache.org/>.
 */

package org.apache.crimson.tree;

import java.util.Locale;

import org.w3c.dom.DOMException;


/**
 * Concrete class for DOM exceptions, associating standard messages
 * with DOM error codes.
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
class DomEx extends DOMException
{
    static String messageString (Locale locale, int code)
    {
	switch (code) {
	  case INDEX_SIZE_ERR:
	    return XmlDocument.catalog.getMessage (locale, "D-000");
	  case DOMSTRING_SIZE_ERR:
	    return XmlDocument.catalog.getMessage (locale, "D-001");
	  case HIERARCHY_REQUEST_ERR:
	    return XmlDocument.catalog.getMessage (locale, "D-002");
	  case WRONG_DOCUMENT_ERR:
	    return XmlDocument.catalog.getMessage (locale, "D-003");
	  case INVALID_CHARACTER_ERR:
	    return XmlDocument.catalog.getMessage (locale, "D-004");
	  case NO_DATA_ALLOWED_ERR:
	    return XmlDocument.catalog.getMessage (locale, "D-005");
	  case NO_MODIFICATION_ALLOWED_ERR:
	    return XmlDocument.catalog.getMessage (locale, "D-006");
	  case NOT_FOUND_ERR:
	    return XmlDocument.catalog.getMessage (locale, "D-007");
	  case NOT_SUPPORTED_ERR:
	    return XmlDocument.catalog.getMessage (locale, "D-008");
	  case INUSE_ATTRIBUTE_ERR:
	    return XmlDocument.catalog.getMessage (locale, "D-009");
	  case INVALID_STATE_ERR:
	    return XmlDocument.catalog.getMessage (locale, "D-010");
	  case SYNTAX_ERR:
	    return XmlDocument.catalog.getMessage (locale, "D-011");
	  case INVALID_MODIFICATION_ERR:
	    return XmlDocument.catalog.getMessage (locale, "D-012");
	  case NAMESPACE_ERR:
	    return XmlDocument.catalog.getMessage (locale, "D-013");
	  case INVALID_ACCESS_ERR:
	    return XmlDocument.catalog.getMessage (locale, "D-014");
	  default:
	    return XmlDocument.catalog.getMessage (locale, "D-900");
	}
    }

    /**
     * Creates a DOM exception which provides a standard message
     * corresponding to the given error code, using the default
     * locale for the message.
     */
    public DomEx (short code)
    {
	super (code, messageString (Locale.getDefault (), code));
    }

    /**
     * Creates a DOM exception which provides a standard message
     * corresponding to the given error code and using the specified
     * locale for the message.
     */
    public DomEx (Locale locale, short code)
    {
	super (code, messageString (locale, code));
    }
}
