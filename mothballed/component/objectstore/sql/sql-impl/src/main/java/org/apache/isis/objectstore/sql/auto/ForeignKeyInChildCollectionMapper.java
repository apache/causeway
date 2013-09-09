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

/**
 * 
 */
package org.apache.isis.objectstore.sql.auto;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToManyAssociationImpl;
import org.apache.isis.objectstore.sql.FieldMappingLookup;
import org.apache.isis.objectstore.sql.ObjectMappingLookup;

/**
 * Used to map 1-to-many collections by creating, in the child table, 1 column
 * per parent collection. The column is named by combining the final part of the
 * parent class name and the collection variable name.
 * 
 * @author Kevin
 */
public class ForeignKeyInChildCollectionMapper extends ForeignKeyCollectionMapper {
    private static final Logger LOG = LoggerFactory.getLogger(ForeignKeyCollectionMapper.class);

    protected final ObjectAssociation priorField; // prevents recursion
    protected final List<ObjectAssociation> priorFields;

    public ForeignKeyInChildCollectionMapper(final ObjectAssociation objectAssociation, final String parameterBase, final FieldMappingLookup lookup, final ObjectMappingLookup objectMapperLookup, final AbstractAutoMapper abstractAutoMapper, final ObjectAssociation field) {
        super(objectAssociation, parameterBase, lookup, objectMapperLookup);

        priorFields = abstractAutoMapper.fields;
        priorField = field;

        setUpFieldMappers();
    }

    protected ForeignKeyInChildCollectionMapper(final FieldMappingLookup lookup, final AbstractAutoMapper abstractAutoMapper, final ObjectAssociation field) {
        super(lookup, abstractAutoMapper, field);
        priorFields = null;
        priorField = null;
    }

    @Override
    protected void getExtraFields(final List<ObjectAssociation> existingFields) {
        if (priorFields != null) {
            for (final ObjectAssociation priorField1 : priorFields) {
                if (existingFields.contains(priorField1) == false) {
                    existingFields.add(priorField1);
                } else {
                    LOG.debug("Skipping prior field: " + priorField1.getName());
                }
            }
        }
    }

    @Override
    protected String determineColumnName(final ObjectAssociation objectAssociation) {
        if (objectAssociation instanceof OneToManyAssociationImpl) {
            final OneToManyAssociationImpl fkAssoc = (OneToManyAssociationImpl) objectAssociation;
            final FacetedMethod peer = fkAssoc.getFacetedMethod();
            final String fullClassName = peer.getIdentifier().getClassName();
            final int lastPos = fullClassName.lastIndexOf('.');
            return fullClassName.substring(lastPos + 1) + "_" + fkAssoc.getId();
        } else {
            return objectAssociation.getSpecification().getShortIdentifier();
        }
    }
}
