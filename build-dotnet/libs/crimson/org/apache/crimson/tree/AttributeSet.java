/*
 * $Id: AttributeSet.java,v 1.1 2005/04/11 23:47:43 rcm Exp $
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
import java.io.Writer;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.*;

import org.xml.sax.Attributes;

import org.apache.crimson.parser.AttributesEx;
import org.apache.crimson.util.XmlNames;


/**
 * Class representing a list of XML attributes.
 *
 * <P> This couples slightly with the Sun XML parser, in that it optionally
 * uses an extended SAX API to see if an attribute was specified in the
 * document or was instead defaulted by attribute processing.
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
final
class AttributeSet implements NamedNodeMap, XmlWritable
{
    private boolean     readonly;
    private Vector      list;
    private Element     ownerElement;
        
    private AttributeSet() {
        // no-arg constructor
    }

    /* Constructs an attribute list, with associated name scope. */
    // package private
    AttributeSet(Element ownerElement) {
        list = new Vector (5);
        this.ownerElement = ownerElement;
    }

    /*
     * Constructs a copy of an attribute list, for use in cloning.
     * AttributeNode.ownerElement is set later when the attributes are
     * associated with an Element.
     */
    // package private
    AttributeSet (AttributeSet original, boolean deep)
    {
        int             size = original.getLength ();

        list = new Vector (size);
        for (int i = 0; i < size; i++) {
            Node        node = original.item (i);

            if (!(node instanceof AttributeNode))
                throw new IllegalArgumentException (((NodeBase)node).
                                                getMessage ("A-003"));

            AttributeNode attr = (AttributeNode)node;
            node = attr.cloneAttributeNode(deep);
            list.addElement (node);
        }
    }

    /**
     * Constructor used to implement Document.importNode().  Only
     * "specified" Attr nodes are copied.  Copy is always deep.
     */
    AttributeSet(AttributeSet original) {
        int size = original.getLength();
        list = new Vector(size);

        for (int i = 0; i < size; i++) {
            Node node = original.item(i);

            if (!(node instanceof AttributeNode)) {
                throw new IllegalArgumentException(((NodeBase)node).
                                                   getMessage ("A-003"));
            }

            AttributeNode attr = (AttributeNode) node;
            // Copy only specified attributes
            if (attr.getSpecified()) {
                node = attr.cloneAttributeNode(true);
                list.addElement(node);
            }
        }
        list.trimToSize();
    }

    /**
     * Create a DOM NamedNodeMap consisting of DOM Level 2 Attr nodes from
     * a SAX2 Attributes object
     */
    static AttributeSet createAttributeSet2(Attributes source)
        throws DOMException
    {
        AttributeSet retval = new AttributeSet();

        int len = source.getLength();
        AttributesEx ex = null;

        retval.list = new Vector(len);
        if (source instanceof AttributesEx) {
            ex = (AttributesEx) source;
        }

        for (int i = 0; i < len; i++) {

            // Process the namespaceURI according to DOM Level 2 spec
            String uri;
            String qName = source.getQName(i);
            if ("xmlns".equals(qName)
                || "xmlns".equals(XmlNames.getPrefix(qName))) {
                // Associate the right namespaceURI with "xmlns" attributes
                uri = XmlNames.SPEC_XMLNS_URI;
            } else {
                uri = source.getURI(i);
                // Translate "" of SAX2 to null.  See DOM2 spec under Node
                // namespaceURI
                if ("".equals(uri)) {
                    uri = null;
                }
            }

            AttributeNode attrNode =
                new AttributeNode(uri, qName,
                                  source.getValue(i),
                                  ex == null    // remember if it was specified
                                  ? true
                                  : ex.isSpecified(i),
                                  ex == null    // remember any default value
                                  ? null
                                  : ex.getDefault(i));
            retval.list.addElement(attrNode);
        }
        return retval;
    }

    /**
     * Create a DOM NamedNodeMap consisting of DOM Level 1 Attr nodes from
     * a SAX2 Attributes object
     */
    static AttributeSet createAttributeSet1(Attributes source)
        throws DOMException
    {
        AttributeSet retval = new AttributeSet();

        int len = source.getLength();
        AttributesEx ex = null;

        retval.list = new Vector(len);
        if (source instanceof AttributesEx) {
            ex = (AttributesEx) source;
        }

	for (int i = 0; i < len; i++) {
            AttributeNode1 attrNode1 = new AttributeNode1(
                source.getQName(i),
                source.getValue(i),
                ex == null	// remember if it was specified
                    ? true
                    : ex.isSpecified(i),
                ex == null	// remember any default value
                    ? null
                    : ex.getDefault(i));
	    retval.list.addElement(attrNode1);
	}
        return retval;
    }

    // package private
    void trimToSize () { list.trimToSize (); }

    // package private
    public void setReadonly ()
    {
        readonly = true;
        for (int i = 0; i < list.size (); i++)
            ((AttributeNode)list.elementAt (i)).setReadonly (true);
    }

    public boolean isReadonly () {
        if (readonly)
            return true;
        for (int i = 0; i < list.size (); i++) {
            if (((AttributeNode)list.elementAt (i)).isReadonly ()) {
                return true; 
            }
        }
        return false;
    }

    // package private
    void setOwnerElement(Element e) {
        if (e != null && ownerElement != null) {
            throw new IllegalStateException(((NodeBase)e).getMessage("A-004"));
        }
        ownerElement = e;

        // need to bind the attributes to this element
        int length = list.size();

        for (int i = 0; i < length; i++) {
            AttributeNode node;

            node = (AttributeNode)list.elementAt(i);
            node.setOwnerElement(null);
            node.setOwnerElement(e);
        }
    }

    // package private
    String getValue (String name)
    {
        Attr    attr = (Attr) getNamedItem (name);

        if (attr == null)
            return "";
        else
            return attr.getValue ();
    }

    public Node getNamedItem (String name)
    {
        int     length = list.size ();
        Node    value;

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
        // DOM L2 spec specifies that Attr.localName is null for L1 created
        // Attr and Element nodes, therefore this MethodInfo cannot be used to
        // lookup such a node.
        if (localName == null) {
            return null;
        }

        for (int i = 0; i < list.size(); i++) {
            Node value = item(i);
            String iLocalName = value.getLocalName();
            if (localName.equals(iLocalName)) {
                String iNamespaceURI = value.getNamespaceURI();
                if (namespaceURI == iNamespaceURI ||
                    (namespaceURI != null
                     && namespaceURI.equals(iNamespaceURI))) {
                    return value;
                }
            }
        }
        return null;
    }

    public int getLength ()
    {
        return list.size ();
    }

    public Node item (int index)
    {
        if (index < 0 || index >= list.size ())
            return null;
        return (Node) list.elementAt (index);
    }

    public Node removeNamedItem(String name)
        throws DOMException
    {
        if (readonly) {
            throw new DomEx(DomEx.NO_MODIFICATION_ALLOWED_ERR);
        }
        for (int i = 0; i < list.size(); i++) {
            Node value = (Node)list.elementAt(i);
            if (value.getNodeName().equals(name)) {
                // Found a match
                list.removeElementAt(i);

                AttributeNode attr = (AttributeNode)value;

                // Replace with Attr node of default value if it has one
                String defaultValue = attr.getDefaultValue();
                if (defaultValue != null) {
                    AttributeNode newAttr = attr.cloneAttributeNode(true);
                    newAttr.setOwnerElement(attr.getOwnerElement());
                    newAttr.setValue(defaultValue);
                    newAttr.setSpecified(false);
                    list.addElement(newAttr);
                }

                // Set the ownerElement of attr to null since we're
                // removing it
                attr.setOwnerElement(null);
                return attr;
            }
        }
        throw new DomEx(DomEx.NOT_FOUND_ERR);
    }

    /**
     * <b>DOM2:</b>
     */
    public Node removeNamedItemNS(String namespaceURI, String localName)
        throws DOMException
    {
        if (readonly) {
            throw new DomEx(DomEx.NO_MODIFICATION_ALLOWED_ERR);
        }

        // See comments for getNamedItemNS() for why localName cannot be null
        if (localName == null) {
            throw new DomEx(DomEx.NOT_FOUND_ERR);
        }

        for (int i = 0; i < list.size(); i++) {
            Node value = (Node)list.elementAt(i);
            String iLocalName = value.getLocalName();
            if (localName.equals(iLocalName)) {
                String iNamespaceURI = value.getNamespaceURI();
                if (namespaceURI == iNamespaceURI ||
                    (namespaceURI != null
                     && namespaceURI.equals(iNamespaceURI))) {
                    // Found a match
                    list.removeElementAt(i);

                    AttributeNode attr = (AttributeNode)value;

                    // Replace with Attr node of default value if it has one
                    String defaultValue = attr.getDefaultValue();
                    if (defaultValue != null) {
                        AttributeNode newAttr = attr.cloneAttributeNode(true);
                        newAttr.setOwnerElement(attr.getOwnerElement());
                        newAttr.setValue(defaultValue);
                        newAttr.setSpecified(false);
                        list.addElement(newAttr);
                    }

                    // Set the ownerElement of attr to null since we're
                    // removing it
                    attr.setOwnerElement(null);
                    return attr;
                }
            }
        }
        throw new DomEx(DomEx.NOT_FOUND_ERR);
    }

    /**
     * Note: this MethodInfo both checks and sets the "ownerElement" of the
     * "value" parameter.  So if "ownerElement" is already set, an
     * incorrect error results.  Callers should avoid setting
     * "ownerElement".
     */
    public Node setNamedItem(Node value)
        throws DOMException
    {
        if (readonly) {
            throw new DomEx(DomEx.NO_MODIFICATION_ALLOWED_ERR);
        }
	if (!(value instanceof AttributeNode) ||
                value.getOwnerDocument() != ownerElement.getOwnerDocument()) {
	    throw new DomEx(DomEx.WRONG_DOCUMENT_ERR);
        }

        AttributeNode att = (AttributeNode)value;
	if (att.getOwnerElement() != null) {
	    throw new DomEx(DomEx.INUSE_ATTRIBUTE_ERR);
        }

        int length = list.size();
        AttributeNode oldAtt;
        for (int i = 0; i < length; i++) {
            oldAtt = (AttributeNode)item(i);
            if (oldAtt.getNodeName().equals(value.getNodeName())) {
                if (oldAtt.isReadonly()) {
                    throw new DomEx(DomEx.NO_MODIFICATION_ALLOWED_ERR);
                }
                att.setOwnerElement(ownerElement);
                list.setElementAt(att, i);
		oldAtt.setOwnerElement(null);
                return oldAtt;
            }
        }
        att.setOwnerElement(ownerElement);
        list.addElement(value);
        return null;
    }
    
    /**
     * <b>DOM2:</b>
     * Spec technically allows other types of nodes also, but this code
     * assumes Attr nodes only
     */
    public Node setNamedItemNS(Node arg) throws DOMException {
        if (readonly) {
            throw new DomEx(DomEx.NO_MODIFICATION_ALLOWED_ERR);
        }

	if (!(arg instanceof AttributeNode) ||
            arg.getOwnerDocument() != ownerElement.getOwnerDocument()) {
	    throw new DomEx(DomEx.WRONG_DOCUMENT_ERR);
        }

        AttributeNode attr = (AttributeNode) arg;
        if (attr.getOwnerElement() != null) {
            throw new DomEx(DomEx.INUSE_ATTRIBUTE_ERR);
        }

        // Both localName and namespaceURI can be null for Attr nodes
        // created by DOM Level 1 MethodInfos
        String localName = attr.getLocalName();
        String namespaceURI = attr.getNamespaceURI();

        int length = list.size();
        for (int i = 0; i < length; i++) {
            AttributeNode oldNode = (AttributeNode) item(i);
            String iLocalName = oldNode.getLocalName();
            String iNamespaceURI = oldNode.getNamespaceURI();
            if ((localName == iLocalName ||
                 (localName != null && localName.equals(iLocalName)))
                && (namespaceURI == iNamespaceURI ||
                    (namespaceURI != null
                     && namespaceURI.equals(iNamespaceURI)))) {
                // Found a matching node so replace it
                if (oldNode.isReadonly()) {
                    throw new DomEx(DomEx.NO_MODIFICATION_ALLOWED_ERR);
                }
                attr.setOwnerElement(ownerElement);
                list.setElementAt(attr, i);
		oldNode.setOwnerElement(null);
                return oldNode;
            }
        }

        // Append instead of replace
        attr.setOwnerElement(ownerElement);
        list.addElement(attr);
        return null;
    }

    /**
     * Writes out the attribute list.  Attributes known to have been
     * derived from the DTD are not (at this time) written out.  Part
     * of writing standalone XML is first ensuring that all attributes
     * are flagged as being specified in the "printed" form (or else
     * are defaulted only in the internal DTD subset).
     */
    public void writeXml (XmlWriteContext context) throws IOException
    {
        Writer          out = context.getWriter ();
        int             length = list.size ();
        AttributeNode   tmp;

        for (int i = 0; i < length; i++) {
            tmp = (AttributeNode) list.elementAt (i);
            if (tmp.getSpecified ()) {
                out.write (' ');
                tmp.writeXml (context);
            }
        }
    }

    /**
     * Does nothing; this type of node has no children.
     */
    public void writeChildrenXml (XmlWriteContext context) throws IOException
    {
    }

    public String toString ()
    {
        try {
            CharArrayWriter w = new CharArrayWriter ();
            XmlWriteContext x = new XmlWriteContext (w);
            writeXml (x);
            return w.toString ();

        } catch (IOException e) {
            return super.toString ();
        }
    }
}
