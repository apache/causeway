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

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.factory.InstanceUtil;
import org.apache.isis.config.IsisConfigurationLegacy;
import org.apache.isis.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.metamodel.progmodel.ProgrammingModelService;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorComposite;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0
 */
//@Service 
@Log4j2
@Deprecated
class MetaModelValidatorServiceDefault {

    public MetaModelValidator get() {
        return metaModelValidator.get();
    }

    // -- HELPER

    @Inject private IsisConfigurationLegacy configuration;
    @Inject private ProgrammingModelService programmingModelService;
    @Inject private ServiceRegistry serviceRegistry; 

    private _Lazy<MetaModelValidator> metaModelValidator = 
            _Lazy.threadSafe(this::createMetaModelValidator);

    private MetaModelValidatorComposite createMetaModelValidator() {

        log.debug("About to create the composite MetaModelValidator.");
        
        val metaModelValidatorClassName =
                configuration.getString(
                        ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME,
                        ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME_DEFAULT);
        val mmValidator = InstanceUtil.createInstance(metaModelValidatorClassName, MetaModelValidator.class);
        val mmValidatorComposite = MetaModelValidatorComposite.asComposite(mmValidator);

        val programmingModel = programmingModelService.getProgrammingModel();
        //programmingModel.refineMetaModelValidator(mmValidatorComposite);
        
        val metaModelRefiners = MetaModelRefiner.getAll(serviceRegistry);

        for (MetaModelRefiner metaModelRefiner : metaModelRefiners) {
            metaModelRefiner.refineProgrammingModel(programmingModel);
            //metaModelRefiner.refineMetaModelValidator(mmValidatorComposite);
        }
        
        if(log.isDebugEnabled()) {
            
            val refinerCount = metaModelRefiners.size();
            val validatorCount = mmValidatorComposite.size();
            
            log.debug("Collected {} validators after also asking {} refiners.",
                    validatorCount,
                    refinerCount);    
        }

        return mmValidatorComposite;
    }

}

