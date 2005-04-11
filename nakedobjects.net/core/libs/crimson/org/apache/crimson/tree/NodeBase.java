/*
 * $Id: NodeBase.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
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


import java.io.Writer;
import java.io.IOException;
import java.util.Locale;

import org.w3c.dom.*;


/**
 * Base class for DOM parse tree nodes which may appear in XmlDocument nodes.
 * This implements "parented by" and "owning document" relationships.  If
 * the parent is not null, this also supports sibling relationships through
 * that parent.  Children may be directly accessed through this class,
 * although it is subclasses which implement that access.
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
// not public ... javadoc looks a bit odd (hidden base class)
// but it's only subclassable within this package anyway
abstract class NodeBase
    implements Node, NodeEx, NodeList, XmlWritable
{
    private ParentNode	parent;
    private int		parentIndex = -1;	// cache

    // package private for speed ...
    XmlDocument		ownerDocument;
    boolean		readonly;
        
    /**
     * Constructs a node which will later have its parent set
     * by some other member of this package.
     */
    // package private 
    NodeBase () { }
        
    /* Returns the node's parent; typesafe convenience rtn. */
    // package private ... 
    ParentNode getParentImpl () { return parent; }

    /* not public yet ... */
    // package private
    public boolean isReadonly ()
	{ return readonly; }

    /* not public yet ... */
    // package private
    public void setReadonly (boolean deep)
    {
	readonly = true;
	if (deep) {
	    TreeWalker	walker = new TreeWalker (this);
	    Node	next;
	    while ((next = walker.getNext ()) != null)
		((NodeBase)next).setReadonly (false);
	}
    }
    
    /**
     * Returns the language id (value of <code>xml:lang</code>
     * attribute) applicable to this node, if known.  Traces up
     * through ancestors as needed.
     */
    public String getLanguage () {
	return getInheritedAttribute ("xml:lang");
    }

    /**
     * Returns the value of a given attribute, tracing up through
     * ancestors if needed.  In the XML standard, two attributes are
     * inherited:  <em>xml:lang</em> and <em>xml:space</em>.  This
     * mechanism can also be involved with Cascading Style Sheets (CSS).
     * The current version of XML Namespaces also uses inheritance.
     *
     * @param name identifies the attribute; colons may be included,
     *	but namespace prefixes are not interpreted
     * @return the value of the inherited attribute, or null if
     *	no value was inherited.
     */
    public String getInheritedAttribute (String name)
    {
	NodeBase	current = this;
	Attr		value = null;

	do {
	    if (current instanceof ElementNode2) {
		ElementNode2 e = (ElementNode2) current;

		if ((value = e.getAttributeNode (name)) != null)
		    break;
	    }
	    current = current.getParentImpl ();
	} while (current != null);
	if (value != null)
	    return value.getValue ();
	return null;
    }

    /**
     * Does nothing; this type of node has no children.
     */
    public void writeChildrenXml (XmlWriteContext context) throws IOException
    {
    }

    // DOM support from W3C recommendation

    /**
     * <b>DOM:</b> Returns the node's parent.  This will be null in cases such
     * as newly created or removed nodes, and for attributes, fragments,
     * and documents.
     */
    public Node getParentNode () {
	return parent;
    }


    /*
     * Assigns the node's parent, and remove it from its current parent.
     * The caller is responsible for letting the new parent know about
     * this child by inserting it into the set of children at the
     * appropriate place, if it's not null.
     *
     * <P> Note that this MethodInfo, of necessity, involves the DOM being in
     * an internally inconsistent state, either with the new parent not yet
     * knowing about its child-to-be after this call, or this node not
     * knowing its true parent before the call.  That's why it's not a
     * public API, here or in DOM.
     */
    void setParentNode (ParentNode arg, int index)
    throws DOMException
    {
	if (parent != null && arg != null)
	    parent.removeChild (this);
	parent = arg;
	parentIndex = index;
    }

        
    // assigns the owner of this document
    protected void setOwnerDocument(XmlDocument doc) {
	ownerDocument = doc;
    }

    /**
     * <b>DOM:</b> Returns the document to which this node belongs.
     */
    public Document getOwnerDocument ()
    {
	return ownerDocument;
    }


    /**
     * <b>DOM:</b>  Returns false.
     * Overridden by subclasses which support children.
     */
    public boolean hasChildNodes() { return false; }

    /**
     * <b>DOM:</b>  does nothing; overridden by subclasses as needed.
     */
    public void setNodeValue (String value)
    {
	if (readonly)
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);
    }
    
    /**
     * <b>DOM:</b>  Returns null; overridden by subclasses as needed.
     */
    public String getNodeValue ()
	{ return null; }
	
    /**
     * <b>DOM:</b>  Returns null.
     * Overridden by subclasses which support children.
     */
    public Node getFirstChild () { return null; }
    
    
    /**
     * <b>DOM:</b>  Returns zero.
     * Overridden by subclasses which support children.
     */
    public int getLength () { return 0; }


    /**
     * <b>DOM:</b>  Returns null.
     * Overridden by subclasses which support children.
     */
    public Node item (int i) { return null; }


    /**
     * <b>DOM:</b> Returns an object which permits "live" access to all
     * this node's children.
     *
     * <P> In this implementation, nodes provide such access without
     * needing another node as an intermediary; "this" is returned.
     */
    public NodeList           getChildNodes ()
	{ return this; }

    /**
     * <b>DOM:</b>  returns null.
     * Overridden by subclasses which support children.
     */
    public Node               getLastChild ()
	{ return null; }

    /**
     * <b>DOM:</b>  Throws a HIERARCHY_REQUEST_ERR DOMException.
     * Overridden by subclasses which support children.
     * @exception DOMException thrown always.
     */
    public Node appendChild (Node newChild)
    throws DOMException
	{ throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR); }

    /**
     * <b>DOM:</b>  Throws a HIERARCHY_REQUEST_ERR DOMException.
     * Overridden by subclasses which support children.
     * @exception DOMException thrown always.
     */
    public Node insertBefore (Node newChild, Node refChild)
    throws DOMException
	{ throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR); }

    /**
     * <b>DOM:</b>  Throws a HIERARCHY_REQUEST_ERR DOMException.
     * Overridden by subclasses which support children.
     * @exception DOMException thrown always.
     */
    public Node replaceChild (Node newChild, Node refChild)
    throws DOMException
	{ throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR); }

    /**
     * <b>DOM:</b>  Throws a HIERARCHY_REQUEST_ERR DOMException.
     * Overridden by subclasses which support children.
     * @exception DOMException thrown always.
     */
    public Node removeChild (Node oldChild)
    throws DOMException
	{ throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR); }

    
    /**
     * <b>DOM:</b>  Returns the node immediately following this node in a
     * breadth first traversal of the tree, or null if there is no
     * such sibling.  In this implementation, sibling access from a
     * node is slower than indexed access from its parent.
     */
    public Node getNextSibling ()
    {
	if (parent == null)
	    return null;
	if (parentIndex < 0 || parent.item (parentIndex) != this)
	    parentIndex = parent.getIndexOf (this);
	return parent.item (parentIndex + 1);
    }

    /**
     * <b>DOM:</b>  Returns the node immediately preceding this node in a
     * breadth first traversal of the tree, or null if there is no
     * such sibling.  In this implementation, sibling access from a
     * node is slower than indexed access from its parent.
     */
    public Node getPreviousSibling ()
    {
	if (parent == null)
	    return null;
	if (parentIndex < 0 || parent.item (parentIndex) != this)
	    parentIndex = parent.getIndexOf (this);
	return parent.item (parentIndex - 1);
    }

    /**
     * <b>DOM:</b> returns null.
     * Overridden by the ElementNode2 subclass.
     */
    public NamedNodeMap       getAttributes ()
	{ return null; }

    /**
     * <b>DOM2:</b> noop.
     * @since DOM Level 2
     * Overridden by subclasses that need to support normalization.
     */
    public void normalize() {
    }

    /**
     * <b>DOM2:</b>
     * @since DOM Level 2
     */
    public boolean isSupported(String feature, String version) {
        return DOMImplementationImpl.hasFeature0(feature, version);
    }

    /**
     * <b>DOM2:</b> returns null.
     * Overridden by subclasses that support namespaces, ie. ElementNode2 and
     * AttributeNode.
     */
    public String getNamespaceURI() {
        return null;
    }

    /**
     * <b>DOM2:</b> returns null.
     * Overridden by subclasses that support namespaces.
     */
    public String getPrefix() {
        return null;
    }

    /**
     * <b>DOM2:</b> throws DOMException.NAMESPACE_ERR
     * Overridden by subclasses that support namespaces.
     */
    public void setPrefix(String prefix) throws DOMException {
	throw new DomEx(DomEx.NAMESPACE_ERR);
    }

    /**
     * <b>DOM2:</b> returns null.
     * Overridden by subclasses that support namespaces.
     */
    public String getLocalName() {
        return null;
    }

    /**
     * Returns whether this node (if it is an element) has any attributes.
     * Overridden by Element implementations
     * @since DOM Level 2
     */
    public boolean hasAttributes() {
        return false;
    }

    public int getIndexOf (Node maybeChild)
	{ return -1; }

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
        Locale locale;

	if (this instanceof XmlDocument)
	    locale = ((XmlDocument)this).getLocale ();
	else if (ownerDocument == null)
	    locale = Locale.getDefault ();	// sigh
	else
	    locale = ownerDocument.getLocale ();
	return XmlDocument.catalog.getMessage (locale, messageId, parameters);
    }
}
