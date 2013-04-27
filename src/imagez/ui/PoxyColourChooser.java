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

import imagez.brush.BrushContext;
import imagez.brush.PaintIcon;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author notzed
 */
public class PoxyColourChooser extends PoxyBox {

	BrushContext bc;
	JColorChooser jcc;
	PaintIcon fgi;
	PaintIcon bgi;
	JRadioButton fgb;
	JRadioButton bgb;

	public PoxyColourChooser(BrushContext bci) {
		super("Select Colours");

		bc = bci;

		JPanel panel = new JPanel(new BorderLayout());

		jcc = new JColorChooser(bc.getForegroundColour());

		// override default behaviour to show the 2 colours
		update();

		panel.add(jcc, BorderLayout.CENTER);

		ButtonGroup bg = new ButtonGroup();

		fgi = new PaintIcon(bc.getForegroundColour());
		fgb = new JRadioButton("Foreground", true);
		fgb.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setMode(MODE_FG);
			}
		});
		bg.add(fgb);

		bgi = new PaintIcon(bc.getBackgroundColour());
		bgb = new JRadioButton(new AbstractAction("Background", bgi) {

			@Override
			public void actionPerformed(ActionEvent e) {
				setMode(MODE_BG);
			}
		});
		bg.add(bgb);

		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));

		buttons.add(Box.createHorizontalGlue());
		buttons.add(fgb);
		buttons.add(Box.createHorizontalStrut(16));
		buttons.add(bgb);
		buttons.add(Box.createHorizontalGlue());

		panel.add(buttons, BorderLayout.NORTH);

		getContentPane().add(panel, BorderLayout.CENTER);

		bc.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(BrushContext.PROP_BACKGROUNDCOLOUR)) {
					System.out.println("bg changed " + evt.getNewValue());
					bgi.setPaint((Color) evt.getNewValue());
					bgb.repaint();
					update();
				} else if (evt.getPropertyName().equals(BrushContext.PROP_FOREGROUNDCOLOUR)) {
					System.out.println("fg changed " + evt.getNewValue());
					fgi.setPaint((Color) evt.getNewValue());
					fgb.repaint();
					update();
				}
			}
		});

		jcc.getSelectionModel().addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				System.out.println("state changed");
				if (mode == MODE_FG) {
					bc.setForegroundColour(jcc.getColor());
				} else {
					bc.setBackgroundColour(jcc.getColor());
				}
				update();
			}
		});

		pack();
	}
	public final static int MODE_FG = 0;
	public final static int MODE_BG = 1;
	int mode = MODE_FG;

	public void setMode(int mode) {
		if (this.mode != mode) {
			System.out.println("set mode " + mode);
			this.mode = mode;
			jcc.setColor(mode == MODE_FG ? bc.getForegroundColour() : bc.getBackgroundColour());
			if (mode == MODE_FG) {
				fgb.setSelected(true);
			} else {
				bgb.setSelected(true);
			}
			update();
		}
	}

	void update() {
		jcc.getPreviewPanel().setForeground(bc.getForegroundColour());
		jcc.getPreviewPanel().setBackground(bc.getBackgroundColour());
		jcc.getPreviewPanel().repaint();
	}
}
