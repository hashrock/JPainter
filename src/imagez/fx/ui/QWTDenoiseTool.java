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

import imagez.fx.dwt.QWTDenoise;
import imagez.image.ZLayer;
import imagez.ui.ImageView;
import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author notzed
 */
public class QWTDenoiseTool extends JPanel implements Runnable, ChangeListener, BasicRequester {

	JSlider slider;
	JSlider scale;
	JSlider weight;
	JLabel sliderLabel;
	JLabel scaleLabel;
	JLabel weightLabel;
	//
	ImageView imageView;
	ZLayer targetLayer;
	ZLayer tmpLayer;
	//
	QWTDenoise engine;

	public QWTDenoiseTool(ImageView imageView) {
		this.imageView = imageView;
		targetLayer = imageView.getCurrentLayer();
		tmpLayer = targetLayer.aquireTmpLayer(ZLayer.TMP_REPLACE);

		setLayout(new GridBagLayout());

		add(new JLabel("Threshold (Smooth)"), c0);
		add(slider = new JSlider(0, 1000), c1);
		add(sliderLabel = new JLabel("0"), c2);

		add(new JLabel("Scale (Sharpen)"), c0);
		add(scale = new JSlider(0, 10000), c1);
		add(scaleLabel = new JLabel("0"), c2);

		add(new JLabel("Weight"), c0);
		add(weight = new JSlider(0, 10000), c1);
		add(weightLabel = new JLabel("0"), c2);

		slider.addChangeListener(this);
		weight.addChangeListener(this);
		scale.addChangeListener(this);

		engine = new QWTDenoise(targetLayer, tmpLayer);

		pool = Executors.newSingleThreadExecutor();
		stateChanged(null);
	}
	ExecutorService pool;
	Future worker;

	public void run() {
		long now = System.currentTimeMillis();
		float th = (slider.getValue() / 100000.0f) + 0.0001f;
		float sc = (scale.getValue() / 10000.0f);
		float we = (weight.getValue() / 10000.0f);

		try {
			engine.denoise(th, sc, we);
		} catch (Throwable t) {
			t.printStackTrace();
		}

		now = System.currentTimeMillis() - now;

		System.out.printf("dct denoise: th %f scale %f weight %f took %03d.%03ds\n", th, sc, we, now / 1000, now % 1000);

		targetLayer.addDamage(targetLayer.getBounds());

		imageView.setCursor(Cursor.getDefaultCursor());
		this.setCursor(Cursor.getDefaultCursor());
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		float th = (slider.getValue() / 100000.0f) + 0.0001f;
		float sc = (scale.getValue() / 10000.0f);
		float we = (weight.getValue() / 10000.0f);

		sliderLabel.setText(String.format("%8.5f", th));
		scaleLabel.setText(String.format("%8.5f", sc));
		weightLabel.setText(String.format("%8.5f", we));

		if (slider.getValueIsAdjusting()
				|| scale.getValueIsAdjusting()
				|| weight.getValueIsAdjusting()) {
			return;
		}
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
