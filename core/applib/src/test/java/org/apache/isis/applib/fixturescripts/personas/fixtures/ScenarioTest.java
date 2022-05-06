package org.apache.isis.applib.fixturescripts.personas.fixtures;

import org.apache.isis.applib.fixturescripts.personas.dom.Person;
import org.apache.isis.testing.fixtures.applib.setup.PersonaEnumPersistAll;


public class ScenarioTest {


    void setup() {
        PersonaEnumPersistAll<Person_persona, Person> persistAll = new PersonaEnumPersistAll<Person_persona, Person>(Person_persona.class);
    }
}
