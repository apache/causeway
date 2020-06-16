package demoapp.dom.viewmodels.jaxbrefentity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import demoapp.dom.events.EventLogEntry;

@Service
public class ChildEntities {

    public Optional<ChildEntity> find(final String name) {
        return repositoryService.firstMatch(ChildEntity.class, x -> x.getName().equals(name));
    }

    public List<ChildEntity> all(final String name) {
        return repositoryService.allInstances(ChildEntity.class);
    }

    @Inject
    RepositoryService repositoryService;

}
