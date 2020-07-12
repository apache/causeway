package demoapp.dom.types.javatime.javatimeoffsettime.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class JavaTimeOffsetTimeJdoEntities {

    public Optional<JavaTimeOffsetTimeJdo> find(final java.time.OffsetTime readOnlyProperty) {
        return repositoryService.firstMatch(JavaTimeOffsetTimeJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<JavaTimeOffsetTimeJdo> all() {
        return repositoryService.allInstances(JavaTimeOffsetTimeJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
