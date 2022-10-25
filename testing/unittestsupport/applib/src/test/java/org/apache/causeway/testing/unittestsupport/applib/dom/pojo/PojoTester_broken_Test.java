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
package org.apache.causeway.testing.unittestsupport.applib.dom.pojo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

public class PojoTester_broken_Test {

    public static class CustomerWithInterferingProperties {
        private String firstName = "";
        public String getFirstName() {
            return firstName + lastName; // this is the deliberate error
        }
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        private String lastName = "";
        public String getLastName() {
            return lastName;
        }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }
    
    @Test
    public void strict_when_interference_between_properties() {
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create().exercise(new CustomerWithInterferingProperties());
        }).isInstanceOf(AssertionFailedError.class)
                .hasMessageContaining("firstName");
    }

    public static class BrokenCustomer {
        private String someString;
        public String getSomeString() {
            return someString;
        }
        public void setSomeString(String someString) {
            // no-op
        }
    }

    @org.junit.jupiter.api.Test
    public void exercise_broken() {

        // given
        final BrokenCustomer customer = new BrokenCustomer();

        // when, then
        Assertions.assertThatThrownBy(() -> {
            PojoTester.create()
                    .exercise(customer);
        }).isInstanceOf(org.opentest4j.AssertionFailedError.class)
                .withFailMessage(() -> "someString: null");

    }

}
