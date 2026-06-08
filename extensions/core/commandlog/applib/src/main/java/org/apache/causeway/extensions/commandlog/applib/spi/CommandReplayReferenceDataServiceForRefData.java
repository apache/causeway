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

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import lombok.RequiredArgsConstructor;

/**
 * Default {@link CommandReplayReferenceDataService} that recognizes classes implementing {@link RefData}.
 *
 * @since 3.5 {@index}
 */
@Component
@RequiredArgsConstructor
public class CommandReplayReferenceDataServiceForRefData implements CommandReplayReferenceDataService {

    private final SpecificationLoader specificationLoader;

    @Override
    public boolean isReferenceData(final Bookmark bookmark) {
        return specificationLoader != null
                && bookmark != null
                && specificationLoader.specForBookmark(bookmark)
                .map(objectSpecification -> objectSpecification.getCorrespondingClass())
                .map(RefData.class::isAssignableFrom)
                .orElse(false);
    }
}
