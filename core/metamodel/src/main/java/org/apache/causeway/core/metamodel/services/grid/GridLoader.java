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
package org.apache.causeway.core.metamodel.services.grid;

import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.layout.resource.LayoutResource;
import org.apache.causeway.applib.services.grid.GridService.LayoutKey;
import org.apache.causeway.commons.functional.Try;

import lombok.extern.slf4j.Slf4j;

@Slf4j
record GridLoader(
        GridLoadingContext gridLoadingContext) {

    /**
     * Optionally returns a new instance of a {@link BSGrid},
     * based on whether the underlying resource could be found, loaded and parsed.
     *
     * <p>The layout alternative will typically be specified through a
     * `layout()` method on the domain object, the value of which is used
     * for the suffix of the layout file (eg "Customer-layout.archived.xml"
     * to use a different layout for customers that have been archived).
     */
    public Try<BSGrid> tryLoad(final LayoutKey layoutKey, final LayoutResource layoutResource) {
        return gridLoadingContext.gridMarshaller(layoutResource.format())
            .orElseThrow()
            .unmarshal(layoutKey.domainClass(), layoutResource.content(), layoutResource.format());
    }

}
