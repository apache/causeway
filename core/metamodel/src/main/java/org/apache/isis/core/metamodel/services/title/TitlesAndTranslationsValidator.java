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
package org.apache.isis.core.metamodel.services.title;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.core.commons.internal.base._Blackhole;
import org.apache.isis.core.config.messages.MessageRegistry;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorAbstract;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

import lombok.NonNull;
import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
public class TitlesAndTranslationsValidator extends MetaModelValidatorAbstract {

    @Override
    public void collectFailuresInto(@NonNull ValidationFailures validationFailures) {
        
        validateServiceTitles();
        validateEnumTitles();
        validateRegisteredMessageTranslation();
        
        super.collectFailuresInto(validationFailures);
    }

    private void validateServiceTitles() {

        val serviceRegistry = super.getMetaModelContext().getServiceRegistry();
        val specificationLoader = super.getMetaModelContext().getSpecificationLoader();
        val titleService = serviceRegistry.lookupServiceElseFail(TitleService.class);

        serviceRegistry.streamRegisteredBeans()
        .forEach(managedBeanAdapter->{

            val serviceInstanceIfAny = managedBeanAdapter.getInstance().getFirst();
            val domainService = serviceInstanceIfAny.orElse(null);
            
            if(domainService == null) {
                
                val deficiencyOrigin = Identifier.classIdentifier(managedBeanAdapter.getBeanClass());
                val facetHolder = specificationLoader.loadSpecification(managedBeanAdapter.getBeanClass());
                
                super.onFailure(
                        facetHolder, 
                        deficiencyOrigin, 
                        "Failed to get instance of service bean %s", 
                        managedBeanAdapter.getId());
                return; // next
            }

            try {

                val title = titleService.titleOf(domainService);
                _Blackhole.consume(title);

            } catch (Exception e) {

                e.printStackTrace();
                
                val deficiencyOrigin = Identifier.classIdentifier(managedBeanAdapter.getBeanClass());
                val facetHolder = specificationLoader.loadSpecification(managedBeanAdapter.getBeanClass());

                super.onFailure(
                        facetHolder, 
                        deficiencyOrigin, 
                        "Failed to get title for service bean %s", 
                        managedBeanAdapter.getId());
            }


        });
    }
    
    
    private void validateEnumTitles() {
        
        val serviceRegistry = super.getMetaModelContext().getServiceRegistry();
        val specificationLoader = super.getMetaModelContext().getSpecificationLoader();
        val titleService = serviceRegistry.lookupServiceElseFail(TitleService.class);

        // (previously we took a protective copy to avoid a concurrent modification exception,
        // but this is now done by SpecificationLoader itself)
        for (val objSpec : specificationLoader.snapshotSpecifications()) {
            val correspondingClass = objSpec.getCorrespondingClass();
            if(correspondingClass.isEnum()) {
                final Object[] enumConstants = correspondingClass.getEnumConstants();
                for (Object enumConstant : enumConstants) {

                    try {

                        val title = titleService.titleOf(enumConstant);
                        _Blackhole.consume(title);

                    } catch (Exception e) {

                        val deficiencyOrigin = Identifier.classIdentifier(correspondingClass);
                        val facetHolder = objSpec;

                        super.onFailure(
                                facetHolder, 
                                deficiencyOrigin, 
                                "Failed to get title for enum constant %s", 
                                "" + enumConstant);
                    }

                }
            }
        }
    }
        
    private void validateRegisteredMessageTranslation() {
        
        val serviceRegistry = super.getMetaModelContext().getServiceRegistry();
        val specificationLoader = super.getMetaModelContext().getSpecificationLoader();
        val translationService = serviceRegistry.lookupServiceElseFail(TranslationService.class);
        
        // as used by the Wicket UI
        final String context = "org.apache.isis.runtime.system.session.IsisSessionFactory";
        final MessageRegistry messageRegistry = new MessageRegistry();
        for (String message : messageRegistry.listMessages()) {

            try {

                val translatedMessage = translationService.translate(context, message);
                _Blackhole.consume(translatedMessage);

            } catch (Exception e) {

                val deficiencyOrigin = Identifier.classIdentifier(MessageRegistry.class);
                val facetHolder = specificationLoader.loadSpecification(MessageRegistry.class);

                super.onFailure(
                        facetHolder, 
                        deficiencyOrigin, 
                        "Failed to translate message %s from MessageRegistry", 
                        "" + message);
            }

        }

    }

}
