package org.apache.isis.testing.fixtures.applib.personas.fixtures;


import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.testing.fixtures.applib.personas.Persona;
import org.apache.isis.testing.fixtures.applib.personas.dom.Customer;
import org.apache.isis.testing.fixtures.applib.personas.dom.CustomerRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Customer_persona
        implements Persona<Customer, CustomerBuilderScript> {

    SteveSingle(1, "Steve", "Single", 21),
    MeghanMarriedMum(2, "Meghan", "Married-Mum", 35);

    private final int id;
    private final String firstName;
    private final String lastName;
    private final int age;

    @Override
    public CustomerBuilderScript builder() {
        return new CustomerBuilderScript(this);
    }

    @Override
    public Customer findUsing(ServiceRegistry serviceRegistry) {
        return serviceRegistry.lookupServiceElseFail(CustomerRepository.class).findById(id).orElseThrow();
    }
}
