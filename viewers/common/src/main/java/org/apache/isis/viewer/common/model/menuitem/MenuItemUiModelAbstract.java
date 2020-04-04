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

import org.apache.isis.applib.layout.component.CssClassFaPosition;
import org.apache.isis.core.commons.internal.collections._Lists;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@RequiredArgsConstructor
public abstract class MenuItemUiModelAbstract<T extends MenuItemUiModelAbstract<T>> {
    
    @Getter private final String name;
    @Getter @Setter private boolean enabled = true; // unless disabled
    @Getter @Setter private String actionIdentifier;
    @Getter @Setter private String cssClass;
    @Getter @Setter private String cssClassFa;
    @Getter @Setter private CssClassFaPosition cssClassFaPosition;
    @Getter @Setter private String description;
    /**
     * Requires a separator before it
     */
    @Getter @Setter private boolean requiresSeparator = false; // unless set otherwise
    @Getter @Setter private boolean separator;

    /**
     * Only populated if not {@link #isEnabled() enabled}.
     */
    @Getter @Setter private String disabledReason;
    
    /**
     * A menu action with no parameters AND an are-you-sure semantics
     * does require an immediate confirmation dialog.
     * <br/>
     * Others don't.
     */
    @Getter @Setter private boolean requiresImmediateConfirmation = false; // unless set otherwise
    @Getter @Setter private boolean prototyping = false; // unless set otherwise
    /**
     * Whether this MenuItem's Action returns a Blob or Clob
     */
    @Getter @Setter private boolean blobOrClob = false; // unless set otherwise
    
    private final List<T> subMenuItems = _Lists.newArrayList();
    protected void addSubMenuItem(final T cssMenuItem) {
        subMenuItems.add(cssMenuItem);
    }
    public List<T> getSubMenuItems() {
        return Collections.unmodifiableList(subMenuItems);
    }
    public void replaceSubMenuItems(List<T> menuItems) {
        subMenuItems.clear();
        subMenuItems.addAll(menuItems);
    }
    public boolean hasSubMenuItems() {
        return subMenuItems.size() > 0;
    }
    
    
    @Getter @Setter(AccessLevel.PROTECTED) private T parent;
    public boolean hasParent() {
        return parent != null;
    }
}
