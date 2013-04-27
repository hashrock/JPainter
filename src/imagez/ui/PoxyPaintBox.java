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
package imagez.ui;

import imagez.TestLayers;
import imagez.image.ZImage;
import imagez.image.ZLayer;
import imagez.image.ZLayerRGBAInt;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

abstract class PoxyAction extends AbstractAction {

	PoxyBoxOld box;
	String iconKey;

	public PoxyAction(PoxyBoxOld box, String iconKey) {
		this.box = box;
		this.iconKey = iconKey;
	}

	@Override
	public Object getValue(String key) {
		if (key.equals(LARGE_ICON_KEY)) {
			try {
				InputStream is = this.getClass().getResourceAsStream("/imagez/icons/" + iconKey);
				return new ImageIcon(ImageIO.read(is));
			} catch (IOException ex) {
				Logger.getLogger(TestLayers.class.getName()).log(Level.SEVERE, null, ex);
				return null;
			}

		}
		return super.getValue(key);
	}
}

class ScratchTransparentAction extends PoxyAction {

	public ScratchTransparentAction(PoxyBoxOld box) {
		super(box, "fill-transparent.png");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ZLayer layer = ((PoxyPaintBox) box).scratchImage.getLayer(0);

		layer.clear();
	}
}

class ScratchImageAction extends PoxyAction implements ImageObserver {

	public ScratchImageAction(PoxyBoxOld box) {
		super(box, "fill-picture.png");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ImageWindow win = null;//ToolModel.getInstance().getImageWindow();
		ImageView iv = win.getImageView();

		Point pos = SwingUtilities.convertPoint(((PoxyPaintBox) box).scratch, new Point(), iv);

		// FIXME: handle zoom

		BufferedImage src = iv.getImage().getImage();

		ZLayer layer = ((PoxyPaintBox) box).scratchImage.getLayer(0);
		Graphics2D g = layer.getImage().createGraphics();
		Rectangle bounds = layer.getBounds();

		g.drawImage(src, 0, 0, bounds.width, bounds.height, pos.x, pos.y, pos.x + bounds.width, pos.y + bounds.height, this);
		g.dispose();

		layer.addDamage(layer.getBounds());

		// FIXME: copy source picture
		// bounds?  from when it started i suppose ...
		// or it's current location?
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		System.out.println("image update " + infoflags);
		return true;
	}
}

class ScratchForegroundAction extends PoxyAction {

	public ScratchForegroundAction(PoxyBoxOld box) {
		super(box, "fill-foreground.png");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ZLayer layer = ((PoxyPaintBox) box).scratchImage.getLayer(0);
		Graphics2D g = layer.getImage().createGraphics();
		Rectangle bounds = layer.getBounds();

		g.setBackground(Color.white);
		g.clearRect(0, 0, bounds.width, bounds.height);
		g.dispose();

		layer.addDamage(layer.getBounds());
	}
}

class ScratchBackgroundAction extends PoxyAction {

	public ScratchBackgroundAction(PoxyBoxOld box) {
		super(box, "fill-background.png");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ZLayer layer = ((PoxyPaintBox) box).scratchImage.getLayer(0);
		Graphics2D g = layer.getImage().createGraphics();
		Rectangle bounds = layer.getBounds();

		g.setBackground(Color.black);
		g.clearRect(0, 0, bounds.width, bounds.height);
		g.dispose();

		layer.addDamage(layer.getBounds());
	}
}

class PoxyButton extends JButton {

	public PoxyButton(PoxyAction pa) {
		super(pa);

		setMargin(new Insets(0, 0, 0, 0));
		setBackground(new Color(0x2299dd));
	}
}

/**
 *
 * @author notzed
 */
public class PoxyPaintBox extends PoxyBoxOld implements ActionListener {

	ImageView scratch;
	ZImage scratchImage;
	JPanel options;
	JPanel tools;
	// not sure where to put this either
	JColorChooser cc;
	//
	ScratchImageAction imageAction;
	//
//	ToolModel tm;
	ToolButton lastTool;

	public PoxyPaintBox() {
		super();
		//setPreferredSize(new Dimension(256 + 150 + 150, 300));
	//	tm = ToolModel.getInstance();

		ButtonGroup toolGroup = getToolGroup();

		scratch = new ImageView(null);
		// HACK: indicates this imageview is not part of most operations
		scratch.setName("scratchArea");
		add(scratch, BorderLayout.CENTER);
		scratchImage = new ZImage(256, 256);
		scratchImage.addLayer(new ZLayerRGBAInt(scratchImage, new Rectangle(256, 256)));
		scratchImage.setUndoEnabled(false);
		scratch.setImage(scratchImage);

		tools = new JPanel();
		add(tools, BorderLayout.NORTH);
		tools.setLayout(new BoxLayout(tools, BoxLayout.X_AXIS));

		options = new JPanel();
		options.setLayout(new CardLayout(2, 2));
		add(options, BorderLayout.EAST);

		// add scratch control buttons
		imageAction = new ScratchImageAction(this);
		tools.add(new PoxyButton(imageAction));
		tools.add(new PoxyButton(new ScratchForegroundAction(this)));
		tools.add(new PoxyButton(new ScratchBackgroundAction(this)));
		tools.add(new PoxyButton(new ScratchTransparentAction(this)));
		tools.add(Box.createHorizontalStrut(8));

		cc = new JColorChooser();
		add(cc, BorderLayout.SOUTH);
		AbstractColorChooserPanel[] panels = cc.getChooserPanels();
		//cc.removeChooserPanel(panels[0]);
		//cc.removeChooserPanel(panels[2]);
		//cc.setPreferredSize(new Dimension(300, 60));
		cc.setPreviewPanel(new JPanel());
		cc.getSelectionModel().addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
	//			tm.setForeground(cc.getColor());
			}
		});

		// add tool buttons
	//	ZTool current = tm.getTool();

		/*
		ZTool[] paintTools = ToolModel.paintTools;
		for (int i = 0; i < paintTools.length; i++) {
			ZTool tool = paintTools[i];
			ToolButton tb = new ToolButton(tool);
			Component widget = tool.getWidget();

			toolGroup.add(tb);
			tools.add(tb);
			tb.addActionListener(this);

			if (widget != null) {
				options.add(widget, tool.getName());
			}
			if (current == null || current.equals(tb.tool)) {
				tb.setSelected(true);
				if (current == null) {
					current = tb.tool;
					tm.setTool(current);
				}
			}
		}

		 * 
		 */
	//	tm.addPropertyChangeListener(new PropertyChangeListener() {
//
//			@Override
//			public void propertyChange(PropertyChangeEvent evt) {
//				String name = evt.getPropertyName();
//
//				if (name.equals(ToolModel.PROP_TOOL)) {
//					System.out.println("something set the current tool?");
//					// setup tool buttons to match or something?
//				} else if (name.equals(ToolModel.PROP_FOREGROUND))  {
//					// where do i put this?
//				}
//			}
//		});

		pack();

		System.out.println("BROKEN: poxy boxes are really poxy");
		
		ToolAdapter toolAdapter = new ToolAdapter(scratch);
	}
	/**
	 * Keeps track of the previous 'image view', for the 'copy image' action
	 *
	 * TODO: copy-image could probably query the z-order of the images and the ones
	 * the tool overlaps with and grab the right one that way
	 */
	ImageView targetImageView;

	public ImageView getTargetImageView() {
		return targetImageView;
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (b && lastTool != null) {
			lastTool.doClick();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ToolButton tb = (ToolButton) e.getSource();

		lastTool = tb;

//		tm.setTool(tb.tool);
		((CardLayout) options.getLayout()).show(options, tb.tool.getName());
	}
}
