/*
 * $Id: ValidatingParser.java,v 1.1 2005/04/11 23:47:43 rcm Exp $
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

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.xml.sax.HandlerBase;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.apache.crimson.util.XmlNames;


/**
 * This parser tests XML documents against the validity constraints
 * specified in the XML 1.0 specification as it parses them.  It
 * reports violations of those constraints using the standard SAX API.
 *
 * <P><em>This parser should be configured to use an <code>ErrorHandler</code>
 * that reject documents with validity errors, otherwise they will be accepted
 * despite errors.</em>  The default error handling, as specified by SAX,
 * ignores all validity errors.  The simplest way to have validity errors
 * have a useful effect is to pass a boolean <em>true</em> value to
 * the parser's constructor.
 *
 * <P> Note that most validity checks are performed during parsing by
 * the base class, for efficiency.  They're disabled by default in
 * that class, and enabled by the constructor in this class.
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
public class ValidatingParser extends Parser2
{
    private SimpleHashtable	ids = new SimpleHashtable ();

    /** Constructs a SAX parser object. */
    public ValidatingParser ()
    {
	setIsValidating (true);
    }

    /**
     * Constructs a SAX parser object, optionally assigning the error
     * handler to report exceptions on recoverable errors (which include
     * all validity errors) as well as fatal errors.
     *
     * @param rejectValidityErrors When true, the parser will use an
     *	error handler which throws exceptions on recoverable errors.
     *	Otherwise it uses the default SAX error handler, which ignores
     *	such errors.
     */
    public ValidatingParser (boolean rejectValidityErrors)
    {
	this ();
	if (rejectValidityErrors)
	    setErrorHandler (new HandlerBase () {
		public void error (SAXParseException x)
		    throws SAXException
		    { throw x; }
		});
    }

    // REMINDER:  validation errors are not fatal, so code flow
    // must continue correctly if error() returns.


    // package private ... overrides base class MethodInfo
    void afterRoot () throws SAXException
    {
	// Make sure all IDREFs match declared ID attributes.  We scan
	// after the document element is parsed, since XML allows forward
	// references, and only now can we know if they're all resolved.

	for (Enumeration e = ids.keys ();
		e.hasMoreElements ();
		) {
	    String id = (String) e.nextElement ();
	    Boolean value = (Boolean) ids.get (id);
	    if (Boolean.FALSE == value)
		error ("V-024", new Object [] { id });
	}
    }

    // package private ... overrides base class MethodInfo
    void afterDocument ()
    {
	ids.clear ();
    }

    // package private ... overrides base class MethodInfo
    void validateAttributeSyntax (AttributeDecl attr, String value)
    throws SAXException
    {
	// ID, IDREF(S) ... values are Names
	if (AttributeDecl.ID == attr.type) {
	    if (!XmlNames.isName (value))
		error ("V-025", new Object [] { value });

	    Boolean		b = (Boolean) ids.getNonInterned (value);
	    if (b == null || b.equals (Boolean.FALSE))
		ids.put (value.intern (), Boolean.TRUE);
	    else
		error ("V-026", new Object [] { value });

	} else if (AttributeDecl.IDREF == attr.type) {
	    if (!XmlNames.isName (value))
		error ("V-027", new Object [] { value });

	    Boolean		b = (Boolean) ids.getNonInterned (value);
	    if (b == null)
		ids.put (value.intern (), Boolean.FALSE);

	} else if (AttributeDecl.IDREFS == attr.type) {
	    StringTokenizer	tokenizer = new StringTokenizer (value);
	    Boolean		b;
	    boolean		sawValue = false;

	    while (tokenizer.hasMoreTokens ()) {
		value = tokenizer.nextToken ();
		if (!XmlNames.isName (value))
		    error ("V-027", new Object [] { value });
		b = (Boolean) ids.getNonInterned (value);
		if (b == null)
		    ids.put (value.intern (), Boolean.FALSE);
		sawValue = true;
	    }
	    if (!sawValue)
		error ("V-039", null);


	// NMTOKEN(S) ... values are Nmtoken(s)
	} else if (AttributeDecl.NMTOKEN == attr.type) {
	    if (!XmlNames.isNmtoken (value))
		error ("V-028", new Object [] { value });

	} else if (AttributeDecl.NMTOKENS == attr.type) {
	    StringTokenizer	tokenizer = new StringTokenizer (value);
	    boolean		sawValue = false;

	    while (tokenizer.hasMoreTokens ()) {
		value = tokenizer.nextToken ();
		if (!XmlNames.isNmtoken (value))
		    error ("V-028", new Object [] { value });
		sawValue = true;
	    }
	    if (!sawValue)
		error ("V-032", null);

	// ENUMERATION ... values match one of the tokens
	} else if (AttributeDecl.ENUMERATION == attr.type) {
	    for (int i = 0; i < attr.values.length; i++)
		if (value.equals (attr.values [i]))
		    return;
	    error ("V-029", new Object [] { value });

	// NOTATION values match a notation name
	} else if (AttributeDecl.NOTATION == attr.type) {
	    //
	    // XXX XML 1.0 spec should probably list references to
	    // externally defined notations in standalone docs as
	    // validity errors.  Ditto externally defined unparsed
	    // entities; neither should show up in attributes, else
	    // one needs to read the external declarations in order
	    // to make sense of the document (exactly what tagging
	    // a doc as "standalone" intends you won't need to do).
	    //
	    for (int i = 0; i < attr.values.length; i++)
		if (value.equals (attr.values [i]))
		    return;
	    error ("V-030", new Object [] { value });

	// ENTITY(IES) values match an unparsed entity(ies)
	} else if (AttributeDecl.ENTITY == attr.type) {
	    // see note above re standalone 
	    if (!isUnparsedEntity (value))
		error ("V-031", new Object [] { value });

	} else if (AttributeDecl.ENTITIES == attr.type) {
	    StringTokenizer	tokenizer = new StringTokenizer (value);
	    boolean		sawValue = false;

	    while (tokenizer.hasMoreTokens ()) {
		value = tokenizer.nextToken ();
		// see note above re standalone 
		if (!isUnparsedEntity (value))
		    error ("V-031", new Object [] { value });
		sawValue = true;
	    }
	    if (!sawValue)
		error ("V-040", null);

	} else if (AttributeDecl.CDATA != attr.type)
	    throw new InternalError (attr.type);
    }


    // package private ... overrides base class MethodInfo
    ContentModel newContentModel (String tag)
    {
	return new ContentModel (tag);
    }

    // package private ... overrides base class MethodInfo
    ContentModel newContentModel (char type, ContentModel next)
    {
	return new ContentModel (type, next);
    }

    // package private ... overrides base class MethodInfo
    ElementValidator newValidator (ElementDecl element)
    {
	if (element.validator != null)
	    return element.validator;
	if (element.model != null)
	    return new ChildrenValidator (element);

	//
	// most types of content model have very simple validation
	// algorithms; only "children" needs mutable state.
	//
	if (element.contentType == null || strANY == element.contentType)
	    element.validator = ElementValidator.ANY;
	else if (strEMPTY == element.contentType)
	    element.validator = EMPTY;
	else // (element.contentType.charAt (1) == '#')
	    element.validator = new MixedValidator (element);
	return element.validator;
    }

    private final EmptyValidator EMPTY = new EmptyValidator ();

    // "EMPTY" model allows nothing
    class EmptyValidator extends ElementValidator
    {
	public void consume (String token) throws SAXException
	    { error ("V-033", null); }

	public void text () throws SAXException
	    { error ("V-033", null); }
    }

    // Mixed content models allow text with selected elements
    class MixedValidator extends ElementValidator
    {
	private ElementDecl		element;

	MixedValidator (ElementDecl element)
	    { this.element = element; }

	public void consume (String type) throws SAXException
	{
	    String model = element.contentType;

	    for (int index = 8; 		// skip "(#PCDATA|"
		    (index = model.indexOf (type, index + 1)) >= 9;
		    ) {
		char	c;

		// allow this type name to suffix -- "|xxTYPE"
		if (model.charAt (index -1) != '|')
		    continue;
		c = model.charAt (index + type.length ());
		if (c == '|' || c == ')')
		    return;
		// allow this type name to prefix -- "|TYPExx"
	    }
	    error ("V-034", new Object [] { element.name, type, model });
	}
    }

    class ChildrenValidator extends ElementValidator
    {
	private ContentModelState	state;
	private String			name;

	ChildrenValidator (ElementDecl element)
	{
	    state = new ContentModelState (element.model);
	    name = element.name;
	}

	public void consume (String token) throws SAXException
	{
	    if (state == null)
		error ("V-035", new Object [] { name, token });
	    else try {
		state = state.advance (token);
	    } catch (EndOfInputException e) {
		error ("V-036", new Object [] { name, token });
	    }
	}

	public void text () throws SAXException
	{
	    error ("V-037", new Object [] { name });
	}

	public void done () throws SAXException
	{
	    if (state != null && !state.terminate ())
		error ("V-038", new Object [] { name });
	}
    }

    private boolean isUnparsedEntity (String name)
    {
	Object e = entities.getNonInterned (name);
	if (e == null || !(e instanceof ExternalEntity))
	    return false;
	return ((ExternalEntity)e).notation != null;
    }
}
