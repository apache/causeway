package org.apache.isis.testing.fixtures.applib.personas.dom;

import java.util.Optional;

public class PersonRepository {

    public Person create(String firstName, String lastName, int age) {
        return Person.builder()
                .firstName(firstName)
                .lastName(lastName)
                .age(age)
                .build();
    }

    public Optional<Person> findById(int id) {
        return Optional.empty();
    }
}
