# Notes

## Token Example

| Token Type | Lexeme |
|------------|--------|
| VAR        | "var"  |
| IDENTIFIER | "x"    |
| EQUAL      | "="    |
| NUMBER     | "10"   |
| SEMICOLON  | ";"    |

## Start and Current

- `start` is reset at the top of the `scanTokens()` loop: `start = current` (line 24). This marks the beginning of each new lexeme.
- `current` is never reset — it only moves forward through the source.

## Incremented

- `current` is incremented in two places:
    - `advance()` — `current++` every time a character is consumed
    - `match()` — `current++` when the next character matches the expected one

# Syntactic vs Lexical

 - 'Syntactic' is what is done with the tokens
 - 'Lexical' operates on characters and creates tokens from them
