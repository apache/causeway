package org.apache.isis.viewer.bdd.common;


public class CellBindingDefault extends CellBinding {

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

        public CellBindingDefault build() {
            return new CellBindingDefault(name, autoCreate, ditto, optional, headText);
        }
    }

    public static Builder builder(final String name, final String... headText) {
        return new Builder(name, headText);
    }

    private CellBindingDefault(final String name, final boolean autoCreate,
            final boolean ditto, final boolean optional,
            final String[] headTexts) {
    	super(name, autoCreate, ditto, optional, headTexts);
    }

	@Override
	protected void copy(ScenarioCell from, ScenarioCell to) {
		to.setText(from.getText());
	}

}
