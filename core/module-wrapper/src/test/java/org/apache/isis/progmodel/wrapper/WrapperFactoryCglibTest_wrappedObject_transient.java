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

package org.apache.isis.progmodel.wrapper;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.events.PropertyModifyEvent;
import org.apache.isis.applib.events.PropertyUsabilityEvent;
import org.apache.isis.applib.events.PropertyVisibilityEvent;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectPersistor;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.progmodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.progmodel.facets.members.disabled.staticmethod.DisabledFacetAlwaysEverywhere;
import org.apache.isis.core.progmodel.facets.properties.accessor.PropertyAccessorFacetViaAccessor;
import org.apache.isis.core.progmodel.facets.properties.modify.PropertySetterFacetViaSetterMethod;
import org.apache.isis.core.runtime.authentication.standard.SimpleSession;
import org.apache.isis.core.tck.dom.claimapp.employees.Employee;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.core.wrapper.WrapperFactoryAbstract;
import org.apache.isis.core.wrapper.WrapperFactoryCglib;
import org.apache.isis.core.wrapper.WrapperFactoryDefault;

public class WrapperFactoryCglibTest_wrappedObject_transient extends WrapperFactoryAbstractTest_wrappedObject_transient {

    @Override
    protected WrapperFactoryAbstract createWrapperFactory() {
        return new WrapperFactoryCglib();
    }
}
