package demoapp.dom.types.temporal.javasqldate.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class TemporalJavaSqlDateJdoEntities {

    public Optional<TemporalJavaSqlDateJdo> find(final java.sql.Date readOnlyProperty) {
        return repositoryService.firstMatch(TemporalJavaSqlDateJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<TemporalJavaSqlDateJdo> all() {
        return repositoryService.allInstances(TemporalJavaSqlDateJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
