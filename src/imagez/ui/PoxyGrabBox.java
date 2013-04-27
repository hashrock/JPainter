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

import imagez.tool.ZTool;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;

/**
 *
 * @author notzed
 */
public class PoxyGrabBox extends PoxyBoxOld implements ActionListener {
	// TODO: most of this is going to be shared amongst all poxybox's, work out how to share code

	JPanel tools;
	JPanel options;
	// tracks local last tool
	ToolButton lastTool;

	public PoxyGrabBox() {
		super();
		setPreferredSize(new Dimension(256, 300));

		ButtonGroup toolGroup = getToolGroup();

		tools = new JPanel();
		add(tools, BorderLayout.NORTH);
		tools.setLayout(new BoxLayout(tools, BoxLayout.X_AXIS));

		options = new JPanel();
		options.setLayout(new CardLayout(2, 2));
		add(options, BorderLayout.EAST);

		/*
		ZTool[] paintTools = ToolModel.selectionTools;
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
		}
		 * 
		 */

		pack();
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

		//ToolModel.getInstance().setTool(tb.tool);
		// FIXME: card switch should be based on toolmodel settings
		((CardLayout) options.getLayout()).show(options, tb.tool.getName());
	}
}
