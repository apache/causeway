package demoapp.dom.types.primitive.doubles.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class PrimitiveDoubleJdoEntities {

    public Optional<PrimitiveDoubleJdoEntity> find(final double readOnlyProperty) {
        return repositoryService.firstMatch(PrimitiveDoubleJdoEntity.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<PrimitiveDoubleJdoEntity> all() {
        return repositoryService.allInstances(PrimitiveDoubleJdoEntity.class);
    }

    @Inject
    RepositoryService repositoryService;

}
