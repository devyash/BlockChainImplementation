import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TransactionClass {

	String txid;
//	String oldTxid;
//	int oldTxidUtxo;
	List<OldTransaction> oldts;
	int M;
	int N;
	List<UTXO> utxos;

	TransactionClass(String txid, List<OldTransaction> oldts, int M, int N, List<UTXO> utxos) {
		this.txid = txid;
		this.oldts = oldts;
		this.M = M;
		this.N = N;
		this.utxos = utxos;

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

	public static TransactionClass parseTransactionClass(String TransactionClass, boolean verboseMode, BlockChainClass bc) {
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
			if(!TransactionClass.endsWith(")"))
				throw new Exception();
			input = TransactionClass.split(";");
			txid = input[0].trim();
			M = Integer.parseInt(input[1].trim());
			N = Integer.parseInt(input[3].trim());
			utxos = new LinkedList<UTXO>();
			oldts = new LinkedList<OldTransaction>();
			outpututxos = input[4].trim();
			outputList = outpututxos.split("\\)");

			// There should be N outputs
			if (outputList.length != N) {
				throwError(TransactionClass.substring(0,8)+": Error N != Number of output UTXOs",verboseMode);
			}


			for (int i = 0; i < outputList.length; i++) {
//				if(!TransactionClass.startsWith("("))
//					throw new Exception();
				String oneUTXO = outputList[i].substring(1); // remove '('
				UTXO utxo = new UTXO(oneUTXO.split(",")[0], Integer.parseInt(oneUTXO.split(",")[1].trim()));
				utxos.add(utxo);
			}

			if (bc.firstTransactionClass) {
				if (M != 0) {
					throwError(TransactionClass.substring(0,8)+": " +"Error - M != Number of input UTXOs",verboseMode);
				}
				bc.firstTransactionClass = !bc.firstTransactionClass;
			} else {
				String listOfT=input[2].trim();
				String []ts=listOfT.split("\\)");

				if (ts.length != M)
					throwError(TransactionClass.substring(0,8)+": Invalid number of Input UXTO", verboseMode);

				for (int i = 0; i < ts.length ; i++) {
					String oneOldT = ts[i].substring(1); // remove '('
					OldTransaction oldt = new OldTransaction(oneOldT.split(",")[0],Integer.parseInt(oneOldT.split(",")[1].trim()));
					oldts.add(oldt);
				}
			}

		} catch (Exception e) {
			if(TransactionClass.length()>=8)
				System.err.println(TransactionClass.substring(0,8)+": TransactionClass format Invalid, Exception: "+e);
			else
				System.err.println(TransactionClass+": TransactionClass format Invalid, Exception: "+e);
			return null;
		}

		TransactionClass t = new TransactionClass(txid, oldts, M, N, utxos);
		return t;
	}

	public static String printAllTransactionClass(BlockChainClass bc, boolean verboseMode) {
		StringBuilder sb = new StringBuilder();
		Iterator itr = bc.block.entrySet().iterator();
		boolean firstTransactionClass = true;
		while (itr.hasNext()) {
			Map.Entry curr = (Map.Entry) itr.next();
			TransactionClass ct = (TransactionClass) curr.getValue();
			sb.append(ct.toString()).append("\n");
		}
		return sb.toString();
	}

	public static boolean executeTransactionClass(TransactionClass t, BlockChainClass bc, boolean verboseMode) {
		if (t == null || bc == null)
			return false;
		if (verifyTransactionClass(t, bc, verboseMode)) {
			t = verifyAndChangeTransactionClassId(t,bc,verboseMode);
			bc.block.put(t.txid, t);
			for(OldTransaction oldt : t.oldts) {
				TransactionClass prevT = bc.block.get(oldt.txid);
				UTXO prevUtxo = prevT.utxos.get(oldt.pos);
				prevUtxo.spent = true;
				bc.wallet.put(prevUtxo.account, bc.wallet.get(prevUtxo.account) - prevUtxo.value);
			}
			for (UTXO utxo : t.utxos) {
				if (bc.wallet.containsKey(utxo.account)) {
					bc.wallet.put(utxo.account, bc.wallet.get(utxo.account) + utxo.value);
				} else
					bc.wallet.put(utxo.account, utxo.value);
			}
			return true;
		}
		return false;
	}

	private static boolean verifyTransactionClass(TransactionClass t, BlockChainClass bc, boolean verboseMode) {
		try {
			if (bc.block.containsKey(t.txid)) {
				throwError(t.txid+": " +"Error - TransactionClass Id already exists", verboseMode);
			}
			 int inputSum = 0;
			 for(OldTransaction oldt : t.oldts) {
				 if (bc.block.containsKey(oldt.txid)) {
					 TransactionClass prevTransactionClass = bc.block.get(oldt.txid);
					 inputSum += prevTransactionClass.utxos.get(oldt.pos).value;
					if (prevTransactionClass.N <= oldt.pos ) {
						throwError(t.txid+": " +"Error Old TransactionClass UTXO does not exist! ", verboseMode);
					} else if (prevTransactionClass.utxos.get(oldt.pos).spent) {
						throwError(t.txid+": " +"Error - UTXO already spent", verboseMode);
					}
				}
				 else
					 throwError(t.txid+": " +"Error - Old TransactionClass does not exist!", verboseMode);
			 }
			 if(getTotalUtxoSum(t) != inputSum && t.M != 0) {
				 throwError(t.txid+": " +"Error - Input UTXO != output UTXO", verboseMode);
			 }

		} catch (Exception e) {
			return false;
		}

		return true;
	}

	private static TransactionClass verifyAndChangeTransactionClassId(TransactionClass t, BlockChainClass bc, boolean verboseMode) {
		 if(!checkSha1(t.toString().substring(0,8),getSha1(t.toString().substring(10))))
		 {
			 try {
				throwError(t.txid+": " +"Error - Wrong TransactionClass ID: "+t.txid+". New txid: "+getSha1(t.toString().substring(10)),true);
			} catch (Exception e) {
				t.txid = getSha1(t.toString().substring(10));
			}
		 }
		 return t;
	}
	private static int getTotalUtxoSum(TransactionClass t) {
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
