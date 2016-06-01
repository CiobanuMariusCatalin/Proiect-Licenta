package CREARE_BAZA_DE_DATE;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class CreazaBazaDeDate {
	public static Statement s;
	public static Connection conn;
	static ResultSet rs;

	public static void creere() {
		try {

			//folosit pentru profiluri
			s.executeUpdate("CREATE SEQUENCE secventa1");
			//folosit pentru comenturi
			s.executeUpdate("CREATE SEQUENCE secventa2");
			//folosit pentru albume poze
			s.executeUpdate("CREATE SEQUENCE secventa3");
			// folosit pentru poze
			s.executeUpdate("CREATE SEQUENCE secventa4");
			//folosit in trigger pentru update_profiluri_table
			s.executeUpdate("CREATE SEQUENCE secventa5");
			//folosit pentru last_update_profil
			s.executeUpdate("CREATE SEQUENCE secventa6");
			//folosit pentru last_update_news
			s.executeUpdate("CREATE SEQUENCE secventa7");
			//folosit pentru mesaje
			s.executeUpdate("CREATE SEQUENCE secventa8");
			//folosit pentru interese_persoane
			s.executeUpdate("CREATE SEQUENCE secventa9");
			//folosit pentru prieteni_temporari
			s.executeUpdate("CREATE SEQUENCE secventa10");
			//folosit pentru arhiva_prieteni_temporari
			s.executeUpdate("CREATE SEQUENCE secventa11");
			//folosit pentru renunta_recomandari_prieteni
			s.executeUpdate("CREATE SEQUENCE secventa12");
			// 1

			s.executeUpdate("CREATE TABLE persoane(username VARCHAR2(50) PRIMARY KEY,"
					+ "parola VARCHAR2(50) NOT NULL)");
			// 2
			s.executeUpdate("CREATE TABLE grupuri(name VARCHAR2(50)  PRIMARY KEY)");

			// 3
			s.executeUpdate("CREATE TABLE profiluri(id NUMBER(10)  PRIMARY KEY ,"
					+ "persoana_username VARCHAR2(50) REFERENCES persoane(username) ON DELETE CASCADE, "
					+ "grup_name VARCHAR2(50) REFERENCES grupuri(name) ON DELETE CASCADE,"
					+"puncte_prieten_temporar NUMBER(10) DEFAULT 0"

					+")");
			// 4
			s.executeUpdate("CREATE TABLE albume_poze(id NUMBER(10) PRIMARY KEY,"
					+ "profil_id NUMBER(10) REFERENCES profiluri(id) ON DELETE CASCADE,"
					+ "nume_album VARCHAR2(50) NOT NULL )");

			// 5
			s.executeUpdate("CREATE TABLE poze(id NUMBER(10) PRIMARY KEY,"
					+ "album_id NUMBER(10) REFERENCES albume_poze(id) ON DELETE CASCADE,"
					+ "data blob,"
					+ "textul VARCHAR2(500),"
					+ "width NUMBER(20) ,"
					+ "height NUMBER(20) )");
			// 6
			s.executeUpdate("CREATE TABLE comenturi(id NUMBER(10) PRIMARY KEY,"
					+ "profil_id NUMBER(10) REFERENCES profiluri(id) ON DELETE CASCADE,"
					+ "coment_id NUMBER(10) REFERENCES comenturi(id) ON DELETE CASCADE,"
					+ "poza_id NUMBER(10) REFERENCES poze(id) ON DELETE SET NULL, "
					+ "textul VARCHAR2(500) NOT NULL,"
					+  "vizibilitate VARCHAR2(50) DEFAULT 'toti',"
					+ "autor VARCHAR2(50) NOT NULL,"
					+ "cuPoza NUMBER(10) REFERENCES poze(id) ON DELETE CASCADE,"
					+ "data_postari DATE NOT NULL" + ")");
			// 7
			s.executeUpdate("CREATE TABLE liste_prieteni(username VARCHAR(50) ,"
					+ "prieten VARCHAR2(50) ,"
					+ "acceptat VARCHAR2(50) DEFAULT 'NU',"
					+ "PRIMARY KEY(username,prieten)" + ")");
			// 8
			s.executeUpdate("CREATE TABLE persoane_grupuri(grup_name VARCHAR2(50) REFERENCES grupuri(name) ON DELETE CASCADE,"
					+ "persoana_username VARCHAR2(50) REFERENCES persoane(username) ON DELETE CASCADE,"
					+ "PRIMARY KEY(grup_name,persoana_username))");
			// 9
			s.executeUpdate("CREATE TABLE mesaje(id NUMBER(10) PRIMARY KEY,"
					+ "username_sender VARCHAR2(50) REFERENCES persoane(username) ON DELETE CASCADE,"
					+ "username_receiver VARCHAR2(50) REFERENCES persoane(username) ON DELETE CASCADE,"
					+ "mesaj VARCHAR2(500) NOT NULL,"
					+ "data_mesaj DATE NOT NULL"
					+ ")");
			// 10
			s.executeUpdate("CREATE TABLE poza_profil_default(poza_id NUMBER(10) REFERENCES poze(id) ON DELETE CASCADE)");
			s.executeUpdate("ALTER TABLE profiluri ADD(poza_profil NUMBER(10) REFERENCES poze(id) ON DELETE SET NULL)");

			// 11
			s.executeUpdate("CREATE TABLE update_profiluri_table("
					+ "id NUMBER(10) PRIMARY KEY,"
					+ "profil_id NUMBER(10) REFERENCES profiluri(id) ON DELETE CASCADE,"
					+ "data_updatarii DATE)");
			// 12
			s.executeUpdate("CREATE TABLE last_update_profil("
					+ "id NUMBER(10) PRIMARY KEY,"
					+ "profil_id NUMBER(10) REFERENCES profiluri(id) ON DELETE CASCADE,"
					+ "data_updatarii DATE)");
			// 12
			s.executeUpdate("CREATE TABLE last_update_news("
					+ "id NUMBER(10) PRIMARY KEY,"
					+ "profil_id NUMBER(10) REFERENCES profiluri(id) ON DELETE CASCADE,"
					+ "data_updatarii DATE)");
			// 12
			s.executeUpdate("CREATE TABLE last_update_coments("
					+ "id NUMBER(10) PRIMARY KEY,"
					+ "coment_id NUMBER(10) REFERENCES comenturi(id) ON DELETE CASCADE,"
					+ "data_updatarii DATE)");
			// 13
			s.executeUpdate("CREATE OR REPLACE TRIGGER update_coments_trigger "
					+ "BEFORE UPDATE OF poza_profil ON profiluri "
					+ "FOR EACH ROW "
					+ "BEGIN "
					+ "DELETE FROM update_profiluri_table WHERE profil_id=:NEW.id;"
					+ "INSERT INTO update_profiluri_table(id,profil_id,data_updatarii) "
					+ "VALUES(secventa5.NEXTVAL,:NEW.id,sysdate);" + "END;");
			//
			s.executeUpdate("CREATE TABLE interese_persoane("
					+"id NUMBER(10) PRIMARY KEY,"
					+"persoana_username VARCHAR2(50) REFERENCES persoane(username),"
					+"interes VARCHAR(500) NOT NULL,"
					+ "data_adaugarii DATE NOT NULL,"
					+ "rating NUMBER(1) NOT NULL)"
					);
			s.executeUpdate("CREATE TABLE prieteni_temporari("
					+"id NUMBER(10) PRIMARY KEY,"
					+"persoana_username VARCHAR2(50) REFERENCES persoane(username),"
					+"prieten VARCHAR2(50) REFERENCES persoane(username),"
					//a_trimis_mesaj imi spune daca persoana_username a trimis un mesaj
					//pe durata cat erau prieteni temporari catre persoana "prieten"
					//a_primit_mesaj zice daca persoana_username a primit vreun mesaj
					//de la prieten in timp ce erau prieteni temporari
					//daca ambele sunt "DA" inseamna ca au avut o conversatie
					//deci le dau 5 puncte amandorura
					//daca este "NU" nu le dau nimic
					+"a_trimis_mesaj VARCHAR(10) DEFAULT 'NU',"
					+"a_primit_mesaj VARCHAR(10) DEFAULT 'NU',"
					+ "data_adaugarii DATE NOT NULL)"
					);
			s.executeUpdate("CREATE TABLE arhiva_prieteni_temporari("
					+"id NUMBER(10) PRIMARY KEY,"
					+"persoana_username VARCHAR2(50) REFERENCES persoane(username) ON DELETE CASCADE,"
					+"prieten VARCHAR2(50) REFERENCES persoane(username) ON DELETE CASCADE,"
					//a_trimis_mesaj si a_primit_mesaj sunt la fel ca in tabelul prieteni_temporari
					+"a_trimis_mesaj VARCHAR(10) DEFAULT 'NU',"
					+"a_primit_mesaj VARCHAR(10) DEFAULT 'NU',"
					//au primite user rating il folosesc in urmatorul caz
					//dupa ce prietenii temporari au fost redistribuiti daca
					//au transmis mesaje intre ei ii vor pune sa isi dea unui altuia
					//user rating acest user rating va adauga puncte partenerului
					//deci ei sunt motivati sa comunice astfel incat sa primeasca puncte
					//de la partener.Folosesc acest camp sa imi dau seama daca au dat punctele
					//in forma de user rating dar doar in cazul daca au transmis un mesaj unui
					//altuia
					+"a_primit_user_rating VARCHAR(10) DEFAULT 'NU',"
					+ "data_adaugarii DATE NOT NULL,"
					+ "data_finalizarii DATE NOT NULL)"
					);
			//folosesc sa imi dau seama cand un utilizator nu vrea o persoana
			//ca recomandare de prieten
			s.executeUpdate("CREATE TABLE renunta_recomandari_prieteni("
					+ "id NUMBER(10) PRIMARY KEY,"
					+ "persoana_username VARCHAR(50) REFERENCES persoane(username) ON DELETE CASCADE,"
					+ "recomandare VARCHAR(50) REFERENCES persoane(username) ON DELETE CASCADE,"
					+ "UNIQUE(persoana_username,recomandare))");
			//folosesc tabelul sa imi dau seama cand un utilizator a primit mesaje noi
			s.executeUpdate("CREATE TABLE mesaje_noi("
					+ "username_sender VARCHAR(50) REFERENCES persoane(username) ON DELETE CASCADE,"
					+ "username_receiver VARCHAR(50) REFERENCES persoane(username) ON DELETE CASCADE,"
					+ "PRIMARY KEY(username_sender,username_receiver))");
			// s.executeUpdate("CREATE SEQUENCE secventa;");
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	// poza de profil default pentru toata lumea
	public static void addPozaDefault() {
		try {
			File fisier1 = new File("avatar.jpg");
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
			PreparedStatement pstmt = conn.prepareStatement(temp);
			pstmt.setInt(1, secventa);
			pstmt.setBytes(2, bytes);
			//m-am uitat eu la rezolutia pozei si am pus datele
			pstmt.setInt(3,100);
			pstmt.setInt(4,100);
			pstmt.executeUpdate();
			s.executeUpdate("INSERT INTO poza_profil_default VALUES("
					+ secventa + ")");

			s.executeUpdate("COMMIT");
			buf.close();
		} catch (Exception e) {
			System.out.println(e + " 2");
		}
	}

	public static void distrugere() {
		try {

			try {
				s.executeUpdate("DROP TABLE poza_profil_default");
			} catch (Exception e) {
				System.out.println(e + "1");
			}
			try {
				s.executeUpdate("ALTER TABLE profiluri DROP COLUMN poza_profil ");
			} catch (Exception e) {
				System.out.println(e + "2");
			}
			try {
				s.executeUpdate("DROP SEQUENCE secventa1");
			} catch (Exception e) {
				System.out.println(e + "3");
			}
			try {
				s.executeUpdate("DROP SEQUENCE secventa2");
			} catch (Exception e) {
				System.out.println(e + "4");
			}
			try {
				s.executeUpdate("DROP SEQUENCE secventa3");
			} catch (Exception e) {
				System.out.println(e + "5");
			}
			try {
				s.executeUpdate("DROP SEQUENCE secventa4");
			} catch (Exception e) {
				System.out.println(e + "6");
			}
			try {
				s.executeUpdate("DROP SEQUENCE secventa5");
			} catch (Exception e) {
				System.out.println(e + "16");
			}
			try {
				s.executeUpdate("DROP SEQUENCE secventa6");
			} catch (Exception e) {
				System.out.println(e + "17");
			}
			try {
				s.executeUpdate("DROP SEQUENCE secventa7");
			} catch (Exception e) {
				System.out.println(e + "27");
			}
			//
			try {
				s.executeUpdate("DROP SEQUENCE secventa8");
			} catch (Exception e) {
				System.out.println(e + "28");
			}
//
			try {
				s.executeUpdate("DROP SEQUENCE secventa9");
			} catch (Exception e) {
				System.out.println(e + "29");
			}
			try {
				s.executeUpdate("DROP SEQUENCE secventa10");
			} catch (Exception e) {
				System.out.println(e + "32");
			}
			try {
				s.executeUpdate("DROP SEQUENCE secventa11");
			} catch (Exception e) {
				System.out.println(e + "33");
			}
			try {
				s.executeUpdate("DROP SEQUENCE secventa12");
			} catch (Exception e) {
				System.out.println(e + "35");
			}
			
			
			try{
				s.executeUpdate("DROP TABLE  mesaje_noi");
			} catch (Exception e) {
				System.out.println(e + "36");
			}
			//
			try{
			s.executeUpdate("DROP TABLE  renunta_recomandari_prieteni");
		} catch (Exception e) {
			System.out.println(e + "31");
		}
			//
			try{
			s.executeUpdate("DROP TABLE prieteni_temporari");
		} catch (Exception e) {
			System.out.println(e + "31");
		}
			try{
			s.executeUpdate("DROP TABLE arhiva_prieteni_temporari");
		} catch (Exception e) {
			System.out.println(e + "34");
		}
			//
			try{
			s.executeUpdate("DROP TABLE interese_persoane");
		} catch (Exception e) {
			System.out.println(e + "30");
		}
			//
			try {
				s.executeUpdate("DROP TABLE update_profiluri_table");
			} catch (Exception e) {
				System.out.println(e + "18");
			}
			//
			try {
				s.executeUpdate("DROP TABLE last_update_profil");
			} catch (Exception e) {
				System.out.println(e + "19");
			}
			//
			try {
				s.executeUpdate("DROP TABLE last_update_news");
			} catch (Exception e) {
				System.out.println(e + "21");
			}
			//
			try {
				s.executeUpdate("DROP TABLE last_update_coments");
			} catch (Exception e) {
				System.out.println(e + "22");
			}
			//
			try {
				s.executeUpdate("DROP TRIGGER update_coments_trigger");
			} catch (Exception e) {
				System.out.println(e + "20");
			}
			// 1
			try {
				s.executeUpdate("DROP TABLE mesaje");
			} catch (Exception e) {
				System.out.println(e + "7");
			}
			// 2
			try {
				s.executeUpdate("DROP TABLE persoane_grupuri");
			} catch (Exception e) {
				System.out.println(e + "8");
			}
			// 3
			try {
				s.executeUpdate("DROP TABLE liste_prieteni");
			} catch (Exception e) {
				System.out.println(e + "9");
			}
			// 4
			try {
				s.executeUpdate("DROP TABLE comenturi");
			} catch (Exception e) {
				System.out.println(e + "10");
			}
			// 5
			try {
				s.executeUpdate("DROP TABLE poze");
			} catch (Exception e) {
				System.out.println(e + "11");
			}
			// 6
			try {
				s.executeUpdate("DROP TABLE albume_poze");
			} catch (Exception e) {
				System.out.println(e + "12");
			}

			// 7
			try {
				s.executeUpdate("DROP TABLE profiluri");
			} catch (Exception e) {
				System.out.println(e + "13");
			}
			// 8
			try {
				s.executeUpdate("DROP TABLE persoane");
			} catch (Exception e) {
				System.out.println(e + "14");
			}
			// 9
			try {
				s.executeUpdate("DROP TABLE grupuri");
			} catch (Exception e) {
				System.out.println(e + "15");
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void addDate() {
		try {
			s.executeUpdate("INSERT INTO tabel VALUES(5)");

		} catch (Exception e) {
			System.out.println(e);
		}

	}

	public static void main(String[] args) {
		try {

			// Connection conn = DriverManager
			// .getConnection("jdbc:ucanaccess://C:/BackUp/Universitate/Proiect Licenta/Baza de date/Licenta.accdb");
			// Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(
					"jdbc:oracle:thin:@localhost:1521:orcl", "cont",
					"parola");

			s = conn.createStatement();

			CreazaBazaDeDate.distrugere();
			CreazaBazaDeDate.creere();
			CreazaBazaDeDate.addPozaDefault();
			// s.executeUpdate("INSERT INTO albume_poze(id,profil_id,nume_album) VALUES("
			// +"secventa3.NEXTVAL,2,\'default\')");

			// Main.addDate();
		} catch (Exception e) {
			System.out.println(e);
		}

	}
}
