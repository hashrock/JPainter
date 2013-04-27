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

import imagez.image.ZLayer;
import imagez.ui.ImageView;
import java.awt.Rectangle;
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
public class BrightnessContrast extends JPanel implements ChangeListener, Runnable, BasicRequester {

	JSlider brightness;
	JSlider contrast;
//
	ImageView imageView;
	ZLayer layer;
	ZLayer tmpLayer;

	public BrightnessContrast(ImageView imageView) {
		this.imageView = imageView;
		layer = imageView.getCurrentLayer();
		tmpLayer = layer.aquireTmpLayer(ZLayer.TMP_REPLACE);

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		brightness = new JSlider(-5000, 5000);
		contrast = new JSlider(-5000, 5000);

		this.add(brightness);
		this.add(contrast);

		brightness.addChangeListener(this);
		contrast.addChangeListener(this);

		SwingUtilities.invokeLater(this);
	}

	void applySettings() {
		//float[] src = ((RGBALayer)layer).getData();
		//float[] dst = tmpLayer.getData();
		Rectangle bounds = layer.getBounds();

		int x = bounds.x;
		int y = bounds.y;
		int w = bounds.width;
		int h = bounds.height;

		float [] src = new float[w*4];
		float [] dst = new float[w*4];

		// TODO: want layer mask stuff

		// Since we are pre-mult alpha, multiply the offset by alpha to make the maths work

		float c = getContrast() + 0.5f;
		float scale = c <= 0.5f ? c / 0.5f : 0.5f / (1.0f - c);
		float offset = getBrigthness();
		for (int j = 0;j<h;j++) {
			layer.getLineRGBA(src, 0, x, j+y, w);
			int pos = 0;

			for (int i =0;i<w;i++) {
				float a = src[pos+3];
				float r = src[pos+0] * scale + offset * a;
				float g = src[pos+1] * scale + offset * a;
				float b = src[pos+2] * scale + offset * a;

				r = Math.min(1, r);
				g = Math.min(1, g);
				b = Math.min(1, b);
				r = Math.max(0, r);
				g = Math.max(0, g);
				b = Math.max(0, b);

				dst[pos+0] = r;
				dst[pos+1] = g;
				dst[pos+2] = b;
				dst[pos+3] = a;
				pos+=4;
			}
			tmpLayer.setLineRGBA(dst, 0, x, y+j, w);
		}
		layer.addDamage(bounds);
	}

	@Override
	public void run() {
		applySettings();
	}

	public void setImageView(ImageView imageView) {
	}

	public float getBrigthness() {
		return brightness.getValue() / 10000.0f;
	}
	public float getContrast() {
		return contrast.getValue() / 10000.0f;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		SwingUtilities.invokeLater(this);
	}

	@Override
	public void requestCancelled() {
		layer.releaseTmpLayer(tmpLayer, false, null);
	}

	@Override
	public void requestOk() {
		layer.releaseTmpLayer(tmpLayer, true, tmpLayer.getBounds());
	}
}
