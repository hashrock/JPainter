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
import java.awt.Point;
import java.awt.Rectangle;

/**
 * For storing a complete change to a layer where more than the
 * image bits are altered.
 *
 * e.g. a resize.
 *
 * @author notzed
 */
public class LayerChangeRecord extends UndoRecord {

	ZLayer src;
	Rectangle bounds;
	Point pos;
	ZLayer delta;

	public LayerChangeRecord(ZLayer src, Rectangle bounds, ZLayer delta) {
		//System.out.println("new layer change" + bounds);
		this.src = src;
		this.bounds = bounds;
		this.delta = delta;
		this.pos = src.getBounds().getLocation();

		System.out.println("new layer change delta: " + delta.toString());

		//ZLayerRGBASparseFloat sl = (ZLayerRGBASparseFloat) delta;
	}

	@Override
	public void redo() {
		System.out.println("redo layer change" + bounds + " " + delta);
		int width = bounds.width;
		float[] dtmp = new float[bounds.width * 4];
		float[] stmp = new float[bounds.width * 4];

		// HACK: track translation before its added to the undo properly
		int offx = (src.getBounds().x - pos.x);
		int offy = (src.getBounds().y - pos.y);

		for (int y = 0; y < bounds.height; y++) {
			delta.getLineRGBA(dtmp, 0, bounds.x, y + bounds.y, width);
			src.getLineRGBA(stmp, 0, offx + bounds.x, offy + y + bounds.y, width);
			for (int i = 0; i < width; i++) {
				stmp[i * 4 + 0] += dtmp[i * 4 + 0];
				stmp[i * 4 + 1] += dtmp[i * 4 + 1];
				stmp[i * 4 + 2] += dtmp[i * 4 + 2];
				stmp[i * 4 + 3] += dtmp[i * 4 + 3];
			}
			src.setLineRGBA(stmp, 0, offx + bounds.x, offy + y + bounds.y, width);
		}

		Rectangle rect = bounds.getBounds();
		rect.translate(offx, offy);
		src.addDamage(rect);
	}

	@Override
	public void undo() {
		System.out.println("undo layer change" + bounds + " detla" + delta.getBounds() + " src bounds: " + src.getBounds());

		int width = bounds.width;
		float[] dtmp = new float[bounds.width * 4];
		float[] stmp = new float[bounds.width * 4];

		// HACK: track translation before its added to the undo properly
		int offx = (src.getBounds().x - pos.x);
		int offy = (src.getBounds().y - pos.y);

		for (int y = 0; y < bounds.height; y++) {
			delta.getLineRGBA(dtmp, 0, bounds.x, y + bounds.y, width);
			src.getLineRGBA(stmp, 0, offx + bounds.x, offy + y + bounds.y, width);
			for (int i = 0; i < width; i++) {
				stmp[i * 4 + 0] -= dtmp[i * 4 + 0];
				stmp[i * 4 + 1] -= dtmp[i * 4 + 1];
				stmp[i * 4 + 2] -= dtmp[i * 4 + 2];
				stmp[i * 4 + 3] -= dtmp[i * 4 + 3];
			}
			src.setLineRGBA(stmp, 0, offx + bounds.x, offy + y + bounds.y, width);
		}

		Rectangle rect = bounds.getBounds();
		rect.translate(offx, offy);
		src.addDamage(rect);
	}
}
