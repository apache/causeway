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

package org.apache.isis.core.integtestsupport.legacy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import org.apache.isis.applib.events.InteractionEvent;
import org.apache.isis.applib.events.PropertyAccessEvent;
import org.apache.isis.applib.services.wrapper.listeners.InteractionAdapter;
import org.apache.isis.applib.services.wrapper.listeners.InteractionListener;
import org.apache.isis.core.integtestsupport.legacy.sample.domain.Customer;

public class InteractionListenerTest extends AbstractTest {

    @Test
    public void shouldBeAbleToAddListener() {
        final Customer proxiedCustRP = getWrapperFactory().wrap(custJsDO);
        final InteractionEvent[] events = { null };
        final InteractionListener l = new InteractionAdapter() {
            @Override
            public void propertyAccessed(final PropertyAccessEvent ev) {
                events[0] = ev;
            }
        };
        getWrapperFactory().addInteractionListener(l);

        proxiedCustRP.getFirstName();
        assertThat(events[0], notNullValue());
        final PropertyAccessEvent ev = (PropertyAccessEvent) events[0];
        assertThat(ev.getMemberNaturalName(), is("First Name"));
    }

}
