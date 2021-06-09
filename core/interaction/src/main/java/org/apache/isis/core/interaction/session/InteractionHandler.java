package org.apache.isis.core.interaction.session;

import java.util.concurrent.Callable;

import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.applib.services.iactnlayer.ThrowingRunnable;
import org.apache.isis.core.security.authentication.Authentication;

import lombok.NonNull;

public interface InteractionHandler {
    /**
     * If present, reuses the current top level {@link InteractionLayer}, otherwise creates a new
     * anonymous one.
     *
     * @see #openInteraction(InteractionContext)
     */
    InteractionLayer openInteraction();

    /**
     * Returns a new or reused {@link InteractionLayer} that is a holder of {@link Authentication}
     * on top of the current thread's authentication layer stack.
     * <p>
     * If available reuses an existing {@link Authentication}, otherwise creates a new one.
     * <p>
     * The {@link InteractionLayer} represents a user's span of activities interacting with
     * the application. The session's stack is later closed using {@link #closeInteractionLayers()}.
     *
     * @param interactionContext
     *
     * @apiNote if the current {@link InteractionLayer} (if any) has an {@link Authentication} that
     * equals that of the given one, as an optimization, no new layer is pushed onto the stack;
     * instead the current one is returned
     */
    InteractionLayer openInteraction(
            @NonNull InteractionContext interactionContext);

    /**
     * @return whether the calling thread is within the context of an open {@link InteractionLayer}
     */
    boolean isInInteraction();

    /**
     * Executes a block of code with a new or reused {@link InteractionContext} using a new or
     * reused {@link InteractionLayer}.
     *
     * <p>
     * If there is currently no {@link InteractionLayer} a new one is created.
     * </p>
     *
     * <p>
     * If there is currently an {@link InteractionLayer} that has an equal {@link InteractionContext}
     * to the given one, it is reused, otherwise a new one is created.
     * </p>
     *
     * @param interactionContext - the context to run under (non-null)
     * @param callable - the piece of code to run (non-null)
     */
    <R> R call(@NonNull InteractionContext interactionContext, @NonNull Callable<R> callable);

    /**
     * Variant of {@link #call(InteractionContext, Callable)} that takes a runnable.
     *
     * @param interactionContext - the user details to run under (non-null)
     * @param runnable (non-null)
     */
    void run(@NonNull InteractionContext interactionContext, @NonNull ThrowingRunnable runnable);

    /**
     * closes all open {@link InteractionLayer}(s) as stacked on the current thread
     */
    void closeInteractionLayers();
}
