package demoapp.dom.types.primitive.chars.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class PrimitiveCharJdoEntities {

    public Optional<PrimitiveCharJdoEntity> find(final char readOnlyProperty) {
        return repositoryService.firstMatch(PrimitiveCharJdoEntity.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<PrimitiveCharJdoEntity> all() {
        return repositoryService.allInstances(PrimitiveCharJdoEntity.class);
    }

    @Inject
    RepositoryService repositoryService;

}
