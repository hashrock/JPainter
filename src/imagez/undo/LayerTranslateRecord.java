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
import java.awt.Rectangle;

/**
 *
 * @author notzed
 */
public class LayerTranslateRecord extends UndoRecord {

	ZLayer src;
	int dx;
	int dy;

	public LayerTranslateRecord(ZLayer src, int dx, int dy) {
		this.src = src;
		this.dx = dx;
		this.dy = dy;
	}

	@Override
	public void redo() {
		Rectangle rect = src.getBounds();
		Rectangle diff = rect.getBounds();

		System.out.println("redo " + this);

		rect.x += dx;
		rect.y += dy;

		diff = diff.union(rect);
		// FIXME: doesn't work since falls outside of layer
		src.addDamage(diff);
	}

	@Override
	public void undo() {
		Rectangle rect = src.getBounds();
		Rectangle diff = rect.getBounds();

		System.out.println("undo " + this);

		rect.x -= dx;
		rect.y -= dy;

		diff = diff.union(rect);
		// FIXME: doesn't work since falls outside of layer
		src.addDamage(diff);
	}

	@Override
	public String toString() {
		return super.toString() + "[" + dx + "," + dy + "]";
	}
}
