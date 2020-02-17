package org.apache.isis.core.config.converters;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.collections._Maps;

@Component
@Named("isisConfig.PatternsConverter")
@ConfigurationPropertiesBinding
public class PatternsConverter implements Converter<String, Map<Pattern, String>> {

    @Override
    public Map<Pattern, String> convert(String source) {
        return toPatternMap(source);
    }

    /**
     * The pattern matches definitions like:
     * <ul>
     * <li>methodNameRegex:value</li>
     * </ul>
     *
     * <p>
     *     Used for associating cssClass and cssClassFa (font awesome icon) values to method pattern names.
     * </p>
     */
    private static final Pattern PATTERN_FOR_COLON_SEPARATED_PAIR = Pattern.compile("(?<methodRegex>[^:]+):(?<value>.+)");

    private static Map<Pattern, String> toPatternMap(String cssClassPatterns) {
        final Map<Pattern,String> valueByPattern = _Maps.newLinkedHashMap();
        if(cssClassPatterns != null) {
            final StringTokenizer regexToCssClasses = new StringTokenizer(cssClassPatterns, ",");
            final Map<String,String> valueByRegex = _Maps.newLinkedHashMap();
            while (regexToCssClasses.hasMoreTokens()) {
                String regexToCssClass = regexToCssClasses.nextToken().trim();
                if (_Strings.isNullOrEmpty(regexToCssClass)) {
                    continue;
                }
                final Matcher matcher = PATTERN_FOR_COLON_SEPARATED_PAIR.matcher(regexToCssClass);
                if(matcher.matches()) {
                    valueByRegex.put(matcher.group("methodRegex"), matcher.group("value"));
                }
            }
            for (Map.Entry<String, String> entry : valueByRegex.entrySet()) {
                final String regex = entry.getKey();
                final String cssClass = entry.getValue();
                valueByPattern.put(Pattern.compile(regex), cssClass);
            }
        }
        return valueByPattern;
    }

}
