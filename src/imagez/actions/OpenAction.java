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
package imagez.actions;

import imagez.image.ZImage;
import imagez.io.ImageLoader;
import imagez.ui.ImageFileChooser;
import imagez.ui.ImageWindow;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleAction;
import javax.accessibility.AccessibleContext;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 *
 * @author notzed
 */
public class OpenAction extends AbstractWindowAction {

	public OpenAction() {
		super("Open", null);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl O"));
	}

	@Override
	public Object getValue(String key) {
		return super.getValue(key);
	}

	void dumpAccessible(Accessible a, int depth) {
		AccessibleContext ac = a.getAccessibleContext();
		for (int i = 0; i < depth; i++) {
			System.out.print(" ");
		}
		AccessibleAction aa = ac.getAccessibleAction();
		System.out.println("accessible " + a.toString() + " action " + aa);
		if (aa != null) {
			int l = aa.getAccessibleActionCount();
			for (int j = 0; j < l; j++) {
				for (int i = 0; i < depth + 2; i++) {
					System.out.print(" ");
				}
				System.out.printf("[%d] = %s\n", j, aa.getAccessibleActionDescription(j));
			}
		}
		int len = ac.getAccessibleChildrenCount();
		for (int i = 0; i < len; i++) {
			Accessible b = ac.getAccessibleChild(i);

			dumpAccessible(b, depth + 2);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//JFileChooser fc = new JFileChooser();
		JFileChooser fc = new ImageFileChooser();

		// TODO: make non-modal.  Pack into a new frame, handle events, etc.

		// Should this be a method on ImageToolbox?

		// HACK: change mode to detail mode to make it nicer to use
		/*
		try {
			JPanel rootPanel = (JPanel) fc.getComponent(0);
			JPanel buttonPanel = (JPanel) rootPanel.getComponent(0);
			int len = buttonPanel.getComponentCount();
			int seen = 0;

			for (int i = 0; i < len; i++) {
				Component c = buttonPanel.getComponent(i);

				if (c instanceof JToggleButton) {
					seen++;
					if (seen == 2) {
						//((JToggleButton)c).setSelected(true);
						((JToggleButton) c).doClick();
					}
				}
			}
		} catch (Exception x) {
			System.out.println("filechooser hacks are dangerous: " + x.getMessage());
		}
		fc.setPreferredSize(new Dimension(500, 600));
		 *
		 */
		fc.setMultiSelectionEnabled(true);

		//dumpAccessible((Accessible)fc.getComponent(0), 2);

		Preferences prefs = Preferences.userNodeForPackage(OpenAction.class);

		String dir = prefs.get("open.drawer", null);

		if (dir != null) {
			fc.setCurrentDirectory(new File(dir));
		}

		if (fc.showOpenDialog(getWindow(e)) == JFileChooser.APPROVE_OPTION) {
			File[] files = fc.getSelectedFiles();

			prefs.put("open.drawer", fc.getCurrentDirectory().getPath());

			ImageLoader il = new ImageLoader();
			// TODO: if files.length > 10 warning or something
			for (int i = 0; i < files.length; i++) {
				try {
					ZImage img = il.loadImage(files[i]);
					ImageWindow win = getWindow(e);
					
					if (win != null && win.isEmpty()) {
						win.setImage(img);
					} else {
						win = new ImageWindow(img);
						win.setVisible(true);
					}
				} catch (IOException x) {
					Logger.getLogger(OpenAction.class.getName()).log(Level.SEVERE, null, x);
					JOptionPane.showMessageDialog(null, "<html>Error loading file<br><br>" + x.getLocalizedMessage(), "Erorr loading file", JOptionPane.ERROR_MESSAGE);
				} catch (Exception x) {
					System.err.println("Internal error loading file");
					x.printStackTrace();
				}
			}
			System.out.println(" loading " + fc.getSelectedFile());
		}
	}
}
