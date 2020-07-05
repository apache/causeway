package demoapp.dom.types.javatime.javatimelocaldate.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class JavaTimeLocalDateJdoEntities {

    public Optional<JavaTimeLocalDateJdo> find(final java.time.LocalDate readOnlyProperty) {
        return repositoryService.firstMatch(JavaTimeLocalDateJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<JavaTimeLocalDateJdo> all() {
        return repositoryService.allInstances(JavaTimeLocalDateJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
