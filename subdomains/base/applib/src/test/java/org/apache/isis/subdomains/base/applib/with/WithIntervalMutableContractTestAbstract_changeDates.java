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
package org.apache.isis.subdomains.base.applib.with;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.isis.testing.unittestsupport.applib.core.jmocking.JUnitRuleMockery2;

public abstract class WithIntervalMutableContractTestAbstract_changeDates<T extends WithIntervalMutable<T>> {
    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);
    protected T withIntervalMutable;
    @Mock
    private WithIntervalMutable.Helper<T> mockChangeDates;

    @Before
    public void setUpWithIntervalMutable() throws Exception {
        withIntervalMutable = doCreateWithIntervalMutable(mockChangeDates);
    }

    protected abstract T doCreateWithIntervalMutable(WithIntervalMutable.Helper<T> mockChangeDates);

    @Test
    public void default0ChangeDates() {
        final LocalDate localDate = new LocalDate(2013,7,1);
        context.checking(new Expectations() {
            {
                oneOf(mockChangeDates).default0ChangeDates();
                will(returnValue(localDate));
            }
        });
        assertThat(withIntervalMutable.default0ChangeDates(), is(localDate));
    }

    @Test
    public void default1ChangeDates() {
        final LocalDate localDate = new LocalDate(2013,7,1);
        context.checking(new Expectations() {
            {
                oneOf(mockChangeDates).default1ChangeDates();
                will(returnValue(localDate));
            }
        });
        assertThat(withIntervalMutable.default1ChangeDates(), is(localDate));
    }

    @Test
    public void validateChangeDates() {
        final LocalDate startDate = new LocalDate(2013,4,1);
        final LocalDate endDate = new LocalDate(2013,7,1);
        final String reason = "xxx";
        context.checking(new Expectations() {
            {
                oneOf(mockChangeDates).validateChangeDates(startDate, endDate);
                will(returnValue(reason));
            }
        });
        assertThat(withIntervalMutable.validateChangeDates(startDate, endDate), is(reason));
    }
}
