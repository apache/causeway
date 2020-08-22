package demoapp.dom.annotDomain.DomainObject.publishing.annotated.disabled;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

import demoapp.dom.annotDomain.DomainObject.publishing.annotated.enabled.DomainObjectPublishingEnabledJdo;

@Service
public class DomainObjectPublishingDisabledJdoEntities {

    public Optional<DomainObjectPublishingDisabledJdo> find(final String value) {
        return repositoryService.firstMatch(DomainObjectPublishingDisabledJdo.class, x -> Objects.equals(x.getProperty(), value));
    }

    public List<DomainObjectPublishingDisabledJdo> all() {
        return repositoryService.allInstances(DomainObjectPublishingDisabledJdo.class);
    }

    public DomainObjectPublishingDisabledJdo first() {
        return all().stream().findFirst().get();
    }

    public DomainObjectPublishingDisabledJdo create(String newValue) {
        return repositoryService.persistAndFlush(new DomainObjectPublishingDisabledJdo(newValue));
    }

    public void remove(DomainObjectPublishingDisabledJdo disabledJdo) {
        repositoryService.removeAndFlush(disabledJdo);
    }

    @Inject
    RepositoryService repositoryService;

}
