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
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.fileaccept.FileAcceptFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueFacet;
import org.apache.isis.core.metamodel.facets.value.string.StringValueSemanticsProvider;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.webapp.context.memento.ObjectMemento;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.links.LinksProvider;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;

import lombok.Getter;
import lombok.Setter;
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
public class ScalarModel extends EntityModel 
implements LinksProvider, FormExecutorContext, ActionArgumentModel {

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
                return scalarModel.getSpecificationLoader().lookupBySpecIdElseLoad(type);
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
                final ManagedObject parentAdapter = scalarModel.getParentEntityModel().load();
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
                final ManagedObject parentAdapter = scalarModel.getParentEntityModel().load();
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
                    final ManagedObject parentAdapter = scalarModel.getParentEntityModel().load();
                    final ManagedObject currentValue = property.get(parentAdapter, InteractionInitiatedBy.USER);
                    final ManagedObject proposedAdapter =
                            parseableFacet.parseTextEntry(currentValue, proposedPojoAsStr, InteractionInitiatedBy.USER);
                    final Consent valid = property.isAssociationValid(parentAdapter, proposedAdapter,
                            InteractionInitiatedBy.USER);
                    return valid.isAllowed() ? null : valid.getReason();
//                } catch (final ConcurrencyException ex) {
//                    // disregard concurrency exceptions because will pick up at the IFormValidator level rather
//                    // than each individual property.
//                    return null;
                } catch (final Exception ex) {
                    return ex.getLocalizedMessage();
                }
            }

            @Override
            public String validate(final ScalarModel scalarModel, final ManagedObject proposedAdapter) {
                final ManagedObject parentAdapter = scalarModel.getParentEntityModel().load();
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
            public ManagedObject getDefault(
                    final ScalarModel scalarModel,
                    final Can<ManagedObject> pendingArgs,
                    final int paramNumUpdated) {

                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento
                        .getProperty(scalarModel.getSpecificationLoader());
                ManagedObject parentAdapter = scalarModel.getParentEntityModel().load();
                return property.getDefault(parentAdapter);
            }

            @Override
            public boolean hasChoices(final ScalarModel scalarModel) {
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty(scalarModel.getSpecificationLoader());
                return property.hasChoices();
            }

            @Override
            public Can<ManagedObject> getChoices(
                    final ScalarModel scalarModel,
                    final Can<ManagedObject> pendingArgs) {

                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento
                        .getProperty(scalarModel.getSpecificationLoader());
                ManagedObject parentAdapter = scalarModel.getParentEntityModel().load();
                final Can<ManagedObject> choices = property.getChoices(
                        parentAdapter,
                        InteractionInitiatedBy.USER);

                return choices;
            }

            @Override
            public boolean hasAutoComplete(final ScalarModel scalarModel) {
                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty(scalarModel.getSpecificationLoader());
                return property.hasAutoComplete();
            }

            @Override
            public Can<ManagedObject> getAutoComplete(
                    final ScalarModel scalarModel,
                    final Can<ManagedObject> pendingArgs, // ignored for properties
                    final String searchArg) {

                final PropertyMemento propertyMemento = scalarModel.getPropertyMemento();
                final OneToOneAssociation property = propertyMemento.getProperty(scalarModel.getSpecificationLoader());
                final ManagedObject parentAdapter =
                        scalarModel.getParentEntityModel().load();
                final Can<ManagedObject> choices =
                        property.getAutoComplete(
                                parentAdapter, 
                                searchArg,
                                InteractionInitiatedBy.USER);
                return choices;
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

                //XXX lombok issue, no val
                ManagedObject parentAdapter = scalarModel.getParentEntityModel().load();

                setObjectFromPropertyIfVisible(scalarModel, property, parentAdapter);
            }

            @Override
            public ManagedObject load(final ScalarModel scalarModel) {
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
                final ObjectMemento adapterMemento = scalarModel.getObjectAdapterMemento();
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
                    //XXX lombok issue, no val
                    ManagedObject parentAdapter = scalarModel.getParentEntityModel().load();
                    final String invalidReasonIfAny = parameter.isValid(parentAdapter, proposedPojoAsStr,
                            InteractionInitiatedBy.USER
                            );
                    return invalidReasonIfAny;
                } catch (final Exception ex) {
                    return ex.getLocalizedMessage();
                }
            }

            @Override
            public String validate(final ScalarModel scalarModel, final ManagedObject proposedAdapter) {
                final ObjectActionParameter parameter = scalarModel.getParameterMemento().getActionParameter(
                        scalarModel.getSpecificationLoader());
                try {
                    //XXX lombok issue, no val
                    ManagedObject parentAdapter = scalarModel.getParentEntityModel().load();
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
            public ManagedObject getDefault(
                    final ScalarModel scalarModel,
                    final Can<ManagedObject> pendingArgs,
                    final int paramNumUpdated) {

                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(scalarModel.getSpecificationLoader());

                //XXX lombok issue, no val
                ManagedObject parentAdapter = scalarModel.getParentEntityModel().load();
                return actionParameter.getDefault(parentAdapter, pendingArgs, paramNumUpdated);
            }

            @Override
            public boolean hasChoices(final ScalarModel scalarModel) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(scalarModel.getSpecificationLoader());
                return actionParameter.hasChoices();
            }
            @Override
            public Can<ManagedObject> getChoices(
                    final ScalarModel scalarModel,
                    final Can<ManagedObject> pendingArgs) {
                
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(scalarModel.getSpecificationLoader());

                //XXX lombok issue, no val
                ManagedObject parentAdapter = scalarModel.getParentEntityModel().load();

                final Can<ManagedObject> choices =
                        actionParameter.getChoices(
                                parentAdapter, 
                                pendingArgs,
                                InteractionInitiatedBy.USER);
                return choices;
            }

            @Override
            public boolean hasAutoComplete(final ScalarModel scalarModel) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(scalarModel.getSpecificationLoader());
                return actionParameter.hasAutoComplete();
            }
            @Override
            public Can<ManagedObject> getAutoComplete(
                    final ScalarModel scalarModel,
                    final Can<ManagedObject> pendingArgs,
                    final String searchArg) {
                
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(scalarModel.getSpecificationLoader());

                ManagedObject parentAdapter = scalarModel.getParentEntityModel().load();
                final Can<ManagedObject> choices = actionParameter.getAutoComplete(
                        parentAdapter,
                        pendingArgs,
                        searchArg,
                        InteractionInitiatedBy.USER);
                return choices;
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
                final ManagedObject parentAdapter =
                        scalarModel.getParentEntityModel().load();
                final ManagedObject defaultAdapter = actionParameter.getDefault(parentAdapter, Can.empty(), null);
                scalarModel.setObject(defaultAdapter);
            }

            @Override
            public ManagedObject load(final ScalarModel scalarModel) {
                final ActionParameterMemento parameterMemento = scalarModel.getParameterMemento();
                final ObjectActionParameter actionParameter = parameterMemento
                        .getActionParameter(scalarModel.getSpecificationLoader());
                final ManagedObject objectAdapter = scalarModel.loadFromSuper();

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

        public abstract String getName(ScalarModel scalarModel);

        public abstract ObjectSpecification getScalarTypeSpec(ScalarModel scalarModel);

        public abstract String getIdentifier(ScalarModel scalarModel);

        public abstract boolean whetherHidden(ScalarModel scalarModel, Where where);

        public abstract String whetherDisabled(ScalarModel scalarModel, Where where);

        public abstract String parseAndValidate(ScalarModel scalarModel, String proposedPojoAsStr);

        public abstract String validate(ScalarModel scalarModel, ManagedObject proposedAdapter);

        public abstract String getCssClass(ScalarModel scalarModel);

        public abstract boolean isRequired(ScalarModel scalarModel);

        public abstract <T extends Facet> T getFacet(ScalarModel scalarModel, Class<T> facetType);

        static boolean isRequired(final FacetHolder facetHolder) {
            final MandatoryFacet mandatoryFacet = facetHolder.getFacet(MandatoryFacet.class);
            final boolean required = mandatoryFacet != null && !mandatoryFacet.isInvertedSemantics();
            return required;
        }

        public abstract ManagedObject getDefault(
                ScalarModel scalarModel,
                Can<ManagedObject> pendingArgs,
                int paramNumUpdated);

        public abstract boolean hasChoices(ScalarModel scalarModel);
        public abstract Can<ManagedObject> getChoices(
                ScalarModel scalarModel,
                Can<ManagedObject> pendingArgs);

        public abstract boolean hasAutoComplete(ScalarModel scalarModel);
        public abstract Can<ManagedObject> getAutoComplete(
                ScalarModel scalarModel,
                Can<ManagedObject> pendingArgs,
                String searchArg);

        public abstract int getAutoCompleteOrChoicesMinLength(ScalarModel scalarModel);

        public abstract String getDescribedAs(ScalarModel scalarModel);

        public abstract Integer getLength(ScalarModel scalarModel);
        public abstract Integer getScale(ScalarModel scalarModel);

        public abstract int getTypicalLength(ScalarModel scalarModel);

        public abstract String getFileAccept(ScalarModel scalarModel);

        public abstract void init(ScalarModel scalarModel);
        public abstract void reset(ScalarModel scalarModel);

        public abstract ManagedObject load(final ScalarModel scalarModel);

        public abstract boolean isCollection(final ScalarModel scalarModel);

        public abstract String toStringOf(final ScalarModel scalarModel);
    }

    private final Kind kind;

    private final EntityModel parentEntityModel;

    @Override
    public ManagedObject load() {
        return kind.load(this);
    }

    private ManagedObject loadFromSuper() {
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
    public ScalarModel(EntityModel parentEntityModel, ActionParameterMemento apm) {
        
        super(parentEntityModel.getCommonContext(),
                EntityModel.Mode.EDIT, 
                EntityModel.RenderingHint.REGULAR);
        
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
            EntityModel parentEntityModel, 
            PropertyMemento pm,
            EntityModel.Mode mode, 
            EntityModel.RenderingHint renderingHint) {
        
        super(parentEntityModel.getCommonContext(), mode, renderingHint);
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
    public EntityModel getParentEntityModel() {
        return parentEntityModel;
    }

    private void getAndStore(final EntityModel parentEntityModel) {
        final ObjectMemento parentAdapterMemento = parentEntityModel.getObjectAdapterMemento();
        final OneToOneAssociation property = propertyMemento.getProperty(getSpecificationLoader());
        final ManagedObject parentAdapter = super.getCommonContext().reconstructObject(parentAdapterMemento); 
        setObjectFromPropertyIfVisible(ScalarModel.this, property, parentAdapter);
    }

    private static void setObjectFromPropertyIfVisible(
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
     * know the {@link ObjectSpecification of} the {@link ManagedObject adapter}
     * without there necessarily being any adapter being
     * {@link #setObject(ManagedObject) set}.
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
        return kind.whetherHidden(this, where);
    }

    public String whetherDisabled() {
        final Where where = getRenderingHint().asWhere();
        return kind.whetherDisabled(this, where);
    }

    public String validate(final ManagedObject proposedAdapter) {
        return kind.validate(this, proposedAdapter);
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

    public Can<ManagedObject> getChoices(
            final Can<ManagedObject> pendingArgs) {
        
        return kind.getChoices(this, pendingArgs);
    }

    public boolean hasAutoComplete() {
        return kind.hasAutoComplete(this);
    }

    public Can<ManagedObject> getAutoComplete(
            final Can<ManagedObject> pendingArgs,
            final String searchTerm) {
        
        return kind.getAutoComplete(this, pendingArgs, searchTerm);
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

    public String getReasonInvalidIfAny() {
        //XXX lombok issue, no val 
        OneToOneAssociation property = getPropertyMemento().getProperty(getSpecificationLoader());
        ManagedObject adapter = getParentEntityModel().load();
        ManagedObject associate = getObject();
        Consent validity = property.isAssociationValid(adapter, associate, InteractionInitiatedBy.USER);
        return validity.isAllowed() ? null : validity.getReason();
    }

    /**
     * Apply changes to the underlying adapter (possibly returning a new adapter).
     *
     * @return adapter, which may be different from the original (if a {@link ViewModelFacet#isCloneable(Object) cloneable} view model, for example.
     */
    public ManagedObject applyValue(ManagedObject adapter) {
        //XXX lombok issue, no val
        OneToOneAssociation property = getPropertyMemento().getProperty(getSpecificationLoader());

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

        //XXX lombok issue, no val
        ManagedObject associate = getObject();
        property.set(adapter, associate, InteractionInitiatedBy.USER);

        final ViewModelFacet recreatableObjectFacet = adapter.getSpecification().getFacet(ViewModelFacet.class);
        if(recreatableObjectFacet != null) {
            final Object viewModel = adapter.getPojo();
            final boolean cloneable = recreatableObjectFacet.isCloneable(viewModel);
            if(cloneable) {
                //XXX lombok issue, no val
                Object newViewModelPojo = recreatableObjectFacet.clone(viewModel);
                adapter = super.getPojoToAdapter().apply(newViewModelPojo);
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

    private transient AssociatedActions associatedActions;

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

    public AssociatedActions associatedActionsIfProperty() {
        if (associatedActions == null) {
            associatedActions = new AssociatedActions(calcAssociatedActionsIfProperty());
        }
        return associatedActions;
    }

    private List<ObjectAction> calcAssociatedActionsIfProperty() {

        if (getKind() != Kind.PROPERTY) {
            return Collections.emptyList();
        }

        final EntityModel parentEntityModel1 = this.getParentEntityModel();
        final ManagedObject parentAdapter = parentEntityModel1.load();

        final OneToOneAssociation oneToOneAssociation =
                this.getPropertyMemento().getProperty(this.getSpecificationLoader());

        return ObjectAction.Util.findForAssociation(parentAdapter, oneToOneAssociation);
    }


    /**
     * Whether this model should be surfaced in the UI using a widget rendered such that it is either already in
     * edit mode (eg for a parameter), or can be switched into edit mode, eg for an editable property or an
     * associated action of a property with 'inline_as_if_edit'
     *
     * @return <tt>true</tt> if the widget for this model must be editable.
     */
    public boolean mustBeEditable() {
        return getMode() == Mode.EDIT ||
                associatedActionsIfProperty().hasAssociatedActionWithInlineAsIfEdit() ||
                getKind() == Kind.PARAMETER;
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
        return (getPromptStyle().isInline() && canEnterEditMode()) ||
                associatedActionsIfProperty().hasAssociatedActionWithInlineAsIfEdit();
    }

    /**
     * The initial call of choicesXxx() for any given scalar argument needs the current values
     * of all args (possibly as initialized through a defaultNXxx().
     * @implNote transient because only temporary hint.
     */
    @Getter @Setter
    private transient Can<ManagedObject> actionArgsHint;


    @Override
    public String toString() {
        return kind.toStringOf(this);
    }
}
