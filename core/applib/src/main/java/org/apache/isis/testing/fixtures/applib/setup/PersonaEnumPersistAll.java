package org.apache.isis.testing.fixtures.applib.setup;

import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptAbstract;
import org.apache.isis.testing.fixtures.applib.personas.PersonaWithBuilderScript;

public class PersonaEnumPersistAll<
            T,
            E extends Enum<E> & PersonaWithBuilderScript<T, B>,
            B extends BuilderScriptAbstract<T, B>
        >
        extends org.apache.isis.testing.fixtures.applib.personas.PersonaEnumPersistAll {


    public PersonaEnumPersistAll(Class<E> personaEnumClass) {
        super(personaEnumClass);
    }
}
