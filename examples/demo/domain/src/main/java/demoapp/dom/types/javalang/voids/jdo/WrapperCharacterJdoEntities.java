package demoapp.dom.types.javalang.voids.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class JavaLangVoidJdoEntities {

    public Optional<JavaLangVoidJdo> find(final Character readOnlyProperty) {
        return repositoryService.firstMatch(JavaLangVoidJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<JavaLangVoidJdo> all() {
        return repositoryService.allInstances(JavaLangVoidJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
