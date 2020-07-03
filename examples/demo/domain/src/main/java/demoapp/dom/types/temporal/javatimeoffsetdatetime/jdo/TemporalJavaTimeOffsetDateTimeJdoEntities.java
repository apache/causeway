package demoapp.dom.types.temporal.javatimeoffsetdatetime.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class TemporalJavaTimeOffsetDateTimeJdoEntities {

    public Optional<TemporalJavaTimeOffsetDateTimeJdo> find(final java.time.OffsetDateTime readOnlyProperty) {
        return repositoryService.firstMatch(TemporalJavaTimeOffsetDateTimeJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<TemporalJavaTimeOffsetDateTimeJdo> all() {
        return repositoryService.allInstances(TemporalJavaTimeOffsetDateTimeJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
