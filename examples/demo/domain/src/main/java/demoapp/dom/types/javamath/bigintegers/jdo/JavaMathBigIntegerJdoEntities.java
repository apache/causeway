package demoapp.dom.types.javamath.bigintegers.jdo;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.repository.RepositoryService;

@Service
public class JavaMathBigIntegerJdoEntities {

    public Optional<JavaMathBigIntegerJdo> find(final java.math.BigInteger readOnlyProperty) {
        return repositoryService.firstMatch(JavaMathBigIntegerJdo.class, x -> x.getReadOnlyProperty() == readOnlyProperty);
    }

    public List<JavaMathBigIntegerJdo> all() {
        return repositoryService.allInstances(JavaMathBigIntegerJdo.class);
    }

    @Inject
    RepositoryService repositoryService;

}
