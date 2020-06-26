package demoapp.dom.types.primitive.ints.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class PrimitiveIntJdoEntities {

    public Optional<PrimitiveIntJdoEntity> find(final int readOnlyProperty) {
        return repositoryService.firstMatch(PrimitiveIntJdoEntity.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<PrimitiveIntJdoEntity> all() {
        return repositoryService.allInstances(PrimitiveIntJdoEntity.class);
    }

    @Inject
    RepositoryService repositoryService;

}
