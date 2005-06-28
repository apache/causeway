/*
 * $Id: XmlDocument.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
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


import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.Writer;
import java.io.IOException;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

import org.w3c.dom.*;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.XMLReader;

import org.apache.crimson.parser.Parser2;
import org.apache.crimson.parser.Resolver;
import org.apache.crimson.parser.ValidatingParser;

import org.apache.crimson.util.MessageCatalog;
import org.apache.crimson.util.XmlNames;


/**
 * This class implements the DOM <em>Document</em> interface, and also
 * provides static factory MethodInfos to create document instances.  Instances
 * represent the top level of an XML 1.0 document, typically consisting
 * of processing instructions followed by one tree of XML data.  These
 * documents may be written out for transfer or storage using a variety
 * of text encodings.
 *
 * <P> The static factory MethodInfos do not offer any customization options.
 * in particular, they do not enforce XML Namespaces when parsing, do not
 * offer customizable element factories, and discard certain information
 * which is not intended to be significant to applications.  If your
 * application requires more sophisticated use of DOM, you may need
 * to use SAX directly with an <em>XmlDocumentBuilder</em>.
 *
 * <P> <b>Note: element factories are deprecated</b> because they are
 * non-standard.  They are only provided in this version for backwards
 * compatibility.  Instances are factories for their subsidiary nodes, but
 * applications may provide their own element factory to bind element tags
 * to particular DOM implementation classes (which must subclass
 * ElementNode).  For example, a factory may use a set of classes which
 * support the HTML DOM MethodInfos, or which support MethodInfos associated with
 * XML vocabularies for specialized problem domains as found within
 * Internet Commerce systems.  For example, an element tag
 * <code>&lt;PurchaseOrder&gt;</code> could be mapped to a
 * <code>com.startup.commerce.PurchaseOrder</code> class.  The factory can
 * also use XML Namespace information, if desired.
 *
 * <P> Since DOM requires nodes to be owned exclusively by one document,
 * they can't be moved from one document to another using DOM APIs.  This
 * class provides an <em>changeNodeOwner</em> functionality which may be
 * used to change the document associated with a node, and with any of its
 * children.
 *
 * <P> <em> Only the core DOM model is supported here, not the HTML support.
 * Such support basically adds a set of convenience element types, and so
 * can be implemented through element factories and document subclasses.</em>
 *
 * @see XmlDocumentBuilder
 *
 * @author David Brownell
 * @author Rajiv Mordani
 * @version $Revision: 1.1 $
 */
public class XmlDocument extends ParentNode implements DocumentEx
{
    // package private (with jdk 1.1 'javac' bug workaround)
    static /* final */ String           eol;

    static {
        String  temp;
        try { temp = System.getProperty ("line.separator", "\n"); }
        catch (SecurityException e) { temp = "\n"; }
        eol = temp;
    }

    static final MessageCatalog         catalog = new Catalog ();

    private Locale              locale = Locale.getDefault ();

    private String              systemId;
    private ElementFactory      factory;

    // package private
    int                         mutationCount;
    boolean replaceRootElement;

    /**
     * Constructs an empty document object.
     */
    public XmlDocument() {
        // No-op
    }

    /**
     * Construct an XML document from the data at the specified URI,
     * optionally validating.  This uses validating parser if
     * validation is requested, otherwise uses non-validating
     * parser.  XML Namespace conformance is not tested when parsing.
     *
     * @param documentURI The URI (normally URL) of the document
     * @param doValidate If true, validity errors are treated as fatal
     *
     * @exception IOException as appropriate
     * @exception SAXException as appropriate
     * @exception SAXParseException (with line number information)
     *  for parsing errors
     * @exception IllegalStateException at least when the parser
     *  is configured incorrectly
     * @deprecated Use JAXP javax.xml.parsers package instead
     */
    public static XmlDocument createXmlDocument (
        String  documentURI,
        boolean doValidate
    ) throws IOException, SAXException
    {
        return createXmlDocument (new InputSource (documentURI), doValidate);
    }


    /**
     * Construct an XML document from the data at the specified URI,
     * using the nonvalidating parser.  XML Namespace conformance
     * is not tested when parsing.
     *
     * @param documentURI The URI (normally URL) of the document
     *
     * @exception IOException as appropriate
     * @exception SAXException as appropriate
     * @exception SAXParseException (with line number information)
     *  for parsing errors
     * @exception IllegalStateException at least when the parser
     *  is configured incorrectly
     * @deprecated Use JAXP javax.xml.parsers package instead
     */
    public static XmlDocument createXmlDocument (String documentURI)
    throws IOException, SAXException
    {
        return createXmlDocument (new InputSource (documentURI), false);
    }


    /**
     * Construct an XML document from input stream, optionally validating.
     * This document must not require interpretation of relative URLs,
     * since the base URL is not known.  This uses the validating parser
     * if validation is requested, otherwise uses the non-validating
     * parser.  XML Namespace conformance is not tested when parsing.
     *
     * @param in Holds xml document
     * @param doValidate If true, validity errors are treated as fatal
     *
     * @exception IOException as appropriate
     * @exception SAXException as appropriate
     * @exception SAXParseException (with line number information)
     *  for parsing errors
     * @exception IllegalStateException at least when the parser
     *  is configured incorrectly
     * @deprecated Use JAXP javax.xml.parsers package instead
     */
    public static XmlDocument createXmlDocument (
        InputStream     in,
        boolean         doValidate
    ) throws IOException, SAXException
    {
        return createXmlDocument (new InputSource (in), doValidate);
    }

    /**
     * Construct an XML document from the data in the specified input
     * source, optionally validating.  This uses the validating parser
     * if validation is requested, otherwise uses the non-validating
     * parser.  XML Namespace conformance is not tested when parsing.
     *
     * @param in The input source of the document
     * @param doValidate If true, validity errors are treated as fatal
     *
     * @exception IOException as appropriate
     * @exception SAXException as appropriate
     * @exception SAXParseException (with line number information)
     *  for parsing errors
     * @exception IllegalStateException at least when the parser
     *  is configured incorrectly
     * @deprecated Use JAXP javax.xml.parsers package instead
     */
    public static XmlDocument createXmlDocument(InputSource in,
                                                boolean doValidate)
        throws IOException, SAXException
    {
        // Create XMLReader allowing user to override using system property
        // String defaultReader = "org.apache.xerces.parsers.SAXParser";
        String defaultReader = "org.apache.crimson.parser.XMLReaderImpl";
        String prop;
        try {
            prop = System.getProperty("org.xml.sax.driver", defaultReader);
        } catch (SecurityException se) {
            // This can happen if we are running as an applet
            prop = defaultReader;
        }
        XMLReader xmlReader = XMLReaderFactory.createXMLReader(prop);

        //
        // Namespace related features needed for XmlDocumentBuilder
        //
        String namespaces = "http://xml.org/sax/features/namespaces";
        xmlReader.setFeature(namespaces, true);

        String nsPrefixes = "http://xml.org/sax/features/namespace-prefixes";
        xmlReader.setFeature(nsPrefixes, true);

        // Create XmlDocumentBuilder instance
        XmlDocumentBuilder builder = new XmlDocumentBuilder();

        // Use as the ContentHandler
        xmlReader.setContentHandler(builder);
          
        // org.xml.sax.ext.LexicalHandler
        String lexHandler = "http://xml.org/sax/properties/lexical-handler";
        xmlReader.setProperty(lexHandler, builder);

        // org.xml.sax.ext.DeclHandler
        String declHandler
            = "http://xml.org/sax/properties/declaration-handler";
        xmlReader.setProperty(declHandler, builder);

        // DTDHandler
        xmlReader.setDTDHandler(builder);

        // Validation
        String validation = "http://xml.org/sax/features/validation";
        xmlReader.setFeature(validation, doValidate);

        // If validating, use an error handler that throws an exception for
        // validation errors.
        if (doValidate) {
            xmlReader.setErrorHandler(new DefaultHandler() {
                public void error(SAXParseException e) throws SAXException {
                    throw e;
                }
            });
        }

        builder.setDisableNamespaces(true);

        // Parse the input
        xmlReader.parse(in);
        return builder.getDocument();
    }

    /**
     * Returns the locale to be used for diagnostic messages.
     */
    public Locale       getLocale ()
        { return locale; }
    
    /**
     * Assigns the locale to be used for diagnostic messages.
     * Multi-language applications, such as web servers dealing with
     * clients from different locales, need the ability to interact
     * with clients in languages other than the server's default.
     * When an XmlDocument is created, its locale is the default
     * locale for the virtual machine.
     *
     * @see #chooseLocale
     */
    public void setLocale (Locale locale)
    {
        if (locale == null)
            locale = Locale.getDefault ();
        this.locale = locale;
    }

    /**
     * Chooses a client locale to use for diagnostics, using the first
     * language specified in the list that is supported by this DOM
     * implementation.  That locale is then automatically assigned using <a
     * href="#setLocale(java.util.Locale)">setLocale()</a>.  Such a list
     * could be provided by a variety of user preference mechanisms,
     * including the HTTP <em>Accept-Language</em> header field.
     *
     * @see org.apache.crimson.util.MessageCatalog
     *
     * @param languages Array of language specifiers, ordered with the most
     *  preferable one at the front.  For example, "en-ca" then "fr-ca",
     *  followed by "zh_CN".  Both RFC 1766 and Java styles are supported.
     * @return The chosen locale, or null.
     */
    public Locale chooseLocale (String languages [])
    {
        Locale  l = catalog.chooseLocale (languages);

        if (l != null)
            setLocale (l);
        return l;
    }

    
    /**
     * Writes the document in UTF-8 character encoding, as a well formed
     * XML construct.
     *
     * @param out stream on which the document will be written 
     */
    public void write (OutputStream out) throws IOException
    {
        Writer  writer = new OutputStreamWriter (out, "UTF8");
        write (writer, "UTF-8");
    }

    /**
     * Writes the document as a well formed XML construct.  If the
     * encoding can be determined from the writer, that is used in
     * the document's XML declaration.  The encoding name may first
     * be transformed from a Java-internal form to a standard one;
     * for example, Java's "UTF8" is the standard "UTF-8".
     *
     * <P> <em>Use of UTF-8 (or UTF-16) OutputStreamWriters is strongly
     * encouraged. </em>  All other encodings may lose critical data,
     * since the standard Java output writers substitute characters
     * such as the question mark for data which they can't encode in
     * the current output encoding.  The IETF and other organizations
     * strongly encourage the use of UTF-8; also, all XML processors
     * are guaranteed to support it.
     *
     * @see #write(java.io.Writer,java.lang.String)
     *
     * @param out stream on which the document will be written 
     */
    public void write (Writer out) throws IOException
    {
        String  encoding = null;

        if (out instanceof OutputStreamWriter)
            encoding = java2std (((OutputStreamWriter)out).getEncoding ());
        write (out, encoding);
    }


    //
    // Try some of the common conversions from Java's internal names
    // (which must fit in class names) to standard ones understood by
    // most other code.  We use the IETF's preferred names; case is
    // supposed to be ignored, note.
    //
    // package private 
    static String java2std (String encodingName)
    {
        if (encodingName == null)
            return null;

        //
        // ISO-8859-N is a common family of 8 bit encodings;
        // N=1 is the eight bit subset of UNICODE, and there
        // seem to be at least drafts for some N >10.
        //
        if (encodingName.startsWith ("ISO8859_"))       // JDK 1.2
            return "ISO-8859-" + encodingName.substring (8);
        if (encodingName.startsWith ("8859_"))          // JDK 1.1
            return "ISO-8859-" + encodingName.substring (5);

        // XXX seven bit encodings ISO-2022-* ...
        // XXX EBCDIC encodings ... 

        if ("ASCII7".equalsIgnoreCase (encodingName)
                || "ASCII".equalsIgnoreCase (encodingName))
            return "US-ASCII";
        
        //
        // All XML parsers _must_ support UTF-8 and UTF-16.
        // (UTF-16 ~= ISO-10646-UCS-2 plus surrogate pairs)
        //
        if ("UTF8".equalsIgnoreCase (encodingName))
            return "UTF-8";
        if (encodingName.startsWith ("Unicode"))
            return "UTF-16";
        
        //
        // Some common Japanese character sets.
        //
        if ("SJIS".equalsIgnoreCase (encodingName))
            return "Shift_JIS";
        if ("JIS".equalsIgnoreCase (encodingName))
            return "ISO-2022-JP";
        if ("EUCJIS".equalsIgnoreCase (encodingName))
            return "EUC-JP";

        // else we can't really do anything
        return encodingName;
    }

    
    /**
     * Writes the document in the specified encoding, and listing
     * that encoding in the XML declaration.  The document will be
     * well formed XML; at this time, it will not additionally be
     * valid XML or standalone, since it includes no document type
     * declaration.
     *
     * <P> Note that the document will by default be "pretty printed".
     * Extra whitespace is added to indent children of elements according
     * to their level of nesting, unless those elements have (or inherit)
     * the <em>xml:space='preserve'</em> attribute value.  This space
     * will be removed if, when the document is read back with DOM, a
     * call to <em>ElementNode.normalize</em> is made.  To avoid this
     * pretty printing, use a write context configured to disable it,
     * or explicitly assign an <em>xml:space='preserve'</em> attribute to
     * the root node of your document.
     *
     * <P> Also, if a SAX parser was used to construct this tree, data
     * will have been discarded.  Most of that will be insignificant in
     * terms of a "logical" view of document data:  comments, whitespace
     * outside of the top level element, the exact content of the XML
     * directive, and entity references were expanded.  However, <em>if a
     * DOCTYPE declaration was provided, it was also discarded</em>.
     * Such declarations will often be logically significant, due to the
     * attribute value defaulting and normalization they can provide.
     *
     * <P> In general, DOM does not support "round tripping" data from
     * XML to DOM and back without losing data about physical structures
     * and DTD information.  "Logical structure" will be preserved.
     *
     * @see #setDoctype
     * @see #writeXml
     *
     * @param out the writer to use when writing the document
     * @param encoding the encoding name to use; this should be a
     *  standard encoding name registered with the IANA (like "UTF-8")
     *  not a Java-internal name (like "UTF8").
     */
    public void write (Writer out, String encoding)
    throws IOException
    {
        //
        // We put a pretty minimal declaration here, which is the
        // best we can do given SAX input and DOM.  For the moment
        // this precludes our generating "standalone" annotations.
        //
        out.write ("<?xml version=\"1.0\"");
        if (encoding != null) {
            out.write (" encoding=\"");
            out.write (encoding);
            out.write ('\"');
        }
        out.write ("?>");
        out.write (eol);
        out.write (eol);

        writeChildrenXml (createWriteContext (out, 0));
        out.write (eol);
        out.flush ();
    }

    /**
     * Returns an XML write context set up not to pretty-print,
     * and which knows about the entities defined for this document.
     *
     * @param out stream on which the document will be written 
     */
    public XmlWriteContext createWriteContext (Writer out)
    {
        return new ExtWriteContext (out);
    }

    /**
     * Returns an XML write context which pretty-prints output starting
     * at a specified indent level, and which knows about the entities
     * defined for this document.
     *
     * @param out stream on which the document will be written 
     * @param level initial indent level for pretty-printing
     */
    public XmlWriteContext createWriteContext (Writer out, int level)
    {
        return new ExtWriteContext (out, level);
    }

    /**
     * Writes the document out using the specified context, using
     * an encoding name derived from the stream in the context where
     * that is possible.
     *
     * @see #createWriteContext(java.io.Writer)
     * @see #createWriteContext(java.io.Writer,int)
     *
     * @param context describes how to write the document
     */
    public void writeXml (XmlWriteContext context) throws IOException
    {
        Writer  out = context.getWriter ();
        String  encoding = null;

        //
        // XXX as above, it should be possible to be "told" this name
        // in order to use more standard names.  We can pretty print,
        // or we can use the right encoding name; not both!!
        //
        if (out instanceof OutputStreamWriter)
            encoding = java2std (((OutputStreamWriter)out).getEncoding ());

        //
        // We put a pretty minimal declaration here, which is the
        // best we can do given SAX input and DOM.  For the moment
        // this precludes our generating "standalone" annotations.
        //
        out.write ("<?xml version=\"1.0\"");
        if (encoding != null) {
            out.write (" encoding=\"");
            out.write (encoding);
            out.write ('\"');
        }
        out.write ("?>");
        out.write (eol);
        out.write (eol);

        writeChildrenXml (context);
    }

    /**
     * Writes all the child nodes of the document, following each one
     * with the end-of-line string in use in this environment.
     */
    public void writeChildrenXml (XmlWriteContext context) throws IOException
    {
        int     length = getLength ();
        Writer  out = context.getWriter ();

        if (length == 0)
            return;
        for (int i = 0; i < length; i++) {
            ((NodeBase)item (i)).writeXml (context);
            out.write (eol);
        }
    }


    // package private -- overrides base class MethodInfo
    void checkChildType (int type)
    throws DOMException
    {
        switch (type) {
          case ELEMENT_NODE:
          case PROCESSING_INSTRUCTION_NODE:
          case COMMENT_NODE:
          case DOCUMENT_TYPE_NODE:
            return;
          default:
            throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR);
        }
    }

    /**
     * Assigns the URI associated with the document, which is its
     * system ID.
     *
     * @param uri The document's system ID, as used when storing
     *  the document.
     */
    final public void setSystemId (String uri)
    {
        systemId = uri;
    }

    /**
     * Returns system ID associated with the document, or null if
     * this is unknown.
     *
     * <P> This URI should not be used when interpreting relative URIs,
     * since the document may be partially stored in external parsed
     * entities with different base URIs.  Instead, use MethodInfos in the
     * <em>XmlReadable</em> interface as the document is being parsed,
     * so that the correct base URI is available.
     */
    final public String getSystemId ()
    {
        return systemId;
    }


    // DOM support


    /**
     * DOM:  Appends the specified child node to the document.  Only one
     * element or document type node may be a child of a document.
     *
     * @param node the node to be appended.
     */
    public Node appendChild (Node n)
    throws DOMException
    {
        if (n instanceof Element && getDocumentElement () != null)
            throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR);
        if (n instanceof DocumentType && getDoctype () != null)
            throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR);
        return super.appendChild (n);
    }

    /**
     * DOM:  Inserts the specified child node into the document.  Only one
     * element or document type node may be a child of a document.
     *
     * @param n the node to be inserted.
     * @param refNode the node before which this is to be inserted
     */
    public Node insertBefore (Node n, Node refNode)
    throws DOMException
    {
        if (!replaceRootElement && n instanceof Element && 
            getDocumentElement () != null)
            throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR);
        if (!replaceRootElement && n instanceof DocumentType 
            && getDoctype () != null)
            throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR);
        return super.insertBefore (n, refNode);
    }

    /**
     * <b>DOM:</b>  Replaces the specified child with the new node,
     * returning the original child or throwing an exception.
     * The new child must belong to this particular document.
     *
     * @param newChild the new child to be inserted
     * @param refChild node which is to be replaced
     */
    public Node replaceChild (Node newChild, Node refChild)
    throws DOMException
    {
        if (newChild instanceof DocumentFragment ) {
            int elemCount = 0;
            int docCount = 0;
            replaceRootElement = false;
            ParentNode frag = (ParentNode) newChild;
            Node temp;
            int i = 0;
            while ((temp = frag.item (i)) != null) {
                if (temp instanceof Element) 
                    elemCount++;

                 else if (temp instanceof DocumentType)
                    docCount++;
                i++;
            }
            if (elemCount > 1 || docCount > 1)
                throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR);
            else 
                replaceRootElement = true;
        }
        return super.replaceChild (newChild, refChild);
    }


    /** DOM: Returns the DOCUMENT_NODE node type constant. */
    final public short getNodeType () { return DOCUMENT_NODE; }


    /** DOM: returns the document type (DTD) */
    final public DocumentType getDoctype ()
    {
        // We ignore comments, PIs, whitespace, etc
        // and return the first (only!) doctype.
        for (int i = 0; true; i++) {
            Node n = item (i);
            if (n == null)
                return null;
            if (n instanceof DocumentType)
                return (DocumentType) n;
        }
    }

    /**
     * Establishes how the document prints its document type.  If a system
     * ID (URI) is provided, that is used in a SYSTEM (or PUBLIC, if a public
     * ID is also provided) declaration.  If an internal subset is provided,
     * that will be printed.  The root element in the DTD will be what the
     * document itself provides.
     *
     * @param dtdPublicId Holds a "public identifier" used to identify the
     *  last part of the external DTD subset that is read into the DTD.
     *  This may be omitted, and in any case is ignored unless a system
     *  ID is provided.
     * @param dtdSystemId Holds a "system identifier" (a URI) used to
     *  identify the last part of the external DTD subset that is read
     *  into the DTD.  This may be omitted, in which case the document
     *  type will contain at most an internal subset.  This URI should
     *  not be a relative URI unless the document will be accessed in a
     *  context from which that relative URI makes sense.
     * @param internalSubset Optional; this holds XML text which will
     *  be put into the internal subset.  This must be legal syntax,
     *  and it is not tested by this document.
     */
    public DocumentType setDoctype (
        String  dtdPublicId,
        String  dtdSystemId,
        String  internalSubset
    ) {
        Doctype retval = (Doctype) getDoctype ();

        if (retval != null)
            retval.setPrintInfo (dtdPublicId, dtdSystemId,
                    internalSubset);
        else {
            retval = new Doctype (dtdPublicId, dtdSystemId,
                    internalSubset);
            retval.setOwnerDocument (this);
            insertBefore (retval, getFirstChild ());
        }
        return retval;
    }

    
    /**
     * DOM: Returns the content root element.
     */
    public Element getDocumentElement ()
    {
        // We ignore comments, PIs, whitespace, etc
        // and return the first (only!) element.
        for (int i = 0; true; i++) {
            Node n = item (i);
            if (n == null)
                return null;
            if (n instanceof Element) {
                return (Element)n;
            }
        }
    }

    /**
     * Assigns the element factory to be used by this document.
     *
     * @param factory the element factory to be used; if this is null,
     *	all elements will be implemented by <em>ElementNode</em>.
     * @deprecated
     */
    final public void setElementFactory (ElementFactory factory)
    {
	this.factory = factory;
    }

    /**
     * Returns the element factory to be used by this document.
     * @deprecated
     */
    final public ElementFactory getElementFactory ()
    {
	return factory;
    }


    /**
     * DOM:  Create a new element, associated with this document, with
     * no children, attributes, or parent, by calling createElementEx.
     *
     * @param tagName the tag of the element, used to determine what
     *  type element to create as well as what tag to assign the new node.
     * @exception IllegalArgumentException if a mapping is defined,
     *  but is invalid because the element can't be instantiated or
     *  does not subclass <em>ElementNode</em>.
     */
    public Element createElement(String tagName)
        throws DOMException
    {
        return createElementEx (tagName);
    }

    /**
     * <b>DOM2:</b>
     * @since DOM Level 2
     * Warning: Does not work with the deprecated ElementFactory
     */
    public Element createElementNS(String namespaceURI, String qualifiedName)
        throws DOMException
    {
        // Check arguments and throw appropriate exceptions
        ElementNode2.checkArguments(namespaceURI, qualifiedName);
        ElementNode2 retval = new ElementNode2(namespaceURI, qualifiedName);
        retval.setOwnerDocument(this);
        return retval;
    }

    /**
     * Create a new element, associated with this document, with no
     * children, attributes, or parent.  This uses the element factory,
     * or else directly constructs an ElementNode.
     *
     * @param tagName the tag of the element, used to determine what
     *  type element to create as well as what tag to assign the new node.
     * @exception IllegalArgumentException if a mapping is defined,
     *  but is invalid because the element can't be instantiated or
     *  does not subclass <em>ElementNode</em>.
     * @deprecated Use the standard MethodInfo createElement instead
     */
    final public ElementEx createElementEx(String tagName)
    throws DOMException
    {
        ElementNode retval;
	
	if (!XmlNames.isName (tagName)) {
	    throw new DomEx (DomEx.INVALID_CHARACTER_ERR);
        }

	if (factory != null) {
            // Ask factory to create appropriate ElementNode subtype
	    retval = (ElementNode) factory.createElementEx(tagName);
            // Set the name of the ElementNode
            retval.setTag(tagName);
	} else {
	    retval = new ElementNode(tagName);
        }
	retval.setOwnerDocument(this);
        return retval;
    }

    /**
     * Create a new element, associated with this document, with no
     * children, attributes, or parent.  This uses the element factory,
     * or else directly constructs an ElementNode.
     *
     * @param uri The namespace used to determine what type of element to
     *  create.  This is not stored with the element; the element must be
     *  inserted into a DOM tree in a location where namespace declarations
     *  cause its tag to be interpreted correctly.
     * @param tagName The tag of the element, which should not contain
     *	any namespace prefix.
     * @exception IllegalArgumentException When a mapping is defined,
     *  but is invalid because the element can't be instantiated or
     *  does not subclass <em>ElementNode</em>.
     * @deprecated Use the standard MethodInfo createElementNS instead
     */
    final public ElementEx createElementEx(String uri, String tagName)
    throws DOMException
    {
        ElementNode retval;
	
	if (!XmlNames.isName(tagName)) {
            throw new DomEx (DomEx.INVALID_CHARACTER_ERR);
        }

	if (factory != null) {
            // Ask factory to create appropriate ElementNode subtype
	    retval = (ElementNode) factory.createElementEx(uri, tagName);
            // Set the name of the ElementNode
            retval.setTag(tagName);
	} else {
	    retval = new ElementNode(tagName);
        }
	retval.setOwnerDocument(this);
        return retval;
    }

    /**
     * DOM:  returns a Text node initialized with the given text.
     *
     * @param text The contents of the text node being created, which
     *  should never contain "<em>]]&gt;</em>".
     */
    public Text createTextNode (String text)
    {
        TextNode                        retval;
        
        retval = new TextNode ();
        retval.setOwnerDocument (this);
        if (text != null)
            retval.setText (text.toCharArray ());
        return retval;
    }

    /**
     * DOM:  Returns a CDATA section initialized with the given text.
     *
     * @param text the text which the CDATA section will hold, which
     *  should never contain "<em>]]&gt;</em>".
     */
    public CDATASection createCDATASection (String text)
    {
        CDataNode       retval = new CDataNode ();

        if (text != null)
            retval.setText (text.toCharArray ());
        retval.setOwnerDocument (this);
        return retval;
    }

    // package private ... convenience rtn, reduced mallocation
    TextNode newText (char buf [], int offset, int len)
    throws SAXException
    {
        TextNode        retval = (TextNode) createTextNode (null);
        char            data [] = new char [len];

        System.arraycopy (buf, offset, data, 0, len);
        retval.setText (data);
        return retval;
    }
        

    /**
     * DOM:  Returns a Processing Instruction node for the specified
     * processing target, with the given instructions.
     *
     * @param target the target of the processing instruction
     * @param instructions the processing instruction, which should
     *  never contain "<em>?&gt;</em>".
     */
    public ProcessingInstruction createProcessingInstruction (
        String target,
        String instructions
    ) throws DOMException
    {
        if (!XmlNames.isName (target))
            throw new DomEx (DomEx.INVALID_CHARACTER_ERR);

        PINode  retval = new PINode (target, instructions);
        retval.setOwnerDocument (this);
        return retval;
    }


    /**
     * DOM:  Returns a valueless attribute node with no default value.
     *
     * @param name the name of the attribute.
     */
    public Attr createAttribute(String name) throws DOMException {
        if (!XmlNames.isName(name)) {
            throw new DomEx(DOMException.INVALID_CHARACTER_ERR);
        }
        AttributeNode1 retval = new AttributeNode1(name, "", true, null);
        retval.setOwnerDocument(this);
        return retval;
    }

    /**
     * <b>DOM2:</b>
     * @since DOM Level 2
     */
    public Attr createAttributeNS(String namespaceURI, String qualifiedName)
        throws DOMException
    {
        AttributeNode.checkArguments(namespaceURI, qualifiedName);
        AttributeNode retval = new AttributeNode(namespaceURI, qualifiedName,
                                                 "", true, null);
        retval.setOwnerDocument(this);
        return retval;
    }

    /**
     * DOM:  creates a comment node.
     *
     * @param data The characters which will be in the comment.
     *  This should not include the "<em>--</em>" characters.
     */
    public Comment createComment (String data)
    {
        CommentNode retval = new CommentNode (data);
        retval.setOwnerDocument (this);
        return retval;
    }


    /** DOM:  returns null. */
    public Document getOwnerDoc ()
    {
        return null;
    }

    /**
     * The <code>DOMImplementation</code> object that handles this document.
     */
    public DOMImplementation getImplementation() {
        return DOMImplementationImpl.getDOMImplementation();
    }

    /**
     * DOM:  Creates a new document fragment.
     */
    public DocumentFragment createDocumentFragment ()
    {
        DocFragNode     retval = new DocFragNode ();
        retval.setOwnerDocument (this);
        return retval;
    }


    /**
     * DOM:  Creates an entity reference to the named entity.
     * Note that the entity must already be defined in the document
     * type, and that the name must be a legal entity name.
     *
     * @param name the name of the the parsed entity
     */
    public EntityReference createEntityReference (String name)
    throws DOMException
    {
        if (!XmlNames.isName (name))
            throw new DomEx (DomEx.INVALID_CHARACTER_ERR);

        EntityRefNode retval = new EntityRefNode (name);
        retval.setOwnerDocument (this);
        return retval;
    }


    /** DOM: Returns the string "#document". */
    final public String getNodeName () { return "#document"; }


    /**
     * DOM: Returns a copy of this document.
     *
     * <P> <em>Note:</em> At this time, any element factory or document
     * type associated with this document will not be cloned.
     *
     * @param deep if true, child nodes are also cloned.
     */
    public Node cloneNode (boolean deep)
    {
        XmlDocument     retval = new XmlDocument ();

        retval.systemId = systemId;
        // XXX clone the element factory ...

        if (deep) {
            Node        node;

            for (int i = 0; (node = item (i)) != null; i++) {
                if (node instanceof DocumentType) {
                    // XXX recreate
                    continue;
                }
                node = node.cloneNode (true);
                retval.changeNodeOwner (node);
                retval.appendChild (node);
            }
        }

        return retval;
    }

    
    /**
     * Changes the "owner document" of the given node, and all child
     * and associated attribute nodes, to be this document.  If the
     * node has a parent, it is first removed from that parent.
     * <b>Obsolete</b> Use importNode MethodInfo instead.
     * 
     * @param node
     * @exception DOMException WRONG_DOCUMENT_ERROR when attempting
     *  to change the owner for some other DOM implementation<P>
     *  HIERARCHY_REQUEST_ERROR when the node is a document, document
     *  type, entity, or notation; or when it is an attribute associated
     *  with an element whose owner is not being (recursively) changed.
     */
    final public void changeNodeOwner (Node node)
    throws DOMException
    {
        TreeWalker      walker;
        NodeBase        n;

        if (node.getOwnerDocument () == this)
            return;
        if (!(node instanceof NodeBase))
            throw new DomEx (DomEx.WRONG_DOCUMENT_ERR);

        switch (node.getNodeType ()) {
          // Documents _are_ owners; can't switch identities
          case Node.DOCUMENT_NODE:

          // Entities, Notations only live in doctypes ... we
          // don't support changing their ownership at this time
          case Node.ENTITY_NODE:
          case Node.NOTATION_NODE:
          case Node.DOCUMENT_TYPE_NODE:
            throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR);
        }

        //
        // If node is an attribute, its "scoped" by one element...
        // and if that scope hasn't been changed (i.e. if this isn't
        // a recursive call) we can't really fix anything!
        //
        if (node instanceof AttributeNode) {
            AttributeNode       attr = (AttributeNode) node;
            Element             scope = attr.getOwnerElement();

            if (scope != null && scope.getOwnerDocument () != this)
                throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR);
        }

        // unparent node if needed
        n = (NodeBase) node.getParentNode ();
        if (n != null)
            n.removeChild (node);
        
        // change any children (including self)
        for (walker = new TreeWalker (node),
                    n = (NodeBase) walker.getCurrent ();
                n != null;
                n = (NodeBase) walker.getNext ()) {
            n.setOwnerDocument (this);

            // Elements have associated attributes, which must
            // also have owners changed.
            if (n instanceof Element) {
                NamedNodeMap    list = n.getAttributes ();
                int             length = list.getLength ();
                for (int i = 0; i < length; i++)
                    changeNodeOwner (list.item (i));
            }
        }
    }

    /**
     * Returns the <code>Element</code> whose <code>ID</code> is given by 
     * <code>elementId</code>.
     * 
     * @since DOM Level 2
     */
    public Element getElementById(String elementId) {
        return getElementExById(elementId);
    }

    /**
     * Returns the element whose ID is given by the parameter; or null
     * if no such element exists.  This relies on elements to know the 
     * name of their ID attribute, as will be currently be true only if
     * the document has been parsed from XML text with a DTD using the
     * <em>XmlDocumentBuilder</em> class, or if it has been constructed
     * using specialized DOM implementation classes which know the name
     * of their ID attribute.  (XML allows only one ID attribute per
     * element, and different elements may use different names for their
     * ID attributes.)
     *
     * <P> This may be used to implement internal IDREF linkage, as well
     * as some kinds of <em>XPointer</em> linkage as used in current
     * drafts of <em>XLink</em>.
     *
     * @param id The value of the ID attribute which will be matched
     *  by any element which is returned. 
     * @deprecated  As of DOM level 2, replaced by the MethodInfo
     *              Document.getElementById
     */
    // Note:  HTML DOM has getElementById() with "Element" return type
    public ElementEx getElementExById (String id)
    {
        if (id == null)
            throw new IllegalArgumentException (getMessage ("XD-000"));

        TreeWalker      w = new TreeWalker (this);
        ElementEx       element;

        while ((element = (ElementEx) w.getNextElement (null)) != null) {
            String      idAttr = element.getIdAttributeName ();
            String      value;

            if (idAttr == null)
                continue;
            value = element.getAttribute (idAttr);
            if (value.equals (id))
                return element;
        }
        return null;
    }

    /**
     * @since DOM Level 2
     */
    public Node importNode(Node importedNode, boolean deep)
        throws DOMException
    {
        // First make a copy of the subtree, then change the ownerDocument
        // of the subtree.

        Node node = null;

        switch (importedNode.getNodeType()) {
        case ATTRIBUTE_NODE:
            node = importedNode.cloneNode(true);
            break;
        case DOCUMENT_FRAGMENT_NODE:
            if (deep) {
                node = importedNode.cloneNode(true);
            } else {
                node = new DocFragNode();
            }
            break;
        case DOCUMENT_NODE:
        case DOCUMENT_TYPE_NODE:
            throw new DomEx(DomEx.NOT_SUPPORTED_ERR);
        case ELEMENT_NODE:
            node = ((ElementNode2) importedNode).createCopyForImportNode(deep);
            break;
        case ENTITY_NODE:
            node = importedNode.cloneNode(deep);
            break;
        case ENTITY_REFERENCE_NODE:
            node = importedNode.cloneNode(false);
            break;
        case NOTATION_NODE:
        case PROCESSING_INSTRUCTION_NODE:
        case TEXT_NODE:
        case CDATA_SECTION_NODE:
        case COMMENT_NODE:
        default:
            node = importedNode.cloneNode(false);
            break;
        }

        // Change the ownerDocument of subtree root and any children
        TreeWalker walker;
        NodeBase n;
        for (walker = new TreeWalker (node),
                    n = (NodeBase) walker.getCurrent ();
                n != null;
                n = (NodeBase) walker.getNext ()) {
            n.setOwnerDocument (this);

            // Elements have associated attributes, which must
            // also have owners changed.
            if (n instanceof Element) {
                NamedNodeMap    list = n.getAttributes ();
                int             length = list.getLength ();
                for (int i = 0; i < length; i++)
                    changeNodeOwner (list.item (i));
            }
        }

        return node;
    }


    //
    // Represent document fragments other than the document itself.
    // (This class is primarily motivated for use with editors.)
    //
    static final class DocFragNode extends ParentNode
        implements DocumentFragment
    {
        // package private -- overrides base class MethodInfo
        void checkChildType (int type)
        throws DOMException
        {
            switch (type) {
              case ELEMENT_NODE:
              case PROCESSING_INSTRUCTION_NODE:
              case COMMENT_NODE:
              case TEXT_NODE:
              case CDATA_SECTION_NODE:
              case ENTITY_REFERENCE_NODE:
                return;
              default:
                throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR);
            }
        }

        public void writeXml (XmlWriteContext context) throws IOException
        {
            this.writeChildrenXml (context);
        }

        public Node getParentNode ()
            { return null; }
        
        public void setParentNode (Node p)
            { if (p != null) throw new IllegalArgumentException (); }
        
        public short getNodeType ()
            { return DOCUMENT_FRAGMENT_NODE; }
        
        public String getNodeName () { 
            return ("#document-fragment");
        }
        
        public Node cloneNode (boolean deep)
        {
            DocFragNode retval = new DocFragNode ();
            ((NodeBase)retval).setOwnerDocument 
                                ((XmlDocument)this.getOwnerDocument ());

            if (deep) {
                Node    node;

                for (int i = 0; (node = item (i)) != null; i++) {
                    node = node.cloneNode (true);
                    retval.appendChild (node);
                }
            }
            return retval;
        }
    }


    //
    // Represent entity references.
    //
    final static class EntityRefNode extends ParentNode
        implements EntityReference
    {
        private String  entity;

        EntityRefNode (String name)
        {
            if (name == null)
                throw new IllegalArgumentException (getMessage ("XD-002"));
            entity = name;
        }
        
        // package private -- overrides base class MethodInfo
        void checkChildType (int type)
        throws DOMException
        {
            switch (type) {
              case ELEMENT_NODE:
              case PROCESSING_INSTRUCTION_NODE:
              case COMMENT_NODE:
              case TEXT_NODE:
              case CDATA_SECTION_NODE:
              case ENTITY_REFERENCE_NODE:
                return;
              default:
                throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR);
            }
        }
        public void writeXml (XmlWriteContext context)
        throws IOException
        {
            if (!context.isEntityDeclared (entity))
                throw new IOException (getMessage ("XD-003", new Object[]
                                                        { entity }));

            Writer out = context.getWriter ();

            out.write ('&');
            out.write (entity);
            out.write (';');
        }

        public short getNodeType ()
            { return ENTITY_REFERENCE_NODE; }
        
        public String getNodeName ()
            { return entity; }
        
        public Node cloneNode (boolean deep) { 
            EntityRefNode retval = new EntityRefNode (entity);
            ((NodeBase)retval).setOwnerDocument((
                                XmlDocument)this.getOwnerDocument ());
            if (deep) {
                Node    node;

                for (int i = 0; (node = item (i)) != null; i++) {
                    node = node.cloneNode (true);
                    retval.appendChild (node);
                }
                // XXX
                //throw new RuntimeException (getMessage ("XD-001"));
            }
            return retval;
        }
    }

    class ExtWriteContext extends XmlWriteContext
    {
        ExtWriteContext (Writer out) { super (out); }
        ExtWriteContext (Writer out, int level) { super (out, level); }

        public boolean isEntityDeclared (String name)
        {
            if (super.isEntityDeclared (name))
                return true;

            DocumentType        doctype = getDoctype ();

            if (doctype == null)
                return false;
            else
                return doctype.getEntities ().getNamedItem (name) != null;
        }
    }

    static class Catalog extends MessageCatalog
    {
        Catalog () { super (Catalog.class); }
    }
}
