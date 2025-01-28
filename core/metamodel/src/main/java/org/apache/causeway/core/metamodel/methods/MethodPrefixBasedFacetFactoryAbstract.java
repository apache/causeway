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
package org.apache.causeway.core.metamodel.methods;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.MemberSupportPrefix;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

import lombok.Getter;
import org.jspecify.annotations.NonNull;

public abstract class MethodPrefixBasedFacetFactoryAbstract
extends FacetFactoryAbstract
implements MethodPrefixBasedFacetFactory {

    @Getter(onMethod = @__(@Override))
    private final Can<String> prefixes;

    private final OrphanValidation orphanValidation;

    protected enum OrphanValidation {
        VALIDATE,
        DONT_VALIDATE
    }

    protected MethodPrefixBasedFacetFactoryAbstract(
            final @NonNull MetaModelContext mmc,
            final @NonNull ImmutableEnumSet<FeatureType> featureTypes,
            final @NonNull OrphanValidation orphanValidation,
            final @NonNull Can<String> prefixes) {

        super(mmc, featureTypes);
        this.orphanValidation = orphanValidation;
        this.prefixes = prefixes;
    }

    // -- PROGRAMMING MODEL

    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {

        if(orphanValidation == OrphanValidation.DONT_VALIDATE) return;

        programmingModel
        .addValidator(new MetaModelValidatorAbstract(getMetaModelContext(),
                MetaModelValidator.SKIP_MANAGED_BEANS
                // skip orphaned method validation if annotations are required
                .and(MetaModelValidator.SKIP_WHEN_MEMBER_ANNOT_REQUIRED)) {

            @Override
            public String toString() {
                return "MetaModelValidatorVisiting.Visitor : MethodPrefixBasedFacetFactoryAbstract : " +
                        prefixes.toList().toString();
            }

            @Override
            public void validateObjectEnter(final ObjectSpecification spec) {

                // as an optimization only checking declared members (skipping inherited ones)

                // ensure accepted actions do not have any of the reserved prefixes
                spec.streamDeclaredActions(MixedIn.EXCLUDED)
                .filter(objectAction -> !objectAction.isExplicitlyAnnotated()) // explicit annot. is always allowed, check next
                .forEach(objectAction -> {

                    var actionId = objectAction.getId();

                    for (var prefix : prefixes) {

                        if (isPrefixed(actionId, prefix)) {

                            var explanation =
                                    objectAction.getParameterCount() > 0
                                            && (MemberSupportPrefix.HIDE.getMethodNamePrefixes().contains(prefix)
                                                    || MemberSupportPrefix.DISABLE.getMethodNamePrefixes().contains(prefix))
                                            ? " (such methods must not have parameters, '"
                                                + "causeway.core.meta-model.validator.no-params-only"
                                                + "' config property)"
                                            : "";

                            var messageFormat = "%s#%s: has prefix %s, is probably intended as a supporting method "
                                    + "for a property, collection or action%s.  If the method is intended to "
                                    + "be an action, then rename and use @ActionLayout(named=\"...\") or ignore "
                                    + "completely using @Programmatic";

                            ValidationFailure.raise(
                                    spec,
                                    String.format(
                                            messageFormat,
                                            spec.getFeatureIdentifier().className(),
                                            actionId,
                                            prefix,
                                            explanation));
                        }
                    }
                });

            }
        });
    }

    protected boolean isPropertyOrMixinMain(final ProcessMethodContext processMethodContext) {
        return processMethodContext.isMixinMain()
                || (processMethodContext.getFeatureType()!=null // null check, yet to support some JUnit tests
                        && processMethodContext.getFeatureType().isProperty());
    }

    protected boolean isCollectionOrMixinMain(final ProcessMethodContext processMethodContext) {
        return processMethodContext.isMixinMain()
                || (processMethodContext.getFeatureType()!=null // null check, yet to support some JUnit tests
                        && processMethodContext.getFeatureType().isCollection());
    }

    // -- HELPER

    private static boolean isPrefixed(final String actionId, final String prefix) {
        return actionId.startsWith(prefix) && actionId.length() > prefix.length();
    }

}
