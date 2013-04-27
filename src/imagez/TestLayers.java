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
package imagez;

import imagez.image.ZImage;
import imagez.image.ZLayer;
import imagez.image.ZLayerTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

abstract class ZAction extends AbstractAction {

	String iconKey;

	public ZAction(String iconKey) {
		this.iconKey = iconKey;
	}

	@Override
	public Object getValue(String key) {
		if (key.equals(SMALL_ICON)) {
			try {
				InputStream is = this.getClass().getResourceAsStream("/imagez/icons/" + iconKey);
				return new ImageIcon(ImageIO.read(is));
			} catch (IOException ex) {
				Logger.getLogger(TestLayers.class.getName()).log(Level.SEVERE, null, ex);
			}

		}
		return super.getValue(key);
	}
}

class LayerAddAction extends ZAction {

	ZImage image;

	public LayerAddAction(ZImage image) {
		super("layer-add.png");
		this.image = image;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ZLayer layer = image.createLayer();
		image.addLayer(layer);
	}
}

class LayerView extends JPanel {

	public LayerView(ZImage img) {
		JPanel panel = this;
		panel.setLayout(new BorderLayout());

		panel.setPreferredSize(new Dimension(170, 220));

		JPanel buttons = new JPanel();
		panel.add(buttons, BorderLayout.SOUTH);
		buttons.setLayout(new GridLayout(1, 0));

		//buttons.add(loadSmallButton("layer-add.png", Color.blue));
		buttons.add(new JButton(new LayerAddAction(img)));
		buttons.add(TestLayers.loadSmallButton("layer-up.png", Color.green));
		buttons.add(TestLayers.loadSmallButton("layer-down.png", Color.green));
		buttons.add(TestLayers.loadSmallButton("layer-copy.png", Color.yellow));
		buttons.add(TestLayers.loadSmallButton("layer-delete.png", Color.red));

		JScrollPane scroll = new JScrollPane();
		panel.add(scroll, BorderLayout.CENTER);

		JTable jt = new JTable();

		jt.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jt.setAutoCreateColumnsFromModel(false);
		jt.setTableHeader(null);
		jt.setModel(new ZLayerTableModel(img));
		jt.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		jt.setRowHeight(48);
		jt.setShowGrid(false);
		jt.setIntercellSpacing(new Dimension());

		TableColumn tc;
		tc = new TableColumn(0, 16, null, null);
		tc.setMaxWidth(16);
		tc.setMinWidth(16);
		jt.addColumn(tc);
		tc = new TableColumn(1, 16, null, null);
		tc.setMaxWidth(16);
		tc.setMinWidth(16);
		jt.addColumn(tc);
		tc = new TableColumn(2, 48, null, null);
		tc.setMaxWidth(48);
		tc.setMinWidth(48);
		jt.addColumn(tc);
		jt.addColumn(new TableColumn(3, 128, null, null));

		scroll.setViewportView(jt);
	}
}

/**
 *
 * @author notzed
 */
public class TestLayers extends JFrame {

	public static JButton loadSmallButton(String name, Color colour) {
		JButton button = new JButton();

		try {
			InputStream is = TestLayers.class.getResourceAsStream("/imagez/icons/" + name);
			button.setIcon(new ImageIcon(ImageIO.read(is)));
			button.setMargin(new Insets(0, 0, 0, 0));
			button.setBackground(colour);
		} catch (IOException ex) {
			Logger.getLogger(TestLayers.class.getName()).log(Level.SEVERE, null, ex);
		}

		return button;
	}

	public TestLayers(ZImage img) {

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		panel.setPreferredSize(new Dimension(170, 220));

		JPanel buttons = new JPanel();
		panel.add(buttons, BorderLayout.SOUTH);
		buttons.setLayout(new GridLayout(1, 0));

		//buttons.add(loadSmallButton("layer-add.png", Color.blue));
		buttons.add(new JButton(new LayerAddAction(img)));
		buttons.add(loadSmallButton("layer-up.png", Color.green));
		buttons.add(loadSmallButton("layer-down.png", Color.green));
		buttons.add(loadSmallButton("layer-copy.png", Color.yellow));
		buttons.add(loadSmallButton("layer-delete.png", Color.red));

		JScrollPane scroll = new JScrollPane();
		panel.add(scroll, BorderLayout.CENTER);

		JTable jt = new JTable();

		jt.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jt.setAutoCreateColumnsFromModel(false);
		jt.setTableHeader(null);
		jt.setModel(new ZLayerTableModel(img));
		jt.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		jt.setRowHeight(48);
		jt.setShowGrid(false);
		jt.setIntercellSpacing(new Dimension());

		TableColumn tc;
		tc = new TableColumn(0, 16, null, null);
		tc.setMaxWidth(16);
		tc.setMinWidth(16);
		jt.addColumn(tc);
		tc = new TableColumn(1, 16, null, null);
		tc.setMaxWidth(16);
		tc.setMinWidth(16);
		jt.addColumn(tc);
		tc = new TableColumn(2, 48, null, null);
		tc.setMaxWidth(48);
		tc.setMinWidth(48);
		jt.addColumn(tc);
		jt.addColumn(new TableColumn(3, 128, null, null));

		scroll.setViewportView(jt);

		//jt.setPreferredSize(new Dimension(180, 256));

		this.add(panel);
		pack();

		this.setDefaultCloseOperation(TestLayers.EXIT_ON_CLOSE);
	}

	public static void main(String[] ags) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				ZImage img = new ZImage(256, 256);
				img.addLayer(img.createLayer());
				img.addLayer(img.createLayer());
				img.addLayer(img.createLayer());

				new TestLayers(img).setVisible(true);
				new TestLayers(img).setVisible(true);
			}
		});
	}
}
