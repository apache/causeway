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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * {@link PanelAbstract Panel} to represent an entity on a single page made up
 * of several &lt;div&gt; regions.
 */
public class EntityCombinedPanel extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_PROPERTIES_AND_COLLECTIONS = "entityPropertiesAndCollections";

    
    public EntityCombinedPanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);
        buildGui();
    }

    private void buildGui() {
        final EntityModel model = getModel();
        final ObjectAdapter objectAdapter = model.getObject();
        final CssClassFacet facet = objectAdapter.getSpecification().getFacet(CssClassFacet.class);
        if(facet != null) {
            final String cssClass = facet.cssClass(objectAdapter);
            CssClassAppender.appendCssClassTo(this, cssClass);
        }

        addOrReplace(ComponentType.ENTITY_SUMMARY, model);
        
        getComponentFactoryRegistry().addOrReplaceComponent(this, ID_ENTITY_PROPERTIES_AND_COLLECTIONS, ComponentType.ENTITY_PROPERTIES, model);
    }


}
