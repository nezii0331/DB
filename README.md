# Database Server

## Overview
This project implements a **relational database server** from scratch. The server is capable of processing SQL-like commands, managing persistent storage, and handling multiple databases and tables. The implementation follows the specifications provided in the assignment brief and supports key SQL operations such as `CREATE`, `INSERT`, `SELECT`, `UPDATE`, `DELETE`, `DROP`, `ALTER`, and `JOIN`.

## Features
- **Database Management**: Supports creation (`CREATE DATABASE`) and deletion (`DROP DATABASE`) of databases.
- **Table Management**: Allows table creation (`CREATE TABLE`), modification (`ALTER TABLE`), and deletion (`DROP TABLE`).
- **Data Manipulation**:
    - **Insertion**: `INSERT INTO table VALUES (...)`
    - **Selection**: `SELECT * FROM table WHERE condition`
    - **Update**: `UPDATE table SET column=value WHERE condition`
    - **Deletion**: `DELETE FROM table WHERE condition`
- **Joins**: `JOIN table1 AND table2 ON column1 AND column2`
- **Persistent Storage**: Uses tab-separated files to store data.
- **Case Insensitivity**: Database and table names are stored in lowercase, while column names retain their case.
- **Error Handling**: Provides meaningful error messages for invalid queries.

## System Architecture
### 1. Server & Client Communication
- `DBServer.java` handles incoming SQL commands and interacts with `Databases.java` to execute queries.
- `DBClient.java` allows users to send commands to the server.

### 2. Database & Table Management
- `Databases.java` manages multiple databases, while `DatabaseManager.java` handles file system interactions.
- `Database.java` manages tables within a single database.
- `Table.java` stores data and maintains structure.

### 3. Query Parsing & Execution
- `CommandParser.java` and its subclasses parse SQL commands.
- `QueryExecutor.java` executes parsed commands on `Database` and `Table` objects.
- `ConditionParser.java` evaluates `WHERE` conditions.

## Installation & Setup
### Prerequisites
- Java 17+
- Maven

### Compilation & Execution
1. **Compile the project**
   ```sh
   mvn clean compile
   ```
2. **Run the server**
   ```sh
   mvn exec:java@server
   ```
3. **Run the client**
   ```sh
   mvn exec:java@client
   ```

## Usage Example
```sql
CREATE DATABASE markbook;
USE markbook;
CREATE TABLE marks (name, mark, pass);
INSERT INTO marks VALUES ('Simon', 65, TRUE);
SELECT * FROM marks;
UPDATE marks SET mark = 38 WHERE name == 'Simon';
DELETE FROM marks WHERE mark < 40;
DROP TABLE marks;
DROP DATABASE markbook;
```

## Error Handling
The server returns structured responses for errors, such as:
```sql
SELECT * FROM unknown_table;
[ERROR]: Table does not exist.
```

## Testing
- `ExampleDBTests.java` provides unit tests to validate SQL command execution.
- Example test cases are defined in `example-transcript.docx`.

## Known Issues & Future Improvements
- Enhance SQL parsing for more complex queries.
- Improve concurrency handling for multiple clients.
- Optimize file storage for faster query execution.

## Author
- **Negi Chen**
- University of XYZ, Database Systems Assignment

## License
This project is for educational purposes and follows university submission policies.


## Project Structure
```bash
. 📁DB 
└─ 📁src            
    ├── src/main/java/edu/uob/    # Main Java source files
    │   ├── DBServer.java         # Main database server
    │   ├── QueryExecutor.java    # Query execution logic
    │   ├── Database.java         # Handles database operations
    │   ├── Table.java            # Represents database tables
    │   ├── Row.java              # Represents individual rows in tables
    │   ├── parsers/              # Command parsers (SELECT, INSERT, etc.)
    │   │   ├── CommandParser.java  # Base parser class
    │   │   ├── UseCommandParser.java  # Parses USE commands
    │   │   ├── CreateCommandParser.java  # Parses CREATE commands
    │   │   ├── DropCommandParser.java  # Parses DROP commands
    │   │   ├── InsertCommandParser.java  # Parses INSERT commands
    │   │   ├── SelectCommandParser.java  # Parses SELECT commands
    │   │   ├── UpdateCommandParser.java  # Parses UPDATE commands
    │   │   ├── DeleteCommandParser.java  # Parses DELETE commands
    │   │   ├── AlterCommandParser.java  # Parses ALTER TABLE commands
    │   │   ├── JoinCommandParser.java  # Parses JOIN commands
    │   │   ├── ConditionParser.java  # Parses WHERE conditions
    │   │
    │   ├── utils/                # Utility functions
    │
    ├── src/test/java/edu/uob/    # Unit tests
    │   ├── ExampleDBTests.java   # Sample test cases
    │
    ├── databases/                # Folder storing database files
    ├── docs/                     # Documentation
    ├── pom.xml                   # Maven build configuration
    └── README.md                 # Project documentation
```

