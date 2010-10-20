package org.apache.isis.extensions.bdd.common.fixtures.perform.checkthat.collections;

import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.extensions.bdd.common.StoryBoundValueException;
import org.apache.isis.extensions.bdd.common.StoryCell;
import org.apache.isis.extensions.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.metamodel.adapter.ObjectAdapter;

public class Size extends ThatAbstract {

    public Size() {
        super("size");
    }

    @Override
    protected void doThat(final PerformContext performContext,
            final Iterable<ObjectAdapter> collection) throws StoryBoundValueException {

        final CellBinding thatBinding = performContext.getPeer().getThatItBinding();
        final CellBinding arg0Binding = performContext.getPeer().getArg0Binding();

        if (!arg0Binding.isFound()) {
			throw StoryBoundValueException.current(thatBinding, 
                    "(requires argument)");
        }

        final StoryCell arg0Cell = arg0Binding.getCurrentCell();

        final String expectedSizeStr = arg0Cell.getText();
        final int expectedSize;
        try {
            expectedSize = Integer.parseInt(expectedSizeStr);
        } catch (final NumberFormatException ex) {
            throw StoryBoundValueException.current(arg0Binding, "(not an integer)");
        }

        if (expectedSize <= 0) {
            throw StoryBoundValueException.current(arg0Binding, "(not a positive integer)");
        }

        int actualSize = 0;
        for (@SuppressWarnings("unused")
        final ObjectAdapter eachObject : collection) {
            actualSize++;
        }

        if (expectedSize != actualSize) {
        	throw StoryBoundValueException.current(arg0Binding, "" + actualSize);
        }

    }

}
