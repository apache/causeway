package org.apache.isis.applib.fixturescripts.personas.fixtures;

import org.apache.isis.applib.fixturescripts.personas.dom.Customer;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.setup.PersonaEnumPersistAll;


public class ScenarioTest {


    void setup() {
        PersonaEnumPersistAll<Customer_persona, Customer> persistAll = new PersonaEnumPersistAll<Customer_persona, Customer>(Customer_persona.class);
    }
}
