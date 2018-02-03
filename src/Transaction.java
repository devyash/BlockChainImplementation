import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Transaction {

	String txid;
	String oldTxid;
	int oldTxidUtxo;
	int M;
	int N;
	List<UTXO> utxos;

	Transaction(String txid, String oldTxid, int oldTxidUtxo, int M, int N, List<UTXO> utxos) {
		this.txid = txid;
		this.oldTxid = oldTxid;
		this.oldTxidUtxo = oldTxidUtxo;
		this.M = M;
		this.N = N;
		this.utxos = utxos;

	}
	
	public String toString() {
		//4787df35; 1; (f2cea539, 0); 3; (Bob, 150)(Alice, 845)(Gopesh, 5)
		String res = this.txid+"; "+this.M+"; "+"("+this.oldTxid+", "+this.oldTxidUtxo+")"+"; "+this.N+"; ";
		for(UTXO utxo: this.utxos) {
			res+="("+utxo.account+", "+utxo.value+")"; 
		}
		return res;
		
	}

	public static Transaction parseTransaction(String transaction, boolean verboseMode, BlockChain bc) {
		// f2cea539; 0; ; 1; (Alice, 1000)
		// 4787df35; 1; (f2cea539, 0); 3; (Bob, 150)(Alice, 845)(Gopesh, 5)
		String[] input = null;
		String txid = "";
		String oldTxid = "";
		int oldTxidUtxo = 0;
		int M = 0;
		int N = 0;
		List<UTXO> utxos = null;
		String outpututxos = "";

		String[] outputList;
		try {
			input = transaction.split(";");
			txid = input[0].trim();
			M = Integer.parseInt(input[1].trim());
			N = Integer.parseInt(input[3].trim());
			utxos = new LinkedList<UTXO>();
			outpututxos = input[4].trim();

			outputList = outpututxos.split("\\)");

			// There should be N outputs
			if (outputList.length != N)
				throw new Exception();

			for (int i = 0; i < N; i++) {
				String oneUTXO = outputList[i].substring(1); // remove '('
				UTXO utxo = new UTXO(oneUTXO.split(",")[0], Integer.parseInt(oneUTXO.split(",")[1].trim()));
				utxos.add(utxo);
			}

			if (bc.firstTransaction) {
				if (M != 0)
					throw new Exception();
				bc.firstTransaction = !bc.firstTransaction;
			} else {
				oldTxid = input[2].trim().substring(1, input[2].length() - 2).split(",")[0];
				oldTxidUtxo = Integer.parseInt(input[2].trim().substring(1, input[2].length() - 2).split(",")[1].trim());
				if (M != 1)
					throw new Exception();
			}
			//if(verboseMode)
				//System.out.println("Parsed: "+transaction);
		} catch (Exception e) {
//			System.out.println(e+": "+e.getStackTrace());
			System.out.println("Error: Invalid Format of Transaction - " + transaction);
		}

		return new Transaction(txid, oldTxid, oldTxidUtxo, M, N, utxos);
	}

	public static String printAllTransaction(BlockChain bc, boolean verboseMode) {
		StringBuilder sb = new StringBuilder();
		Iterator itr = bc.block.entrySet().iterator();
		boolean firstTransaction = true; 
		while(itr.hasNext()) {
			Map.Entry curr =(Map.Entry) itr.next();
			Transaction ct = (Transaction) curr.getValue();
			if(!firstTransaction)
				sb.append(ct.toString()).append("\n");
			else {
				firstTransaction=false;
				String res = ct.txid+"; "+ct.M+"; "+"; "+ct.N+"; ";
				for(UTXO utxo: ct.utxos) 
					res+="("+utxo.account+", "+utxo.value+")"; 
				
				sb.append(res).append("\n");
			}
			
		}
		return sb.toString();
	}

	public static boolean executeTransaction(Transaction t, BlockChain bc, boolean verboseMode) {
		if(verifyTransaction(t, bc, verboseMode)) {
			bc.block.put(t.txid, t);
			if(t.oldTxid.length()!=0) {
				UTXO oldUtxo= bc.block.get(t.oldTxid).utxos.get(t.oldTxidUtxo);
//				bc.wallet.put(oldUtxo.account,bc.wallet.get(oldUtxo.account)-oldUtxo.value); //Subtract old value
				for (UTXO utxo : t.utxos) {
					if(bc.wallet.containsKey(utxo.account)) {
						bc.wallet.put(utxo.account,bc.wallet.get(utxo.account)+utxo.value);
					}
					else
						bc.wallet.put(utxo.account,utxo.value);
				}	
			}
			return true;
		}
			return false;
	}

	private static boolean verifyTransaction(Transaction t, BlockChain bc, boolean verboseMode) {
		try {
			if(bc.block.containsKey(t.txid)) {	
				throwError("Error: Transaction Id already exists",verboseMode);
			}
			if (t.oldTxid.length()!=0 ) {
				if(!bc.block.containsKey(t.oldTxid)) {
					throwError("Error: Old Transaction does not exist!",verboseMode);
				}
				else if(bc.block.get(t.oldTxid).N<t.oldTxidUtxo){
					throwError("Error: Old Transaction UTXO does not exist! ",verboseMode);
				}
				else if (getTotalUtxoSum(t)!=get1UtxoSum(bc.block.get(t.oldTxid), t.oldTxidUtxo)) {
					throwError("Error: Invalid Amount of UTXO",verboseMode);
				}
				else if(bc.block.get(t.oldTxid).utxos.get(t.oldTxidUtxo).spent) {
					throwError("Error: UTXO already spent",verboseMode);
				}
			}
			
		}
		catch(Exception e) {
			return false;
		}
		
		return true;
	}
	
	private static int getTotalUtxoSum(Transaction t) {
		int sum = 0;
		for(UTXO utxo: t.utxos) {
			sum+=utxo.value;
		}
		return sum;
	}

	private static int get1UtxoSum(Transaction t, int oldTxidUtxo) {
		return t.utxos.get(oldTxidUtxo).value;
	}

	private static void throwError(String message, boolean verboseMode) throws Exception{
		if(verboseMode)
			System.out.println(message);
		throw new Exception();
	}
// /Users/devyash/eclipse-workspace/SimplifiedBitcoin/transactions.txt
}
