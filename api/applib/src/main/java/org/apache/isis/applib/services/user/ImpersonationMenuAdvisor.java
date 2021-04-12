package org.apache.isis.applib.services.user;

import java.util.List;

/**
 * Enables {@link ImpersonateMenu#impersonateWithRoles(String, boolean, List)},
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
public interface ImpersonationMenuAdvisor {

    List<String> allUserNames();

    List<String> allRoleNames();

    List<String> roleNamesFor(final String applicationUserName);
}
