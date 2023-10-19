package org.apache.causeway.core.metamodel.services.grid.spi;


import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.value.NamedWithMimeType;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.io.DataSource;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.services.grid.GridLoaderServiceDefault;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A simpler SPI for {@link GridLoaderServiceDefault}.
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".LayoutResourceLoaderDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor //JUnit Support
//@Log4j2
public class LayoutResourceLoaderDefault implements LayoutResourceLoader {

    @Override
    public Try<LayoutResource> tryLoadLayoutResource(
            final @NonNull Class<?> type,
            final @NonNull String candidateResourceName) {

        return DataSource.ofResource(type, candidateResourceName)
            .tryReadAsStringUtf8()
            .mapSuccessWhenPresent(fileContent->
                new LayoutResource(
                        candidateResourceName,
                        NamedWithMimeType.CommonMimeType.valueOfFileName(candidateResourceName).orElseThrow(),
                        fileContent));
    }

}
