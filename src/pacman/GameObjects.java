package pacman;

public class GameObjects {

	// Player (pacman) entity
	class PacMan {
		int x, y;
		int diam;

		public PacMan(int d) {
			this.diam = d;
			this.x = d / 2;
			this.y = d / 2;
		}

		public void draw() {

		}
	}

	// Ghost entity
	class Ghost {
		int x, y;

		public void draw() {

		}
	}

}
