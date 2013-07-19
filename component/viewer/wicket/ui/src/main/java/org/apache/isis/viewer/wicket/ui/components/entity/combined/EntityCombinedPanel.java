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

package org.apache.isis.viewer.wicket.ui.components.entity.combined;

import org.apache.wicket.Component;

import org.apache.isis.applib.annotation.MemberGroupLayout.ColumnSpans;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupLayoutFacet;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.util.ObjectSpecifications.MemberGroupLayoutHint;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * {@link PanelAbstract Panel} to represent an entity on a single page made up
 * of several &lt;div&gt; regions.
 */
public class EntityCombinedPanel extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    public EntityCombinedPanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);
        buildGui();
    }

    private void buildGui() {
        final EntityModel model = getModel();
        final CssClassFacet facet = model.getObject().getSpecification().getFacet(CssClassFacet.class);
        if(facet != null) {
            this.add(new CssClassAppender(facet.value()));
        }

        final MemberGroupLayoutFacet mglFacet = model.getObject().getSpecification().getFacet(MemberGroupLayoutFacet.class);
        final ColumnSpans columnSpans = mglFacet.getColumnSpans();
        
        addOrReplace(ComponentType.ENTITY_SUMMARY, model);
        model.setMemberGroupLayoutHint(MemberGroupLayoutHint.LEFT);
        
        final Component leftColumn = getComponentFactoryRegistry().addOrReplaceComponent(this, "entityPropertiesLeft", ComponentType.ENTITY_PROPERTIES, model);
        addClassForSpan(leftColumn, columnSpans.getLeft());
        
        if(!mglFacet.getMiddle().isEmpty()) {
            model.setMemberGroupLayoutHint(MemberGroupLayoutHint.MIDDLE);
            final Component middleColumn = getComponentFactoryRegistry().addOrReplaceComponent(this, "entityPropertiesMiddle", ComponentType.ENTITY_PROPERTIES, model);
            addClassForSpan(middleColumn, columnSpans.getMiddle());
        } else {
            permanentlyHide("entityPropertiesMiddle");
        }
        final Component rightColumn = addOrReplace(ComponentType.ENTITY_COLLECTIONS, model);
        addClassForSpan(rightColumn, columnSpans.getRight());
    }

    private static void addClassForSpan(final Component component, final int numGridCols) {
        component.add(new CssClassAppender("span"+numGridCols));
    }
}
