package demoapp.dom.types.primitive.chars.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class PrimitiveCharJdoEntities {

    public Optional<PrimitiveCharJdo> find(final char readOnlyProperty) {
        return repositoryService.firstMatch(PrimitiveCharJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<PrimitiveCharJdo> all() {
        return repositoryService.allInstances(PrimitiveCharJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
