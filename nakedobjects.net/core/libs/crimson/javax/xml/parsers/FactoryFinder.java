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
import java.io.FileInputStream;

import java.util.Properties;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This class is duplicated for each JAXP subpackage so keep it in
 * sync.  It is package private.
 *
 * This code is designed to implement the JAXP 1.1 spec pluggability
 * feature and is designed to run on JDK version 1.1 and later including
 * JVMs that perform early linking like the Microsoft JVM in IE 5.  Note
 * however that it must be compiled on a JDK version 1.2 or later system
 * since it calls Thread#getContextClassLoader().  The code also runs both
 * as part of an unbundled jar file and when bundled as part of the JDK.
 */
class FactoryFinder {
    /** Temp debug code - this will be removed after we test everything
     */
    private static boolean debug = false;
    static {
        // Use try/catch block to support applets
        try {
            debug = System.getProperty("jaxp.debug") != null;
        } catch (Exception x) {
        }
    }

    private static void debugPrintln(String msg) {
        if (debug) {
            System.err.println("JAXP: " + msg);
        }
    }

    /**
     * Figure out which ClassLoader to use.  For JDK 1.2 and later use the
     * context ClassLoader if possible.  Note: we defer linking the class
     * that calls an API only in JDK 1.2 until runtime so that we can catch
     * LinkageError so that this code will run in older non-Sun JVMs such
     * as the Microsoft JVM in IE.
     */
    private static ClassLoader findClassLoader()
        throws ConfigurationError
    {
        ClassLoader classLoader;
		 classLoader = FactoryFinder.class.getClassLoader();
// .NET port
//		 try 
//		 {
//            // Construct the name of the concrete class to instantiate
//            Class clazz = Class.forName(FactoryFinder.class.getName()
//                                        + "$ClassLoaderFinderConcrete");
//            ClassLoaderFinder clf = (ClassLoaderFinder) clazz.newInstance();
//            classLoader = clf.getContextClassLoader();
//        } catch (LinkageError le) {
//            // Assume that we are running JDK 1.1, use the current ClassLoader
//            classLoader = FactoryFinder.class.getClassLoader();
//        } catch (ClassNotFoundException x) {
//            // This case should not normally happen.  MS IE can throw this
//            // instead of a LinkageError the second time Class.forName() is
//            // called so assume that we are running JDK 1.1 and use the
//            // current ClassLoader
//            classLoader = FactoryFinder.class.getClassLoader();
//        } catch (Exception x) {
//            // Something abnormal happened so throw an error
//            throw new ConfigurationError(x.toString(), x);
//        }
        return classLoader;
    }

    /**
     * Create an instance of a class using the specified ClassLoader
     */
    private static Object newInstance(String className,
                                      ClassLoader classLoader)
        throws ConfigurationError
    {
        try {
            Class spiClass;
            if (classLoader == null) {
                spiClass = Class.forName(className);
            } else {
                spiClass = classLoader.loadClass(className);
            }
            return spiClass.newInstance();
        } catch (ClassNotFoundException x) {
            throw new ConfigurationError(
                "Provider " + className + " not found", x);
        } catch (Exception x) {
            throw new ConfigurationError(
                "Provider " + className + " could not be instantiated: " + x,
                x);
        }
    }

    /**
     * Finds the implementation Class object in the specified order.  Main
     * entry point.
     * @return Class object of factory, never null
     *
     * @param factoryId             Name of the factory to find, same as
     *                              a property name
     * @param fallbackClassName     Implementation class name, if nothing else
     *                              is found.  Use null to mean no fallback.
     *
     * Package private so this code can be shared.
     */
    static Object find(String factoryId, String fallbackClassName)
        throws ConfigurationError
    {
        ClassLoader classLoader = findClassLoader();

        // Use the system property first
        try {
            String systemProp =
                System.getProperty( factoryId );
            if( systemProp!=null) {
                debugPrintln("found system property" + systemProp);
                return newInstance(systemProp, classLoader);
            }
        } catch (SecurityException se) {
        }

        // try to read from $java.home/lib/xml.properties
        try {
            String javah=System.getProperty( "java.home" );
            String configFile = javah + File.separator +
                "lib" + File.separator + "jaxp.properties";
            File f=new File( configFile );
            if( f.exists()) {
                Properties props=new Properties();
                props.load( new FileInputStream(f));
                String factoryClassName = props.getProperty(factoryId);
                debugPrintln("found java.home property " + factoryClassName);
                return newInstance(factoryClassName, classLoader);
            }
        } catch(Exception ex ) {
            if( debug ) ex.printStackTrace();
        }

        String serviceId = "META-INF/services/" + factoryId;
        // try to find services in CLASSPATH
        try {
            InputStream is=null;
            if (classLoader == null) {
                is=ClassLoader.getSystemResourceAsStream( serviceId );
            } else {
                is=classLoader.getResourceAsStream( serviceId );
            }
        
            if( is!=null ) {
                debugPrintln("found " + serviceId);
                BufferedReader rd =
                    new BufferedReader(new InputStreamReader(is, "UTF-8"));
        
                String factoryClassName = rd.readLine();
                rd.close();

                if (factoryClassName != null &&
                    ! "".equals(factoryClassName)) {
                    debugPrintln("loaded from services: " + factoryClassName);
                    return newInstance(factoryClassName, classLoader);
                }
            }
        } catch( Exception ex ) {
            if( debug ) ex.printStackTrace();
        }

        if (fallbackClassName == null) {
            throw new ConfigurationError(
                "Provider for " + factoryId + " cannot be found", null);
        }

        debugPrintln("loaded from fallback value: " + fallbackClassName);
        return newInstance(fallbackClassName, classLoader);
    }

    static class ConfigurationError extends Error {
        private Exception exception;

        /**
         * Construct a new instance with the specified detail string and
         * exception.
         */
        ConfigurationError(String msg, Exception x) {
            super(msg);
            this.exception = x;
        }

        Exception getException() {
            return exception;
        }
    }

    /*
     * The following nested classes allow getContextClassLoader() to be
     * called only on JDK 1.2 and yet run in older JDK 1.1 JVMs
     */
// .NET port
//    private static abstract class ClassLoaderFinder {
//        abstract ClassLoader getContextClassLoader();
//    }
//
//    static class ClassLoaderFinderConcrete extends ClassLoaderFinder {
//        ClassLoader getContextClassLoader() {
//            return Thread.currentThread().getContextClassLoader();
//        }
//    }
}
