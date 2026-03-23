
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static TokenType.*;


public class Scanner {
    private final String source;
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private final List<Token> tokens = new ArrayList<>();


    public Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while(!isAtEnd()) {
           start = current;
           scanTokens();
        }

        tokens.add(new Token(EOF,"", null, line));
        return tokens;
    }

    public boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
        }
    }

    public char advance() {
        return source.charAt(current++);
    }
    private void addToken(TokenType tok) { //tokens with no value (ops, keywords)
        addToken(tok, null);
    }
    public void addToken(TokenType type, Object literal) { //tokens that carry value NUMBER. STRING
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }




}
