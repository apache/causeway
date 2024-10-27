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
package org.apache.causeway.core.metamodel.specloader.postprocessor;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.postprocessors.MetaModelPostProcessor;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PostProcessor implements AutoCloseable {

    private final ProgrammingModel programmingModel;
    private Can<MetaModelPostProcessor> enabledPostProcessors = Can.empty(); // populated at #init

    public void init() {
        enabledPostProcessors = programmingModel.streamPostProcessors()
                .filter(MetaModelPostProcessor::isEnabled)
                .collect(Can.toCan());
    }

    @Override
    public void close() {
        enabledPostProcessors = Can.empty();
    }

    public void postProcess(final ObjectSpecification objectSpecification) {

        for (var postProcessor : enabledPostProcessors) {

            if(!postProcessor.getFilter().test(objectSpecification)) {
                continue;
            }

            postProcessor.postProcessObject(objectSpecification);

            objectSpecification.streamRuntimeActions(MixedIn.INCLUDED)
            .forEach(act->{
                act.streamParameters().forEach(param ->
                    postProcessor.postProcessParameter(objectSpecification, act, param));
                postProcessor.postProcessAction(objectSpecification, act);
            });

            objectSpecification.streamProperties(MixedIn.INCLUDED)
            .forEach(prop->postProcessor.postProcessProperty(objectSpecification, prop));

            objectSpecification.streamCollections(MixedIn.INCLUDED)
            .forEach(coll->postProcessor.postProcessCollection(objectSpecification, coll));

        }
    }

}
