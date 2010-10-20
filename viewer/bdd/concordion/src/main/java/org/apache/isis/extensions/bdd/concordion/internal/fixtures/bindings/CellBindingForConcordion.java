package org.apache.isis.extensions.bdd.concordion.internal.fixtures.bindings;

import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.StoryCell;

public class CellBindingForConcordion extends CellBinding {

    public static class Builder {
        private final String name;
        private final String[] headText;
        private boolean autoCreate;
        private boolean ditto;
        private boolean optional;

        public Builder(final String name, final String... headText) {
            this.name = name;
            this.headText = headText;
        }

        public Builder autoCreate() {
            this.autoCreate = true;
            return this;
        }

        public Builder ditto() {
            this.ditto = true;
            return this;
        }

        public Builder optional() {
            this.optional = true;
            return this;
        }

        public CellBindingForConcordion build() {
            return new CellBindingForConcordion(name, autoCreate, ditto, optional, headText);
        }
    }

    public static Builder builder(final String name, final String... headText) {
        return new Builder(name, headText);
    }

    private CellBindingForConcordion(final String name, final boolean autoCreate,
            final boolean ditto, final boolean optional,
            final String[] headTexts) {
    	super(name, autoCreate, ditto, optional, headTexts);
    }

	@Override
	protected void copy(StoryCell from, StoryCell to) {
		to.setText(from.getText());
	}

}
