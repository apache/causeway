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
package org.apache.isis.extensions.groovy.testapp.claims.fixture;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.applib.value.Money;
import org.apache.isis.applib.value.Date;

import org.apache.isis.extensions.groovy.testapp.claims.dom.claim.Claim;
import org.apache.isis.extensions.groovy.testapp.claims.dom.employee.Employee;

import org.apache.isis.extensions.groovy.applib.DomainObjectBuilder;


class ClaimsFixture extends AbstractFixture {
    
    @Override
    public void install() {
        def builder = new DomainObjectBuilder(getContainer(), Employee.class, Claim.class)
        
        builder.employee(id: 'fred', name:"Fred Smith")
        builder.employee(id: "tom", name: "Tom Brown") { approver( refId: 'fred') }
        builder.employee(name: "Sam Jones") { approver( refId: 'fred') }
        
        builder.claim(id: 'tom:1', date: days(-16), description: "Meeting with client") {
            claimant( refId: 'tom')
            claimItem( dateIncurred: days(-16), amount: money(38.50), description: "Lunch with client")
            claimItem( dateIncurred: days(-16), amount: money(16.50), description: "Euston - Mayfair (return)")
        }
        builder.claim(id: 'tom:2', date: days(-18), description: "Meeting in city office") {
            claimant( refId: 'tom')
            claimItem( dateIncurred: days(-18), amount: money(18.00), description: "Car parking")
            claimItem( dateIncurred: days(-18), amount: money(26.50), description: "Reading - London (return)")
        }
        builder.claim(id: 'fred:1', date: days(-14), description: "Meeting at clients") {
            claimant( refId: 'fred')
            claimItem( dateIncurred: days(-14), amount: money(18.00), description: "Car parking")
            claimItem( dateIncurred: days(-14), amount: money(26.50), description: "Reading - London (return)")
        }
    }
    
    private Date days(int days) { new Date().add(0,0,days) }
    
    private Money money(double amount) { new Money(amount, "USD") }
}
