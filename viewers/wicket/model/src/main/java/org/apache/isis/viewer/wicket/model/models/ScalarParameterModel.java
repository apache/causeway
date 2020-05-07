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

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.objectvalue.fileaccept.FileAcceptFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.value.bigdecimal.BigDecimalValueFacet;
import org.apache.isis.core.metamodel.facets.value.string.StringValueSemanticsProvider;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.specloader.specimpl.PendingParameterModel;
import org.apache.isis.core.metamodel.specloader.specimpl.PendingParameterModelHead;
import org.apache.isis.core.webapp.context.memento.ObjectMemento;
import org.apache.isis.viewer.wicket.model.mementos.ActionParameterMemento;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

public class ScalarParameterModel extends ScalarModel
implements ActionArgumentModel {

    private static final long serialVersionUID = 1L;
    
    @Getter private final ActionParameterMemento parameterMemento;
    /**
     * The initial call of choicesXxx() for any given scalar argument needs the current values
     * of all args (possibly as initialized through a defaultNXxx().
     * @implNote transient because only temporary hint.
     */
    @Getter @Setter
    private transient PendingParameterModel actionArgsHint;

    /**
     * Creates a model representing an action parameter of an action of a parent
     * object, with the {@link #getObject() value of this model} to be default
     * value (if any) of that action parameter.
     */
    public ScalarParameterModel(EntityModel parentEntityModel, ActionParameterMemento apm) {
        super(parentEntityModel, apm);
        this.parameterMemento = apm;
    }
    

    @Override
    public String getName() {
        return getParameterMemento().getActionParameter(getSpecificationLoader()).getName();
    }

    @Override
    public ObjectSpecification getScalarTypeSpec() {
        return getParameterMemento().getSpecification(getSpecificationLoader());
    }

    @Override
    public String getIdentifier() {
        return "" + getParameterMemento().getNumber();
    }

    @Override
    public String getCssClass() {
        final ObjectMemento adapterMemento = getObjectAdapterMemento();
        if (adapterMemento == null) {
            // shouldn't happen
            return null;
        }
        final ObjectActionParameter actionParameter = getParameterMemento()
                .getActionParameter(getSpecificationLoader());
        final ObjectAction action = actionParameter.getAction();
        final String objectSpecId = action.getOnType().getSpecId().asString().replace(".", "-");
        final String parmId = actionParameter.getId();

        return "isis-" + objectSpecId + "-" + action.getId() + "-" + parmId;
    }

    @Override
    public String whetherDisabled(Where where) {
        // always enabled
        return null;
    }

    @Override
    public boolean whetherHidden(Where where) {
        // always enabled
        return false;
    }

    @Override
    public String parseAndValidate(final String proposedPojoAsStr) {
        final ObjectActionParameter parameter = getParameterMemento().getActionParameter(
                getSpecificationLoader());
        try {
            ManagedObject parentAdapter = getParentUiModel().load();
            final String invalidReasonIfAny = parameter.isValid(parentAdapter, proposedPojoAsStr,
                    InteractionInitiatedBy.USER
                    );
            return invalidReasonIfAny;
        } catch (final Exception ex) {
            return ex.getLocalizedMessage();
        }
    }

    @Override
    public String validate(final ManagedObject proposedAdapter) {
        final ObjectActionParameter parameter = getParameterMemento().getActionParameter(
                getSpecificationLoader());
        try {
            ManagedObject parentAdapter = getParentUiModel().load();
            final String invalidReasonIfAny = parameter.isValid(parentAdapter, proposedAdapter.getPojo(),
                    InteractionInitiatedBy.USER
                    );
            return invalidReasonIfAny;
        } catch (final Exception ex) {
            return ex.getLocalizedMessage();
        }
    }

    @Override
    public boolean isRequired() {
        final FacetHolder facetHolder = getParameterMemento().getActionParameter(
                getSpecificationLoader());
        return isRequired(facetHolder);
    }

    @Override
    public <T extends Facet> T getFacet(final Class<T> facetType) {
        final FacetHolder facetHolder = getParameterMemento().getActionParameter(
                getSpecificationLoader());
        return facetHolder.getFacet(facetType);
    }

    @Override
    public ManagedObject getDefault(
            @NonNull final PendingParameterModel pendingArgs) {
        
        return getParameter().getDefault(pendingArgs);
    }

    @Override
    public boolean hasChoices() {
        final ActionParameterMemento parameterMemento = getParameterMemento();
        final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(getSpecificationLoader());
        return actionParameter.hasChoices();
    }
    @Override
    public Can<ManagedObject> getChoices(
            @NonNull final PendingParameterModel pendingArgs) {
        return getParameter().getChoices(pendingArgs, InteractionInitiatedBy.USER);
    }

    @Override
    public boolean hasAutoComplete() {
        final ActionParameterMemento parameterMemento = getParameterMemento();
        final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(getSpecificationLoader());
        return actionParameter.hasAutoComplete();
    }
    @Override
    public Can<ManagedObject> getAutoComplete(
            @NonNull final PendingParameterModel pendingArgs,
            final String searchArg) {
        
        return getParameter().getAutoComplete(pendingArgs, searchArg, InteractionInitiatedBy.USER);
    }
    
    @Override
    public int getAutoCompleteOrChoicesMinLength() {
        if (hasAutoComplete()) {
            final ActionParameterMemento parameterMemento = getParameterMemento();
            final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(
                    getSpecificationLoader());
            return actionParameter.getAutoCompleteMinLength();
        } else {
            return 0;
        }
    }

    @Override
    public String getDescribedAs() {
        final ActionParameterMemento parameterMemento = getParameterMemento();
        final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(getSpecificationLoader());
        return actionParameter.getDescription();
    }

    @Override
    public Integer getLength() {
        final ActionParameterMemento parameterMemento = getParameterMemento();
        final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(getSpecificationLoader());
        final BigDecimalValueFacet facet = actionParameter.getFacet(BigDecimalValueFacet.class);
        return facet != null? facet.getPrecision(): null;
    }

    @Override
    public Integer getScale() {
        final ActionParameterMemento parameterMemento = getParameterMemento();
        final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(getSpecificationLoader());
        final BigDecimalValueFacet facet = actionParameter.getFacet(BigDecimalValueFacet.class);
        return facet != null? facet.getScale(): null;
    }

    @Override
    public int getTypicalLength() {
        final ActionParameterMemento parameterMemento = getParameterMemento();
        final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(getSpecificationLoader());
        final TypicalLengthFacet facet = actionParameter.getFacet(TypicalLengthFacet.class);
        return facet != null? facet.value() : StringValueSemanticsProvider.TYPICAL_LENGTH;
    }


    @Override
    public String getFileAccept() {
        final ActionParameterMemento parameterMemento = getParameterMemento();
        final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(getSpecificationLoader());
        final FileAcceptFacet facet = actionParameter.getFacet(FileAcceptFacet.class);
        return facet != null? facet.value(): null;
    }

    @Override
    public ManagedObject load() {
        final ActionParameterMemento parameterMemento = getParameterMemento();
        final ObjectActionParameter actionParameter = parameterMemento
                .getActionParameter(getSpecificationLoader());
        final ManagedObject objectAdapter = loadFromSuper();

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
    public boolean isCollection() {
        final ActionParameterMemento parameterMemento = getParameterMemento();
        final ObjectActionParameter actionParameter = parameterMemento.getActionParameter(getSpecificationLoader());
        return actionParameter.getFeatureType() == FeatureType.ACTION_PARAMETER_COLLECTION;
    }

    @Override
    public String toStringOf() {
        return getName() + ": " + getParameterMemento().toString();
    }
    
    @Override
    protected List<ObjectAction> calcAssociatedActions() {
        return Collections.emptyList();
    }
    
    public ObjectActionParameter getParameter() {
        val parameterMemento = getParameterMemento();
        val actionParameter = parameterMemento.getActionParameter(getSpecificationLoader());
        return actionParameter;
    }
    
    public PendingParameterModelHead getPendingParamHead() {
        val actionParameter = getParameter();
        val actionOwner = getParentUiModel().load();
        return actionParameter.getAction().newPendingParameterModelHead(actionOwner);
    }


    
}
