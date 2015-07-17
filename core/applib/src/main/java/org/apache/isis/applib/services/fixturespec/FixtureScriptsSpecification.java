package org.apache.isis.applib.services.fixturespec;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.fixturescripts.FixtureResultList;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.fixturescripts.FixtureScripts;

/**
 * Pulls together the various state that influences the behaviour of {@link FixtureScripts} service.
 */
public class FixtureScriptsSpecification {

    /**
     * In Whether to show available {@link FixtureScript}s using a choices or an autocomplete.
     */
    public enum DropDownPolicy {
        AUTO_COMPLETE,
        CHOICES;

        public boolean isAutoComplete() {
            return this == AUTO_COMPLETE;
        }

        public boolean isChoices() {
            return this == CHOICES;
        }
    }

    /**
     * Typically preferable to use the create using the {@link FixtureScriptsSpecification.Builder}
     * (obtained from {@link #builder(Class)}).
     * @param packagePrefix  - to search for fixture script implementations, eg "com.mycompany"
     * @param nonPersistedObjectsStrategy - how to handle any non-persisted objects that are {@link FixtureScripts#newFixtureResult(FixtureScript, String, Object, boolean) added} to a {@link FixtureResultList}.
     * @param multipleExecutionStrategy - whether more than one instance of the same fixture script class can be run multiple times
     * @param runScriptDefaultScriptClass - the fixture script to provide as a default in {@link FixtureScripts#runFixtureScript(FixtureScript, String)} action.
     * @param runScriptDropDownPolicy - whether the {@link FixtureScripts#runFixtureScript(FixtureScript, String)} should use a choices or an autoComplete.
     * @param recreateScriptClass - if specified, then make the {@link FixtureScriptsDefault#recreateObjectsAndReturnFirst()} action visible.
     */
    public FixtureScriptsSpecification(
            final String packagePrefix,
            final FixtureScripts.NonPersistedObjectsStrategy nonPersistedObjectsStrategy,
            final FixtureScripts.MultipleExecutionStrategy multipleExecutionStrategy,
            final Class<? extends FixtureScript> runScriptDefaultScriptClass,
            final DropDownPolicy runScriptDropDownPolicy,
            final Class<? extends FixtureScript> recreateScriptClass) {
        this.packagePrefix = packagePrefix;
        this.nonPersistedObjectsStrategy = nonPersistedObjectsStrategy;
        this.multipleExecutionStrategy = multipleExecutionStrategy;
        this.recreateScriptClass = recreateScriptClass;
        this.runScriptDefaultScriptClass = runScriptDefaultScriptClass;
        this.dropDownPolicy = runScriptDropDownPolicy;
    }

    private final String packagePrefix;
    private final FixtureScripts.NonPersistedObjectsStrategy nonPersistedObjectsStrategy;
    private final FixtureScripts.MultipleExecutionStrategy multipleExecutionStrategy;

    private final Class<? extends FixtureScript> recreateScriptClass;
    private final Class<? extends FixtureScript> runScriptDefaultScriptClass;
    private final DropDownPolicy dropDownPolicy;

    @Programmatic
    public String getPackagePrefix() {
        return packagePrefix;
    }

    @Programmatic
    public FixtureScripts.NonPersistedObjectsStrategy getNonPersistedObjectsStrategy() {
        return nonPersistedObjectsStrategy;
    }

    @Programmatic
    public FixtureScripts.MultipleExecutionStrategy getMultipleExecutionStrategy() {
        return multipleExecutionStrategy;
    }

    @Programmatic
    public Class<? extends FixtureScript> getRunScriptDefaultScriptClass() {
        return runScriptDefaultScriptClass;
    }

    @Programmatic
    public DropDownPolicy getRunScriptDropDownPolicy() {
        return dropDownPolicy;
    }

    @Programmatic
    public Class<? extends FixtureScript> getRecreateScriptClass() {
        return recreateScriptClass;
    }

    public static class Builder {
        private final String packagePrefix;
        private FixtureScripts.NonPersistedObjectsStrategy nonPersistedObjectsStrategy = FixtureScripts.NonPersistedObjectsStrategy.PERSIST;
        private FixtureScripts.MultipleExecutionStrategy multipleExecutionStrategy = FixtureScripts.MultipleExecutionStrategy.IGNORE;
        private Class<? extends FixtureScript> recreateScriptClass = null;
        private Class<? extends FixtureScript> defaultScriptClass = null;
        private DropDownPolicy dropDownPolicy = DropDownPolicy.CHOICES;

        public Builder(final Class<?> contextClass) {
            this(contextClass.getPackage().getName());
        }
        public Builder(final String packagePrefix) {
            this.packagePrefix = packagePrefix;
        }

        public Builder with(FixtureScripts.NonPersistedObjectsStrategy nonPersistedObjectsStrategy) {
            this.nonPersistedObjectsStrategy = nonPersistedObjectsStrategy;
            return this;
        }
        public Builder with(FixtureScripts.MultipleExecutionStrategy multipleExecutionStrategy) {
            this.multipleExecutionStrategy = multipleExecutionStrategy;
            return this;
        }
        public Builder withRecreate(Class<? extends FixtureScript> recreateScriptClass) {
            this.recreateScriptClass = recreateScriptClass;
            return this;
        }
        public Builder withRunScriptDefault(Class<? extends FixtureScript> defaultScriptClass) {
            this.defaultScriptClass = defaultScriptClass;
            return this;
        }
        public Builder withRunScriptDropDown(DropDownPolicy dropDownPolicy) {
            this.dropDownPolicy = dropDownPolicy;
            return this;
        }

        public FixtureScriptsSpecification build() {
            return new FixtureScriptsSpecification(
                    packagePrefix,
                    nonPersistedObjectsStrategy, multipleExecutionStrategy,
                    defaultScriptClass, dropDownPolicy, recreateScriptClass
            );
        }
    }

    public static Builder builder(final Class<?> contextClass) {
        return new Builder(contextClass);
    }
    public static Builder builder(final String packagePrefix) {
        return new Builder(packagePrefix);
    }
}
