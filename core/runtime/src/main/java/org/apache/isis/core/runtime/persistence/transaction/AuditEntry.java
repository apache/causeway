package org.apache.isis.core.runtime.persistence.transaction;

import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class AuditEntry {

    @NonNull private final AdapterAndProperty adapterAndProperty;
    @NonNull private final PreAndPostValues preAndPostValues;
    
}
