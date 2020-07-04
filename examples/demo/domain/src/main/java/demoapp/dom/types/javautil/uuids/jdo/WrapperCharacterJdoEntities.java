package demoapp.dom.types.javautil.uuids.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class JavaUtilUuidJdoEntities {

    public Optional<JavaUtilUuidJdo> find(final Character readOnlyProperty) {
        return repositoryService.firstMatch(JavaUtilUuidJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<JavaUtilUuidJdo> all() {
        return repositoryService.allInstances(JavaUtilUuidJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
