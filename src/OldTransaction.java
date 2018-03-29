public class OldTransaction {
	public String txid;
	public int pos;

	OldTransaction(String txid, int pos) {
		this.txid = txid;
		this.pos = pos;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "("+this.txid+","+this.pos+")";
	}
}
