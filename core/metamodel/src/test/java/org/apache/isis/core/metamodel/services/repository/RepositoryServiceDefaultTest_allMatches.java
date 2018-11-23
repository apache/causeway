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

package org.apache.isis.core.metamodel.services.repository;

import java.util.List;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

public class RepositoryServiceDefaultTest_allMatches {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private RepositoryServiceInternalDefault repositoryService;

    @Mock
    private TransactionService mockTransactionService;

    @Before
    public void setUp() throws Exception {
        _Config.clear();
        repositoryService = new RepositoryServiceInternalDefault() {
            @Override <T> List<T> submitQuery(final Query<T> query) {
                return null;
            }
        };
        repositoryService.transactionService = mockTransactionService;
    }

    @Test
    public void whenAutoflush() throws Exception {
        // given
        repositoryService.init();
        // expect
        context.checking(new Expectations() {{
            oneOf(mockTransactionService).flushTransaction();
        }});
        // when
        repositoryService.allMatches(null);
    }

    @Test
    public void whenDisableAutoflush() throws Exception {
        // given
        _Config.put("isis.services.container.disableAutoFlush", true);
        repositoryService.init();
        // expect
        context.checking(new Expectations() {{
            never(mockTransactionService).flushTransaction();
        }});
        // when
        repositoryService.allMatches(null);
    }

    @Test
    public void whenDisableAutoflushSetToFalse() throws Exception {
        // given
        _Config.put("isis.services.container.disableAutoFlush", false);
        repositoryService.init();
        // expect
        context.checking(new Expectations() {{
            oneOf(mockTransactionService).flushTransaction();
        }});
        // when
        repositoryService.allMatches(null);
    }

}
