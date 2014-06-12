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

package org.apache.isis.core.progmodel.facets.object.wizard.iface.iface;

import java.lang.reflect.Method;
import java.util.List;
import org.apache.isis.applib.Wizard;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.actions.invoke.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.object.wizard.viewmodel.WizardFacetAbstract;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

public class WizardFacetForInterface extends WizardFacetAbstract {

    public WizardFacetForInterface(final FacetHolder holder) {
        super(holder);
    }

    @Override
    public void next(Object pojo) {
        final Wizard wizard = (Wizard) pojo;
        wizard.next();
    }

    @Override
    public String disableNext(Object pojo) {
        final Wizard wizard = (Wizard) pojo;
        return wizard.disableNext();
    }

    @Override
    public void previous(Object pojo) {
        final Wizard wizard = (Wizard) pojo;
        wizard.previous();
    }

    @Override
    public String disablePrevious(Object pojo) {
        final Wizard wizard = (Wizard) pojo;
        return wizard.disablePrevious();
    }

    @Override
    public Object finish(Object pojo) {
        final Wizard wizard = (Wizard) pojo;
        return wizard.finish();
    }

    @Override
    public String disableFinish(Object pojo) {
        final Wizard wizard = (Wizard) pojo;
        return wizard.disableFinish();
    }

    @Override
    public boolean isWizardAction(ObjectAction objectAction) {
        final ActionInvocationFacet actionInvocationFacet = objectAction.getFacet(ActionInvocationFacet.class);
        final ImperativeFacet facet = ImperativeFacet.Util.getImperativeFacet(actionInvocationFacet);
        if(facet == null) {
            return false;
        }
        final List<Method> actionMethods = facet.getMethods();
        final Method[] wizardMethods = Wizard.class.getDeclaredMethods();
        for (Method actionMethod : actionMethods) {
            for (Method wizardMethod : wizardMethods) {
                if(match(actionMethod, wizardMethod)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean match(Method method1, Method method2) {
        final Class<?> declaringClass1 = method1.getDeclaringClass();
        final Class<?> declaringClass2 = method2.getDeclaringClass();

        if(!declaringClass1.isAssignableFrom(declaringClass2) && !declaringClass2.isAssignableFrom(declaringClass1)) {
            return false;
        }

        final String method1Name = method1.getName();
        final String method2Name = method2.getName();

        if(!method1Name.equals(method2Name)) {
            return false;
        }

        final Class<?>[] method1ParameterTypes = method1.getParameterTypes();
        final Class<?>[] method2ParameterTypes = method2.getParameterTypes();

        if(method1ParameterTypes.length != method2ParameterTypes.length) {
            return false;
        }

        for (int i = 0; i < method1ParameterTypes.length ; i++) {
            if(method1ParameterTypes[i] != method2ParameterTypes[i]) {
                return false;
            }
        }
        return true;
    }
}
