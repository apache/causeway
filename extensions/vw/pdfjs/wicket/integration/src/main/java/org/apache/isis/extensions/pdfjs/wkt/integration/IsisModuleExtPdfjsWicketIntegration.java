package org.apache.isis.extensions.pdfjs.wkt.integration;

import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.protocol.http.WebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.viewer.wicket.model.isis.WicketApplicationInitializer;

import lombok.val;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({

})
public class IsisModuleExtPdfjsWicketIntegration
implements WicketApplicationInitializer {

    @Override
    public void init(final WebApplication webApplication) {
        // pdf.js cmap support
        val resourceGuard =
                (SecurePackageResourceGuard) webApplication.getResourceSettings().getPackageResourceGuard();
        resourceGuard.addPattern("+*.bcmap");
    }

}
