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

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidator;

import lombok.val;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
class _ValidateUtil{

    void runValidators(
            final ProgrammingModel programmingModel,
            final SpecificationLoader specLoader) {

        log.debug("Running MetaModelValidators ...");

        val snapshot = specLoader.snapshotSpecifications();

        programmingModel.streamValidators()
        .filter(MetaModelValidator::isEnabled)
        .forEach(validator -> {
            log.debug("Running validator: {}", validator);
            try {
                runValidator(validator, snapshot);
            } catch (Throwable t) {
                log.error(t);
                throw t;
            } finally {
                log.debug("Done validator: {}", validator);
            }
        });

        log.debug("Done running MetaModelValidators.");
    }


    // -- HELPER

    private void runValidator(final MetaModelValidator validator, final Can<ObjectSpecification> snapshot) {
        validator.validateEnter();
        snapshot.forEach(objSpec->runValidator(validator, objSpec));
        validator.validateExit();
    }

    private void runValidator(final MetaModelValidator validator, final ObjectSpecification objSpec) {
        if(!validator.getFilter().test(objSpec)) return;

        validator.validateObjectEnter(objSpec);

        objSpec.streamRuntimeActions(MixedIn.INCLUDED)
        .forEach(act->{
            act.streamParameters().forEach(param ->
                validator.validateParameter(objSpec, act, param));
            validator.validateAction(objSpec, act);
        });

        objSpec.streamProperties(MixedIn.INCLUDED)
        .forEach(prop->validator.validateProperty(objSpec, prop));

        objSpec.streamCollections(MixedIn.INCLUDED)
        .forEach(coll->validator.validateCollection(objSpec, coll));

        validator.validateObjectExit(objSpec);
    }

}
