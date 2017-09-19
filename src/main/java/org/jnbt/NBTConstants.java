package org.jnbt;

import java.nio.charset.Charset;

/**
 * A class which holds constant values.
 *
 * @author Graham Edgecombe
 */
public final class NBTConstants {

    /**
     * The character set used by NBT (UTF-8).
     */
    public static final Charset CHARSET = Charset.forName("UTF-8");

    /**
     * Tag type constants.
     */
    public static final int
        TYPE_END = 0x0,
        TYPE_BYTE = 0x1,
        TYPE_SHORT = 0x2,
        TYPE_INT = 0x3,
        TYPE_LONG = 0x4,
        TYPE_FLOAT = 0x5,
        TYPE_DOUBLE = 0x6,
        TYPE_BYTE_ARRAY = 0x7,
        TYPE_STRING = 0x8,
        TYPE_LIST = 0x9,
        TYPE_COMPOUND = 0xa;

    /**
     * Default private constructor.
     */
    private NBTConstants() {

    }

}