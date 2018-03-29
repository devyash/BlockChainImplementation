import java.io.IOException;

public class txblk {
	public static void main(String[] args) throws IOException {
		boolean interactiveMode = false;
		boolean verboseMode = false;
		InteractiveMode im = new InteractiveMode();
		BlockChain bc = new BlockChain();
		im.showScreen(bc, interactiveMode, verboseMode);
	}
}

// /Users/devyash/eclipse-workspace/SimplifiedBitcoin/transactions.txt


