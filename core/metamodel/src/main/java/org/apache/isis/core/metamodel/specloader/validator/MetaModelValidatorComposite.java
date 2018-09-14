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

import java.util.List;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

public class MetaModelValidatorComposite extends MetaModelValidatorAbstract {

    private final List<MetaModelValidator> validators = _Lists.newArrayList();


    public MetaModelValidatorComposite add(final MetaModelValidator validator) {
        validators.add(validator);
        return this;
    }

    public MetaModelValidatorComposite addAll(
            final MetaModelValidator... validators) {

        for (final MetaModelValidator validator : validators) {
            add(validator);
        }
        return this;
    }


    @Override
    public void init(final SpecificationLoader specificationLoader) {
        super.init(specificationLoader);
        for (final MetaModelValidator validator : validators) {
            validator.init(specificationLoader);
        }
    }


    @Override
    public void validate(final ValidationFailures validationFailures)  {
        for (final MetaModelValidator validator : validators) {
            validator.validate(validationFailures);
        }
    }


    public static MetaModelValidatorComposite asComposite(final MetaModelValidator baseMetaModelValidator) {
        final MetaModelValidatorComposite metaModelValidatorComposite = new MetaModelValidatorComposite();
        metaModelValidatorComposite.add(baseMetaModelValidator);
        return metaModelValidatorComposite;
    }
}
