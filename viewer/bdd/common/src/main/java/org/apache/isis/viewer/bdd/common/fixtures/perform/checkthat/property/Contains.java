package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.property;

import java.util.Date;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.progmodel.facets.value.DateValueFacet;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryBoundValueException;
import org.apache.isis.viewer.bdd.common.StoryCell;
import org.apache.isis.viewer.bdd.common.fixtures.DateParser;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;
import org.apache.isis.viewer.bdd.common.util.Strings;

public class Contains extends ThatSubcommandAbstract {

    public Contains() {
        super("contains", "is", "does contain");
    }

    @Override
    public ObjectAdapter that(final PerformContext performContext) throws StoryBoundValueException {

        final OneToOneAssociation otoa = (OneToOneAssociation) performContext
                .getObjectMember();

        // if we have an expected result
        CellBinding arg0Binding = performContext.getPeer().getArg0Binding();
		final StoryCell arg0Cell = arg0Binding.getCurrentCell();
		final String expected = arg0Cell.getText();

        // get
        final ObjectAdapter resultAdapter = otoa.get(performContext
                .getOnAdapter());

        // see if matches null
        if (resultAdapter == null) {
            if (Strings.emptyString(expected)) {
                return resultAdapter;
            }
            throw StoryBoundValueException.current(arg0Binding, "(is null)");
        }

        final String resultTitle = resultAdapter.titleString();

        if (!Strings.emptyString(expected)) {

            // see if expected matches an alias
            final ObjectAdapter expectedAdapter = performContext.getPeer().getAliasRegistry().getAliased(expected);
            if (expectedAdapter != null) {
                // known
                if (resultAdapter == expectedAdapter) {
                    return resultAdapter;
                }
                throw StoryBoundValueException.current(arg0Binding, resultTitle);
            }

            // otherwise, see if date and if so compare as such
            DateValueFacet dateValueFacet = resultAdapter.getSpecification().getFacet(DateValueFacet.class);
            if(dateValueFacet != null) {
                Date resultDate = dateValueFacet.dateValue(resultAdapter);
                
                DateParser dateParser = performContext.getDateParser();
                Date expectedDate = dateParser.parse(expected);
                if (expectedDate != null) {
                    if(expectedDate.compareTo(resultDate) == 0) {
                        return resultAdapter; // ok
                    } 
                }
                String format = dateParser.format(resultDate);
                throw StoryBoundValueException.current(arg0Binding, format);
            }

            
            // otherwise, compare title
            if (!Strings.nullSafeEquals(resultTitle, expected)) {
            	throw StoryBoundValueException.current(arg0Binding, resultTitle);
            }
        } else {
            // try to provide a default
            final String resultAlias = performContext.getPeer().getAliasRegistry().getAlias(resultAdapter);
            final String resultStr = resultAlias != null ? resultAlias
                    : resultTitle;
            performContext.getPeer().provideDefault(arg0Cell, resultStr);
        }

        return resultAdapter;
    }

}
