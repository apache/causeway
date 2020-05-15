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
package org.apache.isis.viewer.wicket.model.models;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.wicket.model.Model;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Collections;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.webapp.context.memento.ObjectMemento;
import org.apache.isis.viewer.common.model.feature.ScalarUiModel;
import org.apache.isis.viewer.common.model.object.ObjectUiModel;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.HasRenderingHints;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.Mode;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.RenderingHint;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.links.LinksProvider;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Represents a scalar of an entity, either a {@link Kind#PROPERTY property} or
 * a {@link Kind#PARAMETER parameter}.
 *
 * <p>
 * Is the backing model to each of the fields that appear in forms (for entities
 * or action dialogs).
 *
 */
@Log4j2
public abstract class ScalarModel 
extends ManagedObjectModel 
implements HasRenderingHints, ScalarUiModel, LinksProvider, FormExecutorContext {

    private static final long serialVersionUID = 1L;

    private enum Kind {
        PROPERTY,
        PARAMETER;
    }
    @NonNull private final Kind kind;
    public boolean isProperty() { return kind == Kind.PROPERTY; }
    public boolean isParameter() { return kind == Kind.PARAMETER; }
    

    private final EntityModel parentEntityModel;
    
    @Getter(onMethod = @__(@Override)) 
    @Setter(onMethod = @__(@Override)) 
    private Mode mode;
    
    @Getter(onMethod = @__(@Override)) 
    @Setter(onMethod = @__(@Override)) 
    private RenderingHint renderingHint;
    

    /**
     * Creates a model representing an action parameter of an action of a parent
     * object, with the {@link #getObject() value of this model} to be default
     * value (if any) of that action parameter.
     */
    protected ScalarModel(EntityModel parentEntityModel, ActionParameterMemento apm) {
        
        super(parentEntityModel.getCommonContext());
        
        this.kind = Kind.PARAMETER;
        this.parentEntityModel = parentEntityModel;
        this.pendingModel = new PendingModel(this);
        this.mode = ObjectUiModel.Mode.EDIT;
        this.renderingHint = ObjectUiModel.RenderingHint.REGULAR;
    }

    /**
     * Creates a model representing a property of a parent object, with the
     * {@link #getObject() value of this model} to be current value of the
     * property.
     */
    protected ScalarModel(
            EntityModel parentEntityModel, 
            PropertyMemento pm,
            ObjectUiModel.Mode mode, 
            ObjectUiModel.RenderingHint renderingHint) {
        
        super(parentEntityModel.getCommonContext());
        this.kind = Kind.PROPERTY;
        this.parentEntityModel = parentEntityModel;
        this.pendingModel = new PendingModel(this);
        this.mode = mode;
        this.renderingHint = renderingHint;
    }

    protected ManagedObject loadFromSuper() {
        return super.load();
    }

    @Override
    public EntityModel getParentUiModel() {
        return parentEntityModel;
    }

    private transient ManagedObject owner;
    @Override
    public ManagedObject getOwner() {
        if(owner==null) {
            owner = getParentUiModel().load(); 
        }
        return owner;  
    }
    
    /**
     * Whether the scalar represents a {@link Kind#PROPERTY property} or a
     * {@link Kind#PARAMETER}.
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * Overrides superclass' implementation, because a {@link ScalarModel} can
     * know the {@link ObjectSpecification of} the {@link ManagedObject adapter}
     * without there necessarily having any adapter 
     * {@link #setObject(ManagedObject) set}.
     */
    @Override
    public ObjectSpecification getTypeOfSpecification() {
        return getScalarTypeSpec();
    }

    @Override
    public Optional<ObjectSpecId> getTypeOfSpecificationId() {
        return Optional.of(getScalarTypeSpec().getSpecId());
    }
    

    public boolean isScalarTypeAnyOf(final Class<?>... requiredClass) {
        final String fullName = getTypeOfSpecification().getFullIdentifier();
        return _NullSafe.stream(requiredClass)
                .map(Class::getName)
                .anyMatch(fullName::equals);
    }

    public boolean isScalarTypeSubtypeOf(final Class<?> requiredClass) {
        final Class<?> scalarType = getTypeOfSpecification().getCorrespondingClass();
        return _NullSafe.streamNullable(requiredClass)
                .anyMatch(x -> x.isAssignableFrom(scalarType));
    }

    public String getObjectAsString() {
        final ManagedObject adapter = getObject();
        if (adapter == null) {
            return null;
        }
        return adapter.titleString(null);
    }

    public void setObjectAsString(final String enteredText) {
        // parse text to get adapter
        ParseableFacet parseableFacet = getTypeOfSpecification().getFacet(ParseableFacet.class);
        if (parseableFacet == null) {
            throw new RuntimeException("unable to parse string for " + getTypeOfSpecification().getFullIdentifier());
        }
        ManagedObject adapter = parseableFacet.parseTextEntry(getObject(), enteredText,
                InteractionInitiatedBy.USER);

        setObject(adapter);
    }

    public void setPendingAdapter(final ManagedObject objectAdapter) {
        if(isCollection()) {
            val pojos = objectAdapter.getPojo();
            val memento = super.getMementoService()
                    .mementoForPojos(_Casts.uncheckedCast(pojos), getTypeOfSpecification().getSpecId());
            setPendingMemento(memento);
        } else {
            val memento = super.getMementoService()
                    .mementoForObject(objectAdapter);
            setPendingMemento(memento);
        }
    }
    
    public boolean whetherHidden() {
        final Where where = getRenderingHint().asWhere();
        return whetherHidden(where);
    }

    protected abstract boolean whetherHidden(Where where);

    public String whetherDisabled() {
        final Where where = getRenderingHint().asWhere();
        return whetherDisabled(where);
    }

    protected abstract String whetherDisabled(Where where);

    public abstract String validate(ManagedObject proposedAdapter);

    public boolean isRequired() {
        return !getMetaModel().isOptional();
    }

    public abstract String getCssClass();

    public final <T extends Facet> T getFacet(final Class<T> facetType) {
        return getMetaModel().getFacet(facetType);
    }

    /**
     * Additional links to render (if any)
     */
    private List<LinkAndLabel> linkAndLabels = _Lists.newArrayList();

    @Override
    public List<LinkAndLabel> getLinks() {
        return Collections.unmodifiableList(linkAndLabels);
    }

    @Override
    public PromptStyle getPromptStyle() {
        final PromptStyleFacet facet = getFacet(PromptStyleFacet.class);
        if(facet == null) {
            // don't think this can happen actually, see PromptStyleFacetFallback
            return PromptStyle.INLINE;
        }
        PromptStyle promptStyle = facet.value();
        if (promptStyle == PromptStyle.AS_CONFIGURED) {
            // I don't think this can happen, actually...
            // when the metamodel is built, it should replace AS_CONFIGURED with one of the other prompts
            // (see PromptStyleConfiguration and PromptStyleFacetFallback)
            return PromptStyle.INLINE;
        }
        return promptStyle;
    }

    public boolean canEnterEditMode() {
        boolean editable = isEnabled();
        return editable && isViewMode();
    }

    public boolean isEnabled() {
        return whetherDisabled() == null;
    }


    @Override
    protected void onDetach() {
        clearPending();
        super.onDetach();
    }

    // //////////////////////////////////////

    //    @Override
    //    public boolean isInlinePrompt() {
    //        return getPromptStyle() == PromptStyle.INLINE && canEnterEditMode();
    //    }


    private InlinePromptContext inlinePromptContext;

    /**
     * Further hint, to support inline prompts...
     */
    @Override
    public InlinePromptContext getInlinePromptContext() {
        return inlinePromptContext;
    }

    public void setInlinePromptContext(InlinePromptContext inlinePromptContext) {
        if (this.inlinePromptContext != null) {
            // otherwise the components created for an property edit inline prompt will overwrite the original
            // components on the underlying page (which we go back to if the prompt is cancelled).
            return;
        }
        this.inlinePromptContext = inlinePromptContext;
    }

    protected transient AssociatedActions associatedActions;

    public static class AssociatedActions {
        private final ObjectAction firstAssociatedWithInlineAsIfEdit;
        private final List<ObjectAction> remainingAssociated;

        AssociatedActions(final Can<ObjectAction> allAssociated) {
            final List<ObjectAction> temp = allAssociated.toList();
            this.firstAssociatedWithInlineAsIfEdit = firstAssociatedActionWithInlineAsIfEdit(allAssociated);
            if(this.firstAssociatedWithInlineAsIfEdit != null) {
                temp.remove(firstAssociatedWithInlineAsIfEdit);
            }
            remainingAssociated = Collections.unmodifiableList(temp);
        }

        public List<ObjectAction> getRemainingAssociated() {
            return remainingAssociated;
        }
        public ObjectAction getFirstAssociatedWithInlineAsIfEdit() {
            return firstAssociatedWithInlineAsIfEdit;
        }
        public boolean hasAssociatedActionWithInlineAsIfEdit() {
            return firstAssociatedWithInlineAsIfEdit != null;
        }

        private static ObjectAction firstAssociatedActionWithInlineAsIfEdit(final Can<ObjectAction> objectActions) {
            for (ObjectAction objectAction : objectActions) {
                final PromptStyle promptStyle = ObjectAction.Util.promptStyleFor(objectAction);
                if(promptStyle.isInlineAsIfEdit()) {
                    return objectAction;
                }
            }
            return null;
        }
    }


    /**
     * Whether this model should be surfaced in the UI using a widget rendered such that it is either already in
     * edit mode (eg for a parameter), or can be switched into edit mode, eg for an editable property or an
     * associated action of a property with 'inline_as_if_edit'
     *
     * @return <tt>true</tt> if the widget for this model must be editable.
     */
    public boolean mustBeEditable() {
        return getMode() == Mode.EDIT 
                || getKind() == Kind.PARAMETER
                || hasAssociatedActionWithInlineAsIfEdit();
    }

    /**
     * Similar to {@link #mustBeEditable()}, though not called from the same locations.
     *
     * My suspicion is that it amounts to more or less the same set of conditions.
     *
     * @return
     */
    @Override
    public boolean isInlinePrompt() {
        return (getPromptStyle().isInline() && canEnterEditMode()) 
                || hasAssociatedActionWithInlineAsIfEdit();
    }

    @Override
    public String toString() {
        return toStringOf();
    }

    protected abstract String toStringOf();

    protected abstract ObjectSpecification getScalarTypeSpec();

    protected abstract String getIdentifier();

    public AssociatedActions getAssociatedActions() {
        if (associatedActions == null) {
            associatedActions = new AssociatedActions(calcAssociatedActions());
        }
        return associatedActions;
    }
    
    protected abstract Can<ObjectAction> calcAssociatedActions();
    
    public final boolean hasAssociatedActionWithInlineAsIfEdit() {
        return getAssociatedActions().hasAssociatedActionWithInlineAsIfEdit();
    }
    
    // //////////////////////////////////////////////////////////
    // Pending
    // //////////////////////////////////////////////////////////
    
    private final PendingModel pendingModel;

    @RequiredArgsConstructor
    private static final class PendingModel extends Model<ObjectMemento> {
        private static final long serialVersionUID = 1L;

        @NonNull private final ManagedObjectModel pendingValueModel;

        /**
         * Whether pending has been set (could have been set to null)
         */
        private boolean hasPending;
        /**
         * The new value (could be set to null; hasPending is used to distinguish).
         */
        private ObjectMemento pending;

        @Override
        public ObjectMemento getObject() {
            if (hasPending) {
                return pending;
            }
            
            if(pendingValueModel.memento()!=null) {
                return pendingValueModel.memento();
            }
            
            //XXX [a.huber] as I don't understand the big picture here, given newly introduced branch above,
            // there might be a slight chance, that this is dead code anyway ...
            val adapter = pendingValueModel.getObject();
            val pojo = adapter.getPojo();
            if(pojo!=null && _Collections.isCollectionOrArrayOrCanType(pojo.getClass())) {
                val specId = pendingValueModel.getTypeOfSpecification().getSpecId();
                log.warn("potentially a bug, wild guess fix for non-scalar %s", specId);
                val pojos = _NullSafe.streamAutodetect(pojo)
                        .collect(Collectors.<Object>toList());
                return pendingValueModel.getMementoService().mementoForPojos(pojos, specId);
            }
            return pendingValueModel.getMementoService().mementoForObject(adapter);
        }

        @Override
        public void setObject(final ObjectMemento adapterMemento) {
            pending = adapterMemento;
            hasPending = true;
        }

        public void clearPending() {
            this.hasPending = false;
            this.pending = null;
        }

        private ManagedObject getPendingAdapter() {
            val memento = getObject();
            return pendingValueModel.getCommonContext().reconstructObject(memento);
        }

        public ManagedObject getPendingElseCurrentAdapter() {
            return hasPending ? getPendingAdapter() : pendingValueModel.getObject();
        }

        public ObjectMemento getPendingMemento() {
            return pending;
        }

        public void setPendingMemento(ObjectMemento selectedAdapterMemento) {
            this.pending = selectedAdapterMemento;
            hasPending=true;
        }
    }


    public ManagedObject getPendingElseCurrentAdapter() {
        return pendingModel.getPendingElseCurrentAdapter();
    }

    public ManagedObject getPendingAdapter() {
        return pendingModel.getPendingAdapter();
    }

    public ObjectMemento getPendingMemento() {
        return pendingModel.getPendingMemento();
    }

    public void setPendingMemento(ObjectMemento selectedAdapterMemento) {
        pendingModel.setPendingMemento(selectedAdapterMemento);
    }

    public void clearPending() {
        pendingModel.clearPending();
    }
    
    // //////////////////////////////////////////////////////////
    
}
