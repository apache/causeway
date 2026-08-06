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
package org.apache.causeway.extensions.commandlog.applib.spi;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import org.apache.causeway.applib.domain.RefData;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class CommandReplayReferenceDataServiceForRefDataTest {

    private static final Bookmark CATEGORY =
            Bookmark.forLogicalTypeNameAndIdentifier("demo.Category", "STD");
    private static final Bookmark CUSTOMER =
            Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", "1");

    @Test
    void markerAndNonMarkerClassesAreClassifiedConservatively() {
        assertThat(serviceFor(CATEGORY, RefDataCategory.class).isReferenceData(CATEGORY)).isTrue();
        assertThat(serviceFor(CUSTOMER, Customer.class).isReferenceData(CUSTOMER)).isFalse();
    }

    @Test
    void unknownTypeAndNullBookmarkAreNotReferenceData() {
        var specificationLoader = mock(SpecificationLoader.class);
        when(specificationLoader.specForBookmark(CATEGORY)).thenReturn(Optional.empty());
        var service = new CommandReplayReferenceDataServiceForRefData(specificationLoader);

        assertThat(service.isReferenceData(CATEGORY)).isFalse();
        assertThat(service.isReferenceData(null)).isFalse();
    }

    @Test
    void defaultClassifierComposesWithCustomClassifiers() {
        var markerService = serviceFor(CATEGORY, Customer.class);

        assertThat(CommandReplayReferenceDataService.isReferenceData(
                List.<CommandReplayReferenceDataService>of(markerService, CATEGORY::equals), CATEGORY)).isTrue();
    }

    @Test
    void classificationUsesMetamodelTypeInformationOnly() {
        var specificationLoader = mock(SpecificationLoader.class);
        var objectSpecification = mock(ObjectSpecification.class);
        doReturn(RefDataCategory.class).when(objectSpecification).getCorrespondingClass();
        when(specificationLoader.specForBookmark(CATEGORY)).thenReturn(Optional.of(objectSpecification));
        clearInvocations(specificationLoader, objectSpecification);
        var service = new CommandReplayReferenceDataServiceForRefData(specificationLoader);

        assertThat(service.isReferenceData(CATEGORY)).isTrue();
        verify(specificationLoader).specForBookmark(CATEGORY);
        verify(objectSpecification).getCorrespondingClass();
        verifyNoMoreInteractions(specificationLoader, objectSpecification);
    }

    @Test
    void defaultClassifierIsRegisteredByCommandlogModule() {
        var registered = List.of(CausewayModuleExtCommandLogApplib.class
                .getAnnotation(Import.class).value());

        assertThat(registered).contains(CommandReplayReferenceDataServiceForRefData.class);
    }

    private static CommandReplayReferenceDataServiceForRefData serviceFor(
            final Bookmark bookmark,
            final Class<?> correspondingClass) {
        var specificationLoader = mock(SpecificationLoader.class);
        var objectSpecification = mock(ObjectSpecification.class);
        doReturn(correspondingClass).when(objectSpecification).getCorrespondingClass();
        when(specificationLoader.specForBookmark(bookmark)).thenReturn(Optional.of(objectSpecification));
        return new CommandReplayReferenceDataServiceForRefData(specificationLoader);
    }

    private static class RefDataCategory implements RefData {
    }

    private static class Customer {
    }
}
