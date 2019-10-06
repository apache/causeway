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
package org.apache.isis.extensions.incubator;

import org.springframework.stereotype.Component;

import org.apache.isis.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.metamodel.facets.actions.support.SupportingMethodValidatorRefinerFactory;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorComposite;

@Component
public class IncubatorMetaModelPlugin implements MetaModelRefiner {

    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator) {
    }

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        programmingModel.add(
                ProgrammingModel.ProcessingOrder.C2_AFTER_METHOD_REMOVING, 
                SupportingMethodValidatorRefinerFactory.class,
                ProgrammingModel.Marker.INCUBATING
                );
    }

}
