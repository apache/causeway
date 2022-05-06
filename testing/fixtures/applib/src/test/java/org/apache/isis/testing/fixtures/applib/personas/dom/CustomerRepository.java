package org.apache.isis.testing.fixtures.applib.personas.dom;

import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public class CustomerRepository {

    public Customer create(String firstName, String lastName, int age) {
        return Customer.builder()
                .firstName(firstName)
                .lastName(lastName)
                .age(age)
                .build();
    }

    public Optional<Customer> findById(int id) {
        return Optional.empty();
    }
}
