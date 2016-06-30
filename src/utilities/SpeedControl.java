package utilities;

/**
 * speed control
 * use init(s,f) to set the frame/step ratio
 * NOTE: s must <= f
 * call start() to reset counters
 * call isMove per frame to see if step are to be taken
 */
public class SpeedControl {
	// move steps per frames
	int steps;
	int frames;

	int frameCount;
	int stepCount;

	float frameStepRatio;

	public SpeedControl() {
		this.start(1, 1);
	}

	public void start(int s, int f) throws Error {
		if (f < s)
			throw new Error("SpeedControl.init(...): frame must >= step");

		this.steps = s;
		this.frames = f;
		this.frameStepRatio = (float) this.frames / (float) this.steps;

		this.stepCount = this.steps;
		this.frameCount = this.frames;
	}

	// return 1 if move, 0 not move
	public int isMove() {
		this.frameCount--;

		float ratio = (float) this.frameCount / (float) this.stepCount;

		if (this.frameCount == 0)
			this.frameCount = this.frames;

		if (ratio < this.frameStepRatio) {
			this.stepCount--;
			if (this.stepCount == 0)
				this.stepCount = this.steps;
			return (1);
		}
		return (0);
	}
	// (c) 2016 Joshua Sonnet
}
