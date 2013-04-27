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
package imagez.brush;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import javax.swing.Icon;

/**
 *
 * @author notzed
 */
public class PaintIcon implements Icon {

	Paint paint;

	public PaintIcon(Paint paint) {
		this.paint = paint;
	}

	public void setPaint(Paint paint) {
		this.paint = paint;
	}
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D gg = (Graphics2D) g.create();
		
		gg.translate(x, y);
		gg.setPaint(paint);
		gg.fillRect(0, 0, 24, 24);
		gg.dispose();
	}

	@Override
	public int getIconWidth() {
		return 24;
	}

	@Override
	public int getIconHeight() {
		return 24;
	}
}