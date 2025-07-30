package ru.kelf54.prototype;

import org.springframework.stereotype.Service;
import ru.kelf54.starter.audit.WeylandWatchingYou;

@Service
public class DemoService {

    @WeylandWatchingYou(mode = "CONSOLE")
    public String testAudit(String input) {
        return "Processed: " + input.toUpperCase();
    }

    public String demoEntryPoint(String param) {
        return testAudit(param);
    }
}
