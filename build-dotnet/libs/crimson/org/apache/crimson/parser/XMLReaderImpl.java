/*
 * $Id: XMLReaderImpl.java,v 1.1 2005/04/11 23:47:43 rcm Exp $
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

import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;

import org.xml.sax.*;
import org.xml.sax.ext.*;


/**
 * This implements the SAX2 XMLReader.
 * @author Rajiv Mordani
 * @author Edwin Goei
 * @version $Revision: 1.1 $
 */

public class XMLReaderImpl implements XMLReader {
    //
    // Internal constants for the sake of convenience: features
    //
    private final static String FEATURES = "http://xml.org/sax/features/";
    private final static String NAMESPACES = FEATURES + "namespaces";
    private final static String NAMESPACE_PREFIXES =
            FEATURES + "namespace-prefixes";
    private final static String STRING_INTERNING =
            FEATURES + "string-interning";
    private final static String VALIDATION = FEATURES + "validation";
    private final static String EXTERNAL_GENERAL =
            FEATURES + "external-general-entities";
    private final static String EXTERNAL_PARAMETER =
            FEATURES + "external-parameter-entities";

    // Features for org.sax.xml.ext.LexicalHandler
    private final static String LEXICAL_PARAMETER_ENTITIES = FEATURES +
            "lexical-handler/parameter-entities";

    // Properties
    private static final String PROPERTIES = "http://xml.org/sax/properties/";
    private final static String LEXICAL_HANDLER =
            PROPERTIES + "lexical-handler";
    private final static String DECLARATION_HANDLER =
            PROPERTIES + "declaration-handler";

    // Features with their default values
    private boolean namespaces = true;
    private boolean prefixes = false;
    private boolean validation = false;

    // Properties
    private LexicalHandler lexicalHandler;
    private DeclHandler declHandler;

    // SAX2 core event handlers
    private ContentHandler contentHandler;
    private DTDHandler dtdHandler;
    private ErrorHandler errorHandler;
    private EntityResolver entityResolver;

    private Parser2 parser;
    private boolean parsing;            // true iff we are currently parsing


    ////////////////////////////////////////////////////////////////////
    // Constructors.
    ////////////////////////////////////////////////////////////////////

   /**
     * Default constructor is explicitly declared here
     */
    public XMLReaderImpl() {
        super();
    }


    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.XMLReader.
    ////////////////////////////////////////////////////////////////////

    /**
     * Check a parser feature.
     *
     * @param name The feature name, as a complete URI.
     * @return The current feature state.
     * @exception org.xml.sax.SAXNotRecognizedException If the feature
     *            name is not known.
     * @exception org.xml.sax.SAXNotSupportedException If querying the
     *            feature state is not supported.
     * @see org.xml.sax.XMLReader#setFeature
     */
    public boolean getFeature(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        if (name.equals(NAMESPACES)) {
            return namespaces;
        } else if (name.equals(NAMESPACE_PREFIXES)) {
            return prefixes;
        } else if (name.equals(VALIDATION)) {
            return validation;
        } else if (name.equals(STRING_INTERNING) ||
                   name.equals(EXTERNAL_GENERAL) ||
                   name.equals(EXTERNAL_PARAMETER)) {
            return true;
        } else if (name.equals(LEXICAL_PARAMETER_ENTITIES)) {
            return false;
        } else {
            throw new SAXNotRecognizedException("Feature: " + name);
        }
    }

    /**
     * Set a feature for the parser.
     *
     * @param name The feature name, as a complete URI.
     * @param state The requested feature state.
     * @exception org.xml.sax.SAXNotRecognizedException If the feature
     *            name is not known.
     * @exception org.xml.sax.SAXNotSupportedException If the feature
     *            state is not supported.
     * @see org.xml.sax.XMLReader#getFeature
     */
    public void setFeature(String name, boolean state)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        if (name.equals(NAMESPACES)) {
            checkNotParsing("feature", name);
            namespaces = state;
            if (!namespaces && !prefixes) {
                prefixes = true;
            }
        } else if (name.equals(NAMESPACE_PREFIXES)) {
            checkNotParsing("feature", name);
            prefixes = state;
            if (!prefixes && !namespaces) {
                namespaces = true;
            }
        } else if (name.equals(VALIDATION)) {
            checkNotParsing("feature", name);
            if (validation != state) {
                parser = null;
            }
            validation = state;
        } else if (name.equals(STRING_INTERNING)) {
            if (state == false) {
                throw new SAXNotSupportedException("Feature: " + name
                                                   + " State: false");
            }
            // else true is OK
        } else if (name.equals(EXTERNAL_GENERAL) ||
                   name.equals(EXTERNAL_PARAMETER) ||
                   name.equals(LEXICAL_PARAMETER_ENTITIES)) {
            throw new SAXNotSupportedException("Feature: " + name);
        } else {
            throw new SAXNotRecognizedException("Feature: " + name);
        }
    }

    /**
     * Get a parser property.
     *
     * @param name The property name.
     * @return The property value.
     * @exception org.xml.sax.SAXNotRecognizedException If the feature
     *            name is not known.
     * @exception org.xml.sax.SAXNotSupportedException If the feature
     *            state is not supported.
     * @see org.xml.sax.XMLReader#getProperty
     */
    public Object getProperty(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        if (name.equals(LEXICAL_HANDLER)) {
            return lexicalHandler;
        } else if (name.equals(DECLARATION_HANDLER)) {
            return declHandler;
        } else {
            throw new SAXNotRecognizedException("Property: " + name);
        }
    }

    /**
     * Set a parser property.
     *
     * @param name The property name.
     * @param value The property value.
     * @exception org.xml.sax.SAXNotRecognizedException If the feature
     *            name is not known.
     * @exception org.xml.sax.SAXNotSupportedException If the feature
     *            state is not supported.
     * @see org.xml.sax.XMLReader#getProperty
     */
    public void setProperty(String name, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        String detail = "Property: " + name;
        if (name.equals(LEXICAL_HANDLER)) {
            if (!(value instanceof LexicalHandler)) {
                throw new SAXNotSupportedException(detail);
            }
            lexicalHandler = (LexicalHandler)value;
        } else if (name.equals(DECLARATION_HANDLER)) {
            if (!(value instanceof DeclHandler)) {
                throw new SAXNotSupportedException(detail);
            }
            declHandler = (DeclHandler)value;
        } else {
            throw new SAXNotRecognizedException("Property: " + name);
        }
    }

    /**
     * Set the entity resolver.
     *
     * @param resolver The new entity resolver.
     * @exception java.lang.NullPointerException If the entity resolver
     *            parameter is null.
     * @see org.xml.sax.XMLReader#setEntityResolver
     */
    public void setEntityResolver(EntityResolver resolver) {
        if (resolver == null) {
            throw new NullPointerException("Null entity resolver");
        }
        entityResolver = resolver;
        if (parser != null) {
            parser.setEntityResolver(resolver); 
        }
    }

    /**
     * Return the current entity resolver.
     *
     * @return The current entity resolver, or null if none was supplied.
     * @see org.xml.sax.XMLReader#getEntityResolver
     */
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }

    /**
     * Set the DTD handler.
     *
     * @param resolver The new DTD handler.
     * @exception java.lang.NullPointerException If the DTD handler
     *            parameter is null.
     * @see org.xml.sax.XMLReader#setEntityResolver
     */
    public void setDTDHandler(DTDHandler handler) {
        if (handler == null) {
            throw new NullPointerException("Null DTD handler");
        }
        dtdHandler = handler;
        if (parser != null) {
            parser.setDTDHandler(dtdHandler);
        }
    }

    /**
     * Return the current DTD handler.
     *
     * @return The current DTD handler, or null if none was supplied.
     * @see org.xml.sax.XMLReader#getEntityResolver
     */
    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }

    /**
     * Set the content handler.
     *
     * @param resolver The new content handler.
     * @exception java.lang.NullPointerException If the content handler
     *            parameter is null.
     * @see org.xml.sax.XMLReader#setEntityResolver
     */
    public void setContentHandler(ContentHandler handler) {
        if (handler == null) {
            throw new NullPointerException("Null content handler");
        }
        contentHandler = handler;
        if (parser != null) {
            parser.setContentHandler(handler);
        }
    }

    /**
     * Return the current content handler.
     *
     * @return The current content handler, or null if none was supplied.
     * @see org.xml.sax.XMLReader#getEntityResolver
     */
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    /**
     * Set the error handler.
     *
     * @param resolver The new error handler.
     * @exception java.lang.NullPointerException If the error handler
     *            parameter is null.
     * @see org.xml.sax.XMLReader#setEntityResolver
     */
    public void setErrorHandler(ErrorHandler handler) {
        if (handler == null) {
            throw new NullPointerException("Null error handler");
        }
        errorHandler = handler;
        if (parser != null) {
            parser.setErrorHandler(errorHandler);
        }
    }

    /**
     * Return the current error handler.
     *
     * @return The current error handler, or null if none was supplied.
     * @see org.xml.sax.XMLReader#getEntityResolver
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    /**
     * Parse an XML document.
     *
     * @param systemId The absolute URL of the document.
     * @exception java.io.IOException If there is a problem reading
     *            the raw content of the document.
     * @exception org.xml.sax.SAXException If there is a problem
     *            processing the document.
     * @see #parse(org.xml.sax.InputSource)
     * @see org.xml.sax.Parser#parse(java.lang.String)
     */
    public void parse(String systemId)
        throws IOException, SAXException
    {
        parse(new InputSource(systemId));
    }

    /**
     * Parse an XML document.
     *
     * @param input An input source for the document.
     * @exception java.io.IOException If there is a problem reading
     *            the raw content of the document.
     * @exception org.xml.sax.SAXException If there is a problem
     *            processing the document.
     * @see #parse(java.lang.String)
     * @see org.xml.sax.Parser#parse(org.xml.sax.InputSource)
     */
    public void parse(InputSource input)
        throws IOException, SAXException
    {
        if (parsing) {
            throw new SAXException("Parser is already in use");
        }
        parsing = true;

        // Reuse existing parser if one already exists
        if (parser == null) {
            if (validation) {
                parser = new ValidatingParser();
            } else {
                parser = new Parser2();
            }
        }

        // Set up parser state.  Note: we set the parser state independent
        // of new parser creation since the caller may set the validation
        // feature and the handlers in an arbritrary order.  This allows us
        // to reuse existing parser instances when possible.
        parser.setNamespaceFeatures(namespaces, prefixes);
        parser.setContentHandler(contentHandler);
        parser.setDTDHandler(dtdHandler);
        parser.setErrorHandler(errorHandler);
        parser.setEntityResolver(entityResolver);
        // SAX2 ext handler
        parser.setLexicalHandler(lexicalHandler);
        // SAX2 ext handler
        parser.setDeclHandler(declHandler);

        try {
            parser.parse(input);
        } finally {
            parsing = false;
        }
    }


    ////////////////////////////////////////////////////////////////////
    // Internal utility MethodInfos.
    ////////////////////////////////////////////////////////////////////

    /**
     * Throw an exception if we are parsing.
     *
     * <p>Use this MethodInfo to detect illegal feature or
     * property changes.</p>
     *
     * @param type The type of thing (feature or property).
     * @param name The feature or property name.
     * @exception org.xml.sax.SAXNotSupportedException If a
     *            document is currently being parsed.
     */
    private void checkNotParsing(String type, String name)
        throws SAXNotSupportedException
    {
        if (parsing) {
            throw new SAXNotSupportedException("Cannot change " +
                                               type + ' ' +
                                               name + " while parsing");
                                               
        }
    }
}
