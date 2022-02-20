package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.internal;

import java.util.List;

public class DefaultMultiNode implements MultiNode {

    private final List<? extends Chunk> chunks;
    private final int chunkCount;
    private final ScaleTreeNode[] nodes;

    public DefaultMultiNode(List<? extends Chunk> chunks) {
        this.chunks = chunks;
        this.chunkCount = chunks.size();
        this.nodes = new ScaleTreeNode[chunkCount];

        for (int i = 0; i < chunkCount; ++i) {
            this.nodes[i] = chunks.get(i).tree().root;
        }
    }

    @Override
    public long point() {
        for (int i = 0; i < chunkCount; ++i) {
            if (nodes[i] != null) {
                return nodes[i].point;
            }
        }

        return 0L;
    }

    @Override
    public boolean isNull() {
        for (int i = 0; i < chunkCount; ++i) {
            if (nodes[i] != null) {
                return false;
            }
        }

        return true;
    }

    @Override
    public long subtreeUpdateCount() {
        long result = 0L;

        for (int i = 0; i < chunkCount; ++i) {
            if (nodes[i] != null) {
                result += chunks.get(i).subtreeUpdateCountFor(nodes[i]);
            }
        }

        return result;
    }

    @Override
    public boolean hasLeft() {
        for (int i = 0; i < chunkCount; ++i) {
            if (nodes[i] != null && nodes[i].left != null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void toLeft() {
        for (int i = 0; i < chunkCount; ++i) {
            if (nodes[i] != null) {
                nodes[i] = nodes[i].left;
            }
        }
    }

    @Override
    public long leftSubtreeUpdateCount() {
        long result = 0L;

        for (int i = 0; i < chunkCount; ++i) {
            if (nodes[i] != null && nodes[i].left != null) {
                result += chunks.get(i).subtreeUpdateCountFor(nodes[i].left);
            }
        }

        return result;
    }

    @Override
    public boolean hasRight() {
        for (int i = 0; i < chunkCount; ++i) {
            if (nodes[i] != null && nodes[i].right != null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void toRight() {
        for (int i = 0; i < chunkCount; ++i) {
            if (nodes[i] != null) {
                nodes[i] = nodes[i].right;
            }
        }
    }

    @Override
    public long rightSubtreeUpdateCount() {
        long result = 0L;

        for (int i = 0; i < chunkCount; ++i) {
            if (nodes[i] != null && nodes[i].right != null) {
                result += chunks.get(i).subtreeUpdateCountFor(nodes[i].right);
            }
        }

        return result;
    }
}
