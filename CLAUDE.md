# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java Spring Boot application focused on expression processing and tree building for monitoring/metrics systems. The project processes complex expressions, builds hierarchical tree structures, and manages filtering conditions for data analysis.

## Development Environment

### Technology Stack
- **Java**: JDK 1.8
- **Spring Boot**: 2.7.18
- **Database**: Oracle (using MyBatis for ORM)
- **JSON Library**: Alibaba FastJSON 1.2.83
- **Build Tool**: Maven

### Key Dependencies
- Spring Boot Web (REST APIs)
- MyBatis Spring Boot Starter (database access)
- Lombok (code generation)
- FastJSON (JSON processing)

## Build and Development Commands

### Maven Commands
```bash
# Build the project
mvn clean compile

# Run tests
mvn test

# Package the application
mvn package

# Run the application
mvn spring-boot:run

# Skip tests during build
mvn clean package -DskipTests
```

### Running the Application
The main application class is `com.example.TreeBuilderExample`. It can be run using:
```bash
mvn spring-boot:run
```

## Architecture and Key Components

### Core Modules

1. **Expression Processing** (`com.example.expression.*`)
   - `ExpressionGrouper`: Groups expressions by sourceId and adds parentheses
   - `FormulaParserImpl`: Parses complex formulas and expressions
   - `FormulaVisitorImpl`: Visitor pattern implementation for formula processing

2. **Tree Building** (`com.example.service.*`)
   - `TreeBuilderService`: Core service for building hierarchical tree structures
   - Manages complex expression parsing and tree construction

3. **Entity Layer** (`com.example.entity.*`)
   - `MonChildrenExpr`: Main entity for multi-level tree expressions
   - `FilterCondition`: Represents filtering conditions in expressions
   - `Element`: Base element/entity structure

4. **Data Access** (`com.example.dao.*`)
   - `MonChildrenExprDao`: Data access object for expression entities
   - Uses MyBatis XML mappings for Oracle database operations

5. **Web Layer** (`com.example.controller.*`)
   - `TreeBuilderController`: REST endpoints for tree building operations
   - `/api/tree` endpoints for expression processing

6. **Utilities** (`com.example.util.*`)
   - `ExpressionProcessor`: Handles expression processing utilities
   - `SpringContextUtil`: Spring context utilities

### Database Schema

The application works with Oracle database and includes:
- **MON_CHILDREN_EXPR**: Main table for storing tree expressions
- Backup tables for various metrics and conditions
- MyBatis XML mappings located in `confignew/src/main/resources/mapper/`

## Code Standards and Conventions

### Java Coding Standards
- Use JDK 1.8 syntax features
- Follow Spring Boot 2.7 conventions
- Use Lombok for boilerplate code reduction
- Apply Oracle SQL syntax rules for database queries

### JSON Processing
- Use Alibaba FastJSON for all JSON operations
- Follow FastJSON serialization/deserialization patterns

### Database Access
- Use MyBatis for all database operations
- Write Oracle-compliant SQL in XML mapper files
- Use parameterized queries to prevent SQL injection

## Project Structure

```
├── confignew/                    # Main Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── controller/   # REST controllers
│   │   │   │   ├── dao/         # Data access objects
│   │   │   │   ├── entity/      # Entity classes
│   │   │   │   ├── expression/  # Expression processing
│   │   │   │   ├── filter/      # Filter processing
│   │   │   │   ├── service/     # Business logic
│   │   │   │   └── util/        # Utilities
│   │   │   └── resources/
│   │   │       └── mapper/      # MyBatis XML mappings
│   │   └── test/                # Test files
│   └── pom.xml                  # Maven configuration
├── 202504/, 202506/, 202507/    # Version-specific modules
├── evlgz/                       # Evaluation-related code
└── 工作台/                      # Workspace directory
```

## Development Guidelines

### Expression Processing
- Expressions are processed using parser/visitor pattern
- Group expressions by sourceId for proper hierarchical structure
- Use parentheses to ensure proper operator precedence

### Tree Building
- Build hierarchical structures from flat expression data
- Handle complex parent-child relationships
- Support multiple origin types and system IDs

### Database Operations
- Use batch operations for better performance
- Leverage Oracle-specific features like SYSDATE
- Implement proper transaction management

## Testing

- Test classes are located in `confignew/src/test/java/`
- Use Spring Boot Test framework
- Example test: `TreeBuilderServiceTest.java`

## Special Considerations

- The project deals with complex mathematical expressions and hierarchical data structures
- Oracle database syntax must be followed for all SQL operations
- Expression processing requires careful handling of operator precedence and grouping
- The system appears to be used for monitoring/metrics data analysis
- 你是一个资深的软件工程师