package pacman;

public class PacMan {
	// Game Object
	@SuppressWarnings("unused")
	private static GameMainClass pacMan;

	private PacMan() {
	}

	public static void main(String[] args) {
		// Create the game
		PacMan.pacMan = new GameMainClass();
	}

	// (c) 2016 Joshua Sonnet
}
