package org.apache.isis.applib.fixturescripts.personas.fixtures;

import javax.inject.Inject;

import org.apache.isis.applib.fixturescripts.personas.dom.Customer;
import org.apache.isis.applib.fixturescripts.personas.dom.CustomerRepository;
import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptWithResult;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CustomerBuilderScript extends BuilderScriptWithResult<Customer> {

    private final Customer_persona persona;

    @Override
    protected Customer buildResult(ExecutionContext ec) {
        return customerRepository.create(persona.getFirstName(), persona.getLastName(), persona.getAge());
    }

    @Inject CustomerRepository customerRepository;

}
