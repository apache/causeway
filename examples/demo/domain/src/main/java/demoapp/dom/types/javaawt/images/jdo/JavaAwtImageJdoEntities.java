package demoapp.dom.types.javaawt.images.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Image;

@Service
public class JavaAwtImageJdoEntities {

    public Optional<JavaAwtImageJdo> find(final java.awt.Image readOnlyProperty) {
        return repositoryService.firstMatch(JavaAwtImageJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<JavaAwtImageJdo> all() {
        return repositoryService.allInstances(JavaAwtImageJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
