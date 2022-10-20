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
package org.apache.causeway.core.metamodel.postprocessors.all.i18n;

import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facets.all.described.MemberDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.described.ObjectDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.described.ParamDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.i8n.HasMemoizableTranslation;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.facets.all.named.ObjectNamedFacet;
import org.apache.causeway.core.metamodel.facets.all.named.ParamNamedFacet;
import org.apache.causeway.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

public class TranslationPostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Inject
    public TranslationPostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    public boolean isEnabled() {
        // force PoWriter to be called to capture text that needs translating
        return super.getMetaModelContext().getTranslationService().getMode().isWrite();
    }

    @Override
    public void postProcessObject(final ObjectSpecification objectSpec) {
        memoizeTranslations(
                Stream.of(
                        objectSpec.lookupFacet(ObjectNamedFacet.class),
                        objectSpec.lookupFacet(ObjectDescribedFacet.class)));
    }

    @Override
    public void postProcessAction(final ObjectSpecification objectSpec, final ObjectAction act) {
        memoizeTranslations(
                Stream.of(
                        act.lookupFacet(MemberNamedFacet.class),
                        act.lookupFacet(MemberDescribedFacet.class)));
    }

    @Override
    public void postProcessParameter(
            final ObjectSpecification objectSpecification,
            final ObjectAction objectAction,
            final ObjectActionParameter param) {
        memoizeTranslations(
                Stream.of(
                        param.lookupFacet(ParamNamedFacet.class),
                        param.lookupFacet(ParamDescribedFacet.class)));
    }

    @Override
    public void postProcessProperty(final ObjectSpecification objectSpec, final OneToOneAssociation prop) {
        memoizeTranslations(
                Stream.of(
                        prop.lookupFacet(MemberNamedFacet.class),
                        prop.lookupFacet(MemberDescribedFacet.class)));
    }

    @Override
    public void postProcessCollection(final ObjectSpecification objectSpec, final OneToManyAssociation coll) {
        memoizeTranslations(
                Stream.of(
                        coll.lookupFacet(MemberNamedFacet.class),
                        coll.lookupFacet(MemberDescribedFacet.class)));
    }

    // -- HELPER

    private void memoizeTranslations(final Stream<Optional<? extends Facet>> facetStream) {
        facetStream
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(facet->facet instanceof HasMemoizableTranslation)
        .map(HasMemoizableTranslation.class::cast)
        .forEach(HasMemoizableTranslation::memoizeTranslations);
    }

}
