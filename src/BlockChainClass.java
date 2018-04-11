import java.util.HashMap;
import java.util.LinkedHashMap;

public class BlockChainClass {
	public HashMap<String, Integer> wallet;
	public LinkedHashMap<String, TransactionClass> block;
	public boolean firstTransactionClass;

	BlockChainClass() {
		this.wallet = new HashMap<String, Integer>();
		this.block = new LinkedHashMap<String, TransactionClass>();
		this.firstTransactionClass = true;
	}

	public void whipeBlockChain() {
		this.wallet = new HashMap<String, Integer>();
		this.block = new LinkedHashMap<String, TransactionClass>();
		;
		this.firstTransactionClass = true;
	}
}
