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

import imagez.blend.BlendMode;
import imagez.blend.Normal;
import imagez.image.ZLayer;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Base class for all pen-like drawing tools.
 *
 * On pen-down a separate layer is use to render all operations to.
 * On pen up the layer is merged to the target layer.
 *
 * This class manages the layer.
 *
 * @author notzed
 */
abstract public class PenTool extends ZDragTool {

	final boolean debugEdit = false;
	Graphics2D penGraphics;
	ZLayer penLayer;
	BlendMode mode = new Normal();
	Rectangle bounds;

	public PenTool() {
	}

	public ZLayer getPenLayer() {
		return penLayer;
	}

	public Graphics2D getPenGraphics() {
		return penGraphics;
	}
	protected float opacity = 1.0f;
	public static final String PROP_OPACITY = "opacity";

	public float getOpacity() {
		return opacity;
	}

	public void setOpacity(float opacity) {
		float oldOpacity = this.opacity;
		this.opacity = opacity;
		propertyChangeSupport.firePropertyChange(PROP_OPACITY, oldOpacity, opacity);
	}
	public static final String PROP_MODE = "mode";

	public void setMode(BlendMode mode) {
		BlendMode oldMode = this.mode;
		this.mode = mode;
		propertyChangeSupport.firePropertyChange(PROP_MODE, oldMode, mode);
	}

	public BlendMode getMode() {
		return mode;
	}

	public void penDown(MouseEvent e) {
	}

	public void penMoved(MouseEvent e) {
	}

	public void penUp(MouseEvent e) {
	}

	/**
	 * Used by implementations to indicate which part of the layer was
	 * modified by the pen.
	 * @param area
	 */
	public void penTouched(Rectangle area) {
		if (bounds.isEmpty()) {
			bounds = new Rectangle(area);
		} else {
			bounds = bounds.union(area);
		}

		getLayer().addDamage(area);
		//penLayer.addDamage(area);
	}
	JFrame toolLayer;
	JLabel toolImage;

	@Override
	public void mousePressed(MouseEvent e) {
		super.mousePressed(e);

		penLayer = getLayer().aquireTmpLayer(ZLayer.TMP_OVERLAY);

		penLayer.setOpacity(opacity);
		penLayer.setMode(mode);

		bounds = new Rectangle();

		penGraphics = penLayer.getImage().createGraphics();
		// translate relative to layer offset
		// should creategraphics or something do that?
		penGraphics.translate(-penLayer.getBounds().x, -penLayer.getBounds().y);
		// up to implementations?
		//	penGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		//	penGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (debugEdit) {
			if (toolLayer == null) {
				toolLayer = new JFrame(this.getName() + " work layer");
				toolLayer.setPreferredSize(new Dimension(512, 512));
				toolImage = new JLabel();
				toolLayer.setVisible(true);
				toolLayer.add(toolImage);
				toolLayer.pack();
			}
			toolImage.setIcon(new ImageIcon(penLayer.getImage()));
			toolImage.setSize(penLayer.getImage().getWidth(), penLayer.getImage().getHeight());
		}

		penDown(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);

		penUp(e);

		if (penLayer != null) {
			getLayer().releaseTmpLayer(penLayer, true, bounds);
			penLayer = null;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		penMoved(e);
		if (toolImage != null) {
			toolImage.repaint();
		}
	}
}
