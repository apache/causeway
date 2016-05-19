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

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

public abstract class MetaModelValidatorAbstract implements MetaModelValidator {

    private SpecificationLoader specificationLoaderSpi;

    // ////////////////////////////////////////////////////////////////////
    // init, shutdown
    // ////////////////////////////////////////////////////////////////////

    public void init() {
        ensureThatState(specificationLoaderSpi, is(notNullValue()));
    }

    public void shutdown() {
    }

    // ////////////////////////////////////////////////////////////////////
    // Dependencies (due to *Aware)
    // ////////////////////////////////////////////////////////////////////

    public SpecificationLoader getSpecificationLoader() {
        return specificationLoaderSpi;
    }

    @Override
    public void setSpecificationLoader(final SpecificationLoader specificationLoader) {
        this.specificationLoaderSpi = specificationLoader;
    }

    
    
}
