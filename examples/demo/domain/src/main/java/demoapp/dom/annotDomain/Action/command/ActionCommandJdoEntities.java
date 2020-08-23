package demoapp.dom.annotDomain.Action.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class ActionCommandJdoEntities {

    public Optional<ActionCommandJdo> find(final String value) {
        return repositoryService.firstMatch(ActionCommandJdo.class, x -> Objects.equals(x.getProperty(), value));
    }

    public List<ActionCommandJdo> all() {
        return repositoryService.allInstances(ActionCommandJdo.class);
    }

    public ActionCommandJdo first() {
        return all().stream().findFirst().get();
    }

    @Inject
    RepositoryService repositoryService;

}
