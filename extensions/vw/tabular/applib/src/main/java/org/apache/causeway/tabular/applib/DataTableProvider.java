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
package org.apache.causeway.tabular.applib;

import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.beans.CausewayBeanTypeRegistry;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.metamodel.tabular.simple.DataColumn;
import org.apache.causeway.core.metamodel.tabular.simple.DataTable;

import lombok.val;

/**
 * TODO Early draft (wip)
 */
public abstract class DataTableProvider {

    @Inject SpecificationLoader specLoader;
    @Inject CausewayBeanTypeRegistry beanTypeRegistry;

    /**
     * Returns an empty {@link DataTable} for given domain object type.
     * It can be populated later on using {@link DataTable#withDataElements(Can)}.
     */
    public DataTable getDataTable(final Class<?> domainType) {
        val typeSpec = specLoader.specForTypeElseFail(domainType);
        return new DataTable(typeSpec, DataColumn.orderByColumnId());
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
