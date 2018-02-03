import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InteractiveMode {

	public void showScreen(BlockChain bc, boolean interactiveMode, boolean verboseMode) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String fileName = "";
		boolean printError = true;
		while(true) {
			if(interactiveMode) {
				System.out.println("[F]ile\n" + 
						"[T]ransaction\n" + 
						"[P]rint\n" + 
						"[H]elp\n" + 
						"[D]ump\n" + 
						"[W]ipe\n" + 
						"[I]nteractive\n" + 
						"[V]erbose\n" + 
						"[B]alance\n" + 
						"[E]xit");
				System.out.println("Select a command:");
			}
			String input = null;
			input = br.readLine();
			switch(input) {
				case "F":
					if(interactiveMode) {
						System.out.println("Supply filename:");
					}
					fileName = br.readLine();
					readFile(fileName, printError, bc);
					
					break;
				case "T":
					if(interactiveMode) {
						System.out.println("Enter Transaction: ");
					}
					String transaction = br.readLine();
					// call the transaction method here
					Transaction.parseTransaction(transaction,verboseMode, bc);
					break;
				case "P":
					System.out.println(Transaction.printAllTransaction(bc,verboseMode));
					break;
				case "H":
					System.out.println("[F]ile:  Supply filename:<infilename>.  Read in a file of transactions. Any invalid transaction shall be identified with an error message to stderr, but not stored. Print an error message to stderr if the input file named cannot be opened. The message shall be “Error: file <infilename> cannot be opened for reading” on a single line, where <infilename> is the name provided as additional command input.  \n" + 
					"\n" + 
					"[T]ransaction: Supply Transaction:<see format below>   Read in a single transaction in the format shown below.  It shall be checked for validity against the ledger, and added if it is valid. If it is not valid, then do not add it to the ledger and print a message to stderr with the transaction number followed by a colon, a space, and the reason it is invalid on a single line.\n" + 
					"\n" + 
					"[E]xit:  Quit the program\n" + 
					"\n" + 
					"[P]rint:  Print current ledger (all transactions in the order they were added) to stdout in the transaction format given below, one transaction per line.\n" + 
					"\n" + 
					"[H]elp:  Command Summary\n" + 
					"\n" + 
					"[D]ump:  Supply filename:<outfilename>.  Dump ledger to the named file. Print an error message to stderr if the output file named cannot be opened. The message shall be “Error: file <outfilename> cannot be opened for writing” on a single line, where <outfilename> is the name provided as additional command input. \n" + 
					"\n" + 
					"[W]ipe:  Wipe the entire ledger to start fresh.\n" + 
					"\n" + 
					"[I]nteractive: Toggle interactive mode. Start in non-interactive mode, where no command prompts are printed. Print command prompts and prompts for additional input in interactive mode, starting immediately (i.e., print a command prompt following the I command).\n" + 
					"\n" + 
					"[V]erbose: Toggle verbose mode. Start in non-verbose mode. In verbose mode, print additional diagnostic information as you wish. At all times, output each transaction number as it is read in, followed by a colon, a space, and the result (“good” or “bad”). \n" + 
					"\n" + 
					"[B]alance:  Supply username:  (e.g. Alice).  This command prints the current balance of a user.    \n" + 
					"\n" + 
					"Format of Transactions:\n" + 
					"<TransID>; M; (<TransID>, <vout>)^M; N; (<AcctID>, <amount>)^N \n" + 
					"Items in angle brackets are parameters, M and N are whole numbers, and caret M (or N) indicates M (or N) repetitions of the parenthesized pairs. \n" + 
					"\n" + 
					"");
					break;
				case "D":
					fileName = br.readLine();	
					Transaction.writeToFile(Transaction.printAllTransaction(bc,verboseMode),fileName);
					break;
				case "W":
					bc.whipeBlockChain();
					break;
				case "I":
					interactiveMode = !interactiveMode;
					break;
				case "V":
					verboseMode = !verboseMode;
					break;
				case "B":
					System.out.println("Supply username:");
					String username=br.readLine();
					break;
				case "E":
					System.out.println("Good-bye");
					System.exit(0);
					break;
				default:
					System.out.println("Wrong output selected! Please Type H to see the commands available");
					break;
				}
		}

	}

	private void readFile(String fileName, boolean printError, BlockChain bc) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
			String line;
			while((line = br.readLine())!=null) {
				Transaction.parseTransaction(line, printError, bc);
			}
		} catch (FileNotFoundException e) {
			System.out.println("Error: file '"+fileName+"' cannot be opened for reading.");
		}
	}
	
}
