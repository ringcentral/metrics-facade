package com.ringcentral.platform.metrics.infoProviders;

import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.names.MetricNameMask;
import com.ringcentral.platform.metrics.names.MetricNamed;
import com.ringcentral.platform.metrics.predicates.CompositeMetricNamedPredicate;
import com.ringcentral.platform.metrics.predicates.DefaultMetricNamedPredicate;
import com.ringcentral.platform.metrics.predicates.MetricNamedPredicate;

import java.util.*;

import static com.ringcentral.platform.metrics.names.MetricNameMask.ItemType.FIXED_PART;
import static com.ringcentral.platform.metrics.names.MetricNameMask.anyMetricNameMask;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkState;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

public class MaskTreeMetricNamedInfoProvider<I> implements PredicativeMetricNamedInfoProvider<I> {

    private int infoOrderNumSeq;

    private final Node<I> inRoot = new Node<>(null, null);
    private final Node<I> exRoot = new Node<>(null, null);

    private final List<Entry<I>> notMaskInEntries = new ArrayList<>();
    private final List<Entry<I>> notMaskExEntries = new ArrayList<>();

    private final Map<String, List<Entry<I>>> keyToEntries = new HashMap<>();

    @Override
    public MaskTreeMetricNamedInfoProvider<I> addInfo(MetricNamedPredicate predicate, I info) {
        return addInfo(null, predicate, info);
    }

    @Override
    public MaskTreeMetricNamedInfoProvider<I> addInfo(String key, MetricNamedPredicate predicate, I info) {
        List<Entry<I>> keyEntries;

        if (key != null) {
            if (keyToEntries.containsKey(key)) {
                throw new IllegalArgumentException("Duplicate key " + key);
            }

            keyEntries = new ArrayList<>(predicate instanceof CompositeMetricNamedPredicate ? 4 : 1);
        } else {
            keyEntries = null;
        }

        MetricNamedPredicate p = requireNonNull(predicate);

        if (p instanceof MetricNameMask) {
            addMaskEntry(key, keyEntries, (MetricNameMask)p, null, info, nextInfoOrderNum(), true);
        } else if (p instanceof DefaultMetricNamedPredicate) {
            DefaultMetricNamedPredicate dp = (DefaultMetricNamedPredicate)p;
            addMaskEntry(key, keyEntries, dp.nameMask(), dp.additionalPredicate(), info, nextInfoOrderNum(), true);
        } else if (p instanceof CompositeMetricNamedPredicate) {
            addEntries(key, keyEntries, (CompositeMetricNamedPredicate)p, info);
        } else {
            addNotMaskEntry(key, keyEntries, p, info, nextInfoOrderNum(), true);
        }

        if (key != null) {
            keyToEntries.put(key, keyEntries);
        }

        return this;
    }

    private int nextInfoOrderNum() {
        checkState(infoOrderNumSeq < Integer.MAX_VALUE, "Info count limit exceeded");
        return ++infoOrderNumSeq;
    }

    private void addMaskEntry(
        String key,
        List<Entry<I>> keyEntries,
        MetricNameMask mask,
        MetricNamedPredicate additionalPredicate,
        I info,
        int infoOrderNum,
        boolean inclusion) {

        int maskSize = mask.size();
        Node<I> node = inclusion ? inRoot : exRoot;
        int i = 0;

        while (i < maskSize && mask.item(i).type() == FIXED_PART) {
            node = node.ensureChild(mask.item(i).fixedPart());
            ++i;
        }

        Entry<I> entry;

        if (i == maskSize) {
            entry = new Entry<>(additionalPredicate, info, infoOrderNum, node, node.nameEntries);
            node.nameEntries.add(entry);
        } else if (i + 1 == maskSize) {
            entry = new Entry<>(additionalPredicate, info, infoOrderNum, node, node.anyNameSuffixEntries);
            node.anyNameSuffixEntries.add(entry);
        } else {
            MetricNameMask submask = mask.submask(i);
            int i2 = i;

            MetricNamedPredicate p =
                additionalPredicate != null ?
                n -> submask.matches(n.name(), i2) && additionalPredicate.matches(n) :
                n -> submask.matches(n.name(), i2);

            entry = new Entry<>(p, info, infoOrderNum, node, node.nameSuffixMaskEntries);
            node.nameSuffixMaskEntries.add(entry);
        }

        if (key != null) {
            keyEntries.add(entry);
        }
    }

    private void addEntries(
        String key,
        List<Entry<I>> keyEntries,
        CompositeMetricNamedPredicate p,
        I info) {

        int infoOrderNum = nextInfoOrderNum();

        if (p.hasInclusionPredicates()) {
            for (MetricNamedPredicate inPredicate : p.inclusionPredicates()) {
                if (inPredicate instanceof MetricNameMask) {
                    addMaskEntry(key, keyEntries, (MetricNameMask)inPredicate, null, info, infoOrderNum, true);
                } else if (inPredicate instanceof DefaultMetricNamedPredicate) {
                    DefaultMetricNamedPredicate dp = (DefaultMetricNamedPredicate)inPredicate;
                    addMaskEntry(key, keyEntries, dp.nameMask(), dp.additionalPredicate(), info, infoOrderNum, true);
                } else {
                    addNotMaskEntry(key, keyEntries, inPredicate, info, infoOrderNum, true);
                }
            }
        } else {
            addMaskEntry(key, keyEntries, anyMetricNameMask(), null, info, infoOrderNum, true);
        }

        if (p.hasExclusionPredicates()) {
            for (MetricNamedPredicate exPredicate : p.exclusionPredicates()) {
                if (exPredicate instanceof MetricNameMask) {
                    addMaskEntry(key, keyEntries, (MetricNameMask)exPredicate, null, info, infoOrderNum, false);
                } else if (exPredicate instanceof DefaultMetricNamedPredicate) {
                    DefaultMetricNamedPredicate dp = (DefaultMetricNamedPredicate)exPredicate;
                    addMaskEntry(key, keyEntries, dp.nameMask(), dp.additionalPredicate(), info, infoOrderNum, false);
                } else {
                    addNotMaskEntry(key, keyEntries, exPredicate, info, infoOrderNum, false);
                }
            }
        }
    }

    private void addNotMaskEntry(
        String key,
        List<Entry<I>> keyEntries,
        MetricNamedPredicate p,
        I info,
        int infoOrderNum,
        boolean inclusion) {

        List<Entry<I>> list = inclusion ? notMaskInEntries : notMaskExEntries;
        Entry<I> entry = new Entry<>(p, info, infoOrderNum, null, list);
        list.add(entry);

        if (key != null) {
            keyEntries.add(entry);
        }
    }

    @Override
    public MaskTreeMetricNamedInfoProvider<I> removeInfo(String key) {
        List<Entry<I>> entries = keyToEntries.remove(key);

        if (entries != null) {
            entries.forEach(e -> {
                e.containingList.remove(e);
                Node<I> node = e.node;

                while (node != null && node.parent != null && node.isEmpty()) {
                    node.parent.children.remove(node.namePart);
                    node = node.parent;
                }
            });
        }

        return this;
    }

    @Override
    public List<I> infosFor(MetricNamed named) {
        MetricName name = named.name();
        int nameSize = name.size();
        ResultBuilder<I> resultBuilder = new ResultBuilder<>();

        if (!notMaskInEntries.isEmpty()) {
            for (Entry<I> e : notMaskInEntries) {
                if (e.predicate.matches(named)) {
                    resultBuilder.add(e);
                }
            }
        }

        Node<I> node = inRoot;

        for (int i = 0; node != null && i <= nameSize; ++i) {
            if (i == nameSize) {
                for (Entry<I> e : node.nameEntries) {
                    if (!e.hasPredicate() || e.predicate.matches(named)) {
                        resultBuilder.add(e);
                    }
                }
            }

            for (Entry<I> e : node.anyNameSuffixEntries) {
                if (!e.hasPredicate() || e.predicate.matches(named)) {
                    resultBuilder.add(e);
                }
            }

            if (i < nameSize) {
                for (Entry<I> e : node.nameSuffixMaskEntries) {
                    if (e.predicate.matches(named)) {
                        resultBuilder.add(e);
                    }
                }

                node = node.child(name.part(i));
            } else {
                break;
            }
        }

        if (!notMaskExEntries.isEmpty()) {
            for (Entry<I> e : notMaskExEntries) {
                if (e.predicate.matches(named)) {
                    resultBuilder.remove(e.infoOrderNum);
                }
            }
        }

        node = exRoot;

        for (int i = 0; node != null && i <= nameSize; ++i) {
            if (i == nameSize) {
                for (Entry<I> e : node.nameEntries) {
                    if (!e.hasPredicate() || e.predicate.matches(named)) {
                        resultBuilder.remove(e.infoOrderNum);
                    }
                }
            }

            for (Entry<I> e : node.anyNameSuffixEntries) {
                if (!e.hasPredicate() || e.predicate.matches(named)) {
                    resultBuilder.remove(e.infoOrderNum);
                }
            }

            if (i < nameSize) {
                for (Entry<I> e : node.nameSuffixMaskEntries) {
                    if (e.predicate.matches(named)) {
                        resultBuilder.remove(e.infoOrderNum);
                    }
                }

                node = node.child(name.part(i));
            } else {
                break;
            }
        }

        return resultBuilder.build();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static class Entry<I> {

        final MetricNamedPredicate predicate;
        final I info;
        final Integer infoOrderNum;
        final Node<I> node;
        final List<Entry<I>> containingList;

        Entry(
            MetricNamedPredicate predicate,
            I info,
            Integer infoOrderNum,
            Node<I> node,
            List<Entry<I>> containingList) {

            this.predicate = predicate;
            this.info = info;
            this.infoOrderNum = infoOrderNum;
            this.node = node;
            this.containingList = containingList;
        }

        boolean hasPredicate() {
            return predicate != null;
        }
    }

    private static class ResultBuilder<I> {

        private TreeMap<Integer, I> infos;

        void add(Entry<I> entry) {
            if (infos == null) {
                infos = new TreeMap<>();
            }

            infos.put(entry.infoOrderNum, entry.info);
        }

        void remove(int infoOrderNum) {
            if (infos != null) {
                infos.remove(infoOrderNum);
            }
        }

        List<I> build() {
            if (infos != null) {
                return infos.size() == 1 ? singletonList(infos.firstEntry().getValue()) : new ArrayList<>(infos.values());
            } else {
                return emptyList();
            }
        }
    }

    private static class Node<I> {

        final List<Entry<I>> nameEntries = new ArrayList<>(4);
        final List<Entry<I>> anyNameSuffixEntries = new ArrayList<>(4);
        final List<Entry<I>> nameSuffixMaskEntries = new ArrayList<>(4);
        final Node<I> parent;
        final String namePart;
        final Map<String, Node<I>> children = new HashMap<>();

        Node(Node<I> parent, String namePart) {
            this.parent = parent;
            this.namePart = namePart;
        }

        Node<I> ensureChild(String childNamePart) {
            return children.computeIfAbsent(childNamePart, p -> new Node<>(this, childNamePart));
        }

        Node<I> child(String childNamePart) {
            return children.get(childNamePart);
        }

        boolean isEmpty() {
            return nameEntries.isEmpty()
                && anyNameSuffixEntries.isEmpty()
                && nameSuffixMaskEntries.isEmpty()
                && children.isEmpty();
        }
    }
}
