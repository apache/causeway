package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.collections;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;

public class Size extends ThatAbstract {

    public Size() {
        super("size");
    }

    @Override
    protected void doThat(final PerformContext performContext,
            final Iterable<ObjectAdapter> collection) throws ScenarioBoundValueException {

        final CellBinding thatBinding = performContext.getPeer().getThatItBinding();
        final CellBinding arg0Binding = performContext.getPeer().getArg0Binding();

        if (!arg0Binding.isFound()) {
			throw ScenarioBoundValueException.current(thatBinding, 
                    "(requires argument)");
        }

        final ScenarioCell arg0Cell = arg0Binding.getCurrentCell();

        final String expectedSizeStr = arg0Cell.getText();
        final int expectedSize;
        try {
            expectedSize = Integer.parseInt(expectedSizeStr);
        } catch (final NumberFormatException ex) {
            throw ScenarioBoundValueException.current(arg0Binding, "(not an integer)");
        }

        if (expectedSize <= 0) {
            throw ScenarioBoundValueException.current(arg0Binding, "(not a positive integer)");
        }

        int actualSize = 0;
        for (@SuppressWarnings("unused")
        final ObjectAdapter eachObject : collection) {
            actualSize++;
        }

        if (expectedSize != actualSize) {
        	throw ScenarioBoundValueException.current(arg0Binding, "" + actualSize);
        }

    }

}
