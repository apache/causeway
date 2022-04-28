package org.apache.isis.testing.integtestsupport.applib;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.core.security.authentication.manager.UserMementoRefiner;

/**
 * Use to execute integration tests where any {@link UserMementoRefiner} services are honoured.
 * These can be used to tweak the current user/role.  (Normally {@link UserMementoRefiner}s are only
 * consulted using the authentication process, but in integration tests the authentication phase is skipped).
 *
 * <p>
 * This can be useful for various use cases, though one use case is as an alternative to using the
 * {@link NoPermissionChecks} extension.
 * </p>
 *
 * <p>
 *     To use, annotate integration test class using <code>@ExtendWith(UserMementoRefiners.class)</code>
 * </p>
 */
public class UserMementoRefiners implements BeforeEachCallback {

    @Override
    public void beforeEach(final ExtensionContext extensionContext) {
        _Helper.getInteractionFactory(extensionContext)
            .ifPresent(interactionService ->
                interactionService.currentInteractionContext().ifPresent(
                    currentInteractionContext -> _Helper.getServiceRegistry(extensionContext).ifPresent(
                        serviceRegistry -> {
                            UserMemento user = currentInteractionContext.getUser();
                            for (UserMementoRefiner userMementoRefiner : serviceRegistry.select(UserMementoRefiner.class)) {
                                user = userMementoRefiner.refine(user);
                            }
                            interactionService.openInteraction(currentInteractionContext.withUser(user));
                        }
                    )
                )
            );
    }

}
