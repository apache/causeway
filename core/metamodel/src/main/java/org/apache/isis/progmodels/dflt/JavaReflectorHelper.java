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

package org.apache.isis.progmodels.dflt;

import java.util.Collection;

import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidator;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;

public final class JavaReflectorHelper  {
    
    //private static final Logger LOG = LoggerFactory.getLogger(JavaReflectorHelper.class);

    private JavaReflectorHelper(){}

    public static SpecificationLoader createObjectReflector(
            final ProgrammingModel programmingModel,
            final Collection<MetaModelRefiner> metaModelRefiners,
            final MetaModelValidator mmv,
            final ServicesInjector servicesInjector) {

        MetaModelValidatorComposite metaModelValidator = MetaModelValidatorComposite.asComposite(mmv);
        for (MetaModelRefiner metaModelRefiner : metaModelRefiners) {
            metaModelRefiner.refineProgrammingModel(programmingModel);
            metaModelRefiner.refineMetaModelValidator(metaModelValidator);
        }

        programmingModel.refineMetaModelValidator(metaModelValidator);

        return new SpecificationLoader(programmingModel, metaModelValidator, servicesInjector);
    }

}
