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

import imagez.ui.ImageView;
import java.awt.Color;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.KeyStroke;

/**
 * Basic painting tool.
 *
 * Paint is applied at regular intervals based on travelled distance.
 * @author notzed
 */
public class BrushTool extends PaintTool {

	public BrushTool() {
		paint = new RadialGradientPaint(new Point2D.Float(0, 0), 5,
				new float[]{0.0f, 1.0f}, new Color[]{Color.BLACK, new Color(0, 0, 0, 0)});
		brush = new java.awt.geom.Ellipse2D.Float(-5, -5, 11, 11);
	}

	PropertyChangeListener listener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			setBrush(source.brushContext.getShape().getShape(source.brushContext.getRadiusX(), source.brushContext.getRadiusY()));
		}
	};
	
	@Override
	public void setImageView(ImageView source) {
		if (source != null) {
			source.brushContext.addPropertyChangeListener(listener);
		} else if (this.source != null) {
			this.source.brushContext.removePropertyChangeListener(listener);			
		}
		super.setImageView(source);
	}
	
	@Override
	public String getName() {
		return "brush";
	}

	@Override
	public Icon getIcon() {
		return super.getIcon("tool-brush.png");
	}

	@Override
	public Shape getBrush() {
		System.out.println("get brush");
		return source.brushContext.getShape().getShape(source.brushContext.getRadiusX(), source.brushContext.getRadiusY());
	//	return super.getBrush();
	}
	
	Color oldfg;

	@Override
	public Paint getPaint() {
		return source.brushContext.getFill().getPaint(source.brushContext.getForegroundColour(), source.brushContext.getBackgroundColour(),
				source.brushContext.getRadiusX(), source.brushContext.getRadiusY());
		/*
		Color fg = source.getForegroundColour();

		if (!fg.equals(oldfg)) {
			float rgba[] = new float[4];

			fg.getComponents(rgba);
			Color edge = new Color(rgba[0], rgba[1], rgba[2], 0f);

			oldfg = fg;
			paint = new RadialGradientPaint(new Point2D.Float(0, 0), 5,
					new float[]{0.0f, 1.0f}, new Color[]{fg, edge});
		}

		return paint;
		 * 
		 */
	}
}
