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
package org.apache.isis.core.metamodel.facets.schema;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.facets.schema.cmd.v2.CommandDtoValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.facets.schema.ixn.v2.InteractionDtoValueFacetUsingSemanticsProviderFactory;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;

import lombok.val;

@Component
@Named("isis.metamodel.isisSchema.MetaModelRefiner")
public class IsisSchemaMetaModelRefiner
implements MetaModelRefiner {

    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {

        val mmc = programmingModel.getMetaModelContext();

        programmingModel.addFactory(
                ProgrammingModel.FacetProcessingOrder.G1_VALUE_TYPES,
                new InteractionDtoValueFacetUsingSemanticsProviderFactory(mmc));
        programmingModel.addFactory(
                ProgrammingModel.FacetProcessingOrder.G1_VALUE_TYPES,
                new CommandDtoValueFacetUsingSemanticsProviderFactory(mmc));
    }

}

