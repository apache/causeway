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
package org.apache.isis.core.metamodel.facets.object.immutable;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;

/**
 * Indicates that the instances of this class are not immutable and so may be
 * modified either through the viewer or indeed programmatically.
 * <p>
 * Overrides at type level the object editing configuration
 * {@link org.apache.isis.core.config.IsisConfiguration.Applib.Annotation.DomainObject#getEditing()}
 *
 * @see ValueFacet
 *
 * @since 2.0
 */
public interface EditingEnabledFacet extends Facet {


}
