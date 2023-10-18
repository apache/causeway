package org.apache.causeway.core.metamodel.services.grid.spi;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.value.NamedWithMimeType;

import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.services.grid.GridLoaderServiceDefault;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * A simpler SPI for {@link GridLoaderServiceDefault}.
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".LayoutResourceLoaderDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor //JUnit Support
@Log4j2
public class LayoutResourceLoaderDefault implements LayoutResourceLoader {

    @Override
    public Optional<LayoutResource> tryLoadLayoutResource(
            final @NonNull Class<?> type,
            final @NonNull String candidateResourceName) {
        try {
            return Optional.ofNullable(
                            _Resources.loadAsStringUtf8(type, candidateResourceName))
                    .map(fileContent->new LayoutResource(
                            candidateResourceName,
                            NamedWithMimeType.CommonMimeType.valueOfFileName(candidateResourceName).orElseThrow(),
                            fileContent));
        } catch (IOException ex) {
            log.error(
                    "Failed to load layout file {} (relative to {}.class)",
                    candidateResourceName, type.getName(), ex);
        }
        return Optional.empty();
    }

}
