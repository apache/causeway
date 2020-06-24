package org.apache.isis.viewer.wicket.model.links;

import java.io.Serializable;
import java.util.Optional;

import org.apache.wicket.markup.html.link.AbstractLink;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.internal.base._Lazy;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.common.model.action.ActionUiMetaModel;
import org.apache.isis.viewer.common.model.object.ObjectUiModel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class LinkAndLabelAbstract implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final ActionLinkUiComponentFactoryWkt uiComponentFactory;
    
    /**
     * used when explicitly named (eg. menu bar layout file), otherwise {@code null}
     */
    @Getter private final String named;
    
    /**
     * domain object that is the <em>Action's</em> holder or owner
     */
    @Getter private final ObjectUiModel actionHolder;
    
    /**
     * framework internal <em>Action</em> model
     */
    @Getter private final ObjectAction objectAction;
    
    // implements HasUiComponent<T>
    @Getter(lazy = true) 
    private final AbstractLink uiComponent = uiComponentFactory.newActionLinkUiComponent(getActionUiMetaModel());
    
    public ActionUiMetaModel getActionUiMetaModel() {
        return actionUiMetaModel.get();
    }
    
    @Override
    public String toString() {
        return Optional.ofNullable(named).orElse("") + 
                " ~ " + objectAction.getIdentifier().toFullIdentityString();
    }
    
    // -- SHORTCUTS
    
    public String getLabel() {
        return getActionUiMetaModel().getLabel();
    }
    
    // -- VISIBILITY
    
    public boolean isVisible() {
        return isVisible(actionHolder.getManagedObject(), objectAction);
    }
    
    private static boolean isVisible(
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
    
    
    // -- HELPER
    
    private final _Lazy<ActionUiMetaModel> actionUiMetaModel = _Lazy.threadSafe(this::createActionUiMetaModel);
    
    private ActionUiMetaModel createActionUiMetaModel() {
        return ActionUiMetaModel.of(actionHolder.getManagedObject(), objectAction);
    }
    
}
