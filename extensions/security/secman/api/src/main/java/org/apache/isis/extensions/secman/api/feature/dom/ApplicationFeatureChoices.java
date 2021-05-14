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
package org.apache.isis.extensions.secman.api.feature.dom;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.commons.internal.functions._Predicates;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Supports mixins to add and filter permissions of
 * {@link org.apache.isis.extensions.secman.api.role.dom.ApplicationRole}
 * and {@link org.apache.isis.extensions.secman.api.user.dom.ApplicationUser}.
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ApplicationFeatureChoices {

    final ApplicationFeatureRepository featureRepository;

    public static final String DESCRIBED_AS = "To refine the search by feature-sort (namespace, type, member), "
            + "use one of "
            + "sort:n sort:t sort:m.";

    public Collection<ApplicationFeatureChoices.AppFeat> autoCompleteFeature(
            final @MinLength(3) String search) {

        final Predicate<ApplicationFeatureId> searchRefine;
        final String searchTerm;

        if(search.startsWith("sort:n")) {
            searchRefine = ApplicationFeatureChoices::isNamespace;
            searchTerm = search.substring(6).trim();
        } else if(search.startsWith("sort:t")) {
            searchRefine = ApplicationFeatureChoices::isType;
            searchTerm = search.substring(6).trim();
        } else if(search.startsWith("sort:m")) {
            searchRefine = ApplicationFeatureChoices::isMember;
            searchTerm = search.substring(6).trim();
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

    private static boolean isNamespace(ApplicationFeatureId featureId) {
        return featureId.getSort().isNamespace();
    }

    private static boolean isType(ApplicationFeatureId featureId) {
        return featureId.getSort().isType();
    }

    private static boolean isMember(ApplicationFeatureId featureId) {
        return featureId.getSort().isMember();
    }


    // -- FEATURE VIEW MODEL WRAPPING A VALUE TYPE

    /**
     * Viewmodel wrapper around value type {@link ApplicationFeatureId}. Introduced,
     * because at the time of writing,
     * autoComplete/choices do not support value types.
     */
    @DomainObject(
            nature = Nature.VIEW_MODEL,
            objectType = AppFeat.OBJECT_TYPE
    )
    @AllArgsConstructor @NoArgsConstructor @EqualsAndHashCode
    public static class AppFeat
    implements
        Comparable<AppFeat>,
        ViewModel {

        public static final String OBJECT_TYPE = IsisModuleExtSecmanApi.NAMESPACE + ".AppFeat";

        @Property
        @Getter
        private ApplicationFeatureId featureId;

        public String title() {
            return toString();
        }

        @Override
        public int compareTo(AppFeat o) {
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

        @Override
        public String viewModelMemento() {
            return featureId!=null
                    ? featureId.asEncodedString()
                    : "<no id>";
        }

        @Override
        public void viewModelInit(String memento) {
            featureId = ApplicationFeatureId.parseEncoded(memento); // fail by intention if memento is '<no id>'
        }

    }

}
