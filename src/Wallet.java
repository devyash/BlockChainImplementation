import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Wallet {
	public static void main(String[] args) {
		HashMap <String, String> accountKeys = new HashMap();
		try {
			showScreen(true, false,accountKeys);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void showScreen(boolean interactiveMode, boolean verboseMode, HashMap<String, String> accountKeys) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String fileName = "", accountName = "";
		boolean displayOnce = true;
		while (true) {
			if (displayOnce && interactiveMode) {
				System.out.println("Wallet:\n [R]ead key file \n [S]ign transaction \n [I]nterativeMode \n [E]xit");
				displayOnce = false;
			}
			if (interactiveMode)
				System.out.println("Select a command:");
			String input = null;
			input = br.readLine();
			switch (input.toUpperCase()) {
			case "R":
				if (interactiveMode) {
					System.out.println("Supply <account name> <keyfilename>");
				}
				String line = br.readLine();
				try {
					String[] temp = line.split(" ");
					accountName = temp[0];
					fileName = temp[1];
					readKeyFile(fileName, accountName,accountKeys);
					System.out.println("Key: Private key "+accountName+" "+fileName+" read");
				}
				catch(Exception e) {
					System.out.println("Error: Private key "+accountName+" "+fileName+" not read");	
					e.printStackTrace();
				}

				break;
			case "S":
				if (interactiveMode) {
					System.out.println("Enter Account Name: ");
				}
				accountName = br.readLine();
				if (interactiveMode) {
					System.out.println("Enter Transaction: ");
				}
				String transaction = br.readLine();
				try {
					System.out.println(transaction);
					System.out.println(signTransaction(transaction, accountKeys,accountName));
				}
				catch(Exception e) {
					System.out.println("Error: transaction not signed");
				}
				//Transaction transaction = Transaction.parseTransaction(line, verboseMode, bc, signature);
				//Transaction.executeTransaction(transaction, bc, true);
				break;
			case "E":
				System.out.println("Good-bye");
				System.exit(0);
				break;
			case "I":
				interactiveMode = !interactiveMode;
				displayOnce = true;
				break;
			default:
				if(verboseMode)
					System.err.println("Wrong output selected! Please Type H to see the commands available");
				break;
			}
		}
	}
	private static String signTransaction(String transaction, HashMap<String, String> accountKeys, String accountName) throws Exception {
		//Errors include invalid transaction ID (not in ledger), invalid transaction (transaction has multiple accounts that own inputs), and no private key found (program has not been given the private key file corresponding to the account that owns the inputs).
		//Verification to be done in ledger
		transaction = formatTransaction(transaction);
		String transactionToSign = getTransactionToSign(transaction);
		SHA256RSA obj = new SHA256RSA();
		String signature = "";
		if(!accountKeys.containsKey(accountName))
			signature = obj.signSHA256RSA(transactionToSign,accountKeys.get(accountName));
		else
			System.out.println(accountName + "private key does not exist!");
		return signature;
	}
	private static String formatTransaction(String transaction) {
		//TODO: What to do if it is first Transaction or if it is other format> How do we know?
		return transaction;
	}
	private static String getTransactionToSign(String transaction) {
		//4787df35; 1; (f2cea539, 0); 3; (Bob, 150)(Alice, 845)(Gopesh, 5)
		return transaction.substring(10);
	}
	private static String getAccountName(String transaction) {
		//4787df35; 1; (f2cea539, 0); 3; (Bob, 150)(Alice, 845)(Gopesh, 5)
		return transaction.substring(0,8);
	}
	private static void readKeyFile(String fileName, String accountName, HashMap<String, String> accountKeys) throws IOException {
		//Problems include invalid account name (syntactic error), and various errors opening and reading the file that is supposed to contain the key.
		// /Users/devyash/eclipse-workspace/SimplifiedBitcoin/transactions.txt
				BufferedReader br = null;
				br = new BufferedReader(new FileReader(fileName));
				String line = "";
				while ((line = br.readLine())!= null) {
						line = line.replace("//s+", "");
						accountKeys.put(accountName,line);
					}
				
				br.close();
		
	}
}

