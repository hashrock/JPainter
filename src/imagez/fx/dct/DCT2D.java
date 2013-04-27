/*
 * Copyright (C) 2011 notzed
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package imagez.fx.dct;

/**
 * Rather optimistic: general interface for DCTs.
 * @author notzed
 */
public interface DCT2D {

	public void forward(float[] patch, int offset);

	public void inverse(float[] patch, int offset);
}
