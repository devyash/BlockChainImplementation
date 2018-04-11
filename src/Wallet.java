import java.io.IOException;
import java.util.HashMap;

public class wallet {
	public static void main(String[] args) throws IOException {
		HashMap <String, String> accountKeys = new HashMap();
		boolean interactiveMode = false;
		boolean verboseMode = false;
		InteractiveModeClass im = new InteractiveModeClass();
		BlockChainClass bc = new BlockChainClass();
		im.showScreen(bc, interactiveMode, verboseMode, accountKeys);
	}
}

// /Users/devyash/eclipse-workspace/SimplifiedBitcoin/transactions.txt

///Users/devyash/eclipse-workspace/Wallet/aliceprivate_key.pem
