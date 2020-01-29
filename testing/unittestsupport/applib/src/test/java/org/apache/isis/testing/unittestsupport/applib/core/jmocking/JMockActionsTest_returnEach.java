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

package org.apache.isis.testing.unittestsupport.applib.core.jmocking;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.testing.unittestsupport.applib.core.jmocking.JUnitRuleMockery2.Mode;

public class JMockActionsTest_returnEach {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private CollaboratorForReturnEach collaborator;

    @Test
    public void poke() {
        context.checking(new Expectations() {
            {
                exactly(3).of(collaborator).readValue();
                will(JMockActions.returnEach(1,2,3));
            }
        });
        assertThat(new ClassUnderTestForReturnEach(collaborator).prependAndRead("foo"), is("foo 1"));
        assertThat(new ClassUnderTestForReturnEach(collaborator).prependAndRead("bar"), is("bar 2"));
        assertThat(new ClassUnderTestForReturnEach(collaborator).prependAndRead("baz"), is("baz 3"));
    }

    public interface CollaboratorForReturnEach {
        public int readValue();
    }

    public static class ClassUnderTestForReturnEach {
        private final CollaboratorForReturnEach collaborator;

        private ClassUnderTestForReturnEach(final CollaboratorForReturnEach collaborator) {
            this.collaborator = collaborator;
        }

        public String prependAndRead(String prepend) {
            return prepend + " " + collaborator.readValue();
        }
    }


}
