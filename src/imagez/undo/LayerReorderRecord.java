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

import imagez.image.ZImage;

/**
 * Re-order layers.
 * @author notzed
 */
public class LayerReorderRecord extends UndoRecord {

	ZImage src;
	int oindex;
	int nindex;

	public LayerReorderRecord(ZImage src, int oindex, int nindex) {
		this.src = src;
		this.oindex = oindex;
		this.nindex = nindex;
	}

	@Override
	public void redo() {
		System.out.println("redo " + this);
		src.moveLayer(oindex, nindex);
	}

	@Override
	public void undo() {
		System.out.println("undo " + this);
		src.moveLayer(nindex, oindex);
	}

	@Override
	public String toString() {
		return super.toString() + "[" + oindex + "," + nindex + "]";
	}
}
