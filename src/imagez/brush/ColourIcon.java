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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import javax.swing.Icon;

/**
 *
 * @author notzed
 */
public class ColourIcon implements Icon {

	Color fg;
	Color bg;

	public ColourIcon(Color fg, Color bg) {
		this.fg = fg;
		this.bg = bg;
	}

	public void setForeground(Color c) {
		fg = c;
	}

	public void setBackground(Color c) {
		bg = c;
	}
	public static final int CLICK_NONE = -1;
	public static final int CLICK_FG = 0;
	public static final int CLICK_BG = 1;

	public int checkClick(Point p) {
		int w = getIconWidth();
		int h = getIconHeight();

		if (p.x > w / 3 && p.y > h / 3) {
			return CLICK_FG;
		} else if (p.x < w * 2 / 3 && p.y < h * 2 / 3) {
			return CLICK_BG;
		}
		return CLICK_NONE;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D gg = (Graphics2D) g.create();
		int w = getIconWidth();
		int h = getIconHeight();

		gg.translate(x, y);
		gg.setColor(bg);
		gg.fillRect(0, 0, w * 2 / 3, h * 2 / 3);
		gg.setColor(fg);
		gg.fillRect(w / 3, h / 3, w * 2 / 3, h * 2 / 3);

		gg.dispose();
	}

	@Override
	public int getIconWidth() {
		return 32;
	}

	@Override
	public int getIconHeight() {
		return 32;
	}
}