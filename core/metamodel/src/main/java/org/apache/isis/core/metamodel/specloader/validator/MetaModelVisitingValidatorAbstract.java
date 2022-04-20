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
package org.apache.isis.core.metamodel.specloader.validator;

import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.context.MetaModelContext;

import lombok.val;

public abstract class MetaModelVisitingValidatorAbstract
extends MetaModelValidatorAbstract
implements MetaModelVisitingValidator {

    protected MetaModelVisitingValidatorAbstract(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    public final void validate() {

        if(!isEnabled()){
            return;
        }

        val shouldRunConcurrent =
                getMetaModelContext().getConfiguration().getCore().getMetaModel().getValidator().isParallelize();

        getMetaModelContext().getSpecificationLoader()
        .forEach(this::validate, shouldRunConcurrent);

        summarize();

    }

}
