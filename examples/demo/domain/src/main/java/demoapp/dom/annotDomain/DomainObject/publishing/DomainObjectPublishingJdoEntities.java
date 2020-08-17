package demoapp.dom.annotDomain.DomainObject.publishing;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class DomainObjectPublishingJdoEntities {

    public Optional<DomainObjectPublishingJdo> find(final String value) {
        return repositoryService.firstMatch(DomainObjectPublishingJdo.class, x -> Objects.equals(x.getProperty(), value));
    }

    public List<DomainObjectPublishingJdo> all() {
        return repositoryService.allInstances(DomainObjectPublishingJdo.class);
    }

    public DomainObjectPublishingJdo first() {
        return all().stream().findFirst().get();
    }

    @Inject
    RepositoryService repositoryService;

}
