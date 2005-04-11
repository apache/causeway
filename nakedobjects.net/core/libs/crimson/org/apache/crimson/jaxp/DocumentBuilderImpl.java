/*
 * $Id: DocumentBuilderImpl.java,v 1.1 2005/04/11 23:47:43 rcm Exp $
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

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

import org.apache.crimson.parser.XMLReaderImpl;

import org.apache.crimson.tree.XmlDocument;
import org.apache.crimson.tree.XmlDocumentBuilder;
import org.apache.crimson.tree.XmlDocumentBuilderNS;
import org.apache.crimson.tree.DOMImplementationImpl;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Rajiv Mordani
 * @version $Revision: 1.1 $
 */
public class DocumentBuilderImpl extends DocumentBuilder {

    private DocumentBuilderFactory dbf;

    private EntityResolver er = null;
    private ErrorHandler eh = null;
    private XMLReader xmlReader = null;
    private XmlDocumentBuilder builder = null;

    private boolean namespaceAware = false;
    private boolean validating = false;

    DocumentBuilderImpl(DocumentBuilderFactory dbf)
        throws ParserConfigurationException
    {
        this.dbf = dbf;
        namespaceAware = dbf.isNamespaceAware();

        xmlReader = new XMLReaderImpl();

        try {
            // Validation
            validating = dbf.isValidating();
            String validation = "http://xml.org/sax/features/validation";
            xmlReader.setFeature(validation, validating);

            // If validating, provide a default ErrorHandler that prints
            // validation errors with a warning telling the user to set an
            // ErrorHandler
            if (validating) {
                setErrorHandler(new DefaultValidationErrorHandler());
            }

            // SAX2 namespace-prefixes should be true for either builder
            String nsPrefixes =
                    "http://xml.org/sax/features/namespace-prefixes";
            xmlReader.setFeature(nsPrefixes, true);

            // Set SAX2 namespaces feature appropriately
            String namespaces = "http://xml.org/sax/features/namespaces";
            xmlReader.setFeature(namespaces, namespaceAware);

            // Use the appropriate DOM builder based on "namespaceAware"
            if (namespaceAware) {
                builder = new XmlDocumentBuilderNS();
            } else {
                builder = new XmlDocumentBuilder();
            }

            // Use builder as the ContentHandler
            xmlReader.setContentHandler(builder);
          
            // org.xml.sax.ext.LexicalHandler
            String lexHandler = "http://xml.org/sax/properties/lexical-handler";
            xmlReader.setProperty(lexHandler, builder);

            // org.xml.sax.ext.DeclHandler
            String declHandler =
                    "http://xml.org/sax/properties/declaration-handler";
            xmlReader.setProperty(declHandler, builder);

            // DTDHandler
            xmlReader.setDTDHandler(builder);
        } catch (SAXException e) {
            // Handles both SAXNotSupportedException, SAXNotRecognizedException
            throw new ParserConfigurationException(e.getMessage());
        }

        // Set various builder properties obtained from DocumentBuilderFactory
        builder.setIgnoreWhitespace(dbf.isIgnoringElementContentWhitespace());
        builder.setExpandEntityReferences(dbf.isExpandEntityReferences());
        builder.setIgnoreComments(dbf.isIgnoringComments());
        builder.setPutCDATAIntoText(dbf.isCoalescing());
    }

    public Document newDocument() {
        return new XmlDocument(); 
    }

    public DOMImplementation getDOMImplementation() {
        return DOMImplementationImpl.getDOMImplementation(); 
    }

    public Document parse(InputSource is) throws SAXException, IOException {
        if (is == null) {
            throw new IllegalArgumentException("InputSource cannot be null");
        }

        if (er != null) {
            xmlReader.setEntityResolver(er);
        }

        if (eh != null) {
            xmlReader.setErrorHandler(eh);      
        }

        xmlReader.parse(is);
        return builder.getDocument();
    }

    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    public boolean isValidating() {
        return validating;
    }

    public void setEntityResolver(org.xml.sax.EntityResolver er) {
        this.er = er;
    }

    public void setErrorHandler(org.xml.sax.ErrorHandler eh) {
        // If app passes in a ErrorHandler of null, then ignore all errors
        // and warnings
        this.eh = (eh == null) ? new DefaultHandler() : eh;
    }
}
