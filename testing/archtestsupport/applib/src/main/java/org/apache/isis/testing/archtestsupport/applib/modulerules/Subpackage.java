package org.apache.isis.testing.archtestsupport.applib.modulerules;

import java.util.List;

/**
 * Defines the rules for which subpackages or a module to check and whether the classes in those subpackages can access
 * the classes in other subpackages either for the same module, or for modules that are referenced
 * ({@link org.springframework.context.annotation.Import}ed) directly or transitively.
 *
 * @since 2.0 {@index}
 */
public interface Subpackage {

    /**
     * The name of the subpackage, for example &quot;dom&quot;, &quot;api&quot;, &quot;spi&quot; or &quot;fixtures&quot;.
     */
    String getName();

    /**
     * A list of the (names of the) subpackages where classes in the same module as this package have access.
     *
     * <p>
     *     For example, the &quot;dom&quot; subpackage can probably be referenced from the &quot;menu&quot; subpackage,
     *     but not vice versa.
     * </p>
     *
     * <p>
     *     The special value of &quot;*&quot; is a wildcard meaning that all subpackages (in the same module) can access.
     * </p>
     */
    List<String> mayBeAccessedBySubpackagesInSameModule();

    /**
     * A list of the (names of the) subpackages where classes in the packages of other referencing modules may
     * have access.
     *
     * <p>
     *     For example, in some cases the the &quot;dom&quot; subpackage may <i>not</i> be accessible from other
     *     modules if the intention is to require all programmatic access through an &quot;api&quot; subpackage
     *     (where the classes in <code>dom</code> implement interfaces defined in <code>api</code>).
     * </p>
     *
     * <p>
     *     The special value of &quot;*&quot; is a wildcard meaning that all subpackages (in other modules) can access.
     * </p>
     */
    List<String> mayBeAccessedBySubpackagesInReferencingModules();
}
