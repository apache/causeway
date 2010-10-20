package org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.isis.extensions.bdd.common.fixtures.perform.PerformOwner;

public abstract class ThatSubcommandAbstract implements ThatSubcommand {

    private final List<String> subkeys;
    private PerformOwner owner;

    public ThatSubcommandAbstract(final String... subkeys) {
        this.subkeys = Collections.unmodifiableList(Arrays.asList(subkeys));
    }

    public List<String> getSubkeys() {
        return subkeys;
    }

    protected PerformOwner getOwner() {
        return owner;
    }

    /**
     * Injected.
     */
    public void setOwner(final PerformOwner owner) {
        this.owner = owner;
    }

}
