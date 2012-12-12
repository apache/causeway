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

package org.apache.isis.objectstore.sql;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.objectstore.sql.mapping.FieldMappingFactory;

public abstract class AbstractFieldMappingFactory implements FieldMappingFactory {

    protected String getTypeOverride(final ObjectSpecification object, final ObjectAssociation field, final String type) {
        // isis.persistor.sql.automapper.default
        final IsisConfiguration configParameters = IsisContext.getConfiguration();
        final String find = object.getShortIdentifier() + "." + field.getId();
        final String property = SqlObjectStore.BASE_NAME + ".automapper.type." + find;
        final String dataType = configParameters.getString(property, type);
        return dataType;
    }
}
