package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.extensions.jpa.metamodel.specloader.progmodelfacets.JpaProgrammingModelFacets;
import org.apache.isis.extensions.jpa.metamodel.specloader.validator.JpaMetaModelValidator;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.fixtures.JpaPrimitiveValuedEntity;
import org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.fixtures.JpaPrimitiveValuedEntityRepository;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;

public class OpenJpaPersistenceMechanismInstallerTest_openAndClose {

    private JpaPrimitiveValuedEntityRepository repo = new JpaPrimitiveValuedEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = IsisSystemWithFixtures.builder()
        .with(configuration())
        .with(new JpaProgrammingModelFacets())
        .with(new JpaMetaModelValidator())
        .with(new OpenJpaPersistenceMechanismInstaller())
        .withServices(repo)
        .with(hsqldbListener())
        .build()
        ;

    private static IsisSystemWithFixtures.Listener hsqldbListener() {
        return new IsisSystemWithFixtures.ListenerAdapter(){

            @Override
            public void postSetupSystem(boolean firstTime) throws Exception {
                final OpenJpaObjectStore objectStore = (OpenJpaObjectStore) IsisContext.getPersistenceSession().getObjectStore();
                Connection connection = objectStore.getConnection();
                Statement statement = connection.createStatement();
                statement.executeUpdate("DELETE FROM JPAPRIMITIVEVALUEDENTITY");
            }
        };
    }

    private static IsisConfiguration configuration() {
        final IsisConfigurationDefault configuration = new IsisConfigurationDefault();
        Properties props = new Properties();
        props.put("isis.persistor.openjpa.impl.openjpa.jdbc.SynchronizeMappings", "buildSchema");
        props.put("isis.persistor.openjpa.impl.openjpa.ConnectionURL", "jdbc:hsqldb:mem:test");
        props.put("isis.persistor.openjpa.impl.openjpa.ConnectionDriverName", "org.hsqldb.jdbcDriver");
        props.put("isis.persistor.openjpa.impl.openjpa.ConnectionUserName", "sa");
        props.put("isis.persistor.openjpa.impl.openjpa.ConnectionPassword", "");
        props.put("isis.persistor.openjpa.impl.openjpa.Log", "DefaultLevel=ERROR, Tool=INFO");
        props.put("isis.persistor.openjpa.impl.openjpa.RuntimeUnenhancedClasses", "supported"); // in production, should always pre-enhance using the maven openjpa plugin

        configuration.add(props);
        return configuration;
    }

    @Test
    public void servicesBootstrapped() {
        final List<Object> services = IsisContext.getServices();
        assertThat(services.size(), is(1));
        assertThat(services.get(0), is((Object)repo));
        
        final ObjectAdapter serviceAdapter = IsisContext.getPersistenceSession().getAdapterManager().getAdapterFor(repo);
        assertThat(serviceAdapter, is(not(nullValue())));
        
        assertThat(serviceAdapter.getOid(), is(equalTo((Oid)RootOidDefault.create(ObjectSpecId.of("JpaPrimitiveValuedEntities"), "1"))));
    }
    
    @Test
    public void beginTranAndThenCommit() {
        iswf.beginTran();
        iswf.commitTran();
    }

    @Test
    public void emptyList() {
        iswf.beginTran();
        final List<JpaPrimitiveValuedEntity> list = repo.list();
        assertThat(list.size(), is(0));
        iswf.commitTran();
    }

    @Test
    public void persistThenRetrieve() throws Exception {
        iswf.beginTran();
        final JpaPrimitiveValuedEntity entity = repo.newEntity();
        iswf.commitTran();
        
        iswf.bounceSystem();
        
        iswf.beginTran();
        final List<JpaPrimitiveValuedEntity> list = repo.list();
        assertThat(list.size(), is(1));
        iswf.commitTran();
    }

}
