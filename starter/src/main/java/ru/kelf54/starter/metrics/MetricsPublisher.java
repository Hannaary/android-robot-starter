package ru.kelf54.starter.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.kelf54.starter.core.CommandService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MetricsPublisher {
    private final MeterRegistry registry;
    private CommandService commandService;
    private final ConcurrentHashMap<String, AtomicInteger> authorCounters = new ConcurrentHashMap<>();
    private final Counter commandsCounter;

    public MetricsPublisher(MeterRegistry registry) {
        this.registry = registry;
        this.commandsCounter = Counter.builder("android.commands.total")
                .description("Total commands executed")
                .register(registry);
    }

    public void incrementAuthorCount(String author) {
        authorCounters.computeIfAbsent(author, k -> new AtomicInteger(0)).incrementAndGet();
        commandsCounter.increment(); // Увеличиваем общий счетчик
    }

    @PostConstruct
    public void init() {
        registerMetrics();
    }

    private void registerMetrics() {
        Gauge.builder("android.queue.size", commandService, CommandService::getQueueSize)
                .register(registry);

        Gauge.builder("android.author.commands", this, mp -> mp.getAuthorCommandsCount())
                .register(registry);
    }

    public int getAuthorCommandsCount() {
        return authorCounters.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum();
    }

    public void setCommandService(CommandService commandService) {
        this.commandService = commandService;
    }
}
