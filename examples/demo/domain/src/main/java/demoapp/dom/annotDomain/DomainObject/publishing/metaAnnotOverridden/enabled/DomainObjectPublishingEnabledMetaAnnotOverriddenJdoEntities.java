package demoapp.dom.annotDomain.DomainObject.publishing.metaAnnotOverridden.enabled;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

import demoapp.dom.annotDomain.DomainObject.publishing.metaAnnot.enabled.DomainObjectPublishingEnabledMetaAnnotatedJdo;

@Service
public class DomainObjectPublishingEnabledMetaAnnotOverriddenJdoEntities {

    public Optional<DomainObjectPublishingEnabledMetaAnnotOverriddenJdo> find(final String value) {
        return repositoryService.firstMatch(DomainObjectPublishingEnabledMetaAnnotOverriddenJdo.class, x -> Objects.equals(x.getProperty(), value));
    }

    public List<DomainObjectPublishingEnabledMetaAnnotOverriddenJdo> all() {
        return repositoryService.allInstances(DomainObjectPublishingEnabledMetaAnnotOverriddenJdo.class);
    }

    public DomainObjectPublishingEnabledMetaAnnotOverriddenJdo first() {
        return all().stream().findFirst().get();
    }

    public DomainObjectPublishingEnabledMetaAnnotOverriddenJdo create(String newValue) {
        return repositoryService.persistAndFlush(new DomainObjectPublishingEnabledMetaAnnotOverriddenJdo(newValue));
    }

    public void remove(DomainObjectPublishingEnabledMetaAnnotOverriddenJdo enabledJdo) {
        repositoryService.removeAndFlush(enabledJdo);
    }

    @Inject
    RepositoryService repositoryService;

}
