package demoapp.dom.annotDomain.DomainObject.publishing.metaAnnot.enabled;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class DomainObjectPublishingEnabledMetaAnnotatedJdoEntities {

    public Optional<DomainObjectPublishingEnabledMetaAnnotatedJdo> find(final String value) {
        return repositoryService.firstMatch(DomainObjectPublishingEnabledMetaAnnotatedJdo.class, x -> Objects.equals(x.getProperty(), value));
    }

    public List<DomainObjectPublishingEnabledMetaAnnotatedJdo> all() {
        return repositoryService.allInstances(DomainObjectPublishingEnabledMetaAnnotatedJdo.class);
    }

    public Optional<DomainObjectPublishingEnabledMetaAnnotatedJdo> first() {
        return all().stream().findFirst();
    }

    public DomainObjectPublishingEnabledMetaAnnotatedJdo create(String newValue) {
        return repositoryService.persistAndFlush(new DomainObjectPublishingEnabledMetaAnnotatedJdo(newValue));
    }

    public void remove(DomainObjectPublishingEnabledMetaAnnotatedJdo enabledJdo) {
        repositoryService.removeAndFlush(enabledJdo);
    }

    @Inject
    RepositoryService repositoryService;

}
