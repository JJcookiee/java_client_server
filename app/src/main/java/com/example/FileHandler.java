package com.example;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
/**
 * FileHandler class
 */
final class FileHandler {
    
    static FileHandler instance = new FileHandler();

    PrintStream slp_print;
    PrintStream debug_print;

    /**
     * FileHandler constructor
     * Creates PrintStream for server_log_persistent
     * Creates PrintStream for debug_log
     * @exception IOExecption error creating file
     */
    FileHandler() {
        try{
           slp_print = new PrintStream(new FileOutputStream("files/server_log_persistent.txt", true)); 
        } catch (IOException e) {
            Debug("Error creating persistent log file: " + e.getMessage());
        }

        try {
            debug_print = new PrintStream(new FileOutputStream("files/debug_log.txt", true));
        } catch (IOException e) {
            Debug("Error creating debug log file: " + e.getMessage());
        }
    }

    /**
     * Takes a string and prints it to the debug log with the time
     * @param s
     */
    static void Debug(String s) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        instance.debug_print.println(time + ": \n" + s);
    }

    /**
     * Takes a string and saves it to a new file with the date and time and counter if multiple saved per minute
     * @param s
     * @exception IOExeception error saving file
     */
    static void Save(String s) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss"));
        int counter = 0;
        File newFile = new File("files/server_log_" + time + ".txt");
        while (newFile.exists()) {
            counter++;
            newFile = new File("files/server_log_" + time + "_" + counter + ".txt");
        }
        try {
            newFile.createNewFile();
            try (PrintStream newPrint = new PrintStream(new FileOutputStream(newFile, true))) {
                newPrint.println(s);
            }
        } catch (IOException e) {
            Debug("Error saving log file: " + e.getMessage());
        }
    }

    /**
     * Takes a string and adds it to server_log_persistent
     * @param s
     */
    static void Update(String s) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        instance.slp_print.println(time + ": \n" + s);
    }
}
