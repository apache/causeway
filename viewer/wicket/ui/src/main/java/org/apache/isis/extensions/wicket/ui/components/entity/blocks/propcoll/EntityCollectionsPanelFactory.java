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


package org.apache.isis.extensions.wicket.ui.components.entity.blocks.propcoll;

import org.apache.isis.extensions.wicket.model.models.EntityModel;
import org.apache.isis.extensions.wicket.ui.ComponentFactory;
import org.apache.isis.extensions.wicket.ui.ComponentType;
import org.apache.isis.extensions.wicket.ui.components.entity.EntityComponentFactoryAbstract;
import org.apache.isis.extensions.wicket.ui.components.entity.blocks.propcoll.EntityPropertiesAndOrCollectionsPanel.Render;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

/**
 * {@link ComponentFactory} for {@link EntityPropertiesAndOrCollectionsPanel}.
 */
public class EntityCollectionsPanelFactory extends EntityComponentFactoryAbstract {

	private static final long serialVersionUID = 1L;

	public EntityCollectionsPanelFactory() {
		super(ComponentType.ENTITY_COLLECTIONS);
	}

	public Component createComponent(String id, IModel<?> model) {
		EntityModel entityModel = (EntityModel) model;
		return new EntityPropertiesAndOrCollectionsPanel(id, entityModel, Render.COLLECTIONS_ONLY);
	}
}
