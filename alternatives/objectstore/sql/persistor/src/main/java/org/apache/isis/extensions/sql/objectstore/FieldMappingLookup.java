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


package org.apache.isis.extensions.sql.objectstore;

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.extensions.sql.objectstore.jdbc.JdbcGeneralValueMapper;
import org.apache.isis.extensions.sql.objectstore.mapping.FieldMapping;
import org.apache.isis.extensions.sql.objectstore.mapping.FieldMappingFactory;
import org.apache.isis.extensions.sql.objectstore.mapping.ObjectReferenceMapping;
import org.apache.isis.extensions.sql.objectstore.mapping.ObjectReferenceMappingFactory;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.log4j.Logger;


public class FieldMappingLookup {
    private static final Logger LOG = Logger.getLogger(FieldMappingLookup.class);
    private final Map<ObjectSpecification, FieldMappingFactory> fieldMappings = new HashMap<ObjectSpecification, FieldMappingFactory>();
    private final Map<ObjectSpecification, ObjectReferenceMappingFactory> referenceMappings = new HashMap<ObjectSpecification, ObjectReferenceMappingFactory>();
    private FieldMappingFactory referenceFieldMappingfactory;
    private ObjectReferenceMappingFactory objectReferenceMappingfactory;

    public FieldMapping createMapping(ObjectAssociation field) {
        ObjectSpecification spec = field.getSpecification();
        FieldMappingFactory factory = fieldMappings.get(spec);
        if (factory != null) {
            return factory.createFieldMapping(field);
        } else if (spec.isEncodeable()) {
            factory = new JdbcGeneralValueMapper.Factory("VARCHAR(65)");
            addFieldMappingFactory(spec, factory);
            return factory.createFieldMapping(field);
        } else if (true /* TODO test for reference */) {
            factory = referenceFieldMappingfactory;
            addFieldMappingFactory(spec, factory);
            return factory.createFieldMapping(field);
        } else {
            throw new IsisException("No mapper for " + spec + " (no default mapper)");
        }
    }

    public ObjectReferenceMapping createMapping(ObjectSpecification spec) {
        return createMapping(spec.getShortName(), spec);
    }
    
    public ObjectReferenceMapping createMapping(String columnName, ObjectSpecification spec) {
        ObjectReferenceMappingFactory factory = referenceMappings.get(spec);
        if (factory != null) {
            return factory.createReferenceMapping(columnName, spec);
        } else if (spec.isEncodeable()) {
            // TODO add generic encodeable mapping
            throw new NotYetImplementedException();
        } else if (true /* TODO test for reference */) {
            factory = objectReferenceMappingfactory;
         //   add(spec, factory);
            return factory.createReferenceMapping(columnName, spec); // TODO: here
        } else {
            throw new IsisException("No mapper for " + spec + " (no default mapper)");
        }
    }
    
    public void addFieldMappingFactory(final Class valueType, final FieldMappingFactory mapper) {
        ObjectSpecification spec = IsisContext.getSpecificationLoader().loadSpecification(valueType);
        addFieldMappingFactory(spec, mapper);
    }

    private void addFieldMappingFactory(final ObjectSpecification specification, final FieldMappingFactory mapper) {
        LOG.debug("add mapper " + mapper + " for " + specification);
        fieldMappings.put(specification, mapper);
    }

    public void addReferenceMappingFactory(final ObjectSpecification specification, final ObjectReferenceMappingFactory mapper) {
       LOG.debug("add mapper " + mapper + " for " + specification);
       referenceMappings.put(specification, mapper);
    }

    public void init() {
    // fieldMappingFactory.load(this);
    }

    public IdMapping createIdMapping() {
        // TODO inject and use external factory
        IdMapping idMapping = new IdMapping();
        idMapping.init();
        return idMapping;
    }

    public VersionMapping createVersionMapping() {
        // TODO inject and use external factory
        VersionMapping versionMapping = new VersionMapping();
        versionMapping.init();
        return versionMapping;
    }

    public void setReferenceFieldMappingFactory(FieldMappingFactory referenceMappingfactory) {
        this.referenceFieldMappingfactory = referenceMappingfactory;
    }

    public void setObjectReferenceMappingfactory(ObjectReferenceMappingFactory objectReferenceMappingfactory) {
        this.objectReferenceMappingfactory = objectReferenceMappingfactory;
    }

    public TitleMapping createTitleMapping() {
        // TODO inject and use external factory
        return new TitleMapping();
    }
}
