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
package org.apache.isis.metamodel.specloader.postprocessor;

import java.util.List;

import org.apache.isis.metamodel.progmodel.ObjectSpecificationPostProcessor;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.spec.ObjectSpecification;

public class PostProcessor {

    private final ProgrammingModel programmingModel;
    // populated at #init
    List<ObjectSpecificationPostProcessor> postProcessors;

    public PostProcessor(final ProgrammingModel programmingModel) {
        this.programmingModel = programmingModel;
    }

    public void init() {
        postProcessors = programmingModel.getPostProcessors();
//        for (final ObjectSpecificationPostProcessor postProcessor : postProcessors) {
//            if(postProcessor instanceof ServicesInjectorAware) {
//                final ServicesInjectorAware servicesInjectorAware = (ServicesInjectorAware) postProcessor;
//                servicesInjectorAware.setServicesInjector(servicesInjector);
//            }
//        }
    }
    public void postProcess(final ObjectSpecification objectSpecification) {

        for (final ObjectSpecificationPostProcessor postProcessor : postProcessors) {
            postProcessor.postProcess(objectSpecification);
        }


    }

}
