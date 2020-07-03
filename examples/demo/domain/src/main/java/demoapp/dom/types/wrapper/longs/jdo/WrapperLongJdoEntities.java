package demoapp.dom.types.wrapper.longs.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class WrapperLongJdoEntities {

    public Optional<WrapperLongJdo> find(final Long readOnlyProperty) {
        return repositoryService.firstMatch(WrapperLongJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<WrapperLongJdo> all() {
        return repositoryService.allInstances(WrapperLongJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
