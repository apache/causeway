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

import java.util.Optional;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelValidator;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@UtilityClass
@Log4j2
class _ValidateUtil{

    void runValidators(
            final ProgrammingModel programmingModel,
            final SpecificationLoader specLoader) {

        log.debug("Running MetaModelValidators ...");

        var snapshot = specLoader.snapshotSpecifications();

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

        var actionValidator = _Casts.castTo(MetaModelValidator.ActionValidator.class, validator);
        var parameterValidator = _Casts.castTo(MetaModelValidator.ParameterValidator.class, validator);
        var propertyValidator = _Casts.castTo(MetaModelValidator.PropertyValidator.class, validator);
        var collectionValidator = _Casts.castTo(MetaModelValidator.CollectionValidator.class, validator);

        validator.validateEnter();
        snapshot
            .stream()
            .filter(validator.getFilter())
            .forEach(objSpec->runValidator(validator,
                    actionValidator, parameterValidator, propertyValidator, collectionValidator, objSpec));
        validator.validateExit();
    }

    private void runValidator(
            final MetaModelValidator objValidator,
            final Optional<MetaModelValidator.ActionValidator> actionValidator,
            final Optional<MetaModelValidator.ParameterValidator> parameterValidator,
            final Optional<MetaModelValidator.PropertyValidator> propertyValidator,
            final Optional<MetaModelValidator.CollectionValidator> collectionValidator,
            final ObjectSpecification objSpec) {

        objValidator.validateObjectEnter(objSpec);

        actionValidator
        .ifPresentOrElse(
                validator->
                    objSpec.streamRuntimeActions(MixedIn.INCLUDED)
                    .forEach(act->{
                        parameterValidator.ifPresent(paramValidator->
                            act.streamParameters().forEach(param ->
                                paramValidator.validateParameter(objSpec, act, param)));
                        validator.validateAction(objSpec, act);
                    }),
                ()->
                    parameterValidator.ifPresent(paramValidator->
                        objSpec.streamRuntimeActions(MixedIn.INCLUDED)
                        .forEach(act->
                                act.streamParameters().forEach(param ->
                                    paramValidator.validateParameter(objSpec, act, param))))
                );

        propertyValidator
        .ifPresent(validator->{
            objSpec.streamProperties(MixedIn.INCLUDED)
            .forEach(prop->validator.validateProperty(objSpec, prop));
        });

        collectionValidator
        .ifPresent(validator->{
            objSpec.streamCollections(MixedIn.INCLUDED)
            .forEach(coll->validator.validateCollection(objSpec, coll));
        });

        objValidator.validateObjectExit(objSpec);
    }

}
