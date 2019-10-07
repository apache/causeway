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
package org.apache.isis.metamodel.specloader;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.IsisConfigurationLegacy;
import org.apache.isis.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.progmodel.ProgrammingModelInitFilter;
import org.apache.isis.metamodel.progmodel.ProgrammingModelService;
import org.apache.isis.metamodel.progmodels.dflt.ProgrammingModelFacetsJava8;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.metamodel.specloader.validator.ValidationFailures;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service @Log4j2
public class ProgrammingModelServiceDefault implements ProgrammingModelService {

    @Override
    public ProgrammingModel getProgrammingModel() {
        return programmingModel.get();
    }
    
    @Override
    public ValidationFailures getValidationResult() {
        return validationResult.get();
    }

    // -- HELPER

    @Inject private IsisConfigurationLegacy configurationLegacy;
    @Inject private IsisConfiguration configuration;
    @Inject private ServiceRegistry serviceRegistry;
    @Inject private ProgrammingModelInitFilter programmingModelInitFilter;
     
    private _Lazy<ProgrammingModel> programmingModel = 
            _Lazy.threadSafe(this::createProgrammingModel);

    private _Lazy<ValidationFailures> validationResult = 
            _Lazy.threadSafe(this::validate);
    
    private MetaModelValidatorComposite metaModelValidator;
    
    private ProgrammingModel createProgrammingModel() {
        
        log.debug("About to create the ProgrammingModel.");

        val programmingModel = new ProgrammingModelFacetsJava8();

        // from all plugins out there, add their contributed FacetFactories to the programming model
        val metaModelRefiners = MetaModelRefiner.getAll(serviceRegistry);
        for (val metaModelRefiner : metaModelRefiners) {
            metaModelRefiner.refineProgrammingModel(programmingModel);
        }

        // finalize the programming model (make it immutable)
        programmingModel.init(programmingModelInitFilter);
        
        if(log.isDebugEnabled()) {
            
            val facetFactoryCount = programmingModel.streamFactories().count();
            val validatorCount = programmingModel.streamValidators().count();
            val postProcessorCount = programmingModel.streamPostProcessors().count();
            
            val refinerCount = metaModelRefiners.size();
            
            log.debug("Collected {} validators after also asking {} refiners.",
                    validatorCount,
                    refinerCount); 
            
            log.debug("ProgrammingModel created with {} facet-factories and {} post-processors.", 
                    facetFactoryCount, postProcessorCount);    
        }
        
        return programmingModel;
    }
    
//    private MetaModelValidatorComposite createMetaModelValidator() {
//        
//        val metaModelValidatorClassName =
//                configurationLegacy.getString(
//                        ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME,
//                        ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME_DEFAULT);
//        
//        log.debug("About to create the MetaModelValidator {}.", metaModelValidatorClassName);
//        
//        val mmValidator = InstanceUtil.createInstance(metaModelValidatorClassName, MetaModelValidator.class);
//        val mmValidatorComposite = MetaModelValidatorComposite.asComposite(mmValidator);
//        return mmValidatorComposite;
//    }
    
    private ValidationFailures validate() {
        val failures = new ValidationFailures();
        programmingModel.get().streamValidators()
        .forEach(validator->validator.validateInto(failures));
        return failures;
    }


}
