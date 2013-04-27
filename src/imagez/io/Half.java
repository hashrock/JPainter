/*
 *  Copyright (c) 2011 Michael Zucchi
 *
 *  This file is part of ImageZ, a bitmap image editing appliction.
 *
 *  ImageZ is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ImageZ is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ImageZ.  If not, see <http://www.gnu.org/licenses/>.
 */
package imagez.io;

/**
 *
 * @author notzed
 */
public class Half {

	// limits
	static final float HALF_MIN = 5.96046448e-08f; // Smallest positive half
	static final float HALF_NRM_MIN = 6.10351562e-05f; // Smallest positive normalized half
	static final float HALF_MAX = 65504.0f;        // Largest positive half
	static final float HALF_EPSILON = 0.00097656f;     // Smallest positive e for which
	// half (1.0 + e) != half (1.0)
	static final int HALF_MANT_DIG = 11;          // Number of digits in mantissa
	// (significand + hidden leading 1)
	static final int HALF_DIG = 2;           // Number of base 10 digits that
	// can be represented without change
	static final int HALF_RADIX = 2;            // Base of the exponent
	static final int HALF_MIN_EXP = -13;             // Minimum negative integer such that
	// HALF_RADIX raised to the power of
	// one less than that integer is a
	// normalized half
	static final int HALF_MAX_EXP = 16;              // Maximum positive integer such that
	// HALF_RADIX raised to the power of
	// one less than that integer is a
	// normalized half
	static final int HALF_MIN_10_EXP = -4;              // Minimum positive integer such
	// that 10 raised to that power is
	// a normalized half
	static final int HALF_MAX_10_EXP = 4;               // Maximum positive integer such
	// that 10 raised to that power is
	// a normalized half

	//---------------------------------------------------
	// Interpret an unsigned short bit pattern as a half,
	// and convert that half to the corresponding float's
	// bit pattern.
	//---------------------------------------------------
	static float toFloat(short y) {
		int s = (y >> 15) & 0x00000001;
		int e = (y >> 10) & 0x0000001f;
		int m = y & 0x000003ff;

		if (e == 0) {
			if (m == 0) {
				//
				// Plus or minus zero
				//

				return s << 31;
			} else {
				//
				// Denormalized number -- renormalize it
				//

				while (0 != (m & 0x00000400)) {
					m <<= 1;
					e -= 1;
				}

				e += 1;
				m &= ~0x00000400;
			}
		} else if (e == 31) {
			if (m == 0) {
				//
				// Positive or negative infinity
				//

				return (s << 31) | 0x7f800000;
			} else {
				//
				// Nan -- preserve sign and significand bits
				//

				return (s << 31) | 0x7f800000 | (m << 13);
			}
		}

		//
		// Normalized number
		//

		e = e + (127 - 15);
		m = m << 13;

		//
		// Assemble s, e and m.
		//

		return Float.intBitsToFloat((s << 31) | (e << 23) | m);
	}

	//-----------------------------------------------------
	// Float-to-half conversion -- general case, including
	// zeroes, denormalized numbers and exponent overflows.
	//-----------------------------------------------------
	static short toHalf(float f) {
		int i = Float.floatToRawIntBits(f);
		//
		// Our floating point number, f, is represented by the bit
		// pattern in integer i.  Disassemble that bit pattern into
		// the sign, s, the exponent, e, and the significand, m.
		// Shift s into the position where it will go in in the
		// resulting half number.
		// Adjust e, accounting for the different exponent bias
		// of float and half (127 versus 15).
		//

		int s = (i >> 16) & 0x00008000;
		int e = ((i >> 23) & 0x000000ff) - (127 - 15);
		int m = i & 0x007fffff;

		//
		// Now reassemble s, e and m into a half:
		//

		if (e <= 0) {
			if (e < -10) {
				//
				// E is less than -10.  The absolute value of f is
				// less than HALF_MIN (f may be a small normalized
				// float, a denormalized float or a zero).
				//
				// We convert f to a half zero with the same sign as f.
				//

				return (short) s;
			}

			//
			// E is between -10 and 0.  F is a normalized float
			// whose magnitude is less than HALF_NRM_MIN.
			//
			// We convert f to a denormalized half.
			//

			//
			// Add an explicit leading 1 to the significand.
			//

			m = m | 0x00800000;

			//
			// Round to m to the nearest (10+e)-bit value (with e between
			// -10 and 0); in case of a tie, round to the nearest even value.
			//
			// Rounding may cause the significand to overflow and make
			// our number normalized.  Because of the way a half's bits
			// are laid out, we don't have to treat this case separately;
			// the code below will handle it correctly.
			//

			int t = 14 - e;
			int a = (1 << (t - 1)) - 1;
			int b = (m >> t) & 1;

			m = (m + a + b) >> t;

			//
			// Assemble the half from s, e (zero) and m.
			//

			return (short) (s | m);
		} else if (e == 0xff - (127 - 15)) {
			if (m == 0) {
				//
				// F is an infinity; convert f to a half
				// infinity with the same sign as f.
				//

				return (short) (s | 0x7c00);
			} else {
				//
				// F is a NAN; we produce a half NAN that preserves
				// the sign bit and the 10 leftmost bits of the
				// significand of f, with one exception: If the 10
				// leftmost bits are all zero, the NAN would turn
				// into an infinity, so we have to set at least one
				// bit in the significand.
				//

				m >>= 13;
				return (short) (s | 0x7c00 | m | (m == 0 ? 1 : 0));
			}
		} else {
			//
			// E is greater than zero.  F is a normalized float.
			// We try to convert f to a normalized half.
			//

			//
			// Round to m to the nearest 10-bit value.  In case of
			// a tie, round to the nearest even value.
			//

			m = m + 0x00000fff + ((m >> 13) & 1);

			if ((m & 0x00800000) != 0) {
				m = 0;             // overflow in significand,
				e += 1;             // adjust exponent
			}

			//
			// Handle exponent overflow
			//

			if (e > 30) {
				//overflow ();        // Cause a hardware floating point overflow;
				return (short) (s | 0x7c00);  // if this returns, the half becomes an
			}                       // infinity with the same sign as f.

			//
			// Assemble the half from s, e and m.
			//
			return (short) (s | (e << 10) | (m >> 13));
		}
	}
}
