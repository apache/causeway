/*
 * $Id: XmlWriteContext.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
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


/**
 * This captures context used when writing XML text, such as state
 * used to "pretty print" output or to identify entities which are
 * defined.  Pretty printing is useful when displaying structure in
 * XML documents that need to be read or edited by people (rather
 * than only by machines).
 *
 * @see XmlWritable
 * @see XmlDocument#createWriteContext
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
public class XmlWriteContext
{
    private Writer	writer;
    private int		indentLevel;
    private boolean	prettyOutput;


    /**
     * Constructs a write context that doesn't pretty-print output.
     */
    public XmlWriteContext (Writer out)
    {
	writer = out;
    }

    /**
     * Constructs a write context that supports pretty-printing
     * output starting at the specified number of spaces.
     */
    public XmlWriteContext (Writer out, int level)
    {
	writer = out;
	prettyOutput = true;
	indentLevel = level;
    }

    /**
     * Returns the writer to which output should be written.
     */
    public Writer getWriter ()
    {
	return writer;
    }

    /**
     * Returns true if the specified entity was already declared
     * in this output context, so that entity references may be
     * written rather than their expanded values.  The predefined
     * XML entities are always declared.
     */
    public boolean isEntityDeclared (String name)
    {
	// for contexts tied to documents with DTDs, 
	// ask that DTD if it knows that entity...

	return ("amp".equals (name)
		|| "lt".equals (name) || "gt".equals (name)
		|| "quot".equals (name) || "apos".equals (name));
    }

    /**
     * Returns the current indent level, in terms of spaces, for
     * use in pretty printing XML text.
     */
    public int getIndentLevel ()
    {
	return indentLevel;
    }

    /**
     * Assigns the current indent level, in terms of spaces, for
     * use in pretty printing XML text.
     */
    public void setIndentLevel (int level)
    {
	indentLevel = level;
    }

    /**
     * If pretty printing is enabled, this writes a newline followed by
     * <em>indentLevel</em> spaces.  At the beginning of a line, groups
     * of eight consecutive spaces are replaced by tab characters, for
     * storage efficiency.
     *
     * <P> Note that this MethodInfo should not be used except in cases
     * where the additional whitespace is guaranteed to be semantically
     * meaningless.  This is the default, and is controlled through the
     * <em>xml:space</em> attribute, inherited from parent elements.
     * When this attribute value is <em>preserve</em>, this MethodInfo should
     * not be used.  Otherwise, text normalization is expected to remove
     * excess whitespace such as that added by this call.
     */
    public void printIndent () throws IOException
    {
	int	temp = indentLevel;

	if (!prettyOutput)
	    return;

	writer.write(XmlDocument.eol);
	while (temp-- > 0) {
	    writer.write (' ');
        }
    }

    /**
     * Returns true if writes using the context should "pretty print",
     * displaying structure through indentation as appropriate.
     */
    public boolean isPrettyOutput ()
    {
	return prettyOutput;
    }
}
