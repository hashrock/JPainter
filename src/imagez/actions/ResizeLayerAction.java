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

import imagez.fx.ui.BasicRequester;
import imagez.fx.ui.RequesterFrame;
import imagez.image.ZImage;
import imagez.image.ZLayer;
import imagez.ui.ImageWindow;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Resize a layer
 * @author notzed
 */
public class ResizeLayerAction extends AbstractWindowAction {

	public ResizeLayerAction() {
		super("Resize Layer", null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ImageWindow win = getWindow(e);
		ZLayer layer = win.getImageView().getCurrentLayer();

		if (layer != null) {
			ResizeLayer resize = new ResizeLayer(layer);

			new RequesterFrame("Resize Layer", win, resize, resize).setVisible(true);
		}
	}

	enum ResizeMode {

		NEAREST {

			@Override
			public String toString() {
				return "Nearest Neighbour";
			}

			@Override
			Object getHint() {
				return RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
			}
		},
		BILINEAR {

			public String toString() {
				return "Bi-Linear";
			}

			@Override
			Object getHint() {
				return RenderingHints.VALUE_INTERPOLATION_BILINEAR;
			}
		},
		BICUBIC {

			public String toString() {
				return "Bi-Cubic";
			}

			@Override
			Object getHint() {
				return RenderingHints.VALUE_INTERPOLATION_BICUBIC;
			}
		};

		abstract Object getHint();
	}

	class ResizeLayer extends JPanel implements BasicRequester {

		private final ZLayer layer;
		JSpinner width;
		JSpinner height;
		JComboBox mode;

		public ResizeLayer(ZLayer layer) {
			this.layer = layer;

			setLayout(new GridBagLayout());

			Rectangle bounds = layer.getBounds();

			add(new JLabel("Width"), c0);
			add(width = new JSpinner(new SpinnerNumberModel(bounds.width, 1, 16384, 1)), c1);
			add(new JLabel("Height"), c0);
			add(height = new JSpinner(new SpinnerNumberModel(bounds.height, 1, 16384, 1)), c1);

			add(new JLabel("Interpolation"), c0);
			add(mode = new JComboBox(ResizeMode.values()), c1);
			mode.setSelectedItem(ResizeMode.BILINEAR);
		}

		@Override
		public void requestCancelled() {
			// do nothing
		}

		@Override
		public void requestOk() {
			int w = ((Number) width.getValue()).intValue();
			int h = ((Number) height.getValue()).intValue();
			
			layer.getZImage().getEditor().resizeLayer(layer, new Dimension(w, h), ((ResizeMode)mode.getSelectedItem()).getHint());
		}
	}
}
