/*
 * $Id: PINode.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
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
 * Node representing an XML processing instruction.
 *
 * <P> <em>Functionality to restore in some other way: </em>
 *
 * As a convenience function, the instruction data may optionally
 * be parsed as element attributes are parsed.  There is no requirement
 * to use this particular syntax for instruction data.
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
final
//public
class PINode extends NodeBase implements ProcessingInstruction
{
    private String      target;
    private char	data [];
        
    /** Constructs a processing instruction node. */
    public PINode () { }

    /** Constructs a processing instruction node. */
    public PINode (String target, String text)
    {
	data = text.toCharArray ();
        this.target = target;
    }

    PINode (String target, char buf [], int offset, int len)
    {
	data = new char [len];
	System.arraycopy (buf, offset, data, 0, len);
        this.target = target;
    }

    /** DOM:  Returns the PROCESSING_INSTRUCTION_NODE node type. */
    public short getNodeType () { return PROCESSING_INSTRUCTION_NODE; }

    /** DOM:  Returns the processor the instruction is directed to. */
    public String getTarget () { return target; }

    /** DOM:  Assigns the processor the instruction is directed to. */
    public void setTarget (String target) { this.target = target; }

    /** DOM: Returns the text data as a string. */
    public String getData () { return new String (data); }

    /** DOM: Assigns the text data. */
    public void setData (String data) { 
        if (isReadonly ())
	    throw new DomEx (DomEx.NO_MODIFICATION_ALLOWED_ERR);
    
        this.data = data.toCharArray (); 
    }

    /** DOM: Returns the text data as a string. */
    public String getNodeValue () { return getData (); }

    /** DOM: Assigns the text data. */
    public void setNodeValue (String data) { setData (data); }

    /**
     * Writes the processing instruction as well formed XML text.
     *
     * <P> <em> Doesn't currently check for the <b>?&gt;</b> substrings
     * in PI data, which are illegal </em>
     */
    public void writeXml (XmlWriteContext context) throws IOException
    {
	Writer	out = context.getWriter ();

        out.write ("<?");
        out.write (target);
        if (data != null) {
            out.write (' ');
            out.write (data);
        }
        out.write ("?>");
    }

    /** Returns a new processing instruction with the same content as this. */
    public Node cloneNode (boolean deep) { 
    	PINode retval = new PINode (target, data, 0, data.length); 
	retval.setOwnerDocument ((XmlDocument) this.getOwnerDocument ());
	return retval;
    }

    /** Returns the PI target name. */
    public String getNodeName () { return target; }
}
