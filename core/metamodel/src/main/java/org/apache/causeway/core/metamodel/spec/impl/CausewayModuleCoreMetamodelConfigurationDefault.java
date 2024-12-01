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
package org.apache.causeway.core.metamodel.spec.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModelInitFilter;

import lombok.extern.log4j.Log4j2;

@Configuration
@Import({
    SpecificationLoaderDefault.class,
})
@Log4j2
public class CausewayModuleCoreMetamodelConfigurationDefault {

    @Bean(name=CausewayModuleCoreMetamodel.NAMESPACE + ".ProgrammingModelDefault")
    @Qualifier("Default")
    public ProgrammingModel programmingModel(
            final MetaModelContext mmc,
            /**
             * plugin FacetFactories, Validators and PostProcessors to the programming model
             */
            final List<MetaModelRefiner> metaModelRefiners,
            final ProgrammingModelInitFilter programmingModelInitFilter) {

        log.info("About to create the ProgrammingModel w/ {} refiners.", metaModelRefiners.size());
        var programmingModel = new ProgrammingModelDefault(mmc, metaModelRefiners);

        // finalize the programming model (make it immutable)
        programmingModel.init(programmingModelInitFilter);

        if(log.isInfoEnabled()) {
            var refinerCount = metaModelRefiners.size();
            var facetFactoryCount = programmingModel.streamFactories().count();
            var validatorCount = programmingModel.streamValidators().count();
            var postProcessorCount = programmingModel.streamPostProcessors().count();

            log.info("Collected after asking {} refiners, and passing filter '{}':",
                    refinerCount,
                    programmingModelInitFilter.getClass());
            log.info(" - {} facet-factories", facetFactoryCount);
            log.info(" - {} validators", validatorCount);
            log.info(" - {} post-processors", postProcessorCount);
        }
        return programmingModel;
    }

}
