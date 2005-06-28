/*
 * $Id: SimpleElementFactory.java,v 1.1 2005/04/11 23:47:44 rcm Exp $
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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;

import org.w3c.dom.*;


/**
 * This is a convenience class for creating application-specific elements
 * associated with specified (or default) XML namespaces.  It maintains
 * tables mapping element tag names to classes, and uses them as needed
 * to instantiate classes.  The string <em>*Element</em>, which is not a
 * legal XML element name, may be used to map otherwise unrecognized tags
 * to a particular class.  If this factory is not configured, then all
 * mappings are to the <a href=ElementNode.html>ElementNode</a> class.
 * Erroneous mappings are fatal errors.
 *
 * <P> A suggested XML syntax for recording these bindings, which may
 * in the future be explicitly supported, is: <PRE>
 * <b>&lt;bindings xmlns="..."&gt;</b>
 *     &lt;!-- first, bindings for the "default" namespace --&gt;
 *     <b>&lt;binding tag="..." class="..."/&gt;</b>
 *     &lt;binding <b>tag="*Element"</b> class="..."/&gt;
 *     ...
 *
 *     &lt;!-- then bindings for other namespaces --&gt;
 *     <b>&lt;namespace uri="..."&gt;</b>
 *         &lt;binding tag="..." class="..."/&gt;
 *         ...
 *     <b>&lt;/namespace&gt;</b>
 *
 *     &lt;!-- can specify JAR files for namespaces --&gt;
 *     &lt;namespace uri="..." <b>jar="..."</b>&gt;
 *         &lt;binding tag="..." class="..."/&gt;
 *         ...
 *     &lt;/namespace&gt;
 *     ...
 * <b>&lt;/bindings&gt;</b>
 * </PRE>
 *
 * <P> Note that while most URIs used to identify namespaces will be URLs,
 * such as <em>http://www.example.com/xml/purchasing</em>, some may also
 * be URNs like <em>urn:uuid:221ffe10-ae3c-11d1-b66c-00805f8a2676</em>.
 * You can't assume that the URIs are associated with web-accessible data;
 * they must be treated as being no more than distinguishable strings.
 *
 * <P> Applications classes configuring an element factory will need to
 * provide their own class loader (<code>this.class.getClassLoader</code>)
 * to get the desired behavior in common cases.  Classes loaded via some
 * URL will similarly need to use a network class loader.
 *
 * @author David Brownell
 * @version $Revision: 1.1 $
 */
public class SimpleElementFactory implements ElementFactory
{
    // in the absense of a mapping tied to namespace URI, use these
    private Dictionary		defaultMapping;
    private ClassLoader		defaultLoader;

    private String		defaultNs;

    // these hold mappings tied to namespace URIs
    private Dictionary		nsMappings;
    private Dictionary		nsLoaders;
    private Locale 		locale = Locale.getDefault ();


    /**
     * Constructs an unconfigured element factory.
     */
    public SimpleElementFactory () { }

    /**
     * Records a default element name to namespace mapping, for use
     * by namespace-unaware DOM construction and when a specific
     * namespace mapping is not available.
     *
     * @param dict Keys are element names, and values are either class
     *	names (interpreted with respect to <em>loader</em>) or class
     *	objects.  This value may not be null, and the dictionary is
     *	retained and modified by the factory.
     * @param loader If non-null, this is used instead of the bootstrap
     *	class loader when mapping from class names to class objects.
     */
    public void addMapping (Dictionary dict, ClassLoader loader)
    {
	if (dict == null)
	    throw new IllegalArgumentException ();
	defaultMapping = dict;
	defaultLoader = loader;
    }

    /**
     * Records a namespace-specific mapping between element names and
     * classes.
     *
     * @param namespace A URI identifying the namespace for which the
     *	mapping is defined
     * @param dict Keys are element names, and values are either class
     *	names (interpreted with respect to <em>loader</em>) or class
     *	objects.  This value may not be null, and the dictionary is
     *	retained and modified by the factory.
     * @param loader If non-null, this is used instead of the bootstrap
     *	class loader when mapping from class names to class objects.
     */
    public void addMapping (
	String		namespace,
	Dictionary	dict,
	ClassLoader	loader
    ) {
	if (namespace == null || dict == null)
	    throw new IllegalArgumentException ();
	if (nsMappings == null) {
	    nsMappings = new Hashtable ();
	    nsLoaders = new Hashtable ();
	}
	nsMappings.put (namespace, dict);
	if (loader != null)
	    nsLoaders.put (namespace, loader);
    }

    /**
     * Defines a URI to be treated as the "default" namespace.  This
     * is used only when choosing element classes, and may not be
     * visible when instances are asked for their namespaces. 
     */
    public void setDefaultNamespace (String ns)
	{ defaultNs = ns; }

    private Class map2Class (
	String		key,
	Dictionary	node2class,
	ClassLoader	loader
    )
    {
	Object		mapResult = node2class.get (key);

	if (mapResult instanceof Class)
	    return (Class) mapResult;
	if (mapResult == null)
	    return null;
        
	if (mapResult instanceof String) {
	    String	className = (String) mapResult;
	    Class	retval;

	    try {
                if (loader == null) {
                    // Find the appropriate ClassLoader to use depending on
                    // if we are part of the JDK or not and taking JDK
                    // version into account.
                    loader = findClassLoader();
                }
		if (loader == null)
		    retval = Class.forName (className);
		else
		    retval = loader.loadClass (className);

		//
		// We really have no option here.  DOM requires two
		// bidirectional relationships (parent/child, and
		// between siblings) and one unidirectional one (nodes
		// belong to one document) but doesn't provide APIs that
		// would suffice to maintain them.  So those APIs
		// must rely on knowledge of the implementation to
		// which things are connecting.
		//
		if (!ElementNode.class.isAssignableFrom (retval))
		    throw new IllegalArgumentException (getMessage ("SEF-000",
		    			new Object [] { key, className }));
		node2class.put (key, retval);
		return retval;
		
	    } catch (ClassNotFoundException e) {
		throw new IllegalArgumentException (getMessage ("SEF-001",
			new Object [] { key, className, e.getMessage ()}));
	    }
	}

	// another option:  clone elements, resetting parent
	// and document associations?

	throw new IllegalArgumentException (getMessage ("SEF-002", 
				new Object [] { key }));
    }

    private ElementNode doMap (
	String		tagName,
	Dictionary	node2class,
	ClassLoader	loader
    ) {
        Class		theClass;
	ElementNode	retval;

	theClass = map2Class (tagName, node2class, loader);
	if (theClass == null)
	    theClass = map2Class ("*Element", node2class, loader);
	if (theClass == null)
	    retval = new ElementNode (tagName);
	else {
	    try {
		retval = (ElementNode) theClass.newInstance ();
	    } catch (Exception e) {
		    //InstantiationException
		    //IllegalAccessException
		throw new IllegalArgumentException (getMessage ("SEF-003",
			new Object [] {tagName, theClass.getName (),
					e.getMessage () }));
	    }
	}
	return retval;
    }

    /**
     * Creates an element by using the mapping associated with the
     * specified namespace, or the default namespace as appropriate.
     * If no mapping associated with that namespace is defined, then
     * the default mapping is used.
     *
     * @param namespace URI for namespace; null indicates use of
     *	the default namespace, if any.
     * @param localName element tag, without any embedded colon
     */
    public ElementEx createElementEx (String namespace, String localName)
    {
	Dictionary	mapping = null;

	if (namespace == null)
	    namespace = defaultNs;

	if (nsMappings != null)
	    mapping = (Dictionary) nsMappings.get (namespace);
	if (mapping == null)
	    return doMap (localName, defaultMapping, defaultLoader);
	else
	    return doMap (localName, mapping,
		(ClassLoader)nsLoaders.get (namespace));
    }

    /**
     * Creates an element by using the default mapping.
     *
     * @param tag element tag
     */
    public ElementEx createElementEx (String tag)
    {
	return doMap (tag, defaultMapping, defaultLoader);
    }

    /*
     * Gets the messages from the resource bundles for the given messageId.
     */
    String getMessage (String messageId) {
   	return getMessage (messageId, null);
    }

    /*
     * Gets the messages from the resource bundles for the given messageId
     * after formatting it with the parameters passed to it.
     */
    //XXX use the default locale only at this point.
    String getMessage (String messageId, Object[] parameters) {
	return XmlDocument.catalog.getMessage (locale, messageId, parameters);
    }


    /*
     * The following section of code tries to get our ContextClassLoader,
     * if we are running in Java 2.  This code is derived from
     * javax.xml.parsers.FactoryFinder.
     */

    /**
     * Figure out which ClassLoader to use.  For JDK 1.2 and later use the
     * context ClassLoader if possible.  Note: we defer linking the class
     * that calls an API only in JDK 1.2 until runtime so that we can catch
     * LinkageError so that this code will run in older non-Sun JVMs such
     * as the Microsoft JVM in IE.
     */
    private static ClassLoader findClassLoader()
    {
        ClassLoader classLoader;
		 // .NET port
		  classLoader = SimpleElementFactory.class.getClassLoader();
//        try {
//            // Construct the name of the concrete class to instantiate
//            Class clazz = Class.forName(SimpleElementFactory.class.getName()
//                                        + "$ClassLoaderFinderConcrete");
//            ClassLoaderFinder clf = (ClassLoaderFinder) clazz.newInstance();
//            classLoader = clf.getContextClassLoader();
//        } catch (LinkageError le) {
//            // Assume that we are running JDK 1.1, use the current ClassLoader
//            classLoader = SimpleElementFactory.class.getClassLoader();
//        } catch (ClassNotFoundException x) {
//            // This case should not normally happen.  MS IE can throw this
//            // instead of a LinkageError the second time Class.forName() is
//            // called so assume that we are running JDK 1.1 and use the
//            // current ClassLoader
//            classLoader = SimpleElementFactory.class.getClassLoader();
//        } catch (Exception x) {
//            // Something abnormal happened so just use current ClassLoader
//            classLoader = SimpleElementFactory.class.getClassLoader();
//        }
        return classLoader;
    }

    /*
     * The following nested classes allow getContextClassLoader() to be
     * called only on JDK 1.2 and yet run in older JDK 1.1 JVMs
     */
// .NET port - commented out; the code above should still work...
//    private static abstract class ClassLoaderFinder {
//        abstract ClassLoader getContextClassLoader();
//    }

//    static class ClassLoaderFinderConcrete extends ClassLoaderFinder {
//        ClassLoader getContextClassLoader() {
//            return Thread.currentThread().getContextClassLoader();
//        }
//    }

}
