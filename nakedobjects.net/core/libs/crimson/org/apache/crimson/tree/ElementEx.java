/*
 * $Id: ElementEx.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
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

import org.w3c.dom.Attr;
import org.w3c.dom.Element;


/**
 * This extends the DOM Element interface with additional features.  An ID
 * attribute may be visible, and applications may request that memory usage
 * be reduced.
 *
 * <P> There is also support for a single strongly associated object,
 * permitting trees of XML objects to be coupled to other frameworks
 * without requiring either subclassing or external tables to manage
 * such associations.  Such techniques will be required in some cases,
 * perhaps in conjunction with this <em>userObject</em>.
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
public interface ElementEx extends Element, NodeEx {
    /**
     * Returns the name of the attribute declared to hold the element's ID,
     * or null if no such declaration is known.  This is normally declared
     * in the Document Type Declaration (DTD).  Parsers are not required to
     * parse DTDs, and document trees constructed without a parser may not
     * have access to the DTD, so such declarations may often not be known.
     *
     * <P> ID attributes are used within XML documents to support links
     * using IDREF and IDREFS attributes.  They are also used in current
     * drafts of XPointer and XSL specifications.
     *
     * @return the name of the ID attribute
     */
    public String	getIdAttributeName ();

    /**
     * Returns the object associated with this element.  In cases where
     * more than one such object must be so associated, the association
     * must be maintained externally.
     */
    public Object	getUserObject ();

    /**
     * Assigns an object to be associated with this element.
     */
    public void		setUserObject (Object obj);

    /**
     * Requests that the element minimize the amount of space it uses,
     * to conserve memory.  Children are not affected.
     */
    public void		trimToSize ();
}
