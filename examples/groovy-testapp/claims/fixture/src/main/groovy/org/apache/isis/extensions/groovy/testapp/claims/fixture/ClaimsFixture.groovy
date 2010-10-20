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
