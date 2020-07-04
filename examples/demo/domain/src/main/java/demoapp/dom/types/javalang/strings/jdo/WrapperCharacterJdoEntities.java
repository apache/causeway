package demoapp.dom.types.javalang.strings.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class JavaLangStringJdoEntities {

    public Optional<JavaLangStringJdo> find(final Character readOnlyProperty) {
        return repositoryService.firstMatch(JavaLangStringJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<JavaLangStringJdo> all() {
        return repositoryService.allInstances(JavaLangStringJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
