package Prezentare;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;



public class Script {

	public static Statement s;
	public static Connection conn;
	static ResultSet rs;

	public static void trimiteMesajePaginaMesaje(String cont,
			String partenerConversatie, String mesaj, String data) {

		try {
			s.executeUpdate("INSERT INTO mesaje(id,username_sender,username_receiver,mesaj,data_mesaj)"
					+ " VALUES(secventa8.NEXTVAL,'"
					+ cont
					+ "','"
					+ partenerConversatie + "','" + mesaj + "'," + data + ")");
			// folosesc pentru prieten temporar sa imi dau seama
			// ca au vorbit cat timp erau prieteni temporari
			// astfel isi primesc punctele .Trebuie ca ambii sa isi
			// fii transmis un mesaj
			// ca sa primeasca punctele. daca a vorbit doar una
			// dintre persoane nu se vor primii punctele
			// astfel am 2 coloana care imi zice pentru fiecare daca
			// a trimis cel putin un mesaj catre prietenul temporar
			// si alta coloana care imi zice daca a primit cel putin
			// 1 mesaj de la prietenul temporar
			// astfel pot sa imi dau seama daca au interschimbat
			// mesaje intre ei
			s.executeUpdate("UPDATE prieteni_temporari SET a_trimis_mesaj='DA' WHERE "
					+ " persoana_username='"
					+ cont
					+ "' AND prieten='"
					+ partenerConversatie + "'");
			s.executeUpdate("UPDATE prieteni_temporari SET a_primit_mesaj='DA' WHERE "
					+ " persoana_username='"
					+ partenerConversatie
					+ "' AND prieten='" + cont + "'");

			// sa imi dau seama ca a trimis mesaje noi

			s.executeUpdate("DELETE FROM mesaje_noi WHERE username_sender='"
					+ cont + "' AND username_receiver='" + partenerConversatie
					+ "'");
			s.executeUpdate("INSERT INTO mesaje_noi(username_sender,username_receiver) VALUES('"
					+ cont + "','" + partenerConversatie + "')");
			s.executeUpdate("COMMIT");
		} catch (Exception e) {
			System.out.println(e);
			try {
				s.executeUpdate("ROLLBACK");
			} catch (Exception e2) {
				System.out.println(e2);
			}
		}

	}

	public static void addPozaProfil(String numePoza, String profil) {
		try {
			File fisier1 = new File(numePoza);
			byte[] buffer = new byte[8192];
			BufferedInputStream buf = new BufferedInputStream(
					new FileInputStream(fisier1));
			ByteArrayOutputStream rez = new ByteArrayOutputStream();
			int lung;
			while ((lung = buf.read(buffer)) != -1) {
				rez.write(buffer);
			}
			byte[] bytes = rez.toByteArray();

			int secventa = -1;
			rs = s.executeQuery("SELECT secventa4.NEXTVAL FROM DUAL");
			while (rs.next()) {
				secventa = rs.getInt(1);
			}

			String temp = "INSERT INTO poze(id,data,width,height) VALUES(?,?,?,?)";
			PreparedStatement ps = conn.prepareStatement(temp);
			ps.setInt(1, secventa);
			ps.setBytes(2, bytes);
			// m-am uitat eu la rezolutia pozei si am pus datele
			ps.setInt(3, 1920);
			ps.setInt(4, 1080);
			ps.executeUpdate();
			s.executeUpdate("UPDATE profiluri SET poza_profil="
					+ secventa
					+ " WHERE id=(SELECT id FROM profiluri WHERE persoana_username="
					+ "'" + profil + "')");

			s.executeUpdate("COMMIT");
			buf.close();
		} catch (Exception e) {
			System.out.println(e + " 2");
			try {
				s.executeUpdate("ROLLBACK");
			} catch (Exception e2) {
				System.out.println(e2);
			}
		}
	}

	public static void addMesajPoza(String numePoza, String autor,
			String profil, String mesaj, String vizibilitate, int width,
			int height, String data) {
		try {
			numePoza = "Prezentare/Poze Post/" + numePoza + ".jpg";
			File fisier1 = new File(numePoza);
			byte[] buffer = new byte[8192];
			BufferedInputStream buf = new BufferedInputStream(
					new FileInputStream(fisier1));
			ByteArrayOutputStream rez = new ByteArrayOutputStream();
			int lung;
			while ((lung = buf.read(buffer)) != -1) {
				rez.write(buffer);
			}
			byte[] bytes = rez.toByteArray();

			int profil_id = -1;
			rs = s.executeQuery("SELECT id FROM profiluri WHERE persoana_username="
					+ "'" + profil + "'");
			while (rs.next()) {
				profil_id = rs.getInt(1);
			}

			int album_id = -1;
			rs = s.executeQuery("SELECT id FROM albume_poze WHERE profil_id="
					+ profil_id + " AND nume_album='default'");

			while (rs.next()) {
				album_id = rs.getInt(1);
			}

			int nrSecventa = -1;
			rs = s.executeQuery("SELECT secventa4.NEXTVAL FROM DUAL");
			while (rs.next()) {
				nrSecventa = rs.getInt(1);
			}
			String temp = "INSERT INTO poze(id,album_id,data,width,height) VALUES(?,?,?,?,?)";
			PreparedStatement ps = conn.prepareStatement(temp);
			ps.setInt(1, nrSecventa);
			ps.setInt(2, album_id);
			ps.setBytes(3, bytes);
			ps.setInt(4, width);
			ps.setInt(5, height);
			ps.executeUpdate();

			s.executeUpdate("INSERT INTO comenturi(id,profil_id,textul,autor,cuPoza,data_postari,vizibilitate) VALUES(secventa2.NEXTVAL,"
					+ profil_id
					+ ",'"
					+ mesaj
					+ "',"
					+ "'"
					+ autor
					+ "',"
					+ nrSecventa
					+ ","
					+ data
					+ ","
					+ "'"
					+ vizibilitate
					+ "'"
					+ ")");
			s.executeUpdate("COMMIT");
		} catch (Exception e) {
			System.out.println(e);
			try {
				s.executeUpdate("ROLLBACK");
			} catch (Exception e2) {
				System.out.println(e2);
			}

		}
	}

	public static void addMesajText(String profil, String autor, String mesaj,
			String data, String vizibilitate) {
		try {
			// adaugarea de posturi pe pagina de profil a lui Marius
			s.executeUpdate("INSERT INTO comenturi(id,profil_id,textul,autor,data_postari,vizibilitate) VALUES("
					+ "secventa2.NEXTVAL"
					+ ","
					+ profil
					+ ","
					+ "'"
					+ mesaj
					+ " ',"
					+ "'"
					+ autor
					+ "',"
					+ data
					+ ","
					+ "'"
					+ vizibilitate + "'" + ")");
			s.executeUpdate("COMMIT");
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void addComentText(int coment_id, String cont, String mesaj,
			String data) {
		try {
			s.executeUpdate("INSERT INTO comenturi(id,coment_id,textul,autor,data_postari) VALUES("
					+ "secventa2.NEXTVAL"
					+ ","
					+ coment_id
					+ ","
					+ "'"
					+ mesaj
					+ "'," + "'" + cont + "'," + data + ")");

			s.executeUpdate("COMMIT");
		} catch (Exception e) {
			System.out.println(e);
			try {
				s.executeUpdate("ROLLBACK");
			} catch (Exception e2) {
				System.out.println(e2);
			}

		}
	}

	public static void addComentPoza(int idComent, String numePoza,
			String autor, String mesaj, String data, int width, int height) {
		try {

			numePoza = "Prezentare/Poze Post/" + numePoza + ".jpg";
			File fisier1 = new File(numePoza);
			byte[] buffer = new byte[8192];
			BufferedInputStream buf = new BufferedInputStream(
					new FileInputStream(fisier1));
			ByteArrayOutputStream rez = new ByteArrayOutputStream();
			int lung;
			while ((lung = buf.read(buffer)) != -1) {
				rez.write(buffer);
			}
			byte[] bytes = rez.toByteArray();
			int nrSecventa = -1;
			rs = s.executeQuery("SELECT secventa4.NEXTVAL FROM DUAL");
			while (rs.next()) {
				nrSecventa = rs.getInt(1);
			}
			String temp = "INSERT INTO poze(id,data,width,height) VALUES(?,?,?,?)";
			PreparedStatement ps = conn.prepareStatement(temp);
			ps.setInt(1, nrSecventa);
			ps.setBytes(2, bytes);
			ps.setInt(3, width);
			ps.setInt(4, height);
			ps.executeUpdate();

			s.executeUpdate("INSERT INTO comenturi(id,coment_id,textul,autor,cuPoza,data_postari) VALUES(secventa2.NEXTVAL,"
					+ idComent
					+ ",'"
					+ mesaj
					+ "',"
					+ "'"
					+ autor
					+ "',"
					+ nrSecventa + "," 
					+ data
							+ ")");

			s.executeUpdate("COMMIT");
		} catch (Exception e) {
			System.out.println(e);
			try{
			s.executeUpdate("ROLLBACK");
			}catch(Exception e2){
				System.out.println(e2);
			}
			
		}

	}

	public static void main(String[] args) {
		try {

			conn = DriverManager.getConnection(
					"jdbc:oracle:thin:@localhost:1521:orcl", "cont",
					"parola");

			s = conn.createStatement();
			String adresa = "localhost";

			int port = 505;
			Socket cs = null;
			DataOutputStream dos = null;
			DataInputStream dis = null;
			String[] useri = { "Marius", "Ana", "Maria", "Oana", "Cristina",
					"Andreea", "Catalin", "Bogdan", "Vasile", "Mihai",
					"Cosmin", "Andrei", "Razvan", "Victor", "Radu", "Madalina" };
			// crearea utilizatorilor
			for (int i = 0; i < useri.length; i++) {
				try {
					cs = new Socket(adresa, port);
					dos = new DataOutputStream(cs.getOutputStream());
					dis = new DataInputStream(cs.getInputStream());
					dos.writeUTF("signUp");

					dos.writeUTF(useri[i]);
					dos.writeUTF("a");
					String rezultat = dis.readUTF();
					System.out.println("Userul " + useri[i] + " " + rezultat);

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

			// le pun utilizatorilor poze de profil
			for (int i = 0; i < useri.length; i++) {
				addPozaProfil("Prezentare/Poze Profil/" + i + ".jpg", useri[i]);
			}

			int nrRecomandari = 5;
			// adaugarea cererilor de prietenie pentru Marius
			for (int i = nrRecomandari; i < useri.length; i++) {

				try {
					s.executeUpdate("INSERT INTO liste_prieteni(username,prieten,acceptat) VALUES("
							+ "'" + useri[0] + "','" + useri[i] + "','NU')");
					s.executeUpdate("INSERT INTO liste_prieteni(username,prieten,acceptat) VALUES("
							+ "'"
							+ useri[i]
							+ "','"
							+ useri[0]
							+ "','PENDING')");
					s.executeUpdate("COMMIT");
				} catch (Exception e) {
					s.executeUpdate("ROLLBACK");
					System.out.println(e);

				}
			}

			// adaugarea prietenilor pentru primii nrRecomandari inafara de
			// primu
			for (int j = 1; j < nrRecomandari; j++)
				for (int i = nrRecomandari; i < useri.length; i++) {
					int nrRandom = (int) ((Math.random() * 1000) % 3);
					if (nrRandom != 2)
						try {
							s.executeUpdate("INSERT INTO liste_prieteni(username,prieten,acceptat) VALUES("
									+ "'"
									+ useri[j]
									+ "','"
									+ useri[i]
									+ "','DA')");
							s.executeUpdate("INSERT INTO liste_prieteni(username,prieten,acceptat) VALUES("
									+ "'"
									+ useri[i]
									+ "','"
									+ useri[j]
									+ "','DA')");
							s.executeUpdate("COMMIT");
						} catch (Exception e) {
							s.executeUpdate("ROLLBACK");
							System.out.println(e);

						}
				}

			// adaugarea de posturi text pe pagina de profil a lui Marius
			// addMesajText(String profil,String autor,String mesaj,String
			// data,String vizibilitate)
			addMesajText("1", "Ana", "salut", "sysdate-10", "toti");
			addMesajText("1", "Ana", "De cand nu te-am mai vazut", "sysdate-9",
					"toti");
			addMesajText("1", "Maria", "Neata", "sysdate-0.001", "toti");
			addMesajText("1", "Oana", "La multi ani", "sysdate-0.8", "toti");
			addMesajText("1", "Mihai", "La multi ani", "sysdate-0.5", "toti");
			addMesajText("1", "Bogdan", "La multi ani", "sysdate-0.7", "toti");
			addMesajText("1", "Vasile", "La multi ani", "sysdate-0.6", "toti");
			// adaugarea de posturi poze si text pe pagina de profil a lui
			// Marius
			// addMesajPoza(String numePoza,String autor,String profil,String
			// mesaj,String vizibilitate,int width,int height,String data)
			addMesajPoza("2", "Marius", "Marius", "Ce ziceti de poza asta?",
					"toti", 1920, 1080, "sysdate-0.2");
			addMesajPoza("0", "Ana", "Marius", "misto", "toti", 1920, 1080,
					"sysdate-500");
			addMesajPoza("1", "Ana", "Marius", "Ce frumos e aici", "toti",
					1920, 1080, "sysdate-1");
			addMesajPoza("3", "Marius", "Marius", " ", "toti", 1920, 1080,
					"sysdate-26");
			addMesajPoza("4", "Marius", "Marius", " ", "toti", 1920, 1080,
					"sysdate-250");
			addMesajPoza("5", "Marius", "Marius", " ", "toti", 1920, 1080,
					"sysdate-2");
			addMesajPoza("6", "Marius", "Marius", " ", "toti", 1920, 1080,
					"sysdate-1");
			addMesajPoza("7", "Marius", "Marius", " ", "toti", 1920, 1080,
					"sysdate-100");
			addMesajPoza("8", "Marius", "Marius", " ", "toti", 1920, 1080,
					"sysdate-3");
			addMesajPoza("9", "Marius", "Marius", " ", "toti", 1920, 1080,
					"sysdate-1");
			addMesajPoza("10", "Marius", "Marius", " ", "toti", 1920, 1080,
					"sysdate-2");
			addMesajPoza("11", "Marius", "Marius", " ", "toti", 1920, 1080,
					"sysdate-4");
			addMesajPoza("12", "Marius", "Marius", " ", "toti", 1920, 1080,
					"sysdate-4");
			addMesajPoza("13", "Marius", "Marius", " ", "toti", 1920, 1080,
					"sysdate-4");
			addMesajPoza("14", "Marius", "Marius", " ", "toti", 1920, 1080,
					"sysdate-4");

			// pt ana
			addMesajPoza("15", "Ana", "Ana", " ", "toti", 1920, 1080,
					"sysdate-" + 0.10);
			addMesajPoza("16", "Marius", "Ana", " ", "toti", 1920, 1080,
					"sysdate-" + 0.4);
			addMesajPoza("17", "Ana", "Ana", " ", "toti", 1920, 1080,
					"sysdate-2");
			addMesajPoza("18", "Maria", "Ana", " ", "toti", 1920, 1080,
					"sysdate-12");
			addMesajPoza("19", "Marius", "Ana", " ", "toti", 1920, 1080,
					"sysdate-4");
			addMesajPoza("20", "Bogdan", "Ana", " ", "toti", 1920, 1080,
					"sysdate-200");
			addMesajPoza("21", "Vasile", "Ana", " ", "toti", 1920, 1080,
					"sysdate-500");

			// pt Maria
			addMesajPoza("22", "Ana", "Maria", " ", "toti", 1920, 1080,
					"sysdate-" + 0.10);
			addMesajPoza("23", "Marius", "Maria", " ", "toti", 1920, 1080,
					"sysdate-" + 0.4);
			addMesajPoza("24", "Ana", "Maria", " ", "toti", 1920, 1080,
					"sysdate-2");
			addMesajPoza("25", "Maria", "Maria", " ", "toti", 1920, 1080,
					"sysdate-12");
			addMesajPoza("26", "Marius", "Maria", " ", "toti", 1920, 1080,
					"sysdate-4");
			addMesajPoza("27", "Bogdan", "Maria", " ", "toti", 1920, 1080,
					"sysdate-200");
			addMesajPoza("28", "Vasile", "Maria", " ", "toti", 1920, 1080,
					"sysdate-500");

			// adaug comentarii unui post
			// addComentText(int coment_id, String cont, String mesaj,String
			// data)
			addComentText(9, "Ana", "frumoasa poza", "sysdate-1.3");
			addComentText(9, "Marius", "Ms Ana", "sysdate-1.2");
			
			
			//addComentPoza(int idComent, String numePoza,String autor, String mesaj, String data, int width, int height)
			addComentPoza(9,"29","Bogdan","Uite alta poza interesanta","sysdate-3",1920,1080);
			addComentPoza(9,"30","Maria"," ","sysdate-4",1920,1080);
			addComentPoza(9,"31","Vasile"," ","sysdate-5",1920,1080);
			addComentPoza(9,"32","Radu"," ","sysdate-6",1920,1080);
			addComentPoza(9,"33","Oana"," ","sysdate-6",1920,1080);
			addComentPoza(9,"34","Ana"," ","sysdate-6",1920,1080);
			
			
			// adaug puncte prieten temporar
			for (int i = 0; i < useri.length; i++) {
				try {
					int puncte = (int) ((Math.random() * 10000) % 300);
					s.executeUpdate("UPDATE profiluri SET puncte_prieten_temporar="
							+ puncte
							+ " WHERE persoana_username='"
							+ useri[i]
							+ "'");
					s.executeQuery("COMMIT");
				} catch (Exception e) {
					s.executeQuery("ROLLBACK");
				}

			}
				
			
			//bag date in arhiva de prieteni temporari pt marius
			try{
			s.executeUpdate("INSERT into arhiva_prieteni_temporari(id,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,data_finalizarii,a_primit_user_rating)"
					+ "VALUES(secventa11.NEXTVAL,'Marius','Bogdan',SYSDATE-11,'DA','DA',SYSDATE-10,'DA')");
			s.executeUpdate("INSERT into arhiva_prieteni_temporari(id,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,data_finalizarii,a_primit_user_rating)"
					+ "VALUES(secventa11.NEXTVAL,'Bogdan','Marius',SYSDATE-11,'DA','DA',SYSDATE-10,'DA')");
		
			s.executeUpdate("INSERT into arhiva_prieteni_temporari(id,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,data_finalizarii,a_primit_user_rating)"
					+ "VALUES(secventa11.NEXTVAL,'Marius','Vasile',SYSDATE-10,'DA','DA',SYSDATE-9,'DA')");
			s.executeUpdate("INSERT into arhiva_prieteni_temporari(id,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,data_finalizarii,a_primit_user_rating)"
					+ "VALUES(secventa11.NEXTVAL,'Vasile','Marius',SYSDATE-10,'DA','DA',SYSDATE-9,'DA')");
			
			
			s.executeUpdate("INSERT into arhiva_prieteni_temporari(id,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,data_finalizarii,a_primit_user_rating)"
					+ "VALUES(secventa11.NEXTVAL,'Marius','Radu',SYSDATE-9,'DA','DA',SYSDATE-8,'DA')");
			s.executeUpdate("INSERT into arhiva_prieteni_temporari(id,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,data_finalizarii,a_primit_user_rating)"
					+ "VALUES(secventa11.NEXTVAL,'Radu','Marius',SYSDATE-9,'DA','DA',SYSDATE-8,'DA')");
			
			
			s.executeUpdate("INSERT into arhiva_prieteni_temporari(id,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,data_finalizarii,a_primit_user_rating)"
					+ "VALUES(secventa11.NEXTVAL,'Marius','Madalina',SYSDATE-8,'DA','DA',SYSDATE-7,'DA')");
			s.executeUpdate("INSERT into arhiva_prieteni_temporari(id,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,data_finalizarii,a_primit_user_rating)"
					+ "VALUES(secventa11.NEXTVAL,'Madalina','Marius',SYSDATE-8,'DA','DA',SYSDATE-7,'DA')");
			
			
			
			s.executeUpdate("INSERT into arhiva_prieteni_temporari(id,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,data_finalizarii,a_primit_user_rating)"
					+ "VALUES(secventa11.NEXTVAL,'Marius','Razvan',SYSDATE-7,'DA','DA',SYSDATE-6,'DA')");
			s.executeUpdate("INSERT into arhiva_prieteni_temporari(id,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,data_finalizarii,a_primit_user_rating)"
					+ "VALUES(secventa11.NEXTVAL,'Razvan','Marius',SYSDATE-7,'DA','DA',SYSDATE-6,'DA')");
			
			
			s.executeUpdate("INSERT into arhiva_prieteni_temporari(id,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,data_finalizarii,a_primit_user_rating)"
					+ "VALUES(secventa11.NEXTVAL,'Marius','Andrei',SYSDATE-6,'DA','DA',SYSDATE-5,'DA')");
			s.executeUpdate("INSERT into arhiva_prieteni_temporari(id,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,data_finalizarii,a_primit_user_rating)"
					+ "VALUES(secventa11.NEXTVAL,'Andrei','Marius',SYSDATE-6,'DA','DA',SYSDATE-5,'DA')");
			
			
			s.executeUpdate("INSERT into arhiva_prieteni_temporari(id,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,data_finalizarii,a_primit_user_rating)"
					+ "VALUES(secventa11.NEXTVAL,'Marius','Cosmin',SYSDATE-4,'DA','DA',SYSDATE-3,'DA')");
			s.executeUpdate("INSERT into arhiva_prieteni_temporari(id,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,data_finalizarii,a_primit_user_rating)"
					+ "VALUES(secventa11.NEXTVAL,'Cosmin','Marius',SYSDATE-4,'DA','DA',SYSDATE-3,'DA')");
			
			
			s.executeUpdate("COMMIT");
			}
			catch(Exception e){
				System.out.println(e);
				try{
			s.executeUpdate("ROLLBACK");
				}catch(Exception e2){
					System.out.println(e2);
				}
			}
			
			
			
			// trimite mesaje private
			// trimiteMesajePaginaMesaje(String cont,String
			// partenerConversatie,String mesaj,String data)
			for (int i = 0; i < 40; i++) {
				try {
					trimiteMesajePaginaMesaje("Marius", "Cosmin", i + "",
							"sysdate-1");
					Thread.sleep(1000);
				} catch (Exception e) {
					System.out.println(e);
				}
			}

			try {
				trimiteMesajePaginaMesaje("Ana", "Marius", "Buna ce faci?",
						"sysdate");
				Thread.sleep(1000);
				trimiteMesajePaginaMesaje("Marius", "Ana", "Eu bine tu?",
						"sysdate");
				Thread.sleep(1000);
				trimiteMesajePaginaMesaje("Ana", "Marius",
						"Eu tot bine. Ai vzt Avengers?", "sysdate");
				Thread.sleep(1000);
				trimiteMesajePaginaMesaje("Marius", "Ana",
						"Inca nu o sa ma duc maine sa il vad", "sysdate");
				Thread.sleep(1000);
				trimiteMesajePaginaMesaje("Ana", "Marius",
						"O sa iti placa la nebunie", "sysdate");
				Thread.sleep(1000);
				trimiteMesajePaginaMesaje("Bogdan", "Marius",
						"Sa nu uiti ca maine mergem la avengers", "sysdate-0.5");
				Thread.sleep(1000);
				trimiteMesajePaginaMesaje("Marius", "Maria",
						"Buna ce mai faci?", "sysdate-1");
				Thread.sleep(1000);
				trimiteMesajePaginaMesaje("Marius", "Radu",
						"Joci ceva mai tarziu?", "sysdate-2");
				Thread.sleep(1000);
				trimiteMesajePaginaMesaje("Marius", "Vasile",
						"Ti-ai facut tema pentru maine?", "sysdate-5");
				Thread.sleep(1000);
				trimiteMesajePaginaMesaje("Marius", "Victor",
						"Vin la madalina mai tarziu", "sysdate-6");
				Thread.sleep(1000);
				trimiteMesajePaginaMesaje("Marius", "Madalina",
						"Ce frumos e afara", "sysdate-7");
				Thread.sleep(1000);
			} catch (Exception e) {
				System.out.println(e);
			}

		} catch (Exception e) {
			System.out.println(e);
		}

	}

}
