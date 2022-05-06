package org.apache.isis.applib.fixturescripts.personas.fixtures;

import javax.inject.Inject;

import org.apache.isis.applib.fixturescripts.personas.dom.Person;
import org.apache.isis.applib.fixturescripts.personas.dom.PersonRepository;
import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptWithResult;

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
