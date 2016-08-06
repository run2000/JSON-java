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

/**
 * @author JSON.org
 * @version 2016-08-02
 */
public class LimitParameters {

    private int keyLength = Integer.MAX_VALUE;
    private long stringLength = Long.MAX_VALUE;
    private int mantissaDigits = Short.MAX_VALUE;
    private int exponentDigits = Byte.MAX_VALUE;
    private int contentNodes = Integer.MAX_VALUE;
    private int nestingDepth = Integer.MAX_VALUE;

    public LimitParameters() {
    }

    /**
     * The maximum length of any key.
     */
    public void setKeyLength(int keyLength) {
        this.keyLength = (keyLength <= 0) ? Integer.MAX_VALUE : keyLength;
    }

    /**
     * The maximum length of any string value.
     */
    public void setStringLength(long stringLength) {
        this.stringLength = (stringLength <= 0) ? Long.MAX_VALUE : stringLength;
    }

    /**
     * The maximum number of mantissa digits in a number.
     */
    public void setMantissaDigits(int mantissaDigits) {
        this.mantissaDigits = (mantissaDigits <= 0) ? Short.MAX_VALUE : mantissaDigits;
    }

    /**
     * The maximum number of exponent digits in a number.
     */
    public void setExponentDigits(int exponentDigits) {
        this.exponentDigits = (exponentDigits < 0) ? Byte.MAX_VALUE : exponentDigits;
    }

    /**
     * Total number of child nodes per object or array.
     */
    public void setContentNodes(int contentNodes) {
        this.contentNodes = (contentNodes <= 0) ? Integer.MAX_VALUE : contentNodes;
    }

    /**
     * Total nesting depth of objects or arrays.
     */
    public void setNestingDepth(int nestingDepth) {
        this.nestingDepth = (nestingDepth <= 0) ? Integer.MAX_VALUE : nestingDepth;
    }

    /**
     * The maximum length of any key.
     */
    public int getKeyLength() {
        return keyLength;
    }

    /**
     * The maximum length of any string value.
     */
    public long getStringLength() {
        return stringLength;
    }

    /**
     * The maximum number of mantissa digits in a number.
     */
    public int getMantissaDigits() {
        return mantissaDigits;
    }

    /**
     * The maximum number of exponent digits in a number.
     */
    public int getExponentDigits() {
        return exponentDigits;
    }

    /**
     * Total number of child nodes per object or array.
     */
    public int getContentNodes() {
        return contentNodes;
    }

    /**
     * Total nesting depth of objects or arrays.
     */
    public int getNestingDepth() {
        return nestingDepth;
    }

    public static LimitParameters secureDefaults() {
        LimitParameters params = new LimitParameters();

        params.setKeyLength(1024);
        params.setStringLength(Integer.MAX_VALUE);
        params.setMantissaDigits(17);
        params.setExponentDigits(3);
        params.setContentNodes(10000);
        params.setNestingDepth(200);

        return params;
    }
}
