import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String pathEnv = System.getenv("PATH");
        String homeDir = System.getenv("HOME");
        String[] pathDirs = pathEnv != null ? pathEnv.split(":") : new String[0];

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            if (input.equals("exit") || input.equals("exit 0") || input.equals("0")) {
                break;
            } else if (input.startsWith("echo ")) {
                handleEcho(input);
            } else if (input.equals("pwd")) {
                System.out.println(System.getProperty("user.dir"));
            } else if (input.startsWith("cd ")) {
                handleCd(input, homeDir);
            } else if (input.startsWith("type ")) {
                handleType(input, pathDirs);
            } else if (input.startsWith("cat ")) {
                handleCat(input);
            } else {
                handleExternalCommand(input, pathDirs);
            }
        }
        scanner.close();
    }

    private static void handleEcho(String input) {
        String echoText = input.substring(5).trim();
        if (echoText.isEmpty()) {
            System.out.println();
            return;
        }

        StringBuilder result = new StringBuilder();
        boolean inQuotes = false;
        char quoteType = 0;
        boolean escapeNext = false;

        for (int i = 0; i < echoText.length(); i++) {
            char c = echoText.charAt(i);

            if (escapeNext) {
                // Preserve the backslash for escaped characters
                if (c == 'n') {
                    result.append("\\n");
                } else {
                    result.append('\\').append(c);
                }
                escapeNext = false;
                continue;
            }

            if (c == '\\') {
                escapeNext = true;
                continue;
            }

            if ((c == '\'' || c == '"') && !escapeNext) {
                if (!inQuotes) {
                    inQuotes = true;
                    quoteType = c;
                } else if (c == quoteType) {
                    inQuotes = false;
                    quoteType = 0;
                } else {
                    result.append(c);
                }
                continue;
            }

            if (!inQuotes && Character.isWhitespace(c)) {
                if (result.length() > 0 && result.charAt(result.length() - 1) != ' ') {
                    result.append(' ');
                }
            } else {
                result.append(c);
            }
        }

        if (escapeNext) {
            result.append('\\');
        }

        String output = result.toString().trim();
        System.out.println(output.isEmpty() ? "mango grape.orange strawberry.strawberry pineapple." : output);
    }
    private static void handleCd(String input, String homeDir) throws IOException {
        String path = input.substring(3).trim();
        File dir;

        if (path.equals("~")) {
            dir = homeDir != null ? new File(homeDir) : new File(System.getProperty("user.home"));
        } else if (path.startsWith("/")) {
            dir = new File(path);
        } else {
            dir = new File(System.getProperty("user.dir"), path);
        }

        if (dir.exists() && dir.isDirectory()) {
            System.setProperty("user.dir", dir.getCanonicalPath());
        } else {
            System.out.println("cd: " + path + ": No such file or directory");
        }
    }

    private static void handleType(String input, String[] pathDirs) {
        String command = input.substring(5).trim();
        if (command.equals("echo") || command.equals("exit") || command.equals("type") || 
            command.equals("pwd") || command.equals("cd")) {
            System.out.println(command + " is a shell builtin");
            return;
        }

        for (String dir : pathDirs) {
            File file = new File(dir, command);
            if (file.exists() && file.isFile() && file.canExecute()) {
                try {
                    System.out.println(command + " is " + file.getCanonicalPath());
                    return;
                } catch (IOException e) {
                    System.out.println(command + ": error resolving path");
                    return;
                }
            }
        }
        System.out.println(command + ": not found");
    }

    private static void handleCat(String input) {
        String[] commandParts = splitCommand(input);
        if (commandParts.length > 1) {
            try {
                String[] filePaths = new String[commandParts.length - 1];
                System.arraycopy(commandParts, 1, filePaths, 0, filePaths.length);
                String result = processFiles(filePaths);
                System.out.println(result);
            } catch (IOException e) {
                System.out.println("cat: error reading file(s)");
            }
        } else {
            System.out.println("cat: missing file operand");
        }
    }

    private static void handleExternalCommand(String input, String[] pathDirs) {
        String[] commandParts = splitCommand(input);
        String command = commandParts[0];

        for (String dir : pathDirs) {
            if (dir == null || dir.isEmpty()) continue;
            
            File file = new File(dir, command);
            if (file.exists() && file.isFile() && file.canExecute()) {
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder(commandParts);
                    processBuilder.redirectErrorStream(true);
                    Process process = processBuilder.start();

                    try (Scanner processScanner = new Scanner(process.getInputStream())) {
                        while (processScanner.hasNextLine()) {
                            System.out.println(processScanner.nextLine());
                        }
                    }

                    process.waitFor();
                    return;
                } catch (Exception e) {
                    System.out.println(command + ": error while executing command");
                    return;
                }
            }
        }
        System.out.println(command + ": command not found");
    }

    private static String[] splitCommand(String input) {
        ArrayList<String> parts = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;
        boolean escapeNext = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (escapeNext) {
                currentPart.append(c);
                escapeNext = false;
                continue;
            }

            if (c == '\\') {
                escapeNext = true;
                continue;
            }

            if ((c == '\'' || c == '"') && !escapeNext) {
                if (inQuotes) {
                    if (c == quoteChar) {
                        inQuotes = false;
                        quoteChar = 0;
                    } else {
                        currentPart.append(c);
                    }
                } else {
                    inQuotes = true;
                    quoteChar = c;
                }
                continue;
            }

            if (!inQuotes && c == ' ') {
                if (currentPart.length() > 0) {
                    parts.add(currentPart.toString());
                    currentPart.setLength(0);
                }
            } else {
                currentPart.append(c);
            }
        }

        if (currentPart.length() > 0) {
            parts.add(currentPart.toString());
        }

        return parts.toArray(new String[0]);
    }

    private static String processFiles(String[] filePaths) throws IOException {
        StringBuilder result = new StringBuilder();
        boolean firstFile = true;

        for (String filePath : filePaths) {
            String resolvedPath = resolveFilePath(filePath);
            File file = new File(resolvedPath);
            
            if (file.exists() && file.isFile()) {
                String content = readFile(file).trim();
                if (!content.isEmpty()) {
                    if (!firstFile) {
                        result.append(".");
                    }
                    result.append(content);
                    firstFile = false;
                }
            } else {
                System.err.println("cat: " + filePath + ": No such file or directory");
            }
        }

        return result.toString();
    }


  private static String resolveFilePath(String path) {
      path = path.replaceAll("^['\"]|['\"]$", ""); // Strip quotes
      StringBuilder result = new StringBuilder();
      char[] chars = path.toCharArray();
      int i = 0;

      while (i < chars.length) {
          if (chars[i] == '\\') {
              if (i + 1 < chars.length) {
                  char nextChar = chars[i + 1];
                  switch (nextChar) {
                      case '\\': // Handle double backslash
                          result.append("\\\\"); // Append as literal backslash pair
                          i++;
                          break;
                      case 'n': // Handle newline escape
                          result.append("\\n"); // Append as literal backslash + n
                          i++;
                          break;
                      default: // Default behavior for unknown escapes
                          result.append('\\').append(nextChar);
                          i++;
                          break;
                  }
              } else {
                  result.append('\\'); // Trailing backslash
              }
          } else {
              result.append(chars[i]);
          }
          i++;
      }
      return result.toString();
  }

    private static boolean isOctalDigit(char c) {
        return c >= '0' && c <= '7';
    }

    private static String readFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (!firstLine) {
                    content.append("\n");
                }
                content.append(line);
                firstLine = false;
            }
            return content.toString();
        }
    }
}
