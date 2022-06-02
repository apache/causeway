package org.apache.isis.testing.fixtures.applib.personas.fixtures;

import org.apache.isis.testing.fixtures.applib.setup.PersonaEnumPersistAll;

import lombok.val;


public class ScenarioTest {


    void setup() {
        val persistAll = new PersonaEnumPersistAll<>(Person_persona.class);
    }
}
