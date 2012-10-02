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

package org.apache.isis.viewer.wicket.ui.components.entity.selector.dropdown;

import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.selector.dropdown.DropDownSelectorPanelAbstract;

/**
 * Provides a drop-down for selecting other views that support
 * {@link ComponentType#ENTITY} with a backing {@link EntityModel}.
 * 
 * <p>
 * Most of the heavy lifting is factored out into the superclass,
 * {@link DropDownSelectorPanelAbstract}.
 * 
 * <p>
 * Note that this class should be registered (in
 * <tt>ComponentFactoryListDefault</tt>) prior to any other views.
 */
public class EntityDropDownSelector extends DropDownSelectorPanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    public EntityDropDownSelector(final String id, final EntityModel model, final ComponentFactory factory) {
        super(id, ComponentType.ENTITY.toString(), model, factory);
    }

}
