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
package domainapp.dom.impl;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.hamcrest.CoreMatchers.containsString;

public class HelloWorldObjectTest_delete {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    TitleService mockTitleService;

    @Mock
    MessageService mockMessageService;

    @Mock
    RepositoryService mockRepositoryService;

    @Test
    public void happy_case() throws Exception {

        // given
        final HelloWorldObject object = HelloWorldObject.create("Foo");
        object.titleService = mockTitleService;
        object.messageService = mockMessageService;
        object.repositoryService = mockRepositoryService;

        // expecting
        context.checking(new Expectations() {{
            allowing(mockTitleService).titleOf(object); will(returnValue("Foo"));

            oneOf(mockMessageService).informUser(with(containsString("'Foo' deleted")));
            oneOf(mockRepositoryService).removeAndFlush(object);
        }});

        // when
        object.delete();
    }


}