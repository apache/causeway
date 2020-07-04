package demoapp.dom.types.isis.blobs.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class IsisBlobJdoEntities {

    public Optional<IsisBlobJdo> find(final Character readOnlyProperty) {
        return repositoryService.firstMatch(IsisBlobJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<IsisBlobJdo> all() {
        return repositoryService.allInstances(IsisBlobJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
