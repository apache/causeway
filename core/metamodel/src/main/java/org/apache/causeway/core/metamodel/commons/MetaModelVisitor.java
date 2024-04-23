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
package org.apache.causeway.core.metamodel.commons;

import java.util.function.Predicate;

import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;

/**
 * Base for visiting the meta-model.
 */
public interface MetaModelVisitor {

    /** whether this validator should run at all; eg. could be disabled via configuration */
    default boolean isEnabled() {
        return true;
    }

    /** this validator only processes {@link ObjectSpecification}(s) that pass this filter */
    Predicate<ObjectSpecification> getFilter();

    // -- PREDEFINED FILTERS

    /** all types pass this filter */
    public final static Predicate<ObjectSpecification> ALL = __->true;

    /** no types pass this filter */
    public final static Predicate<ObjectSpecification> NONE = __->false;

    /** types pass this filter, if not-injectable (aka not managed by Spring) */
    public final static Predicate<ObjectSpecification> SKIP_MANAGED_BEANS =
            spec->!spec.isInjectable();

    /**
     * non-abstract types pass this filter, if introspect-able
     */
    public final static Predicate<ObjectSpecification> SKIP_ABSTRACT =
            spec->!spec.isAbstract()
                && spec.getBeanSort().isToBeIntrospected();

    /** types pass this filter, if is NOT a mixin */
    public final static Predicate<ObjectSpecification> SKIP_MIXINS =
            spec->!spec.isMixin();

    /** types pass this filter, if IS a mixin */
    public final static Predicate<ObjectSpecification> MIXINS =
            spec->spec.isMixin();

    /** types pass this filter, if either not {@link ObjectSpecificationAbstract} or member-annotation is not required */
    public final static Predicate<ObjectSpecification> SKIP_WHEN_MEMBER_ANNOT_REQUIRED =
            spec->(!(spec instanceof ObjectSpecificationAbstract)
                    || !((ObjectSpecificationAbstract)spec)
                    .getIntrospectionPolicy()
                    .getMemberAnnotationPolicy()
                    .isMemberAnnotationsRequired());

}
