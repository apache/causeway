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
package org.apache.isis.core.metamodel.services.metamodel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jmock.Expectations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.metamodel.DomainMember;
import org.apache.isis.applib.services.metamodel.DomainModel;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.commons.internal.context._Context;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.spec.Hierarchical;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionDefault;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

class MetaModelServiceDefaultTest {

    ServiceInjector stubServicesInjector;
    MetaModelServiceDefault mockMetaModelService;
    ObjectAction action;
    ObjectSpecification mockSpec;

    FacetedMethod mockFacetedMethod;


    @BeforeEach
    void setUp() throws Exception {

        _Context.clear();
        JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);


        mockFacetedMethod = context.mock(FacetedMethod.class);
        Matcher<Class<? extends Facet>> facetMatcher = _Casts.uncheckedCast(Matchers.any(Class.class));
        context.checking(new Expectations() {
            {
                allowing(mockFacetedMethod).getIdentifier();
                will(returnValue(Identifier.actionIdentifier("Customer", "reduceheadcount")));

                allowing(mockFacetedMethod).getFacet(with(facetMatcher));
                will(returnValue(null));

                allowing(mockFacetedMethod).getParameters();
                will(returnValue(Collections.emptyList()));
            }
        });

        mockSpec = context.mock(ObjectSpecification.class);
        context.checking(new Expectations() {
            {
                allowing(mockSpec).getFullIdentifier();
                will(returnValue("mocked"));

                allowing(mockSpec).subclasses(Hierarchical.Depth.DIRECT);
                will(returnValue(Collections.emptyList()));

                allowing(mockSpec).isManagedBean();
                will(returnValue(true));
            }
        });

        action = new ObjectActionDefault(mockFacetedMethod);

        mockMetaModelService = context.mock(MetaModelServiceDefault.class);
        context.checking(new Expectations() {
            {
                allowing(mockMetaModelService).getDomainModel();
                will(returnValue(new DomainModelDefault(_Lists.of(new DomainMemberDefault(mockSpec, action)))));

            }
        });

    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test @DisplayName("member to XML marshalling should not throw")
    void member_marshalling() throws JAXBException {
        DomainMember domainMember = new DomainMemberDefault(mockSpec, action);

        JAXBContext jaxbContext = JAXBContext.newInstance(DomainMemberDefault.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        jaxbMarshaller.marshal(domainMember, noopOutput());
    }

    @Test @DisplayName("model to XML marshalling should not throw")
    void model_marshalling() throws JAXBException {
        DomainModel domainMembers = mockMetaModelService.getDomainModel();

        JAXBContext jaxbContext = JAXBContext.newInstance(DomainModelDefault.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        jaxbMarshaller.marshal(domainMembers, noopOutput());
    }


    @Test @DisplayName("example to XML marshalling should not throw")
    void example_marshalling() throws JAXBException {

        JAXBContext jaxbContext = JAXBContext.newInstance(Employees.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        jaxbMarshaller.marshal(createEmployees(), noopOutput());
    }

    // -- HELPER

    private OutputStream noopOutput(){
        return new OutputStream() {
            @Override public void write(int b) throws IOException {}
        };  
    }

    @XmlRootElement(name = "employee")
    @XmlAccessorType (XmlAccessType.FIELD)
    public static class Employee
    {
        private Integer id;
        private String firstName;
        private String lastName;
        private double income;

        //Getters and Setters

        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }
        public String getFirstName() {
            return firstName;
        }
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        public String getLastName() {
            return lastName;
        }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        public double getIncome() {
            return income;
        }
        public void setIncome(double income) {
            this.income = income;
        }

    }

    @XmlRootElement(name = "employees")
    @XmlAccessorType (XmlAccessType.FIELD)
    public static class Employees
    {
        @XmlElement(name = "employee")
        private List<Employee> employees = null;

        public List<Employee> getEmployees() {
            return employees;
        }

        public void setEmployees(List<Employee> employees) {
            this.employees = employees;
        }
    }

    private Employees createEmployees(){
        Employees employees = new Employees();
        employees.setEmployees(new ArrayList<Employee>());
        //Create two employees
        Employee emp1 = new Employee();
        emp1.setId(1);
        emp1.setFirstName("Lokesh");
        emp1.setLastName("Gupta");
        emp1.setIncome(100.0);

        Employee emp2 = new Employee();
        emp2.setId(2);
        emp2.setFirstName("John");
        emp2.setLastName("Mclane");
        emp2.setIncome(200.0);

        //Add the employees in list
        employees.getEmployees().add(emp1);
        employees.getEmployees().add(emp2);

        return employees;
    }



}
