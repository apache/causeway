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
package org.apache.causeway.core.metamodel.facets.object.title;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.object.icon.IconFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;

/**
 * Mechanism for obtaining the title of an instance of a class, used to label
 * the instance in the viewer (usually alongside an icon representation).
 *
 * <p>
 * In the standard Apache Causeway Programming Model, typically corresponds to a
 * method named <tt>title</tt>.
 *
 * @see IconFacet
 */
public interface TitleFacet extends Facet {

    /**
     * Provide a title for the target object.
     */
    String title(TitleRenderRequest titleRenderRequest);


    default String title(final ManagedObject targetAdapter) {
        return title(TitleRenderRequest.builder()
                .object(targetAdapter)
                .build());
    }

}
