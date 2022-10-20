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
package org.apache.causeway.core.metamodel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting.MetaModelContext_forTestingBuilder;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;

import lombok.Getter;
import lombok.val;

/**
 * Prototypical test base for the MM module.
 *
 * @apiNote Other modules should adapt their own version,
 * taking this one as a blueprint.
 *
 * @since 2.0
 */
public abstract class MetaModelTestAbstract
implements HasMetaModelContext {

    @Getter(onMethod_ = {@Override})
    private MetaModelContext metaModelContext;

    @BeforeEach
    void setUp() throws Exception {
        val mmcBuilder = MetaModelContext_forTesting.builder();
        onSetUp(mmcBuilder);
        metaModelContext = mmcBuilder.build();
        afterSetUp();
    }

    @AfterEach
    void tearDown() throws Exception {
        onTearDown();
        metaModelContext.getSpecificationLoader().disposeMetaModel();
        metaModelContext = null;
    }

    protected void onSetUp(MetaModelContext_forTestingBuilder mmcBuilder) {
    }

    protected void afterSetUp() {
    }

    protected void onTearDown() {
    }

}
