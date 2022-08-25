package org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.wicket.util.string.Strings;
import org.springframework.lang.Nullable;
import org.wicketstuff.select2.ChoiceProvider;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.placeholder.PlaceholderRenderService;
import org.apache.isis.applib.services.placeholder.PlaceholderRenderService.PlaceholderLiteral;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.config.IsisConfiguration.Viewer.Wicket;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMementoForEmpty;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext.HasCommonContext;

import lombok.val;

public abstract class ChoiceProviderAbstract
extends ChoiceProvider<ObjectMemento>
implements HasCommonContext {
    private static final long serialVersionUID = 1L;

    /** arbitrary string */
    private static final String NULL_ID = "VGN6r6zKTiLhUsA0WkdQ17LvMU1IYdb0";

    /**
     * Whether to not prepend <code>null</code> as choice candidate.
     */
    protected abstract boolean isRequired();

    /**
     * Get choice candidates with filtering (don't include <code>null</code>).
     */
    protected abstract Can<ObjectMemento> query(@Nullable String term);

    protected abstract @Nullable ObjectMemento mementoFromId(final String id);

    @Override
    public final String getDisplayValue(final ObjectMemento choiceMemento) {
        if (choiceMemento == null
                || choiceMemento instanceof ObjectMementoForEmpty) {
            return getPlaceholderRenderService().asText(PlaceholderLiteral.NULL_REPRESENTATION);
        }
        return translate(choiceMemento.getTitle());
    }

    @Override
    public final String getIdValue(final ObjectMemento choiceMemento) {
        if (choiceMemento == null) {
            return NULL_ID;
        }
        return choiceMemento.bookmark().stringify();
    }

    @Override
    public final void query(
            final String term,
            final int page,
            final org.wicketstuff.select2.Response<ObjectMemento> response) {

        val mementosFiltered = query(term);

        if(isRequired()) {
            response.addAll(mementosFiltered.toList());
            return;
        }

        // else, if not mandatory, prepend null
        val mementosIncludingNull = mementosFiltered.toArrayList();
        mementosIncludingNull.add(0, null);

        response.addAll(mementosIncludingNull);
    }

    @Override // not final, as Value Choices override this, because they don't use idToMemento(..)
    public /*final*/ Collection<ObjectMemento> toChoices(final Collection<String> ids) {
        return _NullSafe.stream(ids)
                .map(this::mementoFromIdWithNullHandling)
                .collect(Collectors.toList());
    }

    // -- UTILITY

    /**
     * Filters all choices against a term by using their
     * {@link ManagedObject#titleString() title string}
     *
     * @param term The term entered by the user
     * @param choiceMementos The collections of choices to filter
     * @return A list of all matching choices
     */
    protected final Can<ObjectMemento> filter(
            final String term,
            final Can<ObjectMemento> choiceMementos) {

        if (Strings.isEmpty(term)) {
            return choiceMementos;
        }

        val translationContext = TranslationContext.empty();
        val translator = getCommonContext().getTranslationService();
        val termLower = term.toLowerCase();

        return choiceMementos.filter((final ObjectMemento candidateMemento)->{
            val title = translator.translate(translationContext, candidateMemento.getTitle());
            return title.toLowerCase().contains(termLower);
        });

    }

    // -- HELPER

    private @Nullable ObjectMemento mementoFromIdWithNullHandling(final String id) {
        if(NULL_ID.equals(id)) {
            return null;
        }
        return mementoFromId(id);
    }

    // -- DEPENDENCIES

    protected final Wicket getSettings() {
        return getCommonContext().getConfiguration().getViewer().getWicket();
    }

    /**
     * Translate without context: Tooltips, Button-Labels, etc.
     */
    protected String translate(final String input) {
        return getCommonContext().getTranslationService().translate(TranslationContext.empty(), input);
    }

    protected PlaceholderRenderService getPlaceholderRenderService() {
        return getCommonContext().getPlaceholderRenderService();
    }

}
