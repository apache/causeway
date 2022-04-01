package org.apache.isis.testing.fixtures.applib.setup;

import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptAbstract;
import org.apache.isis.testing.fixtures.applib.personas.PersonaWithBuilderScript;

public class PersonaEnumPersistAll<
        E extends Enum<E> & PersonaWithBuilderScript<? extends BuilderScriptAbstract<T>>,
        T>
        extends org.apache.isis.applib.fixturescripts.setup.PersonaEnumPersistAll{


    public PersonaEnumPersistAll(Class personaEnumClass) {
        super(personaEnumClass);
    }
}
