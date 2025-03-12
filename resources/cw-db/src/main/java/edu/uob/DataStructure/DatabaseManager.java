package edu.uob.DataStructure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages multiple databases (Create, Drop)
 */

public class DatabaseManager {
    private final String storageFolderPath;
    private final Map<String, Database> databases;

    public DatabaseManager(String storageFolderPath) {
        this.storageFolderPath = storageFolderPath;
        this.databases = new HashMap<>();
        loadDatabases();
    }

    /**
     * Load all existing databases from storage
     */
    private void loadDatabases() {
        File storageFolder = new File(storageFolderPath);
        if (!storageFolder.exists()) {
            try {
                Files.createDirectories(storageFolder.toPath());
            } catch (IOException e) {
                System.err.println("Failed to create storage directory: " + e.getMessage());
                return;
            }
        }

        File[] databaseFolders = storageFolder.listFiles(File::isDirectory);

        if (databaseFolders != null) {
            for (File folder : databaseFolders) {
                try {
                    String dbName = folder.getName().toLowerCase();
                    databases.put(dbName, new Database(dbName, folder.getPath()));
                } catch (Exception e) {
                    System.err.println("Error loading database " + folder.getName() + ": " + e.getMessage());
                    // Continue processing the next database
                }
            }
        }
    }

    /**
     * Create a new database
     */
    public String createDatabase(String name) {
        name = name.toLowerCase();
        if (databases.containsKey(name)) {
            return "[ERROR] Database " + name + " already exists";
        }

        String dbPath = storageFolderPath + File.separator + name;
        try {
            Files.createDirectories(Paths.get(dbPath));
            databases.put(name, new Database(name, dbPath));
            return "[OK]";
        } catch (IOException e) {
            return "[ERROR] Failed to create database: " + e.getMessage();
        }
    }

    /**
     * Drop (delete) a database
     */
    public String dropDatabase(String name) {
        name = name.toLowerCase();
        if (!databases.containsKey(name)) {
            return "[ERROR] Database " + name + " does not exist";
        }

        String dbPath = storageFolderPath + File.separator + name;
        File dbDirectory = new File(dbPath);

        try {
            File[] files = dbDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.delete()) {
                        return "[ERROR] Failed to delete file: " + file.getName();
                    }
                }
            }

            if (dbDirectory.delete()) {
                databases.remove(name);
                return "[OK]";
            } else {
                return "[ERROR] Failed to delete database folder";
            }
        } catch (Exception e) {
            return "[ERROR] Error dropping database: " + e.getMessage();
        }
    }

    /**
     * Get a database by name
     */

    public boolean databaseExists(String name) {
        return databases.containsKey(name.toLowerCase());
    }
    public Database getDatabase(String name) {
        return databases.get(name.toLowerCase());
    }

}