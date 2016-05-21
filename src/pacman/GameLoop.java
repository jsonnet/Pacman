package pacman;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GameLoop extends JPanel {

	// private static final long serialVersionUID = 1L;

	/* RENDER */
	private boolean running = false;
	private boolean paused = false;
	private int actualFps = (int) Preferences.TARGET_FPS;
	// Current FPS Count
	private int frameCount = 0;
	// Delta of gameLoop and fps difference
	private float interpolation;
	private int lastDrawX, lastDrawY;

	/* PHYSICS */

	/* OBJECTS */
	// Copysave objects
	private JFrame jframe;
	private GameObjects gameObjects;

	public GameLoop(JFrame jframe, GameObjects gameObjects) {
		this.jframe = jframe;
		this.gameObjects = gameObjects;

		// Start the main game
		this.running = true;
		this.runGameLoop();
	}

	// Starts a new thread and runs the game loop in it
	private void runGameLoop() {
		Thread loop = new Thread() {
			@Override
			public void run() {
				GameLoop.this.gameLoop();
			}
		};
		loop.start();
	}

	// Only run this in another Thread!
	private void gameLoop() {
		// We will need the last update time
		double lastUpdateTime = System.nanoTime();
		// Store the last time we rendered
		double lastRenderTime = System.nanoTime();

		// Simple way of finding FPS
		int lastSecondTime = (int) (lastUpdateTime / 1000000000);

		while (this.running) {
			double now = System.nanoTime();
			int updateCount = 0;

			if (!this.paused) {
				// Do as many game updates as we need to, potentially playing catchup
				while (now - lastUpdateTime > Preferences.TIME_BETWEEN_UPDATES && updateCount < Preferences.MAX_UPDATES_BEFORE_RENDER) {
					this.updateGame();
					lastUpdateTime += Preferences.TIME_BETWEEN_UPDATES;
					updateCount++;
				}

				// If for some reason an update takes forever, we don't want to do an insane number of catchups
				if (now - lastUpdateTime > Preferences.TIME_BETWEEN_UPDATES)
					lastUpdateTime = now - Preferences.TIME_BETWEEN_UPDATES;

				// Render. To do so, we need to calculate interpolation for a smooth render
				float interpolation = Math.min(1.0f, (float) ((now - lastUpdateTime) / Preferences.TIME_BETWEEN_UPDATES));
				this.drawGame(interpolation);
				lastRenderTime = now;

				// Update the frames we got
				int thisSecond = (int) (lastUpdateTime / 1000000000);
				if (thisSecond > lastSecondTime) {
					// LogHelper.log("NEW SECOND " + thisSecond + " " + this.frameCount);
					this.actualFps = this.frameCount;
					this.frameCount = 0;
					lastSecondTime = thisSecond;
				}

				// Yield until it has been at least the target time between renders. This saves the CPU from hogging
				while (now - lastRenderTime < Preferences.TARGET_TIME_BETWEEN_RENDERS && now - lastUpdateTime < Preferences.TIME_BETWEEN_UPDATES) {
					Thread.yield();

					// This stops the app from consuming all your CPU. It makes this slightly less accurate, but is worth it
					// You can remove this line and it will still work (better), your CPU just climbs on certain OSes
					// FYI on some OS's this can cause pretty bad stuttering
					try {
						Thread.sleep(1);
					} catch (Exception e) {
					}

					now = System.nanoTime();
				}
			}
		}
	}

	// Game ticks with physics
	private void updateGame() {
		// TODO update each tick
		this.jframe.repaint();
	}

	private void setInterpolation(float interp) {
		this.interpolation = interp;
	}

	private void drawGame(float interpolation) {
		this.setInterpolation(interpolation);
		this.jframe.repaint();
	}

	private void render(Graphics g) {
		// TODO render each tick
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.render(g);

		// Render some text to the screen
		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", 1, 10));
		g.drawString("FPS: " + this.actualFps, 5, 10);
		g.drawString("© 2016 Joshua Sonnet", Preferences.HEIGHT - 21 - 100, Preferences.WIDTH - 35);

		// Count fps
		this.frameCount++;
	}

	// (c) 2016 Joshua Sonnet
}
