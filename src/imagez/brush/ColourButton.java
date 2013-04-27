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
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JButton;

/**
 * Foreground/background colour button.
 * @author notzed
 */
public class ColourButton extends JButton {
	Color fg;
	Color bg;

	public ColourButton() {
		setPreferredSize(new Dimension(24, 24));
		setMinimumSize(new Dimension(24, 24));
	}

	public void setForegroundColour(Color fg) {
		this.fg = fg;
		repaint();
	}

	public void setBackgroundColour(Color bg) {
		this.bg = bg;
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		int w = getWidth();
		int h = getHeight();
		
		System.out.println("button size = " + w + ", " + h);
		
		g.setColor(fg);
		g.fillRect(0, 0, w*2/3, h*2/3);
		g.setColor(bg);
		g.fillRect(w/3, h/3, w*2/3, h*2/3);
		
		super.paintComponent(g);
	}
	
}
