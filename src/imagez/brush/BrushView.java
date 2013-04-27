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

import imagez.ui.ImageWindow;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

/**
 * List of brushes
 * @author notzed
 */
public class BrushView extends JPanel {

	JList list;
	BrushListModel model;

	public BrushView() {
		JScrollPane scroll;

		setLayout(new BorderLayout());

		list = new JList();
		list.setVisibleRowCount(-1);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setCellRenderer(new BrushRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		model = new BrushListModel();
		model.initBrushes();
		list.setModel(model);

		scroll = new JScrollPane(list);
		add(scroll, BorderLayout.CENTER);
	}

	class BrushRenderer extends JLabel implements ListCellRenderer {

		BrushIcon bi;

		public BrushRenderer() {
			bi = new BrushIcon(null);
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			BrushContext bc = (BrushContext) value;

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			bi.setBrush(bc);
			setIcon(bi);

			return this;
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				JFrame frame = new JFrame("test");

				frame.getContentPane().add(new BrushView());
				frame.pack();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		});
	}
}
