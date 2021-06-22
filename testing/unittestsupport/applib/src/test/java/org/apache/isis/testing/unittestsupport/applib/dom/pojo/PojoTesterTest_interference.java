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
package org.apache.isis.testing.unittestsupport.applib.dom.pojo;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.containsString;

import org.apache.isis.testing.unittestsupport.applib.dom.pojo.PojoTester;

public class PojoTesterTest_interference {
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none().handleAssertionErrors();

    public static class Customer {
        private String firstName;
        public String getFirstName() {
            return firstName;
        }
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        
        private String lastName;
        public String getLastName() {
            return lastName;
        }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }
    
    @Test
    public void strict_happyCase() {
        PojoTester.strict().exercise(new Customer());
    }

    
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
    public void strict_whenInterferenceBetweenProperties() {
        expectedException.expectMessage(containsString("firstName"));
        PojoTester.strict().exercise(new CustomerWithInterferingProperties());
    }

}
