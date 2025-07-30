package ru.kelf54.starter.core;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record Command(
        @Size(max = 1000) String description,
        Priority priority,
        @Size(max = 100) String author,
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$") String time
) {
    public enum Priority { COMMON, CRITICAL }
}
