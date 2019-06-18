package org.apache.isis.applib.services.xactn;

import java.util.UUID;

import org.apache.isis.applib.services.HasUniqueId;

import lombok.Data;

@Data(staticConstructor = "of")
public final class TransactionId implements HasUniqueId {

	private final UUID uniqueId;
	
	/**
     * The {@link HasUniqueId#getUniqueId()} is (as of 1.13.0) actually an identifier for the request/
     * interaction, and there can actually be multiple transactions within such a request/interaction.  
     * The sequence (0-based) is used to distinguish such.
     */
	private final int sequence;

}
