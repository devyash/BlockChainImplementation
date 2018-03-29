import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Transaction {

	String txid;
//	String oldTxid;
//	int oldTxidUtxo;
	List<OldTransaction> oldts;
	int M;
	int N;
	List<UTXO> utxos;
	String signature;

	Transaction(String txid, List<OldTransaction> oldts, int M, int N, List<UTXO> utxos, String signature) {
		this.txid = txid;
		this.oldts = oldts;
		this.M = M;
		this.N = N;
		this.utxos = utxos;
		this.signature = signature;

	}

	public String toString() {
		// 4787df35; 1; (f2cea539, 0); 3; (Bob, 150)(Alice, 845)(Gopesh, 5)
		String res;
		res = this.txid + "; " + this.M + "; ";
		if(this.M > 0) {
			if(this.oldts!=null) {
				for(OldTransaction oldt : this.oldts) {
					if(oldt.txid!=null && oldt.pos>-1) {
						res += "("+oldt.txid+", " + oldt.pos + ")";
					}
				}
			}
		}
		res+="; " +this.N+ "; ";

		for (UTXO utxo : this.utxos) {
			res += "(" + utxo.account + ", " + utxo.value + ")";
		}
		return res;

	}

	public static Transaction parseTransaction(String transaction, boolean verboseMode, BlockChain bc, String signature) {
		// f2cea539; 0; ; 1; (Alice, 1000)
		// 4787df35; 1; (f2cea539, 0); 3; (Bob, 150)(Alice, 845)(Gopesh, 5)
		String[] input = null;
		String txid = "";
		int M = 0;
		int N = 0;
		List<UTXO> utxos = null;
		List<OldTransaction> oldts = new LinkedList<OldTransaction>();
		String outpututxos = "";
		

		String[] outputList;
		try {
			if(!transaction.endsWith(")"))
				throw new Exception();
			input = transaction.split(";");
			txid = input[0].trim();
			M = Integer.parseInt(input[1].trim());
			N = Integer.parseInt(input[3].trim());
			utxos = new LinkedList<UTXO>();
			oldts = new LinkedList<OldTransaction>();
			outpututxos = input[4].trim();
			outputList = outpututxos.split("\\)");

			// There should be N outputs
			if (outputList.length != N) {
				throwError(transaction.substring(0,8)+": Error N != Number of output UTXOs",verboseMode);
			}


			for (int i = 0; i < outputList.length; i++) {
//				if(!transaction.startsWith("("))
//					throw new Exception();
				String oneUTXO = outputList[i].substring(1); // remove '('
				UTXO utxo = new UTXO(oneUTXO.split(",")[0], Integer.parseInt(oneUTXO.split(",")[1].trim()));
				utxos.add(utxo);
			}

			if (bc.firstTransaction) {
				if (M != 0) {
					throwError(transaction.substring(0,8)+": " +"Error - M != Number of input UTXOs",verboseMode);
				}
				bc.firstTransaction = !bc.firstTransaction;
			} else {
				String listOfT=input[2].trim();
				String []ts=listOfT.split("\\)");

				if (ts.length != M)
					throwError(transaction.substring(0,8)+": Invalid number of Input UXTO", verboseMode);

				for (int i = 0; i < ts.length ; i++) {
					String oneOldT = ts[i].substring(1); // remove '('
					OldTransaction oldt = new OldTransaction(oneOldT.split(",")[0],Integer.parseInt(oneOldT.split(",")[1].trim()));
					oldts.add(oldt);
				}
			}

		} catch (Exception e) {
			if(transaction.length()>=8)
				System.err.println(transaction.substring(0,8)+": Transaction format Invalid, Exception: "+e);
			else
				System.err.println(transaction+": Transaction format Invalid, Exception: "+e);
			return null;
		}

		Transaction t = new Transaction(txid, oldts, M, N, utxos,signature);
		return t;
	}

	public static String printAllTransaction(BlockChain bc, boolean verboseMode) {
		StringBuilder sb = new StringBuilder();
		Iterator itr = bc.block.entrySet().iterator();
		boolean firstTransaction = true;
		while (itr.hasNext()) {
			Map.Entry curr = (Map.Entry) itr.next();
			Transaction ct = (Transaction) curr.getValue();
			sb.append(ct.toString()).append("\n");
		}
		return sb.toString();
	}
	public static String printAllCurrentTransactionWithSignature(BlockChain bc, boolean verboseMode) {
		StringBuilder sb = new StringBuilder();
		Iterator itr = bc.currentBlock.entrySet().iterator();
		boolean firstTransaction = true;
		while (itr.hasNext()) {
			Map.Entry curr = (Map.Entry) itr.next();
			Transaction ct = (Transaction) curr.getValue();
			if(ct.signature!=null && ct.signature.length()>2)
				sb.append(ct.toString()).append("\r\n").append(ct.signature).append("\r\n");
		}
		return sb.toString();
	}

	public static boolean executeTransaction(Transaction t, BlockChain bc, boolean verboseMode) {
		if (t == null || bc == null)
			return false;
		if (verifyTransaction(t, bc, verboseMode)) {
			t = verifyAndChangeTransactionId(t,bc,verboseMode);
			bc.block.put(t.txid, t);
			bc.currentBlock.put(t.txid, t);
			updateWallet(t,bc);
			return true;
		}
		return false;
	}

	static void updateWallet(Transaction t, BlockChain bc) {
		for(OldTransaction oldt : t.oldts) {
			Transaction prevT = bc.block.get(oldt.txid);
			UTXO prevUtxo = prevT.utxos.get(oldt.pos);
			prevUtxo.spent = true;
			bc.wallet.get(prevUtxo.account).value -= prevUtxo.value;
		}
		for (UTXO utxo : t.utxos) {
			if (bc.wallet.containsKey(utxo.account)) {
				bc.wallet.get(utxo.account).value +=utxo.value;
			} else
				bc.wallet.put(utxo.account, new Wallet(utxo.account,utxo.value));
		}
	}

	private static boolean verifyTransaction(Transaction t, BlockChain bc, boolean verboseMode) {
		try {
			if (bc.block.containsKey(t.txid)) {
				throwError(t.txid+": " +"Error - Transaction Id already exists", verboseMode);
			}
			 int inputSum = 0;
			 for(OldTransaction oldt : t.oldts) {
				 if (bc.block.containsKey(oldt.txid)) {
					 Transaction prevTransaction = bc.block.get(oldt.txid);
					 inputSum += prevTransaction.utxos.get(oldt.pos).value;
					if (prevTransaction.N <= oldt.pos ) {
						throwError(t.txid+": " +"Error Old Transaction UTXO does not exist! ", verboseMode);
					} else if (prevTransaction.utxos.get(oldt.pos).spent) {
						throwError(t.txid+": " +"Error - UTXO already spent", verboseMode);
					}
				}
				 else
					 throwError(t.txid+": " +"Error - Old Transaction does not exist!", verboseMode);
			 }
			 if(getTotalUtxoSum(t) != inputSum && t.M != 0) {
				 throwError(t.txid+": " +"Error - Input UTXO != output UTXO", verboseMode);
			 }

		} catch (Exception e) {
			return false;
		}

		return true;
	}

	private static Transaction verifyAndChangeTransactionId(Transaction t, BlockChain bc, boolean verboseMode) {
		 if(!checkSha1(t.toString().substring(0,8),getSha1(t.toString().substring(10))))
		 {
			 try {
				throwError(t.txid+": " +"Error - Wrong Transaction ID: "+t.txid+". New txid: "+getSha1(t.toString().substring(10)),true);
			} catch (Exception e) {
				t.txid = getSha1(t.toString().substring(10));
			}
		 }
		 return t;
	}
	private static int getTotalUtxoSum(Transaction t) {
		int sum = 0;
		for (UTXO utxo : t.utxos) {
			sum += utxo.value;
		}
		return sum;
	}


	private static void throwError(String message, boolean verboseMode) throws Exception {
		if (verboseMode)
			System.err.println(message);
		throw new Exception();
	}

	public static boolean checkSha1(String txid, String Sha1) {
		return txid.equals(Sha1);
	}

	public static String getSha1(String password) {
		String sha1 = "";
		password = password + "\n";
		try {
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(password.getBytes("UTF-8"));
			sha1 = byteToHex(crypt.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sha1.substring(0, 8);
	}

	public static String byteToHex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}
}
