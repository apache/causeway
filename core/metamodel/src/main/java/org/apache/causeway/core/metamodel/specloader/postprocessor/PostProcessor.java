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
import org.apache.causeway.core.metamodel.postprocessors.ObjectSpecificationPostProcessor;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class PostProcessor {

    private final ProgrammingModel programmingModel;
    private Can<ObjectSpecificationPostProcessor> enabledPostProcessors = Can.empty(); // populated at #init

    public void init() {
        enabledPostProcessors = programmingModel.streamPostProcessors()
                .filter(ObjectSpecificationPostProcessor::isEnabled)
                .collect(Can.toCan());
    }

    public void shutdown() {
        enabledPostProcessors = Can.empty();
    }

    public void postProcess(final ObjectSpecification objectSpecification) {
        // calling count on these 3 streams so these are actually consumed,
        // as a side-effect the meta-model potentially gets further populated
//        objectSpecification.streamRuntimeActions(MixedIn.INCLUDED).count();
//        objectSpecification.streamCollections(MixedIn.INCLUDED).count();
//        objectSpecification.streamProperties(MixedIn.INCLUDED).count();

        postProcessObject(objectSpecification);

        objectSpecification.streamRuntimeActions(MixedIn.INCLUDED)
        .forEach(act->postProcessAction(objectSpecification, act));

        objectSpecification.streamProperties(MixedIn.INCLUDED)
        .forEach(prop->postProcessProperty(objectSpecification, prop));

        objectSpecification.streamCollections(MixedIn.INCLUDED)
        .forEach(coll->postProcessCollection(objectSpecification, coll));
    }

    // -- HELPER

    private void postProcessObject(
            final ObjectSpecification objectSpecification) {
        for (val postProcessor : enabledPostProcessors) {
            postProcessor.postProcessObject(objectSpecification);
        }
    }

    private void postProcessAction(
            final ObjectSpecification objectSpecification,
            final ObjectAction act) {
        for (val postProcessor : enabledPostProcessors) {
            act.streamParameters().forEach(param ->
                postProcessor.postProcessParameter(objectSpecification, act, param));
            postProcessor.postProcessAction(objectSpecification, act);
        }
    }

    private void postProcessProperty(
            final ObjectSpecification objectSpecification,
            final OneToOneAssociation prop) {
        for (val postProcessor : enabledPostProcessors) {
            postProcessor.postProcessProperty(objectSpecification, prop);
        }
    }

    private void postProcessCollection(
            final ObjectSpecification objectSpecification,
            final OneToManyAssociation coll) {
        for (val postProcessor : enabledPostProcessors) {
            postProcessor.postProcessCollection(objectSpecification, coll);
        }
    }

}
