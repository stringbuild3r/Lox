# Codebase Method Reference

This document describes every method and constructor in the Java source files in this repository.
Constructors are included because they execute code and define object initialization behavior.

Compiled `.class` files are build artifacts, so this reference documents the source files only.

## `src/lox/Lox.java`

`Lox` is the command-line entry point and currently drives the scanner. It can run a file or launch an interactive prompt. It also owns the global `hadError` flag used to decide whether a run saw a reported error.

### `public static void main(String[] args) throws IOException`

This is the JVM entry point for the interpreter.

What happens:

- Checks how many command-line arguments were passed.
- If more than one argument is present, prints `Usage: jlox [script]` and exits with code `64`.
- If exactly one argument is present, treats that argument as a script path and calls `runFile(args[0])`.
- If no arguments are present, starts the interactive prompt by calling `runPrompt()`.
- Lets `IOException` propagate instead of handling file or input failures locally.

### `private static void runFile(String path) throws IOException`

Runs Lox source code from a file.

What happens:

- Reads every byte from the file at `path`.
- Converts those bytes into a `String` using the platform default charset.
- Passes that source string to `run(String source)`.
- After scanning, checks `hadError`.
- If any error was reported, exits with code `65`, which is used here as a data format error.

### `private static void runPrompt() throws IOException`

Runs the interpreter in interactive prompt mode.

What happens:

- Wraps `System.in` in an `InputStreamReader`.
- Wraps that reader in a `BufferedReader` so it can read one line at a time.
- Enters an infinite loop.
- Prints the prompt text `> ` before every read.
- Reads one input line.
- If `readLine()` returns `null`, input has ended, so the loop breaks.
- Otherwise, sends that line to `run(String source)`.
- Resets `hadError` to `false` after each prompt line so one failed line does not poison the rest of the interactive session.

### `private static void run(String source)`

Runs the current interpreter pipeline for a source string.

What happens:

- Creates a new `Scanner` using the provided source text.
- Calls `scanner.scanTokens()` to convert source text into a list of tokens.
- Iterates over every token in the returned list.
- Prints each token to standard output using the token's `toString()` method.
- The current implementation stops after lexing; it does not parse or evaluate the program yet.

### `static void error(int line, String message)`

Reports an error at a specific source line.

What happens:

- Delegates to `report(int line, String where, String message)`.
- Passes an empty `where` value because this overload reports only a line number, not a token location.

### `private static void report(int line, String where, String message)`

Formats and records an interpreter error.

What happens:

- Prints an error message to standard error in the format `[line N] Error: message`, with `where` inserted after `Error` when supplied.
- Sets `hadError` to `true`.
- This flag is later used by `runFile()` to decide whether the process should exit with an error code.

## `src/lox/Scanner.java`

`Scanner` turns raw source text into a sequence of `Token` objects. It tracks the current lexeme with `start`, the current scan cursor with `current`, and the current source line with `line`.

### Static initializer

This block is not a method, but it is executable class initialization code.

What happens:

- Creates the static `keywords` map.
- Registers every reserved Lox keyword string, such as `class`, `fun`, `true`, and `while`.
- Maps each keyword string to its corresponding `TokenType`.
- Lets `identifier()` distinguish user-defined identifiers from reserved language words.

### `Scanner(String source)`

Constructs a scanner for one source string.

What happens:

- Stores the provided source text in the instance field `source`.
- Leaves `start`, `current`, and `line` at their default initialized values of `0`, `0`, and `1`.
- Leaves the token list empty until `scanTokens()` is called.

### `List<Token> scanTokens()`

Scans the entire source string and returns all tokens.

What happens:

- Loops while `isAtEnd()` is false.
- At the start of each token, sets `start` to `current`.
- Calls `scanToken()` to consume one token or ignored character sequence.
- After all source characters are consumed, appends an `EOF` token with an empty lexeme and `null` literal.
- Returns the accumulated token list.

### `private boolean isAtEnd()`

Checks whether the scanner cursor has reached the end of the source.

What happens:

- Compares `current` with `source.length()`.
- Returns `true` when `current` is greater than or equal to the source length.
- Returns `false` when there are still characters left to scan.

### `private void scanToken()`

Consumes and classifies the next token-sized piece of source.

What happens:

- Calls `advance()` to consume the current character.
- Uses a `switch` on that consumed character.
- Emits single-character tokens for punctuation and operators like `(`, `)`, `{`, `}`, `,`, `.`, `-`, `+`, `;`, and `*`.
- Handles two-character operator forms by calling `match('=')`, producing either the two-character token or the one-character token for `!`, `=`, `<`, and `>`.
- Handles `/` specially:
  - If followed by another `/`, consumes characters until the end of the line or end of file, treating the whole sequence as a comment.
  - Otherwise emits a `SLASH` token.
- Ignores spaces, carriage returns, and tabs.
- Increments `line` when it sees a newline.
- Calls `string()` when it sees a double quote.
- Calls `number()` when the current character begins a numeric literal.
- Calls `identifier()` when the current character begins an identifier or keyword.
- Reports `Unexpected character.` through `Lox.error()` when no known token rule matches.

### `private boolean isDigit(char c)`

Checks whether a character is an ASCII digit.

What happens:

- Returns `true` for characters from `'0'` through `'9'`.
- Returns `false` for every other character.

### `private void number()`

Consumes a number literal and adds a `NUMBER` token.

What happens:

- Continues consuming digits while `peek()` sees another digit.
- Checks for a fractional part by looking for `.` followed by another digit.
- If a fractional part exists, consumes the dot and then consumes the following digits.
- Extracts the full numeric lexeme from `source.substring(start, current)`.
- Parses that substring into a `Double`.
- Adds a `NUMBER` token whose literal value is the parsed double.

### `private char peekNext()`

Looks one character past the current cursor without consuming anything.

What happens:

- If `current + 1` would be outside the source string, returns the null sentinel character `'\0'`.
- Otherwise returns `source.charAt(current + 1)`.
- This is used by `number()` to decide whether a `.` begins a fractional numeric literal.

### `private char advance()`

Consumes and returns the current source character.

What happens:

- Reads `source.charAt(current)`.
- Increments `current` after reading.
- Returns the character that was just consumed.

### `private void addToken(TokenType tok)`

Adds a token that has no literal runtime value.

What happens:

- Delegates to `addToken(TokenType type, Object literal)`.
- Supplies `null` as the literal.
- This is used for punctuation, operators, and keywords where the token type and lexeme are enough.

### `private void addToken(TokenType type, Object literal)`

Adds a token with an optional literal value.

What happens:

- Extracts the token text from `source.substring(start, current)`.
- Creates a new `Token` with the provided type, extracted lexeme, literal value, and current line number.
- Appends that token to the scanner's `tokens` list.

### `private boolean match(char expected)`

Conditionally consumes the next character if it matches an expected value.

What happens:

- Returns `false` immediately if the scanner is already at the end of the source.
- Checks the unconsumed character at `current`.
- If that character is not `expected`, returns `false` without advancing.
- If it matches, increments `current` and returns `true`.
- This is how `scanToken()` recognizes two-character operators like `!=`, `==`, `<=`, and `>=`.

### `private char peek()`

Looks at the current source character without consuming it.

What happens:

- If the scanner is at the end of the source, returns the null sentinel character `'\0'`.
- Otherwise returns `source.charAt(current)`.
- This supports lookahead in string, number, identifier, and comment scanning.

### `private void string()`

Consumes a string literal and adds a `STRING` token.

What happens:

- Consumes characters until it finds a closing double quote or reaches the end of the source.
- Increments `line` for newline characters inside the string.
- If the source ends before a closing quote, reports `Unterminated string.` and returns without adding a token.
- Consumes the closing quote.
- Extracts the string value between the opening and closing quotes, excluding both quotes.
- Adds a `STRING` token with the extracted value as its literal.

### `private void identifier()`

Consumes an identifier or reserved keyword.

What happens:

- Consumes characters while `peek()` is alphanumeric according to `isAlphaNumeric(char c)`.
- Extracts the full identifier text from `source.substring(start, current)`.
- Looks up that text in the static `keywords` map.
- If the text is a reserved keyword, uses the keyword's token type.
- If the text is not reserved, uses `IDENTIFIER`.
- Adds the resulting token.

### `private boolean isAlpha(char c)`

Checks whether a character can begin or appear in a Lox identifier as a letter-like character.

What happens:

- Returns `true` for ASCII lowercase letters.
- Returns `true` for ASCII uppercase letters.
- Returns `true` for underscore.
- Returns `false` for all other characters.

### `private boolean isAlphaNumeric(char c)`

Checks whether a character is valid after the first character of an identifier.

What happens:

- Returns `true` if `isAlpha(c)` is true.
- Returns `true` if `isDigit(c)` is true.
- Returns `false` otherwise.

## `src/lox/Token.java`

`Token` is a simple data holder for scanner output. Each token records a type, the raw source lexeme, an optional literal value, and the line number where it was found.

### `Token(TokenType type, String lexeme, Object literal, int line)`

Constructs one token.

What happens:

- Stores the token type.
- Stores the raw lexeme matched from source text.
- Stores the literal value, which is used for tokens like strings and numbers and is `null` for tokens without a literal value.
- Stores the source line number for error reporting.

### `public String toString()`

Returns a printable representation of the token.

What happens:

- Concatenates the token type, lexeme, and literal value separated by spaces.
- Returns that string.
- This is what `Lox.run()` prints for each scanned token.

## `src/lox/Expr.java`

`Expr` is an abstract base class for expression AST nodes. The current source defines nested node classes and constructors only; there are no visitor or evaluation methods in this file.

### `Binary(Expr left, Token operator, Expr right)`

Constructs a binary expression node.

What happens:

- Stores the left operand expression.
- Stores the operator token.
- Stores the right operand expression.
- Represents expressions such as `a + b`, `x == y`, or `left * right`.

### `Grouping(Expr expression)`

Constructs a grouping expression node.

What happens:

- Stores the inner expression.
- Represents a parenthesized expression such as `(a + b)`.

### `Literal(Object value)`

Constructs a literal expression node.

What happens:

- Stores the literal value.
- Represents source literals such as numbers, strings, booleans, or `nil`.

### `Unary(Token operator, Expr right)`

Constructs a unary expression node.

What happens:

- Stores the unary operator token.
- Stores the operand expression to the right of the operator.
- Represents expressions such as `!condition` or `-number`.

## `src/lox/TokenType.java`

`TokenType` is an enum of all token categories the scanner can currently produce.

This file defines no methods or constructors directly. Its constants cover:

- Single-character tokens.
- One- or two-character operator tokens.
- Literal token types.
- Reserved keyword token types.
- `EOF`, the end-of-file marker.

## `src/tool/GenerateAst.java`

`GenerateAst` is a small code-generation utility for producing AST class source code. It currently targets `Expr`.

### `public static void main(String[] args) throws IOException`

Runs the AST generator from the command line.

What happens:

- Checks that exactly one command-line argument was supplied.
- If the argument count is wrong, prints `Usage: generate_ast <output directory>` to standard error and exits with code `64`.
- Treats the only argument as the output directory.
- Calls `defineAst()` to generate an `Expr` AST source file.
- Supplies four expression node definitions: `Binary`, `Grouping`, `Literal`, and `Unary`.

### `private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException`

Writes a Java source file for an AST base class and its nested node classes.

What happens:

- Builds an output path in the form `outputDir/baseName.java`.
- Opens a `PrintWriter` for that path using UTF-8.
- Writes the `lox` package declaration.
- Writes an import for `java.util.List`.
- Starts an abstract class named by `baseName`.
- Loops over each type description string.
- Splits each type description around `:` into a class name and a field list.
- Calls `defineType()` to write one nested AST node class for that type.
- Writes an abstract `accept(Visitor<R> visitor)` method declaration.
- Closes the generated class.
- Attempts to write an `@Override` implementation of `accept()` after the generated class has already been closed.
- Closes the writer.

Important current behavior:

- `defineAst()` writes an abstract `accept()` method, but the checked-in `Expr.java` does not currently contain that method.
- `defineAst()` does not currently call `defineVisitor()`, so generated code that references `Visitor<R>` would need that visitor interface to be emitted elsewhere or added here.
- The attempted generated `accept()` implementation is outside the emitted class body because it is written after `writer.println("}")`.
- The attempted generated `accept()` implementation references `className`, but `className` is scoped inside the earlier `for` loop. That makes the current `GenerateAst.java` source fail to compile.
- `defineType()` does not currently emit concrete `accept()` implementations inside each generated nested class.

### `private static void defineVisitor(PrintWriter writer, String baseName, List<String> types)`

Writes a visitor interface for generated AST classes.

What happens:

- Writes the start of an interface named `Visitor<R>`.
- Loops over every type description.
- Extracts the AST node class name from the portion before `:`.
- Writes one visitor method for that node type.
- Uses a method name in the form `visit<TypeName><BaseName>`.
- Uses a parameter type matching the concrete node class and a parameter name matching the lowercase base name.
- Writes the closing brace for the interface.

Important current behavior:

- This helper exists but is not called by `defineAst()`, so it currently has no effect when the generator runs.

### `private static void defineType(PrintWriter writer, String baseName, String className, String fieldList)`

Writes one nested AST node class.

What happens:

- Writes a static nested class named `className` that extends `baseName`.
- Writes a constructor whose parameters are the fields listed in `fieldList`.
- Splits `fieldList` by comma and space to get individual field declarations.
- For each field declaration, extracts the field name by splitting on a space and taking the second part.
- Writes constructor assignments from each parameter into the matching instance field.
- Writes one final field declaration for each field in the list.
- Closes the nested class.

## Repository Files Without Source Methods

The following files are present but do not define Java source methods:

- `NOTES.md`: notes/documentation, not source code.
- `lox.iml`: IDE module metadata.
- `src/lox/*.class` and `src/tool/*.class`: compiled Java bytecode artifacts.
