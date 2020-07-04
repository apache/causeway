package demoapp.dom.types.isis.treenodes.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class IsisTreeNodeJdoEntities {

    public Optional<IsisTreeNodeJdo> find(final Character readOnlyProperty) {
        return repositoryService.firstMatch(IsisTreeNodeJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<IsisTreeNodeJdo> all() {
        return repositoryService.allInstances(IsisTreeNodeJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
