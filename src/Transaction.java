import java.util.LinkedList;
import java.util.List;

public class Transaction {
	
	String TXID;
	String oldTXID;
	int M;
	int N;
	List <UTXO> transactions;

	public static String parseTransaction(String line, boolean printError, BlockChain bc) {
		//f2cea539; 0; ; 1; (Alice, 1000)
		//4787df35; 1; (f2cea539, 0); 3; (Bob, 150)(Alice, 845)(Gopesh, 5)
		try {
			String [] input = line.split(";");
			String TEXID=input[0].trim();
			int M = Integer.parseInt(input[1].trim());
			int N = Integer.parseInt(input[3].trim());
			List <UTXO> transactions = new LinkedList<UTXO>();
			String outputTransactions = input[4].trim();
			
			String [] outputList = outputTransactions.split("\\)");
			
			//There should be N outputs	
			if(outputList.length!=N)
				throw new Exception();
			
			for(int i=0;i<N;i++) {
				String oneUTXO = outputList[i].substring(1); //remove '('
				UTXO utxo = new UTXO(oneUTXO.split(",")[0],Integer.parseInt(oneUTXO.split(",")[1].trim()));
				transactions.add(utxo);
			}
			
			if(bc.firstTransaction) {
				if(M!=0)
					throw new Exception();
				bc.firstTransaction= !bc.firstTransaction;
			}
			else {
				if(M!=1)
					throw new Exception();
			}
			System.out.println("TXID: "+TEXID+"\n  M" +M+"\n N"+N+"\n TRANSACTIONS:"+transactions);
		}
		catch(Exception e) {
			System.out.println(e);
			System.out.println("Error: Invalid Format of Transaction - "+ line);
		}
				
		return "";
	}

	
	public  static String printAllTransaction(BlockChain bc,boolean verboseMode){
		return "";	
	}
	
	public static boolean writeToFile(String output, String filename) {
		return true;
	}
	public static String verifyTransaction(String transaction, BlockChain bc) {
		return transaction;
		// TODO Auto-generated method stub
		
	}


}

