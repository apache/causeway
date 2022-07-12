package org.apache.isis.extensions.executionlog.applib.dom;

import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;

@EqualsAndHashCode(of = {"interactionId", "sequence"})
@NoArgsConstructor
public class ExecutionLogEntryPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SEPARATOR = "_";

    @Getter @Setter
    public UUID interactionId;
    @Getter @Setter
    public int sequence;

    public ExecutionLogEntryPK(final String value) {
        val token = new StringTokenizer (value, SEPARATOR);
        this.interactionId = UUID.fromString(token.nextToken());
        this.sequence = Integer.parseInt(token.nextToken());
    }

    @Override
    public String toString() {
        return interactionId + SEPARATOR + sequence;
    }

}
