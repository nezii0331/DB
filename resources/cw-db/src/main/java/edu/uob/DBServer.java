package edu.uob;

import edu.uob.DataStructure.*;
import edu.uob.CommandParser.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;

/** This class implements the DB server. */
public class DBServer {

    private static final char END_OF_TRANSMISSION = 4;
    private String storageFolderPath;
    private Databases databases;
    private Database currentDatabase;

    public static void main(String args[]) throws IOException {
        DBServer server = new DBServer();
        server.blockingListenOn(8888);
    }

    /**
     * KEEP this signature otherwise we won't be able to mark your submission correctly.
     */
    public DBServer() {
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
        try {
            // Create the database storage folder if it doesn't already exist !
            Files.createDirectories(Paths.get(storageFolderPath));
        } catch(IOException ioe) {
            System.out.println("Can't seem to create database storage folder " + storageFolderPath);
        }
        databases = new Databases(storageFolderPath);
        currentDatabase = null;
    }

    /**
     * KEEP this signature (i.e. {@code edu.uob.DBServer.handleCommand(String)}) otherwise we won't be
     * able to mark your submission correctly.
     *
     * <p>This method handles all incoming DB commands and carries out the required actions.
     */
    public String handleCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return "[ERROR] Empty command";
        }
        // Check if the command ends with a semicolon
        if (!command.trim().endsWith(";")) {
            return "[ERROR] Semicolon missing at end of line (or similar message !)";
        }
        // Remove the semicolon for parsing
        command = command.trim().substring(0, command.trim().length() - 1);
        // Check for invalid syntax
        if (command.contains("(;") || command.contains(");") || command.contains(";;")) {
            return "[ERROR] Invalid syntax";
        }
        try {
            CommandParser parser = CommandParser.createParser(command);
            if (parser == null) {
                return "[ERROR] Invalid command syntax";
            }
            boolean parseSuccess = parser.parseCommand(command);
            if (!parseSuccess) {
                return "[ERROR] Command parsing failed";
            }
            String cmdType = parser.getCommandType();
            // Check if database selection is required
            if (needsDatabaseSelected(cmdType) && databases.getCurrentDatabase() == null) {
                return "[ERROR] No database selected. Use 'USE database_name' first.";
            }

            String validationError = validateCommand(parser, command);
            if (validationError != null) {
                return validationError;
            }
            return executeCommand(parser);
        } catch (Exception e) {
            return "[ERROR] " + e.getMessage();
        }
    }

    /**
     * Validate command
     */
    private String validateCommand(CommandParser parser, String command) {
        String cmdType = parser.getCommandType();

        // Validate database and table names to PlainText rules
        if (cmdType.equals("USE")) {
            UseCommandParser useParser = (UseCommandParser) parser;
            if (!isValidPlainText(useParser.getDatabaseName())) {
                return "[ERROR] Invalid database name: must contain only letters and digits";
            }
        } else if (cmdType.equals("CREATE DATABASE")) {
            CreateCommandParser createParser = (CreateCommandParser) parser;
            if (!isValidPlainText(createParser.getDatabaseName())) {
                return "[ERROR] Invalid database name: must contain only letters and digits";
            }
        } else if (cmdType.equals("CREATE TABLE")) {
            CreateCommandParser createParser = (CreateCommandParser) parser;
            if (!isValidPlainText(createParser.getTableName())) {
                return "[ERROR] Invalid table name: must contain only letters and digits";
            }
            // Validate column names
            for (String colName : createParser.getColumnNames()) {
                if (!isValidPlainText(colName)) {
                    return "[ERROR] Invalid column name: " + colName;
                }
            }
        }
        return null; // Validation passed
    }

    /**
     * (only letters and digits)
     */
    private boolean isValidPlainText(String text) {
        return text != null && text.matches("[a-zA-Z0-9]+");
    }

    /**
     * Execute the parsed command
     */
    private String executeCommand(CommandParser parser) {
        String cmdType = parser.getCommandType();

        try {
            switch (cmdType) {
                case "USE":
                    return handleUseCommand((UseCommandParser) parser);
                case "CREATE DATABASE":
                    return handleCreateDatabaseCommand((CreateCommandParser) parser);
                case "CREATE TABLE":
                    return handleCreateTableCommand((CreateCommandParser) parser);
                case "INSERT INTO":
                    return handleInsertCommand((InsertCommandParser) parser);
                case "SELECT":
                    return handleSelectCommand((SelectCommandParser) parser);
                case "ALTER TABLE":
                    return handleAlterCommand((AlterCommandParser) parser);
                case "UPDATE":
                    return handleUpdateCommand((UpdateCommandParser) parser);
                case "DELETE":
                    return handleDeleteCommand((DeleteCommandParser) parser);
                case "DROP DATABASE":
                case "DROP TABLE":
                    return handleDropCommand((DropCommandParser) parser);
                case "JOIN":
                    return handleJoinCommand((JoinCommandParser) parser);
                default:
                    return "[ERROR] Unsupported command type: " + cmdType;
            }
        } catch (ClassCastException e) {
            return "[ERROR] Invalid command parser type";
        } catch (Exception e) {
            return "[ERROR] Command execution failed: " + e.getMessage();
        }
    }

    private String handleUseCommand(UseCommandParser parser) {
        String useResult = databases.useDatabase(parser.getDatabaseName());
        if (useResult.startsWith("[OK]")) {
            currentDatabase = databases.getCurrentDatabase();
        }
        return useResult;
    }

    private String handleCreateDatabaseCommand(CreateCommandParser parser) {
        return databases.createDatabase(parser.getDatabaseName());
    }
    private String handleCreateTableCommand(CreateCommandParser parser) {
        if (currentDatabase == null) {
            return "[ERROR] No database selected.";
        }
        return currentDatabase.createTable(
                parser.getTableName(),
                parser.getColumnNames()
        );
    }

    private String handleInsertCommand(InsertCommandParser parser) {
        if (currentDatabase == null) {
            return "[ERROR] No database selected.";
        }
        return currentDatabase.insertRow(
                parser.getTableName(),
                parser.getValues()
        );
    }

    private String handleSelectCommand(SelectCommandParser parser) {
        if (currentDatabase == null) {
            return "[ERROR] No database selected.";
        }

        String condition = parser.getCondition();
        try {
            return currentDatabase.select(
                    parser.getTableName(),
                    parser.getColumnNames(),
                    condition
            );
        } catch (Exception e) {
            return "[ERROR] Select failed: " + e.getMessage();
        }
    }

    private String handleAlterCommand(AlterCommandParser parser) {
        if (currentDatabase == null) {
            return "[ERROR] No database selected.";
        }
        try {
            return currentDatabase.alterTable(
                    parser.getTableName(),
                    parser.getAlterType(),
                    parser.getAttributeName()
            );
        } catch (Exception e) {
            return "[ERROR] Alter table failed: " + e.getMessage();
        }
    }

    private String handleDeleteCommand(DeleteCommandParser parser) {
        if (currentDatabase == null) {
            return "[ERROR] No database selected.";
        }
        try {
            return currentDatabase.delete(
                    parser.getTableName(),
                    parser.getCondition()
            );
        } catch (Exception e) {
            return "[ERROR] Delete failed: " + e.getMessage();
        }
    }

    private String handleUpdateCommand(UpdateCommandParser parser) {
        if (currentDatabase == null) {
            return "[ERROR] No database selected.";
        }
        try {
            return currentDatabase.update(
                    parser.getTableName(),
                    parser.getNameValuePare(),
                    parser.getCondition()
            );
        } catch (Exception e) {
            return "[ERROR] Update failed: " + e.getMessage();
        }
    }

    private String handleDropCommand(DropCommandParser parser) {
        if (parser.getCommandType().equals("DROP DATABASE")) {
            return databases.dropDatabase(parser.getDatabaseName());
        } else if (parser.getCommandType().equals("DROP TABLE")) {
            if (currentDatabase == null) {
                return "[ERROR] No database selected.";
            }
            return currentDatabase.dropTable(parser.getTableName());
        }
        return "[ERROR] Invalid DROP command";
    }

    private String handleJoinCommand(JoinCommandParser parser) {
        if (currentDatabase == null) {
            return "[ERROR] No database selected.";
        }
        try {
            return currentDatabase.join(
                    parser.getTableName(),
                    parser.getSecondTableName(),
                    parser.getFirstJoinColumn(),
                    parser.getSecondJoinColumn()
            );
        } catch (Exception e) {
            return "[ERROR] Join failed: " + e.getMessage();
        }
    }

    private boolean needsDatabaseSelected(String cmdType) {
        return !cmdType.equals("USE") &&
                !cmdType.equals("CREATE DATABASE") &&
                !cmdType.equals("DROP DATABASE");
    }


    //  === Methods below handle networking aspects of the project - you will not need to change these ! ===

    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.err.println("Server encountered a non-fatal IO error:");
                    e.printStackTrace();
                    System.err.println("Continuing...");
                }
            }
        }
    }

    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
             BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {

            System.out.println("Connection established: " + serverSocket.getInetAddress());
            while (!Thread.interrupted()) {
                String incomingCommand = reader.readLine();
                System.out.println("Received message: " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
