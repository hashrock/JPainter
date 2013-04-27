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

import java.awt.Color;
import javax.swing.Icon;
import javax.swing.KeyStroke;

/**
 * Basic painting tool.
 *
 * Paint is applied at regular intervals based on travelled distance.
 * @author notzed
 */
public class FeltTool extends PaintTool {

	public FeltTool() {
		paint = Color.BLUE;
		//brush = new java.awt.geom.Ellipse2D.Float(-3f, -3f, 6f, 6f);
		brush = new java.awt.geom.Ellipse2D.Float(-20f, -20f, 41f, 41f);
	}

	@Override
	public Icon getIcon() {
		return super.getIcon("tool-felt-pen.png");
	}

	@Override
	public String getName() {
		return "felt pen";
	}
}
