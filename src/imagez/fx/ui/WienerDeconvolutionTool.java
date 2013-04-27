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

import imagez.fx.WienerDeconvolution;
import imagez.image.ZLayer;
import imagez.ui.ImageView;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author notzed
 */
public class WienerDeconvolutionTool extends JPanel implements Runnable, ChangeListener, BasicRequester {

	JSlider slider;
	JSlider kslider;
	//
	ImageView imageView;
	ZLayer targetLayer;
	ZLayer tmpLayer;
	//
	WienerDeconvolution engine;

	public WienerDeconvolutionTool(ImageView imageView) {
		this.imageView = imageView;
		targetLayer = imageView.getCurrentLayer();
		tmpLayer = targetLayer.aquireTmpLayer(ZLayer.TMP_REPLACE);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		slider = new JSlider(1000, 10000);
		add(slider);
		slider.addChangeListener(this);
		kslider = new JSlider(1, 1000);
		add(kslider);
		kslider.addChangeListener(this);

		engine = new WienerDeconvolution(targetLayer, tmpLayer);

		SwingUtilities.invokeLater(this);
	}
	Thread thread;

	public void run() {
		long now = System.currentTimeMillis();
		float sigma = (slider.getValue() / 1000.0f);
		float k = (kslider.getValue() / 1000.0f);

		engine.deconvolveMT(sigma, k);

		now = System.currentTimeMillis() - now;
		System.out.printf("sigma %f k %f took %03d.%03ds\n", sigma, k, now / 1000, now % 1000);

		targetLayer.addDamage(targetLayer.getBounds());
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		SwingUtilities.invokeLater(this);
	}

	@Override
	public void requestCancelled() {
		targetLayer.releaseTmpLayer(tmpLayer, false, null);
	}

	@Override
	public void requestOk() {
		targetLayer.releaseTmpLayer(tmpLayer, true, null);
	}
}
