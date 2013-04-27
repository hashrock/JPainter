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
package imagez.tool;

import imagez.image.ZLayer;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.KeyStroke;

/**
 * Layer translation?
 *
 * I really want this in the 'affine tool' depending on where you press
 * 
 * @author notzed
 */
public class TranslationTool extends ZDragTool {

	Point start;
	Point finish;
	ZLayer targetLayer;
	Point pos;

	@Override
	public String getName() {
		return "Translate";
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == e.BUTTON1) {
			Point p = getImagePixel(e);

			targetLayer = getLayer();
			pos = targetLayer.getBounds().getLocation();
			start = p;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Point p = getImagePixel(e);

		if (targetLayer != null) {
			dragTo(p);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		Point p = getImagePixel(e);

		if (targetLayer != null) {
			dragTo(p);
			Rectangle b = targetLayer.getBounds();
			// HACK: add undo record for translate here
			targetLayer.getZImage().getUndoManager().addLayerTranslate(targetLayer, b.x - pos.x, b.y - pos.y);
			targetLayer = null;
		}
	}

	void dragTo(Point to) {
		int dx = to.x - start.x;
		int dy = to.y - start.y;

		targetLayer.addDamage(targetLayer.getBounds());

		// FIXME: don't poke it directly
		targetLayer.getBounds().x += dx;
		targetLayer.getBounds().y += dy;

		// not going to work ... oh well
		targetLayer.addDamage(targetLayer.getBounds());

		start = to;
	}
}
