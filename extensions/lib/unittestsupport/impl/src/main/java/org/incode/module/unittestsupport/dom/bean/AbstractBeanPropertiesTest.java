package org.incode.module.unittestsupport.dom.bean;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.unittestsupport.jmocking.JUnitRuleMockery2;
import org.jmock.auto.Mock;
import org.junit.Rule;

public abstract class AbstractBeanPropertiesTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    protected RepositoryService mockRepositoryService;

    protected PojoTester newPojoTester() {
        final PojoTester pojoTester = PojoTester.relaxed()
			.withFixture(FixtureDatumFactoriesForJoda.dates())
            .withFixture(RepositoryService.class, mockRepositoryService)
            ;
        return pojoTester;
    }

    protected static <T> PojoTester.FixtureDatumFactory<T> pojos(Class<T> compileTimeType) {
        return FixtureDatumFactoriesForAnyPojo.pojos(compileTimeType, compileTimeType);
    }

    protected static <T> PojoTester.FixtureDatumFactory<T> pojos(Class<T> compileTimeType, Class<? extends T> runtimeType) {
        return FixtureDatumFactoriesForAnyPojo.pojos(compileTimeType, runtimeType);
    }

}
