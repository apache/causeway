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
package org.apache.isis.core.metamodel.methods;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.config.progmodel.ProgrammingModelConstants;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectMemberAbstract;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelVisitingValidatorAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

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

        if(orphanValidation == OrphanValidation.DONT_VALIDATE) {
            return;
        }

        val noParamsOnly = getConfiguration().getCore().getMetaModel().getValidator().isNoParamsOnly();

        programmingModel
        .addValidator(new MetaModelVisitingValidatorAbstract(programmingModel.getMetaModelContext()) {

            @Override
            public String toString() {
                return "MetaModelValidatorVisiting.Visitor : MethodPrefixBasedFacetFactoryAbstract : " +
                        prefixes.toList().toString();
            }

            @Override
            public void validate(final ObjectSpecification spec) {

                if(spec.isManagedBean()) {
                    return;
                }

                if(spec instanceof ObjectSpecificationAbstract
                        && ((ObjectSpecificationAbstract)spec).getIntrospectionPolicy()
                            .getMemberAnnotationPolicy().isMemberAnnotationsRequired()) {
                    return; // skip orphaned method validation if annotations are required
                }


                // as an optimization only checking declared members (skipping inherited ones)

                // ensure accepted actions do not have any of the reserved prefixes
                spec.streamDeclaredActions(MixedIn.EXCLUDED)
                .forEach(objectAction -> {

                    if(((ObjectMemberAbstract)objectAction).isExplicitlyAnnotated()) {
                        return; // thats always allowed, check next
                    }

                    val actionId = objectAction.getId();

                    for (val prefix : prefixes) {

                        if (isPrefixed(actionId, prefix)) {

                            val explanation =
                                    objectAction.getParameterCount() > 0
                                            && noParamsOnly
                                            && (ProgrammingModelConstants.HIDE_PREFIX.equals(prefix)
                                                    || ProgrammingModelConstants.DISABLE_PREFIX.equals(prefix))
                                            ? " (such methods must have no parameters, '"
                                                + "isis.core.meta-model.validator.no-params-only"
                                                + "' config property)"
                                            : "";

                            val messageFormat = "%s#%s: has prefix %s, is probably intended as a supporting method "
                                    + "for a property, collection or action%s.  If the method is intended to "
                                    + "be an action, then rename and use @ActionLayout(named=\"...\") or ignore "
                                    + "completely using @Programmatic";

                            ValidationFailure.raise(
                                    spec,
                                    String.format(
                                            messageFormat,
                                            spec.getFeatureIdentifier().getClassName(),
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
                || (
                        processMethodContext.getFeatureType()!=null // null check, yet to support some JUnit tests
                        && processMethodContext.getFeatureType().isProperty()
                   );
    }

    // -- HELPER

    private static boolean isPrefixed(final String actionId, final String prefix) {
        return actionId.startsWith(prefix) && actionId.length() > prefix.length();
    }



}
