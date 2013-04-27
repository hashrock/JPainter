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
package imagez.io;

import imagez.ui.ImageWindow;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author notzed
 */
public class SaveImageWindow extends JFrame {

	ImageSaveOptions so;

	public SaveImageWindow(ImageWindow iwindow, ImageSaveOptions iso) {
		super();
		this.so = iso;

		setContentPane(so);
		//setLocation(iwindow.getLocationOnScreen());
		// this seems to not work very nicely
		setLocationRelativeTo(iwindow);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Save " + so.getTitle());
		getRootPane().setDefaultButton(so.save);
		so.nameBox.requestFocusInWindow();
		pack();

		// hack hack

		so.save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					so.save();
					dispose();
				} catch (IOException ex) {
					// TODO: do i leave the window up if it fails?  Probably
					Logger.getLogger(SaveImageWindow.class.getName()).log(Level.SEVERE, null, ex);
					System.out.println("show error");
					JOptionPane.showMessageDialog(null, "<html>Error saving:<br><br>" + ex.getLocalizedMessage(), "Error saving", JOptionPane.ERROR_MESSAGE);
				} finally {
				}
			}
		});
		so.saveVersion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//so.saveVersion();
			}
		});

		so.cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}
}
