/*
 * $Id: ElementNode2.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;

import java.util.Enumeration;

import org.apache.crimson.util.XmlNames;

import org.w3c.dom.*;


/**
 * Modified version of ElementNode to support DOM Level 2 MethodInfos.  This
 * class is named ElementNode2 for backward compatibility since old DOM
 * Level 1 apps may have subclassed ElementNode.
 * 
 * This class represents XML elements in a parse tree, and is often
 * subclassed to add custom behaviors.  When an XML Document object
 * is built using an <em>XmlDocumentBuilder</em> instance, simple
 * declarative configuration information may be used to control whether
 * this class, or some specialized subclass (e.g. supporting HTML DOM
 * MethodInfos) is used for elements in the resulting tree.
 *
 * <P> As well as defining new MethodInfos to provide behaviors which are
 * specific to application frameworks, such as Servlets or Swing, such
 * subclasses may also override MethodInfos such as <em>doneParse</em>
 * and <em>appendChild</em> to perform some kinds of processing during
 * tree construction.  Such processing can include transforming tree
 * structure to better suit the needs of a given application.  When
 * such transformation is done, the <em>XmlWritable</em> MethodInfos
 * may need to be overridden to make elements transform themselves back
 * to XML without losing information.  (One common transformation is
 * eliminating redundant representations of data; attributes of an XML
 * element may correspond to defaultable object properties, and so on.)
 *
 * <P> Element nodes also support a single <em>userObject</em> property,
 * which may be used to bind objects to elements where subclassing is
 * either not possible or is inappropriate.  For example, user interface
 * objects often derive from <code>java.awt.Component</code>, so that
 * they can't extend a different class (<em>ElementNode</em>).
 *
 * @see XmlDocumentBuilder
 *
 * @author David Brownell
 * @author Edwin Goei
 */
public class ElementNode2 extends NamespacedNode implements ElementEx
{
    protected AttributeSet	attributes;
    private String		idAttributeName;
    private Object		userObject;

    private static final char	tagStart [] = { '<', '/' };
    private static final char	tagEnd [] = { ' ', '/', '>' };
    
    public ElementNode2(String namespaceURI, String qName)
        throws DomEx
    {
        super(namespaceURI, qName);
    }

    /**
     * Make a clone of this node and return it.  Used for cloneNode().
     */
    ElementNode2 makeClone() {
        ElementNode2 retval = new ElementNode2(namespaceURI, qName);
        if (attributes != null) {
            retval.attributes = new AttributeSet(attributes, true);
            retval.attributes.setOwnerElement(retval);
        }
        retval.idAttributeName = idAttributeName;
        retval.userObject = userObject;
        retval.ownerDocument = ownerDocument;
        return retval;
    }

    /**
     * @return New ElementNode2 which is a copy of "this" but without
     * attributes that are defaulted in the original document.
     *
     * Used to implement Document.importNode().
     */
    ElementNode2 createCopyForImportNode(boolean deep) {
        ElementNode2 retval = new ElementNode2(namespaceURI, qName);
        if (attributes != null) {
            // Copy only "specified" Attr-s
            retval.attributes = new AttributeSet(attributes);
            retval.attributes.setOwnerElement(retval);
        }
        retval.userObject = userObject;

        if (deep) {
            // Copy ownerDocument to prevent appendChild() from throwing
            // WRONG_DOCUMENT_ERR for deep copies.  This gets changed to
            // the correct ownerDocument later in Document.importNode().
            retval.ownerDocument = ownerDocument;

            for (int i = 0; true; i++) {
                Node node = item(i);
                if (node == null) {
                    break;
                }
                if (node instanceof ElementNode2) {
                    retval.appendChild(
                        ((ElementNode2) node).createCopyForImportNode(true));
                } else {
                    retval.appendChild(node.cloneNode(true));
                }
            }
        }
        return retval;
    }

    static void checkArguments(String namespaceURI, String qualifiedName)
        throws DomEx
    {
        // [6] QName ::= (Prefix ':')? LocalPart
        // [7] Prefix ::= NCName
        // [8] LocalPart ::= NCName

	if (qualifiedName == null) {
            throw new DomEx(DomEx.NAMESPACE_ERR);
        }

	int first = qualifiedName.indexOf(':');

        if (first <= 0) {
            // no Prefix, only check LocalPart
            if (!XmlNames.isUnqualifiedName(qualifiedName)) {
                throw new DomEx(DomEx.INVALID_CHARACTER_ERR);
            }
            return;
        }

        // Prefix exists, check everything

	int last = qualifiedName.lastIndexOf(':');
	if (last != first) {
            throw new DomEx(DomEx.NAMESPACE_ERR);
        }
	
        String prefix = qualifiedName.substring(0, first);
        String localName = qualifiedName.substring(first + 1);
	if (!XmlNames.isUnqualifiedName(prefix)
                || !XmlNames.isUnqualifiedName(localName)) {
            throw new DomEx(DomEx.INVALID_CHARACTER_ERR);
        }

        // If we get here then we must have a valid prefix
        if (namespaceURI == null
            || (prefix.equals("xml") &&
                !XmlNames.SPEC_XML_URI.equals(namespaceURI))) {
            throw new DomEx(DomEx.NAMESPACE_ERR);
        }
    }

    public void trimToSize ()
    {
	super.trimToSize ();
	if (attributes != null)
	    attributes.trimToSize ();
    }

    // Assigns the element's attributes.
    void setAttributes (AttributeSet a)
    {
	AttributeSet oldAtts = attributes;

	// Check if the current AttributeSet or any attribute is readonly
	// isReadonly checks if any of the attributes in the AttributeSet
	// is readonly..
	if (oldAtts != null && oldAtts.isReadonly()) {
	    throw new DomEx(DomEx.NO_MODIFICATION_ALLOWED_ERR);
        }

	if (a != null) {
	    a.setOwnerElement(this);
        }
	attributes = a;
	if (oldAtts != null) {
	    oldAtts.setOwnerElement(null);
        }
    }

    // package private -- overrides base class MethodInfo
    void checkChildType (int type)
    throws DOMException
    {
	switch (type) {
	  case ELEMENT_NODE:
	  case TEXT_NODE:
	  case COMMENT_NODE:
	  case PROCESSING_INSTRUCTION_NODE:
	  case CDATA_SECTION_NODE:
	  case ENTITY_REFERENCE_NODE:
	    return;
	  default:
	    throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR);
	}
    }

    // package private -- overrides base class MethodInfo
    public void setReadonly (boolean deep)
    {
	if (attributes != null)
	    attributes.setReadonly ();
	super.setReadonly (deep);
    }

    /** <b>DOM:</b> Returns the attributes of this element. */
    public NamedNodeMap getAttributes ()
    {
	if (attributes == null)
	    attributes = new AttributeSet (this);
        return attributes;
    }
        
    /**
     * Returns whether this node (if it is an element) has any attributes.
     * @since DOM Level 2
     */
    public boolean hasAttributes() {
        return attributes != null;
    }

    /**
     * Returns the element and its content as a string, which includes
     * all the markup embedded in this element.  If the element is not
     * fully constructed, the content will not be an XML tag.
     */
    public String toString ()
    {
	try {
	    CharArrayWriter	out = new CharArrayWriter ();
	    XmlWriteContext	x = new XmlWriteContext (out);
	    writeXml (x);
	    return out.toString ();
	} catch (Exception e) {
	    return super.toString ();
	}
    }
    
    
    /**
     * Writes this element and all of its children out, as well
     * formed XML.
     */
    public void writeXml (XmlWriteContext context) throws IOException
    {
	Writer	out = context.getWriter ();

	if (qName == null)
	   throw new IllegalStateException ( getMessage ("EN-002"));
	   
	out.write (tagStart, 0, 1);	// "<"
	out.write (qName);
	
        if (attributes != null)
	    attributes.writeXml (context);

	//
	// Write empty nodes as "<EMPTY />" to make sure version 3
	// and 4 web browsers can read empty tag output as HTML.
	// XML allows "<EMPTY/>" too, of course.
	//
	if (!hasChildNodes ())
	    out.write (tagEnd, 0, 3);	// " />"
	else  {
	    out.write (tagEnd, 2, 1);	// ">"
	    writeChildrenXml (context);
	    out.write (tagStart, 0, 2);	// "</"
	    out.write (qName);
	    out.write (tagEnd, 2, 1);	// ">"
	}
    }

    /**
     * Assigns the name of the element's ID attribute; only one attribute
     * may have the ID type.  XML supports a kind of validatable internal
     * linking using ID attributes, with IDREF attributes identifying
     * specific nodes (and IDREFS attributes identifying sets of them).
     */
    public void setIdAttributeName (String attName)
    {
	if (readonly)
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);
	idAttributeName = attName;
    }

    /**
     * Returns the name of the element's ID attribute, if one is known.
     */
    public String getIdAttributeName ()
	{ return idAttributeName; }

    
    public void setUserObject (Object userObject)
	{ this.userObject = userObject; }

    public Object getUserObject ()
	{ return userObject; }

    // DOM support

    /** <b>DOM:</b> Returns the ELEMENT_NODE node type. */
    public short getNodeType ()  { return ELEMENT_NODE; }

    /** <b>DOM:</b> Returns the name of the XML tag for this element. */
    public String getTagName () { return qName; }
    
    /**
     * Returns <code>true</code> when an attribute with a given name is
     * specified on this element or has a default value, <code>false</code>
     * otherwise.
     * @since DOM Level 2
     */
    public boolean hasAttribute(String name) {
        return getAttributeNode(name) != null;
    }

    /**
     * Returns <code>true</code> when an attribute with a given local name
     * and namespace URI is specified on this element or has a default
     * value, <code>false</code> otherwise.
     * @since DOM Level 2
     */
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return getAttributeNodeNS(namespaceURI, localName) != null;
    }

    /** <b>DOM:</b> Returns the value of the named attribute, or an empty
     * string 
     */
    public String getAttribute (String name)
    {
	return (attributes == null)
	    ? ""
	    : attributes.getValue (name);
    }

    /**
     * Retrieves an attribute value by local name and namespace URI. 
     * @since DOM Level 2
     */
    public String getAttributeNS(String namespaceURI, String localName) {
	if (attributes == null) {
	    return "";
        }
	Attr attr = getAttributeNodeNS(namespaceURI, localName);
	if (attr == null) {
	    return "";
        }
	return attr.getValue();
    }

    /**
     * Retrieves an <code>Attr</code> node by local name and namespace URI. 
     * @since DOM Level 2
     */
    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
	if (localName == null) {
	    return null;
        }
	if (attributes == null) {
            return null;
        }
        for (int i = 0; ; i++) {
            AttributeNode attr = (AttributeNode) attributes.item(i);
            if (attr == null) {
                return null;
            }
            if (localName.equals(attr.getLocalName())
                && (attr.getNamespaceURI() == namespaceURI
                    || attr.getNamespaceURI().equals(namespaceURI))) {
                return attr;
            }
        }
    }
    
    /**
     * <b>DOM:</b> Assigns or modifies the value of the specified attribute.
     */
    public void setAttribute (String name, String value)
    throws DOMException
    {
	NodeBase att;                   // Common superclass of all Attr nodes

	if (readonly)
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);
        if (!XmlNames.isName(name)) {
            throw new DomEx(DOMException.INVALID_CHARACTER_ERR);
        }
	if (attributes == null)
	    attributes = new AttributeSet (this);
	if ((att = (NodeBase) attributes.getNamedItem (name)) != null)
	    att.setNodeValue (value);
	else {
	    att = new AttributeNode1(name, value, true, null);
	    att.setOwnerDocument ((XmlDocument) getOwnerDocument ());
            /* "ownerElement" should be null before calling "setNamedItem" */
	    attributes.setNamedItem (att);
	}
    }
    
    /**
     * <b>DOM2:</b>
     * @since DOM Level 2
     */
    public void setAttributeNS(String namespaceURI, String qualifiedName, 
                               String value)
        throws DOMException
    {
        AttributeNode.checkArguments(namespaceURI, qualifiedName);

        Attr attr = getAttributeNodeNS(namespaceURI,
                XmlNames.getLocalPart(qualifiedName));
        if (attr == null) {
            AttributeNode newAttr = new AttributeNode(namespaceURI,
                                                      qualifiedName, value,
                                                      true, null);
	    newAttr.setOwnerDocument((XmlDocument)getOwnerDocument());
            setAttributeNodeNS(newAttr);
        } else {
            attr.setValue(value);
            attr.setPrefix(XmlNames.getPrefix(qualifiedName));
        }
    }

    /**
     * <b>DOM2:</b>
     * @since DOM Level 2
     */
    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
	if (readonly) {
	    throw new DomEx(DomEx.NO_MODIFICATION_ALLOWED_ERR);
        }
        if (newAttr.getOwnerDocument() != getOwnerDocument()) {
	    throw new DomEx(DomEx.WRONG_DOCUMENT_ERR);
        }

	if (attributes == null) {
	    attributes = new AttributeSet(this);
        }

        // Note: ownerElement of newAttr is both checked and set in the
        // following call to AttributeSet.setNamedItemNS(Node)
	return (Attr)attributes.setNamedItemNS(newAttr);
    }

    /** <b>DOM:</b> Remove the named attribute. */
    public void removeAttribute (String name)
    throws DOMException
    {
	if (readonly)
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);
	if (attributes == null) {
            return;
        }
        try {
            attributes.removeNamedItem (name);
        } catch (DOMException x) {
            // DOM2 does not allow a NOT_FOUND_ERR exception to be thrown
            if (x.code != DOMException.NOT_FOUND_ERR) {
                throw x;
            }
        }
    }

    /**
     * <b>DOM2:</b>
     * @since DOM Level 2
     */
    public void removeAttributeNS(String namespaceURI, String localName)
        throws DOMException
    {
	if (readonly) {
	    throw new DomEx(DomEx.NO_MODIFICATION_ALLOWED_ERR);
        }
        try {
            attributes.removeNamedItemNS(namespaceURI, localName);
        } catch (DOMException x) {
            // DOM2 does not allow a NOT_FOUND_ERR exception to be thrown
            if (x.code != DOMException.NOT_FOUND_ERR) {
                throw x;
            }
        }
    }

    /** <b>DOM:</b>  returns the attribute */
    public Attr getAttributeNode (String name)
    {
	if (attributes != null)
	    return (Attr) attributes.getNamedItem (name);
	else
	    return null;
    }
    
    /** <b>DOM:</b> assigns the attribute */
    public Attr setAttributeNode (Attr newAttr)
    throws DOMException
    {
	if (readonly)
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);
	if (!(newAttr instanceof AttributeNode))
	    throw new DomEx (DomEx.WRONG_DOCUMENT_ERR);

	if (attributes == null) 
	    attributes = new AttributeSet (this);

        // Note: ownerElement of newAttr is both checked and set in the
        // following call to AttributeSet.setNamedItem(Node)
	return (Attr) attributes.setNamedItem(newAttr);
    }
    
    /** <b>DOM:</b> removes the attribute with the same name as this one */
    public Attr removeAttributeNode (Attr oldAttr)
    throws DOMException
    {
	if (isReadonly ())
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);

	Attr	attr = getAttributeNode (oldAttr.getNodeName ());
	if (attr == null)
	    throw new DomEx (DomEx.NOT_FOUND_ERR);
	removeAttribute (attr.getNodeName ());
	return attr;
    }

    /**
     * Creates a new unparented node whose attributes are the same as
     * this node's attributes; if <em>deep</em> is true, the children
     * of this node are cloned as children of the new node.
     */
    public Node cloneNode (boolean deep)
    {
	try {
	    ElementNode2 retval = makeClone();
	    if (deep) {
		for (int i = 0; true; i++) {
		    Node	node = item (i);
		    if (node == null)
			break;
		    retval.appendChild (node.cloneNode (true));
		}
	    }
	    return retval;
	} catch (DOMException e) {
	    throw new RuntimeException (getMessage ("EN-001"));
	}
    }

    /**
     * Convenience MethodInfo to construct a non-prettyprinting XML write
     * context and call writeXml with it.  Subclasses may choose to
     * to override this MethodInfo to generate non-XML text, 
     *
     * @param out where to emit the XML content of this node
     */
    public void write (Writer out) throws IOException
    {
	writeXml (new XmlWriteContext (out));
    }
}
