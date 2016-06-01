package CREARE_BAZA_DE_DATE;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class CreazaPrieteniTemporari {
	public static String creazaPrieteniFunctie(){
		String adresa = "localhost";

		int port = 505;
		Socket cs=null;
		DataOutputStream dos=null;
		DataInputStream dis=null;
		try {
			cs = new Socket(adresa, port);
			dos = new DataOutputStream(cs.getOutputStream());
			dis = new DataInputStream(cs.getInputStream());
			
			dos.writeUTF("createPrieteniTemporari");
			
			cs.close();
			dis.close();
			dos.close();
			return "succes";
		} catch (Exception e) {
			System.out.println(e);
			try{
			cs.close();
			dis.close();
			dos.close();
			}catch(Exception e2){
				System.out.println(e2);
			}
			return "esec "+e;
		}
	}
	public static void creazaPrieteniProcedura(){
		String adresa = "localhost";

		int port = 505;
		Socket cs=null;
		DataOutputStream dos=null;
		DataInputStream dis=null;
		try {
			cs = new Socket(adresa, port);
			dos = new DataOutputStream(cs.getOutputStream());
			dis = new DataInputStream(cs.getInputStream());
			
			dos.writeUTF("createPrieteniTemporari");
			
			cs.close();
			dis.close();
			dos.close();
			
		} catch (Exception e) {
			System.out.println(e);
			try{
			cs.close();
			dis.close();
			dos.close();
			}catch(Exception e2){
				System.out.println(e2);
			}
		
		}
	}
	public static void main(String[] args) {
		creazaPrieteniProcedura();
	}
}
