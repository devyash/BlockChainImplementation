import java.io.IOException;

public class ledger {
	public static void main(String[] args) throws IOException {
		boolean interactiveMode = false;
		boolean verboseMode = false;
		InteractiveMode im = new InteractiveMode();
		BlockChain bc = new BlockChain();
		im.showScreen(bc, interactiveMode, verboseMode);
	}
}
