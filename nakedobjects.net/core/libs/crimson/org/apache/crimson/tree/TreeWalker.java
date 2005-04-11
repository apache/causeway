/*
 * $Id: TreeWalker.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
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

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * This class implements a preorder depth first walk over the tree rooted
 * at a given DOM node.  The traversal is "live", and terminates either when
 * that given node is reached when climbing back up, or when a null parent
 * node is reached.  It may be restarted via <em>reset()</em>.
 *
 * <P> The way this remains live is to have a "current" node to which the
 * walk is tied.  If the tree is modified, that current node will always
 * still be valid ... even if it is no longer connected to the rest of
 * the document, or if it's reconnected at a different location.  The
 * behavior of tree modifications is specified by DOM, and the interaction
 * with a walker's current node is specified entirely by knowing that only
 * the getNextSibling, getParentNode, and getFirstChild MethodInfos are used
 * for walking the tree.
 *
 * <P> For example, if the current branch is cut off, the walk will stop
 * when it tries to access what were parents or siblings of that node.
 * (That is, the walk will continue over the branch that was cut.)  If
 * that is not the intended behaviour, one must change the "current" branch
 * before cutting ... much like avoiding trimming a branch off a real
 * tree if someone is sitting on it.  The <em>removeCurrent()</em>
 * MethodInfo encapsulates that logic.
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
public class TreeWalker
{
    // yes, this is really a "TreeIterator" but the DOM WG plans to
    // use such names in Level 2 ... so, we avoid it for now.  (note
    // that they've also discussed a "TreeWalker" that's rather more
    // complex than this iterator...)

    private Node	startPoint;
    private Node	current;

    /**
     * Constructs a tree walker starting at the given node.
     */
    public TreeWalker (Node initial)
    {
	if (initial == null) {
	    throw new IllegalArgumentException (XmlDocument.catalog.
				getMessage (Locale.getDefault (), "TW-004"));
	}
	if (!(initial instanceof NodeBase)) { 
	    throw new IllegalArgumentException (XmlDocument.catalog.
				getMessage (Locale.getDefault (), "TW-003"));
	}
	startPoint = current = initial;
    }

    /**
     * Returns the current node.
     */
    public Node getCurrent ()
    {
	return current;
    }

    /**
     * Advances to the next node, and makes that current.  Returns
     * null if there are no more nodes through which to walk,
     * because the initial node was reached or because a null
     * parent was reached.
     *
     * @return the next node (which becomes current), or else null
     */
    public Node getNext ()
    {
	Node	next;

	if (current == null)
	    return null;

	switch (current.getNodeType ()) {
	  case Node.DOCUMENT_FRAGMENT_NODE:
	  case Node.DOCUMENT_NODE:
	  case Node.ELEMENT_NODE:
	  case Node.ENTITY_REFERENCE_NODE:
	    //
	    // For elements that can have children, visit those
	    // children before any siblings (i.e. depth first)
	    // and after visiting this node (i.e. preorder)
	    //
	    next = current.getFirstChild ();
	    if (next != null) {
		current = next;
		return next;
	    }
	    // FALLTHROUGH

	  case Node.ATTRIBUTE_NODE:
	    // NOTE:  attributes "should" have children ...

	  case Node.CDATA_SECTION_NODE:
	  case Node.COMMENT_NODE:
	  case Node.DOCUMENT_TYPE_NODE:
	  case Node.ENTITY_NODE:
	  case Node.NOTATION_NODE:
	  case Node.PROCESSING_INSTRUCTION_NODE:
	  case Node.TEXT_NODE:
	    //
	    // For childless nodes, only look at siblings.  If no
	    // siblings, climb the tree till we get to a spot there
	    // are siblings, or till we terminate our walk.
	    //
	    for (Node here = current;
		    here != null && here != startPoint;
		    here = here.getParentNode ()) {
		next = here.getNextSibling ();
		if (next != null) {
		    current = next;
		    return next;
		}
	    }
	    current = null;
	    return null;
	}
	throw new InternalError (((NodeBase)startPoint).getMessage ("TW-000", 
				new Object [] { Short.toString (current.
					getNodeType ()) }));
    }


    /**
     * Convenience MethodInfo to walk only through elements with the specified
     * tag name.  This just calls getNext() and filters out the nodes which
     * aren't desired.  It returns null when the iteration completes.
     *
     * @param tag the tag to match, or null to indicate all elements
     * @return the next matching element, or else null
     */
    public Element getNextElement (String tag)
    {
	for (Node next = getNext ();
		next != null;
		next = getNext ()) {
	    if (next.getNodeType () == Node.ELEMENT_NODE
		    && (tag == null || tag.equals (next.getNodeName ())))
		return (Element) next;
	}
	current = null;
	return null;
    }

    /**
     * Namespace version
     */
    public Element getNextElement(String nsURI, String localName) {
	for (Node next = getNext(); next != null; next = getNext()) {
	    if (next.getNodeType() == Node.ELEMENT_NODE
                && (nsURI == null || nsURI.equals(next.getNamespaceURI()))
                && (localName == null ||
                    localName.equals(next.getLocalName()))) {
		return (Element)next;
            }
	}
	current = null;
	return null;
    }


    /**
     * Resets the walker to the state in which it was created:  the
     * current node will be the node given to the constructor.  If
     * the tree rooted at that node has been modified from the previous
     * traversal, the sequence of nodes returned by <em>getNext()</em>
     * will differ accordingly.
     */
    public void reset ()
    {
	current = startPoint;
    }


    /**
     * Removes the current node; reassigns the current node to be the
     * next one in the current walk that isn't a child of the (removed)
     * current node, and returns that new current node.  In a loop, this
     * could be used instead of a <em>getNext()</em>.
     *
     * @return the next node (which becomes current), or else null
     * @throws IllegalStateException if the current node is null
     *	or has no parent (it is a Document or DocumentFragment)
     */
    public Node removeCurrent ()
    {
	if (current == null)
	    throw new IllegalStateException (((NodeBase)startPoint).
						getMessage ("TW-001"));

	Node	toRemove = current;
	Node	parent = current.getParentNode ();
	Node	retval = null;

	if (parent == null)
	    throw new IllegalStateException (((NodeBase)startPoint).
	    					getMessage ("TW-002"));
	
	//
	// Don't look at children, just siblings/parents
	//
	for (Node here = current;
		here != null && here != startPoint;
		here = here.getParentNode ()) {
	    retval = here.getNextSibling ();
	    if (retval != null) {
		current = retval;
		break;
	    }
	}
	parent.removeChild (toRemove);
	return retval;
    }
}
