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
package org.apache.isis.viewer.wicket.ui.components.entity;

import java.util.List;
import java.util.Map;

import com.google.common.collect.FluentIterable;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.layout.v1_0.ColumnMetadata;
import org.apache.isis.applib.layout.v1_0.PropertyGroupMetadata;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecifications;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.wicket.model.models.EntityModel;

public final class PropUtil {

    private PropUtil() {
    }

    public static List<String> propertyGroupNames(
            final EntityModel entityModel,
            final ColumnMetadata.Hint hint, final ColumnMetadata columnMetaDataIfAny) {
        final ObjectAdapter adapter = entityModel.getObject();
        final ObjectSpecification objSpec = adapter.getSpecification();

        final Map<String, List<ObjectAssociation>> associationsByGroup =
                propertiesByMemberOrder(adapter);

        return columnMetaDataIfAny != null
                ? FluentIterable
                .from(columnMetaDataIfAny.getPropertyGroups())
                .transform(PropertyGroupMetadata.Util.nameOf())
                .toList()
                : ObjectSpecifications.orderByMemberGroups(objSpec, associationsByGroup.keySet(),
                hint);
    }

    public static Map<String, List<ObjectAssociation>> propertiesByMemberOrder(final ObjectAdapter adapter) {
        final List<ObjectAssociation> properties = visibleProperties(adapter);
        return ObjectAssociation.Util.groupByMemberOrderName(properties);
    }

    private static List<ObjectAssociation> visibleProperties(final ObjectAdapter adapter) {
        return visibleProperties(adapter, Filters.<ObjectAssociation>any());
    }

    private static List<ObjectAssociation> visibleProperties(
            final ObjectAdapter adapter,
            final Filter<ObjectAssociation> filter) {
        final ObjectSpecification objSpec = adapter.getSpecification();

        return objSpec.getAssociations(
                Contributed.INCLUDED, visiblePropertiesFilter(adapter, filter));
    }

    @SuppressWarnings("unchecked")
    private static Filter<ObjectAssociation> visiblePropertiesFilter(
            final ObjectAdapter adapter,
            final Filter<ObjectAssociation> filter) {
        return Filters.and(
                ObjectAssociation.Filters.PROPERTIES,
                ObjectAssociation.Filters.dynamicallyVisible(
                        adapter, InteractionInitiatedBy.USER, Where.OBJECT_FORMS),
                filter);
    }

}
