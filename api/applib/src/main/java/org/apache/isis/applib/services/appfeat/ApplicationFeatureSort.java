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
package org.apache.isis.applib.services.appfeat;

import org.apache.isis.commons.internal.base._Strings;

public enum ApplicationFeatureSort {
    
    /** 
     * logical namespace
     */
    NAMESPACE,
    
    /** 
     * {@code namespace + "." + typeSimpleName} 
     * makes up the fully qualified logical type
     */
    TYPE,
    
    /** 
     * {@code namespace + "." + typeSimpleName + "." + memberName} 
     * makes up the fully qualified logical member
     */
    MEMBER;

    public boolean hideClassName() {
        return this == ApplicationFeatureSort.NAMESPACE;
    }
    
    public boolean hideMember() {
        return this == ApplicationFeatureSort.NAMESPACE || this == ApplicationFeatureSort.TYPE;
    }

    public static void ensurePackage(final ApplicationFeatureId feature) {
        if(feature.sort != ApplicationFeatureSort.NAMESPACE) {
            throw new IllegalStateException("Can only be called for a package; " + feature.toString());
        }
    }

    public static void ensurePackageOrClass(final ApplicationFeatureId applicationFeatureId) {
        if(applicationFeatureId.sort != ApplicationFeatureSort.NAMESPACE && applicationFeatureId.sort != ApplicationFeatureSort.TYPE) {
            throw new IllegalStateException("Can only be called for a package or a class; " + applicationFeatureId.toString());
        }
    }

    public static void ensureClass(final ApplicationFeatureId feature) {
        if(feature.sort != ApplicationFeatureSort.TYPE) {
            throw new IllegalStateException("Can only be called for a class; " + feature.toString());
        }
    }

    public static void ensureMember(final ApplicationFeatureId feature) {
        if(feature.sort != ApplicationFeatureSort.MEMBER) {
            throw new IllegalStateException("Can only be called for a member; " + feature.toString());
        }
    }

    @Override
    public String toString() {
        return _Strings.capitalize(name());
    }
    
    // -- REFACTORING
    
    static void initNamespace(final ApplicationFeatureId feature, final String fullyQualifiedName) {
        feature.setNamespace(fullyQualifiedName);
        feature.setTypeSimpleName(null);
        feature.setMemberName(null);
        feature.sort = NAMESPACE;
    }
    
    static void initType(final ApplicationFeatureId feature, final String fullyQualifiedName) {
        final int i = fullyQualifiedName.lastIndexOf(".");
        if(i != -1) {
            feature.setNamespace(fullyQualifiedName.substring(0, i));
            feature.setTypeSimpleName(fullyQualifiedName.substring(i+1));
        } else {
            feature.setNamespace("");
            feature.setTypeSimpleName(fullyQualifiedName);
        }
        feature.setMemberName(null);
        feature.sort = TYPE;
    }
    
    static void initMember(final ApplicationFeatureId feature, final String fullyQualifiedName) {
        final int i = fullyQualifiedName.lastIndexOf("#");
        if(i == -1) {
            throw new IllegalArgumentException("Malformed, expected a '#': " + fullyQualifiedName);
        }
        final String className = fullyQualifiedName.substring(0, i);
        final String memberName = fullyQualifiedName.substring(i+1);
        initType(feature, className);
        feature.setMemberName(memberName);
        feature.sort = MEMBER;
    }
    

}
