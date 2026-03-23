package lox;

public class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme; //raw source that was matched -> ex. "var" "+"
        this.literal = literal;
        this.line = line; //source line numb for err
    }
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
