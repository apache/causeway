/*
 * $Id: TextNode.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
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

import org.w3c.dom.*;


/**
 * Node representing XML text.
 *
 * <P> Subclasses should not currently attempt to modify the
 * representation of content, but may add new MethodInfos to support
 * more sophisticated access or manipulation of that content.
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
public class TextNode extends DataNode implements Text
{
    //
    // XXX Really want a more flexible representation of text than
    // we have here ... it should be possible to take up only part
    // of a buffer, and in fact to be able to share one with other
    // text nodes.  For readonly trees that will reduce the heap
    // impact; and for editable ones it can facilitate intelligent
    // editing support.  Text seems more expensive than attributes,
    // which is quite odd.
    //

    /**
     * Constructs a text object with no text and unattached
     * to any document.
     */
    public TextNode () { }

        
    /**
     * Constructs text object by copying text from the input buffer.
     */
    public TextNode (char buf [], int offset, int len)
    {
	super (buf, offset, len);
    }

    /**
     * Constructs a text object by copying text from the string.
     */
    public TextNode (String s)
    {
	super (s);
    }

    /**
     * Writes the text, escaping XML metacharacters as needed
     * to let this text be parsed again without change.
     */
    public void writeXml (XmlWriteContext context) throws IOException
    {
	Writer	out = context.getWriter ();
	int	start = 0, last = 0;

	// XXX saw this once -- being paranoid
	if (data == null)
	    { System.err.println ("Null text data??"); return; }

	while (last < data.length) {
	    char c = data [last];

	    //
	    // escape markup delimiters only ... and do bulk
	    // writes wherever possible, for best performance
	    //
	    // note that character data can't have the CDATA
	    // termination "]]>"; escaping ">" suffices, and
	    // doing it very generally helps simple parsers
	    // that may not be quite correct.
	    //
	    if (c == '<') {			// not legal in char data
		out.write (data, start, last - start);
		start = last + 1;
		out.write ("&lt;");
	    } else if (c == '>') {		// see above
		out.write (data, start, last - start);
		start = last + 1;
		out.write ("&gt;");
	    } else if (c == '&') {		// not legal in char data
		out.write (data, start, last - start);
		start = last + 1;
		out.write ("&amp;");
	    }
	    last++;
	}
	out.write (data, start, last - start);
    }

    /**
     * Combines this text node with its next sibling to create a
     * single text node.  If the next node is not text, nothing is
     * done.  This should be used with care, since large spans of
     * text may not be efficient to represent.
     */
    public void joinNextText ()
    {
	Node	next = getNextSibling ();
	char	tmp [], nextText [];

	if (next == null || next.getNodeType () != TEXT_NODE)
	    return;
	getParentNode ().removeChild (next);

	nextText = ((TextNode)next).getText ();
	tmp = new char [data.length + nextText.length];
	System.arraycopy (data, 0, tmp, 0, data.length);
	System.arraycopy (nextText, 0, tmp, data.length, nextText.length);
	data = tmp;
    }


    // DOM support

    /** DOM: Returns the TEXT_NODE node type constant. */
    public short getNodeType () { return TEXT_NODE; }


    /**
     * DOM:  Splits this text node into two, returning the part
     * beginning at <em>offset</em>.  The original node has that 
     * text removed, and the two nodes are siblings in the natural
     * order.
     */
    public Text splitText (int offset)
    throws DOMException
    {
	TextNode	retval;
	char		delta [];

        if (isReadonly ())
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);
	
	try {
		retval = new TextNode (data, offset, data.length - offset);
	}
	catch (ArrayIndexOutOfBoundsException ae) {
		throw new DomEx (DOMException.INDEX_SIZE_ERR);
	}
	catch (NegativeArraySizeException nae) {
		throw new DomEx (DOMException.INDEX_SIZE_ERR);
	}
	getParentNode().insertBefore (retval, getNextSibling ());
	delta = new char [offset];
	System.arraycopy (data, 0, delta, 0, offset);
	data = delta;
	return retval;
    }


    /**
     * DOM: returns a new text node with the same contents as this one.
     */
    public Node cloneNode (boolean deep)
    {
    	TextNode retval = new TextNode (data, 0, data.length);
	retval.setOwnerDocument ((XmlDocument) this.getOwnerDocument ());
	return retval;
    }

    /**
     * DOM:  Returns the string "#text".
     */
    public String getNodeName () { return "#text"; }
}
