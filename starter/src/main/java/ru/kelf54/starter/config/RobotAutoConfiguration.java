package ru.kelf54.starter.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.kelf54.starter.audit.AuditAspect;
import ru.kelf54.starter.core.CommandService;
import ru.kelf54.starter.metrics.MetricsPublisher;

@Configuration
public class RobotAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CommandService commandService(MetricsPublisher metricsPublisher) {
        CommandService service = new CommandService(metricsPublisher);
        metricsPublisher.setCommandService(service);
        return service;
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditAspect auditAspect() {
        return new AuditAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricsPublisher metricsPublisher(MeterRegistry registry) {
        return new MetricsPublisher(registry);
    }
}
