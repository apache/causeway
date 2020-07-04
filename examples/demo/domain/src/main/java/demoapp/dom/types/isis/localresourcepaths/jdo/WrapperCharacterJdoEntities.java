package demoapp.dom.types.isis.localresourcepaths.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class IsisLocalResourcePathJdoEntities {

    public Optional<IsisLocalResourcePathJdo> find(final Character readOnlyProperty) {
        return repositoryService.firstMatch(IsisLocalResourcePathJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<IsisLocalResourcePathJdo> all() {
        return repositoryService.allInstances(IsisLocalResourcePathJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
