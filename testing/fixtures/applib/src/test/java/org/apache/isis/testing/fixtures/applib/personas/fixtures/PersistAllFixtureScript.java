package org.apache.isis.testing.fixtures.applib.personas.fixtures;

import javax.resource.spi.work.ExecutionContext;

import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.personas.dom.Person;
import org.apache.isis.testing.fixtures.applib.setup.PersonaEnumPersistAll;


public class PersistAllFixtureScript extends FixtureScript {

    @Override
    protected void execute(ExecutionContext executionContext) {

        // create them all.
        final PersonaEnumPersistAll<Person, Person_persona, PersonBuilderScript> persistAll = new PersonaEnumPersistAll<>(Person_persona.class);

        executionContext.executeChildren(this, Person_persona.SteveSingle, Person_persona.MeghanMarriedMum);

        Person person = executionContext.executeChildT(this, Person_persona.SteveSingle.builder()).getObject();
    }
}
