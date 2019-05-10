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

package org.apache.isis.core.runtime.system.session;

import static org.apache.isis.config.internal._Config.getConfiguration;

import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract.DeprecatedPolicy;
import org.apache.isis.core.metamodel.specloader.ReflectorConstants;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.progmodels.dflt.JavaReflectorHelper;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;

import lombok.val;

/**
 * 
 */
final class SpecificationLoaderFactory {
    
    // -- CREATE SPECIFICATION LOADER

    public SpecificationLoader createSpecificationLoader() {

        val metaModelRefiners = MetaModelRefiner.getAll();
        val configuration = getConfiguration();
        val programmingModel = createProgrammingModel(configuration);
        val mmValidator = createMetaModelValidator(configuration);

        return JavaReflectorHelper.createSpecificationLoader(
                programmingModel, metaModelRefiners, mmValidator);
    }

    // -- HELPER
    
    private MetaModelValidator createMetaModelValidator(IsisConfiguration configuration) {
        
        final String metaModelValidatorClassName =
                configuration.getString(
                        ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME,
                        ReflectorConstants.META_MODEL_VALIDATOR_CLASS_NAME_DEFAULT);
        return InstanceUtil.createInstance(metaModelValidatorClassName, MetaModelValidator.class);
    }

    private ProgrammingModel createProgrammingModel(IsisConfiguration configuration) {
        
        final DeprecatedPolicy deprecatedPolicy = DeprecatedPolicy.parse(configuration);

        final ProgrammingModel programmingModel = new ProgrammingModelFacetsJava5(deprecatedPolicy);
        ProgrammingModel.Util.includeFacetFactories(configuration, programmingModel);
        ProgrammingModel.Util.excludeFacetFactories(configuration, programmingModel);
        return programmingModel;
    }


}
