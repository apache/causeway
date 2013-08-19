/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.commons.lang.CastUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResultSet;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.object.bounded.BoundedFacetUtils;
import org.apache.isis.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMemberContext;

public class ObjectActionContributee extends ObjectActionImpl implements ContributeeMember {

    private final ObjectAdapter serviceAdapter;
    private final ObjectActionImpl serviceAction;
    private int contributeeParam;
    private final ObjectSpecification contributeeType;

    /**
     * Lazily initialized by {@link #getParameters()} (so don't use directly!)
     */
    private List<ObjectActionParameterContributee> parameters;

    /**
     * @param contributeeParam - the parameter number which corresponds to the contributee, and so should be suppressed.
     */
    public ObjectActionContributee(
            final ObjectAdapter serviceAdapter,
            final ObjectActionImpl serviceAction,
            final int contributeeParam,
            final ObjectSpecification contributeeType,
            final ObjectMemberContext objectMemberContext) {
        super(serviceAction.getFacetedMethod(), objectMemberContext, null);
        
        this.serviceAdapter = serviceAdapter;
        this.serviceAction = serviceAction;
        this.contributeeType = contributeeType;
        this.contributeeParam = contributeeParam;
    }

    @Override
    public ObjectSpecification getOnType() {
        return contributeeType;
    }

    public int getParameterCount() {
        return serviceAction.getParameterCount() - 1;
    }

    public synchronized List<ObjectActionParameter> getParameters() {
        
        if (this.parameters == null) {
            final List<ObjectActionParameter> serviceParameters = serviceAction.getParameters();
            
            final List<ObjectActionParameterContributee> contributeeParameters = Lists.newArrayList();
            
            int contributeeParamNum = 0;
            for (int serviceParamNum = 0; serviceParamNum < serviceParameters.size(); serviceParamNum++ ) {
                if(serviceParamNum == contributeeParam) {
                    // skip so is omitted from the Contributed action
                    continue;
                }
                
                final ObjectActionParameterAbstract serviceParameter = 
                        (ObjectActionParameterAbstract) serviceParameters.get(serviceParamNum);
                final ObjectActionParameterContributee contributedParam;
                if(serviceParameter instanceof ObjectActionParameterParseable) {
                    contributedParam = new ObjectActionParameterParseableContributee(
                            serviceAdapter, serviceAction, serviceParameter, serviceParamNum, 
                            contributeeParamNum, this);
                } else if(serviceParameter instanceof OneToOneActionParameterImpl) {
                    contributedParam = new OneToOneActionParameterContributee(
                            serviceAdapter, serviceAction, serviceParameter, serviceParamNum, 
                            contributeeParamNum, this);
                } else {
                    throw new RuntimeException("Unknown implementation of ObjectActionParameter; " + serviceParameter.getClass().getName());
                }
                contributeeParameters.add(contributedParam);
                
                contributeeParamNum++;
            }
            this.parameters = contributeeParameters;
        }
        return CastUtils.listOf(parameters, ObjectActionParameter.class);
    }

    
    @Override
    public Consent isVisible(final AuthenticationSession session, final ObjectAdapter contributee, Where where) {
        return serviceAction.isVisible(session, serviceAdapter, where);
    }

    @Override
    public Consent isUsable(final AuthenticationSession session, final ObjectAdapter contributee, Where where) {
        return serviceAction.isUsable(session, serviceAdapter, where);
    }

    @Override
    public ObjectAdapter[] getDefaults(final ObjectAdapter target) {
        final ObjectAdapter[] contributorDefaults = serviceAction.getDefaults(serviceAdapter);
        return removeElementFromArray(contributorDefaults, contributeeParam, new ObjectAdapter[]{});
    }

    @Override
    public ObjectAdapter[][] getChoices(final ObjectAdapter target) {
        final ObjectAdapter[][] serviceChoices = serviceAction.getChoices(serviceAdapter);
        return removeElementFromArray(serviceChoices, contributeeParam, new ObjectAdapter[][]{});
    }
        
    public Consent isProposedArgumentSetValid(final ObjectAdapter contributee, final ObjectAdapter[] proposedArguments) {
        ObjectAdapter[] serviceArguments = argsPlusContributee(contributee, proposedArguments);
        return serviceAction.isProposedArgumentSetValid(serviceAdapter, serviceArguments);
    }

    @Override
    public ObjectAdapter execute(final ObjectAdapter contributee, final ObjectAdapter[] arguments) {
        return serviceAction.execute(serviceAdapter, argsPlusContributee(contributee, arguments));
    }

    private ObjectAdapter[] argsPlusContributee(final ObjectAdapter contributee, final ObjectAdapter[] arguments) {
        return addElementToArray(arguments, contributeeParam, contributee, new ObjectAdapter[]{});
    }

    // //////////////////////////////////////

    private static <T> T[] addElementToArray(T[] array, final int n, final T element, final T[] type) {
        List<T> list = Lists.newArrayList(Arrays.asList(array));
        list.add(n, element);
        return list.toArray(type);
    }

    private static <T> T[] removeElementFromArray(T[] array, int n, T[] t) {
        List<T> list = Lists.newArrayList(Arrays.asList(array));
        list.remove(n);
        return list.toArray(t);
    }


}
