package demoapp.dom.types.primitive.floats.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class PrimitiveFloatJdoEntities {

    public Optional<PrimitiveFloatJdoEntity> find(final float readOnlyProperty) {
        return repositoryService.firstMatch(PrimitiveFloatJdoEntity.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<PrimitiveFloatJdoEntity> all() {
        return repositoryService.allInstances(PrimitiveFloatJdoEntity.class);
    }

    @Inject
    RepositoryService repositoryService;

}
