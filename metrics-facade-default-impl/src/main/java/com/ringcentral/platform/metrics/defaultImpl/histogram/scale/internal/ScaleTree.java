package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.internal;

import com.ringcentral.platform.metrics.scale.Scale;

import java.util.*;

import static com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot.*;
import static java.lang.Math.sqrt;

@SuppressWarnings("NonAtomicOperationOnVolatileField")
public class ScaleTree {

    public static final long INITIAL_SNAPSHOT_NUM = Long.MIN_VALUE;

    public interface NodeConsumer {
        void consumeNode(ScaleTreeNode node);
    }

    public interface SubtreeUpdateCountProvider {
        long subtreeUpdateCountFor(ScaleTreeNode node);
    }

    public interface NodeUpdateCountConsumer {
        void consumeNodeUpdateCount(ScaleTreeNode node, long updateCount);
    }

    public static class StandardDeviationCalculator implements NodeUpdateCountConsumer {

        private double mean;
        private double geometricDeviationSum;

        @Override
        public void consumeNodeUpdateCount(ScaleTreeNode node, long updateCount) {
            double deviation = (node.point * 1.0) - mean;
            geometricDeviationSum += (deviation * deviation) * updateCount;
        }

        public double standardDeviation(long treeUpdateCount) {
            return sqrt(geometricDeviationSum / treeUpdateCount);
        }

        public void reset(double mean) {
            this.mean = mean;
            this.geometricDeviationSum = 0.0;
        }
    }

    public final ScaleTreeNode root;

    public volatile long updateEpoch;
    public volatile boolean snapshotInProgress;
    public volatile long snapshotNum = INITIAL_SNAPSHOT_NUM;

    public static ScaleTree of(
        Scale scale,
        int scaleSplitFactor,
        long[] bucketUpperBounds,
        int maxIndexedLevel,
        boolean resettable,
        long initialUpdateEpoch) {

        List<Long> points = points(scale, bucketUpperBounds, scaleSplitFactor);

        ScaleTreeNode root = buildSubtree(
            points,
            0,
            points.size() - 1,
            0,
            resettable,
            initialUpdateEpoch);

        if (maxIndexedLevel >= 0) {
            Queue<ScaleTreeNode> queue = new ArrayDeque<>();
            queue.add(root);
            int i = 0;

            while (!queue.isEmpty()) {
                ScaleTreeNode node = queue.poll();
                node.levelOrderIndex = i++;

                if (node.level < maxIndexedLevel) {
                    if (node.left != null) {
                        queue.add(node.left);
                    }

                    if (node.right != null) {
                        queue.add(node.right);
                    }
                }
            }
        }

        return new ScaleTree(root, initialUpdateEpoch);
    }

    private static List<Long> points(
        Scale scale,
        long[] bucketUpperBounds,
        int scaleSplitFactor) {

        if ((bucketUpperBounds == null || bucketUpperBounds.length == 0) && scaleSplitFactor < 2) {
            return scale.points();
        }

        SortedSet<Long> points = new TreeSet<>(scale.points());

        if (bucketUpperBounds != null && bucketUpperBounds.length > 0) {
            for (long bucketUpperBound : bucketUpperBounds) {
                points.add(bucketUpperBound);
            }
        }

        if (scaleSplitFactor > 1) {
            Set<Long> additionalPoints = new HashSet<>();
            Long lastPoint = null;

            for (Long point : points) {
                if (lastPoint != null) {
                    long step = Math.max((point - lastPoint) / scaleSplitFactor, 1L);
                    long additionalPoint = lastPoint + step;

                    while (additionalPoint < point) {
                        additionalPoints.add(additionalPoint);
                        additionalPoint += step;
                    }
                }

                lastPoint = point;
            }

            if (!additionalPoints.isEmpty()) {
                points.addAll(additionalPoints);
            }
        }

        return new ArrayList<>(points);
    }

    private static ScaleTreeNode buildSubtree(
        List<Long> points,
        int low,
        int high,
        int level,
        boolean resettable,
        long initialUpdateEpoch) {

        if (low > high) {
            return null;
        }

        int mid = (low + high) >>> 1;

        ScaleTreeNode left = buildSubtree(points, low, mid - 1, level + 1, resettable, initialUpdateEpoch);
        ScaleTreeNode right = buildSubtree(points, mid + 1, high, level + 1, resettable, initialUpdateEpoch);

        ScaleTreeNode subtreeRoot = new ScaleTreeNode(
            points.get(mid),
            left,
            right,
            level,
            resettable,
            initialUpdateEpoch);

        if (left != null) {
            left.parent = subtreeRoot;
        }

        if (right != null) {
            right.parent = subtreeRoot;
        }

        return subtreeRoot;
    }

    private ScaleTree(ScaleTreeNode root, long updateEpoch) {
        this.root = root;
        this.updateEpoch = updateEpoch;
    }

    public void startUpdateEpoch() {
        ++updateEpoch;
    }

    public ScaleTreeNode nodeForValue(long value) {
        ScaleTreeNode node = root;

        while (true) {
            if (value < node.point) {
                if (node.left != null) {
                    node = node.left;
                } else {
                    break;
                }
            } else if (value > node.point) {
                if (node.right != null) {
                    node = node.right;
                } else {
                    ScaleTreeNode n = node;

                    while (n != null) {
                        if (n.isLeftChild()) {
                            node = n.parent;
                            break;
                        } else {
                            n = n.parent;
                        }
                    }

                    break;
                }
            } else {
                break;
            }
        }

        return node;
    }

    public void traversePostOrder(NodeConsumer consumer) {
        traversePostOrder(root, consumer, Integer.MAX_VALUE);
    }

    public void traversePostOrder(NodeConsumer consumer, int maxLevel) {
        // assuming that maxLevel >= 0
        traversePostOrder(root, consumer, maxLevel);
    }

    void traversePostOrder(ScaleTreeNode node, NodeConsumer consumer, int maxLevel) {
        if (node.level < maxLevel) {
            if (node.left != null) {
                traversePostOrder(node.left, consumer, maxLevel);
            }

            if (node.right != null) {
                traversePostOrder(node.right, consumer, maxLevel);
            }
        }

        consumer.consumeNode(node);
    }

    public void traverseUpdateCount(
        SubtreeUpdateCountProvider subtreeUpdateCountProvider,
        NodeUpdateCountConsumer consumer) {

        traverseUpdateCount(root, subtreeUpdateCountProvider, consumer);
    }

    long traverseUpdateCount(
        ScaleTreeNode node,
        SubtreeUpdateCountProvider subtreeUpdateCountProvider,
        NodeUpdateCountConsumer consumer) {

        if (node == null) {
            return 0L;
        }

        long subtreeUpdateCount = subtreeUpdateCountProvider.subtreeUpdateCountFor(node);

        if (subtreeUpdateCount > 0L) {
            long updateCount =
                subtreeUpdateCount
                - traverseUpdateCount(node.left, subtreeUpdateCountProvider, consumer)
                - traverseUpdateCount(node.right, subtreeUpdateCountProvider, consumer);

            consumer.consumeNodeUpdateCount(node, updateCount);
        }

        return subtreeUpdateCount;
    }

    public void startSnapshot() {
        ++snapshotNum;
        snapshotInProgress = true;
    }

    public void endSnapshot() {
        snapshotInProgress = false;
    }

    @SuppressWarnings("DuplicatedCode")
    public long min(SubtreeUpdateCountProvider subtreeUpdateCountProvider) {
        long min = NO_VALUE;
        ScaleTreeNode node = root;

        while (true) {
            while (node.left != null && subtreeUpdateCountProvider.subtreeUpdateCountFor(node.left) > 0L) {
                node = node.left;
            }

            if (node.right == null) {
                break;
            }

            long nodeSubtreeUpdateCount = subtreeUpdateCountProvider.subtreeUpdateCountFor(node);
            long rightSubtreeUpdateCount = subtreeUpdateCountProvider.subtreeUpdateCountFor(node.right);

            if (rightSubtreeUpdateCount == 0L || nodeSubtreeUpdateCount > rightSubtreeUpdateCount) {
                break;
            }

            node = node.right;
        }

        if (subtreeUpdateCountProvider.subtreeUpdateCountFor(node) > 0L) {
            min = node.point;
        }

        return min;
    }

    @SuppressWarnings("DuplicatedCode")
    public long max(SubtreeUpdateCountProvider subtreeUpdateCountProvider) {
        long max = NO_VALUE;
        ScaleTreeNode node = root;

        while (true) {
            while (node.right != null && subtreeUpdateCountProvider.subtreeUpdateCountFor(node.right) > 0L) {
                node = node.right;
            }

            if (node.left == null) {
                break;
            }

            long nodeSubtreeUpdateCount = subtreeUpdateCountProvider.subtreeUpdateCountFor(node);
            long leftSubtreeUpdateCount = subtreeUpdateCountProvider.subtreeUpdateCountFor(node.left);

            if (leftSubtreeUpdateCount == 0L || nodeSubtreeUpdateCount > leftSubtreeUpdateCount) {
                break;
            }

            node = node.left;
        }

        if (subtreeUpdateCountProvider.subtreeUpdateCountFor(node) > 0L) {
            max = node.point;
        }

        return max;
    }

    public double standardDeviation(
        SubtreeUpdateCountProvider subtreeUpdateCountProvider,
        StandardDeviationCalculator calculator,
        long treeUpdateCount,
        double mean) {

        if (treeUpdateCount > 0L) {
            calculator.reset(mean);
            traverseUpdateCount(subtreeUpdateCountProvider, calculator);
            return calculator.standardDeviation(treeUpdateCount);
        } else {
            return NO_VALUE_DOUBLE;
        }
    }

    public long[] bucketSizes(long[] bucketUpperBounds, SubtreeUpdateCountProvider subtreeUpdateCountProvider) {
        long[] bucketSizes = new long[bucketUpperBounds.length];
        return addBucketSizesTo(bucketSizes, bucketUpperBounds, subtreeUpdateCountProvider);
    }

    public long[] addBucketSizesTo(
        long[] bucketSizes,
        long[] bucketUpperBounds,
        SubtreeUpdateCountProvider subtreeUpdateCountProvider) {

        for (int i = 0; i < bucketUpperBounds.length; ++i) {
            long bound = bucketUpperBounds[i];
            ScaleTreeNode node = root;

            while (true) {
                if (bound < node.point) {
                    if (node.left != null) {
                        node = node.left;
                    } else {
                        break;
                    }
                } else if (bound > node.point) {
                    if (node.right != null) {
                        bucketSizes[i] += (subtreeUpdateCountProvider.subtreeUpdateCountFor(node) - subtreeUpdateCountProvider.subtreeUpdateCountFor(node.right));
                        node = node.right;
                    } else {
                        bucketSizes[i] += subtreeUpdateCountProvider.subtreeUpdateCountFor(node);
                        break;
                    }
                } else {
                    bucketSizes[i] += subtreeUpdateCountProvider.subtreeUpdateCountFor(node);

                    if (node.right != null) {
                        bucketSizes[i] -= subtreeUpdateCountProvider.subtreeUpdateCountFor(node.right);
                    }

                    break;
                }
            }
        }

        return bucketSizes;
    }

    public double[] percentileValues(double[] quantiles, SubtreeUpdateCountProvider subtreeUpdateCountProvider) {
        double[] percentileValues = new double[quantiles.length];

        percentilesLoop:
        for (int i = 0; i < quantiles.length; ++i) {
            percentileValues[i] = NO_VALUE_DOUBLE;
            ScaleTreeNode node = root;

            long count = subtreeUpdateCountProvider.subtreeUpdateCountFor(node);

            if (count == 0L) {
                continue;
            }

            long percentileCount = Math.min(Math.max(Math.round(count * quantiles[i]), 0L), count);

            while (node != null) {
                long leftSubtreeUpdateCount = subtreeUpdateCountProvider.subtreeUpdateCountFor(node.left);
                long rightSubtreeUpdateCount = subtreeUpdateCountProvider.subtreeUpdateCountFor(node.right);

                if (percentileCount > leftSubtreeUpdateCount
                    && percentileCount <= (count - rightSubtreeUpdateCount)
                    && (count - leftSubtreeUpdateCount - rightSubtreeUpdateCount) > 0L) {

                    percentileValues[i] = node.point;
                    continue percentilesLoop;
                }

                if (percentileCount <= leftSubtreeUpdateCount) {
                    if (leftSubtreeUpdateCount > 0L) {
                        node = node.left;
                        count = leftSubtreeUpdateCount;
                    } else if ((count - leftSubtreeUpdateCount - rightSubtreeUpdateCount) > 0L) {
                        percentileValues[i] = node.point;
                        continue percentilesLoop;
                    } else if (rightSubtreeUpdateCount > 0L) {
                        node = node.right;
                        percentileCount -= (count - rightSubtreeUpdateCount);
                        count = rightSubtreeUpdateCount;
                    } else {
                        percentileValues[i] = node.point;
                        continue percentilesLoop;
                    }
                } else if (rightSubtreeUpdateCount > 0L) {
                    node = node.right;
                    percentileCount -= (count - rightSubtreeUpdateCount);
                    count = rightSubtreeUpdateCount;
                } else {
                    percentileValues[i] = node.point;
                    continue percentilesLoop;
                }
            }
        }

        return percentileValues;
    }
}
