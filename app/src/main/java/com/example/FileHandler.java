package com.example;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalTime;

final class FileHandler {
    
    static FileHandler instance = new FileHandler();

    PrintStream slp_print;
    PrintStream debug_print;


    FileHandler() {
        File logFile = new File("logs/server_log_persistent.txt");
        if (!logFile.exists()) {
            try {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
                slp_print = new PrintStream(new FileOutputStream(logFile, true));
            } catch (Exception e) {
                Debug("Error creating log file: " + e.getMessage());
            }
        }

        File debugFile = new File("logs/debug_log.txt");
        if (!debugFile.exists()) {
            try {
                debugFile.getParentFile().mkdirs();
                debugFile.createNewFile();
                debug_print = new PrintStream(new FileOutputStream(debugFile, true));
            } catch (Exception e) {
                //Debug("Error creating debug log file: " + e.getMessage()); bit redundant dont you think
            }
        }
    }

    static void Debug(String s) {
        LocalTime time = LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
        instance.debug_print.println(time + ": \n" + s);
    }

    static void Save(String s) {
        LocalTime time = LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
        int counter = 0;
        File newFile = new File("logs/server_log_" + time + ".txt");
        while (newFile.exists()) {
            counter++;
            newFile = new File("logs/server_log_" + time + "_" + counter + ".txt");
        }
        try {
            newFile.createNewFile();
            PrintStream newPrint = new PrintStream(new FileOutputStream(newFile, true));
            newPrint.println(s);
            newPrint.close();
        } catch (Exception e) {
            Debug("Error saving log file: " + e.getMessage());
        }
    }

    static void Update(String s) {
        LocalTime time = LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
        instance.slp_print.println(time + ": \n" + s);
    }
}
