package org.apache.isis.testing.fixtures.applib.personas.fixtures;

import javax.resource.spi.work.ExecutionContext;

import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptAbstract;
import org.apache.isis.testing.fixtures.applib.personas.dom.Employee;
import org.apache.isis.testing.fixtures.applib.personas.dom.Person;

import lombok.Getter;


public class EmployeeBuilder extends BuilderScriptAbstract<Employee> {

    private Person_persona persona;

    @Getter
    private Employee object;

    @Override
    protected void execute(ExecutionContext executionContext) {
        Person person = objectFor(persona, executionContext);
        object = Employee.builder().person(person).build();
    }

}
