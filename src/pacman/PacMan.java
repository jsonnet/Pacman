package pacman;

import javax.swing.JFrame;

public class PacMan {
	// Game Object
	private static PacMan pacMan;

	private PacMan() {
		// Create window frame
		JFrame jframe = new JFrame();

		// Create window object to handle everything
		Window window = new Window();
		// Create gameObjects for the knowledge of how to render
		GameObjects gameObjects = new GameObjects();
		// Create the gameLoop to run the game
		GameLoop gameLoop = new GameLoop(jframe, gameObjects);

		// Sets all needed params of the JFrame and adds the game to the panel
		jframe = window.createWindow(jframe, gameLoop);
	}

	public static void main(String[] args) {
		// Create the game
		PacMan.pacMan = new PacMan();
	}

	// (c) 2016 Joshua Sonnet
}
