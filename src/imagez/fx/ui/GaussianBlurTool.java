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

import imagez.fx.GaussianBlurFX;
import imagez.image.ZLayer;
import imagez.ui.ImageView;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 *
 * @author notzed
 */
public class GaussianBlurTool extends JPanel implements Runnable, ChangeListener, BasicRequester {

	JSlider slider;
	//
	ImageView imageView;
	ZLayer targetLayer;
	ZLayer tmpLayer;
	//
	GaussianBlurFX blurEngine;

	public GaussianBlurTool(ImageView imageView) {
		this.imageView = imageView;
		targetLayer = imageView.getCurrentLayer();
		tmpLayer = targetLayer.aquireTmpLayer(ZLayer.TMP_REPLACE);

		setLayout(new BorderLayout());
		slider = new JSlider(1, 20000);
		add(slider, BorderLayout.SOUTH);
		slider.addChangeListener(this);

		//
		blurEngine = new GaussianBlurFX(targetLayer, tmpLayer, targetLayer.getBounds());

		SwingUtilities.invokeLater(this);
	}
	Thread thread;

	public void run() {
		long now = System.currentTimeMillis();
		float sigma = (slider.getValue() / 1000.0f);

		blurEngine.startBlur(sigma);
		blurEngine.waitBlur();

		now = System.currentTimeMillis() - now;
		System.out.printf("sigma %f took %03d.%03ds\n", sigma, now / 1000, now % 1000);

		targetLayer.addDamage(targetLayer.getBounds());
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		SwingUtilities.invokeLater(this);
	}
	/*
	void applySettings() {
	float sigma = (slider.getValue() / 5.0f);

	long now = System.currentTimeMillis();

	float[] kernel = getKernel(sigma);

	RGBALayer layer = (RGBALayer) targetLayer;
	Rectangle bounds = layer.bounds;
	float[] rgba = layer.getData();

	if (true) {
	float[] tmp = new float[bounds.width * bounds.height];

	float[] r = new float[bounds.width * bounds.height];
	float[] g = new float[bounds.width * bounds.height];
	float[] b = new float[bounds.width * bounds.height];
	float[] a = new float[bounds.width * bounds.height];

	int len = r.length;
	for (int i = 0; i < len; i++) {
	r[i] = rgba[i * 4 + 0];
	g[i] = rgba[i * 4 + 1];
	b[i] = rgba[i * 4 + 2];
	a[i] = rgba[i * 4 + 3];
	}

	blur16b(r, tmp, bounds, kernel, kernel.length);
	blur16b(g, tmp, bounds, kernel, kernel.length);
	blur16b(b, tmp, bounds, kernel, kernel.length);
	blur16b(a, tmp, bounds, kernel, kernel.length);

	now = System.currentTimeMillis() - now;
	System.out.printf("sigma %f size %d took %03d.%03ds\n", sigma, kernel.length, now / 1000, now % 1000);

	// write back to temp layer
	float[] data = tmpLayer.getData();
	for (int i = 0; i < len; i++) {
	data[i * 4 + 0] = r[i];
	data[i * 4 + 1] = g[i];
	data[i * 4 + 2] = b[i];
	data[i * 4 + 3] = a[i];
	}

	imageView.getImage().addDamage(bounds);

	if (false) {
	// cheat a bit, write back to zimage directly
	//				int[] aa = ((DataBufferInt) zimage.getImage().getRaster().getDataBuffer()).getData();
	for (int i = 0; i < len; i++) {
	int R = (int) (r[i] * 255.0);
	int G = (int) (g[i] * 255.0);
	int B = (int) (b[i] * 255.0);
	int A = (int) (a[i] * 255.0);

	//					aa[i] = (A << 24) | (R << 16) | (G << 8) | B;
	}
	}
	} else {
	float[] tmp = new float[bounds.width * bounds.height * 4];

	blur16rgba(rgba, tmp, tmpLayer.getData(), bounds, kernel, kernel.length);

	now = System.currentTimeMillis() - now;
	System.out.printf("sigma %f size %d took %03d.%03ds\n", sigma, kernel.length, now / 1000, now % 1000);

	//System.arraycopy(tmp, 0, tmpLayer.getData(), 0, rgba.length);
	//zimage.addDamage(bounds);
	//zimage.refresh(bounds);
	//label.repaint();
	}
	//label.repaint();
	}

	float[] blur16rgba(float[] src, float[] tmp, float[] dst, Rectangle bounds, float kernel[], int length) {
	int pos = 0;
	int pmod = length;
	int w = bounds.width;
	int h = bounds.height;

	// Perform horizontal
	for (int j = 0; j < h; j++) {
	for (int i = 0; i < w - length; i++) {
	float vr = src[pos * 4 + 0] * kernel[0];
	float vg = src[pos * 4 + 1] * kernel[0];
	float vb = src[pos * 4 + 2] * kernel[0];
	float va = src[pos * 4 + 3] * kernel[0];

	for (int k = 1; k < length; k++) {
	vr += src[(pos + k) * 4 + 0] * kernel[k];
	vg += src[(pos + k) * 4 + 1] * kernel[k];
	vb += src[(pos + k) * 4 + 2] * kernel[k];
	va += src[(pos + k) * 4 + 3] * kernel[k];
	}
	tmp[(pos + length / 2 - 1) * 4 + 0] = vr;
	tmp[(pos + length / 2 - 1) * 4 + 1] = vg;
	tmp[(pos + length / 2 - 1) * 4 + 2] = vb;
	tmp[(pos + length / 2 - 1) * 4 + 3] = va;
	pos++;
	}
	pos += pmod;
	}

	for (int i = 0; i < w; i += 64) {
	pos = i;
	for (int j = 0; j < h - length; j++) {
	for (int l = 0; l < 64; l++) {
	float vr = 0;
	float vg = 0;
	float vb = 0;
	float va = 0;
	for (int k = 0; k < length; k++) {
	vr += tmp[(pos + l + k * w) * 4 + 0] * kernel[k];
	vg += tmp[(pos + l + k * w) * 4 + 1] * kernel[k];
	vb += tmp[(pos + l + k * w) * 4 + 2] * kernel[k];
	va += tmp[(pos + l + k * w) * 4 + 3] * kernel[k];
	}
	dst[(pos + l + (length / 2 - 1) * w) * 4 + 0] = vr;
	dst[(pos + l + (length / 2 - 1) * w) * 4 + 1] = vg;
	dst[(pos + l + (length / 2 - 1) * w) * 4 + 2] = vb;
	dst[(pos + l + (length / 2 - 1) * w) * 4 + 3] = va;
	}
	pos += w;
	}
	}

	return dst;
	}

	// could extend to do rgba at once?
	float[] blur16b(float[] src, float[] dst, Rectangle bounds, float kernel[], int length) {
	int pos = 0;
	int pmod = length;
	int w = bounds.width;
	int h = bounds.height;

	// Perform horizontal
	for (int j = 0; j < h; j++) {
	for (int i = 0; i < w - length; i++) {
	float v = src[pos] * kernel[0];
	for (int k = 1; k < length; k++) {
	v += src[pos + k] * kernel[k];
	}
	dst[pos + length / 2 - 1] = v;
	pos++;
	}
	pos += pmod;
	}

	for (int i = 0; i < w; i += 64) {
	pos = i;
	for (int j = 0; j < h - length; j++) {
	for (int l = 0; l < 64; l++) {
	float v = 0;
	for (int k = 0; k < length; k++) {
	v += dst[pos + l + k * w] * kernel[k];
	}
	src[pos + l + (length / 2 - 1) * w] = v;
	}
	pos += w;
	}
	}

	return dst;
	}

	float[] getKernel(float sigma) {
	int length = (int) (Math.ceil(sigma * 6 + 1));
	//length = 32;
	float[] kernel = new float[length];

	double sig = -1.0 / (2 * sigma * sigma);
	//double fact = 1.0 / Math.sqrt(2 * Math.PI * sigma * sigma);
	float energy = 0;
	for (int j = 0; j < length; j++) {
	int y = (j - length / 2);
	float v = (float) Math.exp(sig * (y * y));

	kernel[j] = v;
	energy += v;
	}
	for (int j = 0; j < length; j++) {
	kernel[j] /= energy;

	//System.out.printf("%2d: %f\n", j, kernel[j]);
	}

	return kernel;
	}
	 */

	@Override
	public void requestCancelled() {
		targetLayer.releaseTmpLayer(tmpLayer, false, null);
	}

	@Override
	public void requestOk() {
		targetLayer.releaseTmpLayer(tmpLayer, true, null);
	}
}
