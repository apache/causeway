package org.apache.isis.testing.fixtures.applib.personas.fixtures;

import javax.inject.Inject;
import javax.resource.spi.work.ExecutionContext;

import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptWithResult;
import org.apache.isis.testing.fixtures.applib.personas.dom.Person;
import org.apache.isis.testing.fixtures.applib.personas.dom.PersonRepository;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class PersonBuilderScript extends BuilderScriptWithResult<Person> {

    private final Person_persona persona;

    @Override
    protected Person buildResult(ExecutionContext ec) {
        return customerRepository.create(persona.getFirstName(), persona.getLastName(), persona.getAge());
    }

    @Inject PersonRepository customerRepository;

}
