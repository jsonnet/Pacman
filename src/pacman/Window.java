package pacman;

import javax.swing.JFrame;

public class Window { // implements MouseListener, KeyListener

	// Copy of the gameLoop instance
	private GameLoop gameLoop;

	protected JFrame createWindow(JFrame jframe, GameLoop gameLoop) {
		this.gameLoop = gameLoop;

		// Should be self explanatory
		jframe.add(gameLoop);
		jframe.setTitle("Pacman");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setSize(Preferences.WIDTH, Preferences.HEIGHT);
		// jframe.addMouseListener(this);
		// jframe.addKeyListener(this);
		jframe.setResizable(false);
		jframe.setVisible(true);

		return jframe;
	}
}
