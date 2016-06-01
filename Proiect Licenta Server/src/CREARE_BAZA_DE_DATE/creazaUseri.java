package CREARE_BAZA_DE_DATE;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class creazaUseri {
	public static void main(String[] args) {
		String adresa = "localhost";

		int port = 505;
		Socket cs = null;
		DataOutputStream dos = null;
		DataInputStream dis = null;
		//String[] useri={"Marius","Ana","Maria","Oana","Cristina","Andreea","Catalin","Bogdan","Vasile","Mihai","Cosmin","Andrei","Razvan","Victor"};
		for (char i = 'a'; i <= 'z'; i++) {
		//for(int i=0;i<useri.length;i++){
			try {
				cs = new Socket(adresa, port);
				dos = new DataOutputStream(cs.getOutputStream());
				dis = new DataInputStream(cs.getInputStream());
				dos.writeUTF("signUp");
				dos.writeUTF(i+ "");
				dos.writeUTF(i+ "");
			//	dos.writeUTF(useri[i]);
				//dos.writeUTF("a");
				String rezultat = dis.readUTF();
				//System.out.println("Userul "+useri[i] + " " + rezultat);
				System.out.println("Userul "+i + " " + rezultat);
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
