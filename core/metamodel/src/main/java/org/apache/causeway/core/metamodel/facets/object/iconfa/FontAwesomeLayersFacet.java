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
package org.apache.causeway.core.metamodel.facets.object.iconfa;

import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.members.cssclassfa.FontAwesomeLayersProvider;
import org.apache.causeway.core.metamodel.object.ManagedObject;

/**
 * <i>Font Awesome</i> icon for a domain object.
 * <p>
 * The facet is checked each time the object is rendered, allowing the icon to be
 * changed on an instance-by-instance basis. For example, the icon might be adapted
 * with an overlay to represent its state through some well-defined lifecycle (eg
 * pending approval, approved, rejected).
 * <p>
 * In the standard Apache Causeway Programming Model, typically corresponds to a method named {@code iconFaLayers}.
 *
 * @see FontAwesomeLayers
 *
 * @since 2.0
 */
public interface FontAwesomeLayersFacet extends Facet {

    public FontAwesomeLayers layers(final ManagedObject object);

    FontAwesomeLayersProvider getCssClassFaFactory(@Nullable Supplier<ManagedObject> domainObjectProvider);
}
