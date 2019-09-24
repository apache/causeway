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

package org.apache.isis.applib.events;

import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class InteractionEventTest {

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    private InteractionEvent interactionEvent;

    private Object source;
    private Identifier identifier;

    private Class<? extends InteractionEventTest> advisorClass;

    @Before
    public void setUp() {
        source = new Object();
        identifier = Identifier.actionIdentifier("CustomerOrder", "cancelOrder", new Class[] { String.class, boolean.class });
        advisorClass = this.getClass();
    }

    @Test
    public void getIdentifier() {
        interactionEvent = new InteractionEvent(source, identifier) {

            
        };
        assertThat(interactionEvent.getIdentifier(), is(identifier));
    }

    @Test
    public void getSource() {
        interactionEvent = new InteractionEvent(source, identifier) {

            
        };
        assertThat(interactionEvent.getSource(), is(source));
    }

    @Test
    public void getClassName() {
        interactionEvent = new InteractionEvent(source, identifier) {

            
        };
        assertThat(interactionEvent.getClassName(), equalTo("CustomerOrder"));
    }

    @Test
    public void getClassNaturalName() {
        interactionEvent = new InteractionEvent(source, identifier) {

            
        };
        assertThat(interactionEvent.getClassNaturalName(), equalTo("Customer Order"));
    }

    @Test
    public void getMember() {
        interactionEvent = new InteractionEvent(source, identifier) {

            
        };
        assertThat(interactionEvent.getMemberName(), equalTo("cancelOrder"));
    }

    @Test
    public void getMemberNaturalName() {
        interactionEvent = new InteractionEvent(source, identifier) {

            
        };
        assertThat(interactionEvent.getMemberNaturalName(), equalTo("Cancel Order"));
    }

    @Test
    public void shouldInitiallyNotVeto() {
        interactionEvent = new InteractionEvent(source, identifier) {

            
        };
        assertThat(interactionEvent.isVeto(), is(false));
    }

    @Test
    public void afterAdvisedShouldVeto() {
        interactionEvent = new InteractionEvent(source, identifier) {

            
        };
        interactionEvent.advised("some reason", this.getClass());
        assertThat(interactionEvent.isVeto(), is(true));
    }

    @Test
    public void afterAdvisedShouldReturnReason() {
        interactionEvent = new InteractionEvent(source, identifier) {

            
        };
        interactionEvent.advised("some reason", this.getClass());
        assertThat(interactionEvent.isVeto(), is(true));
    }

    @Test
    public void afterAdvisedShouldReturnAdvisorClass() {
        interactionEvent = new InteractionEvent(source, identifier) {

            
        };
        interactionEvent.advised("some reason", advisorClass);
        assertEquals(interactionEvent.getAdvisorClass(), advisorClass);
    }

}
