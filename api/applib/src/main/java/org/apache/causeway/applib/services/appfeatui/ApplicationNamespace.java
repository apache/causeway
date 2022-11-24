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
package org.apache.causeway.applib.services.appfeatui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.SortedSet;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureSort;

/**
 * @since 2.x  {@index}
 */
@Named(ApplicationNamespace.LOGICAL_TYPE_NAME)
@DomainObject
@DomainObjectLayout(paged=100)
public class ApplicationNamespace extends ApplicationFeatureViewModel {

    static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE_FEAT + ".ApplicationNamespace";

    public static abstract class CollectionDomainEvent<T> extends ApplicationFeatureViewModel.CollectionDomainEvent<ApplicationNamespace, T> {}

    // -- CONSTRUCTORS

    public ApplicationNamespace() { }
    public ApplicationNamespace(final ApplicationFeatureId featureId) {
        super(featureId);
    }
    @Inject
    public ApplicationNamespace(final String memento) {
        super(memento);
    }

    // -- CONTENTS (collection, for namespaces only)

    @Collection(
            domainEvent = Contents.DomainEvent.class
    )
    @CollectionLayout(
            defaultView="table",
            sequence = "4"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Contents {
        class DomainEvent extends CollectionDomainEvent<ApplicationFeatureViewModel> {}
    }

    @Contents
    public List<ApplicationFeatureViewModel> getContents() {
        final SortedSet<ApplicationFeatureId> contents = getFeature().getContents();
        return asViewModels(contents, ApplicationFeatureViewModel.class);
    }
    @MemberSupport public boolean hideContents() {
        return getSort() != ApplicationFeatureSort.NAMESPACE;
    }


}
