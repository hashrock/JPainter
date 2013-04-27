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

import imagez.fx.dct.DCTDenoise;
import imagez.image.ZLayer;
import imagez.ui.ImageView;
import java.awt.Cursor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author notzed
 */
public class DCTDenoiseTool extends JPanel implements Runnable, ChangeListener, BasicRequester {

	JSlider slider;
	//
	ImageView imageView;
	ZLayer targetLayer;
	ZLayer tmpLayer;
	//
	DCTDenoise engine;

	public DCTDenoiseTool(ImageView imageView) {
		this.imageView = imageView;
		targetLayer = imageView.getCurrentLayer();
		tmpLayer = targetLayer.aquireTmpLayer(ZLayer.TMP_REPLACE);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(slider = new JSlider(0, 1000));
		slider.addChangeListener(this);

		engine = new DCTDenoise(targetLayer, tmpLayer);

		pool = Executors.newSingleThreadExecutor();
		stateChanged(null);
	}
	ExecutorService pool;
	Future worker;

	public void run() {
		long now = System.currentTimeMillis();
		float sigma = (slider.getValue() / 10000.0f);

		engine.denoise(sigma);

		now = System.currentTimeMillis() - now;

		System.out.printf("dct denoise: sigma %f took %03d.%03ds\n", sigma, now / 1000, now % 1000);

		targetLayer.addDamage(targetLayer.getBounds());

		imageView.setCursor(Cursor.getDefaultCursor());
		this.setCursor(Cursor.getDefaultCursor());
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (slider.getValueIsAdjusting())
			return;
		//SwingUtilities.invokeLater(this);
		if (worker != null) {
			worker.cancel(true);
		}
		imageView.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		worker = pool.submit(this);
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
