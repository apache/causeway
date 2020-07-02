package demoapp.dom.types.primitive.ints.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class PrimitiveIntJdoEntities {

    public Optional<PrimitiveIntJdo> find(final int readOnlyProperty) {
        return repositoryService.firstMatch(PrimitiveIntJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<PrimitiveIntJdo> all() {
        return repositoryService.allInstances(PrimitiveIntJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
