package ru.kelf54.starter.core;

import org.springframework.stereotype.Service;
import ru.kelf54.starter.exception.QueueFullException;
import ru.kelf54.starter.metrics.MetricsPublisher;

import java.util.concurrent.*;

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
            // Синхронная обработка критических команд
            processCommand(command);
        } else {
            // Асинхронная обработка обычных команд с обработкой переполнения очереди
            try {
                executor.execute(() -> processCommand(command));
            } catch (RejectedExecutionException ex) {
                throw new QueueFullException("Queue is full, cannot accept more commands");
            }
        }
    }

    private void processCommand(Command command) {
        // Логика выполнения команды
        System.out.println("Executing: " + command.description());
        try {
            Thread.sleep(500); // намеренная задержка, чтобы продемонстрировать переполнение очереди
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Обновляем метрики
        metricsPublisher.incrementAuthorCount(command.author());
    }

    public int getQueueSize() {
        return commandQueue.size();
    }
}
