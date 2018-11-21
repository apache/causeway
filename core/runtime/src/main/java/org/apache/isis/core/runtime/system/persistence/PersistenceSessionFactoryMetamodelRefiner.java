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
package org.apache.isis.core.runtime.system.persistence;

import org.apache.isis.core.metamodel.facetapi.MetaModelRefiner;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorToCheckModuleExtent;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorToCheckObjectSpecIdsUnique;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.datastoreidentity.JdoDatastoreIdentityAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.discriminator.JdoDiscriminatorAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.query.JdoQueryAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.version.JdoVersionAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.column.BigDecimalDerivedFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.column.MandatoryFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.column.MaxLengthDerivedFromJdoColumnAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.notpersistent.JdoNotPersistentAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.facets.prop.primarykey.JdoPrimaryKeyAnnotationFacetFactory;
import org.apache.isis.objectstore.jdo.metamodel.specloader.validator.JdoMetaModelValidator;

public class PersistenceSessionFactoryMetamodelRefiner implements MetaModelRefiner {

    @Override
    public void refineProgrammingModel(ProgrammingModel programmingModel) {
        programmingModel.addFactory(
                new JdoPersistenceCapableAnnotationFacetFactory(), ProgrammingModel.Position.BEGINNING);
        programmingModel.addFactory(new JdoDatastoreIdentityAnnotationFacetFactory());

        programmingModel.addFactory(new JdoPrimaryKeyAnnotationFacetFactory());
        programmingModel.addFactory(new JdoNotPersistentAnnotationFacetFactory());
        programmingModel.addFactory(new JdoDiscriminatorAnnotationFacetFactory());
        programmingModel.addFactory(new JdoVersionAnnotationFacetFactory());

        programmingModel.addFactory(new JdoQueryAnnotationFacetFactory());

        programmingModel.addFactory(new BigDecimalDerivedFromJdoColumnAnnotationFacetFactory());
        programmingModel.addFactory(new MaxLengthDerivedFromJdoColumnAnnotationFacetFactory());
        // must appear after JdoPrimaryKeyAnnotationFacetFactory (above)
        // and also MandatoryFacetOnPropertyMandatoryAnnotationFactory
        // and also PropertyAnnotationFactory
        programmingModel.addFactory(new MandatoryFromJdoColumnAnnotationFacetFactory());
    }

    @Override
    public void refineMetaModelValidator(
            MetaModelValidatorComposite metaModelValidator) {
        metaModelValidator.add(new JdoMetaModelValidator());
        metaModelValidator.add(new MetaModelValidatorToCheckObjectSpecIdsUnique());
        metaModelValidator.add(new MetaModelValidatorToCheckModuleExtent());
    }
}
