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

import imagez.TestLayers;
import imagez.image.ZImage;
import imagez.image.ZLayer;
import imagez.ui.ImageView;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

/**
 * Base class for all mouse driven tools.
 *
 * Handles mouse input and holding the layer.
 *
 * TODO: must also handle coordinate translation/zooming.
 * @author notzed
 */
public abstract class ZTool implements MouseListener {

	ImageView source;
	boolean active;

	protected ZTool() {
	}

	public ImageView getSource() {
		return source;
	}
	
	public void setImageView(ImageView source) {
		if (source == this.source) {
			return;
		}

		if (this.source != null) {
			//this.source.removeMouseListener(this);
		}

		this.source = source;
		if (source != null) {
			//System.out.println("listning to window" + source);
			//source.addMouseListener(this);
		}
	}

	// TODO: should probably be in 'action' interface
	abstract public String getName();

	public Color getColour() {
		return Color.blue;
	}

	// FIXME: should probably all just be done using Action interface

	public Icon getIcon() {
		BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_GRAY);

		return new ImageIcon(bi);
	}

	public Icon getSmallIcon() {
		ImageIcon ic = (ImageIcon) getIcon();
		BufferedImage ii = (BufferedImage) ic.getImage();

		BufferedImage si = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

		Graphics2D gg = si.createGraphics();

		gg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		gg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		gg.drawImage(ii, 0, 0, 16, 16, null);
		gg.dispose();

		return new ImageIcon(si);
	}

	public Icon getIcon(String iconKey) {
		// FIXMEL put thyis somewhere more global?
		try {
			InputStream is = this.getClass().getResourceAsStream("/imagez/icons/" + iconKey);
			return new ImageIcon(ImageIO.read(is));
		} catch (IOException ex) {
			Logger.getLogger(TestLayers.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	public ZImage getImage() {
		return source.getImage();
	}

	public ZLayer getLayer() {
		return source.getCurrentLayer();
	}

	public Component getWidget() {
		return new JLabel("No options");
	}

	/**
	 * TODO: not sure this is the nicest solution here, tools are getting messy.
	 * 
	 * Ask this tool to paint itself to the display.
	 * 
	 * This is for optional tool related graphics such as selection
	 * handles, brush outline, etc.
	 * 
	 * Tools must invoke repaint() as necessary to trigger a repaint
	 * of the area.
	 * @param g
	 */
	public void paint(Graphics2D g) { }
	public void repaint(Rectangle rect) {
		if (source != null) {
			source.imageChanged(rect);
		}
	}
	/**
	 * Convert mouse location into integer pixel coordinates
	 *
	 * TODO: get this from imageView
	 * @param e
	 * @return
	 */
	public Point getImagePixel(MouseEvent e) {
		Point2D.Double p = source.convertPoint(e.getPoint());

		// TODO: scaling for zoom
		return new Point((int) p.x, (int) p.y);
	}

	public Point2D.Double getImageReal(MouseEvent e) {
		// TODO: scaling for zoom
		return source.convertPoint(e.getPoint());
	}

	/**
	 * Convert mouse location into floating point coordinates
	 * @param e
	 * @return
	 */
	public Point2D.Float getImageCoordinates(MouseEvent e) {
		// TODO: scaling for zoom
		return new Point2D.Float(e.getX(), e.getY());
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}
	protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}
}
