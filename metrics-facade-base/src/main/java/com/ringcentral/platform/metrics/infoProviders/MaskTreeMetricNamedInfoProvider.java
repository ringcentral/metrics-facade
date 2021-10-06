package com.ringcentral.platform.metrics.infoProviders;

import com.ringcentral.platform.metrics.names.*;
import com.ringcentral.platform.metrics.predicates.*;

import java.util.*;

import static com.ringcentral.platform.metrics.names.MetricNameMask.ItemType.*;
import static com.ringcentral.platform.metrics.names.MetricNameMask.*;
import static com.ringcentral.platform.metrics.utils.Preconditions.*;
import static java.util.Collections.*;
import static java.util.Objects.*;

public class MaskTreeMetricNamedInfoProvider<I> implements PredicativeMetricNamedInfoProvider<I> {

    private int infoOrderNumSeq;

    private final Node<I> inRoot = new Node<>();
    private final Node<I> exRoot = new Node<>();

    private final List<Entry<I>> notMaskInEntries = new ArrayList<>();
    private final List<Entry<I>> notMaskExEntries = new ArrayList<>();

    @Override
    public MaskTreeMetricNamedInfoProvider<I> addInfo(MetricNamedPredicate predicate, I info) {
        MetricNamedPredicate p = requireNonNull(predicate);

        if (p instanceof MetricNameMask) {
            addMaskEntry((MetricNameMask)p, null, info, nextInfoOrderNum(), true);
        } else if (p instanceof DefaultMetricNamedPredicate) {
            DefaultMetricNamedPredicate dp = (DefaultMetricNamedPredicate)p;
            addMaskEntry(dp.nameMask(), dp.additionalPredicate(), info, nextInfoOrderNum(), true);
        } else if (p instanceof CompositeMetricNamedPredicate) {
            addEntries((CompositeMetricNamedPredicate)p, info);
        } else {
            addNotMaskEntry(p, info, nextInfoOrderNum(), true);
        }

        return this;
    }

    private int nextInfoOrderNum() {
        checkState(infoOrderNumSeq < Integer.MAX_VALUE, "Info count limit exceeded");
        return ++infoOrderNumSeq;
    }

    private void addMaskEntry(
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

        if (i == maskSize) {
            node.nameEntries.add(new Entry<>(additionalPredicate, info, infoOrderNum));
        } else if (i + 1 == maskSize) {
            node.anyNameSuffixEntries.add(new Entry<>(additionalPredicate, info, infoOrderNum));
        } else {
            MetricNameMask submask = mask.submask(i);
            int i2 = i;

            MetricNamedPredicate p =
                additionalPredicate != null ?
                n -> submask.matches(n.name(), i2) && additionalPredicate.matches(n) :
                n -> submask.matches(n.name(), i2);

            node.nameSuffixMaskEntries.add(new Entry<>(p, info, infoOrderNum));
        }
    }

    private void addEntries(CompositeMetricNamedPredicate p, I info) {
        int infoOrderNum = nextInfoOrderNum();

        if (p.hasInclusionPredicates()) {
            for (MetricNamedPredicate inPredicate : p.inclusionPredicates()) {
                if (inPredicate instanceof MetricNameMask) {
                    addMaskEntry((MetricNameMask)inPredicate, null, info, infoOrderNum, true);
                } else if (inPredicate instanceof DefaultMetricNamedPredicate) {
                    DefaultMetricNamedPredicate dp = (DefaultMetricNamedPredicate)inPredicate;
                    addMaskEntry(dp.nameMask(), dp.additionalPredicate(), info, infoOrderNum, true);
                } else {
                    addNotMaskEntry(inPredicate, info, infoOrderNum, true);
                }
            }
        } else {
            addMaskEntry(anyMetricNameMask(), null, info, infoOrderNum, true);
        }

        if (p.hasExclusionPredicates()) {
            for (MetricNamedPredicate exPredicate : p.exclusionPredicates()) {
                if (exPredicate instanceof MetricNameMask) {
                    addMaskEntry((MetricNameMask)exPredicate, null, info, infoOrderNum, false);
                } else if (exPredicate instanceof DefaultMetricNamedPredicate) {
                    DefaultMetricNamedPredicate dp = (DefaultMetricNamedPredicate)exPredicate;
                    addMaskEntry(dp.nameMask(), dp.additionalPredicate(), info, infoOrderNum, false);
                } else {
                    addNotMaskEntry(exPredicate, info, infoOrderNum, false);
                }
            }
        }
    }

    private void addNotMaskEntry(MetricNamedPredicate p, I info, int infoOrderNum, boolean inclusion) {
        (inclusion ? notMaskInEntries : notMaskExEntries).add(new Entry<>(p, info, infoOrderNum));
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

        Entry(MetricNamedPredicate predicate, I info, Integer infoOrderNum) {
            this.predicate = predicate;
            this.info = info;
            this.infoOrderNum = infoOrderNum;
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
        final Map<String, Node<I>> children = new HashMap<>();

        Node<I> ensureChild(String namePart) {
            return children.computeIfAbsent(namePart, np -> new Node<>());
        }

        Node<I> child(String namePart) {
            return children.get(namePart);
        }
    }
}
