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
package org.apache.isis.jdo.persistence;

import org.springframework.stereotype.Component;

import org.apache.isis.jdo.metamodel.facets.object.datastoreidentity.JdoDatastoreIdentityAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.object.discriminator.JdoDiscriminatorAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.object.query.JdoQueryAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.object.version.JdoVersionAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.prop.column.BigDecimalDerivedFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.prop.column.MandatoryFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.prop.column.MaxLengthDerivedFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.prop.notpersistent.JdoNotPersistentAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.facets.prop.primarykey.JdoPrimaryKeyAnnotationFacetFactory;
import org.apache.isis.jdo.metamodel.specloader.validator.JdoMetaModelValidator;
import org.apache.isis.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorToCheckModuleExtent;
import org.apache.isis.metamodel.specloader.validator.MetaModelValidatorToCheckObjectSpecIdsUnique;
import org.apache.isis.runtime.system.context.IsisContext;

import lombok.val;

@Component
public class PersistenceSessionFactoryMetamodelRefiner implements MetaModelRefiner {

    final ProgrammingModel.ProcessingOrder order = 
            ProgrammingModel.ProcessingOrder.A2_AFTER_FALLBACK_DEFAULTS;
    
    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        programmingModel.add(order, JdoPersistenceCapableAnnotationFacetFactory.class);
        programmingModel.add(order, JdoDatastoreIdentityAnnotationFacetFactory.class);

        programmingModel.add(order, JdoPrimaryKeyAnnotationFacetFactory.class);
        programmingModel.add(order, JdoNotPersistentAnnotationFacetFactory.class);
        programmingModel.add(order, JdoDiscriminatorAnnotationFacetFactory.class);
        programmingModel.add(order, JdoVersionAnnotationFacetFactory.class);

        programmingModel.add(order, JdoQueryAnnotationFacetFactory.class);

        programmingModel.add(order, BigDecimalDerivedFromJdoColumnAnnotationFacetFactory.class);
        programmingModel.add(order, MaxLengthDerivedFromJdoColumnAnnotationFacetFactory.class);
        // must appear after JdoPrimaryKeyAnnotationFacetFactory (above)
        // and also MandatoryFacetOnPropertyMandatoryAnnotationFactory
        // and also PropertyAnnotationFactory
        programmingModel.add(order, MandatoryFromJdoColumnAnnotationFacetFactory.class);

        
        val config = IsisContext.getConfiguration();
        
        // these validators add themselves to the programming model
        new JdoMetaModelValidator(config, programmingModel);
        new MetaModelValidatorToCheckObjectSpecIdsUnique(config, programmingModel);
        new MetaModelValidatorToCheckModuleExtent(config, programmingModel);
    }
}
