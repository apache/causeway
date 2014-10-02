package org.apache.isis.viewer.wicket.ui.components.widgets.themepicker;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.ActiveThemeProvider;
import de.agilecoders.wicket.core.settings.IBootstrapSettings;
import de.agilecoders.wicket.core.settings.ITheme;
import de.agilecoders.wicket.core.settings.SingleThemeProvider;
import de.agilecoders.wicket.core.util.Attributes;
import de.agilecoders.wicket.themes.markup.html.bootstrap.BootstrapThemeTheme;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchTheme;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchThemeProvider;
import de.agilecoders.wicket.themes.markup.html.vegibit.VegibitTheme;
import de.agilecoders.wicket.themes.markup.html.vegibit.VegibitThemeProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

/**
 * A panel used as a Navbar item to change the application theme/skin
 */
public class ThemePicker extends Panel {

    /**
     * Constructor
     *
     * @param id component id
     */
    public ThemePicker(String id) {
        super(id);

        final BootstrapThemeTheme bootstrapTheme = new BootstrapThemeTheme();
        List<BootswatchTheme> bootswatchThemes = Arrays.asList(BootswatchTheme.values());
        List<VegibitTheme> vegibitThemes = Arrays.asList(VegibitTheme.values());

        List<ITheme> allThemes = new ArrayList<>();
        allThemes.addAll(bootswatchThemes);
        allThemes.addAll(vegibitThemes);
        allThemes.add(bootstrapTheme);

        ListView<ITheme> themesView = new ListView<ITheme>("themes", allThemes) {
            ActiveThemeProvider activeThemeProvider = Bootstrap.getSettings().getActiveThemeProvider();

            @Override
            protected void populateItem(ListItem<ITheme> item) {
                final ITheme theme = item.getModelObject();
                if (theme.equals(activeThemeProvider.getActiveTheme())) {
                    item.add(AttributeModifier.append("class", "active"));
                }
                item.add(new AjaxLink<Void>("themeLink") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        IBootstrapSettings bootstrapSettings = Bootstrap.getSettings();
                        ActiveThemeProvider activeThemeProvider = bootstrapSettings.getActiveThemeProvider();
                        activeThemeProvider.setActiveTheme(theme);
                        if (theme instanceof BootstrapThemeTheme) {
                            bootstrapSettings.setThemeProvider(new SingleThemeProvider(theme));
                        } else if (theme instanceof BootswatchTheme) {
                            bootstrapSettings.setThemeProvider(new BootswatchThemeProvider((BootswatchTheme) theme));
                        } else if (theme instanceof VegibitTheme) {
                            bootstrapSettings.setThemeProvider(new VegibitThemeProvider((VegibitTheme) theme));
                        }
                        target.add(getPage()); // repaint the whole page
                    }
                }.setBody(Model.of(theme.name())));
            }
        };
        add(themesView);
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);

        tag.setName("li");
        Attributes.addClass(tag, "dropdown");
    }
}
