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
package org.apache.causeway.core.metamodel.services.title;

import javax.inject.Inject;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.commons.internal.base._Blackhole;
import org.apache.causeway.core.config.messages.MessageRegistry;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;

/**
 *
 * @since 2.0
 *
 */
public class TitlesAndTranslationsValidator
extends MetaModelValidatorAbstract {

    @Inject
    public TitlesAndTranslationsValidator(final MetaModelContext mmc) {
        super(mmc, MetaModelValidator.NONE);
    }

    @Override
    public void validateEnter() {
        validateServiceTitles();
        validateEnumTitles();
        validateRegisteredMessageTranslation();
    }

    private void validateServiceTitles() {

        var serviceRegistry = getServiceRegistry();
        var specificationLoader = getSpecificationLoader();
        var titleService = getTitleService();

        serviceRegistry.streamRegisteredBeans()
        .forEach(managedBeanAdapter->{

            var domainService = managedBeanAdapter.lookupInstance().orElse(null);
            var logicalTypeName = managedBeanAdapter.getId();

            if(domainService == null) {

                var deficiencyOrigin = Identifier.classIdentifier(
                        LogicalType.eager(managedBeanAdapter.getBeanClass(), logicalTypeName));

                ValidationFailure.raise(
                        specificationLoader,
                        deficiencyOrigin,
                        String.format(
                                "Failed to get instance of service bean %s",
                                managedBeanAdapter.getId())
                        );
                return; // next
            }

            try {

                var title = titleService.titleOf(domainService);
                _Blackhole.consume(title);

            } catch (Exception e) {

                e.printStackTrace();

                var deficiencyOrigin = Identifier.classIdentifier(
                        LogicalType.eager(managedBeanAdapter.getBeanClass(), logicalTypeName));

                ValidationFailure.raise(
                        specificationLoader,
                        deficiencyOrigin,
                        String.format(
                                "Failed to get title for service bean %s",
                                managedBeanAdapter.getId())
                        );
            }

        });
    }

    private void validateEnumTitles() {

        var specificationLoader = getSpecificationLoader();
        var titleService = getTitleService();

        // (previously we took a protective copy to avoid a concurrent modification exception,
        // but this is now done by SpecificationLoader itself)
        for (var objSpec : specificationLoader.snapshotSpecifications()) {
            var correspondingClass = objSpec.getCorrespondingClass();
            if(correspondingClass.isEnum()) {
                final Object[] enumConstants = correspondingClass.getEnumConstants();
                for (Object enumConstant : enumConstants) {

                    try {

                        var title = titleService.titleOf(enumConstant);
                        _Blackhole.consume(title);

                    } catch (Exception e) {

                        var deficiencyOrigin = Identifier.classIdentifier(objSpec.getLogicalType());
                        var facetHolder = objSpec;

                        ValidationFailure.raise(
                                facetHolder.getSpecificationLoader(),
                                deficiencyOrigin,
                                String.format(
                                        "Failed to get title for enum constant %s",
                                        "" + enumConstant)
                                );
                    }

                }
            }
        }
    }

    private void validateRegisteredMessageTranslation() {

        var specificationLoader = getSpecificationLoader();
        var translationService = getTranslationService();

        // see @ConfirmUiModel#translate()
        var translationContext = TranslationContext.forClassName(MessageRegistry.class);

        var messageRegistry = new MessageRegistry();
        for (String message : messageRegistry.listMessages()) {

            try {

                var translatedMessage = translationService.translate(translationContext, message);
                _Blackhole.consume(translatedMessage);

            } catch (Exception e) {

                var spec = specificationLoader.specForTypeElseFail(MessageRegistry.class);
                var deficiencyOrigin = Identifier.classIdentifier(spec.getLogicalType());

                ValidationFailure.raise(
                        spec.getSpecificationLoader(),
                        deficiencyOrigin,
                        String.format(
                                "Failed to translate message %s from MessageRegistry",
                                "" + message)
                        );
            }

        }

    }

}
