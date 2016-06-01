package CREARE_BAZA_DE_DATE;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class cereriPrietenieTest {
	public static void main(String[] args) {
		String adresa = "localhost";

		int port = 505;
		Socket cs = null;
		DataOutputStream dos = null;
		DataInputStream dis = null;
		
		for (char i = 'h'; i <= 'z'; i++) {
	
			try {
				cs = new Socket(adresa, port);
				dos = new DataOutputStream(cs.getOutputStream());
				dis = new DataInputStream(cs.getInputStream());
			       dos.writeUTF("addFriend");
			
		
			
				dos.writeUTF(i+"");
				dos.writeUTF("a");
				cs.close();
				dis.close();
				dos.close();
			} catch (Exception e) {
				System.out.println(e);
				try {
					cs.close();
					dis.close();
					dos.close();
				} catch (Exception e2) {
					System.out.println(e2);
				}
			}
		}

	}
}
