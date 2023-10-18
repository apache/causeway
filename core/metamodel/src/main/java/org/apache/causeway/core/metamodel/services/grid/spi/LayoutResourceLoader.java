package org.apache.causeway.core.metamodel.services.grid.spi;


import lombok.NonNull;

import java.util.Optional;

import org.apache.causeway.applib.annotation.Programmatic;

import org.apache.causeway.core.metamodel.services.grid.GridLoaderServiceDefault;

/**
 * A simpler SPI for {@link GridLoaderServiceDefault}.
 */
public interface LayoutResourceLoader {

    @Programmatic
    Optional<LayoutResource> tryLoadLayoutResource(
            final @NonNull Class<?> type,
            final @NonNull String candidateResourceName);

}
