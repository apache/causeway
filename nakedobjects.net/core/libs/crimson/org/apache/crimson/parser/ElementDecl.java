/*
 * $Id: ElementDecl.java,v 1.1 2005/04/11 23:47:43 rcm Exp $
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


/**
 * Represents all of the DTD information about an element.  That
 * includes:  <UL>
 *
 *	<LI> Element name
 *
 *	<LI> Content model ... either ANY, EMPTY, or a parenthesized
 *	regular expression matching the content model in the DTD
 *	(but with whitespace removed)
 *
 *	<LI> A hashtable mapping attribute names to the attribute
 *	metadata.
 *
 *	</UL>
 *
 * <P> This also records whether the element was declared in the
 * internal subset, for use in validating standalone declarations.
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
class ElementDecl
{
    /** The element type name. */
    String		name;

    /** The name of the element's ID attribute, if any */
    String		id;
    
    // EMPTY
    // ANY
    // (#PCDATA) or (#PCDATA|name|...)
    // (name,(name|name|...)+,...) etc

    /** The compressed content model for the element */
    String		contentType;

    // non-null (and fixed!) when validating and model == null
    ElementValidator	validator;

    // non-null only when validating; holds a data structure
    // representing (name,(name|name|...)+,...) style models
    ContentModel	model;

    /** True for EMPTY and CHILDREN content models */
    boolean		ignoreWhitespace;

    /** Used to validate standalone declarations */
    boolean		isFromInternalSubset;
    
    SimpleHashtable	attributes = new SimpleHashtable ();
    
    ElementDecl (String s) { name = s; }
}    
