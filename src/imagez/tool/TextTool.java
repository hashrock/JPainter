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

import imagez.ui.ImageView;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

/**
 *
 * @author notzed
 */
public class TextTool extends ZTool {

	@Override
	public String getName() {
		return "Text";
	}

	@Override
	public Component getWidget() {
		return null;
	}
	JInternalFrame frame;
	JTextPane textPane;

	void createEditor() {
		frame = new JInternalFrame("Edit Text");

		JPanel panel = new JPanel();

		frame.setContentPane(panel);
		frame.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(true);
		textPane = new JTextPane();
		//panel.add(textPane);
		panel.setLayout(new BorderLayout());

		{
			Action[] actions = textPane.getActions();
			for (int i=0;i<actions.length;i++) {
				System.out.println(" " + actions[i].getValue(Action.NAME));
			}
		}
		
		JScrollPane textScroll = new JScrollPane(textPane);

		panel.add(textScroll, BorderLayout.CENTER);

		frame.setPreferredSize(new Dimension(250, 300));
		//frame.setMaximumSize(new Dimension(250, 300));
		frame.setLocation(160, 50);

		//panel.setMaximumSize(new Dimension(250, 300));

		System.out.println("BROKEN: No where to put text tool!");
		
		frame.pack();
		frame.setVisible(true);
		Container c = ((JFrame)source.getTopLevelAncestor()).getLayeredPane();
		//Container c = null;
		c.add(frame, 0);
		textPane.requestFocusInWindow();

		StyledDocument sd = textPane.getStyledDocument();
		sd.addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				try {
					System.out.printf("%s: %d %d '%s'\n", e.getType(), e.getOffset(), e.getLength(), e.getDocument().getText(e.getOffset(), e.getLength()));
				} catch (BadLocationException ex) {
					Logger.getLogger(TextTool.class.getName()).log(Level.SEVERE, null, ex);
				}
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				System.out.printf("%s: %d %d\n", e.getType(), e.getOffset(), e.getLength());
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				System.out.println(e);
			}
		});
	}

	@Override
	public void setImageView(ImageView source) {
		super.setImageView(source);
		if (source != null) {
			if (frame == null) {
				createEditor();
			}
		} else {
			if (frame != null) {
				Container c = frame.getParent();

				c.remove(frame);
				frame.dispose();
				c.repaint();
				System.out.println("frame gone");
			}
			frame = null;
		}
	}
}
