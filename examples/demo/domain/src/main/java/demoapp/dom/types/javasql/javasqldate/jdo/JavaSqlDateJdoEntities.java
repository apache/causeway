package demoapp.dom.types.javasql.javasqldate.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class JavaSqlDateJdoEntities {

    public Optional<JavaSqlDateJdo> find(final java.sql.Date readOnlyProperty) {
        return repositoryService.firstMatch(JavaSqlDateJdo.class, x -> x.getReadOnlyProperty().equals(readOnlyProperty));
    }

    public List<JavaSqlDateJdo> all() {
        return repositoryService.allInstances(JavaSqlDateJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
