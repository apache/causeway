package demoapp.dom.viewmodels.jaxbrefentity;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class ChildJdoEntities {

    public Optional<ChildJdo> find(final String name) {
        return repositoryService.firstMatch(ChildJdo.class, x -> x.getName().equals(name));
    }

    public List<ChildJdo> all() {
        return repositoryService.allInstances(ChildJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
