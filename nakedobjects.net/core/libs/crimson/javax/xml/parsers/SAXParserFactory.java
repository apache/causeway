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

import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * Defines a factory API that enables applications to configure and
 * obtain a SAX based parser to parse XML documents.<p>
 * An implementation of the <code>SAXParserFactory</code> class is
 * <em>NOT</em> guaranteed to be thread safe. It is up to the user application 
 * to make sure about the use of the <code>SAXParserFactory</code> from 
 * more than one thread. Alternatively the application can have one instance 
 * of the <code>SAXParserFactory</code> per thread.
 * An application can use the same instance of the factory to obtain one or 
 * more instances of the <code>SAXParser</code> provided the instance
 * of the factory isn't being used in more than one thread at a time.
 * <p>
 *
 * The static <code>newInstance</code> MethodInfo returns a new concrete 
 * implementation of this class.
 *
 * @since JAXP 1.0
 * @version 1.0
 */

public abstract class SAXParserFactory {
    private boolean validating = false;
    private boolean namespaceAware= false;
    
    protected SAXParserFactory () {
    
    }

    /**
     * Obtain a new instance of a <code>SAXParserFactory</code>. This
     * static MethodInfo creates a new factory instance
     * This MethodInfo uses the following ordered lookup procedure to determine
     * the <code>SAXParserFactory</code> implementation class to
     * load:
     * <ul>
     * <li>
     * Use the <code>javax.xml.parsers.SAXParserFactory</code> system
     * property.
     * </li>
     * <li>
     * Use the properties file "lib/jaxp.properties" in the JRE directory.
     * This configuration file is in standard <code>java.util.Properties
     * </code> format and contains the fully qualified name of the
     * implementation class with the key being the system property defined
     * above.
     * </li>
     * <li>
     * Use the Services API (as detailed in the JAR specification), if
     * available, to determine the classname. The Services API will look
     * for a classname in the file
     * <code>META-INF/services/javax.xml.parsers.SAXParserFactory</code>
     * in jars available to the runtime.
     * </li>
     * <li>
     * Platform default <code>SAXParserFactory</code> instance.
     * </li>
     * </ul>
     *
     * Once an application has obtained a reference to a
     * <code>SAXParserFactory</code> it can use the factory to
     * configure and obtain parser instances.
     *
     * @return A new instance of a SAXParserFactory.
     *
     * @exception FactoryConfigurationError if the implementation is
     * not available or cannot be instantiated.
     */

    public static SAXParserFactory newInstance()
        throws FactoryConfigurationError
    {
        try {
            return (SAXParserFactory) FactoryFinder.find(
                /* The default property name according to the JAXP spec */
                "javax.xml.parsers.SAXParserFactory",
                /* The fallback implementation class name */
                "org.apache.crimson.jaxp.SAXParserFactoryImpl");
        } catch (FactoryFinder.ConfigurationError e) {
            throw new FactoryConfigurationError(e.getException(),
                                                e.getMessage());
        }
    }
    
    /**
     * Creates a new instance of a SAXParser using the currently
     * configured factory parameters.
     *
     * @return A new instance of a SAXParser.
     *
     * @exception ParserConfigurationException if a parser cannot
     * be created which satisfies the requested configuration.
     */
    
    public abstract SAXParser newSAXParser()
        throws ParserConfigurationException, SAXException;

    
    /**
     * Specifies that the parser produced by this code will
     * provide support for XML namespaces. By default the value of this is set
     * to <code>false</code>.
     *
     * @param awareness true if the parser produced by this code will
     *                  provide support for XML namespaces; false otherwise.
     */
    
    public void setNamespaceAware(boolean awareness)
    {
        this.namespaceAware = awareness;
    }

    /**
     * Specifies that the parser produced by this code will
     * validate documents as they are parsed. By default the value of this is
     * set to <code>false</code>.
     *
     * @param validating true if the parser produced by this code will
     *                   validate documents as they are parsed; false otherwise.
     */
    
    public void setValidating(boolean validating)
    {
        this.validating = validating;
    }

    /**
     * Indicates whether or not the factory is configured to produce
     * parsers which are namespace aware.
     *
     * @return true if the factory is configured to produce
     *         parsers which are namespace aware; false otherwise.
     */
    
    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    /**
     * Indicates whether or not the factory is configured to produce
     * parsers which validate the XML content during parse.
     *
     * @return true if the factory is configured to produce parsers which validate
     *         the XML content during parse; false otherwise.
     */
    
    public boolean isValidating() {
        return validating;
    }

    /**
     *
     * Sets the particular feature in the underlying implementation of
     * org.xml.sax.XMLReader.
     * A list of the core features and properties can be found at
     * <a href="http://www.megginson.com/SAX/Java/features.html"> http://www.megginson.com/SAX/Java/features.html </a>
     *
     * @param name The name of the feature to be set.
     * @param value The value of the feature to be set.
     * @exception SAXNotRecognizedException When the underlying XMLReader does
     *            not recognize the property name.
     *
     * @exception SAXNotSupportedException When the underlying XMLReader
     *            recognizes the property name but doesn't support the
     *            property.
     *
     * @see org.xml.sax.XMLReader#setFeature
     */
    public abstract void setFeature(String name, boolean value)
        throws ParserConfigurationException, SAXNotRecognizedException,
                SAXNotSupportedException;

    /**
     *
     * Returns the particular property requested for in the underlying
     * implementation of org.xml.sax.XMLReader.
     *
     * @param name The name of the property to be retrieved.
     * @return Value of the requested property.
     *
     * @exception SAXNotRecognizedException When the underlying XMLReader does
     *            not recognize the property name.
     *
     * @exception SAXNotSupportedException When the underlying XMLReader
     *            recognizes the property name but doesn't support the
     *            property.
     *
     * @see org.xml.sax.XMLReader#getProperty
     */
    public abstract boolean getFeature(String name)
        throws ParserConfigurationException, SAXNotRecognizedException,
                SAXNotSupportedException;
}
