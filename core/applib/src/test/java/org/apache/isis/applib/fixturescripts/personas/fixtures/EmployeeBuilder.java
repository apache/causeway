package org.apache.isis.applib.fixturescripts.personas.fixtures;

import org.apache.isis.applib.fixturescripts.personas.dom.Employee;
import org.apache.isis.applib.fixturescripts.personas.dom.Person;
import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptAbstract;

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

    @Override
    public BuilderScriptAbstract setPrereq(Block prereq) {
        return null;
    }
}
