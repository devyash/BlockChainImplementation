import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class BlockChain {
	public HashMap<String, Wallet> wallet;
	public LinkedHashMap<String, Transaction> block;
	List <Block> blockChain = new LinkedList<Block>();
	public LinkedHashMap<String, Transaction> currentBlock;
	public boolean firstTransaction;

	BlockChain() {
		this.wallet = new HashMap<String, Wallet>();
		this.block = new LinkedHashMap<String, Transaction>();
		this.currentBlock = new LinkedHashMap<String, Transaction>();
		this.firstTransaction = true;
		this.blockChain = new LinkedList<Block>();
	}

	public void wipeBlockChain() {
		this.currentBlock = new LinkedHashMap<String, Transaction>();
	}
}
