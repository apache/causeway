package org.apache.isis.applib.fixturescripts;

public interface EnumWithBuilderScript<T, F extends BuilderScriptAbstract<T,F>>  {

    F toFixtureScript();

}

