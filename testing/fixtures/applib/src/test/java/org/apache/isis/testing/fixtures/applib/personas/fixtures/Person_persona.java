package org.apache.isis.testing.fixtures.applib.personas.fixtures;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.testing.fixtures.applib.personas.Persona;
import org.apache.isis.testing.fixtures.applib.personas.dom.Person;
import org.apache.isis.testing.fixtures.applib.personas.dom.PersonRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Person_persona
        implements Persona<Person, PersonBuilderScript> {

    SteveSingle(1, "Steve", "Single", 21),
    MeghanMarriedMum(2, "Meghan", "Married-Mum", 35);

    private final int id;
    private final String firstName;
    private final String lastName;
    private final int age;

    @Override
    public PersonBuilderScript builder() {
        return new PersonBuilderScript(this);
    }

    @Override
    public Person findUsing(ServiceRegistry serviceRegistry2) {
        return serviceRegistry2.lookupServiceElseFail(PersonRepository.class).findById(id).orElseThrow(RuntimeException::new);
    }
}
