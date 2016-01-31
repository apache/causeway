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
package org.apache.isis.viewer.wicket.ui.components.entity.column;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.applib.layout.fixedcols.FCColumn;
import org.apache.isis.applib.layout.common.FieldSet;
import org.apache.isis.applib.layout.common.PropertyLayoutData;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupLayoutFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecifications;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.entity.PropUtil;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;

/**
 * Adds properties (in property groups) and collections to a column.
 *
 * <p>
 *     If {@link FCColumn} is present, then only those properties and collections for that
 *     column metadata are rendered.   Otherwise the {@link MemberGroupLayoutFacet} on the
 *     {@link ObjectSpecification} in conjunction with the provided {@link FCColumn.Hint} is
 *     used to filter down to just those properties/collections in the column.
 * </p>
 */
public class EntityColumn extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_PROPERTY_GROUP = "propertyGroup";


    // view metadata (populated for EntityTabbedPanel, absent for EntityEditablePanel)
    private final FCColumn columnMetaDataIfAny;
    // which column to render (populated for EntityEditablePanel, not required and so absent for EntityTabbedPanel)
    final FCColumn.Hint hint;

    public EntityColumn(
            final String id,
            final EntityModel entityModel) {

        super(id, entityModel);

        columnMetaDataIfAny = entityModel.getFCColumn();
        hint = entityModel.getFCColumn().getHint();

        buildGui();
    }

    private void buildGui() {
        addPropertiesAndCollections(this, getModel());
    }

    private void addPropertiesAndCollections(
            final MarkupContainer col,
            final EntityModel entityModel) {
        addPropertiesInColumn(col, entityModel);
        addCollectionsIfRequired(col, entityModel);
    }

    private void addPropertiesInColumn(
            final MarkupContainer markupContainer,
            final EntityModel entityModel) {

        final ObjectAdapter adapter = entityModel.getObject();
        final ObjectSpecification objSpec = adapter.getSpecification();

        final Map<String, List<ObjectAssociation>> associationsByGroup = PropUtil.propertiesByMemberOrder(adapter);

        final RepeatingView memberGroupRv = new RepeatingView(ID_PROPERTY_GROUP);
        markupContainer.add(memberGroupRv);

        final ImmutableMap<String, FieldSet> propertyGroupMetadataByNameIfAny =
                columnMetaDataIfAny != null
                    ? Maps.uniqueIndex(columnMetaDataIfAny.getFieldSets(), FieldSet.Util.nameOf())
                    : null;

        final Collection<String> groupNames =
                propertyGroupMetadataByNameIfAny != null
                    ? propertyGroupMetadataByNameIfAny.keySet()
                    : ObjectSpecifications.orderByMemberGroups(objSpec, associationsByGroup.keySet(), hint);

        for(final String groupName: groupNames) {


            final FieldSet fieldSet;
            if (propertyGroupMetadataByNameIfAny != null) {
                fieldSet = propertyGroupMetadataByNameIfAny.get(groupName);
            }
            else {
                final List<ObjectAssociation> associationsInGroup = associationsByGroup.get(groupName);
                fieldSet = new FieldSet(groupName);
                fieldSet.setProperties(
                        FluentIterable
                                .from(associationsInGroup)
                                .transform(
                                    new Function<ObjectAssociation, PropertyLayoutData>() {
                                        @Override
                                        public PropertyLayoutData apply(final ObjectAssociation assoc) {
                                            return new PropertyLayoutData(assoc.getId());
                                        }
                                    }).toList());
            }

            if(fieldSet.getProperties().isEmpty()) {
                continue;
            }

            final String id = memberGroupRv.newChildId();

            final EntityModel entityModelWithHints = entityModel.cloneWithPropertyGroupMetadata(fieldSet);
            final WebMarkupContainer memberGroupRvContainer = new org.apache.isis.viewer.wicket.ui.components.entity.propgroup.PropertyGroup(id, entityModelWithHints);

            memberGroupRv.add(memberGroupRvContainer);
        }
    }

    private void addCollectionsIfRequired(
            final MarkupContainer column,
            final EntityModel entityModel) {

        if(columnMetaDataIfAny != null) {
            getComponentFactoryRegistry()
                    .addOrReplaceComponent(column, "collections", ComponentType.ENTITY_COLLECTIONS, entityModel);
        } else {
            Components.permanentlyHide(column, "collections");
        }
    }



    ///////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////

    protected DeploymentType getDeploymentType() {
        return IsisContext.getDeploymentType();
    }

}
