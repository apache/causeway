package demoapp.dom.types.javalang.floats.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class WrapperFloatJdoEntities {

    public Optional<WrapperFloatJdo> find(final Float readOnlyProperty) {
        return repositoryService.firstMatch(WrapperFloatJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<WrapperFloatJdo> all() {
        return repositoryService.allInstances(WrapperFloatJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
