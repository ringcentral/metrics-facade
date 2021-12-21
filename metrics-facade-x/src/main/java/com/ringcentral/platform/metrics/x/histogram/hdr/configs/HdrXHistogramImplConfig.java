package com.ringcentral.platform.metrics.x.histogram.hdr.configs;

import com.github.rollingmetrics.histogram.hdr.RecorderSettings;
import com.ringcentral.platform.metrics.x.histogram.configs.XHistogramImplConfig;
import com.ringcentral.platform.metrics.x.histogram.hdr.HdrXHistogramImpl;

import java.time.Duration;
import java.util.Optional;

public class HdrXHistogramImplConfig implements XHistogramImplConfig {

    public static final HdrXHistogramImpl.Type DEFAULT_TYPE = HdrXHistogramImpl.Type.NEVER_RESET;
    public static final int DEFAULT_SIGNIFICANT_DIGITS = 2;
    public static final int MAX_CHUNKS = 60;
    public static final long MIN_CHUNK_RESET_PERIOD_MS = 1000L;

    public static final HdrXHistogramImplConfig DEFAULT = new HdrXHistogramImplConfig(
        DEFAULT_TYPE,
        -1,
        -1L,
        new RecorderSettings(
            DEFAULT_SIGNIFICANT_DIGITS,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()),
        null);

    private final HdrXHistogramImpl.Type type;
    private final int chunkCount;
    private final long chunkResetPeriodMs;
    private final RecorderSettings recorderSettings;
    private final Duration snapshotTtl;

    public HdrXHistogramImplConfig(
        HdrXHistogramImpl.Type type,
        int chunkCount,
        long chunkResetPeriodMs,
        RecorderSettings recorderSettings,
        Duration snapshotTtl) {

        this.type = type;
        this.chunkCount = chunkCount;
        this.chunkResetPeriodMs = chunkResetPeriodMs;
        this.recorderSettings = recorderSettings;
        this.snapshotTtl = snapshotTtl;
    }

    public HdrXHistogramImpl.Type type() {
        return type;
    }

    public int chunkCount() {
        return chunkCount;
    }

    public long chunkResetPeriodMs() {
        return chunkResetPeriodMs;
    }

    public RecorderSettings recorderSettings() {
        return recorderSettings;
    }

    public boolean hasSnapshotTtl() {
        return snapshotTtl != null;
    }

    public Duration snapshotTtl() {
        return snapshotTtl;
    }
}
