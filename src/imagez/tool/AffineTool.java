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
import imagez.ui.ImageView;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.KeyStroke;

/**
 * Affine transormation tool.
 *
 * Can I put them all into one tool?  Perhaps ...
 * @author notzed
 */
public class AffineTool extends ZDragTool {

	float angle = 0;
	Point pos;
	Point centre;
	// test idea - low quality high speed version
	boolean downscale = true;
	Image scaledImage;
	ZLayer scaledLayer;
	float scaleAmount;

	@Override
	public String getName() {
		return "Affine Tool";
	}

	@Override
	public void setImageView(ImageView source) {
		if (this.source != source) {
			if (scaledImage != null) {
				scaledImage.flush();
				scaledImage = null;
			}
			pos = null;
		}
		super.setImageView(source);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		pos = this.getImagePixel(e);

		angle = 0;

		Rectangle b = getLayer().getBounds();
		centre = new Point(b.width / 2, b.height / 2);

		if (scaledImage == null || scaledLayer != getLayer()) {
			// test idea: rotate a lower-res for the live-view
			BufferedImage img = getLayer().getImage();
			scaledLayer = getLayer();
			if (downscale && img.getWidth() > 512) {
				scaleAmount = 512f / img.getWidth();
				scaledImage = img.getScaledInstance((int) (img.getWidth() * scaleAmount), (int) (img.getHeight() * scaleAmount), Image.SCALE_FAST);
			} else {
				scaleAmount = 1;
				scaledImage = img.getScaledInstance(img.getWidth(), img.getHeight(), Image.SCALE_FAST);
			}
			scaledImage.setAccelerationPriority(1);
		}
		//angle += 0.1;;
		//source.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		pos = null;
		source.repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Point m = getImagePixel(e);

		double dx1 = centre.x - pos.x;
		double dy1 = centre.y - pos.y;
		double dx2 = centre.x - m.x;
		double dy2 = centre.y - m.y;

		angle = (float) -(Math.atan2(dx2, dy2) - Math.atan2(dx1, dy1));
		source.repaint();
	}

	@Override
	public void paint(Graphics2D g) {
		if (pos != null) {
			BufferedImage img = getLayer().getImage();
			AffineTransform at = new AffineTransform();

			//g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
			//g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			//g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			at.translate(img.getWidth() / 2, img.getHeight() / 2);
			at.rotate(angle);
			at.translate(-img.getWidth() / 2, -img.getHeight() / 2);
			if (downscale) {
				at.scale(1 / scaleAmount, 1 / scaleAmount);
			}
			g.drawImage(scaledImage, at, null);
		}
	}
}
