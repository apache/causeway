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
import javax.inject.Singleton;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.factory.InstanceUtil;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.metamodel.progmodel.ProgrammingModelService;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorService;

import lombok.val;

/**
 * @since 2.0
 */
@Singleton
public class MetaModelValidatorServiceDefault implements MetaModelValidatorService {

    @Override
    public MetaModelValidator get() {
        return metaModelValidator.get();
    }
    
    // -- HELPER
    
    @Inject private IsisConfiguration configuration;
    @Inject private ProgrammingModelService programmingModelService;
    @Inject private ServiceRegistry serviceRegistry; 
    
    private _Lazy<MetaModelValidator> metaModelValidator = 
            _Lazy.threadSafe(this::createMetaModelValidator);
            
    private MetaModelValidator createMetaModelValidator() {
        
        final String metaModelValidatorClassName =
                configuration.getString(
                        ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME,
                        ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME_DEFAULT);
        val mmValidator = InstanceUtil.createInstance(metaModelValidatorClassName, MetaModelValidator.class);
        val mmValidatorComposite = MetaModelValidatorComposite.asComposite(mmValidator);
        
        val programmingModel = programmingModelService.get();
        val metaModelRefiners = MetaModelRefiner.getAll(serviceRegistry);
        
        for (MetaModelRefiner metaModelRefiner : metaModelRefiners) {
            metaModelRefiner.refineProgrammingModel(programmingModel);
            metaModelRefiner.refineMetaModelValidator(mmValidatorComposite);
        }
        
        programmingModel.refineMetaModelValidator(mmValidatorComposite);
        
        return mmValidator;
    }
    
}

