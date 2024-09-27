package com.ringcentral.platform.metrics.test.time;

import com.ringcentral.platform.metrics.utils.TimeMsProvider;

import static com.ringcentral.platform.metrics.utils.TimeUnitUtils.NANOS_PER_MS;

public class TestTimeMsProvider implements TimeMsProvider {

    final TestTimeNanosProvider timeNanosProvider;
    final long initialTimeNanos;

    public TestTimeMsProvider(TestTimeNanosProvider timeNanosProvider) {
        this.timeNanosProvider = timeNanosProvider;
        this.initialTimeNanos = timeNanosProvider.timeNanos();
    }

    @Override
    public long timeMs() {
        return stableTimeMs();
    }

    @Override
    public long stableTimeMs() {
        return (timeNanosProvider.timeNanos() - initialTimeNanos) / NANOS_PER_MS;
    }
}
