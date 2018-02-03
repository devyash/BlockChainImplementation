import java.util.HashMap;

import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

public class BlockChain {
	HashMap<String,Integer> account;
	HashMap<String, List> transactionsinput;
	HashMap<String, List> transactionsoutput;
	boolean firstTransaction;
	
	BlockChain(){
		this.account = new HashMap();
		this.transactionsinput = new HashMap();
		this.transactionsoutput = new HashMap();
		this.firstTransaction=true;
	}
	
	public void whipeBlockChain(){
		this.account = new HashMap();
		this.transactionsinput = new HashMap();
		this.transactionsoutput = new HashMap();
		this.firstTransaction = true;
	}
}
