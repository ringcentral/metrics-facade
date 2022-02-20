package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.internal;

import com.ringcentral.platform.metrics.defaultImpl.histogram.scale.internal.ScaleTree.SubtreeUpdateCountProvider;
import com.ringcentral.platform.metrics.scale.Scale;
import org.junit.Test;

import static com.ringcentral.platform.metrics.scale.LinearScaleBuilder.linear;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ScaleTreeTest {

    @Test
    public void nodeForValue() {
        ScaleTree tree = ScaleTree.of(
            linear().steps(10, 9).withInf().build(),
            1,
            new long[] {},
            4,
            true,
            0L);

        assertThat(tree.nodeForValue(-2).point, is(0L));
        assertThat(tree.nodeForValue(-1).point, is(0L));
        assertThat(tree.nodeForValue(0).point, is(0L));

        for (int i = 0; i < 10; ++i) {
            for (int j = 1; j <= 10; ++j) {
                long expected = (i + 1) * 10L;

                if (expected > 90) {
                    expected = Long.MAX_VALUE;
                }

                assertThat(tree.nodeForValue(i * 10 + j).point, is(expected));
            }
        }
    }

    @Test
    public void buckets() {
        Scale scale = linear().steps(10, 9).withInf().build();
        long[] bucketUpperBounds = scale.points().stream().mapToLong(l -> l).toArray();

        ScaleTree tree = ScaleTree.of(
            scale,
            1,
            bucketUpperBounds,
            3,
            true,
            0L);

        SubtreeUpdateCountProvider subtreeUpdateCountProvider = node -> node.subtreeUpdateCount.sum();
        long[] bucketSizes = tree.bucketSizes(bucketUpperBounds, subtreeUpdateCountProvider);

        for (long bucketSize : bucketSizes) {
            assertThat(bucketSize, is(0L));
        }

        tree.traversePostOrder(node -> {
            node.subtreeUpdateCount.increment();

            if (node.left != null) {
                node.subtreeUpdateCount.add(node.left.subtreeUpdateCount.sum());
            }

            if (node.right != null) {
                node.subtreeUpdateCount.add(node.right.subtreeUpdateCount.sum());
            }
        });

        bucketSizes = tree.bucketSizes(bucketUpperBounds, subtreeUpdateCountProvider);

        assertThat(bucketSizes[0], is(1L));
        assertThat(bucketSizes[1], is(2L));
        assertThat(bucketSizes[2], is(3L));
        assertThat(bucketSizes[3], is(4L));
        assertThat(bucketSizes[4], is(5L));
        assertThat(bucketSizes[5], is(6L));
        assertThat(bucketSizes[6], is(7L));
        assertThat(bucketSizes[7], is(8L));
        assertThat(bucketSizes[8], is(9L));
        assertThat(bucketSizes[9], is(10L));
        assertThat(bucketSizes[10], is(11L));
        assertThat(tree.bucketSizes(new long[] { -1L }, node -> node.subtreeUpdateCount.sum())[0], is(0L));
        assertThat(tree.bucketSizes(new long[] { 250L }, node -> node.subtreeUpdateCount.sum())[0], is(10L));

        tree.traversePostOrder(node -> node.subtreeUpdateCount.reset());
        bucketSizes = tree.bucketSizes(bucketUpperBounds, subtreeUpdateCountProvider);

        for (long bucketSize : bucketSizes) {
            assertThat(bucketSize, is(0L));
        }

        scale = linear().steps(90, 1).build();

        tree = ScaleTree.of(
            scale,
            9,
            null,
            3,
            true,
            0L);

        tree.traversePostOrder(node -> {
            node.subtreeUpdateCount.increment();

            if (node.left != null) {
                node.subtreeUpdateCount.add(node.left.subtreeUpdateCount.sum());
            }

            if (node.right != null) {
                node.subtreeUpdateCount.add(node.right.subtreeUpdateCount.sum());
            }
        });

        assertThat(tree.bucketSizes(new long[] { -1L }, node -> node.subtreeUpdateCount.sum())[0], is(0L));
        assertThat(tree.bucketSizes(new long[] { 50L }, node -> node.subtreeUpdateCount.sum())[0], is(6L));
        assertThat(tree.bucketSizes(new long[] { 90L }, node -> node.subtreeUpdateCount.sum())[0], is(10L));
        assertThat(tree.bucketSizes(new long[] { 125L }, node -> node.subtreeUpdateCount.sum())[0], is(10L));
        assertThat(tree.bucketSizes(new long[] { Long.MAX_VALUE }, node -> node.subtreeUpdateCount.sum())[0], is(10L));
    }
}