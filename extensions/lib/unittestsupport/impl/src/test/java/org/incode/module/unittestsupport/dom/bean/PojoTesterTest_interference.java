package org.incode.module.unittestsupport.dom.bean;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.incode.module.unittestsupport.dom.bean.PojoTester;

import static org.hamcrest.Matchers.containsString;

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
