package demoapp.dom.types.primitive.longs.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class PrimitiveLongJdoEntities {

    public Optional<PrimitiveLongJdo> find(final long readOnlyProperty) {
        return repositoryService.firstMatch(PrimitiveLongJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<PrimitiveLongJdo> all() {
        return repositoryService.allInstances(PrimitiveLongJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
