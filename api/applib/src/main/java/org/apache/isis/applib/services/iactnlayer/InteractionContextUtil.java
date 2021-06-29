package org.apache.isis.applib.services.iactnlayer;

import org.apache.isis.applib.services.user.UserMemento;

import lombok.experimental.UtilityClass;


@UtilityClass
public class InteractionContextUtil{

    /**
     * For internal usage, not formal API.
     *
     * <p>
     *     Instead, use {@link InteractionContext#withUser(UserMemento)}, which honours the value semantics of this class.
     * </p>
     */
    public static void replaceUserIn(InteractionContext interactionContext, UserMemento userMemento) {
        interactionContext.replaceUser(userMemento);
    }

}
