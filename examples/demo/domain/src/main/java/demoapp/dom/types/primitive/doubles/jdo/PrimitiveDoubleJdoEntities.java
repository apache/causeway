package demoapp.dom.types.primitive.doubles.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class PrimitiveDoubleJdoEntities {

    public Optional<PrimitiveDoubleJdo> find(final double readOnlyProperty) {
        return repositoryService.firstMatch(PrimitiveDoubleJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<PrimitiveDoubleJdo> all() {
        return repositoryService.allInstances(PrimitiveDoubleJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
