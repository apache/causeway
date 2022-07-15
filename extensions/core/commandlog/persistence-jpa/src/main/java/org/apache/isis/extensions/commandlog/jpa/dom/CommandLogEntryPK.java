package org.apache.isis.extensions.commandlog.jpa.dom;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.bookmark.IdStringifier;
import org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.isis.persistence.jpa.integration.typeconverters.java.util.JavaUtilUuidConverter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(of = {"interactionId"})
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CommandLogEntryPK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Convert(converter = JavaUtilUuidConverter.class)
    @Column(name = CommandLogEntry.InteractionId.NAME, nullable = CommandLogEntry.InteractionId.NULLABLE, length = CommandLogEntry.InteractionId.MAX_LENGTH)
    @Getter(AccessLevel.PACKAGE)
    private UUID interactionId;

    @Override
    public String toString() {
        return interactionId != null ? interactionId.toString() : null;
    }


    @Component
    public static class Stringifier extends IdStringifier.AbstractWithPrefix<CommandLogEntryPK> {

        public Stringifier() {
            super(CommandLogEntryPK.class, "u");
        }

        @Override
        public String doStringify(CommandLogEntryPK value) {
            return value.getInteractionId().toString();
        }

        @Override
        protected CommandLogEntryPK doDestring(String stringified, Class<?> owningEntityType) {
            return new CommandLogEntryPK(UUID.fromString(stringified));
        }
    }
}
