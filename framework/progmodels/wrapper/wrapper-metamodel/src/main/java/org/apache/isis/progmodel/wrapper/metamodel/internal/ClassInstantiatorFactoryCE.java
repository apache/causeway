/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.progmodel.wrapper.metamodel.internal;

/**
 * Factory returning a {@link IClassInstantiatorCE}for the current JVM
 */
class ClassInstantiatorFactoryCE {

    private static IClassInstantiatorCE instantiator = new ObjenesisClassInstantiatorCE();

    // ///CLOVER:OFF
    private ClassInstantiatorFactoryCE() {
    }

    // ///CLOVER:ON

    /**
     * Returns the current JVM as specified in the Systtem properties
     * 
     * @return current JVM
     */
    public static String getJVM() {
        return System.getProperty("java.vm.vendor");
    }

    /**
     * Returns the current JVM specification version (1.5, 1.4, 1.3)
     * 
     * @return current JVM specification version
     */
    public static String getJVMSpecificationVersion() {
        return System.getProperty("java.specification.version");
    }

    public static boolean is1_3Specifications() {
        return getJVMSpecificationVersion().equals("1.3");
    }

    /**
     * Returns a class instantiator suitable for the current JVM
     * 
     * @return a class instantiator usable on the current JVM
     */
    public static IClassInstantiatorCE getInstantiator() {
        return instantiator;
    }
}
