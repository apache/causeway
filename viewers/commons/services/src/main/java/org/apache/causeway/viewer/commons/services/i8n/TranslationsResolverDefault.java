package org.apache.causeway.viewer.commons.services.i8n;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.i18n.TranslationsResolver;
import org.apache.causeway.commons.io.TextUtils;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.viewer.commons.services.CausewayModuleViewerCommonsServices;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * An implementation that reads from /WEB-INF/...
 */
@Service
@Named(CausewayModuleViewerCommonsServices.NAMESPACE + ".TranslationsResolverDefault")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class TranslationsResolverDefault implements TranslationsResolver {

    private final ServletContext servletContext;
    private final CausewayConfiguration causewayConfiguration;

    @Override
    public List<String> readLines(final String fileName) {

        final String configLocation =
                causewayConfiguration.getCore().getRuntimeServices().getTranslation().getResourceLocation();

        try {
            if(configLocation != null) {
                log.info( "Reading translations relative to config override location: {}", configLocation);

                return Files.readAllLines(newFile(configLocation, fileName), StandardCharsets.UTF_8);
            } else {
                final URL url = servletContext.getResource("/WEB-INF/" + fileName);
                return readLines(url);
            }
        } catch (final RuntimeException | IOException ignored) {
            return Collections.emptyList();
        }
    }

    static Path newFile(final String dir, final String fileName) {
        final File base = new File(dir);
        final Path path = base.toPath();
        return path.resolve(fileName);
    }

    private static final Pattern nonEmpty = Pattern.compile("^(#:|msgid|msgstr).+$");

    private static List<String> readLines(final URL url) throws IOException {
        if(url == null) {
            return Collections.emptyList();
        }

        val acceptedLines = TextUtils.readLinesFromUrl(url, StandardCharsets.UTF_8)
        .stream()
        .filter(input->input != null && nonEmpty.matcher(input).matches())
        .collect(Collectors.toList());

        return Collections.unmodifiableList(acceptedLines);
    }

}
