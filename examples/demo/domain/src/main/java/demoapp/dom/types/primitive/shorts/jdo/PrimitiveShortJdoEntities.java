package demoapp.dom.types.primitive.shorts.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class PrimitiveShortJdoEntities {

    public Optional<PrimitiveShortJdo> find(final short readOnlyProperty) {
        return repositoryService.firstMatch(PrimitiveShortJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<PrimitiveShortJdo> all() {
        return repositoryService.allInstances(PrimitiveShortJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
