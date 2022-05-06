package org.apache.isis.testing.fixtures.applib.personas;

import org.apache.isis.applib.annotation.Programmatic;

public abstract class BuilderScriptWithResult<T>
        extends BuilderScriptAbstract<T> {

    public T object;

    /**
     * Simply returns the object returned by {@link #buildResult(ExecutionContext)}.
     */
    @Override
    public final T getObject() {
        return object;
    }


    /**
     * Concrete implementation that simply executes {@link #buildResult(ExecutionContext)} and stores the
     * result to be accessed by {@link #getObject()}.
     */
    @Override
    protected void execute(ExecutionContext executionContext) {
        object = buildResult(executionContext);
    }

    /**
     * Hook method to return a single object.
     */
    @Programmatic
    protected abstract T buildResult(final ExecutionContext executionContext);
}
