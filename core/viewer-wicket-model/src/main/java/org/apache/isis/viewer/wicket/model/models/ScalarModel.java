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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.fileaccept.FileAcceptFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueFacet;
import org.apache.isis.core.metamodel.facets.value.string.StringValueSemanticsProvider;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
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
public class ScalarModel extends EntityModel implements LinksProvider, FormExecutorContext, ActionArgumentModel {

    private static final long serialVersionUID = 1L;

    public enum Kind {
        PROPERTY {
            @Override
            public String getName(final ScalarModel scalarModel) {
                return scalarModel.getPropertyMemento().getProperty(scalarModel.getSpecificationLoader()).getName();
            }

            @Override
            public ObjectSpecification getScalarTypeSpec(final ScalarModel scalarModel) {
                ObjectSpecId type = scalarModel.getPropertyMemento().getType();
                return SpecUtils.getSpecificationFor(type, scalarModel.getSpecificationLoader());
            }

            @Override
            public String getIdentifier(final ScalarModel scalarModel) {
                return scalarModel.getPropertyMemento().getIdentifier();
            }

            @Override
            public String getCssClass(final ScalarModel scalarModel) {
                final String objectSpecId =
                        scalarModel.getParentEntityModel().getTypeOfSpecification().getSpecId().asString().replace(".", "-");
                final String propertyId = getIdentifier(scalarModel);
                return "isis-" + objectSpecId + "-" + propertyId;
            }

            @Override
            public boolean whetherHidden(final ScalarModel scalarModel, final Where where) {
                final ObjectAdapter parentAdapter = scalarModel.getParentEntityModel().load();
                final OneToOneAssociation property = scalarModel.getPropertyMemento().getProperty(scalarModel.getSpecificationLoader());
                try {
                    final Consent visibility = property.isVisible(parentAdapter, InteractionInitiatedBy.USER, where);
                    return visibility.isVetoed();
                } catch (final Exception ex) {
                    return true; // will be hidden
                }
            }

            @Override
            public String whetherDisabled(final ScalarModel scalarModel, final Where where) {
                final ObjectAdapter parentAdapter = scalarModel.getParentEntityModel().load();
                final OneToOneAssociation property = scalarModel.getPropertyMemento().getProperty(scalarModel.getSpecificationLoader());
                try {
                    final Consent usable = property.isUsable(parentAdapter, InteractionInitiatedBy.USER, where);
                    return usable.isAllowed() ? null : usable.getReason();
                } catch (final Exception ex) {
                    return ex.getLocalizedMessage();
                }
            }

            @Override
            public String parseAndValidate(final ScalarModel scalarModel, final String proposedPojoAsStr) {
                final OneToOneAssociation property = scalarModel.getPropertyMemento().getProperty(scalarModel.getSpecificationLoader());
                ParseableFacet parseableFacet = property.getFacet(ParseableFacet.class);
                if (parseableFacet == null) {
                    parseableFacet = property.getSpecification().getFacet(ParseableFacet.class);
                }
                try {
                    final ObjectAdapter parentAdapter = scalarModel.getParentEntityModel().load();
                    final ObjectAdapter currentValue = property.get(parentAdapter, InteractionInitiatedBy.USER);
                    final ObjectAdapter proposedAdapter =
                            parseableFacet.parseTextEntry(currentValue, proposedPojoAsStr, InteractionInitiatedBy.USER);
                    final Consent valid = property.isAssociationValid(parentAdapter, proposedAdapter,
                            InteractionInitiatedBy.USER);
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
                final ObjectAdapter parentAdapter = scalarModel.getParentEntityModel().load();
                final OneToOneAssociation property = scalarModel.getPropertyMemento().getProperty(scalarModel.getSpecificationLoader());
                try {
                    final Consent valid = property.isAssociationValid(parentAdapter, proposedAdapter,
                            InteractionInitiatedBy.USER);
                    return valid.isAllowed() ? null : valid.getReason();
                } catch (final Exception ex) {
                    return ex.getLocalizedMessage();
                }
            }

            @Override
            public boolean isRequired(final ScalarModel scalarModel) {
                final FacetHolder facetHolder = scalarModel.getPropertyMemento().getProperty(scalarModel.getSpecificationLoader());
                return isRequired(facetHolder);
            }

            @Override
            public <T extends Facet> T getFacet(final ScalarModel scalarModel, final Class<T> facetType) {
                final FacetHolder facetHolder = scalarModel.getPropertyMemento().getProperty(scalarModel.getSpecificationLoader());
                return facetHolder.getFacet(facetType);
            }

            @Override
            public boolean hasChoices(final ScalarModel scalarModel) {
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty(scalarModel.getSpecificationLoader());
                return property.hasChoices();
            }

            @Override
            public List<ObjectAdapter> getChoices(
                    final ScalarModel scalarModel,
                    final ObjectAdapter[] argumentsIfAvailable,
                    final AuthenticationSession authenticationSession) {
                
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty(scalarModel.getSpecificationLoader());
                ObjectAdapter parentAdapter = scalarModel.getParentEntityModel().load(ConcurrencyChecking.NO_CHECK);
                final ObjectAdapter[] choices = property.getChoices(
                        parentAdapter,
                        InteractionInitiatedBy.USER);

                return choicesAsList(choices);
            }

            @Override
            public boolean hasAutoComplete(final ScalarModel scalarModel) {
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty(scalarModel.getSpecificationLoader());
                return property.hasAutoComplete();
            }

            @Override
            public List<ObjectAdapter> getAutoComplete(
                    final ScalarModel scalarModel,
                    final String searchArg,
                    final AuthenticationSession authenticationSession) {
                
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty(scalarModel.getSpecificationLoader());
                final ObjectAdapter parentAdapter =
                        scalarModel.getParentEntityModel().load(ConcurrencyChecking.NO_CHECK);
                final ObjectAdapter[] choices =
                        property.getAutoComplete(
                                parentAdapter, searchArg,
                                InteractionInitiatedBy.USER);
                return choicesAsList(choices);
            }

            @Override
            public int getAutoCompleteOrChoicesMinLength(ScalarModel scalarModel) {

                if (scalarModel.hasAutoComplete()) {
                    final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                    final OneToOneAssociation property = propertyMemento.getProperty(scalarModel.getSpecificationLoader());
                    return property.getAutoCompleteMinLength();
                } else {
                    return 0;
                }
            }


            @Override
            public void resetVersion(ScalarModel scalarModel) {
                scalarModel.getParentEntityModel().resetVersion();
            }

            @Override
            public String getDescribedAs(final ScalarModel scalarModel) {
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty(scalarModel.getSpecificationLoader());
                return property.getDescription();
            }

            @Override
            public Integer getLength(ScalarModel scalarModel) {
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty(scalarModel.getSpecificationLoader());
                final BigDecimalValueFacet facet = property.getFacet(BigDecimalValueFacet.class);
                return facet != null? facet.getPrecision(): null;
            }

            @Override
            public Integer getScale(ScalarModel scalarModel) {
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty(scalarModel.getSpecificationLoader());
                final BigDecimalValueFacet facet = property.getFacet(BigDecimalValueFacet.class);
                return facet != null? facet.getScale(): null;
            }

            @Override
            public int getTypicalLength(ScalarModel scalarModel) {
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty(scalarModel.getSpecificationLoader());
                final TypicalLengthFacet facet = property.getFacet(TypicalLengthFacet.class);
                return facet != null? facet.value() : StringValueSemanticsProvider.TYPICAL_LENGTH;
            }

            @Override
            public String getFileAccept(ScalarModel scalarModel) {
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty(scalarModel.getSpecificationLoader());
                final FileAcceptFacet facet = property.getFacet(FileAcceptFacet.class);
                return facet != null? facet.value(): null;
            }


            @Override
            public void init(final ScalarModel scalarModel) {
                reset(scalarModel);
            }

            @Override
            public void reset(ScalarModel scalarModel) {
                final OneToOneAssociation property = scalarModel.propertyMemento.getProperty(scalarModel.getSpecificationLoader());

                final ObjectAdapter parentAdapter = scalarModel.getParentEntityModel().load();

                setObjectFromPropertyIfVisible(scalarModel, property, parentAdapter);
            }

            @Override
            public ObjectAdapter load(final ScalarModel scalarModel) {
                return scalarModel.loadFromSuper();
            }

            @Override
            public boolean isCollection(final ScalarModel scalarModel) {
                return false;
            }

            @Override
            public String toStringOf(final ScalarModel scalarModel) {
                return this.name() + ": " + scalarModel.getPropertyMemento().toString();
            }
        },
        PARAMETER {
            @Override
            public String getName(final ScalarModel scalarModel) {
                return scalarModel.getParameterMemento().getActionParameter(scalarModel.getSpecificationLoader()).getName();
            }

            @Override
            public ObjectSpecification getScalarTypeSpec(final ScalarModel scalarModel) {
                return scalarModel.getParameterMemento().getSpecification(scalarModel.getSpecificationLoader());
            }

            @Override
            public String getIdentifier(final ScalarModel scalarModel) {
                return "" + scalarModel.getParameterMemento().getNumber();
            }

            @Override
            public String getCssClass(final ScalarModel scalarModel) {
                final ObjectAdapterMemento adapterMemento = scalarModel.getObjectAdapterMemento();
                if (adapterMemento == null) {
                    // shouldn't happen
                    return null;
                }
                final ObjectActionParameter actionParameter = scalarModel.getParameterMemento()
                        .getActionParameter(scalarModel.getSpecificationLoader());
                final ObjectAction action = actionParameter.getAction();
                final String objectSpecId = action.getOnType().getSpecId().asString().replace(".", "-");
                final String parmId = actionParameter.getId();

                return "isis-" + objectSpecId + "-" + action.getId() + "-" + parmId;
            }

            @Override
            public String whetherDisabled(final ScalarModel scalarModel, Where where) {
                // always enabled
                return null;
            }

            @Override
            public boolean whetherHidden(final ScalarModel scalarModel, Where where) {
                // always enabled
                return false;
            }

            @Override
            public String parseAndValidate(final ScalarModel scalarModel, final String proposedPojoAsStr) {
                final ObjectActionParameter parameter = scalarModel.getParameterMemento().getActionParameter(
                        scalarModel.getSpecificationLoader());
                try {
                    final ObjectAdapter parentAdapter = scalarModel.getParentEntityModel().load();
                    final String invalidReasonIfAny = parameter.isValid(parentAdapter, proposedPojoAsStr,
                            InteractionInitiatedBy.USER
                            );
                    return invalidReasonIfAny;
                } catch (final Exception ex) {
                    return ex.getLocalizedMessage();
                }
            }

            @Override
            public String validate(final ScalarModel scalarModel, final ObjectAdapter proposedAdapter) {
                final ObjectActionParameter parameter = scalarModel.getParameterMemento().getActionParameter(
                        scalarModel.getSpecificationLoader());
                try {
                    final ObjectAdapter parentAdapter = scalarModel.getParentEntityModel().load();
                    final String invalidReasonIfAny = parameter.isValid(parentAdapter, proposedAdapter.getPojo(),
                            InteractionInitiatedBy.USER
                            );
                    return invalidReasonIfAny;
                } catch (final Exception ex) {
                    return ex.getLocalizedMessage();
                }
            }

            @Override
            public boolean isRequired(final ScalarModel scalarModel) {
                final FacetHolder facetHolder = scalarModel.getParameterMemento().getActionParameter(
                        scalarModel.getSpecificationLoader());
                return isRequired(facetHolder);
            }

            @Override
            public <T extends Facet> T getFacet(final ScalarModel scalarModel, final Class<T> facetType) {
                final FacetHolder facetHolder = scalarModel.getParameterMemento().getActionParameter(
                        scalarModel.getSpecificationLoader());
                return facetHolder.getFacet(facetType);
            }

            @Override
            public boolean hasChoices(final ScalarModel scalarModel) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(scalarModel.getSpecificationLoader());
                return actionParameter.hasChoices();
            }
            @Override
            public List<ObjectAdapter> getChoices(
                    final ScalarModel scalarModel,
                    final ObjectAdapter[] argumentsIfAvailable,
                    final AuthenticationSession authenticationSession) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(scalarModel.getSpecificationLoader());

                final ObjectAdapter parentAdapter = scalarModel.getParentEntityModel().load();

                final ObjectAdapter[] choices =
                        actionParameter.getChoices(
                                parentAdapter, argumentsIfAvailable,
                                InteractionInitiatedBy.USER);
                return choicesAsList(choices);
            }

            @Override
            public boolean hasAutoComplete(final ScalarModel scalarModel) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(scalarModel.getSpecificationLoader());
                return actionParameter.hasAutoComplete();
            }
            @Override
            public List<ObjectAdapter> getAutoComplete(
                    final ScalarModel scalarModel,
                    final String searchArg,
                    final AuthenticationSession authenticationSession) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(scalarModel.getSpecificationLoader());

                final ObjectAdapter parentAdapter =
                        scalarModel.getParentEntityModel().load(ConcurrencyChecking.NO_CHECK);
                final ObjectAdapter[] choices = actionParameter.getAutoComplete(
                        parentAdapter, searchArg,
                        InteractionInitiatedBy.USER);
                return choicesAsList(choices);
            }

            @Override
            public int getAutoCompleteOrChoicesMinLength(ScalarModel scalarModel) {
                if (scalarModel.hasAutoComplete()) {
                    final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                    final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(
                            scalarModel.getSpecificationLoader());
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
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(scalarModel.getSpecificationLoader());
                return actionParameter.getDescription();
            }

            @Override
            public Integer getLength(ScalarModel scalarModel) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(scalarModel.getSpecificationLoader());
                final BigDecimalValueFacet facet = actionParameter.getFacet(BigDecimalValueFacet.class);
                return facet != null? facet.getPrecision(): null;
            }

            @Override
            public Integer getScale(ScalarModel scalarModel) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(scalarModel.getSpecificationLoader());
                final BigDecimalValueFacet facet = actionParameter.getFacet(BigDecimalValueFacet.class);
                return facet != null? facet.getScale(): null;
            }

            @Override
            public int getTypicalLength(ScalarModel scalarModel) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(scalarModel.getSpecificationLoader());
                final TypicalLengthFacet facet = actionParameter.getFacet(TypicalLengthFacet.class);
                return facet != null? facet.value() : StringValueSemanticsProvider.TYPICAL_LENGTH;
            }


            @Override
            public String getFileAccept(ScalarModel scalarModel) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(scalarModel.getSpecificationLoader());
                final FileAcceptFacet facet = actionParameter.getFacet(FileAcceptFacet.class);
                return facet != null? facet.value(): null;
            }

            @Override
            public void init(final ScalarModel scalarModel) {
                // no-op
            }

            @Override
            public void reset(ScalarModel scalarModel) {
                final ObjectActionParameter actionParameter = scalarModel.parameterMemento.getActionParameter(
                        scalarModel.getSpecificationLoader());
                final ObjectAdapter parentAdapter =
                        scalarModel.getParentEntityModel().load(ConcurrencyChecking.NO_CHECK);
                final ObjectAdapter defaultAdapter = actionParameter.getDefault(parentAdapter);
                scalarModel.setObject(defaultAdapter);
            }

            @Override
            public ObjectAdapter load(final ScalarModel scalarModel) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento
                        .getActionParameter(scalarModel.getSpecificationLoader());
                final ObjectAdapter objectAdapter = scalarModel.loadFromSuper();

                if(objectAdapter != null) {
                    return objectAdapter;
                }
                if(actionParameter.getFeatureType() == FeatureType.ACTION_PARAMETER_SCALAR) {
                    return objectAdapter;
                }


                // hmmm... I think we should simply return null, as an indicator that there is no "pending" (see ScalarModelWithMultiPending)

                //                // return an empty collection
                //                // TODO: this should probably move down into OneToManyActionParameter impl
                //                final OneToManyActionParameter otmap = (OneToManyActionParameter) actionParameter;
                //                final CollectionSemantics collectionSemantics = otmap.getCollectionSemantics();
                //                final TypeOfFacet typeOfFacet = actionParameter.getFacet(TypeOfFacet.class);
                //                final Class<?> elementType = typeOfFacet.value();
                //                final Object emptyCollection = collectionSemantics.emptyCollectionOf(elementType);
                //                return scalarModel.getCurrentSession().getPersistenceSession().adapterFor(emptyCollection);

                return objectAdapter;

            }

            @Override
            public boolean isCollection(final ScalarModel scalarModel) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(scalarModel.getSpecificationLoader());
                return actionParameter.getFeatureType() == FeatureType.ACTION_PARAMETER_COLLECTION;
            }

            @Override
            public String toStringOf(final ScalarModel scalarModel) {
                return this.name() + ": " + scalarModel.getParameterMemento().toString();
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

        public abstract boolean whetherHidden(ScalarModel scalarModel, Where where);

        public abstract String whetherDisabled(ScalarModel scalarModel, Where where);

        public abstract String parseAndValidate(ScalarModel scalarModel, String proposedPojoAsStr);

        public abstract String validate(ScalarModel scalarModel, ObjectAdapter proposedAdapter);

        public abstract String getCssClass(ScalarModel scalarModel);

        public abstract boolean isRequired(ScalarModel scalarModel);

        public abstract <T extends Facet> T getFacet(ScalarModel scalarModel, Class<T> facetType);

        static boolean isRequired(final FacetHolder facetHolder) {
            final MandatoryFacet mandatoryFacet = facetHolder.getFacet(MandatoryFacet.class);
            final boolean required = mandatoryFacet != null && !mandatoryFacet.isInvertedSemantics();
            return required;
        }

        public abstract boolean hasChoices(ScalarModel scalarModel);
        public abstract List<ObjectAdapter> getChoices(
                final ScalarModel scalarModel,
                final ObjectAdapter[] argumentsIfAvailable,
                final AuthenticationSession authenticationSession);

        public abstract boolean hasAutoComplete(ScalarModel scalarModel);
        public abstract List<ObjectAdapter> getAutoComplete(
                ScalarModel scalarModel,
                String searchArg,
                final AuthenticationSession authenticationSession);

        public abstract int getAutoCompleteOrChoicesMinLength(ScalarModel scalarModel);

        public abstract void resetVersion(ScalarModel scalarModel);

        public abstract String getDescribedAs(ScalarModel scalarModel);

        public abstract Integer getLength(ScalarModel scalarModel);
        public abstract Integer getScale(ScalarModel scalarModel);

        public abstract int getTypicalLength(ScalarModel scalarModel);

        public abstract String getFileAccept(ScalarModel scalarModel);

        public abstract void init(ScalarModel scalarModel);
        public abstract void reset(ScalarModel scalarModel);

        public abstract ObjectAdapter load(final ScalarModel scalarModel);

        public abstract boolean isCollection(final ScalarModel scalarModel);

        public abstract String toStringOf(final ScalarModel scalarModel);
    }

    private final Kind kind;

    private final EntityModel parentEntityModel;

    @Override
    public ObjectAdapter load() {
        return kind.load(this);
    }

    private ObjectAdapter loadFromSuper() {
        return super.load();
    }


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
    public ScalarModel(final EntityModel parentEntityModel, final ActionParameterMemento apm) {
        super(EntityModel.Mode.EDIT, EntityModel.RenderingHint.REGULAR);
        this.kind = Kind.PARAMETER;
        this.parentEntityModel = parentEntityModel;
        this.parameterMemento = apm;

        init();
    }

    /**
     * Creates a model representing a property of a parent object, with the
     * {@link #getObject() value of this model} to be current value of the
     * property.
     */
    public ScalarModel(
            final EntityModel parentEntityModel, final PropertyMemento pm,
            final EntityModel.Mode mode, final EntityModel.RenderingHint renderingHint) {
        super(mode, renderingHint);
        this.kind = Kind.PROPERTY;
        this.parentEntityModel = parentEntityModel;
        this.propertyMemento = pm;

        init();
        getAndStore(parentEntityModel);
    }

    private void init() {
        kind.init(this);
    }

    @Override
    public void reset() {
        kind.reset(this);
    }

    @Override
    public boolean isWithinPrompt() {
        return FormExecutorContext.Util.isWithinPrompt(this);
    }

    @Override
    public EntityModel getParentEntityModel() {
        return parentEntityModel;
    }

    private void getAndStore(final EntityModel parentEntityModel) {
        final ObjectAdapterMemento parentAdapterMemento = parentEntityModel.getObjectAdapterMemento();
        final OneToOneAssociation property = propertyMemento.getProperty(getSpecificationLoader());
        final ObjectAdapter parentAdapter = parentAdapterMemento.getObjectAdapter(ConcurrencyChecking.CHECK,
                getPersistenceSession(), getSpecificationLoader());

        setObjectFromPropertyIfVisible(ScalarModel.this, property, parentAdapter);
    }

    private static void setObjectFromPropertyIfVisible(
            final ScalarModel scalarModel,
            final OneToOneAssociation property,
            final ObjectAdapter parentAdapter) {

        final Where where = scalarModel.getRenderingHint().asWhere();

        final Consent visibility =
                property.isVisible(parentAdapter, InteractionInitiatedBy.FRAMEWORK, where);

        final ObjectAdapter associatedAdapter;
        if (visibility.isAllowed()) {
            associatedAdapter = property.get(parentAdapter, InteractionInitiatedBy.USER);
        } else {
            associatedAdapter = null;
        }

        scalarModel.setObject(associatedAdapter);
    }


    public boolean isCollection() {
        return kind.isCollection(this);
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
    @Override
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
        final ObjectAdapter adapter = getObject();
        if (adapter == null) {
            return null;
        }
        return adapter.titleString(null);
    }

    @Override
    public void setObject(final ObjectAdapter adapter) {
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
            final List<ObjectAdapterMemento> listOfMementos = _NullSafe.stream((Iterable<?>) pojo)
                    .map(ObjectAdapterMemento.Functions.fromPojo(getPersistenceSession()))
                    .collect(Collectors.toList());
            final ObjectAdapterMemento memento =
                    ObjectAdapterMemento.createForList(listOfMementos, getTypeOfSpecification().getSpecId());
            super.setObjectMemento(memento, getPersistenceSession(), getSpecificationLoader()); // associated value
        } else {
            super.setObject(adapter); // associated value
        }
    }

    public void setObjectAsString(final String enteredText) {
        // parse text to get adapter
        final ParseableFacet parseableFacet = getTypeOfSpecification().getFacet(ParseableFacet.class);
        if (parseableFacet == null) {
            throw new RuntimeException("unable to parse string for " + getTypeOfSpecification().getFullIdentifier());
        }
        final ObjectAdapter adapter = parseableFacet.parseTextEntry(getObject(), enteredText,
                InteractionInitiatedBy.USER
                );

        setObject(adapter);
    }

    public boolean whetherHidden() {
        final Where where = getRenderingHint().asWhere();
        return kind.whetherHidden(this, where);
    }

    public String whetherDisabled() {
        final Where where = getRenderingHint().asWhere();
        return kind.whetherDisabled(this, where);
    }

    public String validate(final ObjectAdapter proposedAdapter) {
        return kind.validate(this, proposedAdapter);
    }

    /**
     * Default implementation looks up from singleton, but can be overridden for
     * testing.
     */
    @Override
    protected AuthenticationSession getAuthenticationSession() {
        return getPersistenceSession().getServicesInjector()
                .lookupServiceElseFail(AuthenticationSessionProvider.class).getAuthenticationSession();
    }

    public boolean isRequired() {
        return kind.isRequired(this);
    }

    public String getCssClass() {
        return kind.getCssClass(this);
    }

    public <T extends Facet> T getFacet(final Class<T> facetType) {
        return kind.getFacet(this, facetType);
    }

    public String getDescribedAs() {
        return kind.getDescribedAs(this);
    }

    public String getFileAccept() {
        return kind.getFileAccept(this);
    }

    public boolean hasChoices() {
        return kind.hasChoices(this);
    }

    public List<ObjectAdapter> getChoices(
            final ObjectAdapter[] argumentsIfAvailable,
            final AuthenticationSession authenticationSession) {
        return kind.getChoices(this, argumentsIfAvailable, authenticationSession);
    }

    public boolean hasAutoComplete() {
        return kind.hasAutoComplete(this);
    }

    public List<ObjectAdapter> getAutoComplete(
            final String searchTerm,
            final AuthenticationSession authenticationSession) {
        return kind.getAutoComplete(this, searchTerm, authenticationSession);
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

    /**
     * @return
     */
    public ScalarModelWithMultiPending asScalarModelWithMultiPending() {
        return new ScalarModelWithMultiPending(){

            private static final long serialVersionUID = 1L;

            @Override
            public ArrayList<ObjectAdapterMemento> getMultiPending() {
                final ObjectAdapterMemento pending = ScalarModel.this.getPending();
                return pending != null ? pending.getList() : null;
            }

            @Override
            public void setMultiPending(final ArrayList<ObjectAdapterMemento> pending) {
                final ObjectAdapterMemento adapterMemento = ObjectAdapterMemento.createForList(pending, getScalarModel().getTypeOfSpecification().getSpecId());
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

    public String getReasonInvalidIfAny() {
        final OneToOneAssociation property = getPropertyMemento().getProperty(getSpecificationLoader());

        final ObjectAdapter adapter = getParentEntityModel().load();

        final ObjectAdapter associate = getObject();

        final Consent validity = property.isAssociationValid(adapter, associate, InteractionInitiatedBy.USER);
        return validity.isAllowed() ? null : validity.getReason();
    }

    /**
     * Apply changes to the underlying adapter (possibly returning a new adapter).
     *
     * @return adapter, which may be different from the original (if a {@link ViewModelFacet#isCloneable(Object) cloneable} view model, for example.
     */
    public ObjectAdapter applyValue(ObjectAdapter adapter) {
        final OneToOneAssociation property = getPropertyMemento().getProperty(getSpecificationLoader());

        //
        // previously there was a guard here to only apply changes provided:
        //
        // property.containsDoOpFacet(NotPersistedFacet.class) == null
        //
        // however, that logic is wrong; although a property may not be directly
        // persisted so far as JDO is concerned, it may be indirectly persisted
        // as the result of business logic in the setter.
        //
        // for example, see ExampleTaggableEntity (in isisaddons-module-tags).
        //

        //
        // previously (prior to XML layouts and 'single property' edits) we also used to check if
        // the property was disabled, using:
        //
        // if(property.containsDoOpFacet(DisabledFacet.class)) {
        //    // skip, as per comments above
        //    return;
        // }
        //
        // However, this would seem to be wrong, because the presence of a DisabledFacet doesn't necessarily mean
        // that the property is disabled (its disabledReason(...) might return null).
        //
        // In any case, the only code that calls this method already does the check, so think this is safe
        // to just remove.


        final ObjectAdapter associate = getObject();
        property.set(adapter, associate, InteractionInitiatedBy.USER);

        final ViewModelFacet recreatableObjectFacet = adapter.getSpecification().getFacet(ViewModelFacet.class);
        if(recreatableObjectFacet != null) {
            final Object viewModel = adapter.getPojo();
            final boolean cloneable = recreatableObjectFacet.isCloneable(viewModel);
            if(cloneable) {
                final Object newViewModel = recreatableObjectFacet.clone(viewModel);
                adapter = getPersistenceSession().adapterFor(newViewModel);
            }
        }

        return adapter;
    }

    @Override
    protected void onDetach() {
        clearPending();
        super.onDetach();
    }



    // //////////////////////////////////////

    @Override
    public boolean isInlinePrompt() {
        return getPromptStyle() == PromptStyle.INLINE && canEnterEditMode();
    }


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


    private boolean actionWithInlineAsIfEdit;

    /**
     * Whether there is an action configured for {@link PromptStyle#INLINE_AS_IF_EDIT} for this property.
     */
    public boolean hasActionWithInlineAsIfEdit() {
        return actionWithInlineAsIfEdit;
    }

    public void setHasActionWithInlineAsIfEdit(final boolean inlineAsIfEditHint) {
        this.actionWithInlineAsIfEdit = inlineAsIfEditHint;
    }


    // //////////////////////////////////////

    /**
     * transient because only temporary hint.
     */
    private transient ObjectAdapter[] actionArgsHint;

    @Override
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

    @Override
    public String toString() {
        return kind.toStringOf(this);
    }
}
