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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A compact, vertical, collapsible, tool-bar panel.
 * @author notzed
 */
public class ToolbarSection extends JPanel {

	JToggleButton tb;
	JComponent content;

	public ToolbarSection(String title, JComponent cont) {
		super();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.content = cont;

		tb = new JToggleButton(title);
		tb.setSelected(true);
		tb.setFont(ToolbarLabel.toolBarFont);
		tb.setBorder(null);
		//tb.setMinimumSize(new Dimension(150, 10));
		tb.setMaximumSize(new Dimension(150, 20));
		//tb.setPreferredSize(new Dimension(150, 10));
		tb.setAlignmentX(0.5f);

		add(tb);

		setContent(cont);
		//cont.setAlignmentX(0.5f);
		//add(cont);

		setAlignmentX(0.5f);

		tb.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (content != null) {
					content.setVisible(tb.isSelected());
					content.revalidate();
				}
			}
		});
	}

	public void setContent(JComponent content) {
		if (this.content != null) {
			remove(this.content);
		}
		this.content = content;
		if (content != null) {
			content.setAlignmentX(0.5f);
			add(content);
			revalidate();
		}
	}
	/**
	 * Preset column 1 constraint, right-aligned
	 */
	public final static GridBagConstraints c0;
	/**
	 * Preset column 1 constraint, left-aligned
	 */
	public final static GridBagConstraints c1;
	/**
	 * Preset column 2 constraint, left-aligned
	 */
	public final static GridBagConstraints c2;
	/**
	 * Preset 2-column constraint from 0-1
	 */
	public final static GridBagConstraints c01;
	/**
	 * Preset 3-column constraint from 0-2
	 */
	public final static GridBagConstraints c02;
	/**
	 * Preset 2-column constraint from 2-3
	 */
	public final static GridBagConstraints c23;

	static {
		c0 = new GridBagConstraints();
		c0.gridx = 0;
		c0.anchor = GridBagConstraints.BASELINE_TRAILING;
		c0.ipadx = 0;
		c0.insets = new Insets(1, 2, 1, 2);

		c1 = new GridBagConstraints();
		c1.gridx = 1;
		c1.insets = new Insets(1, 2, 1, 2);
		c1.weightx = 1;
		c1.anchor = GridBagConstraints.BASELINE_LEADING;
		c1.fill = GridBagConstraints.HORIZONTAL;

		c2 = new GridBagConstraints();
		c2.gridx = 2;
		c2.insets = new Insets(1, 2, 1, 2);
		c2.weightx = 1;
		c2.anchor = GridBagConstraints.BASELINE_LEADING;
		c2.fill = GridBagConstraints.HORIZONTAL;

		c01 = new GridBagConstraints();
		c01.gridx = 0;
		c01.gridwidth = 2;
		c01.anchor = GridBagConstraints.BASELINE_LEADING;
		c01.fill = GridBagConstraints.HORIZONTAL;

		c02 = new GridBagConstraints();
		c02.gridx = 0;
		c02.gridwidth = 3;
		c02.anchor = GridBagConstraints.BASELINE_LEADING;
		c02.fill = GridBagConstraints.HORIZONTAL;

		c23 = new GridBagConstraints();
		c23.gridx = 1;
		c23.gridwidth = 2;
		c23.anchor = GridBagConstraints.BASELINE_LEADING;
		c23.fill = GridBagConstraints.HORIZONTAL;
	}
}
