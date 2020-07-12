package demoapp.dom.types.javatime.javatimezoneddatetime.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class JavaTimeZonedDateTimeJdoEntities {

    public Optional<JavaTimeZonedDateTimeJdo> find(final java.time.ZonedDateTime readOnlyProperty) {
        return repositoryService.firstMatch(JavaTimeZonedDateTimeJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<JavaTimeZonedDateTimeJdo> all() {
        return repositoryService.allInstances(JavaTimeZonedDateTimeJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
