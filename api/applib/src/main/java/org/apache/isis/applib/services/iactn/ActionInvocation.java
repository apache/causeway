package org.apache.isis.applib.services.iactn;

import java.util.List;

import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.ixn.v2.ActionInvocationDto;

import lombok.Getter;

/**
 * @since 1.x {@index}
 */
public class ActionInvocation extends Execution<ActionInvocationDto, ActionDomainEvent<?>> {

    @Getter
    private final List<Object> args;

    public ActionInvocation(
            final Interaction interaction,
            final String memberId,
            final Object target,
            final List<Object> args,
            final String targetMember,
            final String targetClass) {
        super(interaction, InteractionType.ACTION_INVOCATION, memberId, target, targetMember, targetClass);
        this.args = args;
    }
    // ...
}
