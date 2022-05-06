package org.apache.isis.applib.fixturescripts.personas.dom;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Person {

    private String firstName;
    private String lastName;
    private int age;

}
