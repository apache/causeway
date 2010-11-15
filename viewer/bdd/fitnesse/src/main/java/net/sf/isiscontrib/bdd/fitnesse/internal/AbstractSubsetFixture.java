package net.sf.isiscontrib.bdd.fitnesse.internal;

import org.apache.isis.viewer.bdd.common.AliasRegistry;

import fitlibrary.SubsetFixture;

public abstract class AbstractSubsetFixture extends SubsetFixture {

    private final AliasRegistry aliasesRegistry;

    public AbstractSubsetFixture(AliasRegistry aliasesRegistry, Object actuals) {
        super(actuals);
        this.aliasesRegistry = aliasesRegistry;
    }

    public AliasRegistry getAliasesRegistry() {
        return aliasesRegistry;
    }

}
