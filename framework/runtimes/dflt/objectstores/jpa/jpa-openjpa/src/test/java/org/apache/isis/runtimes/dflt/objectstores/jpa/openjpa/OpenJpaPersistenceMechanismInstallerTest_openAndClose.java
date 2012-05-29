package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

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
        .with(new JpaProgrammingModelFacets())
        .with(new JpaMetaModelValidator())
        .with(new OpenJpaPersistenceMechanismInstaller())
        .withServices(repo)
        .build();

    @Before
    public void setUp() throws Exception {
        
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
