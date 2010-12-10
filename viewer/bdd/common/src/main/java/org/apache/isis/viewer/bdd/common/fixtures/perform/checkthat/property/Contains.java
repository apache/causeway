package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.property;

import java.util.Date;

import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.progmodel.facets.value.DateValueFacet;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
import org.apache.isis.viewer.bdd.common.fixtures.DateParser;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;

public class Contains extends ThatSubcommandAbstract {

    public Contains() {
        super("contains", "is", "does contain");
    }

    @Override
    public ObjectAdapter that(final PerformContext performContext) throws ScenarioBoundValueException {

        final OneToOneAssociation otoa = (OneToOneAssociation) performContext
                .getObjectMember();

        // if we have an expected result
        CellBinding arg0Binding = performContext.getPeer().getArg0Binding();
		final ScenarioCell arg0Cell = arg0Binding.getCurrentCell();
		final String expected = arg0Cell.getText();

        // get
        final ObjectAdapter resultAdapter = otoa.get(performContext
                .getOnAdapter());

        // see if matches null
        if (resultAdapter == null) {
            if (StringUtils.emptyString(expected)) {
                return resultAdapter;
            }
            throw ScenarioBoundValueException.current(arg0Binding, "(is null)");
        }

        final String resultTitle = resultAdapter.titleString();

        if (!StringUtils.emptyString(expected)) {

            // see if expected matches an alias
            final ObjectAdapter expectedAdapter = performContext.getPeer().getAliasRegistry().getAliased(expected);
            if (expectedAdapter != null) {
                // known
                if (resultAdapter == expectedAdapter) {
                    return resultAdapter;
                }
                throw ScenarioBoundValueException.current(arg0Binding, resultTitle);
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
                throw ScenarioBoundValueException.current(arg0Binding, format);
            }

            
            // otherwise, compare title
            if (!StringUtils.nullSafeEquals(resultTitle, expected)) {
            	throw ScenarioBoundValueException.current(arg0Binding, resultTitle);
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
