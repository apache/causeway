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
package org.apache.isis.core.metamodel.postprocessors.all.i18n;


import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

public class TranslationPostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Override
    public boolean isEnabled() {
        // force PoWriter to be called to capture text that needs translating
        return super.getMetaModelContext().getTranslationService().getMode().isWrite();
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification) {
        memoizeTranslations(objectSpecification);
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, ObjectAction act) {
        memoizeTranslations(act);
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, ObjectAction objectAction, ObjectActionParameter param) {
        memoizeTranslations(param);
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, OneToOneAssociation prop) {
        memoizeTranslations(prop);
    }

    @Override
    protected void doPostProcess(ObjectSpecification objectSpecification, OneToManyAssociation coll) {
        memoizeTranslations(coll);

    }

    // -- HELPER

    private void memoizeTranslations(final FacetHolder facetHolder) {
        facetHolder
            .lookupFacet(NamedFacet.class)
            .ifPresent(NamedFacet::translated); // memoize translation

        facetHolder
            .lookupFacet(DescribedAsFacet.class)
            .ifPresent(DescribedAsFacet::translated); // memoize translation

        facetHolder
            .lookupFacet(PluralFacet.class)
            .ifPresent(PluralFacet::translated); // memoize translation
    }


}
