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
package org.apache.causeway.extensions.secman.applib.feature.api;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.MinLength;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.causeway.commons.internal.functions._Predicates;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Supports mixins to add and filter permissions of
 * {@link org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole}
 * and {@link org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser}.
 *
 * @since 2.0 {@index}
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApplicationFeatureChoices {

    final ApplicationFeatureRepository featureRepository;

    public static final String DESCRIBED_AS =
            "To refine the search by feature-sort (namespace, type, member), "
            + "use one of "
            + "n: t: m:";

    public Collection<ApplicationFeatureChoices.AppFeat> autoCompleteFeature(
            final @MinLength(3) String search) {

        final Predicate<ApplicationFeatureId> searchRefine;
        final String searchTerm;

        if(search.startsWith("n:")) {
            searchRefine = ApplicationFeatureChoices::isNamespace;
            searchTerm = search.substring(2).trim();
        } else if(search.startsWith("t:")) {
            searchRefine = ApplicationFeatureChoices::isType;
            searchTerm = search.substring(2).trim();
        } else if(search.startsWith("m:")) {
            searchRefine = ApplicationFeatureChoices::isMember;
            searchTerm = search.substring(2).trim();
        } else {
            searchRefine = _Predicates.alwaysTrue();
            searchTerm = search.trim();
        }

        val idsByName = featureRepository.getFeatureIdentifiersByName();

        return idsByName.entrySet().stream()
        .filter(entry->searchRefine.test(entry.getValue()))
        .filter(entry -> entry.getKey().contains(searchTerm))
        .map(Map.Entry::getValue)
        .map(ApplicationFeatureChoices.AppFeat::new)
        .collect(Collectors.toCollection(TreeSet::new));
    }

    private static boolean isNamespace(final ApplicationFeatureId featureId) {
        return featureId.getSort().isNamespace();
    }

    private static boolean isType(final ApplicationFeatureId featureId) {
        return featureId.getSort().isType();
    }

    private static boolean isMember(final ApplicationFeatureId featureId) {
        return featureId.getSort().isMember();
    }


    // -- FEATURE VIEW MODEL WRAPPING A VALUE TYPE

    /**
     * Viewmodel wrapper around value type {@link ApplicationFeatureId}. Introduced,
     * because at the time of writing,
     * autoComplete/choices do not support value types.
     */
    @Named(AppFeat.LOGICAL_TYPE_NAME)
    @DomainObject(
            nature = Nature.VIEW_MODEL)
    @AllArgsConstructor @NoArgsConstructor @EqualsAndHashCode
    public static class AppFeat
    implements
        Comparable<AppFeat>,
        ViewModel {

        public static final String LOGICAL_TYPE_NAME = CausewayModuleExtSecmanApplib.NAMESPACE + ".AppFeat";

        @Property
        @Getter
        private ApplicationFeatureId featureId;

        @ObjectSupport public String title() {
            return toString();
        }

        @Override
        public int compareTo(final AppFeat o) {
            val thisId = this.getFeatureId();
            val otherId = o!=null ? o.getFeatureId() : null;
            if(Objects.equals(thisId, otherId)) {
                return 0;
            }
            if(thisId==null) {
                return -1;
            }
            if(otherId==null) {
                return 1;
            }
            return this.getFeatureId().compareTo(o.getFeatureId());
        }

        @Override
        public String toString() {
            return featureId!=null
                    ? featureId.getSort().name() + ": " + featureId.getFullyQualifiedName()
                    : "<no id>";
        }

        // -- VIEWMODEL CONTRACT

        @Inject
        public AppFeat(final String memento) {
            this(ApplicationFeatureId.parseEncoded(memento)); // fail by intention if memento is '<no id>'
        }

        @Override
        public String viewModelMemento() {
            return featureId!=null
                    ? featureId.asEncodedString()
                    : "<no id>";
        }

    }

}
