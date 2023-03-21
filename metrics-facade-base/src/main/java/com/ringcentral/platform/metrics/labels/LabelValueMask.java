package com.ringcentral.platform.metrics.labels;

import java.util.Arrays;

import static com.ringcentral.platform.metrics.utils.Preconditions.*;
import static org.apache.commons.lang3.StringUtils.*;

public class LabelValueMask implements LabelValuePredicate {

    public static final String DEFAULT_OPTIONS_DELIMITER = "|";
    public static final String DEFAULT_OPTION_VARIABLE_PART = "*";

    private final Label label;
    private final Option[] options;

    public static LabelValueMask of(Label label, String s) {
        return of(label, s, DEFAULT_OPTIONS_DELIMITER, DEFAULT_OPTION_VARIABLE_PART);
    }

    public static LabelValueMask of(
        Label label,
        String s,
        String optionsDelimiter,
        String optionVarPart) {

        return new LabelValueMask(
            label,
            Arrays.stream(split(s, optionsDelimiter)).map(os -> Option.of(os, optionVarPart)).toArray(Option[]::new));
    }

    private LabelValueMask(Label label, Option[] options) {
        this.label = label;
        this.options = options;
    }

    @Override
    public Label label() {
        return label;
    }

    @Override
    public boolean matches(String value) {
        for (int i = 0; i < options.length; ++i) {
            if (options[i].matches(value)) {
                return true;
            }
        }

        return false;
    }

    private static class Option {

        final String[] fixedParts;
        final String lastFixedPart;
        final boolean startsWithVarPart;
        final boolean endsWithVarPart;
        final boolean matchesAny;

        Option(
            String[] fixedParts,
            boolean startsWithVarPart,
            boolean endsWithVarPart,
            boolean matchesAny) {

            this.fixedParts = fixedParts;
            this.lastFixedPart = fixedParts.length > 0 ? fixedParts[fixedParts.length - 1] : null;
            this.startsWithVarPart = startsWithVarPart;
            this.endsWithVarPart = endsWithVarPart;
            this.matchesAny = matchesAny;
        }

        static Option of(String s, String varPart) {
            checkArgument(!isBlank(s), "Option is blank");

            return new Option(
                split(s, varPart),
                s.startsWith(varPart),
                s.endsWith(varPart),
                s.equals(varPart));
        }

        boolean matches(String v) {
            if (matchesAny) {
                return true;
            }

            if (fixedParts.length == 1) {
                if (startsWithVarPart && endsWithVarPart) {
                    return v.contains(lastFixedPart);
                } else if (startsWithVarPart) {
                    return v.endsWith(lastFixedPart);
                } else if (endsWithVarPart) {
                    return v.startsWith(lastFixedPart);
                } else {
                    return lastFixedPart.equals(v);
                }
            }

            int len = v.length();
            int pos = 0;

            for (int i = 0; i < fixedParts.length; ++i) {
                if (pos >= len) {
                    return false;
                }

                String fixedPart = fixedParts[i];
                pos = v.indexOf(fixedPart, pos);

                if (pos == -1 || (i == 0 && !startsWithVarPart && pos > 0)) {
                    return false;
                }

                pos = pos + fixedPart.length();
            }

            return endsWithVarPart || pos == len || v.endsWith(lastFixedPart);
        }
    }
}
