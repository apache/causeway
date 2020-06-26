package demoapp.dom.types.primitive.shorts.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class PrimitiveShortJdoEntities {

    public Optional<PrimitiveShortJdoEntity> find(final short readOnlyProperty) {
        return repositoryService.firstMatch(PrimitiveShortJdoEntity.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<PrimitiveShortJdoEntity> all() {
        return repositoryService.allInstances(PrimitiveShortJdoEntity.class);
    }

    @Inject
    RepositoryService repositoryService;

}
