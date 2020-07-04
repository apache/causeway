package demoapp.dom.types.javasql.javasqltimestamp.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class JavaSqlTimestampJdoEntities {

    public Optional<JavaSqlTimestampJdo> find(final java.sql.Timestamp readOnlyProperty) {
        return repositoryService.firstMatch(JavaSqlTimestampJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<JavaSqlTimestampJdo> all() {
        return repositoryService.allInstances(JavaSqlTimestampJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
