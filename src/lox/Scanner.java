package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lox.TokenType.*;


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
           start = current; //start is beginning of lexeme. sets to curr when moving to next tok
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
            //multi-token lexeme
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance(); //end up at the next line so no reread loop
                } else {
                    addToken(SLASH);
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                line++;
                break;

            case '"': string(); break;

            default:
                if(isDigit(c)){
                    number();
                } else {
                    Lox.error(line, "Unexpected charcter.");
                }
                break;
        }
    }
    public boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public void number() {
        while(isDigit(peek())) advance();
        if(peek() == '.' && isDigit(peekNext())) {
            advance();
            while(isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private char peekNext() {
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1); //dont use incop
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


    private boolean match(char expected) {
        if(isAtEnd()) return false;
        if(source.charAt(current) != expected) return false; //current it one ahead of multi-token -> checking for equal for multitoken
        current++; //important increment
        return true;
    }

    private char peek() {
        if(isAtEnd()) return '\0'; //null
        return source.charAt(current);
    }


    private void string() {
        while(peek() != '"' && !isAtEnd()) {
            if(peek() == '\n') line++;
            advance(); //keeps going until string closed
        }

        if(isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }




}
