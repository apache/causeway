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
package org.apache.isis.valuetypes.markdown.metamodel;

import java.util.Collection;
import java.util.Collections;

import javax.inject.Named;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.valuetypes.ValueTypeDefinition;
import org.apache.isis.core.metamodel.valuetypes.ValueTypeProvider;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.valuetypes.markdown.applib.value.Markdown;
import org.apache.isis.valuetypes.markdown.metamodel.facets.MarkdownValueSemanticsProvider;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({
        MarkdownMetaModelRefiner.class,
        MarkdownValueTypeProvider.class,

        MarkdownValueSemanticsProvider.class,

})
public class IsisModuleValMarkdownMetaModel {

}

@Component
@Named("isis.val.MarkdownMetaModelRefiner")
class MarkdownMetaModelRefiner implements MetaModelRefiner {
    @Override
    public void refineProgrammingModel(final ProgrammingModel programmingModel) {
//blueprint
//        programmingModel.addFactory(
//                ProgrammingModel.FacetProcessingOrder.G1_VALUE_TYPES,
//                MarkdownValueFacetUsingSemanticsProviderFactory.class);
    }
}

@Component
@Named("isis.val.MarkdownValueTypeProvider")
class MarkdownValueTypeProvider implements ValueTypeProvider {
    @Override
    public Collection<ValueTypeDefinition> definitions() {
        return Collections.singletonList(
                ValueTypeDefinition.of(Markdown.class, ValueType.STRING));
    }
}
