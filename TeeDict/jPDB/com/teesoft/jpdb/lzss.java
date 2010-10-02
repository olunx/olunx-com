/*
 * This is the LZSS module, which implements an LZ77 style compression
 * algorithm. As implemented here it uses a 12 bit index into the sliding
 * window, and a 4 bit length, which is adjusted to reflect phrase
 * lengths of between 2 and 17 bytes.
 *This is based on 'The Data Compression Book 2nd edition'.
 */

/*
 * lzss.java
 *
 * Created on Aug 5, 2007, 9:48:14 AM
 *
Copyright (C) 2007  Yong Li. All rights reserved.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.teesoft.jpdb;

import java.io.ByteArrayOutputStream;

/**
 *
 * @author wind
 */
public class lzss {

    public lzss() {
    }

    public static int bytesToInt(byte[] b, int start, int length) {
        int res = 0;
        for (int i = 0; i < length; i++) {
            res = res * 256 + byteToInt(b[start + i]);
        }
        return res;
    }

    public static int byteToInt(byte b) {
        if (b >= 0) {
            return b;
        } else {
            return 256 + b;
        }
    }


/*
     * This is the expansion routine for the LZSS algorithm. All it has
     * to do is read in flag bits, decide whether to read in a character or
     * a index/length pair, and take the appropriate action.
     */
    public ByteArrayOutputStream uncompress(byte[] buf) {
        return uncompress(buf, 0, buf.length);
    }

    public ByteArrayOutputStream uncompress(byte[] buf, int start, int length) {
        int i;
        int current_position;
        int c;
        int match_length;
        int match_position;
        current_position = 1;
        byte[] window = new byte[6000];
        bitStruct input = new bitStruct(buf, start, length);

        ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        while (!input.eof()) {
            if (InputBit(input) != 0) {
                c = (int) InputBits( input, 8 );
                output.write(c);
                window[current_position] = (byte) c;
                current_position = MOD_WINDOW(current_position + 1);
            } else {
                match_position = (int) InputBits( input, INDEX_BIT_COUNT );
                if (match_position == END_OF_STREAM) {
                    break;
                }
                match_length = (int) InputBits( input, LENGTH_BIT_COUNT );
                match_length += BREAK_EVEN;
                for (i = 0; i <= match_length; i++) {
                    c = byteToInt(window[MOD_WINDOW(match_position + i)]);
                    output.write(c);
                    window[current_position] = (byte) c;
                    current_position = MOD_WINDOW(current_position + 1);
                }
            }
        }
        return output;
    }

    public static class bitStruct {

        byte[] buf;
        int pos = 0;
        int length;
        int rack = 0;
        int mask = 128;

        bitStruct(byte[] buf, int start, int length) {
            this.buf = buf;
            this.pos = start;
            this.length = start + length;
        }

        int getc() {
            return byteToInt(buf[pos++]);
        }

        private boolean eof() {
            return pos == length;
        }
    }

    int InputBit(bitStruct bit_file) {
        int value;

        if (bit_file.mask == 0x80) {
            bit_file.rack = bit_file.getc();
            if (bit_file.eof()) {
                return 0;
            }
        }
        value = bit_file.rack & bit_file.mask;
        bit_file.mask >>= 1;
        if (bit_file.mask == 0) {
            bit_file.mask = 128;
        }
        return value != 0 ? 1 : 0;
    }

    long InputBits(bitStruct bit_file, int bit_count) {
        long mask;
        long return_value;

        mask = 1L << (bit_count - 1);
        return_value = 0;
        while (mask != 0) {
            if (bit_file.mask == 128) {
                bit_file.rack = bit_file.getc();
            }
            if ((bit_file.rack & bit_file.mask) != 0) {
                return_value |= mask;
            }
            mask >>= 1;
            bit_file.mask >>= 1;
            if (bit_file.mask == 0) {
                bit_file.mask = 128;
            }
        }
        return return_value;
    }
    public static int INDEX_BIT_COUNT = 9;
    public static int LENGTH_BIT_COUNT = 4;
    public static int WINDOW_SIZE = 1 << INDEX_BIT_COUNT;
    public static int RAW_LOOK_AHEAD_SIZE = 1 << LENGTH_BIT_COUNT;
    public static int BREAK_EVEN = 1;
//( ( 1 + INDEX_BIT_COUNT +  LENGTH_BIT_COUNT ) / 9 )
    public static int LOOK_AHEAD_SIZE = 0x11;
//(RAW_LOOK_AHEAD_SIZE + BREAK_EVEN)
    public static int TREE_ROOT = WINDOW_SIZE;
    public static int END_OF_STREAM = 0;
    public static int UNUSED = 1;

    public static int MOD_WINDOW(int a) {
        return (a) & (WINDOW_SIZE - 1);
    }
}
