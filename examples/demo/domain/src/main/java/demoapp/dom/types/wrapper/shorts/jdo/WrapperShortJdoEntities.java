package demoapp.dom.types.wrapper.shorts.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class WrapperShortJdoEntities {

    public Optional<WrapperShortJdo> find(final Short readOnlyProperty) {
        return repositoryService.firstMatch(WrapperShortJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<WrapperShortJdo> all() {
        return repositoryService.allInstances(WrapperShortJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
