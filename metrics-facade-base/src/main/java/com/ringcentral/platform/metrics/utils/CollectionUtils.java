package com.ringcentral.platform.metrics.utils;

import java.util.*;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;

public class CollectionUtils {

    public static <K, V> LinkedHashMap<K, V> linkedHashMapOf(List<K> keys, List<V> values) {
        checkArgument(
            keys.size() == values.size(),
            "keys.size() != values.size()");

        LinkedHashMap<K, V> map = new LinkedHashMap<>(keys.size());

        for (int i = 0; i < keys.size(); ++i) {
            map.put(keys.get(i), values.get(i));
        }

        return map;
    }

    public static <I> List<I> iterToList(Iterator<I> iter) {
        List<I> list = new ArrayList<>();
        iter.forEachRemaining(list::add);
        return list;
    }

    public static <I> Set<I> iterToSet(Iterator<I> iter) {
        Set<I> set = new HashSet<>();
        iter.forEachRemaining(set::add);
        return set;
    }

    public static <I> boolean containsAllInOrder(List<I> container, List<? extends I> items) {
        int containerSize = container.size();
        int itemCount = items.size();

        if (containerSize < itemCount) {
            return false;
        }

        if (containerSize == 0) {
            return true;
        }

        int c = containerSize - 1;
        int i = itemCount - 1;

        while (i >= 0) {
            if (Objects.equals(container.get(c), items.get(i))) {
                --i;
            }

            --c;

            if (i > c) {
                return false;
            }
        }

        return true;
    }
}