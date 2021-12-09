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
package org.apache.isis.core.metamodel.facets.object.recreatable;

import java.util.Optional;

import org.apache.isis.applib.ViewModel;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.HasPostConstructMethodCache;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.NonNull;

public class RecreatableObjectFacetForRecreatableObjectInterface
extends RecreatableObjectFacetAbstract {

    public RecreatableObjectFacetForRecreatableObjectInterface(
            final FacetHolder holder,
            final HasPostConstructMethodCache postConstructMethodCache) {
        super(holder, RecreationMechanism.INITIALIZES, postConstructMethodCache);
    }

    @Override
    protected void doInitialize(final Object pojo, final @NonNull Optional<Bookmark> bookmark) {
        final ViewModel viewModel = (ViewModel) pojo;
        viewModel.viewModelInit(bookmark.map(Bookmark::getIdentifier).orElse(null));
    }

    @Override
    public String serialize(final ManagedObject viewModel) {
        final ViewModel viewModelPojo = (ViewModel) viewModel.getPojo();
        return viewModelPojo.viewModelMemento();
    }
}
