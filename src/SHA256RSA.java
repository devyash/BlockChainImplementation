import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class SHA256RSA {

	public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, IOException, SignatureException {
		// TODO Auto-generated method stub
		String data = "0; ; 1; (Alice, 5000)";
		Signature rsa = Signature.getInstance("SHA256withRSA");
		rsa.initSign(getPrivate("aliceprivate_key.pem"));
		rsa.update(data.getBytes());
		byte[] signature = rsa.sign();
		String base64Signature = Base64.getEncoder().encodeToString(signature);
		String pubFile = "alicepublic_key.pem";
		verify(pubFile, base64Signature, data);
	}
	
	public static String sign(String transaction, String privateKey) throws Exception {
		String privateKeyContent = privateKey;
		privateKeyContent = privateKeyContent.replaceAll("\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\r", "");
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);
        Signature rsa = Signature.getInstance("SHA256withRSA");
        rsa.initSign(getPrivate("aliceprivate_key.pem"));
		rsa.update(transaction.getBytes());
		byte[] signature = rsa.sign();
		return Base64.getEncoder().encodeToString(signature);
	}
	
	public static PrivateKey getPrivate(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
	{
		String privateKeyContent = new String(Files.readAllBytes(Paths.get(filename)));
		privateKeyContent = privateKeyContent.replaceAll("\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\r", "");
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);
        return privKey;
	}
	
	public static void verify(String pubKey, String signature, String data) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, IOException, SignatureException
	{
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initVerify(getPublic(pubKey));
		sig.update(data.getBytes());
		if(sig.verify(Base64.getDecoder().decode(signature)))
		{
			System.out.print("Ok\n");

		}
		else
			System.out.print("Bad");

	}
	
	public static boolean verifyFromTransaction(String pubKey, String signature, String data) throws Exception
	{
		if(pubKey!=null && pubKey.length()>3 && signature!=null && signature.length()>2) {
			Signature sig = Signature.getInstance("SHA256withRSA");
			sig.initVerify(getPublic(pubKey));
			sig.update(data.getBytes());
			try {
				if(sig.verify(Base64.getDecoder().decode(signature)))
				{
					return true;

				}
			}catch(Exception e) {
				return false;
			}
		}
		return false;
	}

	public static boolean verifyFromTransactionObject(BlockChain bc, Transaction t) throws Exception{
		String accountName = InteractiveMode.getAccountNameFromTXID(t.txid, bc);
		if(bc.wallet.containsKey(accountName) && SHA256RSA.verifyFromTransaction(bc.wallet.get(accountName).publicKey,t.signature,t.toString().substring(10))) {
			return true;
		}	
		return false;
	}
	public static PublicKey getPublicfromFile(String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
	{
		String publicKeyContent = new String(Files.readAllBytes(Paths.get(filename)));
		return getPublic(publicKeyContent);
	}
	
	public static PublicKey getPublic(String publicKeyContent) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException
	{
		publicKeyContent = publicKeyContent.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");;
		KeyFactory kf = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
        RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(keySpecX509);
        return pubKey;
	}
	
	public static String sign1(String transaction, String privateKey) throws Exception {
		 // Remove markers and new line characters in private key
       String realPK = privateKey.replaceAll("-----END PRIVATE KEY-----", "")
                            .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                            .replaceAll("\n", "");

       byte[] b1 = Base64.getDecoder().decode(realPK);
       PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(b1);
       KeyFactory kf = KeyFactory.getInstance("RSA");

       Signature privateSignature = Signature.getInstance("SHA256withRSA");
       privateSignature.initSign(kf.generatePrivate(spec));
       privateSignature.update(transaction.getBytes("UTF-8"));
       byte[] s = privateSignature.sign();
       return Base64.getEncoder().encodeToString(s);
	}
	

}