package org.apache.causeway.core.metamodel.services.grid.spi;

import lombok.NonNull;
import lombok.Value;

import org.apache.causeway.applib.value.NamedWithMimeType;

@Value
public class LayoutResource {
    private final @NonNull String resourceName;
    private final @NonNull NamedWithMimeType.CommonMimeType format;
    private final @NonNull String content;
}
