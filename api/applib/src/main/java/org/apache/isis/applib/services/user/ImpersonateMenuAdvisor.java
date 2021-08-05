package org.apache.isis.applib.services.user;

import java.util.List;

/**
 * Enables {@link ImpersonateMenu#impersonateWithRoles(String, List, String)}, to provides choices for user and roles.
 *
 * <p>
 *     This will result in the simpler {@link ImpersonateMenu#impersonate(String)} (which simply allows a
 *     username to be specified, with no roles) being hidden.
 * </p>
 *
 * @since 2.0 {@index}
 */
public interface ImpersonateMenuAdvisor {

    /**
     * Returns the names of all known users.
     *
     * <p>
     *     The {@link ImpersonateMenu} uses this to provide a choices
     *     (drop-down) for the username (string) argument of
     *     {@link ImpersonateMenu#impersonateWithRoles(String, List, String)}.
     * </p>
     */
    List<String> allUserNames();

    /**
     * Returns the names of all known roles.
     *
     * <p>
     *     The {@link ImpersonateMenu} uses this to provide a choices
     *     (drop-down) for the rolenames (list) argument of
     *     {@link ImpersonateMenu#impersonateWithRoles(String, List, String)}.
     * </p>
     */
    List<String> allRoleNames();

    /**
     * Returns the names of the roles of the specified username.
     *
     * <p>
     *     The {@link ImpersonateMenu} uses this to select the defaults
     *     for the rolenames (list) argument of
     *     {@link ImpersonateMenu#impersonateWithRoles(String, List, String)}.
     * </p>
     */
    List<String> roleNamesFor(final String username);

    /**
     * Returns the multi-tenancy token of the specified username.
     *
     * <p>
     *     The {@link ImpersonateMenu} uses this to select the defaults
     *     for the rolenames (list) argument of
     *     {@link ImpersonateMenu#impersonateWithRoles(String, List, String)}.
     * </p>
     */
    String multiTenancyTokenFor(final String username);

}
