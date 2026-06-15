package com.example;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Logger {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ServerLog serverlog;
    private String time;

    public Logger(ServerLog serverlog) {
        this.serverlog = serverlog;
    }

    public void startLogging() {
        time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        try {
            scheduler.scheduleAtFixedRate(() -> {
                time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                String s = serverlog.getString();
                FileHandler.Save(s);
                FileHandler.Update(s);
            }, 0, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            System.err.println("ERROR in logging task: " + e.getMessage());
            e.printStackTrace();
            try {
                FileHandler.Debug("ERROR: " + e.getMessage());
            } catch (Exception ex) {
                System.err.println("Debug also failed: " + ex.getMessage());
            }
        }
    }

    public void stopLogging() {
        scheduler.shutdown();
    }
}
