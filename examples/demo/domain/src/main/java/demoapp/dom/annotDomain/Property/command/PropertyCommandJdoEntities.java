package demoapp.dom.annotDomain.Property.command;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class PropertyCommandJdoEntities {

    final RepositoryService repositoryService;

    public Optional<PropertyCommandJdo> find(final String value) {
        return repositoryService.firstMatch(PropertyCommandJdo.class, x -> Objects.equals(x.getProperty(), value));
    }

    public List<PropertyCommandJdo> all() {
        return repositoryService.allInstances(PropertyCommandJdo.class);
    }

    public PropertyCommandJdo first() {
        return all().stream().findFirst().get();
    }


}
