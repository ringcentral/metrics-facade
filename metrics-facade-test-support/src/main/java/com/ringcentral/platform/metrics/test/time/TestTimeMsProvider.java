package com.ringcentral.platform.metrics.test.time;

import com.ringcentral.platform.metrics.utils.TimeMsProvider;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class TestTimeMsProvider implements TimeMsProvider {

    final TestTimeNanosProvider timeNanosProvider;
    final long initialTimeNanos;
    final long initialTime;

    public TestTimeMsProvider(TestTimeNanosProvider timeNanosProvider) {
        this.timeNanosProvider = timeNanosProvider;
        this.initialTimeNanos = timeNanosProvider.timeNanos();
        this.initialTime = NANOSECONDS.toMillis(this.initialTimeNanos);
    }

    @Override
    public long timeMs() {
        return initialTime + NANOSECONDS.toMillis((timeNanosProvider.timeNanos() - initialTimeNanos));
    }
}
