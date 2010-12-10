package net.sf.isiscontrib.bdd.fitnesse.internal;

import java.util.Map;

import net.sf.isiscontrib.bdd.fitnesse.internal.fixtures.perform.ScenarioCellForFitNesse;

import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioCell;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import fit.Fixture;
import fit.Parse;

public class CellBindingForFitNesse extends CellBinding {

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

        public CellBindingForFitNesse build() {
            return new CellBindingForFitNesse(name, autoCreate, ditto, optional, headText);
        }
    }

    public static Builder builder(final String name, final String... headText) {
        return new Builder(name, headText);
    }

    private CellBindingForFitNesse(final String name, final boolean autoCreate, final boolean ditto,
        final boolean optional, final String[] headTexts) {
        super(name, autoCreate, ditto, optional, headTexts);
    }

    /**
     * Finds the heading text, optionally creating it if not found and {@link Builder#autoCreate() created as an auto}
     * binding.
     * 
     * @param heads
     */
    public void find(final Parse heads) {
        Map<Integer, ScenarioCell> storyValuesMap = toMap(heads);
        if (!locate(heads) && isAutoCreate()) {
            // Append an alias cell to header
            final int size = storyValuesMap.size();
            final Parse aliasCell = new Parse("td", Fixture.gray(getHeadTexts().get(0)), null, null);
            heads.last().more = aliasCell;
            createHeadCell(size, new ScenarioCellForFitNesse(aliasCell));
        }
    }

    @Override
    protected void copy(final ScenarioCell from, ScenarioCell to) {
        to.setText(Fixture.gray(from.getText()));
    }

    private boolean locate(final Parse heads) {
        Map<Integer, ScenarioCell> headStoryValuesMap = toMap(heads);
        return locate(headStoryValuesMap);
    }

    private boolean locate(Map<Integer, ScenarioCell> headStoryValuesMap) {
        for (Integer colNum : headStoryValuesMap.keySet()) {
            ScenarioCell storyValue = headStoryValuesMap.get(colNum);
            final String headText = storyValue.getText();

            if (matches(headText)) {
                setHeadColumn(colNum, storyValue);
                break;
            }
        }

        return isFound();
    }

    public static Map<Integer, ScenarioCell> toMap(final Parse firstParse) {
        Parse eachParse = firstParse;
        Map<Integer, Parse> parseMap = Maps.newLinkedHashMap();
        for (int i = 0; eachParse != null; i++, eachParse = eachParse.more) {
            parseMap.put(i, eachParse);
        }
        Map<Integer, ScenarioCell> storyValuesMap = Maps.transformValues(parseMap, new Function<Parse, ScenarioCell>() {
            @Override
            public ScenarioCell apply(Parse from) {
                return new ScenarioCellForFitNesse(from);
            }
        });
        return storyValuesMap;
    }

}
