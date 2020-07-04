package demoapp.dom.types.isis.markups.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class IsisMarkupJdoEntities {

    public Optional<IsisMarkupJdo> find(final Character readOnlyProperty) {
        return repositoryService.firstMatch(IsisMarkupJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<IsisMarkupJdo> all() {
        return repositoryService.allInstances(IsisMarkupJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
