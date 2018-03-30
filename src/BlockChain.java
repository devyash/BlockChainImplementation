import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;


public class BlockChain {
	public HashMap<String, WalletClass> wallet;
	public LinkedHashMap<String, Transaction> block;
	List <Block> blockChain = new LinkedList<Block>();
	public LinkedHashMap<String, Transaction> currentBlock;
	public boolean firstTransaction;

	BlockChain() {
		this.wallet = new HashMap<String, WalletClass>();
		this.block = new LinkedHashMap<String, Transaction>();
		this.currentBlock = new LinkedHashMap<String, Transaction>();
		this.firstTransaction = true;
		this.blockChain = new LinkedList<Block>();
	}

	public void wipeBlockChain() {
		Iterator itr = this.currentBlock.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry curr = (Map.Entry) itr.next();
			Transaction ct = (Transaction) curr.getValue();
			this.block.remove(ct.txid);
		}
		this.currentBlock = new LinkedHashMap<String, Transaction>();
	}
}
