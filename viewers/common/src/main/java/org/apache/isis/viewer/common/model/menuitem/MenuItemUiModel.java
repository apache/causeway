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
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.component.CssClassFaPosition;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.common.model.action.MenuActionUiModel;

import lombok.AccessLevel;
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
@Log4j2
@Accessors(chain = true)
@RequiredArgsConstructor
public abstract class MenuItemUiModel<T, U extends MenuItemUiModel<T, U>> {
    
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
    
    @Getter @Setter(AccessLevel.PRIVATE) private T actionLinkComponent;
    @Getter @Setter private MenuActionUiModel<T> menuActionUiModel;
    
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
    
    // -- CONSTRUCTION
    
    /**
     * Optionally creates a sub-menu item invoking an action on the provided 
     * {@link MenuActionWkt action model}, based on visibility and usability.
     */
    public void addSubMenuItemFor(
            @NonNull final MenuActionUiModel<T> menuActionModel, 
            @Nullable final Consumer<U> onNewSubMenuItem) {

        val serviceModel = menuActionModel.getServiceModel();
        val objectAction = menuActionModel.getObjectAction();
        val requiresSeparator = menuActionModel.isFirstInSection();
        val actionLinkFactory = menuActionModel.getActionLinkFactory();

        val actionHolder = serviceModel.getManagedObject();
        if(!isVisible(actionHolder, objectAction)) {
            log.debug("not visible {}", objectAction.getName());
            return;
        }

        // build the link
        val linkAndLabel = actionLinkFactory.newLink(objectAction);
        if (linkAndLabel == null) {
            // can only get a null if invisible, so this should not happen given the visibility guard above
            return;
        }

        val actionLabel = menuActionModel.getActionName() != null 
                ? menuActionModel.getActionName() 
                : linkAndLabel.getLabel();

        val menutIem = newSubMenuItem(actionLabel)
                .setDisabledReason(getReasonWhyDisabled(actionHolder, objectAction).orElse(null))
                .setPrototyping(objectAction.isPrototype())
                .setRequiresSeparator(requiresSeparator)
                .setRequiresImmediateConfirmation(
                        ObjectAction.Util.isAreYouSureSemantics(objectAction) &&
                        ObjectAction.Util.isNoParameters(objectAction))
                .setBlobOrClob(ObjectAction.Util.returnsBlobOrClob(objectAction))
                .setDescription(getDescription(objectAction).orElse(null))
                .setActionIdentifier(ObjectAction.Util.actionIdentifierFor(objectAction))
                .setCssClass(ObjectAction.Util.cssClassFor(objectAction, actionHolder))
                .setCssClassFa(ObjectAction.Util.cssClassFaFor(objectAction))
                .setCssClassFaPosition(ObjectAction.Util.cssClassFaPositionFor(objectAction));
        
        menutIem.setActionLinkComponent(linkAndLabel.getLinkComponent());
        
        if(onNewSubMenuItem!=null) {
            onNewSubMenuItem.accept(_Casts.uncheckedCast(menutIem));
        }
    }
    
    protected abstract U newSubMenuItem(final String name);

    
}
