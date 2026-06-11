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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.apache.causeway.applib.domain.RefData;
import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

class CommandReplayReferenceDataServiceForRefDataTest {

    private static final Bookmark CATEGORY = Bookmark.forLogicalTypeNameAndIdentifier("demo.Category", "STD");
    private static final Bookmark CUSTOMER = Bookmark.forLogicalTypeNameAndIdentifier("demo.Customer", "1");

    @Test
    void classifies_bookmark_as_reference_data_when_logical_type_resolves_to_ref_data_class() {
        final var specificationLoader = specificationLoaderFor(CATEGORY, RefDataCategory.class);
        final var service = new CommandReplayReferenceDataServiceForRefData(specificationLoader);

        final var referenceData = service.isReferenceData(CATEGORY);

        assertThat(referenceData).isTrue();
    }

    @Test
    void does_not_classify_bookmark_as_reference_data_when_logical_type_resolves_to_non_marker_class() {
        final var specificationLoader = specificationLoaderFor(CUSTOMER, Customer.class);
        final var service = new CommandReplayReferenceDataServiceForRefData(specificationLoader);

        final var referenceData = service.isReferenceData(CUSTOMER);

        assertThat(referenceData).isFalse();
    }

    @Test
    void does_not_classify_unknown_logical_type_as_reference_data() {
        final var specificationLoader = mock(SpecificationLoader.class);
        when(specificationLoader.specForBookmark(CATEGORY)).thenReturn(Optional.empty());
        final var service = new CommandReplayReferenceDataServiceForRefData(specificationLoader);

        final var referenceData = service.isReferenceData(CATEGORY);

        assertThat(referenceData).isFalse();
    }

    @Test
    void does_not_classify_null_bookmark_as_reference_data() {
        final var service = new CommandReplayReferenceDataServiceForRefData(mock(SpecificationLoader.class));

        final var referenceData = service.isReferenceData(null);

        assertThat(referenceData).isFalse();
    }

    @Test
    void marker_classifier_composes_with_custom_spi_implementations() {
        final var markerService = new CommandReplayReferenceDataServiceForRefData(specificationLoaderFor(CATEGORY, Customer.class));
        final var services = List.<CommandReplayReferenceDataService>of(
                markerService,
                CATEGORY::equals);

        final var referenceData = CommandReplayReferenceDataService.isReferenceData(services, CATEGORY);

        assertThat(referenceData).isTrue();
    }

    @Test
    void classification_uses_metamodel_type_information_only() {
        final var specificationLoader = mock(SpecificationLoader.class);
        final var objectSpecification = mock(ObjectSpecification.class);
        doReturn(RefDataCategory.class).when(objectSpecification).getCorrespondingClass();
        when(specificationLoader.specForBookmark(CATEGORY)).thenReturn(Optional.of(objectSpecification));
        clearInvocations(specificationLoader, objectSpecification);
        final var service = new CommandReplayReferenceDataServiceForRefData(specificationLoader);

        final var referenceData = service.isReferenceData(CATEGORY);

        assertThat(referenceData).isTrue();
        verify(specificationLoader).specForBookmark(CATEGORY);
        verify(objectSpecification).getCorrespondingClass();
        verifyNoMoreInteractions(specificationLoader, objectSpecification);
    }

    private static SpecificationLoader specificationLoaderFor(
            final Bookmark bookmark,
            final Class<?> correspondingClass) {
        final var specificationLoader = mock(SpecificationLoader.class);
        final var objectSpecification = mock(ObjectSpecification.class);
        doReturn(correspondingClass).when(objectSpecification).getCorrespondingClass();
        when(specificationLoader.specForBookmark(bookmark)).thenReturn(Optional.of(objectSpecification));
        return specificationLoader;
    }

    private static class RefDataCategory implements RefData {
    }

    private static class Customer {
    }
}
