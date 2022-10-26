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
package org.apache.causeway.applib.annotation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The available policies as to how class introspection should process
 * members, supporting methods and callback methods.
 * <p>
 * Particularly whether to consider or ignore non-public members as contributors
 * to the meta-model (encapsulation).
 * And also whether or not to enforce presence of the {@link Domain.Include}
 * annotation on those members.
 *
 * @since 2.0 {@index}
 */
public enum Introspection {

    /**
     * Ignore the value provided by this annotation (meaning that the framework will keep searching, in meta
     * annotations or super-classes/interfaces).
     */
    NOT_SPECIFIED,

    /**
     * Introspection should be handled as per the default introspection policy
     * configured in <tt>application.properties</tt>.
     * <p>
     * If not configured, then encapsulation is disabled
     * and presence of the {@link Domain.Include} annotation is not enforced.
     */
    AS_CONFIGURED,

    /**
     * Introspect public and non-public members.
     *
     * <p>
     *      All methods intended to be part of the meta-model
     *      (whether representing a member or a supporting method) must be annotated.
     *      Members using one of {@link Action}, {@link Property}, {@link Collection},
     *      while supporting methods with {@link Domain.Include}
     *      (usually as a meta-annotation on {@link MemberSupport}).
     *      However, the methods can have any visibility, including private.
     * </p>
     *
     * <p>
     *     For mixins (where the mixin class itself is annotated with {@link Action}, {@link Property} or
     *     {@link Collection}, then the member method should instead be annotated the same as the supporting
     *     methods, {@link Domain.Include} or {@link MemberSupport}.
     * </p>
     */
    ENCAPSULATION_ENABLED,

    /**
     * Introspect public members only, while
     * presence of at least one appropriate domain annotation is enforced.
     *
     * <p>
     * All public methods intended to represent members must be annotated (or meta-annotated) with
     * {@link Action}, {@link Property} or {@link Collection}.
     * </p>
     *
     * <p>
     * Any non-excluded public methods with a supporting method prefix
     * do not need to be annotated and
     * are automatically associated with their corresponding member method.
     * If no corresponding member method can be found, meta-model validation will
     * fail with an 'orphaned member support' method violation.
     * </p>
     *
     * <p>
     *     For mixins (where the mixin class itself is annotated with {@link Action}, {@link Property} or
     *     {@link Collection}, then the member method should instead be annotated the same as the supporting
     *     methods, {@link Domain.Include} or {@link MemberSupport}.
     * </p>
     */
    ANNOTATION_REQUIRED,

    /**
     * Introspect public members only, while
     * presence of domain annotations is optional.
     *
     * <p>
     * All public methods are considered as part of the meta-model,
     * unless explicitly excluded using {@link Domain.Exclude}
     * (usually as a meta-annotation on {@link Programmatic}).
     * </p>
     *
     * <p>
     * Any non-excluded public methods with a supporting method prefix
     * do not need to be annotated and
     * are automatically associated with their corresponding member method.
     * If no corresponding member method can be found, meta-model validation will
     * fail with an 'orphaned member support' method violation.
     * </p>
     */
    ANNOTATION_OPTIONAL,

    ;

    public boolean isNotSpecified() {
        return this == NOT_SPECIFIED;
    }

    public boolean isAsConfigured() {
        return this == AS_CONFIGURED;
    }

    /**
     * Effectively applies on a per class basis, when introspecting
     * meta-model members and member-support methods.
     */
    @Getter
    @RequiredArgsConstructor
    public static enum IntrospectionPolicy {
        /**
         * Introspect public and non-public members.
         *
         * <p>
         *     All methods must be annotated.  Typically members will be annotated using one of {@link Action},
         *     {@link Property}, {@link Collection}, and supporting methods with either {@link Domain.Include} or
         *     the equivalent meta-annotation, {@link MemberSupport}.
         * </p>
         *
         * <p>
         *     For mixins (where the mixin class itself is annotated with {@link Action}, {@link Property} or
         *     {@link Collection}, then the member method should instead be annotated the same as the supporting
         *     methods, {@link Domain.Include} or {@link MemberSupport}.
         * </p>
         */
        ENCAPSULATION_ENABLED(
                EncapsulationPolicy.ENCAPSULATED_MEMBERS_SUPPORTED,
                MemberAnnotationPolicy.MEMBER_ANNOTATIONS_REQUIRED,
                SupportMethodAnnotationPolicy.SUPPORT_METHOD_ANNOTATIONS_REQUIRED),

        /**
         * Introspect public members only, requiring the member to be annotated using an appropriate domain annotation
         * {@link Action}, {@link Property}, {@link Collection} is required.
         *
         * <p>
         *     Supporting methods do <i>not</i> need to be annotated.
         * </p>
         *
         * <p>
         *     For mixins (where the mixin class itself is annotated with {@link Action}, {@link Property} or
         *     {@link Collection}, then the member method should instead be annotated the same as the supporting
         *     methods, {@link Domain.Include} or {@link MemberSupport}.
         * </p>
         */
        ANNOTATION_REQUIRED(
                EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED,
                MemberAnnotationPolicy.MEMBER_ANNOTATIONS_REQUIRED,
                SupportMethodAnnotationPolicy.SUPPORT_METHOD_ANNOTATIONS_OPTIONAL),

        /**
         * Introspect public members only; neither the member not supporting methods need be annotated.
         */
        ANNOTATION_OPTIONAL(
                EncapsulationPolicy.ONLY_PUBLIC_MEMBERS_SUPPORTED,
                MemberAnnotationPolicy.MEMBER_ANNOTATIONS_OPTIONAL,
                SupportMethodAnnotationPolicy.SUPPORT_METHOD_ANNOTATIONS_OPTIONAL),
        ;

        private final EncapsulationPolicy encapsulationPolicy;
        private final MemberAnnotationPolicy memberAnnotationPolicy;
        private final SupportMethodAnnotationPolicy supportMethodAnnotationPolicy;

    }


    /**
     * Effectively applies on a per class basis, when introspecting
     * meta-model members and member-support methods.
     */
    public static enum EncapsulationPolicy {
        ONLY_PUBLIC_MEMBERS_SUPPORTED,
        ENCAPSULATED_MEMBERS_SUPPORTED;
        public boolean isEncapsulatedMembersSupported() {
            return this == ENCAPSULATED_MEMBERS_SUPPORTED;
        }
    }

    /**
     * Effectively applies on a per class basis, when introspecting
     * class members for the meta-model.
     */
    public static enum MemberAnnotationPolicy {
        MEMBER_ANNOTATIONS_REQUIRED,
        MEMBER_ANNOTATIONS_OPTIONAL;
        public boolean isMemberAnnotationsRequired() {
            return this == MEMBER_ANNOTATIONS_REQUIRED;
        }
    }

    /**
     * Effectively applies on a per class basis, when introspecting
     * class (object- and member-) support methods for the meta-model.
     */
    public static enum SupportMethodAnnotationPolicy {
        SUPPORT_METHOD_ANNOTATIONS_REQUIRED,
        SUPPORT_METHOD_ANNOTATIONS_OPTIONAL;
        public boolean isSupportMethodAnnotationsRequired() {
            return this == SUPPORT_METHOD_ANNOTATIONS_REQUIRED;
        }
    }

}
