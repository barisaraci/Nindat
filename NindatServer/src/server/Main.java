package server;

public class Main {
	
	private static Terminal terminal;
	
	public static void main(String args[]) {
		terminal = new Terminal();
		terminal.print("Port is 3333 (TCP)", Terminal.Status.WARNING);
		
		Game game = new Game(terminal);
		new Thread(game).start();
	}
	
}
