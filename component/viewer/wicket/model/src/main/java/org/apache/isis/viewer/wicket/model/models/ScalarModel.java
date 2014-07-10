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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.wicket.Session;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.propparam.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.objpropparam.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueFacet;
import org.apache.isis.core.metamodel.facets.value.string.StringValueSemanticsProvider;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.links.LinksProvider;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;
import org.apache.isis.viewer.wicket.model.mementos.SpecUtils;

/**
 * Represents a scalar of an entity, either a {@link Kind#PROPERTY property} or
 * a {@link Kind#PARAMETER parameter}.
 * 
 * <p>
 * Is the backing model to each of the fields that appear in forms (for entities
 * or action dialogs).
 */
public class ScalarModel extends EntityModel implements LinksProvider {

    private static final long serialVersionUID = 1L;

    public enum Kind {
        PROPERTY {
            @Override
            public String getName(final ScalarModel scalarModel) {
                return scalarModel.getPropertyMemento().getProperty().getName();
            }

            @Override
            public ObjectSpecification getScalarTypeSpec(final ScalarModel scalarModel) {
                ObjectSpecId type = scalarModel.getPropertyMemento().getType();
                return SpecUtils.getSpecificationFor(type);
            }

            @Override
            public String getIdentifier(final ScalarModel scalarModel) {
                return scalarModel.getPropertyMemento().getIdentifier();
            }

            @Override
            public String getLongName(final ScalarModel scalarModel) {
                ObjectSpecId objectSpecId = scalarModel.parentObjectAdapterMemento.getObjectSpecId();
                final String specShortName = SpecUtils.getSpecificationFor(objectSpecId).getShortIdentifier();
                return specShortName + "-" + scalarModel.getPropertyMemento().getProperty().getId();
            }

            @Override
            public String disable(final ScalarModel scalarModel, final Where where) {
                final ObjectAdapter parentAdapter = scalarModel.parentObjectAdapterMemento.getObjectAdapter(ConcurrencyChecking.CHECK);
                final OneToOneAssociation property = scalarModel.getPropertyMemento().getProperty();
                try {
                    final AuthenticationSession session = scalarModel.getAuthenticationSession();
                    final Consent usable = property.isUsable(session, parentAdapter, where);
                    return usable.isAllowed() ? null : usable.getReason();
                } catch (final Exception ex) {
                    return ex.getLocalizedMessage();
                }
            }

            @Override
            public String parseAndValidate(final ScalarModel scalarModel, final String proposedPojoAsStr) {
                final OneToOneAssociation property = scalarModel.getPropertyMemento().getProperty();
                ParseableFacet parseableFacet = property.getFacet(ParseableFacet.class);
                if (parseableFacet == null) {
                    parseableFacet = property.getSpecification().getFacet(ParseableFacet.class);
                }
                try {
                    final ObjectAdapter parentAdapter = scalarModel.parentObjectAdapterMemento.getObjectAdapter(ConcurrencyChecking.CHECK);
                    final ObjectAdapter currentValue = property.get(parentAdapter);
                    Localization localization = IsisContext.getLocalization(); 
                    final ObjectAdapter proposedAdapter = parseableFacet.parseTextEntry(currentValue, proposedPojoAsStr, localization);
                    final Consent valid = property.isAssociationValid(parentAdapter, proposedAdapter);
                    return valid.isAllowed() ? null : valid.getReason();
                } catch (final ConcurrencyException ex) {
                    // disregard concurrency exceptions because will pick up at the IFormValidator level rather
                    // than each individual property.
                    return null;
                } catch (final Exception ex) {
                    return ex.getLocalizedMessage();
                }
            }

            @Override
            public String validate(final ScalarModel scalarModel, final ObjectAdapter proposedAdapter) {
                final ObjectAdapter parentAdapter = scalarModel.parentObjectAdapterMemento.getObjectAdapter(ConcurrencyChecking.CHECK);
                final OneToOneAssociation property = scalarModel.getPropertyMemento().getProperty();
                try {
                    final Consent valid = property.isAssociationValid(parentAdapter, proposedAdapter);
                    return valid.isAllowed() ? null : valid.getReason();
                } catch (final Exception ex) {
                    return ex.getLocalizedMessage();
                }
            }

            @Override
            public boolean isRequired(final ScalarModel scalarModel) {
                final FacetHolder facetHolder = scalarModel.getPropertyMemento().getProperty();
                return isRequired(facetHolder);
            }

            @Override
            public <T extends Facet> T getFacet(final ScalarModel scalarModel, final Class<T> facetType) {
                final FacetHolder facetHolder = scalarModel.getPropertyMemento().getProperty();
                return facetHolder.getFacet(facetType);
            }

            @Override
            public boolean hasChoices(final ScalarModel scalarModel) {
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty();
                return property.hasChoices();
            }

            @Override
            public List<ObjectAdapter> getChoices(final ScalarModel scalarModel, final ObjectAdapter[] argumentsIfAvailable) {
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty();
                final ObjectAdapter[] choices = property.getChoices(scalarModel.parentObjectAdapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK));
                return choicesAsList(choices);
            }

            @Override
            public boolean hasAutoComplete(final ScalarModel scalarModel) {
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty();
                return property.hasAutoComplete();
            }

            @Override
            public List<ObjectAdapter> getAutoComplete(final ScalarModel scalarModel, String searchArg) {
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty();
                final ObjectAdapter[] choices = property.getAutoComplete(scalarModel.parentObjectAdapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK), searchArg);
                return choicesAsList(choices);
            }

            @Override
            public int getAutoCompleteOrChoicesMinLength(ScalarModel scalarModel) {
                
                if (scalarModel.hasAutoComplete()) {
                    final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                    final OneToOneAssociation property = propertyMemento.getProperty();
                    return property.getAutoCompleteMinLength();
                } else {
                    return 0;
                }
            }

            
            @Override
            public void resetVersion(ScalarModel scalarModel) {
                scalarModel.parentObjectAdapterMemento.resetVersion();
            }

            @Override
            public String getDescribedAs(final ScalarModel scalarModel) {
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty();
                return property.getDescription();
            }

            @Override
            public Integer getLength(ScalarModel scalarModel) {
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty();
                final BigDecimalValueFacet facet = property.getFacet(BigDecimalValueFacet.class);
                return facet != null? facet.getLength(): null;
            }
            
            @Override
            public Integer getScale(ScalarModel scalarModel) {
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty();
                final BigDecimalValueFacet facet = property.getFacet(BigDecimalValueFacet.class);
                return facet != null? facet.getScale(): null;
            }
            
            @Override
            public int getTypicalLength(ScalarModel scalarModel) {
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty();
                final TypicalLengthFacet facet = property.getFacet(TypicalLengthFacet.class);
                return facet != null? facet.value() : StringValueSemanticsProvider.TYPICAL_LENGTH;
            }

            @Override
            public void reset(ScalarModel scalarModel) {
                final OneToOneAssociation property = scalarModel.propertyMemento.getProperty();
                final ObjectAdapter associatedAdapter = property.get(scalarModel.parentObjectAdapterMemento.getObjectAdapter(ConcurrencyChecking.CHECK));

                scalarModel.setObject(associatedAdapter);
            }
        },
        PARAMETER {
            @Override
            public String getName(final ScalarModel scalarModel) {
                return scalarModel.getParameterMemento().getActionParameter().getName();
            }

            @Override
            public ObjectSpecification getScalarTypeSpec(final ScalarModel scalarModel) {
                return scalarModel.getParameterMemento().getSpecification();
            }

            @Override
            public String getIdentifier(final ScalarModel scalarModel) {
                return "" + scalarModel.getParameterMemento().getNumber();
            }

            @Override
            public String getLongName(final ScalarModel scalarModel) {
                final ObjectAdapterMemento adapterMemento = scalarModel.getObjectAdapterMemento();
                if (adapterMemento == null) {
                    // shouldn't happen
                    return null;
                }
                ObjectSpecId objectSpecId = adapterMemento.getObjectSpecId();
                final String specShortName = SpecUtils.getSpecificationFor(objectSpecId).getShortIdentifier();
                final String parmId = scalarModel.getParameterMemento().getActionParameter().getIdentifier().toNameIdentityString();
                return specShortName + "-" + parmId + "-" + scalarModel.getParameterMemento().getNumber();
            }

            @Override
            public String disable(final ScalarModel scalarModel, Where where) {
                // always enabled
                return null;
            }

            @Override
            public String parseAndValidate(final ScalarModel scalarModel, final String proposedPojoAsStr) {
                final ObjectActionParameter parameter = scalarModel.getParameterMemento().getActionParameter();
                ParseableFacet parseableFacet = parameter.getFacet(ParseableFacet.class);
                if (parseableFacet == null) {
                    parseableFacet = parameter.getSpecification().getFacet(ParseableFacet.class);
                }
                try {
                    final ObjectAdapter parentAdapter = scalarModel.parentObjectAdapterMemento.getObjectAdapter(ConcurrencyChecking.CHECK);
                    Localization localization = IsisContext.getLocalization(); 
                    final String invalidReasonIfAny = parameter.isValid(parentAdapter, proposedPojoAsStr, localization);
                    return invalidReasonIfAny;
                } catch (final Exception ex) {
                    return ex.getLocalizedMessage();
                }
            }

            @Override
            public String validate(final ScalarModel scalarModel, final ObjectAdapter proposedAdapter) {
                // TODO - not yet supported.
                return null;
            }

            @Override
            public boolean isRequired(final ScalarModel scalarModel) {
                final FacetHolder facetHolder = scalarModel.getParameterMemento().getActionParameter();
                return isRequired(facetHolder);
            }

            @Override
            public <T extends Facet> T getFacet(final ScalarModel scalarModel, final Class<T> facetType) {
                final FacetHolder facetHolder = scalarModel.getParameterMemento().getActionParameter();
                return facetHolder.getFacet(facetType);
            }

            @Override
            public boolean hasChoices(final ScalarModel scalarModel) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter();
                return actionParameter.hasChoices();
            }
            @Override
            public List<ObjectAdapter> getChoices(final ScalarModel scalarModel, final ObjectAdapter[] argumentsIfAvailable) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter();
                final ObjectAdapter[] choices = actionParameter.getChoices(scalarModel.parentObjectAdapterMemento.getObjectAdapter(ConcurrencyChecking.CHECK), argumentsIfAvailable);
                return choicesAsList(choices);
            }

            @Override
            public boolean hasAutoComplete(final ScalarModel scalarModel) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter();
                return actionParameter.hasAutoComplete();
            }
            @Override
            public List<ObjectAdapter> getAutoComplete(final ScalarModel scalarModel, final String searchArg) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter();
                final ObjectAdapter[] choices = actionParameter.getAutoComplete(scalarModel.parentObjectAdapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK), searchArg);
                return choicesAsList(choices);
            }

            @Override
            public int getAutoCompleteOrChoicesMinLength(ScalarModel scalarModel) {
                if (scalarModel.hasAutoComplete()) {
                    final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                    final ObjectActionParameter actionParameter = parameterMemento.getActionParameter();
                    return actionParameter.getAutoCompleteMinLength();
                } else {
                    return 0;
                }
            }

            @Override
            public void resetVersion(ScalarModel scalarModel) {
                // no-op?
            }
            @Override
            public String getDescribedAs(final ScalarModel scalarModel) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter();
                return actionParameter.getDescription();
            }

            @Override
            public Integer getLength(ScalarModel scalarModel) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter();
                final BigDecimalValueFacet facet = actionParameter.getFacet(BigDecimalValueFacet.class);
                return facet != null? facet.getLength(): null;
            }
            
            @Override
            public Integer getScale(ScalarModel scalarModel) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter();
                final BigDecimalValueFacet facet = actionParameter.getFacet(BigDecimalValueFacet.class);
                return facet != null? facet.getScale(): null;
            }
            
            @Override
            public int getTypicalLength(ScalarModel scalarModel) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter();
                final TypicalLengthFacet facet = actionParameter.getFacet(TypicalLengthFacet.class);
                return facet != null? facet.value() : StringValueSemanticsProvider.TYPICAL_LENGTH;
            }

            @Override
            public void reset(ScalarModel scalarModel) {
                final ObjectActionParameter actionParameter = scalarModel.parameterMemento.getActionParameter();
                final ObjectAdapter defaultAdapter = actionParameter.getDefault(scalarModel.parentObjectAdapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK));
                scalarModel.setObject(defaultAdapter);
            }
        };

        private static List<ObjectAdapter> choicesAsList(final ObjectAdapter[] choices) {
            if (choices != null && choices.length > 0) {
                return Arrays.asList(choices);
            }
            return Collections.emptyList();
        }

        public abstract String getName(ScalarModel scalarModel);

        public abstract ObjectSpecification getScalarTypeSpec(ScalarModel scalarModel);

        public abstract String getIdentifier(ScalarModel scalarModel);

        public abstract String disable(ScalarModel scalarModel, Where where);

        public abstract String parseAndValidate(ScalarModel scalarModel, String proposedPojoAsStr);

        public abstract String validate(ScalarModel scalarModel, ObjectAdapter proposedAdapter);

        public abstract String getLongName(ScalarModel scalarModel);

        public abstract boolean isRequired(ScalarModel scalarModel);

        public abstract <T extends Facet> T getFacet(ScalarModel scalarModel, Class<T> facetType);

        static boolean isRequired(final FacetHolder facetHolder) {
            final MandatoryFacet mandatoryFacet = facetHolder.getFacet(MandatoryFacet.class);
            final boolean required = mandatoryFacet != null && !mandatoryFacet.isInvertedSemantics();
            return required;
        }

        public abstract boolean hasChoices(ScalarModel scalarModel);
        public abstract List<ObjectAdapter> getChoices(ScalarModel scalarModel, final ObjectAdapter[] argumentsIfAvailable);

        public abstract boolean hasAutoComplete(ScalarModel scalarModel);
        public abstract List<ObjectAdapter> getAutoComplete(ScalarModel scalarModel, String searchArg);
        public abstract int getAutoCompleteOrChoicesMinLength(ScalarModel scalarModel);
        
        public abstract void resetVersion(ScalarModel scalarModel);

        public abstract String getDescribedAs(ScalarModel scalarModel);


        public abstract Integer getLength(ScalarModel scalarModel);
        public abstract Integer getScale(ScalarModel scalarModel);

        public abstract int getTypicalLength(ScalarModel scalarModel);
        
        public abstract void reset(ScalarModel scalarModel);


    }

    private final Kind kind;
    
    private final ObjectAdapterMemento parentObjectAdapterMemento;
    

    /**
     * Populated only if {@link #getKind()} is {@link Kind#PARAMETER}
     */
    private ActionParameterMemento parameterMemento;

    /**
     * Populated only if {@link #getKind()} is {@link Kind#PROPERTY}
     */
    private PropertyMemento propertyMemento;

    /**
     * Creates a model representing an action parameter of an action of a parent
     * object, with the {@link #getObject() value of this model} to be default
     * value (if any) of that action parameter.
     */
    public ScalarModel(final ObjectAdapterMemento parentObjectAdapterMemento, final ActionParameterMemento apm) {
        this.kind = Kind.PARAMETER;
        this.parentObjectAdapterMemento = parentObjectAdapterMemento;
        this.parameterMemento = apm;

        reset();
        setMode(Mode.EDIT);
    }

    /**
     * Creates a model representing a property of a parent object, with the
     * {@link #getObject() value of this model} to be current value of the
     * property.
     */
    public ScalarModel(final ObjectAdapterMemento parentObjectAdapterMemento, final PropertyMemento pm) {
        this.kind = Kind.PROPERTY;
        this.parentObjectAdapterMemento = parentObjectAdapterMemento;
        this.propertyMemento = pm;

        reset();
        setObject(parentObjectAdapterMemento);
        setMode(Mode.VIEW);
    }

    public void reset() {
        kind.reset(this);
    }
    
    public ObjectAdapterMemento getParentObjectAdapterMemento() {
        return parentObjectAdapterMemento;
    }

    protected void setObject(final ObjectAdapterMemento parentObjectAdapterMemento) {
        final OneToOneAssociation property = propertyMemento.getProperty();
        final ObjectAdapter associatedAdapter = property.get(parentObjectAdapterMemento.getObjectAdapter(ConcurrencyChecking.CHECK));

        setObject(associatedAdapter);
    }

    /**
     * Whether the scalar represents a {@link Kind#PROPERTY property} or a
     * {@link Kind#PARAMETER}.
     */
    public Kind getKind() {
        return kind;
    }

    public String getName() {
        return kind.getName(this);
    }

    /**
     * Populated only if {@link #getKind()} is {@link Kind#PROPERTY}
     */
    public PropertyMemento getPropertyMemento() {
        return propertyMemento;
    }

    /**
     * Populated only if {@link #getKind()} is {@link Kind#PARAMETER}
     */
    public ActionParameterMemento getParameterMemento() {
        return parameterMemento;
    }

    /**
     * Overrides superclass' implementation, because a {@link ScalarModel} can
     * know the {@link ObjectSpecification of} the {@link ObjectAdapter adapter}
     * without there necessarily being any adapter being
     * {@link #setObject(ObjectAdapter) set}.
     */
    @Override
    public ObjectSpecification getTypeOfSpecification() {
        return kind.getScalarTypeSpec(this);
    }

    public boolean isScalarTypeAnyOf(final Class<?>... requiredClass) {
        final String fullName = getTypeOfSpecification().getFullIdentifier();
        for (final Class<?> requiredCls : requiredClass) {
            if (fullName.equals(requiredCls.getName())) {
                return true;
            }
        }
        return false;
    }

    public String getObjectAsString() {
        final ObjectAdapter adapter = getObject();
        if (adapter == null) {
            return null;
        }
        return adapter.titleString(null);
    }

    @Override
    public void setObject(final ObjectAdapter adapter) {
        super.setObject(adapter); // associated value
    }

    public void setObjectAsString(final String enteredText) {
        // parse text to get adapter
        final ParseableFacet parseableFacet = getTypeOfSpecification().getFacet(ParseableFacet.class);
        if (parseableFacet == null) {
            throw new RuntimeException("unable to parse string for " + getTypeOfSpecification().getFullIdentifier());
        }
        Localization localization = IsisContext.getLocalization(); 
        final ObjectAdapter adapter = parseableFacet.parseTextEntry(getObject(), enteredText, localization);

        setObject(adapter);
    }

    public String disable(Where where) {
        return kind.disable(this, where);
    }

    public String parseAndValidate(final String proposedPojoAsStr) {
        return kind.parseAndValidate(this, proposedPojoAsStr);
    }

    public String validate(final ObjectAdapter proposedAdapter) {
        return kind.validate(this, proposedAdapter);
    }

    /**
     * Default implementation looks up from singleton, but can be overridden for
     * testing.
     */
    protected AuthenticationSession getAuthenticationSession() {
        return ((AuthenticationSessionProvider) Session.get()).getAuthenticationSession();
    }

    public boolean isRequired() {
        return kind.isRequired(this);
    }

    public String getLongName() {
        return kind.getLongName(this);
    }

    public <T extends Facet> T getFacet(final Class<T> facetType) {
        return kind.getFacet(this, facetType);
    }

    public void resetVersion() {
        kind.resetVersion(this);
    }

    public String getDescribedAs() {
        return kind.getDescribedAs(this);
    }

    public boolean hasChoices() {
        return kind.hasChoices(this);
    }

    public List<ObjectAdapter> getChoices(final ObjectAdapter[] argumentsIfAvailable) {
        return kind.getChoices(this, argumentsIfAvailable);
    }

    public boolean hasAutoComplete() {
        return kind.hasAutoComplete(this);
    }

    public List<ObjectAdapter> getAutoComplete(String searchTerm) {
        return kind.getAutoComplete(this, searchTerm);
    }

    /**
     * for {@link BigDecimal}s only.
     * 
     * @see #getScale()
     */
    public int getLength() {
        return kind.getLength(this);
    }

    /**
     * for {@link BigDecimal}s only.
     * 
     * @see #getLength()
     */
    public Integer getScale() {
        return kind.getScale(this);
    }

    public int getTypicalLength() {
        return kind.getTypicalLength(this);
    }

    /**
     * Additional links to render (if any)
     */
    private List<LinkAndLabel> entityActions = Lists.newArrayList();

    
    public void addEntityActions(List<LinkAndLabel> entityActions) {
        this.entityActions.addAll(entityActions);
    }

    @Override
    public List<LinkAndLabel> getLinks() {
        return Collections.unmodifiableList(entityActions);
    }

    /**
     * @return
     */
    public int getAutoCompleteMinLength() {
        return kind.getAutoCompleteOrChoicesMinLength(this);
    }

    /**
     * @return
     */
    public ScalarModelWithPending asScalarModelWithPending() {
        return new ScalarModelWithPending(){

            private static final long serialVersionUID = 1L;

            @Override
            public ObjectAdapterMemento getPending() {
                return ScalarModel.this.getPending();
            }

            @Override
            public void setPending(ObjectAdapterMemento pending) {
                ScalarModel.this.setPending(pending);
            }

            @Override
            public ScalarModel getScalarModel() {
                return ScalarModel.this;
            }
        };
    }


    // //////////////////////////////////////

    /**
     * transient because only temporary hint.
     */
    private transient ObjectAdapter[] actionArgsHint;

    public void setActionArgsHint(ObjectAdapter[] actionArgsHint) {
        this.actionArgsHint = actionArgsHint;
    }

    /**
     * The initial call of choicesXxx() for any given scalar argument needs the current values
     * of all args (possibly as initialized through a defaultNXxx().
     */
    public ObjectAdapter[] getActionArgsHint() {
        return actionArgsHint;
    }


}
