package demoapp.dom.annotDomain.DomainObject.publishing;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class DomainObjectPublishingEnabledJdoEntities {

    public Optional<DomainObjectPublishingEnabledJdo> find(final String value) {
        return repositoryService.firstMatch(DomainObjectPublishingEnabledJdo.class, x -> Objects.equals(x.getProperty(), value));
    }

    public List<DomainObjectPublishingEnabledJdo> all() {
        return repositoryService.allInstances(DomainObjectPublishingEnabledJdo.class);
    }

    public DomainObjectPublishingEnabledJdo first() {
        return all().stream().findFirst().get();
    }

    public DomainObjectPublishingEnabledJdo cloneAndPersist(DomainObjectPublishingEnabledJdo original) {
        return repositoryService.persistAndFlush(
                new DomainObjectPublishingEnabledJdo(original.getProperty()));
    }

    public void remove(DomainObjectPublishingEnabledJdo enabledJdo) {
        repositoryService.removeAndFlush(enabledJdo);
    }

    @Inject
    RepositoryService repositoryService;

}
