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
 * Builder limits, based on similar limits used in some XML parsers.
 *
 * @author JSON.org
 * @version 2016-08-02
 */
public class BuilderLimits implements Cloneable {

    private int keyLength = Integer.MAX_VALUE;
    private int stringLength = Integer.MAX_VALUE;
    private int mantissaDigits = Short.MAX_VALUE;
    private int exponentDigits = Byte.MAX_VALUE;
    private int contentNodes = Integer.MAX_VALUE;
    private int nestingDepth = Integer.MAX_VALUE;

    // Secure defaults. Cloned for mutability reasons.
    private static final BuilderLimits SECURE_DEFAULTS =
            makeSecureDefaults();

    private static BuilderLimits makeSecureDefaults() {
        BuilderLimits params = new BuilderLimits();

        params.setKeyLength(1024);
        params.setStringLength(Integer.MAX_VALUE);
        params.setMantissaDigits(19);// Long.MAX_VALUE length
        params.setExponentDigits(3); // Double.MAX_VALUE exponent (ieee 754)
        params.setContentNodes(10000);
        params.setNestingDepth(256);

        return params;
    }

    /**
     * Create a new BuilderLimits class with large, but bounded, limits
     * on object building.
     */
    public BuilderLimits() {
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
    public void setStringLength(int stringLength) {
        this.stringLength = (stringLength <= 0) ? Integer.MAX_VALUE : stringLength;
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
    public int getStringLength() {
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

    /**
     * Return a BuilderLimits object that contains some limits for
     * secure processing. These can be customised as needed.
     *
     * @return a BuilderLimits object with secure processing limits
     */
    public static BuilderLimits secureDefaults() {
        return SECURE_DEFAULTS.clone();
    }

    /**
     * Clone this BuilderLimits object for customisation, or to avoid
     * mutation from outside code.
     *
     * @return a clone of this BuilderLimits object
     */
    @Override
    protected BuilderLimits clone() {
        try {
            return (BuilderLimits) super.clone();
        } catch(CloneNotSupportedException e) {
            throw new Error("Clone not cloneable!");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BuilderLimits that = (BuilderLimits) o;

        if (keyLength != that.keyLength) {
            return false;
        }
        if (stringLength != that.stringLength) {
            return false;
        }
        if (mantissaDigits != that.mantissaDigits) {
            return false;
        }
        if (exponentDigits != that.exponentDigits) {
            return false;
        }
        if (contentNodes != that.contentNodes) {
            return false;
        }
        return nestingDepth == that.nestingDepth;

    }

    @Override
    public int hashCode() {
        int result = keyLength;
        result = 31 * result + (int) (stringLength ^ (stringLength >>> 32));
        result = 31 * result + mantissaDigits;
        result = 31 * result + exponentDigits;
        result = 31 * result + contentNodes;
        result = 31 * result + nestingDepth;
        return result;
    }

    @Override
    public String toString() {
        return "BuilderLimits { " +
                "key length = " + keyLength +
                ", string length = " + stringLength +
                ", mantissa digits = " + mantissaDigits +
                ", exponent digits = " + exponentDigits +
                ", content nodes = " + contentNodes +
                ", nesting depth = " + nestingDepth +
                " }";
    }
}