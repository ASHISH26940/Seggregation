# SoDChecker

## Overview
SoDChecker (Segregation of Duties Checker) is a robust Java-based compliance tool designed to identify and report potential conflicts in role-based access control systems. This tool is particularly useful for Oracle Fusion environments where complex role hierarchies and access entitlements require careful monitoring to maintain proper segregation of duties.

The application analyzes relationships between users, roles, privileges, and entitlements to detect instances where a single user might have access to conflicting functions, which could pose security or compliance risks.

## Key Features

* **Comprehensive Data Processing**: Reads user, role, privilege, and hierarchy data from Excel files
* **Advanced Graph Construction**: Builds an integrated Employee-Role and Role-Hierarchy graph
* **Intelligent Conflict Detection**: Identifies SoD violations based on role combinations and hierarchical relationships
* **Customizable Analysis**: Configurable to detect various types of conflicts
* **Detailed Reporting**: Outputs comprehensive violation reports to CSV format for further analysis

## Prerequisites

* Java 11 or later
* Apache Maven 3.6+ (for dependency management)
* Required Java libraries:
  * Apache POI 5.0+ (for Excel file processing)
  * Apache Commons IO 2.8+ (for file operations)
  * SLF4J 1.7+ (for logging)

## Project Structure

```
SoDChecker/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── sodchecker/
│   │   │   │   └── SoDChecker.java        # Main application class
│   │   │   ├── detection/
│   │   │   │   └── SoDViolationDetector.java  # Conflict detection logic
│   │   │   ├── models/
│   │   │   │   └── EmployeeGraph.java     # Data structure for relationships
│   │   │   ├── utils/
│   │   │   │   ├── ExcelReader.java       # Excel file processor
│   │   │   │   └── OutputGenerator.java   # Report generation utility
│   │   ├── resources/
│   │   │   ├── data/                     # Input data directory
│   │   │   │   ├── userDetails.xlsx      # User information
│   │   │   │   ├── userRoleMapping.xlsx  # User-role assignments
│   │   │   │   ├── roleMasterDetails.xlsx # Role definitions
│   │   │   │   ├── roleToRole.xlsx       # Role hierarchy relationships
│   │   │   │   └── pvlgsMaster.xlsx      # Privilege definitions
│   │   │   └── log4j2.xml                # Logging configuration
│   └── test/                             # Unit and integration tests
├── pom.xml                               # Maven configuration
├── README.md                             # Project documentation
└── LICENSE                               # License information
```

## Installation

1. Clone the repository:
```sh
git clone https://github.com/yourusername/SoDChecker.git
cd SoDChecker
```

2. Build the project using Maven:
```sh
mvn clean package
```

3. Run the application:
```sh
java -jar target/sodchecker-1.0.jar
```

Alternatively, run directly with Maven:
```sh
mvn exec:java -Dexec.mainClass="sodchecker.SoDChecker"
```

## Usage Guide

### Input File Requirements

1. **userDetails.xlsx**: Contains user information
   - Required columns: `HCM_EMP_PERSON_ID`, `USER_DISPLAY_NAME`, `USER_ID` (plus others)

2. **userRoleMapping.xlsx**: Maps users to their assigned roles
   - Required columns: `ROLE_ID`, `MEMBERSHIP_ID`, `USER_ID`, effective dates

3. **roleMasterDetails.xlsx**: Defines all roles in the system
   - Required columns: `ROLE_ID`, `ROLE_NAME`, `ROLE_TYPE_CODE`

4. **roleToRole.xlsx**: Defines role hierarchy relationships
   - Required columns: `MEMBERSHIP_ID`, `CHILD_ROLE_ID`, `PARENT_ROLE_ID`

5. **pvlgsMaster.xlsx**: Lists all privileges
   - Required columns: `NAME`, `PRIVILEGE_ID`, `DESCRIPTION`

### Configuration

You can customize the conflict detection rules by modifying the `SoDViolationDetector` class:

```java
// Example: Configure the detector to use specific conflict rules
SoDViolationDetector detector = new SoDViolationDetector(graph);
detector.addConflictRule("Create", "Approve");
detector.addConflictRule("Request", "Authorize");
```

### Running the Analysis

1. Place your Excel files in the `src/main/resources/data/` directory
2. Run the application using one of the methods above
3. Check the console for progress and any warnings
4. Review the generated `output.csv` file for detailed violation reports

## Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| Excel numeric values appearing as scientific notation | Modify `ExcelReader.java` to use `Cell.setCellType(CellType.STRING)` before reading |
| "⚠ Skipping invalid entry" messages | Check input files for missing data or format issues |
| OutOfMemoryError | Increase JVM heap size: `java -Xmx4g -jar target/sodchecker-1.0.jar` |
| Missing column errors | Ensure Excel files contain all required columns with exact spelling |

### Logs

The application uses SLF4J for logging. Check the log file at `logs/sodchecker.log` for detailed information about the execution process.

## Advanced Usage

### Custom Conflict Rules

You can create custom conflict detection rules by extending the `SoDViolationDetector` class:

```java
public class CustomSoDDetector extends SoDViolationDetector {
    public CustomSoDDetector(EmployeeGraph graph) {
        super(graph);
        // Add your custom detection logic here
    }
    
    @Override
    public List<String> detectConflicts() {
        // Implement your custom conflict detection algorithm
    }
}
```

### Performance Optimization

For large data sets, consider:
1. Increasing JVM heap size
2. Using the batch processing option: `SoDChecker --batch-size=1000`
3. Enabling parallel processing: `SoDChecker --parallel`

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the `LICENSE` file for details.

## Acknowledgments

* Oracle Fusion documentation for SoD concepts and architecture
* Apache POI developers for the excellent Excel processing library
* All contributors who have helped improve this tool

## Contact

Project Maintainer: [Your Name](mailto:your.email@example.com)

Project Repository: [https://github.com/yourusername/SoDChecker](https://github.com/yourusername/SoDChecker)