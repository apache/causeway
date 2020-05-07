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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.specimpl.PendingParameterModel;
import org.apache.isis.core.webapp.context.memento.ObjectMemento;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.links.LinksProvider;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;

import lombok.NonNull;
import lombok.val;


/**
 * Represents a scalar of an entity, either a {@link Kind#PROPERTY property} or
 * a {@link Kind#PARAMETER parameter}.
 *
 * <p>
 * Is the backing model to each of the fields that appear in forms (for entities
 * or action dialogs).
 *
 * <p>
 *     NOTE: although this inherits from {@link EntityModel}, this is wrong I think; what is being shared
 *     is just some of the implementation - both objects have to wrap some arbitrary memento holding some state
 *     (a value or entity reference in a ScalarModel's case, an entity reference in an EntityModel's), they have
 *     a view mode, they have a rendering hint, and scalar models have a pending value (not sure if Entity Model really
 *     requires this).
 *     Fundamentally though a ScalarModel is NOT really an EntityModel, so this hierarchy should be broken out with a
 *     common superclass for both EntityModel and ScalarModel.
 * </p>
 */
public abstract class ScalarModel extends EntityModel 
implements LinksProvider, FormExecutorContext {

    private static final long serialVersionUID = 1L;

    private enum Kind {
        PROPERTY,
        PARAMETER;
    }
    @NonNull private final Kind kind;
    public boolean isProperty() { return kind == Kind.PROPERTY; }
    public boolean isParameter() { return kind == Kind.PARAMETER; }
    

    static boolean isRequired(final FacetHolder facetHolder) {
        final MandatoryFacet mandatoryFacet = facetHolder.getFacet(MandatoryFacet.class);
        final boolean required = mandatoryFacet != null && !mandatoryFacet.isInvertedSemantics();
        return required;
    }



    private final EntityModel parentEntityModel;

    /**
     * Creates a model representing an action parameter of an action of a parent
     * object, with the {@link #getObject() value of this model} to be default
     * value (if any) of that action parameter.
     */
    protected ScalarModel(EntityModel parentEntityModel, ActionParameterMemento apm) {
        
        super(parentEntityModel.getCommonContext(),
                EntityModel.Mode.EDIT, 
                EntityModel.RenderingHint.REGULAR);
        
        this.kind = Kind.PARAMETER;
        this.parentEntityModel = parentEntityModel;
    }

    /**
     * Creates a model representing a property of a parent object, with the
     * {@link #getObject() value of this model} to be current value of the
     * property.
     */
    protected ScalarModel(
            EntityModel parentEntityModel, 
            PropertyMemento pm,
            EntityModel.Mode mode, 
            EntityModel.RenderingHint renderingHint) {
        
        super(parentEntityModel.getCommonContext(), mode, renderingHint);
        this.kind = Kind.PROPERTY;
        this.parentEntityModel = parentEntityModel;
    }

    protected ManagedObject loadFromSuper() {
        return super.load();
    }

    @Override
    public EntityModel getParentUiModel() {
        return parentEntityModel;
    }



    protected static void setObjectFromPropertyIfVisible(
            final ScalarModel scalarModel,
            final OneToOneAssociation property,
            final ManagedObject parentAdapter) {

        final Where where = scalarModel.getRenderingHint().asWhere();

        final Consent visibility =
                property.isVisible(parentAdapter, InteractionInitiatedBy.FRAMEWORK, where);

        final ManagedObject associatedAdapter;
        if (visibility.isAllowed()) {
            associatedAdapter = property.get(parentAdapter, InteractionInitiatedBy.USER);
        } else {
            associatedAdapter = null;
        }

        scalarModel.setObject(associatedAdapter);
    }

    public abstract boolean isCollection();

    /**
     * Whether the scalar represents a {@link Kind#PROPERTY property} or a
     * {@link Kind#PARAMETER}.
     */
    public Kind getKind() {
        return kind;
    }

    public abstract String getName();

    /**
     * Overrides superclass' implementation, because a {@link ScalarModel} can
     * know the {@link ObjectSpecification of} the {@link ManagedObject adapter}
     * without there necessarily being any adapter being
     * {@link #setObject(ManagedObject) set}.
     */
    @Override
    public ObjectSpecification getTypeOfSpecification() {
        return getScalarTypeSpec();
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

    @Override
    public void setObject(ManagedObject adapter) {
        if(adapter == null) {
            super.setObject(null);
            return;
        }

        final Object pojo = adapter.getPojo();
        if(pojo == null) {
            super.setObject(null);
            return;
        }

        if(isCollection()) {
            val memento = super.getMementoService()
                    .mementoForPojos(_Casts.uncheckedCast(pojo), getTypeOfSpecification().getSpecId());
                    
            super.setObjectMemento(memento); // associated value
        } else {
            super.setObject(adapter); // associated value
        }
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
            setPending(memento);
        } else {
            val memento = super.getMementoService()
                    .mementoForObject(objectAdapter);
            setPending(memento);
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

    public abstract boolean isRequired();

    public abstract String getCssClass();

    public abstract <T extends Facet> T getFacet(Class<T> facetType);

    public abstract String getDescribedAs();

    public abstract String getFileAccept();

    public abstract boolean hasChoices();
    public abstract Can<ManagedObject> getChoices(PendingParameterModel pendingArgs);

    public abstract boolean hasAutoComplete();
    public abstract Can<ManagedObject> getAutoComplete(PendingParameterModel pendingArgs, String searchTerm);

    /**
     * for {@link BigDecimal}s only.
     *
     * @see #getScale()
     */
    public abstract Integer getLength();

    /**
     * for {@link BigDecimal}s only.
     *
     * @see #getLength()
     */
    public abstract Integer getScale();

    /**
     * Additional links to render (if any)
     */
    private List<LinkAndLabel> linkAndLabels = _Lists.newArrayList();

    @Override
    public List<LinkAndLabel> getLinks() {
        return Collections.unmodifiableList(linkAndLabels);
    }

    /**
     * @return
     */
    public ScalarModelWithPending asScalarModelWithPending() {
        return new ScalarModelWithPending(){

            private static final long serialVersionUID = 1L;

            @Override
            public ObjectMemento getPending() {
                return ScalarModel.this.getPending();
            }

            @Override
            public void setPending(ObjectMemento pending) {
                ScalarModel.this.setPending(pending);
            }

            @Override
            public ScalarModel getScalarModel() {
                return ScalarModel.this;
            }
        };
    }

    /**
     * @return
     */
    public ScalarModelWithMultiPending asScalarModelWithMultiPending() {
        return new ScalarModelWithMultiPending(){

            private static final long serialVersionUID = 1L;

            @Override
            public ArrayList<ObjectMemento> getMultiPending() {
                ObjectMemento pendingMemento = ScalarModel.this.getPending();
                return ObjectMemento.unwrapList(pendingMemento)
                        .orElse(null);
            }

            @Override
            public void setMultiPending(final ArrayList<ObjectMemento> pending) {
                ObjectSpecId specId = getScalarModel().getTypeOfSpecification().getSpecId();
                ObjectMemento adapterMemento = ObjectMemento.wrapMementoList(pending, specId);
                ScalarModel.this.setPending(adapterMemento);
            }

            @Override
            public ScalarModel getScalarModel() {
                return ScalarModel.this;
            }
        };
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

        AssociatedActions(final List<ObjectAction> allAssociated) {
            final List<ObjectAction> temp = _Lists.newArrayList(allAssociated);
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

        private static ObjectAction firstAssociatedActionWithInlineAsIfEdit(final List<ObjectAction> objectActions) {
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

    protected abstract String parseAndValidate(String proposedPojoAsStr);

    protected abstract int getTypicalLength();

    public abstract int getAutoCompleteOrChoicesMinLength();

    public abstract ManagedObject getDefault(PendingParameterModel pendingArgs);

    public int getAutoCompleteMinLength() {
        return getAutoCompleteOrChoicesMinLength();
    }

    public AssociatedActions getAssociatedActions() {
        if (associatedActions == null) {
            associatedActions = new AssociatedActions(calcAssociatedActions());
        }
        return associatedActions;
    }
    
    protected abstract List<ObjectAction> calcAssociatedActions();
    
    public final boolean hasAssociatedActionWithInlineAsIfEdit() {
        return getAssociatedActions().hasAssociatedActionWithInlineAsIfEdit();
    }
    
}
