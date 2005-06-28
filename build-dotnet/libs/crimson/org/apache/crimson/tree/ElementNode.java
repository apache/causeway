/*
 * $Id: ElementNode.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights 
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

/**
 * A DOM Element that was created with a Level 1 create MethodInfo.  Note that
 * the name ElementNode is maintained for backwards compatibility with
 * element factories which may subclass using this name.
 */
public class ElementNode extends ElementNode2
{
    /**
     * Partially constructs an element; its tag will be assigned by the
     * element factory (or subclass), while attributes and the parent (and
     * implicitly, siblings) will be assigned when it is joined to a DOM
     * document.
     *
     * Element factories are deprecated.  For backwards compatibility only.
     */
    public ElementNode() {
        super(null, null);
    }

    /**
     * Construct an element with a particular XML REC "Name".
     */
    public ElementNode(String name) {
        super(null, name);
    }

    /**
     * Make a clone of this node and return it.  Used for cloneNode().
     */
    ElementNode2 makeClone() {
        ElementNode2 retval = new ElementNode(qName);
        if (attributes != null) {
            retval.attributes = new AttributeSet(attributes, true);
            retval.attributes.setOwnerElement(this);
        }
        retval.setIdAttributeName(getIdAttributeName());
        retval.setUserObject(getUserObject());
        retval.ownerDocument = ownerDocument;
        return retval;
    }

    /**
     * Assigns the element's name, when the element has been
     * constructed using the default constructor.  For use by
     * element factories potentially by custom subclasses. 
     *
     * @deprecated Element factories are deprecated.  For backwards
     * compatibility only.
     */
    protected void setTag(String t) {
        qName = t;
    }

    /**
     * The namespace prefix of this node, or <code>null</code> if it is
     * unspecified.
     *
     * @since DOM Level 2
     */
    public String getPrefix() {
        // DOM Level 2 specifies this
        return null;
    }

    /**
     * Returns the local part of the qualified name of this node.
     *
     * @since DOM Level 2
     */
    public String getLocalName() {
        // DOM Level 2 specifies this
        return null;
    }
}
