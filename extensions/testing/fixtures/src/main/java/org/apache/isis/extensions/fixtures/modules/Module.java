package org.apache.isis.extensions.fixtures.modules;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;

/**
 * A module is a class that implements this {@link Module} interface, but in addition is expected to be annotated with
 * {@link Configuration @Configuration} and which defines a dependency other modules by {@link Import @Import}ing them,
 * and conversely may be a dependency of other modules if they import it.
 *
 * <p>
 * Modules are therefore classes that define a module hierarchy using these Spring annotations.
 * </p>
 *
 * <p>
 * These are, in effect, a module hierarchy, declared using types.
 * </p>
 *
 * <p>
 *     Optionally, the <code>@Configuration</code> class can implements this {@link Module} interface.
 *     Doing so allows it to declare setup and teardown fixtures, eg to set up permanent ref data or to teardown
 *     test entities within the module.
 * </p>
 * <p>
 *     These setup/teardown fixtures will be called in the correct order as per the transitive dependency graph
 *     inferred from the <code>@Configuration</code> imports.
 * </p>
 */
public interface Module {

    /**
     * Optionally each module can define a {@link FixtureScript} which holds immutable "reference data".
     *
     * <p>
     * These are automatically executed whenever running integration tests (but are ignored when bootstrapping the
     * runtime as a webapp).
     * </p>
     *
     * <p>
     *     By default, returns a {@link FixtureScript#NOOP noop}.
     * </p>
     */
    default FixtureScript getRefDataSetupFixture() {
        return FixtureScript.NOOP;
    }

    /**
     * Optionally each module can define a tear-down {@link FixtureScript}, used to remove the contents of <i>all</i>
     * entities (both reference data and operational/transactional data).
     *
     * <p>
     * These are automatically executed whenever running integration tests (but are ignored when bootstrapping the
     * runtime as a webapp).
     * </p>
     *
     * <p>
     *     By default, returns a {@link FixtureScript#NOOP noop}.
     * </p>
     */
    default FixtureScript getTeardownFixture() {
        return FixtureScript.NOOP;
    }

}
