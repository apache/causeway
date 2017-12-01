package org.apache.isis.applib.fixturescripts;

public interface PersonaWithBuilderScript<T, F extends BuilderScriptAbstract<T,F>>  {

    F toBuilderScript();

}

