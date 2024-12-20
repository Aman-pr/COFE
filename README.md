# COFE (Custom Command Line File Explorer)

COFE is a lightweight Java-based command-line utility that emulates basic shell functionality. It provides users with tools to navigate the file system, read files, and execute commands directly from the terminal.

## Features

1. **Echo Command**: Mimics shell's `echo`, allowing users to print text to the console.
   - Supports escape characters and quotes for complex inputs.
   
2. **Change Directory (`cd`)**: Enables navigation through the file system.
   - Supports relative, absolute paths, and `~` for the home directory.

3. **Print Working Directory (`pwd`)**: Displays the current directory.

4. **Cat Command (`cat`)**: Concatenates and displays the contents of files.
   - Handles multiple files and supports error handling for non-existent files.

5. **Type Command**: Identifies if a command is a shell built-in or an external executable.

6. **Execute External Commands**: Integrates with the system PATH to execute external programs.

## Requirements

- Java 8 or higher.

## Setup and Usage

1. **Compile the Program**:
   ```bash
   javac Main.java
   ```

2. **Run the Program**:
   ```bash
   java Main
   ```

3. **Use Commands**:
   - Type commands like `echo`, `pwd`, `cd`, `cat`, `type`, or external commands.
   - To exit the program, type `exit`.

## Examples

- **Echo Command**:
  ```
  $ echo Hello, World!
  Hello, World!
  ```

- **Change Directory**:
  ```
  $ cd /path/to/directory
  ```

- **Display Current Directory**:
  ```
  $ pwd
  /current/directory
  ```

- **View File Content**:
  ```
  $ cat file.txt
  This is the content of file.txt.
  ```

- **Identify Commands**:
  ```
  $ type echo
  echo is a shell builtin
  ```

- **Execute External Command**:
  ```
  $ ls
  file1.txt  file2.txt
  ```

## Error Handling

- Handles incorrect paths gracefully with informative error messages.
- Validates inputs for commands and files.

## Contribution

Contributions are welcome! Feel free to submit issues or pull requests on the GitHub repository.

## License

This project is licensed under the MIT License.

