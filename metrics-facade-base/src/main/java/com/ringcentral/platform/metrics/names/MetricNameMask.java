package com.ringcentral.platform.metrics.names;

import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;

import java.util.*;

import static com.ringcentral.platform.metrics.names.MetricNameMask.ItemType.*;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.util.Arrays.copyOfRange;
import static org.apache.commons.lang3.StringUtils.split;

public class MetricNameMask implements MetricNamedPredicate {

    public enum ItemType {
        FIXED_PART,
        ANY_PARTS
    }

    public static class Item {

        private static final Item ANY_PARTS_ITEM = new Item(ANY_PARTS);

        private final ItemType type;
        private final String fixedPart;

        private static Item of(String s) {
            return "**".equals(s) ? ANY_PARTS_ITEM : new Item(FIXED_PART, s);
        }

        private Item(ItemType type) {
            this(type, null);
        }

        private Item(ItemType type, String fixedPart) {
            this.type = type;
            this.fixedPart = fixedPart;
        }

        public ItemType type() {
            return type;
        }

        public String fixedPart() {
            return fixedPart;
        }
    }

    private static final MetricNameMask ANY_METRIC_NAME_MASK = new MetricNameMask(new Item[] { Item.ANY_PARTS_ITEM });
    private final Item[] items;

    /* ****************************** */

    public static MetricNameMask of(String s) {
        return metricNameMask(s);
    }

    public static MetricNameMask forMetricsMatchingNameMask(String s) {
        return metricNameMask(s);
    }

    public static MetricNameMask metricsMatchingNameMask(String s) {
        return metricNameMask(s);
    }

    public static MetricNameMask matchingNameMask(String s) {
        return metricNameMask(s);
    }

    public static MetricNameMask nameMask(String s) {
        return metricNameMask(s);
    }

    public static MetricNameMask metricNameMask(String s) {
        return new MetricNameMask(itemsOf(s));
    }

    /* ****************************** */

    public static MetricNameMask forMetricWithName(String s) {
        return metricNameMask(s);
    }

    public static MetricNameMask metricWithName(String s) {
        return metricNameMask(s);
    }

    /* ****************************** */

    public static MetricNameMask namePrefix(String s) {
        return metricsWithNamePrefix(s);
    }

    public static MetricNameMask forMetricsWithNamePrefix(String s) {
        return metricsWithNamePrefix(s);
    }

    public static MetricNameMask metricsWithNamePrefix(String s) {
        return new MetricNameMask(itemsOf(s, Item.ANY_PARTS_ITEM));
    }

    /* ****************************** */

    public static MetricNameMask allMetrics() {
        return anyMetricNameMask();
    }

    public static MetricNameMask anyMetricNameMask() {
        return ANY_METRIC_NAME_MASK;
    }

    /* ****************************** */

    private static Item[] itemsOf(String s, Item... suffix) {
        List<Item> items = new ArrayList<>();

        Arrays.stream(split(s, ".")).map(Item::of).forEach(item -> {
            if (item.type() == FIXED_PART
                || items.isEmpty()
                || items.get(items.size() - 1).type() != ANY_PARTS) {

                items.add(item);
            }
        });

        checkArgument(
            !items.isEmpty(),
            "No parts in metric name mask '" + s + "'");

        if (suffix != null) {
            for (int i = 0; i < suffix.length; ++i) {
                Item item = suffix[i];

                if (item.type == FIXED_PART
                    || items.get(items.size() - 1).type() != ANY_PARTS) {

                    items.add(item);
                }
            }
        }

        return items.toArray(new Item[0]);
    }

    private MetricNameMask(Item[] items) {
        checkArgument(
            items != null && items.length > 0,
            "items is null or empty");

        this.items = items;
    }

    public Item item(int i) {
        return items[i];
    }

    public int size() {
        return items.length;
    }

    public MetricNameMask submask(int firstItemIndex) {
        checkArgument(
            firstItemIndex >= 0 && firstItemIndex < size(),
            "firstItemIndex must be in 0..(size() - 1)");

        if (firstItemIndex == 0) {
            return this;
        }

        return new MetricNameMask(copyOfRange(items, firstItemIndex, size()));
    }

    @Override
    public boolean matches(MetricNamed named) {
         return matches(named.name());
    }

    public boolean matches(MetricName name) {
        return matches(name, 0);
    }

    public boolean matches(MetricName name, int firstPartIndex) {
        int nameSize = name.size();

        checkArgument(
            firstPartIndex >= 0 && firstPartIndex <= nameSize,
            "firstPartIndex must be in 0..name.size()");

        if (items.length == 1 && items[0].type() == ANY_PARTS) {
            return true;
        }

        if (nameSize - firstPartIndex == 0) {
            return false;
        }

        int i1 = 0;
        int p1 = firstPartIndex;

        while (i1 < items.length && items[i1].type == FIXED_PART) {
            if (p1 < nameSize && items[i1].fixedPart.equals(name.part(p1))) {
                ++i1;
                ++p1;
            } else {
                return false;
            }
        }

        if (i1 == items.length) {
            return p1 == nameSize;
        }

        int i2 = items.length - 1;
        int p2 = nameSize - 1;

        while (items[i2].type == FIXED_PART) {
            if (p2 >= p1 && items[i2].fixedPart.equals(name.part(p2))) {
                --i2;
                --p2;
            } else {
                return false;
            }
        }

        for (int i = i1 + 1; i < i2; ++i) {
            if (items[i].type == FIXED_PART) {
                int anyPartsPos = i + 1;

                while (items[anyPartsPos].type == FIXED_PART) {
                    ++anyPartsPos;
                }

                p1 = findFixedPartSequence(i, anyPartsPos - 1, name, p1, p2);

                if (p1 < 0) {
                    return false;
                }

                i = anyPartsPos;
            }
        }

        return true;
    }

    private int findFixedPartSequence(int i, int lastFixedPartPos, MetricName name, int p1, int p2) {
        int distance = lastFixedPartPos - i;

        if ((p2 - p1) < distance) {
            return -1;
        }

        int maxSeqStartPos = p2 - distance;

        seqSearch:
        while (p1 <= maxSeqStartPos) {
            for (int j = 0; j < (distance + 1); ++j) {
                if (!items[i + j].fixedPart.equals(name.part(p1 + j))) {
                    ++p1;
                    continue seqSearch;
                }
            }

            return p1 + distance + 1;
        }

        return -1;
    }
}
