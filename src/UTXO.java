
public class UTXO {
	public String account;
	public int value;
	public boolean spent;

	UTXO(String account, int value) {
		this.account = account;
		this.value = value;
		this.spent = false;
	}
	
	@Override
	public String toString() {
		return "("+this.account+","+this.value+")";
	}
}
