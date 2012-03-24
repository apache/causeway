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

package org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.tester.WicketTester;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectActionSet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.ui.fixtures.ActionFixtures;
import org.apache.isis.viewer.wicket.ui.fixtures.AdapterFixtures;
import org.apache.isis.viewer.wicket.ui.fixtures.Customers;
import org.apache.isis.viewer.wicket.ui.fixtures.SpecFixtures;
import org.apache.isis.viewer.wicket.ui.fixtures.SystemFixtures;

@RunWith(JMock.class)
public class CssMenuUtilsTest {

    private final Mockery context = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private ObjectAdapter mockAdapter;
    private ObjectAdapterMemento mockAdapterMemento;
    private Oid mockOid;

    private ObjectAction setAction;

    private ObjectAction mockUserAction;
    private ObjectSpecification mockUserActionOnTypeSpec;

    private CssMenuLinkFactory mockLinkBuilder;
    private RuntimeContext mockRuntimeContext;

    @SuppressWarnings("unused")
    private WicketTester wicketTester;

    private Link<String> fakeLink;

    @Before
    public void setUp() throws Exception {
        wicketTester = new WicketTester();

        mockAdapter = context.mock(ObjectAdapter.class);
        mockAdapterMemento = context.mock(ObjectAdapterMemento.class);
        mockOid = context.mock(Oid.class);

        mockUserAction = context.mock(ObjectAction.class, "userAction");
        mockUserActionOnTypeSpec = context.mock(ObjectSpecification.class);

        fakeLink = new Link<String>(CssMenuItem.ID_MENU_LINK) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
            }
        };

        mockLinkBuilder = context.mock(CssMenuLinkFactory.class);
        mockRuntimeContext = context.mock(RuntimeContext.class);
    }

    @After
    public void tearDown() throws Exception {
        wicketTester = null;
    }

    @Ignore("broken...")
    @Test
    public void whenUserActionThenSingleMenuItem() throws Exception {

        new ActionFixtures(context).isVisible(mockUserAction, true);
        new ActionFixtures(context).isUsable(mockUserAction, true);

        new ActionFixtures(context).getName(mockUserAction, "findCustomers");

        new ActionFixtures(context).getParameterCount(mockUserAction, 0);
        new AdapterFixtures(context).getOid(mockAdapter, mockOid);
        new ActionFixtures(context).getOnType(mockUserAction, mockUserActionOnTypeSpec);
        new SpecFixtures(context).getFullName(mockUserActionOnTypeSpec, Customers.class.getName());
        new ActionFixtures(context).getType(mockUserAction, ActionType.USER);
        new ActionFixtures(context).getIdentifier(context, mockUserAction, Identifier.actionIdentifier(Customers.class, "findCustomers", new Class[0]));

        new SystemFixtures(context).newLink(mockLinkBuilder, "linkId", mockAdapterMemento, mockUserAction, fakeLink);

        final CssMenuItem parentMenuItem = CssMenuItem.newMenuItem("parent").build();
        parentMenuItem.newSubMenuItem(mockAdapterMemento, mockUserAction, mockLinkBuilder).build();

        assertThat(parentMenuItem.hasSubMenuItems(), is(true));
        assertThat(parentMenuItem.getSubMenuItems().size(), is(1));
    }

    @Ignore("broken...")
    @Test
    public void whenSetActionWithNoChildrenThenNoMenuItem() throws Exception {

        setAction = new ObjectActionSet("customers", "Customers", Lists.<ObjectAction> newArrayList());

        new SystemFixtures(context).newLink(mockLinkBuilder, "linkId", mockAdapterMemento, setAction, fakeLink);

        new AdapterFixtures(context).getOid(mockAdapter, mockOid);

        final CssMenuItem parentMenuItem = CssMenuItem.newMenuItem("parent").build();
        parentMenuItem.newSubMenuItem(mockAdapterMemento, setAction, mockLinkBuilder);

        assertThat(parentMenuItem.hasSubMenuItems(), is(false));
    }

    @Ignore("broken...")
    @Test
    public void whenSetActionWithOneChildThenMenuItemForSetActionAndMenuItemUnderneath() throws Exception {

        setAction = new ObjectActionSet("customers", "Customers", Collections.singletonList(mockUserAction));

        new AdapterFixtures(context).getOid(mockAdapter, mockOid);

        new SystemFixtures(context).newLink(mockLinkBuilder, "linkId", mockAdapterMemento, setAction, fakeLink);

        final CssMenuItem parentMenuItem = CssMenuItem.newMenuItem("parent").build();
        parentMenuItem.newSubMenuItem(mockAdapterMemento, setAction, mockLinkBuilder).build();

        assertThat(parentMenuItem.hasSubMenuItems(), is(true));
        final List<CssMenuItem> subMenuItems = parentMenuItem.getSubMenuItems();
        assertThat(subMenuItems.size(), is(1));
        final CssMenuItem childMenuItem = subMenuItems.get(0);
        assertThat(childMenuItem, is(not(nullValue(CssMenuItem.class))));
    }

}
