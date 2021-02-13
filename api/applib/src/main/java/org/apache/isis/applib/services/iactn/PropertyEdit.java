package org.apache.isis.applib.services.iactn;

import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.schema.common.v2.InteractionType;
import org.apache.isis.schema.ixn.v2.PropertyEditDto;

import lombok.Getter;

/**
 * @since 1.x {@index}
 */
public class PropertyEdit extends Execution<PropertyEditDto, PropertyDomainEvent<?, ?>> {

    @Getter
    private final Object newValue;

    public PropertyEdit(
            final Interaction interaction,
            final String memberId,
            final Object target,
            final Object newValue,
            final String targetMember,
            final String targetClass) {
        super(interaction, InteractionType.PROPERTY_EDIT, memberId, target, targetMember, targetClass);
        this.newValue = newValue;
    }

    // ...
}
