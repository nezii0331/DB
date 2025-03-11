package main.java.edu.uob.CommandParser;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateCommandParser extends CommandParser {
    private String databaseName;
    private String TableName;
    private List<String> columnName;

    public

    @override
    public boolean parseCommand(String command){
        if(command == "CREATE DATABASE") {
            return parseCreateDatabase(command);
        } else if(command == ""){
            return parseCreateTable(command);
        }
        return false;
    }

    public parseCreateDatabase(){
        commandType = ""
    }


    public String[] getColumnName(){
        return columnName.toArray(new String);
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public
}
