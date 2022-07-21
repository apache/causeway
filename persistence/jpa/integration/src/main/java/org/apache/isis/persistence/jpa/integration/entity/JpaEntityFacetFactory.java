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
package org.apache.isis.persistence.jpa.integration.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.persistence.Entity;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.graph.tree.TreeState;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.bookmark.IdStringifier;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.memento._Mementos;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JpaEntityFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public JpaEntityFacetFactory(final MetaModelContext mmc) {
        super(mmc, ImmutableEnumSet.of(FeatureType.OBJECT));
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();

        val facetHolder = processClassContext.getFacetHolder();

        val entityAnnotation = processClassContext.synthesizeOnType(Entity.class);
        if(!entityAnnotation.isPresent()) {
            return;
        }

        addFacet(
                new JpaEntityFacet(facetHolder, cls, getServiceRegistry()));
    }

    // --

    // -- HELPER - OBJECT ID SERIALIZATION


}
