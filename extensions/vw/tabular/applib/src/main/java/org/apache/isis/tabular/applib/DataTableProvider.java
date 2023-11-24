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
package org.apache.isis.tabular.applib;

import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.tabular.simple.DataTable;

/**
 * TODO Early draft (wip) - could also easily move to MetamodelService
 */
public abstract class DataTableProvider {

    @Inject SpecificationLoader specLoader;
    @Inject IsisBeanTypeRegistry beanTypeRegistry;

    /**
     * Returns an empty {@link DataTable} for given domain object type.
     * It can be populated later on using {@link DataTable#setDataElements(Can)}.
     */
    public DataTable getDataTable(final Class<?> domainType) {
        return DataTable.forDomainType(domainType);
    }

    public Stream<DataTable> streamDataTables() {
        return streamEntityClasses()
            .map(this::getDataTable);
    }

    public Stream<ObjectSpecification> streamEntities() {
        return streamEntityClasses()
                .map(specLoader::specForTypeElseFail);
    }

    public Stream<Class<?>> streamEntityClasses() {
        return beanTypeRegistry.getEntityTypes().keySet()
            .stream()
            //TODO perhaps externalize sorting
            .sorted((a, b)->a.getSimpleName().compareTo(b.getSimpleName()));
    }

}
