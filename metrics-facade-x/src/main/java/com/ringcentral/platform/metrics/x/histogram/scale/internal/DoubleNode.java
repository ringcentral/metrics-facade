package com.ringcentral.platform.metrics.x.histogram.scale.internal;

public class DoubleNode implements MultiNode {

    private Chunk chunk1;
    private ScaleTreeNode node1;

    private Chunk chunk2;
    private ScaleTreeNode node2;

    public DoubleNode(Chunk chunk1, Chunk chunk2) {
        this.chunk1 = chunk1;
        this.node1 = chunk1.tree().root;

        this.chunk2 = chunk2;
        this.node2 = chunk2.tree().root;
    }

    public void reset(Chunk chunk1, Chunk chunk2) {
        this.chunk1 = chunk1;
        this.node1 = chunk1.tree().root;

        this.chunk2 = chunk2;
        this.node2 = chunk2.tree().root;
    }

    @Override
    public long point() {
        if (node1 != null) {
            return node1.point;
        }

        if (node2 != null) {
            return node2.point;
        }

        return 0L;
    }

    @Override
    public boolean isNull() {
        return node1 == null && node2 == null;
    }

    @Override
    public long subtreeUpdateCount() {
        long result = 0L;

        if (node1 != null) {
            result += chunk1.subtreeUpdateCountFor(node1);
        }

        if (node2 != null) {
            result += chunk2.subtreeUpdateCountFor(node2);
        }

        return result;
    }

    @Override
    public boolean hasLeft() {
        return (node1 != null && node1.left != null) || (node2 != null && node2.left != null);
    }

    @Override
    public void toLeft() {
        if (node1 != null) {
            node1 = node1.left;
        }

        if (node2 != null) {
            node2 = node2.left;
        }
    }

    @Override
    public long leftSubtreeUpdateCount() {
        long result = 0L;

        if (node1 != null && node1.left != null) {
            result += chunk1.subtreeUpdateCountFor(node1.left);
        }

        if (node2 != null && node2.left != null) {
            result += chunk2.subtreeUpdateCountFor(node2.left);
        }

        return result;
    }

    @Override
    public boolean hasRight() {
        return (node1 != null && node1.right != null) || (node2 != null && node2.right != null);
    }

    @Override
    public void toRight() {
        if (node1 != null) {
            node1 = node1.right;
        }

        if (node2 != null) {
            node2 = node2.right;
        }
    }

    @Override
    public long rightSubtreeUpdateCount() {
        long result = 0L;

        if (node1 != null && node1.right != null) {
            result += chunk1.subtreeUpdateCountFor(node1.right);
        }

        if (node2 != null && node2.right != null) {
            result += chunk2.subtreeUpdateCountFor(node2.right);
        }

        return result;
    }
}
