import java.util.HashMap;
import java.util.LinkedHashMap;

public class BlockChain {
	public HashMap<String, Integer> wallet;
	public LinkedHashMap<String, Transaction> block;
	public boolean firstTransaction;

	BlockChain() {
		this.wallet = new HashMap<String, Integer>();
		this.block = new LinkedHashMap<String, Transaction>();
		this.firstTransaction = true;
	}

	public void whipeBlockChain() {
		this.wallet = new HashMap<String, Integer>();
		this.block = new LinkedHashMap<String, Transaction>();
		;
		this.firstTransaction = true;
	}
}
