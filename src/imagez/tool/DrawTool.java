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

import imagez.brush.BrushPainter;
import imagez.brush.BrushPainterListener;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import javax.swing.KeyStroke;

/**
 * New draw tool.  to replace brushtool, painttool, felttool
 * @author notzed
 */
public class DrawTool extends PenTool implements BrushPainterListener {

	BrushPainter painter;
	
	@Override
	public String getName() {
		return "Draw";
	}

	@Override
	public void penDown(MouseEvent e) {
		Point2D.Double at = getImageReal(e);
		
		painter = source.brushContext.getDraw().getPainter(source.brushContext);
		painter.addBrushPainterListener(this);
		painter.begin(penGraphics, at.x, at.y);
	}

	@Override
	public void penMoved(MouseEvent e) {
		Point2D.Double at = getImageReal(e);
		
		painter.paintTo(at.x, at.y);
	}

	@Override
	public void penUp(MouseEvent e) {
		painter.end();
		painter.removeBrushPainterListener(this);
	}

	@Override
	public void brushDamaged(Rectangle r) {
		penTouched(r);
	}
	
}
