package demoapp.dom.types.isis.blobs.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Blob;

@Service
public class IsisBlobJdoEntities {

    public Optional<IsisBlobJdo> find(final Blob readOnlyProperty) {
        return repositoryService.firstMatch(IsisBlobJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<IsisBlobJdo> all() {
        return repositoryService.allInstances(IsisBlobJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
