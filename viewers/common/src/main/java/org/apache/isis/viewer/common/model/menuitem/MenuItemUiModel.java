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
package org.apache.isis.viewer.common.model.menuitem;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.viewer.common.model.action.ActionUiModel;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * @since 2.0.0
 * @param <T> - link component type, native to the viewer
 * @param <U> - concrete type implementing this class
 */
@Accessors(chain = true)
@RequiredArgsConstructor
@Log4j2
public abstract class MenuItemUiModel<T, U extends MenuItemUiModel<T, U>> {
    
    @Getter private final String name;

    /**
     * To determine whether requires a separator before it.
     */
    @Getter @Setter private boolean isFirstInSection = false; // unless set otherwise

    @Getter @Setter private ActionUiModel<T> menuActionUiModel;
    
    private final List<U> subMenuItems = _Lists.newArrayList();
    protected void addSubMenuItem(final U cssMenuItem) {
        subMenuItems.add(cssMenuItem);
    }
    public List<U> getSubMenuItems() {
        return Collections.unmodifiableList(subMenuItems);
    }
    /**
     * @param menuItems we assume these have the correct parent already set
     */
    public void replaceSubMenuItems(List<U> menuItems) {
        subMenuItems.clear();
        subMenuItems.addAll(menuItems);
    }
    public boolean hasSubMenuItems() {
        return subMenuItems.size() > 0;
    }
    
    
    @Getter private U parent;
    protected void setParent(U parent) {
        this.parent = parent;
        parent.addSubMenuItem(_Casts.uncheckedCast(this));        
    }
    public boolean hasParent() {
        return parent != null;
    }
    
    // -- CONSTRUCTION
    
    /**
     * Optionally creates a sub-menu item invoking an action on the provided 
     * {@link MenuActionWkt action model}, based on visibility and usability.
     */
    public void addSubMenuItemFor(
            @NonNull final ActionUiModel<T> actionModel,
            final boolean isFirstInSection,
            @Nullable final Consumer<U> onNewSubMenuItem) {

        val objectAction = actionModel.getObjectAction();
        if(!actionModel.isVisible()) {
            log.debug("not visible {}", objectAction.getName());
            return;
        }

        // build the link
        val actionMeta = actionModel.getActionUiMetaModel();
        if (actionMeta == null) {
            // can only get a null if invisible, so this should not happen given the visibility guard above
            return;
        }

        val menutIem = newSubMenuItem(actionMeta.getLabel())
                .setFirstInSection(isFirstInSection);
        
        if(onNewSubMenuItem!=null) {
            onNewSubMenuItem.accept(_Casts.uncheckedCast(menutIem));
        }
    }
    
    protected abstract U newSubMenuItem(final String name);

    
}
