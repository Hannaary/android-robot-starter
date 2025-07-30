package ru.kelf54.starter.core;

import org.springframework.stereotype.Service;
import ru.kelf54.starter.exception.QueueFullException;
import ru.kelf54.starter.metrics.MetricsPublisher;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class CommandService {
    private static final int QUEUE_CAPACITY = 100;
    private final BlockingQueue<Command> commandQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private final Executor executor = new ThreadPoolExecutor(
            1, 1, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(QUEUE_CAPACITY)
    );

    private final MetricsPublisher metricsPublisher;

    public CommandService(MetricsPublisher metricsPublisher) {
        this.metricsPublisher = metricsPublisher;
    }

    public void executeCommand(Command command) {
        if (command.priority() == Command.Priority.CRITICAL) {
            processCommand(command);
        } else {
            if (commandQueue.size() >= QUEUE_CAPACITY) {
                throw new QueueFullException("Command queue overflow");
            }
            executor.execute(() -> processCommand(command));
        }
    }

    private void processCommand(Command command) {
        // Логика выполнения команды
        System.out.println("Executing: " + command.description());

        // Обновляем метрики
        metricsPublisher.incrementAuthorCount(command.author());
    }

    public int getQueueSize() {
        return commandQueue.size();
    }
}
