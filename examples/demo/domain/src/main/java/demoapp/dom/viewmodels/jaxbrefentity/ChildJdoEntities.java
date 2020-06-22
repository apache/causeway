package demoapp.dom.viewmodels.jaxbrefentity;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class ChildJdoEntities {

    public Optional<ChildJdoEntity> find(final String name) {
        return repositoryService.firstMatch(ChildJdoEntity.class, x -> x.getName().equals(name));
    }

    public List<ChildJdoEntity> all() {
        return repositoryService.allInstances(ChildJdoEntity.class);
    }

    @Inject
    RepositoryService repositoryService;

}
