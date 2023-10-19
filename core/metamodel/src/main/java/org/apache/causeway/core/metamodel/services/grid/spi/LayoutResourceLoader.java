package org.apache.causeway.core.metamodel.services.grid.spi;


import java.util.Optional;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.metamodel.services.grid.GridLoaderServiceDefault;

import lombok.NonNull;

/**
 * A simpler SPI for {@link GridLoaderServiceDefault}.
 *
 * @since 2.0 {@index}}
 */
public interface LayoutResourceLoader {

    /**
     * Try to locate and load a {@link LayoutResource} by type and name.
     */
    @Programmatic
    Try<LayoutResource> tryLoadLayoutResource(
            final @NonNull Class<?> type,
            final @NonNull String candidateResourceName);

    /**
     * Optionally returns a {@link LayoutResource} based
     * on whether it could be resolved by type and name
     * and successfully read.
     * <p>
     * Silently ignores exceptions underneath, if any.
     */
    @Programmatic
    default Optional<LayoutResource> lookupLayoutResource(
            final @NonNull Class<?> type,
            final @NonNull String candidateResourceName) {
        return tryLoadLayoutResource(type, candidateResourceName)
                .getValue();
    }

}
