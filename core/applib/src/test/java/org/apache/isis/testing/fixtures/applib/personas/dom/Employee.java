package org.apache.isis.testing.fixtures.applib.personas.dom;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Employee {

    private Person person;

}
