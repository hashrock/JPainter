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
package imagez.fx.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

class RequestCancelAction extends AbstractAction {

	BasicRequester iface;

	public RequestCancelAction(BasicRequester iface) {
		super("Cancel");
		this.iface = iface;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		iface.requestCancelled();
	}
}

class RequestOkAction extends AbstractAction {

	BasicRequester iface;

	public RequestOkAction(BasicRequester iface) {
		super("Ok");
		this.iface = iface;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		iface.requestOk();
	}
}

/**
 * Frame for a standard requester popup.
 * @author notzed
 */
public class RequesterFrame extends JFrame implements BasicRequester {

	JComponent content;
	BasicRequester controller;
	WindowListener listener;

	public RequesterFrame(String title, Component owner, JComponent content, BasicRequester req) {
		super(title);

		this.content = content;
		this.controller = req;

		Container c = this.getContentPane();

		c.setLayout(new BorderLayout());

		// this looks a bit crappy
		JLabel bigTitle = new JLabel(title);
		bigTitle.setFont(new Font("Dialog", Font.BOLD, 18));
		bigTitle.setBorder(new CompoundBorder(new EmptyBorder(3, 3, 3, 3), new LineBorder(Color.black, 1)));
		c.add(bigTitle, BorderLayout.NORTH);

		c.add(content, BorderLayout.CENTER);

		JPanel buttons = new JPanel();

		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.add(Box.createHorizontalGlue());
		buttons.add(new JButton(new RequestOkAction(this)));
		buttons.add(Box.createHorizontalStrut(8));
		buttons.add(new JButton(new RequestCancelAction(this)));

		c.add(buttons, BorderLayout.PAGE_END);

		listener = new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				((JFrame) e.getSource()).dispose();
				controller.requestCancelled();
			}
		};

		addWindowListener(listener);

		// FIXME: get location from req?
		if (owner != null) {
			setLocationRelativeTo(owner);
		}

		pack();
	}

	@Override
	public void requestCancelled() {
		removeWindowListener(listener);
		dispose();
		controller.requestCancelled();
	}

	@Override
	public void requestOk() {
		removeWindowListener(listener);
		dispose();
		controller.requestOk();
	}
}
