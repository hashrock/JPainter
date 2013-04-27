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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.Icon;

/**
 *
 * @author notzed
 */
public class RectangleSelectionTool extends DragSelectionTool {

	public RectangleSelectionTool() {
		super();
	}

	@Override
	public Color getColour() {
		return Color.green;
	}

	@Override
	public String getName() {
		return "Rectangle";
	}

	@Override
	public Icon getIcon() {
		return super.getIcon("tool-select-rectangle.png");
	}

	@Override
	Shape getWorkingSelection(Point start, Point finish) {
		int left = Math.min(start.x, finish.x);
		int right = Math.max(start.x, finish.x);
		int top = Math.min(start.y, finish.y);
		int bottom = Math.max(start.y, finish.y);

		return new Rectangle(left, top, right - left, bottom - top);
	}
}
