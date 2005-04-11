/* 
 * $Id: XmlDocumentBuilder.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
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


import java.io.IOException;

import java.net.URL;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.EntityReference;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.Attributes;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.ext.DeclHandler;

import org.apache.crimson.parser.AttributesEx;


/**
 * This class is a SAX2 ContentHandler which converts a stream of parse
 * events into an in-memory DOM document.  After each <em>Parser.parse()</em>
 * invocation returns, a resulting DOM Document may be accessed via the
 * <em>getDocument</em> MethodInfo.  The parser and its builder should be used
 * together; the builder may be used with only one parser at a time.
 *
 * <P> This builder optionally does XML namespace processing, reporting
 * conformance problems as recoverable errors using the parser's error
 * handler.  
 *
 * <P> Note: element factories are deprecated because they are non-standard
 * and are provided here only for backwards compatibility.  To customize
 * the document, a powerful technique involves using an element factory
 * specifying what element tags (from a given XML namespace) correspond to
 * what implementation classes.  Parse trees produced by such a builder can
 * have nodes which add behaviors to achieve application-specific
 * functionality, such as modifing the tree as it is parsed.
 *
 * <P> The object model here is that XML elements are polymorphic, with
 * semantic intelligence embedded through customized internal nodes.
 * Those nodes are created as the parse tree is built.  Such trees now
 * build on the W3C Document Object Model (DOM), and other models may be
 * supported by the customized nodes.  This allows both generic tools
 * (understanding generic interfaces such as the DOM core) and specialized
 * tools (supporting specialized behaviors, such as the HTML extensions
 * to the DOM core; or for XSL elements) to share data structures.
 *
 * <P> Normally only "model" semantics are in document data structures,
 * but "view" or "controller" semantics can be supported if desired.
 *
 * <P> Elements may choose to intercept certain parsing events directly.
 * They do this by overriding the default implementations of MethodInfos
 * in the <em>XmlReadable</em> interface.  This is normally done to make
 * the DOM tree represent application level modeling requirements, rather
 * than matching an XML structure that may not be optimized appropriately.
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
public class XmlDocumentBuilder implements ContentHandler, LexicalHandler,
    DeclHandler, DTDHandler
{
    // used during parsing
    protected XmlDocument		document;
    protected Locator		locator;
    private Locale		locale = Locale.getDefault ();

    private ElementFactory	factory;
    private Vector		attrTmp = new Vector ();
    
    protected ParentNode        elementStack[];
    protected int         	topOfStack;
    private boolean		inDTD;
    private boolean		inCDataSection;

    private Doctype		doctype;

    // parser modes
    private boolean		disableNamespaces = true; /* Keep this for
                                                             backward API
                                                             compatibility,
                                                             but it does
                                                             not change any
                                                             behavior. */
    private boolean             ignoreWhitespace = false;
    private boolean             expandEntityRefs = true;
    private boolean             ignoreComments = false;
    private boolean             putCDATAIntoText = false;

    
    /**
     * Default constructor is for use in conjunction with a SAX2 parser.
     */
    public XmlDocumentBuilder() {
        // No-op
    }

    
    /**
     * Returns true if certain lexical information is automatically
     * discarded when a DOM tree is built, producing smaller parse trees
     * that are easier to use.
     * <b>Obsolete:</b> for backwards compatibility
     */
    public boolean isIgnoringLexicalInfo () {
	return ignoreWhitespace && expandEntityRefs
                && ignoreComments && putCDATAIntoText;
    }

    /**
     * Controls whether certain lexical information is discarded.
     *
     * <P> That information includes whitespace in element content which
     * is ignorable (note that some nonvalidating XML parsers will not
     * report that information); all comments; which text is found in
     * CDATA sections; and boundaries of entity references.
     *
     * <P> "Ignorable whitespace" as reported by parsers is whitespace
     * used to format XML markup.  That is, all whitespace except that in
     * "mixed" or ANY content models is ignorable.  When it is discarded,
     * pretty-printing may be necessary to make the document be readable
     * again by humans.
     *
     * <P> Whitespace inside "mixed" and ANY content models needs different
     * treatment, since it could be part of the document content.  In such
     * cases XML defines a <em>xml:space</em> attribute which applications
     * should use to determine whether whitespace must be preserved (value
     * of the attribute is <em>preserve</em>) or whether default behavior
     * (such as eliminating leading and trailing space, and normalizing
     * consecutive internal whitespace to a single space) is allowed.
     *
     * @param value true indicates that such lexical information should
     *	be discarded during parsing.
     * <b>Obsolete:</b> for backwards compatibility
     */
    public void setIgnoringLexicalInfo (boolean value) {
        ignoreWhitespace = value;
        expandEntityRefs = value;
        ignoreComments = value;
        putCDATAIntoText = value;
    }

    /**
     * Internal API used by JAXP implementation.  Access is set to "public"
     * to enable inter-package access.  Use JAXP DocumentBuilderFactory
     * class to access this functionality.
     */
    public void setIgnoreWhitespace(boolean value) {
        ignoreWhitespace = value;
    }

    /**
     * Internal API used by JAXP implementation.  Access is set to "public"
     * to enable inter-package access.  Use JAXP DocumentBuilderFactory
     * class to access this functionality.
     */
    public void setExpandEntityReferences(boolean value) {
        expandEntityRefs = value;
    }

    /**
     * Internal API used by JAXP implementation.  Access is set to "public"
     * to enable inter-package access.  Use JAXP DocumentBuilderFactory
     * class to access this functionality.
     */
    public void setIgnoreComments(boolean value) {
        ignoreComments = value;
    }

    /**
     * Internal API used by JAXP implementation.  Access is set to "public"
     * to enable inter-package access.  Use JAXP DocumentBuilderFactory
     * class to access this functionality.
     */
    public void setPutCDATAIntoText(boolean value) {
        putCDATAIntoText = value;
    }


    /**
     * Returns true if namespace conformance is not checked as the
     * DOM tree is built.
     */
    public boolean getDisableNamespaces () {
	return disableNamespaces;
    }

    /**
     * Controls whether namespace conformance is checked during DOM
     * tree construction, or (the default) not.  In this framework, the
     * DOM Builder is responsible for enforcing all namespace constraints.
     * When enabled, this makes constructing a DOM tree slightly slower.
     * (However, at this time it can't enforce the requirement that
     * parameter entity names not contain colons.)
     */
    public void setDisableNamespaces (boolean value) {
	disableNamespaces = value;
    }

    /**
     * Return the result of parsing, after a SAX parser has used this as a
     * content handler during parsing.
     */
    public XmlDocument getDocument() {
        return document;
    }
    

    /**
     * Returns the locale to be used for diagnostic messages by
     * this builder, and by documents it produces.  This uses
     * the locale of any associated parser.
     */
    public Locale getLocale() {
        return locale;
    }
    
    /**
     * Assigns the locale to be used for diagnostic messages.
     * Multi-language applications, such as web servers dealing with
     * clients from different locales, need the ability to interact
     * with clients in languages other than the server's default.
     *
     * <P>When an XmlDocument is created, its locale is the default
     * locale for the virtual machine.  If a parser was recorded,
     * the locale will be associated with that parser.
     *
     * @see #chooseLocale
     */
    public void	setLocale(Locale locale)
        throws SAXException
    {
	if (locale == null) {
	    locale = Locale.getDefault();
        }
	this.locale = locale;
    }

    /**
     * Chooses a client locale to use for diagnostics, using the first
     * language specified in the list that is supported by this builder.
     * That locale is then automatically assigned using <a
     * href="#setLocale(java.util.Locale)">setLocale()</a>.  Such a list
     * could be provided by a variety of user preference mechanisms,
     * including the HTTP <em>Accept-Language</em> header field.
     *
     * @see org.apache.crimson.util.MessageCatalog
     *
     * @param languages Array of language specifiers, ordered with the most
     *	preferable one at the front.  For example, "en-ca" then "fr-ca",
     *  followed by "zh_CN".  Both RFC 1766 and Java styles are supported.
     * @return The chosen locale, or null.
     */
    public Locale chooseLocale (String languages [])
    throws SAXException
    {
	Locale	l = XmlDocument.catalog.chooseLocale (languages);

	if (l != null)
	    setLocale (l);
	return l;
    }

    /*
     * Gets the messages from the resource bundles for the given messageId.
     */
    String getMessage (String messageId) {
   	return getMessage (messageId, null);
    }

    /*
     * Gets the messages from the resource bundles for the given messageId
     * after formatting it with the parameters passed to it.
     */
    String getMessage (String messageId, Object[] parameters) {
   	if (locale == null) {
		getLocale ();
	}
	return XmlDocument.catalog.getMessage (locale, messageId, parameters);
    }

    
    //////////////////////////////////////////////////////////////////////
    // ContentHandler callbacks
    //////////////////////////////////////////////////////////////////////

    /**
     * Receive an object for locating the origin of SAX document events.
     */
    public void setDocumentLocator(Locator locator) {
	this.locator = locator;
    }

    /**
     * This is a factory MethodInfo, used to create an XmlDocument.
     * Subclasses may override this MethodInfo, for example to provide
     * document classes with particular behaviors, or provide
     * particular factory behaviours (such as returning elements
     * that support the HTML DOM MethodInfos, if they have the right
     * name and are in the right namespace).
     */
    public XmlDocument createDocument ()
    {
	XmlDocument retval = new XmlDocument ();

	if (factory != null) {
            retval.setElementFactory(factory);
        }
	return retval;
    }


    /**
     * Assigns the factory to be associated with documents produced
     * by this builder.
     * @deprecated
     */
    final public void setElementFactory(ElementFactory factory)	{
        this.factory = factory;
    }


    /**
     * Returns the factory to be associated with documents produced
     * by this builder.
     * @deprecated
     */
    final public ElementFactory getElementFactory() {
        return factory;
    }


    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument () throws SAXException
    {
	document = createDocument ();

	if (locator != null)
	    document.setSystemId (locator.getSystemId ());

	//
        // XXX don't want fixed size limits!  Fix someday.  For
	// now, wide trees predominate, not deep ones.  This is
	// allowing a _very_ deep tree ... we typically observe
	// depths on the order of a dozen.
	//
        elementStack = new ParentNode [200];
        topOfStack = 0;
        elementStack [topOfStack] = document;

	inDTD = false;
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument () throws SAXException
    {
        if (topOfStack != 0)
            throw new IllegalStateException (getMessage ("XDB-000"));
	document.trimToSize ();
    }
    
    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     */
    public void startPrefixMapping(String prefix, String uri)
	throws SAXException
    {
        // No-op
    }

    /**
     * End the scope of a prefix-URI mapping.
     */
    public void endPrefixMapping(String prefix)	throws SAXException {
        // No-op
    }

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
                attSet = AttributeSet.createAttributeSet1(attributes);
	    } catch (DOMException ex) {
		throw new SAXParseException(getMessage("XDB-002",
                        new Object[] { ex.getMessage() }), locator, ex);
	    }
	}

	//
	// Then create the element, associate its attributes, and
	// stack it for later addition.
	//
        ElementNode e = null;
	try {
            e = (ElementNode) document.createElementEx(qName);
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
    }

    /**
     * Receive notification of the end of an element.
     */
    public void endElement(String namespaceURI, String localName,
                           String qName)
	throws SAXException
    {
        ParentNode e = (ParentNode) elementStack[topOfStack];

        elementStack[topOfStack--] = null;

	// Trusting that the SAX parser is correct, and hasn't
	// mismatched start/end element callbacks.
        // if (!tag.equals (e.getTagName ()))
        //     throw new SAXParseException ((getMessage ("XDB-009", new
	//         Object[] { tag, e.getTagName ()  })), locator);

        e.reduceWaste();	// use less space
    }

    /**
     * Receive notification of character data.
     */
    public void characters(char buf [], int offset, int len)
        throws SAXException
    {
        ParentNode	top = elementStack [topOfStack];

	if (inCDataSection) {
	    String		temp = new String (buf, offset, len);
	    CDATASection	section;

	    section = (CDATASection) top.getLastChild ();
	    section.appendData (temp);
	    return;
	}

        
	try {
	    NodeBase lastChild = (NodeBase) top.getLastChild ();
	    if (lastChild != null && lastChild.getClass() == TextNode.class) {
                // Merge only TextNode data and not CDataNode data
		String tmp  = new String (buf, offset, len);
	   	((TextNode)lastChild).appendData (tmp);
	    } else {
        	TextNode text = document.newText (buf, offset, len);
	        top.appendChild (text);
	    }
	} catch (DOMException ex) {
	    throw new SAXParseException(getMessage("XDB-004",
                    new Object[] { ex.getMessage() }), locator, ex);
	}
    }
    
    /**
     * Receive notification of ignorable whitespace in element content.
     *
     * Reports ignorable whitespace; if lexical information is not ignored
     * the whitespace reported here is recorded in a DOM text (or CDATA, as
     * appropriate) node.
     *
     * @param buf holds text characters
     * @param offset initial index of characters in <em>buf</em>
     * @param len how many characters are being passed
     * @exception SAXException as appropriate
     */
    public void ignorableWhitespace(char buf [], int offset, int len)
        throws SAXException
    {
	if (ignoreWhitespace)
	    return;

        characters(buf, offset, len);
    }
    
    /**
     * Receive notification of a processing instruction.
     */
    public void processingInstruction(String name, String instruction) 
        throws SAXException
    {
	// Ignore PIs in DTD for DOM support
	if (inDTD)
	    return;

        ParentNode	top = elementStack [topOfStack];
        PINode		pi;
	
	try {
	    pi = (PINode) document.createProcessingInstruction (name,
		    instruction);
	    top.appendChild (pi);
	} catch (DOMException ex) {
	    throw new SAXParseException(getMessage("XDB-004",
                    new Object[] { ex.getMessage() }), locator, ex);
	}
    }

    /**
     * Receive notification of a skipped entity.
     */
    public void skippedEntity(String name) throws SAXException {
        // No-op
    }


    //////////////////////////////////////////////////////////////////////
    // org.xml.sax.ext.LexicalHandler callbacks
    //////////////////////////////////////////////////////////////////////

    /**
     * Report the start of DTD declarations, if any.
     */
    public void startDTD(String name, String publicId, String systemId)
	throws SAXException
    {
        DOMImplementation impl = document.getImplementation();
        doctype = (Doctype)impl.createDocumentType(name, publicId, systemId);

        // Set the owner since DOM2 specifies this to be null
        doctype.setOwnerDocument(document);

        inDTD = true;
    }

    /**
     * Report the end of DTD declarations.
     */
    public void endDTD() throws SAXException {
        document.appendChild(doctype);
        inDTD = false;
    }

    /**
     * Report the beginning of an entity in content.
     */
    public void startEntity(String name) throws SAXException {
    	// Our parser doesn't report Paramater entities. Need to make
	// changes for that.

        // Ignore entity refs while parsing DTD
	if (expandEntityRefs || inDTD) {
	    return;
        }

        EntityReference	e = document.createEntityReference(name);
	elementStack[topOfStack++].appendChild(e);
	elementStack[topOfStack] = (ParentNode)e;
    }

    /**
     * Report the end of an entity.
     */
    public void endEntity(String name) throws SAXException {
        // Ignore entity refs while parsing DTD
	if (inDTD) {
            return;
        }

        ParentNode entity = elementStack[topOfStack];

	if (!(entity instanceof EntityReference))
	    return;

	entity.setReadonly(true);
        elementStack[topOfStack--] = null;
        if (!name.equals(entity.getNodeName())) {
            throw new SAXParseException(getMessage("XDB-011",
                    new Object[] { name, entity.getNodeName() }), locator);
	}
    }

    /**
     * Report the start of a CDATA section.
     *
     * <P>If this builder is set to record lexical information then this
     * callback arranges that character data (and ignorable whitespace) be
     * recorded as part of a CDATA section, until the matching
     * <em>endCDATA</em> MethodInfo is called.
     */
    public void startCDATA() throws SAXException {
	if (putCDATAIntoText) {
	    return;
        }

        CDATASection text = document.createCDATASection("");
        ParentNode top = elementStack[topOfStack];
        
	try {
	    inCDataSection = true;
	    top.appendChild(text);
	} catch (DOMException ex) {
	    throw new SAXParseException(getMessage("XDB-004",
                    new Object[] { ex.getMessage() }), locator, ex);
	}
    }
    
    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA() throws SAXException {
        inCDataSection = false;
    }
    
    /**
     * Report an XML comment anywhere in the document.
     */
    public void comment(char[] ch, int start, int length) throws SAXException {
	// Ignore comments if lexical info is to be ignored,
	// or if parsing the DTD
	if (ignoreComments || inDTD) {
	    return;
        }

        String text = new String(ch, start, length);
        Comment comment = document.createComment(text);
        ParentNode top = elementStack[topOfStack];
        
	try {
	    top.appendChild(comment);
	} catch (DOMException ex) {
	    throw new SAXParseException(getMessage("XDB-004",
                    new Object[] { ex.getMessage() }), locator, ex);
	}
    }


    //////////////////////////////////////////////////////////////////////
    // org.xml.sax.ext.DeclHandler callbacks
    //////////////////////////////////////////////////////////////////////

    /**
     * Report an element type declaration.
     */
    public void elementDecl(String name, String model) throws SAXException {
        // ignored
    }

    /**
     * Report an attribute type declaration.
     */
    public void attributeDecl(String eName, String aName, String type,
                              String valueDefault, String value)
	throws SAXException
    {
        // ignored
    }

    /**
     * Report an internal entity declaration.
     */
    public void internalEntityDecl(String name, String value)
	throws SAXException
    {
        // SAX2 reports PEDecls which we ignore for DOM2.  SAX2 also reports
        // only the first defined GEDecl which matches with DOM2.
        if (!name.startsWith("%")) {
            doctype.addEntityNode(name, value);
        }
    }

    /**
     * Report a parsed external entity declaration.
     */
    public void externalEntityDecl(String name, String publicId,
                                   String systemId)
	throws SAXException
    {
        // SAX2 reports PEDecls which we ignore for DOM2.  SAX2 also reports
        // only the first defined GEDecl which matches with DOM2.
        if (!name.startsWith("%")) {
            doctype.addEntityNode(name, publicId, systemId, null);
        }
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
        doctype.addNotation(n, p, s);
    }

    /**
     * Receive notification of an unparsed entity declaration event.
     */
    public void unparsedEntityDecl(String name, String publicId, 
                                   String systemId, String notation)
	throws SAXException
    {
        doctype.addEntityNode(name, publicId, systemId, notation);
    }
}
