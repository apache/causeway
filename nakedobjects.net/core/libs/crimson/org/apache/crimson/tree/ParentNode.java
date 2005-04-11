/*
 * $Id: ParentNode.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
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
import java.io.Writer;

import org.xml.sax.SAXException;

import org.w3c.dom.*;


/**
 * This adds an implementation of "parent of" relationships to the NodeBase
 * class.  It implements operations for maintaining a set of children,
 * providing indexed access to them, and writing them them out as text.
 *
 * <P> The NodeList it implements to describe its children is "live", as
 * required by DOM.  That means that indexed accesses by applications must
 * handle cases associated with unstable indices and lengths.  Indices
 * should not be stored if they can be invalidated by changes, including
 * changes made by other threads.
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
// not public ... javadoc looks a bit odd (hidden base class)
// but it's only subclassable within this package anyway
abstract class ParentNode extends NodeBase
{
    private NodeBase		children [];
    private int			length;

    /**
     * Builds a ParentNode, which can have children that are
     * subclasses of NodeBase.
     */
    // package private
    ParentNode () { }

    /**
     * Called to minimize space utilization.  Affects only
     * this node; children must be individually trimmed.
     */
    public void trimToSize ()
    {
	if (length == 0)
	    children = null;
        else if (children.length != length) {
	    NodeBase	temp [] = new NodeBase [length];

            System.arraycopy (children, 0, temp, 0, length);
	    children = temp;
	}
    }

    // package private
    void reduceWaste ()
    {
	if (children == null)
	    return;

	//
	// Arbitrary -- rather than paying trimToSize() costs
	// on most elements, we routinely accept some waste but
	// do try to reduce egregious waste.  Interacts with
	// the array allocation done in appendChild.
	//
	if ((children.length - length) > 6)
            trimToSize ();
    }


    /**
     * Writes each of the child nodes.  For element nodes, this adds
     * whitespace to indent non-text children (it prettyprints) unless
     * the <em>xml:space='preserve'</em> attribute applies, or the
     * write context disables prettyprinting.
     *
     * @param context describes how the children should be printed
     */
    public void writeChildrenXml (XmlWriteContext context) throws IOException
    {
	if (children == null)
	    return;

	int	oldIndent = 0;
	boolean	preserve = true;
	boolean	pureText = true;

	if (getNodeType () == ELEMENT_NODE) {
	    preserve = "preserve".equals (
		    getInheritedAttribute ("xml:space"));
	    oldIndent = context.getIndentLevel ();
	}

	try {
	    if (!preserve)
		context.setIndentLevel (oldIndent + 2);
	    for (int i = 0; i < length; i++) {
		if (!preserve && children [i].getNodeType () != TEXT_NODE) {
		    context.printIndent ();
		    pureText = false;
		}
		children [i].writeXml (context);
	    }
	} finally {
	    if (!preserve) {
		context.setIndentLevel (oldIndent);
		if (!pureText)
		    context.printIndent ();		// for ETag
	    }
	}
    }


    // package private -- overridden in implementation classes
    abstract void checkChildType (int type)
    throws DOMException;

    // DOM support


    /**
     * <b>DOM:</b>  Returns true if there are children to this node.
     */
    final public boolean hasChildNodes ()
    {
	return length > 0;
    }

    /**
     * <b>DOM:</b>  Returns the first child of this node, else null if there
     * are no children.
     */
    final public Node getFirstChild ()
    {
	if (length == 0)
	    return null;
	return children [0];
    }
    
    /**
     * <b>DOM:</b>  Returns the last child of this node, else null if there
     * are no children.
     */
    final public Node getLastChild ()
    {
	if (length == 0)
	    return null;
	return children [length - 1];
    }

    /** <b>DOM:</b>  Returns the number of children */
    final public int getLength ()
    {
	return length;
    }

    /** <b>DOM:</b>  Returns the Nth child, or null */
    final public Node item (int i)
    {
	if (length == 0 || i >= length)
	    return null;
	try {
	    return children [i];
	} catch (ArrayIndexOutOfBoundsException e) {
	    return null;
	}
    }

    // groups all the "wrong document/implementation" checks
    private NodeBase checkDocument (Node newChild)
    throws DOMException
    {
	if (newChild == null)
	    throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR);

	// check for wrong implementation
	if (!(newChild instanceof NodeBase))
	    throw new DomEx (DomEx.WRONG_DOCUMENT_ERR);

	Document	owner = newChild.getOwnerDocument ();
	XmlDocument	myOwner = ownerDocument;
	NodeBase	child = (NodeBase) newChild;

	// bizarre DOM special case for document
	if (myOwner == null && this instanceof XmlDocument)
	    myOwner = (XmlDocument) this;

	// check for wrong document
	if (owner != null && owner != myOwner)
	    throw new DomEx (DomEx.WRONG_DOCUMENT_ERR);
	
	// permit "unowned" NodeBase children to be added,
	// e.g. if someone constructs an ElementNode directly
	if (owner == null) {
	    child.setOwnerDocument (myOwner);
	}

        if (child.hasChildNodes ()) {
    	    for (int i = 0; true; i++) {
	        Node	node = child.item (i);
	        if (node == null)
		    break;
	        if (node.getOwnerDocument () == null)
	    	    ((NodeBase)node).setOwnerDocument (myOwner);
	        else if (node.getOwnerDocument () != myOwner)
		    throw new DomEx (DomEx.WRONG_DOCUMENT_ERR);
	    }
        }

	return child;
    }

    // makes sure that child isn't an ancestor of this
    private void checkNotAncestor (Node newChild) throws DOMException
    {
	// text, etc ...
	if (!newChild.hasChildNodes ())
	    return;

	Node	ancestor = this;

	while (ancestor != null) {
	    if (newChild == ancestor)
		throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR);
	    ancestor = ancestor.getParentNode ();
	}
    }

    // update mutation count 
    private void mutated ()
    {
	XmlDocument doc = ownerDocument;

	if (doc == null && this instanceof XmlDocument)
	    doc = (XmlDocument) this;
	if (doc != null)
	    doc.mutationCount++;
    }

    //
    // When fragments are appended/inserted/replaced, their entire
    // contents get moved and the fragment becomes empty.
    //
    private void consumeFragment (Node fragment, Node before)
    throws DOMException
    {
	ParentNode	frag = (ParentNode) fragment;
	Node		temp;

	// don't start insertions we can't complete
	for (int i = 0; (temp = frag.item (i)) != null; i++) {
	    checkNotAncestor (temp);
	    checkChildType (temp.getNodeType ());
	}

	while ((temp = frag.item (0)) != null) 
	    insertBefore (temp, before);
    }

    /**
     * <b>DOM:</b>  Appends the child to the set of this node's children.
     * The new child must belong to this document.  
     * 
     * @param newChild the new child to be appended
     */
    public Node appendChild (Node newChild)
    throws DOMException
    {
	NodeBase	child;

	if (readonly)
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);
	child = checkDocument (newChild);

	if (newChild.getNodeType () == DOCUMENT_FRAGMENT_NODE) {
	    consumeFragment (newChild, null);
	    return newChild;
	}

	checkNotAncestor (newChild);
	checkChildType (child.getNodeType ());

	// this is the only place this vector needs allocating,
	// though it may also need to be grown in insertBefore.
	// most elements have very few children
	if (children == null)
	    children = new NodeBase [3];
	else if (children.length == length) {
	    NodeBase temp [] = new NodeBase [length * 2];
            System.arraycopy (children, 0, temp, 0, length);
	    children = temp;
	}

	child.setParentNode (this, length);
	children [length++] = child;
	mutated ();
	return child;
    }


    /**
     * <b>DOM:</b> Inserts the new child before the specified child, which
     * if null indicates appending the new child to the current set of
     * children.  The new child must belong to this particular document.
     * If the newChild is already in the tree, it is first removed.
     *
     * @param newChild the new child to be inserted
     * @param refChild node before which newChild is to be inserted
     */
    public Node insertBefore (Node newChild, Node refChild)
    throws DOMException
    {
	NodeBase	child;

	if (readonly)
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);
	if (refChild == null) 
	    return appendChild (newChild);
	if (length == 0)
	    throw new DomEx (DomEx.NOT_FOUND_ERR);

	child = checkDocument (newChild);

	if (newChild.getNodeType () == DOCUMENT_FRAGMENT_NODE) {
	    consumeFragment (newChild, refChild);
	    return newChild;
	}

	checkNotAncestor (newChild);
	checkChildType (newChild.getNodeType ());

        // If the newChild is already in the tree, it is first removed
        for (int i = 0; i < length; i++) {
            if (children[i] == newChild) {
                removeChild(newChild);
                break;
            }
        }

	// grow array if needed
	if (children.length == length) {
	    NodeBase temp [] = new NodeBase [length * 2];
            System.arraycopy (children, 0, temp, 0, length);
	    children = temp;
	}

	for (int i = 0; i < length; i++) {
	    if (children [i] != refChild)
		continue;
	    child.setParentNode (this, i);
            System.arraycopy (children, i, children, i + 1, length - i);
	    children [i] = child;
	    length++;
	    mutated ();
	    return newChild;
	}

	throw new DomEx (DomEx.NOT_FOUND_ERR);
    }

    /**
     * <b>DOM:</b> Replaces the specified child with the new node,
     * returning the original child or throwing an exception.  The new
     * child must belong to this particular document.  If the newChild is
     * already in the tree, it is first removed.
     *
     * @param newChild the new child to be inserted
     * @param refChild node which is to be replaced
     */
    public Node replaceChild (Node newChild, Node refChild)
    throws DOMException
    {
	NodeBase	child;

	if (readonly)
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);
	if (newChild == null || refChild == null)
	    throw new DomEx (DomEx.HIERARCHY_REQUEST_ERR);
	if (children == null)
	    throw new DomEx (DomEx.NOT_FOUND_ERR);

	child = checkDocument (newChild);

	if (newChild.getNodeType () == DOCUMENT_FRAGMENT_NODE) {
	    consumeFragment (newChild, refChild);
	    return removeChild (refChild);
	}

	checkNotAncestor (newChild);
	checkChildType (newChild.getNodeType ());

        // If the newChild is already in the tree, it is first removed
	for (int i = 0; i < length; i++) {
            if (children[i] == newChild) {
                removeChild(newChild);
                break;
            }
        }

	for (int i = 0; i < length; i++) {
	    if (children [i] != refChild)
		continue;
	    child.setParentNode (this, i);
	    children [i] = child;
	    ((NodeBase) refChild).setParentNode (null, -1);
	    mutated ();
	    return refChild;
	}
	throw new DomEx (DomEx.NOT_FOUND_ERR);
    }


    /**
     * <b>DOM:</b>  removes child if present, returning argument.
     *
     * @param oldChild the node which is to be removed
     */
    public Node removeChild (Node oldChild)
    throws DOMException
    {
	NodeBase	child;

	if (readonly)
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);
	if (!(oldChild instanceof NodeBase))
	    throw new DomEx (DomEx.NOT_FOUND_ERR);
	child = (NodeBase) oldChild;
	for (int i = 0; i < length; i++) {
	    if (children [i] != child)
		continue;
	    if ((i + 1) != length)
		System.arraycopy (children, i + 1, children, i,
		    (length - 1) - i);
	    length--;
	    children [length] = null;
	    child.setParentNode (null, -1);
	    mutated ();
	    return oldChild;
	}
	throw new DomEx (DomEx.NOT_FOUND_ERR);
    }


    /**
     * <b>DOM:</b>  Returns a "live" list view of the elements below this
     * one which have the specified tag name.  Because this is "live", this
     * API is dangerous -- indices are not stable in the face of most tree
     * updates.  Use a TreeWalker instead.
     *
     * @param tagname the tag name to show; or "*" for all elements.
     * @return list of such elements
     */
    public NodeList getElementsByTagName (String tagname)
    {
	if ("*".equals (tagname))
	    tagname = null;
	return new TagList (tagname); 
    }

    /**
     * @since DOM Level 2
     */
    public NodeList getElementsByTagNameNS(String namespaceURI,
                                           String localName) {
	if ("*".equals(namespaceURI)) {
	    namespaceURI = null;
        }
	if ("*".equals(localName)) {
	    localName = null;
        }
	return new TagListNS(namespaceURI, localName); 
    }

    //
    // Slightly optimized to track document mutation count.  For now
    // we assume that a 32 bit counter won't wrap around, and that
    // there's no point in caching list length.
    //
    class TagList implements NodeList {
	protected String        tag;

	protected int		lastMutationCount;
	protected int		lastIndex;
	protected TreeWalker	lastWalker;

	protected int getLastMutationCount() {
	    XmlDocument doc = (XmlDocument) getOwnerDocument ();
	    return (doc == null) ? 0 : doc.mutationCount;
	}

	TagList (String tag) { this.tag = tag; }

	public Node	item (int i)
	{
	    if (i < 0)
		return null;

	    int temp = getLastMutationCount ();

	    // Can we try to reuse the last walker?
	    if (lastWalker != null) {
		if (i < lastIndex || temp != lastMutationCount)
		    lastWalker = null;
	    }

	    // if not, get a new one ...
	    if (lastWalker == null) {
		lastWalker = new TreeWalker (ParentNode.this);
		lastIndex = -1;
		lastMutationCount = temp;
	    }

	    if (i == lastIndex)
		return lastWalker.getCurrent ();

	    Node	node = null;

	    while (i > lastIndex
                   && (node = lastWalker.getNextElement (tag)) != null) {
		lastIndex++;
            }

            // If we walk off the end of the list, throw away lastWalker
            if (node == null) {
                lastWalker = null;
            }
	    return node;
	}

	public int	getLength ()
	{
	    TreeWalker	walker = new TreeWalker (ParentNode.this);
	    Node	node = null;
	    int		retval;

	    for (retval = 0;
		    (node = walker.getNextElement (tag)) != null;
		    retval++)
		continue;
	    return retval;
	}
    }

    // Namespace version
    // XXX Ugly: much code in common with superclass
    class TagListNS extends TagList {
	private String		namespaceURI;

	TagListNS(String namespaceURI, String localName) {
            // XXX Use the super.tag field as a localName
            super(localName);
            this.namespaceURI = namespaceURI;
        }

	public Node item(int i)	{
	    if (i < 0) {
		return null;
            }

	    int temp = getLastMutationCount();

	    // Can we try to reuse the last walker?
	    if (lastWalker != null) {
		if (i < lastIndex || temp != lastMutationCount) {
		    lastWalker = null;
                }
	    }

	    // if not, get a new one ...
	    if (lastWalker == null) {
		lastWalker = new TreeWalker(ParentNode.this);
		lastIndex = -1;
		lastMutationCount = temp;
	    }

	    if (i == lastIndex) {
		return lastWalker.getCurrent();
            }

	    Node node = null;

	    while (i > lastIndex
                   && (node = lastWalker.getNextElement(namespaceURI,
                                                        tag)) != null) {
		lastIndex++;
            }

            // If we walk off the end of the list, throw away lastWalker
            if (node == null) {
                lastWalker = null;
            }
	    return node;
	}

	public int getLength() {
	    TreeWalker walker = new TreeWalker(ParentNode.this);
            int count;
	    for (count = 0;
                 walker.getNextElement(namespaceURI, tag) != null;
                 count++) {
                // noop
            }
	    return count;
	}
    }


    /**
     * Returns the index of the node in the list of children, such
     * that <em>item()</em> will return that child.
     *
     * @param maybeChild the node which may be a child of this one
     * @return the index of the node in the set of children, or
     *	else -1 if that node is not a child
     */
    final public int getIndexOf (Node maybeChild)
    {
	for (int i = 0; i < length; i++)
	    if (children [i] == maybeChild)
		return i;
	return -1;
    }


    /**
     * @since DOM Level 2
     * In DOM2, normalize() was generalized and got moved to Node.
     *
     * XXX Comments below are old:
     * <b>DOM2:</b> Merges all adjacent Text nodes in the tree rooted by this
     * element.  Avoid using this on large blocks of text not separated
     * by markup such as elements or processing instructions, since it
     * can require arbitrarily large blocks of contiguous memory.
     *
     * XXX The following extension breaks a DOM conformance test so the
     * code has been modified to not behave as described:
     * <P> As a compatible extension to DOM, this normalizes treatment
     * of whitespace except when the <em>xml:space='preserve'</em>
     * attribute value applies to a node.  All whitespace is normalized
     * to one space.  This ensures that text which is pretty-printed and
     * then reread (and normalized) retains the same content. </P>
     */
    public void normalize() {
	boolean	preserve = false;
	boolean	knowPreserve = false;

	if (readonly) {
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);
        }

	for (int i = 0; true; i++) {
            Node node = item(i);
	    if (node == null) {
		break;
            }
	    switch (node.getNodeType()) {
            case ELEMENT_NODE:
		((Element)node).normalize ();
		continue;
                // case CDATA_SECTION_NODE:
            case TEXT_NODE: {
                Node node2 = item(i + 1);
                if (node2 == null || node2.getNodeType() != TEXT_NODE) {
                    if (false) {
                        // The following code breaks DOM conformance so this
                        // feature is turned off.

                    // See if xml:space='preserve' is set...
                    if (!knowPreserve) {
                        preserve = "preserve".equals(
                            getInheritedAttribute("xml:space"));
                        knowPreserve = true;
                    }

                    // ... and if not, normalize whitespace
                    if (!preserve) {
                        char[] buf = ((TextNode)node).data;

                        // XXX this isn't supposed to happen
                        if (buf == null || buf.length == 0) {
                            removeChild(node);
                            i--;
                            continue;
                        }

                        int current = removeWhiteSpaces(buf);

                        // compact if it shrank
                        if (current != buf.length) {
                            char[] tmp = new char[current];
                            System.arraycopy(buf, 0, tmp, 0, current);
                            ((TextNode)node).data = tmp;
                        }
                    }
                    }
                    continue;
                }
                ((TextNode) node).joinNextText();
                i--;
                continue;
            }
            default:
		continue;
	    }
	}
    }

    /*
     * removes white leading, trailing and extra white spaces from the buffer.
     * returns the size of the new buf after the white spaces are removed.
     */
    public int removeWhiteSpaces(char[] buf) {
        int current = 0;
        int j = 0;

        // copy to beginning, normalizing whitespace
        // (including leading, trailing) to one space
        while (j < buf.length) {
            boolean sawSpace = false;
            char c = buf[j++];

            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                c = ' ';
                sawSpace = true;
            }
            buf[current++] = c;
            if (sawSpace) {
                while (j < buf.length) {
                    c = buf[j];
                    if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                        j++;
                        continue;
                    } else {
                        break;
                    }
                }
            }
        }
        return current;
    }
}
