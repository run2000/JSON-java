package org.json.stream;

/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import org.json.JSONException;
import org.json.JSONParseException;
import org.json.ParsePosition;
import org.json.Scanner;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * A {@code JSONLexer} takes a source stream and extracts characters and tokens
 * from it. It does not perform any sanity checks on the order that
 * tokens appear. It is used by {@link JSONStreamReader} class to tokenise
 * JSON source streams.
 *
 * @author JSON.org
 * @version 2016-06-08
 */
public final class JSONLexer {

    /**
     * Tokens that can be identified with at most one character lookahead.
     * Produced by {@link JSONLexer} and consumed by the {@link JSONStreamReader}
     * class.
     */
    public enum Token {
        STRING_VALUE,
        NUMBER_VALUE,
        NULL_VALUE,
        TRUE_VALUE,
        FALSE_VALUE,
        START_ARRAY,
        END_ARRAY,
        START_OBJECT,
        END_OBJECT,
        KEY_SEPARATOR,
        VALUE_SEPARATOR,
        END
    }

    private final Scanner scanner;

    /**
     * Construct a {@code JSONLexer} from a {@code Reader}.
     *
     * @param reader     A reader.
     */
    public JSONLexer(Reader reader) {
        scanner = new Scanner(reader);
    }

    /**
     * Construct a {@code JSONLexer} from an {@code InputStream} and a supplied
     * {@code Charset}.
     *
     * @param inputStream   the input stream containing the JSON data
     * @param charset       the character set with which to interpret the
     *                      input stream
     */
    public JSONLexer(InputStream inputStream, Charset charset) {
        scanner = new Scanner(inputStream, charset);
    }

    /**
     * Construct a {@code JSONLexer} from a {@code String}.
     *
     * @param s     A source string.
     */
    public JSONLexer(String s) {
        scanner = new Scanner(new StringReader(s));
    }

    /**
     * Get the next char in the stream, skipping insignificant whitespace.
     * Control characters less than U+0020, apart from newline and
     * carriage return, result in an error.
     *
     * @throws JSONException
     * @return  A character, or 0 if there are no more characters.
     */
    private char nextClean() throws JSONException {
        for (;;) {
            char c = scanner.next();
            switch(c) {
                case (char)0:
                    if (scanner.end()) {
                        return c;
                    } else {
                        throw new JSONParseException("Illegal control code",
                                scanner.parsePosition());
                    }
                case (char)1:
                case (char)2:
                case (char)3:
                case (char)4:
                case (char)5:
                case (char)6:
                case (char)7:
                case (char)8:
                case (char)11:
                case (char)12:
                case (char)14:
                case (char)15:
                case (char)16:
                case (char)17:
                case (char)18:
                case (char)19:
                case (char)20:
                case (char)21:
                case (char)22:
                case (char)23:
                case (char)24:
                case (char)25:
                case (char)26:
                case (char)27:
                case (char)28:
                case (char)29:
                case (char)30:
                case (char)31:
                    throw new JSONParseException("Illegal control code",
                            scanner.parsePosition());
                case (char)9: // \t
                case (char)10: // \n
                case (char)13: // \r
                case (char)32: // ' '
                    // valid whitespace
                    break;
                default:
                    return c;
            }
        }
    }

    /**
     * Get the type of the next token in the stream, skipping whitespace.
     * This parses the literal tokens and separators only.
     *
     * @return the Token type representing the next token type
     */
    public Token nextTokenType() throws JSONException {
        char c = nextClean();

        switch(c) {
            case '{':
                return Token.START_OBJECT;
            case '}':
                return Token.END_OBJECT;
            case '[':
                return Token.START_ARRAY;
            case ']':
                return Token.END_ARRAY;
            case ':':
                return Token.KEY_SEPARATOR;
            case ',':
                return Token.VALUE_SEPARATOR;
            case (char)0:
                if(scanner.end()) {
                    return Token.END;
                }
            case '"':
                scanner.back();
                return Token.STRING_VALUE;
            case 't':
                scanner.next('r');
                scanner.next('u');
                scanner.next('e');
                return Token.TRUE_VALUE;
            case 'f':
                scanner.next('a');
                scanner.next('l');
                scanner.next('s');
                scanner.next('e');
                return Token.FALSE_VALUE;
            case 'n':
                scanner.next('u');
                scanner.next('l');
                scanner.next('l');
                return Token.NULL_VALUE;
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                scanner.back();
                return Token.NUMBER_VALUE;
            default:
                throw new JSONParseException("Unexpected value", scanner.parsePosition());
        }
    }

    /**
     * Return the characters up to the next close quote character.
     * Backslash escape sequences are processed. The formal JSON format only
     * allows string values within double quotes.
     *
     * @param sb the Appendable to which the String value is appended
     * @param <T> a subtype of {@code Appendable}, returned to the caller
     *            for chaining purposes
     * @return the supplied Appendable object
     * @throws JSONException Unterminated string.
     */
    public <T extends Appendable> T nextString(T sb) throws JSONException {
        char c = scanner.next();

        if(c != '"') {
            throw new JSONException("Unexpected string delimiter");
        }
        try {
            for (;;) {
                c = scanner.next();
                switch (c) {
                    case (char)0:
                    case (char)10: // \n
                    case (char)13: // \r
                        throw new JSONParseException("Unterminated string",
                                scanner.parsePosition());
                    case (char)1:
                    case (char)2:
                    case (char)3:
                    case (char)4:
                    case (char)5:
                    case (char)6:
                    case (char)7:
                    case (char)8:
                    case (char)9:
                    case (char)11:
                    case (char)12:
                    case (char)14:
                    case (char)15:
                    case (char)16:
                    case (char)17:
                    case (char)18:
                    case (char)19:
                    case (char)20:
                    case (char)21:
                    case (char)22:
                    case (char)23:
                    case (char)24:
                    case (char)25:
                    case (char)26:
                    case (char)27:
                    case (char)28:
                    case (char)29:
                    case (char)30:
                    case (char)31:
                        throw new JSONParseException("Unescaped control code",
                                scanner.parsePosition());
                    case '\\':
                        c = scanner.next();
                        switch (c) {
                            case 'b':
                                sb.append('\b');
                                break;
                            case 't':
                                sb.append('\t');
                                break;
                            case 'n':
                                sb.append('\n');
                                break;
                            case 'f':
                                sb.append('\f');
                                break;
                            case 'r':
                                sb.append('\r');
                                break;
                            case 'u':
                                sb.append((char) parseUnicodeEscape());
                                break;
                            case '"':
                            case '\\':
                            case '/':
                                sb.append(c);
                                break;
                            default:
                                throw new JSONParseException("Illegal escape",
                                        scanner.parsePosition());
                        }
                        break;
                    case '"':
                        return sb;
                    default:
                        sb.append(c);
                        break;
                }
            }
        } catch (IOException e) {
            throw new JSONException("IOException", e);
        }
    }

    /**
     * Skip the characters up to the next close quote character.
     * Backslash processing is done. The formal JSON format only allows
     * strings in double quotes.
     *
     * @throws JSONException Unterminated string.
     */
    public void skipString() throws JSONException {
        char c = scanner.next();

        if(c != '"') {
            throw new JSONException("Unexpected string delimiter");
        }
        for (;;) {
            c = scanner.next();
            switch (c) {
                case (char)0:
                case (char)10: // \n
                case (char)13: // \r
                    throw new JSONParseException("Unterminated string",
                            scanner.parsePosition());
                case (char)1:
                case (char)2:
                case (char)3:
                case (char)4:
                case (char)5:
                case (char)6:
                case (char)7:
                case (char)8:
                case (char)9:
                case (char)11:
                case (char)12:
                case (char)14:
                case (char)15:
                case (char)16:
                case (char)17:
                case (char)18:
                case (char)19:
                case (char)20:
                case (char)21:
                case (char)22:
                case (char)23:
                case (char)24:
                case (char)25:
                case (char)26:
                case (char)27:
                case (char)28:
                case (char)29:
                case (char)30:
                case (char)31:
                    throw new JSONParseException("Unescaped control code",
                            scanner.parsePosition());
                case '\\':
                    c = scanner.next();
                    switch (c) {
                        case 'b':
                        case 't':
                        case 'n':
                        case 'f':
                        case 'r':
                        case '"':
                        case '\\':
                        case '/':
                            break;
                        case 'u':
                            parseUnicodeEscape();
                            break;
                        default:
                            throw new JSONParseException("Illegal escape",
                                    scanner.parsePosition());
                    }
                    break;
                case '"':
                    return;
                default:
                    break;
            }
        }
    }

    /**
     * Get the next 4 hexadecimal chars in the stream, and parse it as an
     * int value. Any character outside the hexadecimal range is
     * immediately rejected.
     *
     * @return  an int determined from the 4 hexadecimal characters
     */
    private int parseUnicodeEscape() throws JSONException {
        char[] chars = new char[4];

        for (int pos = 0; pos < 4; pos += 1) {
            char c = scanner.next();
            if (scanner.end()) {
                throw new JSONParseException("Substring bounds error",
                        scanner.parsePosition());
            } else if((c >= '0' && c <= '9')
                    || (c >= 'a' && c <= 'f')
                    || (c >= 'A' && c <= 'F')) {
                chars[pos] = c;
            } else {
                throw new JSONParseException("Illegal unicode escape",
                        scanner.parsePosition());
            }
        }
        try {
            return Integer.parseInt(String.valueOf(chars), 16);
        } catch (NumberFormatException e) {
            throw new JSONParseException("Illegal unicode escape", e,
                    scanner.parsePosition());
        }
    }

    /**
     * Parse a number strictly according to the JSON specification.
     *
     * @param sb The Appendable to which the parsed character sequence is
     *           appended
     * @return {@code true} if the number is a floating point value, otherwise
     * {@code false} to indicate an integer value
     */
    public boolean nextNumber(Appendable sb) throws JSONException {
        boolean dbl = false;
        char c = scanner.next();

        try {
            // likely digit
            if(c == '-') {
                sb.append(c);
                c = scanner.next();
            }
            if(c == '0') {
                sb.append(c);
                c = scanner.next();
            } else if((c >= '1') && (c <= '9')) {
                sb.append(c);

                // whole number values
                c = scanner.next();
                while((c >= '0') && (c <= '9')) {
                    sb.append(c);
                    c = scanner.next();
                }
            } else {
                throw new JSONParseException("Expected number", scanner.parsePosition());
            }

            if(c == '.') {
                dbl = true;
                sb.append(c);

                // decimal place values
                c = scanner.next();
                while((c >= '0') && (c <= '9')) {
                    sb.append(c);
                    c = scanner.next();
                }
            }
            if((c == 'e') || (c == 'E')) {
                dbl = true;
                sb.append(c);

                // exponent values
                c = scanner.next();
                if((c == '+') || (c == '-')) {
                    sb.append(c);
                    c = scanner.next();
                }
                if((c >= '0') && (c <= '9')) {
                    sb.append(c);
                    c = scanner.next();
                } else {
                    throw new JSONParseException("Expected exponent value",
                            scanner.parsePosition());
                }
                while((c >= '0') && (c <= '9')) {
                    sb.append(c);
                    c = scanner.next();
                }
            }
            scanner.back();
        } catch (IOException e) {
            throw new JSONException("IO exception", e);
        }
        return dbl;
    }

    /**
     * Skip a number strictly according to the JSON specification.
     */
    public void skipNumber() throws JSONException {
        char c = scanner.next();

        // likely digit
        if(c == '-') {
            c = scanner.next();
        }
        if(c == '0') {
            c = scanner.next();
        } else if((c >= '1') && (c <= '9')) {

            // whole number values
            c = scanner.next();
            while((c >= '0') && (c <= '9')) {
                c = scanner.next();
            }
        } else {
            throw new JSONParseException("Expected number", scanner.parsePosition());
        }

        if(c == '.') {
            // decimal place values
            c = scanner.next();
            while((c >= '0') && (c <= '9')) {
                c = scanner.next();
            }
        }
        if((c == 'e') || (c == 'E')) {
            // exponent values
            c = scanner.next();
            if((c == '+') || (c == '-')) {
                c = scanner.next();
            }
            if((c >= '0') && (c <= '9')) {
                c = scanner.next();
            } else {
                throw new JSONParseException("Expected exponent value",
                        scanner.parsePosition());
            }
            while((c >= '0') && (c <= '9')) {
                c = scanner.next();
            }
        }
        scanner.back();
    }

    /**
     * Indicates the current position of the scanner.
     *
     * @return a {@code ParsePosition} representing the current location of
     * the scanner
     */
    public ParsePosition parsePosition() {
        return scanner.parsePosition();
    }

    /**
     * Make a printable string of this {@code JSONLexer}.
     *
     * @return "JSONLexer at {index} [character {character} line {line}]"
     */
    @Override
    public String toString() {
        return "JSONLexer " + scanner.parsePosition().getPositionDetails();
    }
}
