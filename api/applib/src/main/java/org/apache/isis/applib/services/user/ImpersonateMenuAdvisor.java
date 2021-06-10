package org.apache.isis.applib.services.user;

import java.util.List;

/**
 * Enables {@link ImpersonateMenu#impersonateWithRoles(String, List)},
 * which provides choices for user and roles.
 *
 * <p>
 *     This is in addition to the simpler
 *     {@link ImpersonateMenu#impersonate(String)} (which simply allows a
 *     username to be specified, with no roles).
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
     *     {@link ImpersonateMenu#impersonateWithRoles(String, List)}.
     * </p>
     */
    List<String> allUserNames();

    /**
     * Returns the names of all known roles.
     *
     * <p>
     *     The {@link ImpersonateMenu} uses this to provide a choices
     *     (drop-down) for the rolenames (list) argument of
     *     {@link ImpersonateMenu#impersonateWithRoles(String, List)}.
     * </p>
     */
    List<String> allRoleNames();

    /**
     * Returns the names of the roles of the specified username.
     *
     * <p>
     *     The {@link ImpersonateMenu} uses this to select the defaults
     *     for the rolenames (list) argument of
     *     {@link ImpersonateMenu#impersonateWithRoles(String, List)}.
     * </p>
     */
    List<String> roleNamesFor(final String username);

}
