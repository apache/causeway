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
package org.apache.isis.applib.annotation;

/**
 * The available policies as to whether meta-model inspection should process
 * non-annotated members and their supporting methods.
 *
 * @since 2.0 {@index}
 */
public enum MemberAnnotations {

    /**
     * Ignore the value provided by this annotation (meaning that the framework will keep searching, in meta
     * annotations or super-classes/interfaces).
     */
    NOT_SPECIFIED,

    /**
     * Meta-model annotations should be handled as per the default policy
     * configured in <tt>application.properties</tt>.
     * <p>
     * If not configured, then meta-model annotations are optional.
     */
    AS_CONFIGURED,

    /**
     * Enforces member and member-support annotations to be present.
     */
    ENFORCED,

    /**
     * Member and member-support annotations are optional.
     */
    OPTIONAL

    ;

    public boolean isNotSpecified() {
        return this == NOT_SPECIFIED;
    }

    public boolean isAsConfigured() {
        return this == AS_CONFIGURED;
    }

    public boolean isEnforced() {
        return this == ENFORCED;
    }

    public boolean isOptional() {
        return this == OPTIONAL;
    }

    /**
     * Effectively applies on a per class basis, when introspecting
     * meta-model members and member-support methods.
     */
    public static enum MemberAnnotationPolicy {
        MEMBER_ANNOTATIONS_REQUIRED,
        MEMBER_ANNOTATIONS_OPTIONAL;
        public boolean isMemberAnnotationsRequired() {
            return this == MEMBER_ANNOTATIONS_REQUIRED;
        }
    }

}
