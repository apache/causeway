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
    
    private final ActionParameterMemento parameterMemento;
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
    
    private transient ObjectActionParameter actionParameter;
    
    @Override
    public ObjectActionParameter getActionParameter() {
        if(actionParameter==null) {
            actionParameter = parameterMemento.getActionParameter(getSpecificationLoader()); 
        }
        return actionParameter;  
    }

    @Override
    public String getName() {
        return getActionParameter().getName();
    }

    @Override
    public ObjectSpecification getScalarTypeSpec() {
        return parameterMemento.getSpecification(getSpecificationLoader());
    }

    @Override
    public String getIdentifier() {
        return "" + getNumber();
    }

    @Override
    public String getCssClass() {
        final ObjectMemento adapterMemento = getObjectAdapterMemento();
        if (adapterMemento == null) {
            // shouldn't happen
            return null;
        }
        final ObjectActionParameter actionParameter = getActionParameter();
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
        final ObjectActionParameter parameter = getActionParameter();
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
        final ObjectActionParameter parameter = getActionParameter();
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
        return isRequired(getActionParameter());
    }

    @Override
    public <T extends Facet> T getFacet(final Class<T> facetType) {
        return getActionParameter().getFacet(facetType);
    }

    @Override
    public ManagedObject getDefault(
            @NonNull final PendingParameterModel pendingArgs) {
        
        return getActionParameter().getDefault(pendingArgs);
    }

    @Override
    public boolean hasChoices() {
        return getActionParameter().hasChoices();
    }
    @Override
    public Can<ManagedObject> getChoices(
            @NonNull final PendingParameterModel pendingArgs) {
        return getActionParameter().getChoices(pendingArgs, InteractionInitiatedBy.USER);
    }

    @Override
    public boolean hasAutoComplete() {
        return getActionParameter().hasAutoComplete();
    }
    @Override
    public Can<ManagedObject> getAutoComplete(
            @NonNull final PendingParameterModel pendingArgs,
            final String searchArg) {
        
        return getActionParameter().getAutoComplete(pendingArgs, searchArg, InteractionInitiatedBy.USER);
    }
    
    @Override
    public int getAutoCompleteOrChoicesMinLength() {
        if (hasAutoComplete()) {
            return getActionParameter().getAutoCompleteMinLength();
        } else {
            return 0;
        }
    }

    @Override
    public String getDescribedAs() {
        return getActionParameter().getDescription();
    }

    @Override
    public Integer getLength() {
        final BigDecimalValueFacet facet = getActionParameter().getFacet(BigDecimalValueFacet.class);
        return facet != null? facet.getPrecision(): null;
    }

    @Override
    public Integer getScale() {
        final BigDecimalValueFacet facet = getActionParameter().getFacet(BigDecimalValueFacet.class);
        return facet != null? facet.getScale(): null;
    }

    @Override
    public int getTypicalLength() {
        final TypicalLengthFacet facet = getActionParameter().getFacet(TypicalLengthFacet.class);
        return facet != null? facet.value() : StringValueSemanticsProvider.TYPICAL_LENGTH;
    }


    @Override
    public String getFileAccept() {
        final FileAcceptFacet facet = getActionParameter().getFacet(FileAcceptFacet.class);
        return facet != null? facet.value(): null;
    }

    @Override
    public ManagedObject load() {
        final ManagedObject objectAdapter = loadFromSuper();

        if(objectAdapter != null) {
            return objectAdapter;
        }
        if(getActionParameter().getFeatureType() == FeatureType.ACTION_PARAMETER_SCALAR) {
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
        return getActionParameter().getFeatureType() == FeatureType.ACTION_PARAMETER_COLLECTION;
    }

    @Override
    public String toStringOf() {
        return getName() + ": " + parameterMemento.toString();
    }
    
    @Override
    protected List<ObjectAction> calcAssociatedActions() {
        return Collections.emptyList();
    }
    
    public PendingParameterModelHead getPendingParamHead() {
        val actionParameter = getActionParameter();
        val actionOwner = getParentUiModel().load();
        return actionParameter.getAction().newPendingParameterModelHead(actionOwner);
    }

    @Override
    public ManagedObject getValue() {
        return getObject();
    }

    @Override
    public void setValue(ManagedObject paramValue) {
        super.setObject(paramValue);
    }

    
}
