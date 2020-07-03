package demoapp.dom.types.temporal.javatimelocaldatetime.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class TemporalJavaTimeLocalDateTimeJdoEntities {

    public Optional<TemporalJavaTimeLocalDateTimeJdo> find(final java.time.LocalDateTime readOnlyProperty) {
        return repositoryService.firstMatch(TemporalJavaTimeLocalDateTimeJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<TemporalJavaTimeLocalDateTimeJdo> all() {
        return repositoryService.allInstances(TemporalJavaTimeLocalDateTimeJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
