package com.ringcentral.platform.metrics.x.histogram.scale.internal;

import com.ringcentral.platform.metrics.x.histogram.scale.configs.Scale;

import java.util.*;

import static com.ringcentral.platform.metrics.x.histogram.XHistogramSnapshot.NO_VALUE;
import static java.lang.Math.sqrt;

@SuppressWarnings("NonAtomicOperationOnVolatileField")
public class ScaleTree {

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
    public volatile long snapshotNum = Long.MIN_VALUE;

    public static ScaleTree of(
        Scale scale,
        long[] bucketUpperBounds,
        int maxIndexedLevel,
        boolean resettable,
        long initialUpdateEpoch) {

        List<Long> points = points(scale, bucketUpperBounds);

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

    private static List<Long> points(Scale scale, long[] bucketUpperBounds) {
        if (bucketUpperBounds == null || bucketUpperBounds.length == 0) {
            return scale.points();
        }

        SortedSet<Long> points = new TreeSet<>(scale.points());

        for (long bucketUpperBound : bucketUpperBounds) {
            points.add(bucketUpperBound);
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
                    if (node.isLeftChild()) {
                        node = node.parent;
                    }

                    break;
                }
            } else {
                break;
            }
        }

        return node;
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
            if (node.left != null && subtreeUpdateCountProvider.subtreeUpdateCountFor(node.left) > 0L) {
                node = node.left;

                while (node.left != null && subtreeUpdateCountProvider.subtreeUpdateCountFor(node.left) > 0L) {
                    node = node.left;
                }

                if (node.right == null) {
                    break;
                }

                long nodeSubtreeUpdateCount = subtreeUpdateCountProvider.subtreeUpdateCountFor(node);
                long rightSubtreeUpdateCount = subtreeUpdateCountProvider.subtreeUpdateCountFor(node.right);

                if (nodeSubtreeUpdateCount > rightSubtreeUpdateCount) {
                    break;
                }
            } else if (node.right != null && subtreeUpdateCountProvider.subtreeUpdateCountFor(node.right) > 0L) {
                node = node.right;
            } else {
                break;
            }
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
            if (node.right != null && subtreeUpdateCountProvider.subtreeUpdateCountFor(node.right) > 0L) {
                node = node.right;

                while (node.right != null && subtreeUpdateCountProvider.subtreeUpdateCountFor(node.right) > 0L) {
                    node = node.right;
                }

                if (node.left == null) {
                    break;
                }

                long nodeSubtreeUpdateCount = subtreeUpdateCountProvider.subtreeUpdateCountFor(node);
                long leftSubtreeUpdateCount = subtreeUpdateCountProvider.subtreeUpdateCountFor(node.left);

                if (nodeSubtreeUpdateCount > leftSubtreeUpdateCount) {
                    break;
                }
            } else if (node.left != null && subtreeUpdateCountProvider.subtreeUpdateCountFor(node.left) > 0L) {
                node = node.left;
            } else {
                break;
            }
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
            return NO_VALUE;
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
            ScaleTreeNode node = nodeForValue(bucketUpperBounds[i]);
            bucketSizes[i] += subtreeUpdateCountProvider.subtreeUpdateCountFor(node);

            if (node != null && node.right != null) {
                bucketSizes[i] -= subtreeUpdateCountProvider.subtreeUpdateCountFor(node.right);
            }
        }

        return bucketSizes;
    }

    public double[] percentileValues(double[] quantiles, SubtreeUpdateCountProvider subtreeUpdateCountProvider) {
        double[] percentileValues = new double[quantiles.length];

        percentilesLoop:
        for (int i = 0; i < quantiles.length; ++i) {
            percentileValues[i] = NO_VALUE;
            ScaleTreeNode node = root;
            long nodeCount = subtreeUpdateCountProvider.subtreeUpdateCountFor(node);

            if (nodeCount == 0L) {
                continue;
            }

            long percentileCount = Math.min(Math.max((long)(nodeCount * quantiles[i]), 0L), nodeCount);

            while (node != null) {
                long leftCount = subtreeUpdateCountProvider.subtreeUpdateCountFor(node.left);
                long rightCount = subtreeUpdateCountProvider.subtreeUpdateCountFor(node.right);

                if ((nodeCount - rightCount) == percentileCount) {
                    percentileValues[i] = node.point;
                    continue percentilesLoop;
                }

                if (percentileCount <= leftCount) {
                    if (node.left != null) {
                        node = node.left;
                        nodeCount = leftCount;
                    } else {
                        percentileValues[i] = node.point;
                        continue percentilesLoop;
                    }
                } else {
                    if (node.right != null) {
                        node = node.right;
                        nodeCount = rightCount;
                        percentileCount -= leftCount;
                    } else {
                        percentileValues[i] = node.point;
                        continue percentilesLoop;
                    }
                }
            }
        }

        return percentileValues;
    }
}
