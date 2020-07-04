package demoapp.dom.types.javanet.urls.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class JavaNetUrlJdoEntities {

    public Optional<JavaNetUrlJdo> find(final Character readOnlyProperty) {
        return repositoryService.firstMatch(JavaNetUrlJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<JavaNetUrlJdo> all() {
        return repositoryService.allInstances(JavaNetUrlJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
