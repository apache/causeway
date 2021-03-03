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

public enum ApplicationFeatureType {
    
    /** 
     * logical package aka <i>object namespace</i>
     */
    PACKAGE {
        @Override
        void init(final ApplicationFeatureId feature, final String fullyQualifiedName) {
            feature.setNamespace(fullyQualifiedName);
            feature.setTypeSimpleName(null);
            feature.setMemberName(null);
            feature.type = this;
        }
    },
    
    /** 
     * logical class aka <i>object type</i>
     */
    CLASS {
        @Override
        void init(final ApplicationFeatureId feature, final String fullyQualifiedName) {
            final int i = fullyQualifiedName.lastIndexOf(".");
            if(i != -1) {
                feature.setNamespace(fullyQualifiedName.substring(0, i));
                feature.setTypeSimpleName(fullyQualifiedName.substring(i+1));
            } else {
                feature.setNamespace("");
                feature.setTypeSimpleName(fullyQualifiedName);
            }
            feature.setMemberName(null);
            feature.type = this;
        }
    },
    
    /** 
     * {@code namespace + "." + typeSimpleName + "." + memberName} 
     * make up the fully qualified logical member name
     */
    MEMBER {
        @Override
        void init(final ApplicationFeatureId feature, final String fullyQualifiedName) {
            final int i = fullyQualifiedName.lastIndexOf("#");
            if(i == -1) {
                throw new IllegalArgumentException("Malformed, expected a '#': " + fullyQualifiedName);
            }
            final String className = fullyQualifiedName.substring(0, i);
            final String memberName = fullyQualifiedName.substring(i+1);
            CLASS.init(feature, className);
            feature.setMemberName(memberName);
            feature.type = this;
        }
    };

    public boolean hideClassName() {
        return this == ApplicationFeatureType.PACKAGE;
    }
    
    public boolean hideMember() {
        return this == ApplicationFeatureType.PACKAGE || this == ApplicationFeatureType.CLASS;
    }

    abstract void init(ApplicationFeatureId applicationFeatureId, String fullyQualifiedName);

    public static void ensurePackage(final ApplicationFeatureId feature) {
        if(feature.type != ApplicationFeatureType.PACKAGE) {
            throw new IllegalStateException("Can only be called for a package; " + feature.toString());
        }
    }

    public static void ensurePackageOrClass(final ApplicationFeatureId applicationFeatureId) {
        if(applicationFeatureId.type != ApplicationFeatureType.PACKAGE && applicationFeatureId.type != ApplicationFeatureType.CLASS) {
            throw new IllegalStateException("Can only be called for a package or a class; " + applicationFeatureId.toString());
        }
    }

    public static void ensureClass(final ApplicationFeatureId feature) {
        if(feature.type != ApplicationFeatureType.CLASS) {
            throw new IllegalStateException("Can only be called for a class; " + feature.toString());
        }
    }

    public static void ensureMember(final ApplicationFeatureId feature) {
        if(feature.type != ApplicationFeatureType.MEMBER) {
            throw new IllegalStateException("Can only be called for a member; " + feature.toString());
        }
    }

    @Override
    public String toString() {
        return _Strings.capitalize(name());
    }
    

}
