/*
 * $Id: SAXParserImpl.java,v 1.1 2005/04/11 23:47:43 rcm Exp $
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

package org.apache.crimson.jaxp;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.HandlerBase;
import org.xml.sax.Parser;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderAdapter;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import java.util.*;

/**
 * @author Rajiv Mordani
 * @author Edwin Goei
 * @version $Revision: 1.1 $
 */

/**
 * This is the implementation specific class for the
 * <code>javax.xml.parsers.SAXParser</code>. 
 */
public class SAXParserImpl extends SAXParser {
    private XMLReader xmlReader;
    private Parser parser = null;

    private boolean validating = false;
    private boolean namespaceAware = false;
    
    /**
     * Create a SAX parser with the associated features
     * @param features Hashtable of SAX features, may be null
     */
    SAXParserImpl(SAXParserFactory spf, Hashtable features)
        throws SAXException
    {
        // Instantiate an XMLReader directly and not through SAX so that we
        // use the right ClassLoader
        xmlReader = new org.apache.crimson.parser.XMLReaderImpl();

        // Validation
        validating = spf.isValidating();
        String validation = "http://xml.org/sax/features/validation";

        // If validating, provide a default ErrorHandler that prints
        // validation errors with a warning telling the user to set an
        // ErrorHandler.  Note: this does not handle all cases.
        if (validating) {
            xmlReader.setErrorHandler(new DefaultValidationErrorHandler());
        }

        // Allow SAX parser to use a different ErrorHandler if it wants to
        xmlReader.setFeature(validation, validating);

        // "namespaceAware" == SAX Namespaces feature
        // Note: there is a compatibility problem here with default values:
        // JAXP default is false while SAX 2 default is true!
        namespaceAware = spf.isNamespaceAware();
        String namespaces = "http://xml.org/sax/features/namespaces";
        xmlReader.setFeature(namespaces, namespaceAware);

        setFeatures(features);
    }

    /**
     * Set any features of our XMLReader based on any features set on the
     * SAXParserFactory.
     *
     * XXX Does not handle possible conflicts between SAX feature names and
     * JAXP specific feature names, eg. SAXParserFactory.isValidating()
     */
    private void setFeatures(Hashtable features)
        throws SAXNotSupportedException, SAXNotRecognizedException
    {
        if (features != null) {
            for (Enumeration e = features.keys(); e.hasMoreElements();) {
                String feature = (String)e.nextElement();
                boolean value = ((Boolean)features.get(feature)).booleanValue();
                xmlReader.setFeature(feature, value);
            }
        }
    }

    public Parser getParser() throws SAXException {
        if (parser == null) {
            // Adapt a SAX2 XMLReader into a SAX1 Parser
            parser = new XMLReaderAdapter(xmlReader);

            // Set a DocumentHandler that does nothing to avoid getting
            // exceptions if no DocumentHandler is set by the app
            parser.setDocumentHandler(new HandlerBase());
        }
        return parser;
    }

    /**
     * Returns the XMLReader that is encapsulated by the implementation of
     * this class.
     */
    public XMLReader getXMLReader() {
        return xmlReader;
    }

    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    public boolean isValidating() {
        return validating;
    }

    /**
     * Sets the particular property in the underlying implementation of 
     * org.xml.sax.XMLReader.
     */
    public void setProperty(String name, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        xmlReader.setProperty(name, value);
    }

    /**
     * returns the particular property requested for in the underlying 
     * implementation of org.xml.sax.XMLReader.
     */
    public Object getProperty(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        return xmlReader.getProperty(name);
    }
}
