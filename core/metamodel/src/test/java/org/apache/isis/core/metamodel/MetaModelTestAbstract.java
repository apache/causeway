package org.apache.isis.core.metamodel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.isis.core.metamodel._testing.MetaModelContext_forTesting.MetaModelContext_forTestingBuilder;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;

import lombok.Getter;
import lombok.val;

public abstract class MetaModelTestAbstract
implements HasMetaModelContext {

    @Getter(onMethod_ = {@Override})
    private MetaModelContext metaModelContext;

    @BeforeEach
    void setUp() throws Exception {
        val mmcBuilder = MetaModelContext_forTesting.builder();
        onSetUp(mmcBuilder);
        metaModelContext = mmcBuilder.build();
        afterSetUp();
    }

    @AfterEach
    void tearDown() throws Exception {
        onTearDown();
        metaModelContext.getSpecificationLoader().disposeMetaModel();
    }

    protected void onSetUp(MetaModelContext_forTestingBuilder mmcBuilder) {
    }

    protected void afterSetUp() {
    }

    protected void onTearDown() {
    }

}
