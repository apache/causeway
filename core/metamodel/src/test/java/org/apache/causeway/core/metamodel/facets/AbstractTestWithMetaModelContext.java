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
package org.apache.causeway.core.metamodel.facets;

import java.util.function.BiConsumer;

import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModelAbstract;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModelInitFilterDefault;
import org.apache.causeway.core.metamodel.progmodels.dflt.ProgrammingModelFacetsJava11;

import lombok.Getter;
import lombok.val;

public abstract class AbstractTestWithMetaModelContext
implements HasMetaModelContext {

    @Getter(onMethod_ = {@Override})
    protected MetaModelContext metaModelContext;

    protected final void setupDefaultMetaModelContext() {
        metaModelContext = MetaModelContext_forTesting.builder()
                .build();
    }

    protected final void setupWithDefaultProgrammingModel() {
        metaModelContext = MetaModelContext_forTesting.builder()
                .programmingModelFactory(mmc->{
                    val progModel = new ProgrammingModelFacetsJava11(mmc);
                    progModel.init(new ProgrammingModelInitFilterDefault());
                    return progModel;
                })
                .build();
    }

    protected final void setupWithProgrammingModel(final BiConsumer<MetaModelContext, ProgrammingModelAbstract> factory) {
        metaModelContext = MetaModelContext_forTesting.builder()
                .programmingModelFactory(mmc->{
                    val progModel = new ProgrammingModelAbstract(mmc) {
                        @Override protected void assertNotInitialized(){}
                    };
                    factory.accept(mmc, progModel);
                    progModel.init(new ProgrammingModelInitFilterDefault());
                    return progModel;
                })
                .build();
    }

}
