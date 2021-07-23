package org.apache.isis.testing.archtestsupport.applib.modulerules;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import lombok.RequiredArgsConstructor;

/**
 * A default implementation of {@link Subpackage} that constrains how (the classes in) subpackages across and within
 * modules can reference either.
 *
 * @since 2.0 {@index}
 */
@RequiredArgsConstructor
public enum SubpackageEnum implements Subpackage {

    /**
     * The <i>domain object model</i>module, containing the main business logic of the module.
     *
     * <p>
     * Typically consists of entities and view models and associated repositories and stateless services used by
     * those entities and view models.
     * </p>
     */
    dom(
            singletonList("*"), // wildcard means that all subpackages in this module can access 'dom'
            singletonList("*")  // wildcard means that all subpackages in other modules can access 'dom'
    ),
    /**
     * Optional, constitutes a formal API to the module.
     *
     * <p>
     * If used, then access to the <i>dom</i> module is likely to be restricted to only the subpackages of its
     * own &quot;local&quot; module with no access granted to subpackages of other &quot;external&quot;
     * referencing modules.
     * </p>
     */
    api(
            singletonList("*"), // wildcard means that all subpackages in this module can access 'api'
            singletonList("*")  // wildcard means that all subpackages in other modules can access 'api'
    ),
    /**
     * Optional, but if used then will hold view model that implement some sort of assisted business process, for
     * example dashboards, or filtering.
     */
    app(
            asList("fixtures", "integtests"), // only tests should call
            asList("fixtures", "integtests")  // only tests should call
    ),
    /**
     * Holds the menus that are visible in the UI.
     *
     * <p>
     * These should only be called by the framework, not programmatically (except for tests).
     * </p>
     */
    menu(
            asList("fixtures", "integtests"), // only tests should call
            asList("fixtures", "integtests")  // only tests should call
    ),
    /**
     * Holds mixins that contribute functionality to OTHER modules.
     *
     * <p>
     * Mixins are one of the main techniques for decoupling dependencies between modules.
     * </p>
     *
     * <p>
     * Note that mixins to THIS module normally would just live in the <i>dom</i> subpackage, eg <i>dom.mixins</i>.
     * </p>
     */
    contributions(
            asList("subscribers",               // may subscribe to events fired by mixins
                    "fixtures", "integtests"),  // tests can also call
            asList("subscribers",           // may subscribe to events fired by mixins
                    "fixtures", "integtests")   // tests can also call
    ),
    /**
     * Holds domain services that subscribe to events fired from OTHER modules.
     *
     * <p>
     * Subscriptions is the other main technique for decoupling dependencies between modules.
     * </p>
     */
    subscribers(
            asList("fixtures", "integtests"), // only tests should call
            asList("fixtures", "integtests")  // only tests should call
    ),
    /**
     * Holds the menus that are visible as the REST endpoints.
     *
     * <p>
     * These should only be called by the framework, not programmatically (except for tests).
     * </p>
     */
    restapi(
            asList("fixtures", "integtests"), // only tests should call
            asList("fixtures", "integtests")  // only tests should call
    ),
    /**
     * Define an interface for OTHER modules to implement; this is therefore an alternative and
     * more structured way to decoupled modules.
     *
     * <p>
     * With events, the module that emits the event doesn't know much about what the action to be
     * performed in the other module might be.  But if we use an SPI, then this module will call
     * all implementations of the SPI at certain well-defined points; so it kind of is like a
     * lifecycle sort of thing.
     * </p>
     *
     * <p>
     * Also, the SPI's interface could be more exotic, ie "fatter" than the simple event.
     * </p>
     */
    spi(
            asList("dom", "contributions", "subscribers"),  // callers of a module's own SPI
            singletonList("spiimpl")                        // other modules should only implement the SPI
    ),
    /**
     * These are this module's implementations of OTHER modules' SPI services.
     */
    spiimpl(
            asList("fixtures", "integtests"), // only tests should call
            asList("fixtures", "integtests")  // only tests should call
    ),
    /**
     * Fixture scripts used to setup the systen when prototyping and for integ tests.
     */
    fixtures(
            asList("seed", "integtests"),   // no restrictions
            asList("seed", "integtests")    // no restrictions
    ),
    /**
     * Seed scripts used to setup the systen, for example reference data).
     */
    seed(
            emptyList(), // should not be called directly
            emptyList()  // should not be called directly
    ),
    integtests(
            emptyList(), // should not be called directly
            emptyList()  // should not be called directly
    ),
    ;

    final List<String> local;
    final List<String> referencing;

    public String getName() {
        return name();
    }

    @Override
    public List<String> mayBeAccessedBySubpackagesInSameModule() {
        return local;
    }

    @Override
    public List<String> mayBeAccessedBySubpackagesInReferencingModules() {
        return referencing;
    }

    private static String[] asArray(List<String> list) {
        return list != null ?
                list.toArray(new String[] {}) : null;
    }
}
