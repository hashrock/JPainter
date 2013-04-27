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
package imagez.undo;

import imagez.image.ZLayer;

/**
 * Simplest undo record for a layer - whole layer is replaced with
 * new one.
 * @author notzed
 */
public class LayerCreateRecord extends UndoRecord {

	ZLayer src;
	int index;

	public LayerCreateRecord(ZLayer src, int index) {
		this.src = src;
		this.index = index;
	}

	@Override
	public void redo() {
		System.out.println("redo " + this);
		src.getZImage().addLayer(index, src);
	}

	@Override
	public void undo() {
		System.out.println("undo " + this);
		src.getZImage().removeLayer(src);
	}

	@Override
	public String toString() {
		return super.toString() + "[" + index + "]";
	}
}
