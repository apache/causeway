package demoapp.dom.types.temporal.javasqltimestamp.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class TemporalJavaSqlTimestampJdoEntities {

    public Optional<TemporalJavaSqlTimestampJdo> find(final java.sql.Timestamp readOnlyProperty) {
        return repositoryService.firstMatch(TemporalJavaSqlTimestampJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<TemporalJavaSqlTimestampJdo> all() {
        return repositoryService.allInstances(TemporalJavaSqlTimestampJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
