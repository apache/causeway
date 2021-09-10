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
package org.apache.isis.viewer.wicket.ui.components.collectioncontents.summary;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactoryAbstract;
import org.apache.isis.viewer.wicket.ui.ComponentType;

/**
 * {@link ComponentFactory} for {@link CollectionContentsAsSummary}.
 */
public class CollectionContentsAsSummaryFactory extends ComponentFactoryAbstract implements CollectionContentsAsFactory {

    private static final long serialVersionUID = 1L;

    private static final String NAME = "summary";

    static final Predicate<ObjectAssociation> OF_TYPE_BIGDECIMAL = (final ObjectAssociation objectAssoc) -> {
        final ObjectSpecification objectSpec = objectAssoc.getSpecification();
        return objectSpec.containsNonFallbackFacet(BigDecimalValueFacet.class);
    };

    // //////////////////////////////////////

    public CollectionContentsAsSummaryFactory() {
        super(ComponentType.COLLECTION_CONTENTS, NAME, CollectionContentsAsSummary.class);
    }

    @Override
    public ApplicationAdvice appliesTo(final IModel<?> model) {
        if(!(model instanceof EntityCollectionModel)) {
            return ApplicationAdvice.DOES_NOT_APPLY;
        }
        final EntityCollectionModel entityCollectionModel = (EntityCollectionModel) model;
        final ObjectSpecification elementSpec = entityCollectionModel.getTypeOfSpecification();
        final Stream<ObjectAssociation> associations = elementSpec.streamAssociations(MixedIn.EXCLUDED);

        return appliesIf(associations.anyMatch(OF_TYPE_BIGDECIMAL));
    }

    @Override
    public Component createComponent(final String id, final IModel<?> model) {
        final EntityCollectionModel collectionModel = (EntityCollectionModel) model;
        return new CollectionContentsAsSummary(id, collectionModel);
    }

    @Override
    public CssResourceReference getCssResourceReference() {
        // do not bundle, because of relative CSS images...
        return null;
    }

    @Override
    public IModel<String> getTitleLabel() {
        return new ResourceModel("CollectionContentsAsSummaryFactory.Summary", "Summary");
    }

    @Override
    public IModel<String> getCssClass() {
        return Model.of("fa fa-fw fa-usd");
    }

}
