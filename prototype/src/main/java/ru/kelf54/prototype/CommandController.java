package ru.kelf54.prototype;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kelf54.starter.core.Command;
import ru.kelf54.starter.core.CommandService;

@RestController
@RequestMapping("/api/commands")
public class CommandController {
    private final CommandService commandService;

    public CommandController(CommandService commandService) {
        this.commandService = commandService;
    }

    @PostMapping
    public ResponseEntity<String> addCommand(@RequestBody @Valid Command command) {
        commandService.executeCommand(command);
        return ResponseEntity.accepted().body("Command accepted");
    }
}
