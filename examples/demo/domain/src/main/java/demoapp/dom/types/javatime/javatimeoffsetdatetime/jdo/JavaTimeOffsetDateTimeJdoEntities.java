package demoapp.dom.types.javatime.javatimeoffsetdatetime.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class JavaTimeOffsetDateTimeJdoEntities {

    public Optional<JavaTimeOffsetDateTimeJdo> find(final java.time.OffsetDateTime readOnlyProperty) {
        return repositoryService.firstMatch(JavaTimeOffsetDateTimeJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<JavaTimeOffsetDateTimeJdo> all() {
        return repositoryService.allInstances(JavaTimeOffsetDateTimeJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
