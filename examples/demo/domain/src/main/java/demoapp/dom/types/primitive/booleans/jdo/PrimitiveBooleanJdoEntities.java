package demoapp.dom.types.primitive.booleans.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class PrimitiveBooleanJdoEntities {

    public Optional<PrimitiveBooleanJdo> find(final boolean readOnlyProperty) {
        return repositoryService.firstMatch(PrimitiveBooleanJdo.class, x -> x.isReadOnlyProperty() == readOnlyProperty);
    }

    public List<PrimitiveBooleanJdo> all() {
        return repositoryService.allInstances(PrimitiveBooleanJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
