package demoapp.dom.annotDomain.Action.publishing;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class ActionPublishingJdoEntities {

    public Optional<ActionPublishingJdo> find(final String value) {
        return repositoryService.firstMatch(ActionPublishingJdo.class, x -> Objects.equals(x.getProperty(), value));
    }

    public List<ActionPublishingJdo> all() {
        return repositoryService.allInstances(ActionPublishingJdo.class);
    }

    public ActionPublishingJdo first() {
        return all().stream().findFirst().get();
    }

    @Inject
    RepositoryService repositoryService;

}
