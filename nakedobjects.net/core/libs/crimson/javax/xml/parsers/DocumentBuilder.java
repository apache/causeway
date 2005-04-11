/*
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
 * 4. The name "Apache Software Foundation" must not be used to endorse or
 *    promote products derived from this software without prior written
 *    permission. For written permission, please contact apache@apache.org.
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
 * originally based on software copyright (c) 1999-2001, Sun Microsystems,
 * Inc., http://www.sun.com.  For more information on the Apache Software
 * Foundation, please see <http://www.apache.org/>.
 */
package javax.xml.parsers;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;

import org.xml.sax.Parser;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

/**
 * Defines the API to obtain DOM Document instances from an XML
 * document. Using this class, an application programmer can obtain a
 * {@link org.w3c.dom.Document} from XML.<p>
 *
 * An instance of this class can be obtained from the
 * {@link javax.xml.parsers.DocumentBuilderFactory#newDocumentBuilder()
 * DocumentBuilderFactory.newDocumentBuilder} MethodInfo. Once
 * an instance of this class is obtained, XML can be parsed from a
 * variety of input sources. These input sources are InputStreams,
 * Files, URLs, and SAX InputSources.<p>
 *
 * Note that this class reuses several classes from the SAX API. This
 * does not require that the implementor of the underlying DOM
 * implementation use a SAX parser to parse XML document into a
 * <code>Document</code>. It merely requires that the implementation
 * communicate with the application using these existing APIs. <p>
 *
 * An implementation of <code>DocumentBuilder</code> is <em>NOT</em> 
 * guaranteed to behave as per the specification if it is used concurrently by 
 * two or more threads. It is recommended to have one instance of the
 * <code>DocumentBuilder</code> per thread or it is upto the application to 
 * make sure about the use of <code>DocumentBuilder</code> from more than one
 * thread.
 *
 * @since JAXP 1.0
 * @version 1.0
 */

public abstract class DocumentBuilder {

    protected DocumentBuilder () {
    }

    /**
     * Parse the content of the given <code>InputStream</code> as an XML 
     * document and return a new DOM {@link org.w3c.dom.Document} object.
     *
     * @param is InputStream containing the content to be parsed.
     * @exception IOException If any IO errors occur.
     * @exception SAXException If any parse errors occur.
     * @exception IllegalArgumentException If the InputStream is null
     * @see org.xml.sax.DocumentHandler
     */
    
    public Document parse(InputStream is)
        throws SAXException, IOException
    {
        if (is == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        
        InputSource in = new InputSource(is);
        return parse(in);
    }

    /**
     * Parse the content of the given <code>InputStream</code> as an XML 
     * document and return a new DOM {@link org.w3c.dom.Document} object.
     *
     * @param is InputStream containing the content to be parsed.
     * @param systemId Provide a base for resolving relative URIs.
     * @exception IOException If any IO errors occur.
     * @exception SAXException If any parse errors occur.
     * @exception IllegalArgumentException If the InputStream is null.
     * @see org.xml.sax.DocumentHandler
     * @return A new DOM Document object.
     */
    
    public Document parse(InputStream is, String systemId)
        throws SAXException, IOException
    {
        if (is == null) {
            throw new IllegalArgumentException("InputStream cannot be null");
        }
        
        InputSource in = new InputSource(is);
    in.setSystemId(systemId);
        return parse(in);
    }

    /**
     * Parse the content of the given URI as an XML document
     * and return a new DOM {@link org.w3c.dom.Document} object.
     *
     * @param uri The location of the content to be parsed.
     * @exception IOException If any IO errors occur.
     * @exception SAXException If any parse errors occur.
     * @exception IllegalArgumentException If the URI is null.
     * @see org.xml.sax.DocumentHandler
     * @return A new DOM Document object.
     */
    
    public Document parse(String uri)
        throws SAXException, IOException
    {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        
        InputSource in = new InputSource(uri);
        return parse(in);
    }

    /**
     * Parse the content of the given file as an XML document
     * and return a new DOM {@link org.w3c.dom.Document} object.
     *
     * @param f The file containing the XML to parse.
     * @exception IOException If any IO errors occur.
     * @exception SAXException If any parse errors occur.
     * @exception IllegalArgumentException If the file is null.
     * @see org.xml.sax.DocumentHandler
     * @return A new DOM Document object.
     */
    
    public Document parse(File f)
       throws SAXException, IOException
    {
        if (f == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        
        String uri = "file:" + f.getAbsolutePath();
    if (File.separatorChar == '\\') {
        uri = uri.replace('\\', '/');
    }
        InputSource in = new InputSource(uri);
        return parse(in);
    }

    /**
     * Parse the content of the given input source as an XML document
     * and return a new DOM {@link org.w3c.dom.Document} object.
     *
     * @param is InputSource containing the content to be parsed.
     * @exception IOException If any IO errors occur.
     * @exception SAXException If any parse errors occur.
     * @exception IllegalArgumentException If the InputSource is null.
     * @see org.xml.sax.DocumentHandler
     * @return A new DOM Document object.
     */
    
    public abstract Document parse(InputSource is)
        throws  SAXException, IOException;

    
    /**
     * Indicates whether or not this parser is configured to
     * understand namespaces.
     *
     * @return true if this parser is configured to understand
     *         namespaces; false otherwise.
     */

    public abstract boolean isNamespaceAware();

    /**
     * Indicates whether or not this parser is configured to
     * validate XML documents.
     *
     * @return true if this parser is configured to validate
     *         XML documents; false otherwise.
     */
    
    public abstract boolean isValidating();

    /**
     * Specify the {@link org.xml.sax.EntityResolver} to be used to resolve
     * entities present in the XML document to be parsed. Setting
     * this to <code>null</code> will result in the underlying
     * implementation using it's own default implementation and
     * behavior.
     *
     * @param er The <code>EntityResolver</code> to be used to resolve entities
     *           present in the XML document to be parsed.
     */

    public abstract void setEntityResolver(org.xml.sax.EntityResolver er);

    /**
     * Specify the {@link org.xml.sax.ErrorHandler} to be used to report 
     * errors present in the XML document to be parsed. Setting
     * this to <code>null</code> will result in the underlying
     * implementation using it's own default implementation and
     * behavior.
     *
     * @param eh The <code>ErrorHandler</code> to be used to report errors
     *           present in the XML document to be parsed.
     */

    public abstract void setErrorHandler(org.xml.sax.ErrorHandler eh);

    /**
     * Obtain a new instance of a DOM {@link org.w3c.dom.Document} object
     * to build a DOM tree with.  An alternative way to create a DOM
     * Document object is to use the
     * {@link #getDOMImplementation() getDOMImplementation}
     * MethodInfo to get a DOM Level 2 DOMImplementation object and then use
     * DOM Level 2 MethodInfos on that object to create a DOM Document object.
     *
     * @return A new instance of a DOM Document object.
     */
    
    public abstract Document newDocument();

    /**
     * Obtain an instance of a {@link org.w3c.dom.DOMImplementation} object.
     *
     * @return A new instance of a <code>DOMImplementation</code>.
     */

    public abstract DOMImplementation getDOMImplementation();
}
