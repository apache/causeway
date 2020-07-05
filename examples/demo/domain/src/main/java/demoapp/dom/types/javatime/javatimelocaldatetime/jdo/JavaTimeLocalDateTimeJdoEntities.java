package demoapp.dom.types.javatime.javatimelocaldatetime.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class JavaTimeLocalDateTimeJdoEntities {

    public Optional<JavaTimeLocalDateTimeJdo> find(final java.time.LocalDateTime readOnlyProperty) {
        return repositoryService.firstMatch(JavaTimeLocalDateTimeJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<JavaTimeLocalDateTimeJdo> all() {
        return repositoryService.allInstances(JavaTimeLocalDateTimeJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
