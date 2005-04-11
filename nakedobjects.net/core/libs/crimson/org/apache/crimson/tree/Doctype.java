/*
 * $Id: Doctype.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
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

import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.Writer;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.w3c.dom.*;

import org.xml.sax.SAXException;


/**
 * Class representing a DTD in DOM Level 1; this class exists purely
 * for editor support, and is of dubious interest otherwise.
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
final class Doctype extends NodeBase implements DocumentType
{
    // Stuff generated during parsing ...
    private String	name;
    private Nodemap	entities;
    private Nodemap	notations;

    // ... stuff assigned by apps separately
    private String	publicId;
    private String	systemId;
    private String	internalSubset;


    /**
     * XXX Obsolete, but keep it for backwards compatibility
     */
    // package private
    Doctype (String pub, String sys, String subset)
    {
	publicId = pub;
	systemId = sys;
	internalSubset = subset;
    }

    /**
     * New DOM Level 2 constructor
     */
    // package private
    Doctype(String name, String publicId, String systemId,
            String internalSubset)
    {
        this.name = name;
        this.publicId = publicId;
	this.systemId = systemId;
	this.internalSubset = internalSubset;
	entities = new Nodemap ();
	notations = new Nodemap ();
    }

    // package private
    void setPrintInfo (String pub, String sys, String subset)
    {
	publicId = pub;
	systemId = sys;
	internalSubset = subset;
    }


    /**
     * Writes out a textual DTD, trusting that any internal subset text
     * is in fact well formed XML.
     */
    public void writeXml (XmlWriteContext context) throws IOException
    {
	Writer	out = context.getWriter ();
	Element	root = getOwnerDocument ().getDocumentElement ();

	out.write ("<!DOCTYPE ");
	out.write (root == null ? "UNKNOWN-ROOT" : root.getNodeName ());

	if (systemId != null) {
	    if (publicId != null) {
		out.write (" PUBLIC '");
		out.write (publicId);
		out.write ("' '");
	    } else
		out.write (" SYSTEM '");
	    out.write (systemId);
	    out.write ("'");
	}
	if (internalSubset != null) {
	    out.write (XmlDocument.eol);
	    out.write ("[");
	    out.write (internalSubset);
	    out.write ("]");
	}
	out.write (">");
	out.write (XmlDocument.eol);
    }

    /** DOM: Returns DOCUMENT_TYPE_NODE */
    public short getNodeType ()
	{ return DOCUMENT_TYPE_NODE; }
    
    /** DOM:  Returns the name declared for the document root node. */
    public String getName ()
	{ return name; }
    
    /** DOM:  Returns the name declared for the document root node. */
    public String getNodeName ()
	{ return name; }
    
    /**
     * Only implement shallow clone for now, which is allowed in DOM Level 2.
     */
    public Node cloneNode(boolean deep)
    {
        Doctype retval = new Doctype(name, publicId, systemId, internalSubset);
        retval.setOwnerDocument((XmlDocument)getOwnerDocument());
        return retval;
    }

    /**
     * DOM: Returns the internal, external, and unparsed entities
     * declared in this DTD.
     */
    public NamedNodeMap getEntities ()
	{ return entities; }

    /** DOM: Returns the notations declared in this DTD.  */
    public NamedNodeMap getNotations ()
	{ return notations; }
    
    /**
     * The public identifier of the external subset.
     * @since DOM Level 2
     */
    public String getPublicId() {
        return publicId;
    }

    /**
     * The system identifier of the external subset.
     * @since DOM Level 2
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * The internal subset as a string.
     * @since DOM Level 2
     */
    public String getInternalSubset() {
        return internalSubset;
    }

    protected void setOwnerDocument(XmlDocument doc) {
	super.setOwnerDocument (doc);
	if (entities != null)
	    for (int i = 0; entities.item (i) != null; i++)
		((NodeBase)entities.item (i)).setOwnerDocument (doc);
	if (notations != null)
	    for (int i = 0; notations.item (i) != null; i++)
		((NodeBase)notations.item (i)).setOwnerDocument (doc);
    }


    /** Adds a notation node. */
    // package private
    void addNotation (String name, String pub, String sys)
    {
	NotationNode node = new NotationNode (name, pub, sys);
	node.setOwnerDocument ((XmlDocument)getOwnerDocument ());
	notations.setNamedItem (node);
    }

    /**
     * Adds an entity node for an external entity, which
     * could be parsed or unparsed.
     */
    // package private
    void addEntityNode (String name, String pub, String sys, String not)
    {
	EntityNode node = new EntityNode (name, pub, sys, not);
	node.setOwnerDocument ((XmlDocument)getOwnerDocument ());
	entities.setNamedItem (node);
    }

    /** Adds an entity node for an  internal parsed entity. */
    // package private
    void addEntityNode (String name, String value)
    {
	if ("lt".equals (name) || "gt".equals (name)
		|| "apos".equals (name) || "quot".equals (name)
		|| "amp".equals (name))
	    return;	// predeclared, immutable

	EntityNode node = new EntityNode (name, value);
	node.setOwnerDocument ((XmlDocument)getOwnerDocument ());
	entities.setNamedItem (node);
    }

    /** Marks the sets of entities and notations as readonly. */
    // package private
    void setReadonly ()
    {
	entities.readonly = true;
	notations.readonly = true;
    }


    static class NotationNode extends NodeBase implements Notation
    {
	private String	notation;
	private String	publicId;
	private String	systemId;

	NotationNode (String name, String pub, String sys)
	{
	    notation = name;
	    publicId = pub;
	    systemId = sys;
	}

	public String getPublicId ()
	    { return publicId; }

	public String getSystemId ()
	    { return systemId; }
	
	public short getNodeType ()
	    { return NOTATION_NODE; }
	
	public String getNodeName ()
	    { return notation; }
	
	public Node cloneNode (boolean ignored)
	{
	    NotationNode retval;

	    retval = new NotationNode (notation, publicId, systemId);
	    retval.setOwnerDocument ((XmlDocument)getOwnerDocument ());
	    return retval;
	}

	public void writeXml (XmlWriteContext context) throws IOException
	{
	    Writer out = context.getWriter ();
	    out.write ("<!NOTATION ");
	    out.write (notation);
	    if (publicId != null) {
		out.write (" PUBLIC '");
		out.write (publicId);
		if (systemId != null) {
		    out.write ("' '");
		    out.write (systemId);
		}
	    } else {
		out.write (" SYSTEM '");
		out.write (systemId);
	    }
	    out.write ("'>");
	}
    }


    // DOM permits the children to be null ... we do that consistently
    static class EntityNode extends NodeBase implements Entity
    {
	private String	entityName;

	private String	publicId;
	private String	systemId;
	private String	notation;

	private String	value;

	EntityNode (String name, String pub, String sys, String not)
	{
	    entityName = name;
	    publicId = pub;
	    systemId = sys;
	    notation = not;
	}

	EntityNode (String name, String value)
	{
	    entityName = name;
	    this.value = value;
	}

	public String getNodeName ()
	    { return entityName; }
	
	public short getNodeType ()
	    { return ENTITY_NODE; }

	public String getPublicId ()
	    { return publicId; }

	public String getSystemId ()
	    { return systemId; }

	public String getNotationName ()
	    { return notation; }
	
	public Node cloneNode (boolean ignored)
	{
	    EntityNode retval;

	    retval = new EntityNode (entityName, publicId, systemId,
			    notation);
	    retval.setOwnerDocument ((XmlDocument)getOwnerDocument ());
	    return retval;
	}

	public void writeXml (XmlWriteContext context) throws IOException
	{
	    Writer out = context.getWriter ();
	    out.write ("<!ENTITY ");
	    out.write (entityName);

	    if (value == null) {
		if (publicId != null) {
		    out.write (" PUBLIC '");
		    out.write (publicId);
		    out.write ("' '");
		} else
		    out.write (" SYSTEM '");
		out.write (systemId);
		out.write ("'");
		if (notation != null) {
		    out.write (" NDATA ");
		    out.write (notation);
		}
	    } else {
		out.write (" \"");
		int length = value.length ();
		for (int i = 0; i < length; i++) {
		    char c = value.charAt (i);
		    if (c == '"')
			out.write ("&quot;");
		    else
			out.write (c);
		}
		out.write ('"');
	    }
	    out.write (">");
	}
    }

    static class Nodemap implements NamedNodeMap
    {
	// package private
	boolean	readonly;

	// package private
	java.util.Vector	list = new java.util.Vector ();

	// XXX copied from AttributeSet; share implementation!
	public Node getNamedItem (String name)
	{
	    int	length = list.size ();
	    Node	value;

	    for (int i = 0; i < length; i++) {
		value = item (i);
		if (value.getNodeName ().equals (name))
		    return value;
	    }
	    return null;
	}

        /**
         * <b>DOM2:</b>
         */
        public Node getNamedItemNS(String namespaceURI, String localName) {
            return null;
        }
        
	// XXX copied from AttributeSet; share implementation!
	public int getLength ()
	{
	    return list.size ();
	}

	// XXX copied from AttributeSet; share implementation!
	public Node item (int index)
	{
	    if (index < 0 || index >= list.size ())
		return null;
	    return (Node) list.elementAt (index);
	}


	public Node removeNamedItem (String name)
	throws DOMException
	{
	    throw new DomEx (DOMException.NO_MODIFICATION_ALLOWED_ERR);
	}

        /**
         * <b>DOM2:</b>
         */
        public Node removeNamedItemNS(String namespaceURI, String localName)
            throws DOMException
        {
	    throw new DomEx(DOMException.NO_MODIFICATION_ALLOWED_ERR);
        }

	// caller guarantees (e.g. from parser) uniqueness
	public Node setNamedItem (Node item) throws DOMException
	{
	    if (readonly)
		throw new DomEx (DOMException.NO_MODIFICATION_ALLOWED_ERR);
	    list.addElement (item);
	    return null;
	}

        /**
         * <b>DOM2:</b>
         */
        public Node setNamedItemNS(Node arg) throws DOMException {
            if (readonly) {
                throw new DomEx(DomEx.NO_MODIFICATION_ALLOWED_ERR);
            }

            list.addElement(arg);
            return null;
        }

    }
}
