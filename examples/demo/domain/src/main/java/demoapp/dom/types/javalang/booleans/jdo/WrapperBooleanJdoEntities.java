package demoapp.dom.types.javalang.booleans.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class WrapperBooleanJdoEntities {

    public Optional<WrapperBooleanJdo> find(final Boolean readOnlyProperty) {
        return repositoryService.firstMatch(WrapperBooleanJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<WrapperBooleanJdo> all() {
        return repositoryService.allInstances(WrapperBooleanJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
