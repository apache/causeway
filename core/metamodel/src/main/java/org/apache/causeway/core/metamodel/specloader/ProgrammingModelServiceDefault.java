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
package org.apache.causeway.core.metamodel.specloader;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModelInitFilter;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModelService;
import org.apache.causeway.core.metamodel.progmodels.dflt.ProgrammingModelFacetsJava11;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".ProgrammingModelServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
public class ProgrammingModelServiceDefault
implements ProgrammingModelService {

    @Override
    public ProgrammingModel getProgrammingModel() {
        return programmingModel.get();
    }

    // -- HELPER

    @Inject private ProgrammingModelInitFilter programmingModelInitFilter;
    @Inject private MetaModelContext metaModelContext;

    private _Lazy<ProgrammingModel> programmingModel =
            _Lazy.threadSafe(this::createProgrammingModel);

    private ProgrammingModel createProgrammingModel() {

        log.info("About to create the ProgrammingModel.");

        val programmingModel = new ProgrammingModelFacetsJava11(metaModelContext);

        // from all plugins out there, add their contributed FacetFactories, Validators
        // and PostProcessors to the programming model
        val metaModelRefiners = metaModelContext.getServiceRegistry().select(MetaModelRefiner.class);
        for (val metaModelRefiner : metaModelRefiners) {
            metaModelRefiner.refineProgrammingModel(programmingModel);
        }

        // finalize the programming model (make it immutable)
        programmingModel.init(programmingModelInitFilter);

        if(log.isInfoEnabled()) {

            val refinerCount = metaModelRefiners.size();

            val facetFactoryCount = programmingModel.streamFactories().count();
            val validatorCount = programmingModel.streamValidators().count();
            val postProcessorCount = programmingModel.streamPostProcessors().count();


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
