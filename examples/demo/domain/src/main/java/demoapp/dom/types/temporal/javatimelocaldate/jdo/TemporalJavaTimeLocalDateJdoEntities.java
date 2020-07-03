package demoapp.dom.types.temporal.javatimelocaldate.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class TemporalJavaTimeLocalDateJdoEntities {

    public Optional<TemporalJavaTimeLocalDateJdo> find(final java.time.LocalDate readOnlyProperty) {
        return repositoryService.firstMatch(TemporalJavaTimeLocalDateJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<TemporalJavaTimeLocalDateJdo> all() {
        return repositoryService.allInstances(TemporalJavaTimeLocalDateJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
