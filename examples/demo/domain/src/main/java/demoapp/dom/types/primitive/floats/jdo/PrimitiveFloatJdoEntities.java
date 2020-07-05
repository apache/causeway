package demoapp.dom.types.primitive.floats.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class PrimitiveFloatJdoEntities {

    public Optional<PrimitiveFloatJdo> find(final float readOnlyProperty) {
        return repositoryService.firstMatch(PrimitiveFloatJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<PrimitiveFloatJdo> all() {
        return repositoryService.allInstances(PrimitiveFloatJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
