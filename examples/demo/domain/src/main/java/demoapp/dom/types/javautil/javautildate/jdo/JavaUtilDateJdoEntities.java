package demoapp.dom.types.javautil.javautildate.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class JavaUtilDateJdoEntities {

    public Optional<JavaUtilDateJdo> find(final java.util.Date readOnlyProperty) {
        return repositoryService.firstMatch(JavaUtilDateJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<JavaUtilDateJdo> all() {
        return repositoryService.allInstances(JavaUtilDateJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
