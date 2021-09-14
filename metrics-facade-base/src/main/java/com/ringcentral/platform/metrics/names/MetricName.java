package com.ringcentral.platform.metrics.names;

import com.ringcentral.platform.metrics.MetricKey;

import java.util.*;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

public class MetricName implements MetricKey, Iterable<String> {

    private final List<String> parts;
    private final int hashCode;

    /* ****************************** */

    public static MetricName fromDotSeparated(String name) {
        return metricName(name.split("\\.", -1));
    }

    /* ****************************** */

    public static MetricName of(String part) {
        return metricName(part);
    }

    public static MetricName withName(String part) {
        return metricName(part);
    }

    public static MetricName name(String part) {
        return metricName(part);
    }

    public static MetricName metricName(String part) {
        return new MetricName(List.of(part));
    }

    /* ****************************** */

    public static MetricName of(String... parts) {
        return metricName(parts);
    }

    public static MetricName withName(String... parts) {
        return metricName(parts);
    }

    public static MetricName name(String... parts) {
        return metricName(parts);
    }

    public static MetricName metricName(String... parts) {
        return new MetricName(parts.length > 0 ? List.of(parts) : emptyList());
    }

    /* ****************************** */

    public static MetricName of(MetricName prefix, String suffix) {
        return metricName(prefix, suffix);
    }

    public static MetricName withName(MetricName prefix, String suffix) {
        return metricName(prefix, suffix);
    }

    public static MetricName name(MetricName prefix, String suffix) {
        return metricName(prefix, suffix);
    }

    public static MetricName metricName(MetricName prefix, String suffix) {
        if (prefix.isEmpty()) {
            return metricName(suffix);
        }

        String[] parts = prefix.parts.toArray(new String[prefix.size() + 1]);
        parts[prefix.size()] = suffix;
        return new MetricName(asList(parts));
    }

    /* ****************************** */

    public static MetricName of(MetricName prefix, String... suffix) {
        return metricName(prefix, suffix);
    }

    public static MetricName withName(MetricName prefix, String... suffix) {
        return metricName(prefix, suffix);
    }

    public static MetricName name(MetricName prefix, String... suffix) {
        return metricName(prefix, suffix);
    }

    public static MetricName metricName(MetricName prefix, String... suffix) {
        if (suffix.length == 0) {
            return prefix;
        }

        if (prefix.isEmpty()) {
            return metricName(suffix);
        }

        String[] parts = prefix.parts.toArray(new String[prefix.size() + suffix.length]);

        for (int i = 0; i < suffix.length; ++i) {
            parts[prefix.size() + i] = suffix[i];
        }

        return new MetricName(asList(parts));
    }

    /* ****************************** */

    public static MetricName of(MetricName prefix, MetricName suffix) {
        return metricName(prefix, suffix);
    }

    public static MetricName withName(MetricName prefix, MetricName suffix) {
        return metricName(prefix, suffix);
    }

    public static MetricName name(MetricName prefix, MetricName suffix) {
        return metricName(prefix, suffix);
    }

    public static MetricName metricName(MetricName prefix, MetricName suffix) {
        if (prefix.isEmpty()) {
            return suffix;
        }

        if (suffix.isEmpty()) {
            return prefix;
        }

        String[] parts = prefix.parts.toArray(new String[prefix.size() + suffix.size()]);

        for (int i = 0; i < suffix.size(); ++i) {
            parts[prefix.size() + i] = suffix.part(i);
        }

        return new MetricName(asList(parts));
    }

    /* ****************************** */

    public static MetricName emptyMetricName() {
        return new MetricName(emptyList());
    }

    /* ****************************** */

    private MetricName(List<String> parts) {
        this.parts = requireNonNull(parts);
        this.hashCode = this.parts.hashCode();
    }

    @Override
    public MetricName name() {
        return this;
    }

    @Override
    public Iterator<String> iterator() {
        return parts.iterator();
    }

    public String part(int i) {
        return parts.get(i);
    }

    public String lastPart() {
        return isEmpty() ? null : part(size() - 1);
    }

    public int size() {
        return parts.size();
    }

    public boolean isEmpty() {
        return parts.isEmpty();
    }

    public MetricName withNewPart(String newPart) {
        return withNewPart(newPart, size());
    }

    public MetricName withNewPart(String newPart, int pos) {
        String[] resultParts = new String[size() + 1];
        int i = 0;

        while (i < pos) {
            resultParts[i] = part(i);
            ++i;
        }

        resultParts[i] = newPart;

        while (i < size()) {
            resultParts[i + 1] = part(i);
            ++i;
        }

        return new MetricName(asList(resultParts));
    }

    public MetricName replace(String replacement, int pos) {
        String[] resultParts = parts.toArray(new String[size()]);
        resultParts[pos] = replacement;
        return new MetricName(asList(resultParts));
    }

    public MetricName replace(String replacement1, int pos1, String replacement2, int pos2) {
        String[] resultParts = parts.toArray(new String[size()]);
        resultParts[pos1] = replacement1;
        resultParts[pos2] = replacement2;
        return new MetricName(asList(resultParts));
    }

    public MetricName lowerCase() {
        return lowerCase(Locale.ENGLISH);
    }

    public MetricName lowerCase(Locale locale) {
        if (isEmpty()) {
            return this;
        }

        String[] resultParts = new String[size()];

        for (int i = 0; i < size(); ++i) {
            resultParts[i] = part(i).toLowerCase(locale);
        }

        return new MetricName(asList(resultParts));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        MetricName that = (MetricName)other;

        if (hashCode != that.hashCode) {
            return false;
        }

        return parts.equals(that.parts);
    }

    public boolean equals(MetricName prefix, MetricName suffix) {
        if (prefix.isEmpty()) {
            return equals(suffix);
        }

        if (suffix.isEmpty()) {
            return equals(prefix);
        }

        int prefixSize = prefix.size();
        int suffixSize = suffix.size();

        if (size() != prefixSize + suffixSize) {
            return false;
        }

        if (!Objects.equals(suffix.lastPart(), lastPart())) {
            return false;
        }

        int i = 0;

        while (i < prefixSize) {
            if (!Objects.equals(prefix.part(i), part(i))) {
                return false;
            }

            ++i;
        }

        while (i < size()) {
            if (!Objects.equals(suffix.part(i - prefixSize), part(i))) {
                return false;
            }

            ++i;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return join(".", parts);
    }
}