package demoapp.dom.annotDomain.Property.publishing;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class PropertyPublishingJdoEntities {

    public Optional<PropertyPublishingJdo> find(final String value) {
        return repositoryService.firstMatch(PropertyPublishingJdo.class, x -> Objects.equals(x.getPropertyUsingAnnotation(), value));
    }

    public List<PropertyPublishingJdo> all() {
        return repositoryService.allInstances(PropertyPublishingJdo.class);
    }

    public PropertyPublishingJdo first() {
        return all().stream().findFirst().get();
    }

    @Inject
    RepositoryService repositoryService;

}
