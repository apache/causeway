/*
 * $Id: AttributesExImpl.java,v 1.1 2005/04/11 23:47:43 rcm Exp $
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

import java.util.Vector;

import org.xml.sax.helpers.AttributesImpl;


/**
 * Implementation of the AttributesEx interface which provides additional
 * features to support editor-oriented DOM features: exposing attribute
 * defaulting.  Derived from old AttributeListImpl.java code.
 */
final
class AttributesExImpl extends AttributesImpl implements AttributesEx
{
    // Boolean.TRUE indicates value was specified
    private Vector      specified = new Vector ();

    // non-null value defines default
    private Vector      defaults = new Vector ();

    // ID attribute name, as declared
    private String      idAttributeName;

    AttributesExImpl()
    {
        super();
    }

    /**
     * Clears the attribute list so it has no members
     */
    public void clear()
    {
        super.clear();
        specified.removeAllElements();
        defaults.removeAllElements();
        idAttributeName = null;
    }

    /**
     * Add an attribute to this list
     */
    public void addAttribute(String uri, String localName, String qName,
                             String type, String value, String defaultValue,
                             boolean isSpecified)
    {
        super.addAttribute(uri, localName, qName, type, value);
        defaults.addElement(defaultValue);
        specified.addElement(isSpecified ? Boolean.TRUE : null);
    }

    /**
     * Returns true if the value was specified by a parsed document
     * (by position; no by-name variant).
     */
    public boolean isSpecified(int i) {
        Object value = specified.elementAt(i);
        return value == Boolean.TRUE;
    }

    /**
     * Return the default value of an attribute in this list (by position).
     */
    public String getDefault(int i) {
        try {
            if (i < 0)
                return null;
            return (String) defaults.elementAt(i);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Returns the name of the ID attribute.
     */
    public String getIdAttributeName() {
        return idAttributeName;
    }

    /**
     * Allows parser to set the name of the ID attribute.
     */
    void setIdAttributeName(String name) {
        idAttributeName = name;
    }
}
