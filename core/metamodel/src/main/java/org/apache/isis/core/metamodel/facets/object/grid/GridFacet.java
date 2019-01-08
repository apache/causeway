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
package org.apache.isis.core.metamodel.facets.object.grid;

import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.services.grid.GridSystemService;
import org.apache.isis.applib.services.layout.LayoutService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;

/**
 * Obtain the current grid, derived either from a <code>.layout.xml</code> file, and normalized, or synthesized from
 * existing layout metadata (annotations or <code>layout.json</code>).
 *
 * <p>
 *     Most of the heavy lifting is done by delegating to the {@link LayoutService} and {@link GridSystemService}
 *     services.
 * </p>
 */
public interface GridFacet extends Facet {

    Grid getGrid(final ObjectAdapter objectAdapter);


}