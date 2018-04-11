import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InteractiveMode {

	public void showScreen(BlockChain bc, boolean interactiveMode, boolean verboseMode) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String fileName = "";
		boolean displayOnce = true;
		while (true) {
			if (displayOnce && interactiveMode) {
				System.out.println("[F]ile\n" + "[T]ransaction\n" + "[P]rint\n" + "[H]elp\n" + "[D]ump\n" + "[W]ipe\n"
						+ "[I]nteractive\n" + "[V]erbose\n" + "[B]alance\n" + "[E]xit\n" +"[O]utput transaction block\n[C]heck transaction signature:\n[R]ead Key File");
				displayOnce = false;
			}
			if (interactiveMode)
				System.out.println("Select a command:");
			String input = null;
			input = br.readLine();
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
				String signature = br.readLine();
				Transaction transaction = Transaction.parseTransaction(line, verboseMode, bc, signature);
				Transaction.executeTransaction(transaction, bc, true);
				break;
			case "P":
				System.out.println(Transaction.printAllTransactionNotCommited(bc, verboseMode));
				break;
			case "H":
				System.out.println(
						"[F]ile:  Supply filename:<infilename>.  Read in a file of transactions. Any invalid transaction shall be identified with an error message to stderr, but not stored. Print an error message to stderr if the input file named cannot be opened. The message shall be “Error: file <infilename> cannot be opened for reading” on a single line, where <infilename> is the name provided as additional command input.  \n"
								+ "\n"
								+ "[T]ransaction: Supply Transaction:<see format below>   Read in a single transaction in the format shown below.  It shall be checked for validity against the ledger, and added if it is valid. If it is not valid, then do not add it to the ledger and print a message to stderr with the transaction number followed by a colon, a space, and the reason it is invalid on a single line.\n"
								+ "\n" + "[E]xit:  Quit the program\n" + "\n"
								+ "[P]rint:  Print current ledger (all transactions in the order they were added) to stdout in the transaction format given below, one transaction per line.\n"
								+ "\n" + "[H]elp:  Command Summary\n" + "\n"
								+ "[D]ump:  Supply filename:<outfilename>.  Dump ledger to the named file. Print an error message to stderr if the output file named cannot be opened. The message shall be “Error: file <outfilename> cannot be opened for writing” on a single line, where <outfilename> is the name provided as additional command input. \n"
								+ "\n" + "[W]ipe:  Wipe the entire ledger to start fresh.\n" + "\n"
								+ "[I]nteractive: Toggle interactive mode. Start in non-interactive mode, where no command prompts are printed. Print command prompts and prompts for additional input in interactive mode, starting immediately (i.e., print a command prompt following the I command).\n"
								+ "\n"
								+ "[V]erbose: Toggle verbose mode. Start in non-verbose mode. In verbose mode, print additional diagnostic information as you wish. At all times, output each transaction number as it is read in, followed by a colon, a space, and the result (“good” or “bad”). \n"
								+ "\n"
								+ "[B]alance:  Supply username:  (e.g. Alice).  This command prints the current balance of a user.    \n"
								+ "\n" + "Format of Transactions:\n"
								+ "[O]utput transaction block: collect all correctly signed transactions \n "
								+ "<TransID>; M; (<TransID>, <vout>)^M; N; (<AcctID>, <amount>)^N \n"
								+ "Items in angle brackets are parameters, M and N are whole numbers, and caret M (or N) indicates M (or N) repetitions of the parenthesized pairs. \n"
								+ "\n" + "");
				break;
			case "D":
				fileName = br.readLine().trim();
				writeToFile(Transaction.printAllTransaction(bc, verboseMode), fileName);
				if (verboseMode)
					System.out.println("Finished!");
				break;
			case "W":
				bc.wipeBlockChain();
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
				if(bc.wallet.containsKey(username))
					System.out.println(username + " has " + bc.wallet.get(username).value);
				else
					System.out.println(username + " has no account");
				break;
			case "E":
				System.out.println("Good-bye");
				System.exit(0);
				break;
			case "O":
				System.out.println(Transaction.printAllCurrentTransactionWithSignature(bc, verboseMode));
				try {
					addCurrentBlockToBlockChain(bc);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			case "C":
				//TODO
				System.out.println("Supply <transactionID>");
				String transactionId = br.readLine();
				try {
					if(bc.block.containsKey(transactionId)) {
						if(verifyAccountNames(transactionId,bc)) {
						printGoodOrBadTransaction(transactionId, bc);
						}
						else {
							Transaction t = bc.block.get(transactionId);
							System.out.println("Bad");
							System.out.println("Multiple Input Accounts!");
						}
					}
					else {
						System.out.println("Bad");
						System.out.println("Transaction ID does not exsist!");
					}
				}		
				catch(Exception e) {
					System.out.println(e);
				}
				break;
			case "R":
				//TODO
				System.out.println("Supply <account name> <keyfilename>:");
				String accountName = "",keyFileName="";
				try {
					line = br.readLine();
					String [] temp = line.split(" ");
					accountName = temp[0];
					keyFileName = temp [1];
					readKeyFile(keyFileName, accountName,bc);
					System.out.println("Key: Public key "+accountName+" "+fileName+" read");
				}
				catch(Exception e) {
					System.out.println("Error: Public key "+accountName+" "+fileName+" not read");	
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

	private void readKeyFile(String fileName, String accountName, BlockChain bc) throws Exception {
		String pubKey =  Files.lines(Paths.get(fileName)).collect(Collectors.joining("\n"));
		if(!bc.wallet.containsKey(accountName)) 
			bc.wallet.put(accountName, new WalletClass(accountName,0));
		bc.wallet.get(accountName).publicKey = pubKey;
	}
			
	private void printGoodOrBadTransaction(String transactionId, BlockChain bc) throws Exception {
		if(!bc.block.containsKey(transactionId)) {
			throw new Exception("Error: Transaction ID does not exist.");
		}
		try {
		String accountName="";
		//get account Name from first old transaction
		Transaction currT = bc.block.get(transactionId);
		if(currT.oldts.size()>0) {
			OldTransaction oldT = currT.oldts.get(0);
			Transaction prevT = bc.block.get(oldT.txid); //can take any one prevT
			int pos = oldT.pos;
			UTXO prevUtxo = prevT.utxos.get(pos);
			accountName = prevUtxo.account;
		}
		else {
			accountName ="Alice"; //hardcoding genisis transaction as Alice
		}

		String transaction = bc.block.get(transactionId).toString();
		String signature = bc.block.get(transactionId).signature;
		String pubKy = "";
		if(bc.wallet.get(accountName)!=null)
			 pubKy = bc.wallet.get(accountName).publicKey;
		SHA256RSA obj = new SHA256RSA();
		if(pubKy==null||signature==null||transaction==null||pubKy.length()<8||signature.length()<8||transaction.length()<8)
			throw new Exception("Public Key Not Avaible for User!");
		obj.verify(pubKy, signature, transaction.substring(10));
		}
		catch(Exception e) {
			System.out.println("Bad");
			e.printStackTrace();
		}
		
	}

	private void addCurrentBlockToBlockChain(BlockChain bc) throws Exception {
		if(!bc.currentBlock.isEmpty()) {
		Iterator itr = bc.currentBlock.entrySet().iterator();
		Block newBlock = new Block();
		List<String> txidsToRemove = new LinkedList<String>();
		SHA256RSA obj = new SHA256RSA();
		while (itr.hasNext()) {
			Map.Entry curr = (Map.Entry) itr.next();
			Transaction ct = (Transaction) curr.getValue();
			String accountName = getAccountNameFromTXID(ct.txid,bc);
			if(bc.wallet.containsKey(accountName) && SHA256RSA.verifyFromTransaction(bc.wallet.get(accountName).publicKey,ct.signature,ct.toString().substring(10))) {
				Transaction.updateSignatureValidFlag(ct,bc);
				if(ct.signature!=null && ct.signature.length()>2 &&ct.validSignature) {
					newBlock.transactionIds.add(ct.txid);
					txidsToRemove.add(ct.txid);
					Transaction.updateWallet(ct,bc);
				}
			}
		}
		while(!txidsToRemove.isEmpty())
			bc.currentBlock.remove(txidsToRemove.remove(0));
		bc.blockChain.add(newBlock);
		}
	}

	private void writeToFile(String Transactions, String fileName) {
		File f = new File(fileName);
		if (!f.exists() || !f.canWrite())
			System.err.println("Error: file " + fileName + " cannot be opened for writing”");
		try {
			FileWriter fw = new FileWriter(f);
			fw.write(Transactions);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			System.err.println("Error: file " + fileName + " cannot be opened for writing”");
		}

	}

	private void readFile(String fileName, boolean verboseMode, BlockChain bc) throws IOException {
		// /Users/devyash/eclipse-workspace/SimplifiedBitcoin/transactions.txt
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
			String line,signature,nextLine;
			boolean isTransaction = true;
			line = br.readLine();
			nextLine = line;
			while (line != null && nextLine !=null) {
				line = line.replace("//s+", "");
				line = nextLine;
				signature = br.readLine();
				if(isTransaction(signature)) {
					nextLine = signature;
					signature = null;
				}
				else {
					nextLine = br.readLine();
				}
				if (line.length() < 8)
					break;
				Transaction transaction = Transaction.parseTransaction(line, verboseMode, bc, signature);
				if (verboseMode)
					System.out.println(line);
				if (Transaction.executeTransaction(transaction, bc, verboseMode)) {
					System.out.println(transaction.txid + ": good");
				} else {
					System.out.println(line.substring(0, 8) + ": bad");
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("Error: file '" + fileName + "' cannot be opened for reading.");
		}
	}

	private boolean isTransaction(String signature) {
		return signature!=null && signature.length()>8 && (signature.charAt(8)==';');
	}
	
	static String getAccountNameFromTXID(String txid, BlockChain bc) throws Exception {
		if(!bc.block.containsKey(txid)) {
			throw new Exception("Error: Transaction ID does not exist.");
		}
		//get account Name from first old TransactionClass
		Transaction currT = bc.block.get(txid);
		if(currT.oldts.size()>0) {
		OldTransaction oldT = currT.oldts.get(0);
		Transaction prevT = bc.block.get(oldT.txid); //can take any one prevT
		int pos = oldT.pos;
		UTXO prevUtxo = prevT.utxos.get(pos);
		String accountName = prevUtxo.account;
		return accountName;
		}
		else
			return "Alice";
	}
	
	static boolean verifyAccountNames(String txid, BlockChain bc) throws Exception {
		if(!bc.block.containsKey(txid)) {
			throw new Exception("Error: Transaction ID does not exist.");
		}
		//get account Name from first old TransactionClass
		Transaction currT = bc.block.get(txid);
		Transaction prevT;
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



// /Users/devyash/eclipse-workspace/SimplifiedBitcoin/transactions.txt
