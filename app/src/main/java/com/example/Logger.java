package com.example;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Logger {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ServerLog serverlog;

    public Logger(ServerLog serverlog) {
        this.serverlog = serverlog;
    }

    public void startLogging() {
        FileHandler.Debug("Logger started at: " + serverlog.getTimestamp());
        try {
            scheduler.scheduleAtFixedRate(() -> {
                String s = serverlog.getString();
                FileHandler.Save(s);
                FileHandler.Update(s);
                FileHandler.Debug("Server log saved at: " + serverlog.getTimestamp());
            }, 0, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            FileHandler.Debug("Error in logger: " + e.getMessage());
        }
        
    }

    public void stopLogging() {
        FileHandler.Debug("Logger stopped at: " + serverlog.getTimestamp());
        scheduler.shutdown();
    }
}
