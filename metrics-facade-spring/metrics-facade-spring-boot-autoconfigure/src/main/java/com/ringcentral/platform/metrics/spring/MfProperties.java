package com.ringcentral.platform.metrics.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = MfProperties.PREFIX)
public class MfProperties {

    /**
     * Legacy / custom Metrics Facade settings namespace.
     *
     * <p>This prefix predates the Spring Boot 3/4 metrics-export property layout and is kept
     * unchanged for backward compatibility. All Metrics Facade specific settings
     * (JMX, Prometheus, Zabbix, Zabbix LLD, Telegraf) live under this prefix, e.g.
     * {@code management.metrics.export.mf.prometheus.enabled}.
     */
    public static final String PREFIX = "management.metrics.export.mf";

    /**
     * Enables/disables the Metrics Facade export auto-configuration through the custom
     * {@value #PREFIX} namespace.
     *
     * <p>Precedence with the Spring Boot native switch
     * {@code management.mf.metrics.export.enabled} (read by
     * {@code @ConditionalOnEnabledMetricsExport("mf")}): both switches default to {@code true},
     * and an explicit {@code false} on <em>either</em> switch disables Metrics Facade export.
     * In other words, "disable wins" and the default behavior (neither property set) is
     * unchanged from previous releases.
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
