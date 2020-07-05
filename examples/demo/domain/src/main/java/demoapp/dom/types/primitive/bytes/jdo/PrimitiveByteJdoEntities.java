package demoapp.dom.types.primitive.bytes.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class PrimitiveByteJdoEntities {

    public Optional<PrimitiveByteJdo> find(final byte readOnlyProperty) {
        return repositoryService.firstMatch(PrimitiveByteJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<PrimitiveByteJdo> all() {
        return repositoryService.allInstances(PrimitiveByteJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
