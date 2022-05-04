package org.apache.isis.testing.fixtures.applib.personas.dom;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Customer {

    private String firstName;
    private String lastName;
    private int age;

}
