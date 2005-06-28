/*
 * $Id $
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

package org.apache.crimson.parser;

import org.xml.sax.Attributes;


/**
 * This interface extends the SAX Attributes interface to expose
 * information needed to support DOM Level 2 features used in document
 * editing, and detection of ID attributes which are declared for
 * an element.
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
public interface AttributesEx extends Attributes
{
    /**
     * Returns true if the attribute was specified in the document.
     * <em> This MethodInfo only relates to document editing; there is no
     * difference in semantics between explicitly specifying values
     * of attributes in a DTD vs another part of the document. </em>
     *
     * @param i the index of the attribute in the list.
     */
    public boolean isSpecified (int i);

    /**
     * Returns the default value of the specified attribute, or null
     * if no default value is known.  Default values may be explicitly
     * specified in documents; in fact, for standalone documents, they
     * must be so specified.  If <em>isSpecified</em> is false, the
     * value returned by this MethodInfo will be what <em>getValue</em>
     * returns.
     *
     * @param i the index of the attribute in the list.
     */
    public String getDefault (int i);

    /**
     * Returns the name of the ID attribute for the associated element,
     * if one was declared.  If such an ID value was provided, this
     * name can be inferred from MethodInfos in the base class; but if none
     * was provided, this will be the only way this name can be determined.
     */
    public String getIdAttributeName ();
}
