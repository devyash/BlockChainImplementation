import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

public class InteractiveModeClass {

	public void showScreen(BlockChainClass bc, boolean interactiveMode, boolean verboseMode, HashMap<String, String> accountKeys) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String fileName = "";
		boolean displayOnce = true;
		while (true) {
			if (displayOnce && interactiveMode) {
				System.out.println("[F]ile\n" + "[T]ransaction\n" + "[P]rint\n" + "[H]elp\n" + "[D]ump\n" + "[W]ipe\n"
						+ "[I]nteractive\n" + "[V]erbose\n" + "[B]alance\n" + "[R]ead key file \n[S]ign Transaction"+"\n[E]xit");
				displayOnce = false;
			}
			if (interactiveMode)
				System.out.println("Select a command:");
			String input = null;
			input = br.readLine();
			String accountName = null;
			switch (input.toUpperCase()) {
			case "F":
				if (interactiveMode) {
					System.out.println("Supply filename:");
				}
				fileName = br.readLine();
				readFile(fileName, verboseMode, bc);

				break;
			case "T":
				if (interactiveMode) {
					System.out.println("Enter Transaction: ");
				}
				String line = br.readLine();
				TransactionClass transaction = TransactionClass.parseTransactionClass(line, verboseMode, bc);
				TransactionClass.executeTransactionClass(transaction, bc, true);
				break;
			case "P":
				System.out.println(TransactionClass.printAllTransactionClass(bc, verboseMode));
				break;
			case "H":
				System.out.println(
						"[F]ile:  Supply filename:<infilename>.  Read in a file of TransactionClasss. Any invalid TransactionClass shall be identified with an error message to stderr, but not stored. Print an error message to stderr if the input file named cannot be opened. The message shall be “Error: file <infilename> cannot be opened for reading” on a single line, where <infilename> is the name provided as additional command input.  \n"
								+ "\n"
								+ "[T]ransaction: Supply Transaction:<see format below>   Read in a single Transaction in the format shown below.  It shall be checked for validity against the ledger, and added if it is valid. If it is not valid, then do not add it to the ledger and print a message to stderr with the Transaction number followed by a colon, a space, and the reason it is invalid on a single line.\n"
								+ "\n" + "[E]xit:  Quit the program\n" + "\n"
								+ "[P]rint:  Print current ledger (all Transactions in the order they were added) to stdout in the Transaction format given below, one Transaction per line.\n"
								+ "\n" + "[H]elp:  Command Summary\n" + "\n"
								+ "[D]ump:  Supply filename:<outfilename>.  Dump ledger to the named file. Print an error message to stderr if the output file named cannot be opened. The message shall be “Error: file <outfilename> cannot be opened for writing” on a single line, where <outfilename> is the name provided as additional command input. \n"
								+ "\n" + "[W]ipe:  Wipe the entire ledger to start fresh.\n" + "\n"
								+ "[I]nteractive: Toggle interactive mode. Start in non-interactive mode, where no command prompts are printed. Print command prompts and prompts for additional input in interactive mode, starting immediately (i.e., print a command prompt following the I command).\n"
								+ "\n"
								+ "[V]erbose: Toggle verbose mode. Start in non-verbose mode. In verbose mode, print additional diagnostic information as you wish. At all times, output each Transaction number as it is read in, followed by a colon, a space, and the result (“good” or “bad”). \n"
								+ "\n"
								+ "[B]alance:  Supply username:  (e.g. Alice).  This command prints the current balance of a user.    \n"
								+ "\n" + "Format of Transactions:\n"
								+ "<TransID>; M; (<TransID>, <vout>)^M; N; (<AcctID>, <amount>)^N \n"
								+ "Items in angle brackets are parameters, M and N are whole numbers, and caret M (or N) indicates M (or N) repetitions of the parenthesized pairs. \n"
								+ "\n" + "");
				break;
			case "D":
				fileName = br.readLine().trim();
				writeToFile(TransactionClass.printAllTransactionClass(bc, verboseMode), fileName);
				if (verboseMode)
					System.out.println("Finished!");
				break;
			case "W":
				bc.whipeBlockChain();
				if (verboseMode)
					System.out.println("Wiped!");
				break;
			case "I":
				interactiveMode = !interactiveMode;
				displayOnce = true;

				break;
			case "V":
				verboseMode = !verboseMode;
				break;
			case "B":
				System.out.println("Supply username:");
				String username = br.readLine().trim();
				System.out.println(username + " has " + bc.wallet.get(username));
				break;
			case "E":
				System.out.println("Good-bye");
				System.exit(0);
				break;
			case "R":
				if (interactiveMode) {
					System.out.println("Supply <account name> <keyfilename>");
				}
				String line1 = br.readLine();
				try {
					String[] temp = line1.split(" ");
					accountName = temp[0];
					fileName = temp[1];
					readKeyFile(fileName, (String)accountName,accountKeys);
					System.out.println("Key: Private key "+accountName+" "+fileName+" read");
				}
				catch(Exception e) {
					System.out.println("Error: Private key "+accountName+" "+fileName+" not read");	
					e.printStackTrace();
				}

				break;
			case "S":
				if (interactiveMode) {
					System.out.println("Supply <transactionID>:");
				}
				String txid = br.readLine();
				try {
					if(bc.block.containsKey(txid)) {
						if(verifyAccountNames(txid,bc)){
							// for geneis transaction account name = utxo.account
							TransactionClass t = bc.block.get(txid);
							accountName = getAccountName(t.toString(), bc, verboseMode);
							System.out.println(t.toString());
							System.out.println(signTransactionClass(t.toString(), accountKeys,accountName));
						}else {
							System.out.println("Multiple Input Accounts");
							System.out.println("Error: Transaction not signed");
						}
					}else {
						System.out.println("Transaction id does not exist!");
						System.out.println("Error: Transaction not signed");
					}
				}
				catch(Exception e) {
					System.out.println("Error: Transaction not signed");
					e.printStackTrace();
				}
				
				break;
			default:
				if(verboseMode)
					System.err.println("Wrong output selected! Please Type H to see the commands available");
				break;
			}
		}

	}

	private static String signTransactionClass(String TransactionClass, HashMap<String, String> accountKeys, String accountName) throws Exception {
		//Errors include invalid TransactionClass ID (not in ledger), invalid TransactionClass (TransactionClass has multiple accounts that own inputs), and no private key found (program has not been given the private key file corresponding to the account that owns the inputs).
		//Verification to be done in ledger
		String transactionToSign = getTransactionClassToSign(TransactionClass);
		SHA256RSA obj = new SHA256RSA();
		String signature = "";
		if(accountKeys.containsKey(accountName.trim()))
			signature = obj.sign1(transactionToSign,accountKeys.get(accountName));
		else
			System.out.println(accountName + " private key does not exist!");
		return signature;
	}
	private static String formatTransactionClass(String TransactionClass) {
		//TODO: What to do if it is first TransactionClass or if it is other format> How do we know?
		return TransactionClass;
	}
	private static String getTransactionClassToSign(String TransactionClass) {
		//4787df35; 1; (f2cea539, 0); 3; (Bob, 150)(Alice, 845)(Gopesh, 5)
		return TransactionClass.substring(10);
	}

	private static void readKeyFile(String fileName, String accountName, HashMap<String, String> accountKeys) throws IOException {
		//Problems include invalid account name (syntactic error), and various errors opening and reading the file that is supposed to contain the key.
		// /Users/devyash/eclipse-workspace/SimplifiedBitcoin/TransactionClasss.txt
				BufferedReader br = null;
				br = new BufferedReader(new FileReader(fileName));
				String line = "";
				StringBuilder sb = new StringBuilder();
				while ((line = br.readLine())!= null) {
						line = line.replace("//s+", "");
						sb.append(line);
					}
				accountKeys.put(accountName,sb.toString());
				br.close();
		
	}



	private void writeToFile(String TransactionClasss, String fileName) {
		File f = new File(fileName);
		if (!f.exists() || !f.canWrite())
			System.err.println("Error: file " + fileName + " cannot be opened for writing”");
		try {
			FileWriter fw = new FileWriter(f);
			fw.write(TransactionClasss);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			System.err.println("Error: file " + fileName + " cannot be opened for writing”");
		}

	}

	private void readFile(String fileName, boolean verboseMode, BlockChainClass bc) throws IOException {
		// /Users/devyash/eclipse-workspace/SimplifiedBitcoin/TransactionClasss.txt
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.length() < 8)
					break;
				TransactionClass transaction = TransactionClass.parseTransactionClass(line, verboseMode, bc);
				if (verboseMode)
					System.out.println(line);
				if (TransactionClass.executeTransactionClass(transaction, bc, verboseMode)) {
					System.out.println(transaction.txid + ": good");
				} else {
					System.out.println(line.substring(0, 8) + ": bad");
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error: file '" + fileName + "' cannot be opened for reading.");
		}
	}
	//
	String getAccountNameFromTXID(String txid, BlockChainClass bc) throws Exception {
		if(!bc.block.containsKey(txid)) {
			throw new Exception("Error: Transaction ID does not exist.");
		}
		//get account Name from first old TransactionClass
		TransactionClass currT = bc.block.get(txid);
		OldTransaction oldT = currT.oldts.get(0);
		TransactionClass prevT = bc.block.get(oldT.txid); //can take any one prevT
		int pos = oldT.pos;
		UTXO prevUtxo = prevT.utxos.get(pos);
		String accountName = prevUtxo.account;
		return accountName;
	}
	
	private String getAccountName(String TransactionClass1, BlockChainClass bc, boolean verboseMode) {
		if(TransactionClass1.charAt(10)=='0')
			return "Alice";
		TransactionClass transaction = TransactionClass.parseTransactionClass(TransactionClass1, verboseMode, bc);
		List<OldTransaction> oldt = transaction.oldts;
		for(int i = 0;i<transaction.M;i++) {
			OldTransaction ot = oldt.get(i);
			TransactionClass prevT = bc.block.get(ot.txid);
			UTXO utxo = prevT.utxos.get(ot.pos);
			return utxo.account;
			
		}
		return "Alice";
	}
	//
	boolean verifyAccountNames(String txid, BlockChainClass bc) throws Exception {
		if(!bc.block.containsKey(txid)) {
			throw new Exception("Error: Transaction ID does not exist.");
		}
		//get account Name from first old TransactionClass
		TransactionClass currT = bc.block.get(txid);
		TransactionClass prevT;
		OldTransaction oldT;
		int pos;
		UTXO prevUtxo;
		String accountName="",prevAccountName="";
		for(int i = 0;i<currT.M;i++) {
			 oldT = currT.oldts.get(i);
			 prevT = bc.block.get(oldT.txid); //can take any one prevT
			 pos = oldT.pos;
			 prevUtxo = prevT.utxos.get(pos);
			 accountName = prevUtxo.account;
			if(i!=0 && prevAccountName!=accountName)
					return false;
			 prevAccountName = accountName;
			 
		}
		return true;
	}

}


// /Users/devyash/eclipse-workspace/SimplifiedBitcoin/TransactionClasss.txt
// Alice /Users/devyash/eclipse-workspace/Wallet/aliceprivate_key.pem
// 4787df35; 1; (f2cea539, 0); 3; (Bob, 150)(Alice, 845)(Gopesh, 5)