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

import imagez.actions.ShowLastPoxAction;
import imagez.actions.ShowPaintPoxAction;
import imagez.actions.ShowGrabPoxAction;
import java.awt.BorderLayout;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * Poxy by name ...
 *
 * This is a popup toolbox thing.
 *
 * Idea is to have the main tools available here for easy access.
 * Another idea is to have a scratchpad area to play with the tool.
 * The scratchpad could also contain a snippet of the current view.
 * 
 * I don't really like JInternalFrame but I couldn't get a simple
 * panel to show.
 *
 * @author notzed
 */
public class PoxyBoxOld extends JDialog {

	public PoxyBoxOld() {
		//setPreferredSize(new Dimension(500, 500));

		//setMaximizable(false);
		//setIconifiable(false);

		//setUndecorated(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		getContentPane().setLayout(new BorderLayout());

		//JButton b= new JButton("bob");
		//this.getContentPane().add(b);


		//getContentPane().addMouseMotionListener(this);
		//getContentPane().addMouseListener(this);
		//pack();

		JPanel c = (JPanel) getContentPane();
		InputMap imap = c.getInputMap(c.WHEN_IN_FOCUSED_WINDOW);
		ActionMap amap = c.getActionMap();

		imap.put(KeyStroke.getKeyStroke(' '), "show-pox");
		amap.put("show-pox", ShowLastPoxAction.getInstance());
		imap.put(KeyStroke.getKeyStroke("F1"), "show-paint");
		amap.put("show-paint", ShowPaintPoxAction.getInstance());
		imap.put(KeyStroke.getKeyStroke("F2"), "show-selection");
		amap.put("show-selection", ShowGrabPoxAction.getInstance());
		/*

		imap.put(KeyStroke.getKeyStroke(' '), "hide-tools");
		amap.put("hide-tools", new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
		setVisible(false);
		}
		});
		 */
	}
	//
	private static ButtonGroup toolGroup;

	protected static ButtonGroup getToolGroup() {
		if (toolGroup == null) {
			toolGroup = new ButtonGroup();
		}
		return toolGroup;

	}
	/*
	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
	 * */
}
