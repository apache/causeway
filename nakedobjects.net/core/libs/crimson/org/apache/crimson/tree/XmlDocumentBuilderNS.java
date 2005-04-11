/* 
 * $Id: XmlDocumentBuilderNS.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
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

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.Attributes;

import org.w3c.dom.DOMException;

import org.apache.crimson.parser.AttributesEx;

/**
 * This class implements a Namespace aware DOM tree builder which uses NS
 * versions of the DOM Level 2 create MethodInfos and assumes disableNamespaces
 * is false, ie. JAXP namespaceAware is true.
 */
public class XmlDocumentBuilderNS extends XmlDocumentBuilder
{
    /**
     * Receive notification of the beginning of an element.
     */
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes attributes)
	throws SAXException
    {
	//
	// Convert set of attributes to DOM representation.
	//
        AttributeSet attSet = null;
	int length = attributes.getLength();
	if (length != 0) {
	    try {
                attSet = AttributeSet.createAttributeSet2(attributes);
	    } catch (DOMException ex) {
		throw new SAXParseException(getMessage("XDB-002",
                        new Object[] { ex.getMessage() }), locator, ex);
	    }
	}

	//
	// Then create the element, associate its attributes, and
	// stack it for later addition.
	//
        ElementNode2 e = null;
	try {
            // Translate a SAX empty string to mean no namespaceURI
            if ("".equals(namespaceURI)) {
                namespaceURI = null;
            }
            e = (ElementNode2)document.createElementNS(namespaceURI, qName);
	} catch (DOMException ex) {
	    throw new SAXParseException(getMessage("XDB-004",
                    new Object[] { ex.getMessage() }), locator, ex);
	}
	if (attributes instanceof AttributesEx) {
	    e.setIdAttributeName(
		((AttributesEx)attributes).getIdAttributeName());
        }
	if (length != 0) {
	    e.setAttributes(attSet);
        }

	elementStack[topOfStack++].appendChild(e);
	elementStack[topOfStack] = e;

	//
	// Division of responsibility for namespace processing is (being
	// revised so) that the DOM builder reports errors when namespace
	// constraints are violated, and the parser is ignorant of them.
	//
        // XXX check duplicate attributes here ???
    }

    /**
     * Receive notification of a processing instruction.
     */
    public void processingInstruction(String name, String instruction) 
        throws SAXException
    {
	if (name.indexOf (':') != -1) {
	    throw new SAXParseException((getMessage ("XDB-010")), locator);
        }
        super.processingInstruction(name, instruction);
    }


    //////////////////////////////////////////////////////////////////////
    // org.xml.sax.ext.DeclHandler callbacks
    //////////////////////////////////////////////////////////////////////

    /**
     * Report an internal entity declaration.
     */
    public void internalEntityDecl(String name, String value)
	throws SAXException
    {
        if (name.indexOf (':') != -1) {
            throw new SAXParseException((getMessage("XDB-012")), locator);
        }
        super.internalEntityDecl(name, value);
    }

    /**
     * Report a parsed external entity declaration.
     */
    public void externalEntityDecl(String name, String publicId,
                                   String systemId)
	throws SAXException
    {
        if (name.indexOf (':') != -1) {
            throw new SAXParseException((getMessage("XDB-012")), locator);
        }
        super.externalEntityDecl(name, publicId, systemId);
    }


    //////////////////////////////////////////////////////////////////////
    // org.xml.sax.DTDHandler callbacks
    //////////////////////////////////////////////////////////////////////

    /**
     * Receive notification of a notation declaration event.
     */
    public void notationDecl(String n, String p, String s)
	throws SAXException
    {
        if (n.indexOf(':') != -1) {
            throw new SAXParseException((getMessage("XDB-013")), locator);
        }
        super.notationDecl(n, p, s);
    }

    /**
     * Receive notification of an unparsed entity declaration event.
     */
    public void unparsedEntityDecl(String name, String publicId, 
                                   String systemId, String notation)
	throws SAXException
    {
        if (name.indexOf(':') != -1) {
            throw new SAXParseException((getMessage("XDB-012")), locator);
        }
        super.unparsedEntityDecl(name, publicId, systemId, notation);
    }
}
