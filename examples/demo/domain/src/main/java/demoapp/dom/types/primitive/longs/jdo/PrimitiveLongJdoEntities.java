package demoapp.dom.types.primitive.longs.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class PrimitiveLongJdoEntities {

    public Optional<PrimitiveLongJdoEntity> find(final long readOnlyProperty) {
        return repositoryService.firstMatch(PrimitiveLongJdoEntity.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<PrimitiveLongJdoEntity> all() {
        return repositoryService.allInstances(PrimitiveLongJdoEntity.class);
    }

    @Inject
    RepositoryService repositoryService;

}
