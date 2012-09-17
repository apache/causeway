package org.apache.isis.viewer.wicket.ui.util;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public final class Links {

    public static <T extends Page> AbstractLink newSubmitLink(final String linkId, final PageParameters pageParameters, final Class<T> pageClass) {
        return new SubmitLink(linkId) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit() {
                getForm().setResponsePage(pageClass, pageParameters);
                super.onSubmit();
            }
        };
    }

    public static <T extends Page> AbstractLink newAbstractLink(final String linkId, final PageParameters pageParameters, final Class<T> pageClass) {

      return new Link<T>(linkId) {
          private static final long serialVersionUID = 1L;
          @Override
          public void onClick() {
              this.setResponsePage(pageClass, pageParameters);
          }
          
      };
    }

    public static <T extends Page> AbstractLink newBookmarkablePageLink(final String linkId, final PageParameters pageParameters, final Class<T> pageClass) {
        return new BookmarkablePageLink<T>(linkId, pageClass, pageParameters);
    }

}
