package org.apache.isis.testing.fixtures.applib.fixturescripts;

import java.io.PrintStream;

public abstract class FixtureScript extends org.apache.isis.applib.fixturescripts.FixtureScript{

    public static final FixtureScript NOOP = new FixtureScript() {
        @Override
        protected void execute(final ExecutionContext executionContext) {
        }
    };

    public FixtureScript() {
    }

    public FixtureScript(String friendlyName, String localName) {
        super(friendlyName, localName);
    }

    public FixtureScript(String friendlyName, String localName, PrintStream printStream) {
        super(friendlyName, localName, printStream);
    }

    public FixtureScript(String friendlyName, String localName, Discoverability discoverability) {
        super(friendlyName, localName, discoverability);
    }

    public FixtureScript(String friendlyName, String localName, Discoverability discoverability, PrintStream printStream) {
        super(friendlyName, localName, discoverability, printStream);
    }
}
