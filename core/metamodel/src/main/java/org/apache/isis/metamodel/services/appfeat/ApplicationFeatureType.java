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
package org.apache.isis.metamodel.services.appfeat;

import org.apache.isis.core.commons.lang.StringExtensions;

public enum ApplicationFeatureType {
    PACKAGE {
        @Override
        void init(final ApplicationFeatureId feature, final String fullyQualifiedName) {
            feature.setPackageName(fullyQualifiedName);
            feature.setClassName(null);
            feature.setMemberName(null);
            feature.type = this;
        }
    },
    CLASS {
        @Override
        void init(final ApplicationFeatureId feature, final String fullyQualifiedName) {
            final int i = fullyQualifiedName.lastIndexOf(".");
            if(i != -1) {
                feature.setPackageName(fullyQualifiedName.substring(0, i));
                feature.setClassName(fullyQualifiedName.substring(i+1));
            } else {
                feature.setPackageName("");
                feature.setClassName(fullyQualifiedName);
            }
            feature.setMemberName(null);
            feature.type = this;
        }
    },
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

    static void ensurePackage(final ApplicationFeatureId feature) {
        if(feature.type != ApplicationFeatureType.PACKAGE) {
            throw new IllegalStateException("Can only be called for a package; " + feature.toString());
        }
    }

    static void ensurePackageOrClass(final ApplicationFeatureId applicationFeatureId) {
        if(applicationFeatureId.type != ApplicationFeatureType.PACKAGE && applicationFeatureId.type != ApplicationFeatureType.CLASS) {
            throw new IllegalStateException("Can only be called for a package or a class; " + applicationFeatureId.toString());
        }
    }

    static void ensureClass(final ApplicationFeatureId feature) {
        if(feature.type != ApplicationFeatureType.CLASS) {
            throw new IllegalStateException("Can only be called for a class; " + feature.toString());
        }
    }

    static void ensureMember(final ApplicationFeatureId feature) {
        if(feature.type != ApplicationFeatureType.MEMBER) {
            throw new IllegalStateException("Can only be called for a member; " + feature.toString());
        }
    }


    @Override
    public String toString() {
        return StringExtensions.capitalize(name());
    }

}
