/*
 * $Id: DataNode.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
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
 * Node representing XML character data, such as text (including
 * CDATA sections and comments).
 *
 * <P> At this time this uses an unsophisticated representation
 * which is not suited for complex editing operations.
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
// public
abstract class DataNode extends NodeBase implements CharacterData
{
    // package private
    char        	data [];
    static NodeListImpl childNodes = new NodeListImpl ();

    /*
     * Constructs a data object with no text and unattached
     * to any document.
     */
    DataNode () { }

        
    /*
     * Constructs data node by copying text from the input buffer.
     */
    DataNode (char buf [], int offset, int len)
    {
        data = new char [len];
        System.arraycopy (buf, offset, data, 0, len);
    }

    /*
     * Constructs a data node by copying text from the string.
     */
    DataNode (String s)
    {
	if (s != null) {
	    data = new char [s.length ()];
	    s.getChars (0, data.length, data, 0);
	} else
	    data = new char [0];
    }

    /**
     * Returns the text of the node. This may be modified by
     * the caller only if the length remains unchanged.
     */
    public char [] getText () { return data; }

    /**
     * Assigns the text of the node.  The buffer is consumed; the
     * caller should make copies accordingly.
     */
    public void setText (char buf []) { data = buf; }

    /**
     * Returns the contents of this text as a String.
     */
    public String toString () {
        if (data != null) {
            return new String(data);
        } else {
            return null;
        }
    }


    // DOM support

    /** DOM: Returns the text data as a string. */
    public String getData () { return toString (); }

    /** DOM: Assigns the text data. */
    public void setData (String data) { 
        if (isReadonly ())
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);

	if (data == null) {
	    setText (new char [0]);
	} else {
	    setText (data.toCharArray ()); 
	}
    }

    /** DOM:  Returns the length of the node's data. */
    public int getLength ()
	{ return data == null ? 0 : data.length; }

    /**
     * DOM:  Returns the specified substring of the data in this node.
     */
    public String substringData (int offset, int count)
    throws DOMException
    {
	if (offset < 0 || offset > data.length || count < 0)
	    throw new DomEx (DOMException.INDEX_SIZE_ERR);

	count = Math.min (count, data.length - offset);
	return new String (data, offset, count);
    }

    /**
     * DOM: Appends the string to the existing stored data.
     */
    public void appendData (String newData)
    {
        if (isReadonly ())
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);
	int	length = newData.length ();
	char	tmp [] = new char [length + data.length];

	System.arraycopy (data, 0, tmp, 0, data.length);
	newData.getChars (0, length, tmp, data.length);
	data = tmp;
    }

    /**
     * DOM: Inserts the given data into the existing stored data
     * at the specified offset.
     */
    public void insertData (int offset, String newData)
    throws DOMException
    {
        if (isReadonly ())
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);

	if (offset < 0 || offset > data.length)
	    throw new DomEx (DOMException.INDEX_SIZE_ERR);

	int	length = newData.length ();
	char	tmp [] = new char [length + data.length];

	System.arraycopy (data, 0, tmp, 0, offset);
	newData.getChars (0, length, tmp, offset);
	System.arraycopy (data, offset,
	    tmp, offset + length,
	    data.length - offset);
	data = tmp;
    }

    /**
     * DOM: Removes a range of characters from the text.
     */
    public void deleteData (int offset, int count)
    throws DOMException
    {
	char	tmp [];

        if (isReadonly ())
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);

	if (offset < 0 || offset >= data.length || count < 0)
	    throw new DomEx (DOMException.INDEX_SIZE_ERR);
	count = Math.min (count, data.length - offset);

	tmp = new char [data.length - count];
	System.arraycopy (data, 0, tmp, 0, offset);
	System.arraycopy (data, offset + count, tmp, offset,
			  tmp.length - offset);
	data = tmp;
    }

    /**
     * DOM: Replaces <em>count</em> characters starting at the specified
     * <em>offset</em> in the data with the characters from the
     * specified <em>arg</em>.
     */
    public void replaceData (int offset, int count, String arg)
    throws DOMException
    {
        if (isReadonly ())
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);

	if (offset < 0 || offset >= data.length || count < 0)
	    throw new DomEx (DOMException.INDEX_SIZE_ERR);

	if ((offset + count) >= data.length) {
	    deleteData (offset, count);
	    appendData (arg);
	} else if (arg.length () == count) {
	    arg.getChars (0, (arg.length ()), data, offset);
	} else {
	    char tmp [] = new char [data.length + (arg.length () - count)];
	    System.arraycopy (data, 0, tmp, 0, offset);
	    arg.getChars (0, (arg.length ()), tmp, offset);
	    System.arraycopy (data, (offset + count), tmp, (offset +
	    		      arg.length ()), data.length -(offset + count));
	    data = tmp;
	}
    }

    /**
     * DOM:  Returns the children of this node.
     */
    public NodeList getChildNodes () { return childNodes; }

    /**
     * DOM:  Returns the node's character data.
     */
    public String getNodeValue () { return getData (); }

    /**
     * DOM:  Assigns the node's character data.
     */
    public void setNodeValue (String value) { setData (value); }

    static final class NodeListImpl implements NodeList {
   	public Node item (int i) { return null;}
	public int getLength () { return 0;}
    }
}
