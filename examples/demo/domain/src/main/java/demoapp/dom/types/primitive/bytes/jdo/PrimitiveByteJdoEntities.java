package demoapp.dom.types.primitive.bytes.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class PrimitiveByteJdoEntities {

    public Optional<PrimitiveByteJdoEntity> find(final byte readOnlyProperty) {
        return repositoryService.firstMatch(PrimitiveByteJdoEntity.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<PrimitiveByteJdoEntity> all() {
        return repositoryService.allInstances(PrimitiveByteJdoEntity.class);
    }

    @Inject
    RepositoryService repositoryService;

}
