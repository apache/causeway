/*
 * $Id: SAXParserFactoryImpl.java,v 1.1 2005/04/11 23:47:43 rcm Exp $
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

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import java.util.Hashtable;

/**
 * @author Rajiv Mordani
 * @author Edwin Goei
 * @version $Revision: 1.1 $
 */

/**
 * This is the implementation specific class for the
 * <code>javax.xml.parsers.SAXParserFactory</code>. This is the platform
 * default implementation for the platform.
 */
public class SAXParserFactoryImpl extends SAXParserFactory {
    private Hashtable features;

    /**
     * Creates a new instance of <code>SAXParser</code> using the currently
     * configured factory parameters.
     * @return javax.xml.parsers.SAXParser
     */
    public SAXParser newSAXParser()
        throws ParserConfigurationException
    {
        SAXParser saxParserImpl;
        try {
            saxParserImpl = new SAXParserImpl(this, features);
        } catch (SAXException se) {
            // Translate to ParserConfigurationException
            throw new ParserConfigurationException(se.getMessage());
        }
	return saxParserImpl;
    }

    /**
     * Common code for translating exceptions
     */
    private SAXParserImpl newSAXParserImpl()
        throws ParserConfigurationException, SAXNotRecognizedException, 
        SAXNotSupportedException
    {
        SAXParserImpl saxParserImpl;
        try {
            saxParserImpl = new SAXParserImpl(this, features);
        } catch (SAXNotSupportedException e) {
            throw e;
        } catch (SAXNotRecognizedException e) {
            throw e;
        } catch (SAXException se) {
            throw new ParserConfigurationException(se.getMessage());
        }
        return saxParserImpl;
    }

    /**
     * Sets the particular feature in the underlying implementation of 
     * org.xml.sax.XMLReader.
     */
    public void setFeature(String name, boolean value)
        throws ParserConfigurationException, SAXNotRecognizedException, 
            SAXNotSupportedException
    {
        // XXX This is ugly.  We have to collect the features and then
        // later create an XMLReader to verify the features.
        if (features == null) {
            features = new Hashtable();
        }
        features.put(name, new Boolean(value));

        // Test the feature by possibly throwing SAX exceptions
        try {
            newSAXParserImpl();
        } catch (SAXNotSupportedException e) {
            features.remove(name);
            throw e;
        } catch (SAXNotRecognizedException e) {
            features.remove(name);
            throw e;
        }
    }

    /**
     * returns the particular property requested for in the underlying 
     * implementation of org.xml.sax.XMLReader.
     */
    public boolean getFeature(String name)
        throws ParserConfigurationException, SAXNotRecognizedException,
		SAXNotSupportedException
    {
        // Check for valid name by creating a dummy XMLReader to get
        // feature value
        return newSAXParserImpl().getXMLReader().getFeature(name);
    }
}
