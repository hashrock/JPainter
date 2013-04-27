package imagez.fx.dwt;

/**
 * Kingsbury Q-filters for the dual-tree complex DWT
 * @author notzed
 */
public class DualFilt1 extends Filter {

	DualFilt1() {
		H0l = new float[]{
			0.03516384000000f,
			0f,
			-0.08832942000000f,
			0.23389032000000f,
			0.76027237000000f,
			0.58751830000000f,
			0f,
			-0.11430184000000f,
			0f,
			0};
		H0h = new float[]{
			0f,
			0f,
			-0.11430184000000f,
			0f,
			0.58751830000000f,
			-0.76027237000000f,
			0.23389032000000f,
			0.08832942000000f,
			0f,
			-0.03516384000000f
		};
		H1l = new float[]{
			0f,
			0f,
			-0.11430184000000f,
			0f,
			0.58751830000000f,
			0.76027237000000f,
			0.23389032000000f,
			-0.08832942000000f,
			0f,
			0.03516384000000f
		};
		H1h = new float[]{
			-0.03516384000000f,
			0f,
			0.08832942000000f,
			0.23389032000000f,
			-0.76027237000000f,
			0.58751830000000f,
			0f,
			-0.11430184000000f,
			0f,
			0
		};

		G0l = invert(H0l);
		G0h = invert(H0h);
		G1l = invert(H1l);
		G1h = invert(H1h);
	}
}
