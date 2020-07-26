package demoapp.dom.annotDomain.Property.domainEvent;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

import demoapp.dom.types.javalang.strings.jdo.JavaLangStringJdo;

@Service
public class PropertyDomainEventJdoEntities {

    public Optional<PropertyDomainEventJdo> find(final String readOnlyProperty) {
        return repositoryService.firstMatch(PropertyDomainEventJdo.class, x -> x.getText() == readOnlyProperty);
    }

    public List<PropertyDomainEventJdo> all() {
        return repositoryService.allInstances(PropertyDomainEventJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
