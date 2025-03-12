package edu.uob.DataStructure;

/**
 * Handles switching between databases
 */
public class Databases{
    private Database currentDatabase;
    private final DatabaseManager dbManager;

    public Databases(String storageFolderPath){
        this.dbManager = new DatabaseManager(storageFolderPath);
        this.currentDatabase = null;
    }

    /**
     * Switch to a specific database
     */
    public String useDatabase(String name){
        if (! dbManager.databaseExists(name)) {
            return "[ERROR] Database does not exist: " + name;
        }
        currentDatabase = dbManager.getDatabase(name);
        return "[OK] Switched to database " + name;
    }
    public Database getCurrentDatabase(){
        return currentDatabase;
    }
    public String createDatabase(String name) {
        return dbManager.createDatabase(name);
    }

    public String dropDatabase(String databaseName) {
        if (currentDatabase != null && currentDatabase.getName().equalsIgnoreCase(databaseName)) {
            currentDatabase = null;
        }
        return dbManager.dropDatabase(databaseName);
    }
    /**
     * Execute an SQL-like query on the current database
     */
    public String executeQuery(String query){
        if(currentDatabase == null){
            return "[ERROR] No database selected.";
        }
        return currentDatabase.executeQuery(query);
    }

}