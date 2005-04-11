/*
 * $Id: NodeEx.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
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

import org.w3c.dom.Node;


/**
 * This interface defines accessors to inherited attributes of nodes,
 * and provides support for using XML Namespaces.
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
public interface NodeEx extends Node, XmlWritable
{
    /**
     * Returns the value of a given attribute, tracing up through
     * ancestors if needed.  In the XML standard, two attributes are
     * inherited:  <em>xml:lang</em> and <em>xml:space</em>.  A very
     * similar mechanism is involved with Cascading Style Sheets (CSS).
     * XML Namespaces also use inheritance, using attributes with
     * names like <em>xmlns:foo</em> to declare namespace prefixes.
     *
     * @param name The name of the attribute to be found.  Colons in
     *	this are ignored.
     * @return the value of the identified attribute, or null if no
     *	such attribute is found.
     */
    public String getInheritedAttribute (String name);

    /**
     * Returns the language id (value of <code>xml:lang</code>
     * attribute) applicable to this node, if known.  Traces up
     * through ancestors as needed.
     * @return the value of the <em>xml:lang</em> attribute, or
     *  null if no such attribute is found.
     */
    public String getLanguage ();

    /**
     * Returns the index of the node in the list of children, such
     * that <em>item()</em> will return that child.
     *
     * @param maybeChild the node which may be a child of this one
     * @return the index of the node in the set of children, or
     *	else -1 if that node is not a child of this node.
     */
    public int getIndexOf (Node maybeChild);

    /**
     * Sets the node to be readonly; applies recursively to the children
     * of this node if the parameter is true.
     *
     * @param deep If <code> true </code> recursively set the nodes in the
     * subtree under the current node to be read only.
     * If <code> false </code> then set only the current node to be
     * readonly
     */
    public void setReadonly (boolean deep);

    /**
     * MethodInfo to allow easy determination of whether a node is read only.
     *
     * @return <code> true </code> if the node is read only
     */
    public boolean isReadonly ();
}
