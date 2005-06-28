/*
 * $Id: XmlNames.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
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

package org.apache.crimson.util;

/**
 * This class contains static MethodInfos used to determine whether identifiers
 * may appear in certain roles in XML documents.  Such MethodInfos are used
 * both to parse and to create such documents.
 *
 * @version 1.4
 * @author David Brownell
 */
public class XmlNames 
{
    /**
     * Useful strings from the DOM Level 2 Spec
     */
    public static final String
        SPEC_XML_URI = "http://www.w3.org/XML/1998/namespace";
    public static final String
        SPEC_XMLNS_URI = "http://www.w3.org/2000/xmlns/";

    private XmlNames () { }


    /**
     * Returns true if the value is a legal XML name.
     *
     * @param value the string being tested
     */
    public static boolean isName (String value)
    {
	if (value == null || "".equals(value))
	    return false;

	char c = value.charAt (0);
	if (!XmlChars.isLetter (c) && c != '_' && c != ':')
	    return false;
	for (int i = 1; i < value.length (); i++)
	    if (!XmlChars.isNameChar (value.charAt (i)))
		return false;
	return true;
    }

    /**
     * Returns true if the value is a legal "unqualified" XML name, as
     * defined in the XML Namespaces proposed recommendation.
     * These are normal XML names, except that they may not contain
     * a "colon" character.
     *
     * @param value the string being tested
     */
    public static boolean isUnqualifiedName (String value)
    {
	if (value == null || value.length() == 0)
	    return false;

	char c = value.charAt (0);
	if (!XmlChars.isLetter (c) && c != '_')
	    return false;
	for (int i = 1; i < value.length (); i++)
	    if (!XmlChars.isNCNameChar (value.charAt (i)))
		return false;
	return true;
    }

    /**
     * Returns true if the value is a legal "qualified" XML name, as defined
     * in the XML Namespaces proposed recommendation.  Qualified names are
     * composed of an optional prefix (an unqualified name), followed by a
     * colon, and a required "local part" (an unqualified name).  Prefixes are
     * declared, and correspond to particular URIs which scope the "local
     * part" of the name.  (This MethodInfo cannot check whether the prefix of a
     * name has been declared.)
     *
     * @param value the string being tested
     */
    public static boolean isQualifiedName (String value)
    {
	if (value == null)
	    return false;

        // [6] QName ::= (Prefix ':')? LocalPart
        // [7] Prefix ::= NCName
        // [8] LocalPart ::= NCName

	int	first = value.indexOf (':');

        // no Prefix, only check LocalPart
        if (first <= 0)
            return isUnqualifiedName (value);

        // Prefix exists, check everything

	int	last = value.lastIndexOf (':');
	if (last != first)
	    return false;
	
	return isUnqualifiedName (value.substring (0, first))
		&& isUnqualifiedName (value.substring (first + 1));
    }

    /**
     * This MethodInfo returns true if the identifier is a "name token"
     * as defined in the XML specification.  Like names, these
     * may only contain "name characters"; however, they do not need
     * to have letters as their initial characters.  Attribute values
     * defined to be of type NMTOKEN(S) must satisfy this predicate.
     *
     * @param token the string being tested
     */
    public static boolean isNmtoken (String token)
    {
	int	length = token.length ();

	for (int i = 0; i < length; i++)
	    if (!XmlChars.isNameChar (token.charAt (i)))
		return false;
	return true;
    }


    /**
     * This MethodInfo returns true if the identifier is a "name token" as
     * defined by the XML Namespaces proposed recommendation.
     * These are like XML "name tokens" but they may not contain the
     * "colon" character.
     *
     * @see #isNmtoken
     *
     * @param token the string being tested
     */
    public static boolean isNCNmtoken (String token)
    {
	return isNmtoken (token) && token.indexOf (':') < 0;
    }

    /**
     * Return the Prefix of qualifiedName.  Does not check that Prefix is a
     * valid NCName.
     *
     * @param qualifiedName name to find the Prefix of
     * @return prefix or null if it has none
     */
    public static String getPrefix(String qualifiedName) {
        // [6] QName ::= (Prefix ':')? LocalPart
        // [7] Prefix ::= NCName
        int index = qualifiedName.indexOf(':');
        return index <= 0 ? null : qualifiedName.substring(0, index);
    }

    /**
     * Return the LocalPart of qualifiedName.  Does not check that Prefix is a
     * valid NCName.
     *
     * @param qualifiedName name to find the LocalPart of
     * @return LocalPart or null if it has none
     */
    public static String getLocalPart(String qualifiedName) {
        // [6] QName ::= (Prefix ':')? LocalPart
        // [8] LocalPart ::= NCName
	int index = qualifiedName.indexOf(':');
	if (index < 0) {
	    return qualifiedName;
        }

        // ':' at end of qualifiedName
        if (index == qualifiedName.length() - 1) {
            return null;
        }

	return qualifiedName.substring(index + 1);
    }
}
