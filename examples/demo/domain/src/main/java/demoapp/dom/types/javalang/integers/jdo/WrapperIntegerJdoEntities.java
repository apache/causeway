package demoapp.dom.types.javalang.integers.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class WrapperIntegerJdoEntities {

    public Optional<WrapperIntegerJdo> find(final Integer readOnlyProperty) {
        return repositoryService.firstMatch(WrapperIntegerJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<WrapperIntegerJdo> all() {
        return repositoryService.allInstances(WrapperIntegerJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
