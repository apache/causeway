/*
 * $Id: CDataNode.java,v 1.1 2005/04/11 23:47:43 rcm Exp $
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
 * Node representing XML CDATA text, which is just like other text except
 * for its delimiters (<em>&lt;[CDATA[</em>text<em>]]&gt;</em>).  CDATA
 * is used to embed markup, program source code, and other kinds of text
 * that use XML markup delimiters (<em>&lt;</em> and <em>&amp;</em>) for
 * their own nefarious purposes.
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
//public
class CDataNode extends TextNode implements CDATASection
{
    /**
     * Constructs a CDATA text object with no text.
     */
    public CDataNode () { }

        
    /**
     * Constructs CDATA text object by copying text from the input buffer.
     */
    public CDataNode (char buf [], int offset, int len)
    {
	super (buf, offset, len);
    }

    /**
     * Constructs a CDATA text object by copying text from the string.
     */
    public CDataNode (String s)
    {
	super (s);
    }

    /**
     * Writes the text, breaking this into multiple CDATA sections
     * if necessary to escape <em>[[&lt;</em> in the data.
     */
    public void writeXml (XmlWriteContext context) throws IOException
    {
	Writer	out = context.getWriter ();
	out.write ("<![CDATA[");
	for (int i = 0; i < data.length; i++) {
	    char c = data [i];

	    // embedded "]]>" needs to be split into adjacent
	    // CDATA blocks ... can be split at either point
	    if (c == ']') {
		if ((i + 2) < data.length
			&& data [i + 1] == ']'
			&& data [i + 2] == '>') {
		    out.write ("]]]><![CDATA[");
		    continue;
		}
	    }
	    out.write (c);
	}
	out.write ("]]>");
    }

    // DOM support

    /** DOM: Returns the CDATA_SECTION_NODE node type constant. */
    public short getNodeType () { return CDATA_SECTION_NODE; }

    /** Returns a new CDATA section with the same content as this. */
    public Node cloneNode (boolean deep)
    {
    	CDataNode retval = new CDataNode (data, 0, data.length);
	retval.setOwnerDocument ((XmlDocument)this.getOwnerDocument ());
	return retval;
    }

    /** Returns the string "#cdata-section".*/
    public String getNodeName () { return "#cdata-section"; }
}
