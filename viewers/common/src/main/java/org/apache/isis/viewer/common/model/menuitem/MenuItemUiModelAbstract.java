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
import java.util.Optional;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.component.CssClassFaPosition;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@RequiredArgsConstructor
public abstract class MenuItemUiModelAbstract<T extends MenuItemUiModelAbstract<T>> {
    
    @Getter private final String name;
    @Setter private boolean enabled = true; // unless disabled
    public boolean isEnabled() {
        return enabled && disabledReason == null;
    }
    
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
    /**
     * @param menuItems we assume these have the correct parent already set
     */
    public void replaceSubMenuItems(List<T> menuItems) {
        subMenuItems.clear();
        subMenuItems.addAll(menuItems);
    }
    public boolean hasSubMenuItems() {
        return subMenuItems.size() > 0;
    }
    
    
    @Getter private T parent;
    protected void setParent(T parent) {
        this.parent = parent;
        parent.addSubMenuItem(_Casts.uncheckedCast(this));        
    }
    public boolean hasParent() {
        return parent != null;
    }
    
    // -- VISIBILITY
    
    protected boolean isVisible(
            @NonNull final ManagedObject actionHolder, 
            @NonNull final ObjectAction objectAction) {
        
        // check hidden
        if (actionHolder.getSpecification().isHidden()) {
            return false;
        }
        // check visibility
        final Consent visibility = objectAction.isVisible(
                actionHolder,
                InteractionInitiatedBy.USER,
                Where.ANYWHERE);
        if (visibility.isVetoed()) {
            return false;
        }
        return true;
    }
    
    // -- USABILITY
    
    protected Optional<String> getReasonWhyDisabled(
            @NonNull final ManagedObject actionHolder, 
            @NonNull final ObjectAction objectAction) {
            
        // check usability
        final Consent usability = objectAction.isUsable(
                actionHolder,
                InteractionInitiatedBy.USER,
                Where.ANYWHERE
                );
        return Optional.ofNullable(usability.getReason());
    }
    
    // -- DESCRIBED AS
    
    protected Optional<String> getDescription(
            @NonNull final ObjectAction objectAction) {
        
        val describedAsFacet = objectAction.getFacet(DescribedAsFacet.class);
        return Optional.ofNullable(describedAsFacet)
                .map(DescribedAsFacet::value);
    }

    
}
