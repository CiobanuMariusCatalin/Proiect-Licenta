package Serverul;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Properties;

import oracle.jdbc.pool.*;

public class Serverul extends Thread {
	private DataInputStream dis;
	private DataOutputStream dos;
	private Connection conn;
	private Socket cs;
	private Statement stmt;
	private ResultSet rs;
	static private OracleDataSource ods;

	static void getODS(OracleDataSource oracleds) {
		ods = oracleds;
	}

	Serverul(DataInputStream dis, DataOutputStream dos, Socket cs) {

		this.dis = dis;
		this.dos = dos;
		this.cs = cs;
	}

	public void run() {
		try {
			synchronized (ods) {
				// AM MAI PUS UN TRY CATCH IN INTERIORUL ACESTUI
				// SYNCHRONIZED PENTRU CA DACA AVEM VREO EROARE
				// VREAU SA DAU ROLLBACK LA TOT, DACA PUNEAM IN
				// LOCK-URI DIFERITE NU IMI GARANTA NIMENI
				// CA SE EXECUTAU UNUL DUPA ALTUL SI PUTEAM SA AM
				// BLOCUL MEU , CARE SE STRICA PE LO JUMATE FACEA DOAR
				// JUMATE DIN INSERARILE
				// DORI IESEA DIN LOCK INTRA ALT BLOCK FACEA TOT SI DADEA
				// COMMIT
				// SI IMI SALVA CE NU VROIAM EU DUPA CE DADEA ROLLBACKUL
				// NUMAI AVEA NICI UN EFECT
				conn = ods.getConnection();
			}

			;
			stmt = conn.createStatement();
			String comanda = dis.readUTF();
			/*
			 * Am sincronizat oriunde erau operatii LMD ca sa fiu sigur ca
			 * COMMITUL SAU ROLLBACKLUL URMAU DUPA CE VREAU EU CI NU SE SCHIMBA
			 * LA ALTE THREADURI CE PUNE ALT COD SQL SI IL SINCRONIZEZ DIN
			 * GRESEALA AM MAI SINCRONIZAT SI UNDE AVEAM 2 SAU MAI MULTE
			 * OPERATII CE SE FOLOSEAU UNA DE REZULTATUL CELELATE DE EXEMPLU UNA
			 * SPUNEA NUMARUL DE COMENTURI ALTA ALEGEA COMENTURILE , DACA NU
			 * FACEAM ASTA PUTEAM SA AM UN CAZ DE FORMA BD ZICEA CA SUNT 15
			 * COMENTURI SE TRECEA PE ALT THREAD SE ADAUGA UN COMENT SI
			 * INTORCEAM PREA MULTE SI PE CLIENT LUAM EROARE
			 */
		
				if (comanda.equals("signUp")) {
					System.out.println("signUP");
					String cont = dis.readUTF();
					String parola = dis.readUTF();

					synchronized (ods) {
					try {
						rs = stmt
								.executeQuery("SELECT '1' FROM persoane WHERE username="
										+ "'" + cont + "'");
						/*
						 * System.out.println("A intrat in lock"); int i = 1;
						 * while (i < 35) { Thread.sleep(1000);
						 * System.out.println(i); i++; }
						 */

						boolean gasit = false;
						while (rs.next()) {
							gasit = true;
						}
						if (gasit == true) {
							dos.writeUTF("esec");
						} else {

							stmt.executeUpdate("INSERT INTO persoane(username,parola) VALUES("
									+ "'"
									+ cont
									+ "',"
									+ "'"
									+ parola
									+ "'"
									+ ")");

							rs = stmt
									.executeQuery("SELECT secventa1.NEXTVAL FROM DUAL");
							// am nevoie de numarul de secventa pentru idul
							// profilului
							// si pentru cheia externa la albumul default
							int nrSecventa = -1;
							while (rs.next()) {
								nrSecventa = rs.getInt(1);
							}
							// scot avatarul default si il pun profilului
							rs = stmt
									.executeQuery("SELECT poza_id FROM poza_profil_default");
							int poza_profil = -1;
							while (rs.next()) {
								poza_profil = rs.getInt(1);
							}

							String temp = "INSERT INTO profiluri(id,persoana_username,poza_profil) VALUES(?,?,?)";
							PreparedStatement pstmt = conn
									.prepareStatement(temp);
							pstmt.setInt(1, nrSecventa);
							pstmt.setString(2, cont);
							pstmt.setInt(3, poza_profil);
							pstmt.executeUpdate();

							stmt.executeUpdate("INSERT INTO albume_poze(id,profil_id,nume_album) VALUES("
									+ "secventa3.NEXTVAL,"
									+ nrSecventa
									+ ","
									+ "'default')");
							stmt.executeUpdate("COMMIT");
						}
						dos.writeUTF("succes");
					} catch (Exception e) {
						stmt.executeUpdate("ROLLBACK");
						System.out.println(e);
					}
				}
			}

			if (comanda.equals("login")) {
				System.out.println("login");
				String cont = dis.readUTF();
				String parola = dis.readUTF();

				String temp = "SELECT '1' FROM persoane WHERE username =? AND parola=?";

				PreparedStatement pstmt = conn.prepareStatement(temp);
				pstmt.setString(1, cont);
				pstmt.setString(2, parola);

				rs = pstmt.executeQuery();

				boolean gasit = false;
				while (rs.next()) {
					gasit = true;
				}
				if (gasit == true) {

					dos.writeUTF("acceptat");
				} else
					dos.writeUTF("respins");

			}

			if (comanda.equals("startPaginaProfil")) {
				System.out.println("startPaginaProfil");
				String profil = dis.readUTF();
				String cont = dis.readUTF();

				synchronized (ods) {
					rs = stmt
							.executeQuery("Select '1' FROM profiluri WHERE persoana_username="
									+ "'" + profil + "'");

					boolean profilExistent = false;
					while (rs.next()) {
						profilExistent = true;
					}
					dos.writeBoolean(profilExistent);
					if (profilExistent == false) {
						conn.close();
						dis.close();
						dos.close();
						cs.close();
						return;
					}
					// puncte prieten temporar
					rs = stmt
							.executeQuery("SELECT puncte_prieten_temporar FROM profiluri WHERE persoana_username='"
									+ profil + "'");
					int puncte = -1;
					while (rs.next()) {
						puncte = rs.getInt(1);
					}
					// trimite cate puncte are persoana cu acest profil
					dos.writeInt(puncte);
					
					
					//trimit pe ce loc se afla in clasament
					rs=stmt.executeQuery("SELECT persoana,rownum FROM"
							//TREBUIE sa iau rownum dupa ce face ordonarea daca puneam rownum direct
							//in selectul de mai jos nu mergea ca imi dadea rownum inainte sa fie sortat
							+ " (SELECT persoana_username persoana FROM profiluri  "
							+ "ORDER BY puncte_prieten_temporar DESC,persoana_username ASC)");
					
					while(rs.next()){
						if(rs.getString(1).equals(profil)){
						
							//trimit locul sau in clasament calculat live
							dos.writeInt(rs.getInt(2));
						}
					}
					
					
					// trimit poza de profil a profilului curent
					rs = stmt
							.executeQuery("SELECT poza_profil FROM profiluri WHERE persoana_username='"
									+ profil + "'");
					int id = -1;
					while (rs.next()) {
						id = rs.getInt(1);
					}
					// trimit idul pozei de profil a profilului curent
					dos.writeInt(id);

					rs = stmt
							.executeQuery("SELECT COUNT(*) FROM comenturi c WHERE "
									+ "profil_id = "
									+ "(SELECT id FROM profiluri WHERE  persoana_username ="
									+ "'"
									+ profil
									+ "') "
									// am introdus si vizibilitatea comenturilor
									// vizibilitatea poate sa
									// fie:eu,prieteni,toti
									// aceste optiuni sunt doar pentru cel carui
									// ii apartine contul
									// implicit restul au vizibilitatea toti
									// daca este 'eu' doar cel carui ii apartine
									// profilul
									// ii sunt vizibile posturile
									// daca este'prieteni' doar celui carui ii
									// apartine profilul si prietenii lui
									// pot vedea posturile
									// daca este 'toti' oricine poate vedea
									// posturile
									+ " AND(vizibilitate='toti' OR(vizibilitate='eu' AND "
									+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
									+ cont
									+ "')"
									+ "OR(vizibilitate='prieteni' AND( '"
									+ cont
									+ "' IN("
									+ "SELECT prieten FROM liste_prieteni WHERE username='"
									+ profil
									+ "' AND acceptat='DA' ) OR "
									+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
									+ cont + "'" + ")))" + "");

					while (rs.next()) {
						if (rs.getInt(1) > 10)
							dos.writeInt(10);
						else
							dos.writeInt(rs.getInt(1));
					}

					rs = stmt
							.executeQuery("SELECT c.id,c.textul,c.autor,c.cuPoza,"
									+ "(SELECT p.poza_profil FROM profiluri p WHERE p.persoana_username="
									+ "c.autor),TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss')"
									+ " FROM comenturi c WHERE"
									+ " profil_id = "
									+ " (SELECT id FROM profiluri WHERE persoana_username = "
									+ " '"
									+ profil
									+ "')"
									// am introdus si vizibilitatea comenturilor
									// vizibilitatea poate sa
									// fie:eu,prieteni,toti
									// aceste optiuni sunt doar pentru cel carui
									// ii apartine contul
									// implicit restul au vizibilitatea toti
									// daca este 'eu' doar cel carui ii apartine
									// profilul
									// ii sunt vizibile posturile
									// daca este'prieteni' doar celui carui ii
									// apartine profilul si prietenii lui
									// pot vedea posturile
									// daca este 'toti' oricine poate vedea
									// posturile
									+ " AND(vizibilitate='toti' OR(vizibilitate='eu' AND "
									+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
									+ cont
									+ "')"
									+ "OR(vizibilitate='prieteni' AND( '"
									+ cont
									+ "' IN("
									+ "SELECT prieten FROM liste_prieteni WHERE username='"
									+ profil
									+ "' AND acceptat='DA' ) OR "
									+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
									+ cont
									+ "'"
									+ ")))"
									// pana aici este despre vizibilitatea
									// postului raportata cu cine il aceseaza
									+ "ORDER BY TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss') DESC");
				}
				// incarc maxim 10 pagini
				int contor = 1;
				Statement stmt2 = conn.createStatement();
				while (rs.next() && contor <= 10) {
					contor++;
					dos.writeInt(rs.getInt(1));
					dos.writeUTF(rs.getString(2));
					dos.writeUTF(rs.getString(3));

					// poza de profil
					int idPozaProfil = rs.getInt(5);
					dos.writeInt(idPozaProfil);
					ResultSet rs2;
					rs2 = stmt2
							.executeQuery("SELECT width,height FROM poze WHERE id="
									+ idPozaProfil);
					while (rs2.next()) {
						// width
						dos.writeInt(rs2.getInt(1));
						// height
						dos.writeInt(rs2.getInt(2));

					}

					rs2.close();
					int idPoza = rs.getInt(4);

					if (idPoza != 0) {
						dos.writeUTF("arePoza");
						dos.writeInt(idPoza);
						rs2 = stmt2
								.executeQuery("SELECT width,height FROM poze WHERE id="
										+ idPoza);

						while (rs2.next()) {
							// width
							dos.writeInt(rs2.getInt(1));
							// height
							dos.writeInt(rs2.getInt(2));
						}
						rs2.close();

					} else
						dos.writeUTF("nuArePoza");
					// scriu timestampul postului
					dos.writeUTF(rs.getString(6));

				}
				synchronized (ods) {
					try {
						rs = stmt
								.executeQuery("SELECT id FROM profiluri WHERE persoana_username='"
										+ profil + "'");

						int idProfil = -1;
						while (rs.next()) {
							idProfil = rs.getInt(1);
						}
						// las decat ultima actualizare a pozei de profil , nu
						// le sterg pe toate
						// pt ca poate o vrea si news feed sau comenturi
						stmt.executeUpdate("DELETE FROM update_profiluri_table WHERE profil_id="
								+ idProfil
								+ " AND  TO_CHAR(data_updatarii,'yyyy-mm-dd:HH24:mi:ss')<"
								+ "(SELECT MAX(TO_CHAR(data_updatarii,'yyyy-mm-dd:HH24:mi:ss')) FROM update_profiluri_table"
								+ " WHERE profil_id=" + idProfil + ")");
						// sterg updateurile trecute pentru ca numai am ce face
						// cu ele
						stmt.executeUpdate("DELETE FROM last_update_profil WHERE profil_id="
								+ idProfil);
						stmt.executeUpdate("INSERT INTO last_update_profil(id,profil_id,data_updatarii) VALUES"
								+ "(secventa6.NEXTVAL,"
								+ idProfil
								+ ",sysdate)");
						stmt.executeUpdate("COMMIT");

					} catch (Exception e) {
						System.out.println(e);
						stmt.executeUpdate("ROLLBACK");
					}
				}
			}

			if (comanda.equals("addMessageProfil")) {
				System.out.println("addMessageProfil");
				String profil = dis.readUTF();
				synchronized (ods) {
					try {

						rs = stmt
								.executeQuery("Select '1' FROM profiluri WHERE persoana_username="
										+ "'" + profil + "'");
						boolean profilExistent = false;
						while (rs.next()) {
							profilExistent = true;
						}
						dos.writeBoolean(profilExistent);
						if (profilExistent == false) {
							conn.close();
							dis.close();
							dos.close();
							cs.close();
							return;
						}

						String cont = dis.readUTF();
						String mesaj = dis.readUTF();
						String vizibilitate = dis.readUTF();
						int profil_id = -1;
						rs = stmt.executeQuery("SELECT id FROM profiluri "
								+ " WHERE persoana_username = " + "'" + profil
								+ "'");
						while (rs.next()) {
							profil_id = rs.getInt(1);

						}

						stmt.executeUpdate("INSERT INTO comenturi(id,profil_id,textul,autor,data_postari,vizibilitate) VALUES("
								+ "secventa2.NEXTVAL"
								+ ","
								+ profil_id
								+ ","
								+ "'"
								+ mesaj
								+ " ',"
								+ "'"
								+ cont
								+ "',sysdate," + "'" + vizibilitate + "'" + ")");

						stmt.executeUpdate("COMMIT");
					} catch (Exception e) {
						stmt.executeUpdate("ROLLBACK");
						System.out.println(e);
					}
				}
			}

			if (comanda.equals("startPaginaComent")) {
				System.out.println("startPaginaComent");
				int comentId = dis.readInt();
				synchronized (ods) {
					rs = stmt
							.executeQuery("Select '1' FROM comenturi  WHERE id="
									+ comentId);
					boolean comentExistent = false;
					while (rs.next()) {
						comentExistent = true;
					}
					dos.writeBoolean(comentExistent);
					if (comentExistent == false) {
						conn.close();
						dis.close();
						dos.close();
						cs.close();
						return;
					}

					rs = stmt
							.executeQuery("SELECT COUNT(*) FROM comenturi WHERE "
									+ "coment_id = " + comentId);

					while (rs.next()) {
						if (rs.getInt(1) > 10)
							dos.writeInt(10);
						else
							dos.writeInt(rs.getInt(1));
					}
					rs = stmt
							.executeQuery("SELECT c.id,c.textul,c.autor,c.cuPoza,"
									+ "(SELECT p.poza_profil FROM profiluri p WHERE p.persoana_username="
									+ "c.autor),TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss')"
									+ "FROM comenturi c WHERE"
									+ " coment_id = "
									+ comentId
									+ "ORDER BY TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss') DESC");
				}
				// trimite maxim 10 comenturi
				int contor = 1;
				Statement stmt2 = conn.createStatement();
				while (rs.next() && contor <= 10) {
					contor++;
					dos.writeInt(rs.getInt(1));
					dos.writeUTF(rs.getString(2));
					dos.writeUTF(rs.getString(3));

					// poza de profil
					int idPozaProfil = rs.getInt(5);
					dos.writeInt(idPozaProfil);
					ResultSet rs2;
					rs2 = stmt2
							.executeQuery("SELECT width,height FROM poze WHERE id="
									+ idPozaProfil);
					while (rs2.next()) {
						// width
						dos.writeInt(rs2.getInt(1));
						// height
						dos.writeInt(rs2.getInt(2));

					}

					rs2.close();

					int idPoza = rs.getInt(4);

					if (idPoza != 0) {
						dos.writeUTF("arePoza");
						dos.writeInt(idPoza);
						rs2 = stmt2
								.executeQuery("SELECT width,height FROM poze WHERE id="
										+ idPoza);

						while (rs2.next()) {
							// width
							dos.writeInt(rs2.getInt(1));
							// height
							dos.writeInt(rs2.getInt(2));

						}
						rs2.close();

					} else
						dos.writeUTF("nuArePoza");
					// scriu timestampul
					dos.writeUTF(rs.getString(6));
				}

			}
			if (comanda.equals("addMessageComent")) {
				System.out.println("addMessageComent");
				int coment_id = dis.readInt();
				synchronized (ods) {
					try {
						rs = stmt
								.executeQuery("Select '1' FROM comenturi  WHERE id="
										+ coment_id);
						boolean comentExistent = false;
						while (rs.next()) {
							comentExistent = true;
						}
						dos.writeBoolean(comentExistent);
						if (comentExistent == false) {
							conn.close();
							dis.close();
							dos.close();
							cs.close();
							return;
						}

						String cont = dis.readUTF();
						String mesaj = dis.readUTF();

						rs = stmt.executeQuery("SELECT id FROM comenturi "
								+ " WHERE id = " + coment_id);
						// daca nu exista comentul parinte atunci numai adaugam
						// date
						// nenecesare in bd
						boolean existaComent = false;
						while (rs.next()) {
							existaComent = true;

						}
						if (existaComent != true)
							return;

						stmt.executeUpdate("INSERT INTO comenturi(id,coment_id,textul,autor,data_postari) VALUES("
								+ "secventa2.NEXTVAL"
								+ ","
								+ coment_id
								+ ","
								+ "'"
								+ mesaj
								+ "',"
								+ "'"
								+ cont
								+ "',sysdate)");

						stmt.executeUpdate("COMMIT");
					} catch (Exception e) {
						stmt.executeUpdate("ROLLBACK");

						System.out.println(e);
					}
				}

			}
			if (comanda.equals("searchRezults")) {
				System.out.println("searchRezults");
				String query = dis.readUTF();
				synchronized (ods) {
					rs = stmt
							.executeQuery("SELECT COUNT(*) FROM profiluri WHERE UPPER(persoana_username) LIKE(UPPER('%"
									+ query + "%'))");
					boolean trimisDimensiunea = false;
					while (rs.next()) {
						trimisDimensiunea = true;
						int nrRezultate = rs.getInt(1);
						if (nrRezultate > 15)
							dos.writeInt(15);
						else
							dos.writeInt(nrRezultate);
					}
					if (trimisDimensiunea == false) {
						dos.writeInt(0);
						conn.close();

						dis.close();
						dos.close();
						cs.close();
						return;
					}
					rs = stmt
							.executeQuery("SELECT persoana_username,poza_profil FROM profiluri WHERE UPPER(persoana_username) LIKE(UPPER('%"
									+ query + "%'))");
				}
				int nr = 0;
				Statement stmt2 = conn.createStatement();
				while (rs.next() && nr < 15) {
					nr++;

					dos.writeUTF(rs.getString(1));
					// poza de profil
					int idPozaProfil = rs.getInt(2);
					dos.writeInt(idPozaProfil);
					ResultSet rs2;
					rs2 = stmt2
							.executeQuery("SELECT width,height FROM poze WHERE id="
									+ idPozaProfil);
					while (rs2.next()) {
						dos.writeInt(rs2.getInt(1));
						dos.writeInt(rs2.getInt(2));

					}

					rs2.close();
				}

			}
			if (comanda.equals("friendList")) {
				System.out.println("friendList");
				String profil = dis.readUTF();
				synchronized (ods) {
					rs = stmt
							.executeQuery("SELECT COUNT(*) FROM liste_prieteni WHERE username="
									+ "'" + profil + "' AND acceptat='DA'");
					boolean trimisDimensiunea = false;
					while (rs.next()) {
						trimisDimensiunea = true;
						int nrRezultate = rs.getInt(1);
						// trimit maxim 20 de rezultate
						if (nrRezultate > 20)
							dos.writeInt(20);
						else
							dos.writeInt(nrRezultate);
					}
					if (trimisDimensiunea == false) {
						dos.writeInt(0);
						conn.close();
						dis.close();
						dos.close();
						cs.close();
						return;
					}
					rs = stmt
							.executeQuery("SELECT persoana_username,poza_profil FROM profiluri WHERE persoana_username IN ("
									+ "SELECT prieten FROM liste_prieteni WHERE username="
									+ "'"
									+ profil
									+ "' AND acceptat='DA')"
									+ "ORDER BY persoana_username");
				}
				int nrRezultate = 0;
				Statement stmt2 = conn.createStatement();
				while (rs.next() && nrRezultate < 20) {
					nrRezultate++;
					dos.writeUTF(rs.getString(1));
					// poza de profil
					int idPozaProfil = rs.getInt(2);
					dos.writeInt(idPozaProfil);
					ResultSet rs2;
					rs2 = stmt2
							.executeQuery("SELECT width,height FROM poze WHERE id="
									+ idPozaProfil);
					while (rs2.next()) {
						dos.writeInt(rs2.getInt(1));
						dos.writeInt(rs2.getInt(2));

					}

					rs2.close();
				}
			}
			if (comanda.equals("aflaStatusPrietenie")) {
				System.out.println("aflaStatusPrietenie");
				String contOwner = dis.readUTF();
				String profilOwner = dis.readUTF();
				synchronized (ods) {
					rs = stmt
							.executeQuery("Select '1' FROM profiluri WHERE persoana_username="
									+ "'" + profilOwner + "'");
					boolean profilExistent = false;
					while (rs.next()) {
						profilExistent = true;
					}
					dos.writeBoolean(profilExistent);
					if (profilExistent == false) {
						conn.close();
						dis.close();
						dos.close();
						cs.close();
						return;
					}
					rs = stmt
							.executeQuery("SELECT '1' FROM liste_prieteni WHERE username="
									+ "'"
									+ contOwner
									+ "' AND prieten= '"
									+ profilOwner + "' AND acceptat='DA'");
					while (rs.next()) {
						dos.writeUTF("prieteni");
						conn.close();
						dis.close();
						dos.close();
						cs.close();
						return;
					}
					rs = stmt
							.executeQuery("SELECT '1' FROM liste_prieteni WHERE username="
									+ "'"
									+ contOwner
									+ "' AND prieten= '"
									+ profilOwner + "' AND acceptat='PENDING'");
					while (rs.next()) {
						dos.writeUTF("trebuieProfilOwnerSaDeaAccept");
						conn.close();
						dis.close();
						dos.close();
						cs.close();
						return;
					}
					rs = stmt
							.executeQuery("SELECT '1' FROM liste_prieteni WHERE username="
									+ "'"
									+ profilOwner
									+ "' AND prieten= '"
									+ contOwner + "' AND acceptat='PENDING'");
					while (rs.next()) {
						dos.writeUTF("trebuieContOwnerSaDeaAccept");
						conn.close();
						dis.close();
						dos.close();
						cs.close();
						return;
					}
					dos.writeUTF("straini");
				}
			}
			if (comanda.equals("schimbaStatusPrietenie")) {
				String cont = dis.readUTF();
				String profil = dis.readUTF();
				synchronized (ods) {
					try {
						rs = stmt
								.executeQuery("Select '1' FROM profiluri WHERE persoana_username="
										+ "'" + profil + "'");
						boolean profilExistent = false;
						while (rs.next()) {
							profilExistent = true;
						}
						dos.writeBoolean(profilExistent);
						if (profilExistent == false) {
							conn.close();
							dis.close();
							dos.close();
							cs.close();
							return;
						}

						String metodaDeSchimbare = dis.readUTF();
						System.out.println("schimbaStatusPrietenie "
								+ metodaDeSchimbare);
						if (metodaDeSchimbare.equals("Adauga prieten")) {
							// sa fiu sigur ca nu exista deja un rand in tabelul
							// liste_prieteni intre cei 2
							// posibil sa apara daca apasa simultan add friend
							stmt.executeQuery("SELECT '1' FROM liste_prieteni WHERE username='"
									+ cont + "' AND prieten='" + profil + "'");
							boolean dejaExista = false;
							while (rs.next()) {
								dejaExista = true;

							}
							if (dejaExista == false) {
								stmt.executeUpdate("INSERT INTO liste_prieteni(username,prieten,acceptat) VALUES"
										+ "('"
										+ cont
										+ "','"
										+ profil
										+ "','PENDING')");
								stmt.executeUpdate("INSERT INTO liste_prieteni(username,prieten,acceptat) VALUES"
										+ "('"
										+ profil
										+ "','"
										+ cont
										+ "','NU')");

							}
						}
						if (metodaDeSchimbare
								.equals("Accepta cererea de prietenie")) {
							stmt.executeUpdate("UPDATE liste_prieteni SET acceptat='DA' WHERE "
									+ " username='"
									+ profil
									+ "' AND prieten='"
									+ cont
									+ "' AND acceptat='PENDING'");
							stmt.executeUpdate("UPDATE liste_prieteni SET acceptat='DA' WHERE "
									+ " username='"
									+ cont
									+ "' AND prieten='"
									+ profil + "' AND acceptat='NU'");

						}
						if (metodaDeSchimbare
								.equals("Respinge cererea de prietenie")) {
							stmt.executeUpdate("DELETE FROM liste_prieteni WHERE "
									+ " username='"
									+ profil
									+ "' AND prieten='"
									+ cont
									+ "' AND "
									+ "acceptat='PENDING'");
							stmt.executeUpdate("DELETE FROM liste_prieteni WHERE "
									+ " username='"
									+ cont
									+ "' AND prieten='"
									+ profil + "' AND " + "acceptat='NU'");

						}
						if (metodaDeSchimbare
								.equals("Sterge cererea de prietenie")) {
							stmt.executeUpdate("DELETE FROM liste_prieteni WHERE "
									+ " username='"
									+ cont
									+ "' AND prieten='"
									+ profil + "' AND " + "acceptat='PENDING'");

							stmt.executeUpdate("DELETE FROM liste_prieteni WHERE "
									+ " username='"
									+ profil
									+ "' AND prieten='"
									+ cont
									+ "' AND "
									+ "acceptat='NU'");

						}
						if (metodaDeSchimbare.equals("Sterge prieten")) {
							stmt.executeUpdate("DELETE FROM liste_prieteni WHERE "
									+ " username='"
									+ cont
									+ "' AND prieten='"
									+ profil + "' AND " + "acceptat='DA'");
							stmt.executeUpdate("DELETE FROM liste_prieteni WHERE "
									+ " username='"
									+ profil
									+ "' AND prieten='"
									+ cont
									+ "' AND "
									+ "acceptat='DA'");

						}
						stmt.executeUpdate("COMMIT");
					} catch (Exception e) {
						stmt.executeUpdate("ROLLBACK");
						System.out.println(e);
					}
				}
			}
			if (comanda.equals("stergeComent")) {
				System.out.println("stergeComent");

				synchronized (ods) {

					int idComent = dis.readInt();
					try {
						stmt.executeUpdate("DELETE FROM comenturi WHERE id="
								+ idComent);
						stmt.executeUpdate("COMMIT");
					} catch (Exception e) {
						stmt.executeUpdate("ROLLBACK");
						System.out.println(e);
					}
				}
			}

			if (comanda.equals("uploadPozaPost")) {
				synchronized (ods) {
					try {
						System.out.println("uploadPozaPost");
						String autor = dis.readUTF();
						String profil = dis.readUTF();
						String mesaj = dis.readUTF();
						String vizibilitate = dis.readUTF();
						int lung = dis.readInt();
						byte[] bytes = new byte[lung];
						dis.readFully(bytes, 0, lung);
						int width = dis.readInt();
						int height = dis.readInt();

						int profil_id = -1;
						rs = stmt
								.executeQuery("SELECT id FROM profiluri WHERE persoana_username="
										+ "'" + profil + "'");
						while (rs.next()) {
							profil_id = rs.getInt(1);
						}

						int album_id = -1;
						rs = stmt
								.executeQuery("SELECT id FROM albume_poze WHERE profil_id="
										+ profil_id
										+ " AND nume_album='default'");

						while (rs.next()) {
							album_id = rs.getInt(1);
						}

						int nrSecventa = -1;
						rs = stmt
								.executeQuery("SELECT secventa4.NEXTVAL FROM DUAL");
						while (rs.next()) {
							nrSecventa = rs.getInt(1);
						}
						String temp = "INSERT INTO poze(id,album_id,data,width,height) VALUES(?,?,?,?,?)";
						PreparedStatement pstmt = conn.prepareStatement(temp);
						pstmt.setInt(1, nrSecventa);
						pstmt.setInt(2, album_id);
						pstmt.setBytes(3, bytes);
						pstmt.setInt(4, width);
						pstmt.setInt(5, height);
						pstmt.executeUpdate();

						stmt.executeUpdate("INSERT INTO comenturi(id,profil_id,textul,autor,cuPoza,data_postari,vizibilitate) VALUES(secventa2.NEXTVAL,"
								+ profil_id
								+ ",'"
								+ mesaj
								+ "',"
								+ "'"
								+ autor
								+ "',"
								+ nrSecventa
								+ ","
								+ "sysdate,"
								+ "'"
								+ vizibilitate + "'" + ")");
						stmt.executeUpdate("COMMIT");
					} catch (Exception e) {
						stmt.executeUpdate("ROLLBACK");

						System.out.println(e);
					}
				}

			}
			if (comanda.equals("uploadPozaComent")) {
				synchronized (ods) {
					try {
						System.out.println("uploadPozaComent");
						// verific daca comentul exista
						int coment_id = dis.readInt();
						rs = stmt
								.executeQuery("Select '1' FROM comenturi  WHERE id="
										+ coment_id);
						boolean comentExistent = false;
						while (rs.next()) {
							comentExistent = true;
						}
						dos.writeBoolean(comentExistent);
						if (comentExistent == false) {
							conn.close();
							dis.close();
							dos.close();
							cs.close();
							return;
						}

						String autor = dis.readUTF();
						int idComent = dis.readInt();
						String mesaj = dis.readUTF();
						int lung = dis.readInt();
						byte[] bytes = new byte[lung];

						dis.readFully(bytes, 0, lung);
						int width = dis.readInt();
						int height = dis.readInt();
						int nrSecventa = -1;
						rs = stmt
								.executeQuery("SELECT secventa4.NEXTVAL FROM DUAL");
						while (rs.next()) {
							nrSecventa = rs.getInt(1);
						}
						String temp = "INSERT INTO poze(id,data,width,height) VALUES(?,?,?,?)";
						PreparedStatement pstmt = conn.prepareStatement(temp);
						pstmt.setInt(1, nrSecventa);
						pstmt.setBytes(2, bytes);
						pstmt.setInt(3, width);
						pstmt.setInt(4, height);
						pstmt.executeUpdate();

						stmt.executeUpdate("INSERT INTO comenturi(id,coment_id,textul,autor,cuPoza,data_postari) VALUES(secventa2.NEXTVAL,"
								+ idComent
								+ ",'"
								+ mesaj
								+ "',"
								+ "'"
								+ autor
								+ "'," + nrSecventa + "," + "sysdate)");

						stmt.executeUpdate("COMMIT");
					} catch (Exception e) {
						stmt.executeUpdate("ROLLBACK");
						System.out.println(e);
					}
				}
			}

			if (comanda.equals("schimbaPozaProfil")) {
				synchronized (ods) {
					try {
						System.out.println("schimbaPozaProfil");

						String profil = dis.readUTF();
						int lung = dis.readInt();
						byte[] bytes = new byte[lung];
						dis.readFully(bytes, 0, lung);
						int width = dis.readInt();
						int height = dis.readInt();
						int nrSecventa = -1;
						rs = stmt
								.executeQuery("SELECT secventa4.NEXTVAL FROM DUAL");
						while (rs.next()) {
							nrSecventa = rs.getInt(1);
						}
						String temp = "INSERT INTO poze(id,data,width,height) VALUES(?,?,?,?)";
						PreparedStatement pstmt = conn.prepareStatement(temp);
						pstmt.setInt(1, nrSecventa);
						pstmt.setBytes(2, bytes);
						pstmt.setInt(3, width);
						pstmt.setInt(4, height);
						pstmt.executeUpdate();

						stmt.executeUpdate("UPDATE profiluri SET poza_profil="
								+ nrSecventa
								+ " WHERE id=(SELECT id FROM profiluri WHERE persoana_username="
								+ "'" + profil + "')");
						stmt.executeUpdate("COMMIT");
					} catch (Exception e) {
						stmt.executeUpdate("ROLLBACK");

						System.out.println(e);
					}
				}
			}

			if (comanda.equals("startNewsFeed")) {
				System.out.println("startNewsFeed");
				String profil = dis.readUTF();
				synchronized (ods) {
					// selectez toate comenturile de pe profilul meu si toate
					// comenturile de pe
					// profilurile prietenilor
					rs = stmt
							.executeQuery("SELECT COUNT(*) FROM (("
									+ " SELECT id,textul,autor,cuPoza,data_postari,"
									+ "'"
									+ profil
									+ "' profil,vizibilitate,profil_id"
									+ " FROM "
									+ "comenturi WHERE "
									+ "profil_id = "
									+ "(SELECT id FROM profiluri WHERE  persoana_username ="
									+ "'"
									+ profil
									+ "') )"
									+ "UNION"
									+ "(SELECT id,textul,autor,cuPoza,data_postari,"
									+ "(SELECT persoana_username FROM profiluri p1 WHERE p1.id=c1.profil_id) profil,"
									+ "vizibilitate,profil_id"
									+ "  FROM comenturi c1 WHERE profil_id IN"
									+ "(SELECT id FROM profiluri WHERE persoana_username IN ("
									+ "SELECT prieten FROM liste_prieteni WHERE username='"
									+ profil
									+ "' AND acceptat='DA'))))"

									+ " WHERE "
									// am introdus si vizibilitatea comenturilor
									// vizibilitatea poate sa
									// fie:eu,prieteni,toti
									// aceste optiuni sunt doar pentru cel carui
									// ii apartine contul
									// implicit restul au vizibilitatea toti
									// daca este 'eu' doar cel carui ii apartine
									// profilul
									// ii sunt vizibile posturile
									// daca este'prieteni' doar celui carui ii
									// apartine profilul si prietenii lui
									// pot vedea posturile
									// daca este 'toti' oricine poate vedea
									// posturile
									+ "(vizibilitate='toti' OR(vizibilitate='eu' AND "
									+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
									+ profil
									+ "')"
									+ "OR(vizibilitate='prieteni' AND( '"
									+ profil
									+ "' IN("
									+ "SELECT prieten FROM liste_prieteni WHERE username=profil"
									+ " AND acceptat='DA' ) OR "
									+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
									+ profil + "'" + ")))"
									// pana aici este despre vizibilitatea
									// postului raportata cu cine il aceseaza
									+ "");
					int nr = 0;
					while (rs.next()) {
						nr = rs.getInt(1);
						if (nr > 10)
							dos.writeInt(10);
						else
							dos.writeInt(nr);
					}
					if (nr == 0) {

						conn.close();
						dis.close();
						dos.close();
						cs.close();
						return;
					}
					rs = stmt
							.executeQuery("SELECT c.id,c.textul,c.autor,c.cuPoza,"
									+ "(SELECT p.poza_profil FROM profiluri p WHERE p.persoana_username="
									+ "c.autor),c.profil,TO_CHAR(c.data_postari,'yyyy-mm-dd:HH24:mi:ss')"
									+ " FROM (("
									+ " SELECT id,textul,autor,cuPoza,data_postari,"
									+ "'"
									+ profil
									+ "' profil,vizibilitate,profil_id"
									+ " FROM "
									+ "comenturi WHERE "
									+ "profil_id = "
									+ "(SELECT id FROM profiluri WHERE  persoana_username ="
									+ "'"
									+ profil
									+ "') )"
									+ "UNION"
									+ "(SELECT id,textul,autor,cuPoza,data_postari,"
									+ "(SELECT persoana_username FROM profiluri p1 WHERE p1.id=c1.profil_id) profil,vizibilitate,profil_id"
									+ "  FROM comenturi c1 WHERE profil_id IN"
									+ "(SELECT id FROM profiluri WHERE persoana_username IN ("
									+ "SELECT prieten FROM liste_prieteni WHERE username='"
									+ profil
									+ "' AND acceptat='DA')))) c "
									+ ""
									+ " WHERE "
									// am introdus si vizibilitatea comenturilor
									// vizibilitatea poate sa
									// fie:eu,prieteni,toti
									// aceste optiuni sunt doar pentru cel carui
									// ii apartine contul
									// implicit restul au vizibilitatea toti
									// daca este 'eu' doar cel carui ii apartine
									// profilul
									// ii sunt vizibile posturile
									// daca este'prieteni' doar celui carui ii
									// apartine profilul si prietenii lui
									// pot vedea posturile
									// daca este 'toti' oricine poate vedea
									// posturile
									+ "(vizibilitate='toti' OR(vizibilitate='eu' AND "
									+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
									+ profil
									+ "')"
									+ "OR(vizibilitate='prieteni' AND( '"
									+ profil
									+ "' IN("
									// profil de la egalitatea de mai jos este
									// alias pentru numele profilului
									// pe care se alfa comentul
									+ "SELECT prieten FROM liste_prieteni WHERE username=profil"
									+ " AND acceptat='DA' ) OR "
									+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
									+ profil
									+ "'"
									+ ")))"
									// pana aici este despre vizibilitatea
									// postului raportata cu cine il aceseaza
									+ "ORDER BY TO_CHAR(c.data_postari,'yyyy-mm-dd:HH24:mi:ss') DESC");

					/*
					 * rs = stmt
					 * .executeQuery("SELECT c.id,c.textul,c.autor,c.cuPoza," +
					 * "(SELECT p.poza_profil FROM profiluri p WHERE p.persoana_username="
					 * + "c.autor)" + " FROM comenturi c WHERE" +
					 * " profil_id = " +
					 * " (SELECT id FROM profiluri WHERE persoana_username = " +
					 * " '" + profil + "')" +
					 * "ORDER BY TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss') DESC"
					 * );
					 */
				}
				// trimit maxim 10
				int contor = 1;
				;
				Statement stmt2 = conn.createStatement();
				while (rs.next() && contor <= 10) {
					contor++;
					dos.writeInt(rs.getInt(1));
					dos.writeUTF(rs.getString(2));
					dos.writeUTF(rs.getString(3));

					// poza de profil
					int idPozaProfil = rs.getInt(5);
					dos.writeInt(idPozaProfil);
					ResultSet rs2;
					rs2 = stmt2
							.executeQuery("SELECT width,height FROM poze WHERE id="
									+ idPozaProfil);
					while (rs2.next()) {
						// width
						dos.writeInt(rs2.getInt(1));
						// height
						dos.writeInt(rs2.getInt(2));

					}

					rs2.close();
					int idPoza = rs.getInt(4);

					if (idPoza != 0) {
						dos.writeUTF("arePoza");
						dos.writeInt(idPoza);
						rs2 = stmt2
								.executeQuery("SELECT width,height FROM poze WHERE id="
										+ idPoza);

						while (rs2.next()) {
							// width
							dos.writeInt(rs2.getInt(1));
							// height
							dos.writeInt(rs2.getInt(2));

						}
						rs2.close();

					} else
						dos.writeUTF("nuArePoza");
					dos.writeUTF(rs.getString(6));
					// trimit data postarii
					dos.writeUTF(rs.getString(7));

				}
				synchronized (ods) {
					try {
						rs = stmt
								.executeQuery("SELECT id FROM profiluri WHERE persoana_username='"
										+ profil + "'");

						int idProfil = -1;
						while (rs.next()) {
							idProfil = rs.getInt(1);
						}
						// las decat ultima actualizare a pozei de profil , nu
						// le sterg pe toate
						// pt ca poate o vrea si news feed sau comenturi
						stmt.executeUpdate("DELETE FROM update_profiluri_table WHERE profil_id="
								+ idProfil
								+ " AND  TO_CHAR(data_updatarii,'yyyy-mm-dd:HH24:mi:ss')<"
								+ "(SELECT MAX(TO_CHAR(data_updatarii,'yyyy-mm-dd:HH24:mi:ss')) FROM update_profiluri_table"
								+ " WHERE profil_id=" + idProfil + ")");
						// sterg updateurile trecute pentru ca numai am ce face
						// cu ele
						stmt.executeUpdate("DELETE FROM last_update_news WHERE profil_id="
								+ idProfil);
						stmt.executeUpdate("INSERT INTO last_update_news(id,profil_id,data_updatarii) VALUES"
								+ "(secventa7.NEXTVAL,"
								+ idProfil
								+ ",sysdate)");
						stmt.executeUpdate("COMMIT");

					} catch (Exception e) {
						System.out.println(e);
						stmt.executeUpdate("ROLLBACK");
					}
				}
			}
			if (comanda.equals("startFriendRequests")) {
				System.out.println("startFriendRequests");
				String profil = dis.readUTF();
				synchronized (ods) {
					rs = stmt
							.executeQuery("SELECT COUNT(*) FROM liste_prieteni WHERE prieten="
									+ "'" + profil + "' AND acceptat='PENDING'");
					// in caz ca nu am nici un rezultat de la query
					boolean trimisDimensiunea = false;
					while (rs.next()) {
						trimisDimensiunea = true;
						int nrRezultate = rs.getInt(1);
						if (nrRezultate > 20)
							dos.writeInt(20);
						else
							dos.writeInt(nrRezultate);
					}

					if (trimisDimensiunea == false) {
						dos.writeInt(0);
						conn.close();
						dis.close();
						dos.close();
						cs.close();
						return;
					}
					rs = stmt
							.executeQuery("SELECT persoana_username,poza_profil FROM profiluri WHERE persoana_username IN ("
									+ "SELECT username FROM liste_prieteni WHERE prieten="
									+ "'"
									+ profil
									+ "' AND acceptat='PENDING')"
									+ "ORDER BY persoana_username ASC");
				}
				int nrFriendRequests = 0;
				Statement stmt2 = conn.createStatement();
				// trimit maxim 20 de recomandari de prieteni odata
				while (rs.next() && nrFriendRequests < 20) {
					nrFriendRequests++;
					dos.writeUTF(rs.getString(1));
					// poza de profil
					int idPozaProfil = rs.getInt(2);
					dos.writeInt(idPozaProfil);
					ResultSet rs2;
					rs2 = stmt2
							.executeQuery("SELECT width,height FROM poze WHERE id="
									+ idPozaProfil);
					while (rs2.next()) {
						dos.writeInt(rs2.getInt(1));
						dos.writeInt(rs2.getInt(2));

					}

					rs2.close();
				}
			}
			if (comanda.equals("acceptFriendRequest")) {
				System.out.println("acceptFriendRequest");
				String cont = dis.readUTF();
				String strain = dis.readUTF();
				synchronized (ods) {
					rs = stmt
							.executeQuery("Select '1' FROM liste_prieteni WHERE username="
									+ "'"
									+ strain
									+ "' AND prieten='"
									+ cont
									+ "' " + " AND acceptat='PENDING'");
					boolean cerereaExista = false;
					while (rs.next()) {
						cerereaExista = true;
						rs.getString(1);
					}
					dos.writeBoolean(cerereaExista);
					if (cerereaExista == false) {
						conn.close();
						dis.close();
						dos.close();
						cs.close();
						return;
					}

					try {
						stmt.executeUpdate("UPDATE liste_prieteni"
								+ " SET acceptat='DA' " + "WHERE username= "
								+ "'" + strain + "' AND prieten='" + cont
								+ "' " + " AND acceptat='PENDING'");
						stmt.executeUpdate("UPDATE liste_prieteni"
								+ " SET acceptat='DA' " + "WHERE username= "
								+ "'" + cont + "' AND prieten='" + strain
								+ "' " + " AND acceptat='NU'");
						stmt.executeUpdate("COMMIT");
					} catch (Exception e) {
						stmt.executeUpdate("ROLLBACK");
						System.out.println(e);
					}

				}
			}
			if (comanda.equals("declineFriendRequest")) {
				System.out.println("declineFriendRequest");
				String cont = dis.readUTF();
				String strain = dis.readUTF();
				rs = stmt
						.executeQuery("Select '1' FROM liste_prieteni WHERE username="
								+ "'"
								+ strain
								+ "' AND prieten='"
								+ cont
								+ "' " + " AND acceptat='PENDING'");
				boolean cerereaExista = false;
				while (rs.next()) {
					cerereaExista = true;
					rs.getString(1);

				}
				dos.writeBoolean(cerereaExista);
				if (cerereaExista == false) {
					conn.close();
					dis.close();
					dos.close();
					cs.close();
					return;
				}
				synchronized (ods) {
					try {
						stmt.executeUpdate("DELETE FROM liste_prieteni "
								+ " WHERE username= " + "'" + strain
								+ "' AND prieten='" + cont + "' "
								+ " AND acceptat='PENDING'");
						stmt.executeUpdate("DELETE FROM liste_prieteni "
								+ " WHERE username= " + "'" + cont
								+ "' AND prieten='" + strain + "' "
								+ " AND acceptat='NU'");
						stmt.executeUpdate("COMMIT");
					} catch (Exception e) {
						stmt.executeUpdate("ROLLBACK");
						System.out.println(e);
					}

				}
			}
			if (comanda.equals("updatePaginaProfil")) {

				System.out.println("updatePaginaProfil");
				String profil = dis.readUTF();
				String cont = dis.readUTF();
				rs = stmt
						.executeQuery("SELECT puncte_prieten_temporar FROM profiluri WHERE persoana_username='"
								+ profil + "'");
				int puncte = -1;
				while (rs.next()) {
					puncte = rs.getInt(1);
				}
				// trimite cate puncte are persoana cu acest profil
				dos.writeInt(puncte);

				
				//trimit pe ce loc se afla in clasament
				rs=stmt.executeQuery("SELECT persoana,rownum FROM"
						//TREBUIE sa iau rownum dupa ce face ordonarea daca puneam rownum direct
						//in selectul de mai jos nu mergea ca imi dadea rownum inainte sa fie sortat
						+ " (SELECT persoana_username persoana FROM profiluri  "
						+ "ORDER BY puncte_prieten_temporar DESC,persoana_username ASC)");
				
				while(rs.next()){
					if(rs.getString(1).equals(profil)){
					
						//trimit locul sau in clasament calculat live
						dos.writeInt(rs.getInt(2));
					}
				}
				
				
				
				
				// trimit poza de profil a profilului curent
				rs = stmt
						.executeQuery("SELECT poza_profil FROM profiluri WHERE persoana_username='"
								+ profil + "'");
				int id = -1;
				while (rs.next()) {
					id = rs.getInt(1);
				}
				// trimit idul pozei de profil a profilului curent
				dos.writeInt(id);

				// spun daca comenturile din lista au fost sterse sau nu
				int dim = dis.readInt();
				for (int i = 0; i < dim; i++) {
					int idComent = dis.readInt();
					rs = stmt
							.executeQuery("SELECT '1' FROM comenturi WHERE id="
									+ idComent);
					boolean comentExista = false;
					while (rs.next()) {
						comentExista = true;
					}

					dos.writeBoolean(comentExista);
				}
				// updatez comenturile ramase daca sa modificat ceva

				dim = dis.readInt();
				// daca dim=0 nu se intampla nimic
				for (int i = 0; i < dim; i++) {
					int idComent = dis.readInt();
					// am un tabel in care de fiecare data cand modific pagina
					// de profil
					// scriu acolo idul pagini de profil si data cand sa produs
					// modificarea pozei de profil
					// datele sunt puse in acel tabel cu un trigger la update
					// mai am un tabel care dupa fiecare update a unui client
					// pun acolo
					// idul profilului si data la care sa produs actualizarea
					// profilului
					// asa incat pentru fiecare coment daca data ultimii
					// modificari a pozei de profil a autorului comentului
					// este mai recenta ca data ultimei actualizari a profilului
					// updatez poza de profil

					rs = stmt
							.executeQuery("SELECT '1' FROM update_profiluri_table WHERE profil_id="
									+ "(SELECT id FROM profiluri where persoana_username="
									+ " (SELECT autor FROM comenturi WHERE id= "
									+ idComent
									+ ")) AND TO_CHAR(data_updatarii,'yyyy-mm-dd:HH24:mi:ss')>"
									+ "(SELECT MAX(TO_CHAR(data_updatarii,'yyyy-mm-dd:HH24:mi:ss')) FROM last_update_profil "
									+ " WHERE profil_id=(SELECT id FROM profiluri WHERE persoana_username='"
									+ profil + "'))");

					boolean comentModificat = false;
					while (rs.next()) {
						System.out.println("updated poza profil");
						comentModificat = true;
					}

					dos.writeBoolean(comentModificat);
					if (comentModificat == true) {
						// System.out.println("aici");
						rs = stmt
								.executeQuery("SELECT poza_profil FROM profiluri  WHERE id= "
										+ "(SELECT id FROM profiluri where persoana_username="
										+ "(SELECT autor FROM comenturi WHERE id="
										+ idComent + "))");

						// poza de profil
						Statement stmt2 = conn.createStatement();
						while (rs.next()) {
							int idPozaProfil = rs.getInt(1);
							dos.writeInt(idPozaProfil);
							ResultSet rs2;
							rs2 = stmt2
									.executeQuery("SELECT width,height FROM poze WHERE id="
											+ idPozaProfil);
							while (rs2.next()) {
								// width
								dos.writeInt(rs2.getInt(1));
								// height
								dos.writeInt(rs2.getInt(2));
							}
						}

					}

				}

				// trimit comenturi noi

				boolean suntElementeInLista = dis.readBoolean();
				int primulComent = -1;
				if (suntElementeInLista == true) {
					// daca sunt comenturi pe client iau cel mai recent
					primulComent = dis.readInt();
				}
				synchronized (ods) {
					// daca sunt comenturi pe client iau restul de comenturi
					// relativ la ele
					if (suntElementeInLista == true) {
						rs = stmt
								.executeQuery("SELECT COUNT(*) FROM comenturi WHERE "
										+ "profil_id = "
										+ "(SELECT id FROM profiluri WHERE  persoana_username ="
										+ "'"
										+ profil
										+ "') "
										// am introdus si vizibilitatea
										// comenturilor
										// vizibilitatea poate sa
										// fie:eu,prieteni,toti
										// aceste optiuni sunt doar pentru cel
										// carui ii apartine contul
										// implicit restul au vizibilitatea toti
										// daca este 'eu' doar cel carui ii
										// apartine profilul
										// ii sunt vizibile posturile
										// daca este'prieteni' doar celui carui
										// ii apartine profilul si prietenii lui
										// pot vedea posturile
										// daca este 'toti' oricine poate vedea
										// posturile
										+ " AND(vizibilitate='toti' OR(vizibilitate='eu' AND "
										+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
										+ cont
										+ "')"
										+ "OR(vizibilitate='prieteni' AND( '"
										+ cont
										+ "' IN("
										+ "SELECT prieten FROM liste_prieteni WHERE username='"
										+ profil
										+ "' AND acceptat='DA' ) OR "
										+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
										+ cont
										+ "'"
										+ ")))"
										// pana aici este despre vizibilitatea
										// postului raportata cu cine il
										// aceseaza
										+ " AND TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss')>"
										+ "(SELECT TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss') FROM comenturi WHERE id="
										+ primulComent + ")");
					} else {
						// daca nu sunt comenturi le iau fara nici o conditie in
						// plus
						rs = stmt
								.executeQuery("SELECT COUNT(*) FROM comenturi WHERE "
										+ "profil_id = "
										+ "(SELECT id FROM profiluri WHERE  persoana_username ="
										+ "'"
										+ profil
										+ "') "
										// am introdus si vizibilitatea
										// comenturilor
										// vizibilitatea poate sa
										// fie:eu,prieteni,toti
										// aceste optiuni sunt doar pentru cel
										// carui ii apartine contul
										// implicit restul au vizibilitatea toti
										// daca este 'eu' doar cel carui ii
										// apartine profilul
										// ii sunt vizibile posturile
										// daca este'prieteni' doar celui carui
										// ii apartine profilul si prietenii lui
										// pot vedea posturile
										// daca este 'toti' oricine poate vedea
										// posturile
										+ " AND(vizibilitate='toti' OR(vizibilitate='eu' AND "
										+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
										+ cont
										+ "')"
										+ "OR(vizibilitate='prieteni' AND( '"
										+ cont
										+ "' IN("
										+ "SELECT prieten FROM liste_prieteni WHERE username='"
										+ profil
										+ "' AND acceptat='DA' ) OR "
										+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
										+ cont + "'" + ")))"
										// pana aici este despre vizibilitatea
										// postului raportata cu cine il
										// aceseaza
										+ "");
					}
					// daca nu exista nici un rezultat spre deosebire de
					// startPaginaProfil ce intorcea 0 ca si count aici nu
					// intoarce nimic
					int nrRezultate = 0;

					while (rs.next()) {
						nrRezultate = rs.getInt(1);

						dos.writeInt(rs.getInt(1));
					}

					if (nrRezultate > 0) {

						// aceasi poveste de mai sus
						if (suntElementeInLista == true) {
							rs = stmt
									.executeQuery("SELECT c.id,c.textul,c.autor,c.cuPoza,"
											+ "(SELECT p.poza_profil FROM profiluri p WHERE p.persoana_username="
											+ "c.autor),TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss')"
											+ " FROM comenturi c WHERE"
											+ " profil_id = "
											+ " (SELECT id FROM profiluri WHERE persoana_username = "
											+ " '"
											+ profil
											+ "') AND data_postari>(SELECT data_postari FROM comenturi WHERE id="
											+ primulComent
											+ ")"
											// am introdus si vizibilitatea
											// comenturilor
											// vizibilitatea poate sa
											// fie:eu,prieteni,toti
											// aceste optiuni sunt doar pentru
											// cel
											// carui ii apartine contul
											// implicit restul au vizibilitatea
											// toti
											// daca este 'eu' doar cel carui ii
											// apartine profilul
											// ii sunt vizibile posturile
											// daca este'prieteni' doar celui
											// carui
											// ii apartine profilul si prietenii
											// lui
											// pot vedea posturile
											// daca este 'toti' oricine poate
											// vedea
											// posturile
											+ " AND(vizibilitate='toti' OR(vizibilitate='eu' AND "
											+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
											+ cont
											+ "')"
											+ "OR(vizibilitate='prieteni' AND( '"
											+ cont
											+ "' IN("
											+ "SELECT prieten FROM liste_prieteni WHERE username='"
											+ profil
											+ "' AND acceptat='DA' ) OR "
											+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
											+ cont + "'" + ")))"
											// pana aici este despre
											// vizibilitatea
											// postului raportata cu cine il
											// aceseaza
											+ "ORDER BY TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss') ASC");
						}
						// aceasi poveste de mai sus
						else {
							rs = stmt
									.executeQuery("SELECT c.id,c.textul,c.autor,c.cuPoza,"
											+ "(SELECT p.poza_profil FROM profiluri p WHERE p.persoana_username="
											+ "c.autor),TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss')"
											+ " FROM comenturi c WHERE"
											+ " profil_id = "
											+ " (SELECT id FROM profiluri WHERE persoana_username = "
											+ " '"
											+ profil
											+ "')"
											// am introdus si vizibilitatea
											// comenturilor
											// vizibilitatea poate sa
											// fie:eu,prieteni,toti
											// aceste optiuni sunt doar pentru
											// cel
											// carui ii apartine contul
											// implicit restul au vizibilitatea
											// toti
											// daca este 'eu' doar cel carui ii
											// apartine profilul
											// ii sunt vizibile posturile
											// daca este'prieteni' doar celui
											// carui
											// ii apartine profilul si prietenii
											// lui
											// pot vedea posturile
											// daca este 'toti' oricine poate
											// vedea
											// posturile
											+ " AND(vizibilitate='toti' OR(vizibilitate='eu' AND "
											+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
											+ cont
											+ "')"
											+ "OR(vizibilitate='prieteni' AND( '"
											+ cont
											+ "' IN("
											+ "SELECT prieten FROM liste_prieteni WHERE username='"
											+ profil
											+ "' AND acceptat='DA' ) OR "
											+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
											+ cont + "'" + ")))"
											// pana aici este despre
											// vizibilitatea
											// postului raportata cu cine il
											// aceseaza
											+ "");
						}

					}

					Statement stmt2 = conn.createStatement();
					while (rs.next()) {
						System.out.println("6");
						dos.writeInt(rs.getInt(1));
						dos.writeUTF(rs.getString(2));
						dos.writeUTF(rs.getString(3));

						// poza de profil
						int idPozaProfil = rs.getInt(5);
						dos.writeInt(idPozaProfil);
						ResultSet rs2;
						rs2 = stmt2
								.executeQuery("SELECT width,height FROM poze WHERE id="
										+ idPozaProfil);
						while (rs2.next()) {
							// width
							dos.writeInt(rs2.getInt(1));
							// height
							dos.writeInt(rs2.getInt(2));

						}

						rs2.close();
						int idPoza = rs.getInt(4);

						if (idPoza != 0) {

							dos.writeUTF("arePoza");

							dos.writeInt(idPoza);
							// System.out.println("aici");
							rs2 = stmt2.executeQuery("SELECT width,height FROM"
									+ " poze WHERE id=" + idPoza);

							while (rs2.next()) {
								// width
								dos.writeInt(rs2.getInt(1));
								// height
								dos.writeInt(rs2.getInt(2));

							}
							rs2.close();

						} else
							dos.writeUTF("nuArePoza");
						// scriu timestampul poztului
						dos.writeUTF(rs.getString(6));
					}
				}
				synchronized (ods) {
					try {

						rs = stmt
								.executeQuery("SELECT id FROM profiluri WHERE persoana_username='"
										+ profil + "'");

						int idProfil = -1;
						while (rs.next()) {
							idProfil = rs.getInt(1);
						}
						// las decat ultima actualizare a pozei de profil acest
						// tabel este accesat
						// si de news feed si de profil celelalte tabele imi zic
						// daca am actualizat deja sau nu
						stmt.executeUpdate("DELETE FROM update_profiluri_table WHERE profil_id="
								+ idProfil
								+ " AND  TO_CHAR(data_updatarii,'yyyy-mm-dd:HH24:mi:ss')<"
								+ "(SELECT MAX(TO_CHAR(data_updatarii,'yyyy-mm-dd:HH24:mi:ss')) FROM update_profiluri_table"
								+ " WHERE profil_id=" + idProfil + ")");
						// sterg updatariile de pe pagina de profil de inainte
						stmt.executeUpdate("DELETE FROM last_update_profil WHERE profil_id="
								+ idProfil);
						// zic ca sa intamplat un update pe pagina de profil
						stmt.executeUpdate("INSERT INTO last_update_profil(id,profil_id,data_updatarii) VALUES"
								+ "(secventa6.NEXTVAL,"
								+ idProfil
								+ ",sysdate)");

						stmt.executeUpdate("COMMIT");

					} catch (Exception e) {
						System.out.println(e);
						stmt.executeUpdate("ROLLBACK");
					}
				}
			}
			if (comanda.equals("updatePaginaNewsFeed")) {
				System.out.println("updatePaginaNewsFeed");
				String profil = dis.readUTF();
				// spun daca comenturile din lista au fost sterse sau nu
				int dim = dis.readInt();
				for (int i = 0; i < dim; i++) {
					int idComent = dis.readInt();
					rs = stmt
							.executeQuery("SELECT '1' FROM comenturi WHERE id="
									+ idComent);
					boolean comentExista = false;
					while (rs.next()) {
						comentExista = true;
					}

					dos.writeBoolean(comentExista);
				}
				// updatez comenturile ramase daca sa modificat ceva

				dim = dis.readInt();

				// daca dim=0 nu se intampla nimic
				for (int i = 0; i < dim; i++) {
					int idComent = dis.readInt();
					// am un tabel in care de fiecare data cand modific pagina
					// de profil
					// scriu acolo idul pagini de profil si data cand sa produs
					// modificarea pozei de profil
					// datele sunt puse in acel tabel cu un trigger la update
					// mai am un tabel care dupa fiecare update a unui client
					// pun acolo
					// idul profilului si data la care sa produs actualizarea
					// profilului
					// asa incat pentru fiecare coment daca data ultimii
					// modificari a pozei de profil a autorului comentului
					// este mai recenta ca data ultimei actualizari a profilului
					// updatez poza de profil

					rs = stmt
							.executeQuery("SELECT '1' FROM update_profiluri_table WHERE profil_id="
									+ "(SELECT id FROM profiluri where persoana_username="
									+ " (SELECT autor FROM comenturi WHERE id= "
									+ idComent
									+ ")) AND  TO_CHAR(data_updatarii,'yyyy-mm-dd:HH24:mi:ss')>"
									+ "(SELECT MAX(TO_CHAR(data_updatarii,'yyyy-mm-dd:HH24:mi:ss')) FROM last_update_news "
									+ " WHERE profil_id=(SELECT id FROM profiluri WHERE persoana_username='"
									+ profil + "'))");

					boolean comentModificat = false;
					while (rs.next()) {
						System.out.println("updated poza profil");
						comentModificat = true;
					}

					dos.writeBoolean(comentModificat);
					if (comentModificat == true) {

						rs = stmt
								.executeQuery("SELECT poza_profil FROM profiluri  WHERE id= "
										+ "(SELECT id FROM profiluri where persoana_username="
										+ "(SELECT autor FROM comenturi WHERE id="
										+ idComent + "))");

						// poza de profil
						Statement stmt2 = conn.createStatement();
						while (rs.next()) {
							int idPozaProfil = rs.getInt(1);
							dos.writeInt(idPozaProfil);
							ResultSet rs2;
							rs2 = stmt2
									.executeQuery("SELECT width,height FROM poze WHERE id="
											+ idPozaProfil);
							while (rs2.next()) {
								// width
								dos.writeInt(rs2.getInt(1));
								// height
								dos.writeInt(rs2.getInt(2));

							}
						}

					}

				}

				// trimit comenturi noi

				boolean suntElementeInLista = dis.readBoolean();
				int primulComent = -1;

				if (suntElementeInLista == true) {
					// daca sunt comenturi pe client iau cel mai recent
					primulComent = dis.readInt();
				}

				synchronized (ods) {
					// daca sunt comenturi pe client iau restul de comenturi
					// relativ la ele

					if (suntElementeInLista == true) {
						rs = stmt
								.executeQuery("SELECT COUNT(*) FROM (("
										+ " SELECT id,textul,autor,cuPoza,data_postari data_postari,"
										+ "'"
										+ profil
										+ "' profil,vizibilitate,profil_id"
										+ " FROM "
										+ "comenturi WHERE "
										+ "profil_id = "
										+ "(SELECT id FROM profiluri WHERE  persoana_username ="
										+ "'"
										+ profil
										+ "') )"
										+ "UNION"
										+ "(SELECT id,textul,autor,cuPoza,data_postari data_postari,"
										+ "(SELECT persoana_username FROM profiluri p1 WHERE p1.id=c1.profil_id) profil,vizibilitate,profil_id"
										+ "  FROM comenturi c1 WHERE profil_id IN"
										+ "(SELECT id FROM profiluri WHERE persoana_username IN ("
										+ "SELECT prieten FROM liste_prieteni WHERE username='"
										+ profil
										+ "' AND acceptat='DA'))))"
										+ " WHERE TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss')>"
										+ "(SELECT TO_CHAR(cc.data_postari,'yyyy-mm-dd:HH24:mi:ss') FROM comenturi cc WHERE cc.id="
										+ primulComent
										+ ")"

										// am introdus si vizibilitatea
										// comenturilor
										// vizibilitatea poate sa
										// fie:eu,prieteni,toti
										// aceste optiuni sunt doar pentru cel
										// carui ii apartine contul
										// implicit restul au vizibilitatea toti
										// daca este 'eu' doar cel carui ii
										// apartine profilul
										// ii sunt vizibile posturile
										// daca este'prieteni' doar celui carui
										// ii apartine profilul si prietenii lui
										// pot vedea posturile
										// daca este 'toti' oricine poate vedea
										// posturile
										+ " AND (vizibilitate='toti' OR(vizibilitate='eu' AND "
										+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
										+ profil
										+ "')"
										+ "OR(vizibilitate='prieteni' AND( '"
										+ profil
										+ "' IN("
										// profil de la egalitatea de mai jos
										// este alias pentru numele profilului
										// pe care se alfa comentul
										+ "SELECT prieten FROM liste_prieteni WHERE username=profil"
										+ " AND acceptat='DA' ) OR "
										+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
										+ profil + "'" + ")))"
										// pana aici este despre vizibilitatea
										// postului raportata cu cine il
										// aceseaza

										+ "");
					} else {
						// daca nu sunt comenturi le iau fara nici o conditie in
						// plus

						rs = stmt
								.executeQuery("SELECT COUNT(*) FROM (("
										+ " SELECT id,textul,autor,cuPoza,data_postari data_postari,"
										+ "'"
										+ profil
										+ "' profil,vizibilitate,profil_id"
										+ " FROM "
										+ "comenturi WHERE "
										+ "profil_id = "
										+ "(SELECT id FROM profiluri WHERE  persoana_username ="
										+ "'"
										+ profil
										+ "') )"
										+ "UNION"
										+ "(SELECT id,textul,autor,cuPoza,data_postari,"
										+ "(SELECT persoana_username FROM profiluri p1 WHERE p1.id=c1.profil_id) profil,vizibilitate,profil_id"
										+ "  FROM comenturi c1 WHERE profil_id IN"
										+ "(SELECT id FROM profiluri WHERE persoana_username IN ("
										+ "SELECT prieten FROM liste_prieteni WHERE username='"
										+ profil
										+ "' AND acceptat='DA'))))"

										// am introdus si vizibilitatea
										// comenturilor
										// vizibilitatea poate sa
										// fie:eu,prieteni,toti
										// aceste optiuni sunt doar pentru cel
										// carui ii apartine contul
										// implicit restul au vizibilitatea toti
										// daca este 'eu' doar cel carui ii
										// apartine profilul
										// ii sunt vizibile posturile
										// daca este'prieteni' doar celui carui
										// ii apartine profilul si prietenii lui
										// pot vedea posturile
										// daca este 'toti' oricine poate vedea
										// posturile
										+ " WHERE (vizibilitate='toti' OR(vizibilitate='eu' AND "
										+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
										+ profil
										+ "')"
										+ "OR(vizibilitate='prieteni' AND( '"
										+ profil
										+ "' IN("
										// profil de la egalitatea de mai jos
										// este alias pentru numele profilului
										// pe care se alfa comentul
										+ "SELECT prieten FROM liste_prieteni WHERE username=profil"
										+ " AND acceptat='DA' ) OR "
										+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
										+ profil + "'" + ")))"
										// pana aici este despre vizibilitatea
										// postului raportata cu cine il
										// aceseaza

										+ "");
					}

					// daca nu exista nici un rezultat spre deosebire de
					// startPaginaProfil ce intorcea 0 ca si count aici nu
					// intoarce nimic
					boolean existaRezultate = false;
					while (rs.next()) {

						if (rs.getInt(1) != 0) {
							existaRezultate = true;
							dos.writeInt(rs.getInt(1));
						}
					}
					if (existaRezultate == false) {
						dos.writeInt(0);
					}
					if (existaRezultate == true) {

						// aceasi poveste de mai sus
						if (suntElementeInLista == true) {

							rs = stmt
									.executeQuery("SELECT c.id,c.textul,c.autor,c.cuPoza,"
											+ "(SELECT p.poza_profil FROM profiluri p WHERE p.persoana_username="
											+ "c.autor),c.profil,TO_CHAR(c.data_postari,'yyyy-mm-dd:HH24:mi:ss')"
											+ " FROM (("
											+ " SELECT id,textul,autor,cuPoza,data_postari data_postari,"
											+ "'"
											+ profil
											+ "' profil,vizibilitate,profil_id"
											+ " FROM "
											+ "comenturi WHERE "
											+ "profil_id = "
											+ "(SELECT id FROM profiluri WHERE  persoana_username ="
											+ "'"
											+ profil
											+ "') )"
											+ "UNION"
											+ "(SELECT id,textul,autor,cuPoza,data_postari data_postari,"
											+ "(SELECT persoana_username FROM profiluri p1 WHERE p1.id=c1.profil_id) profil,vizibilitate,profil_id"
											+ "  FROM comenturi c1 WHERE profil_id IN"
											+ "(SELECT id FROM profiluri WHERE persoana_username IN ("
											+ "SELECT prieten FROM liste_prieteni WHERE username='"
											+ profil
											+ "' AND acceptat='DA')))) c "
											+ " WHERE TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss')>"
											+ "(SELECT TO_CHAR(cc.data_postari,'yyyy-mm-dd:HH24:mi:ss') FROM comenturi cc WHERE cc.id="
											+ primulComent
											+ ")"

											// am introdus si vizibilitatea
											// comenturilor
											// vizibilitatea poate sa
											// fie:eu,prieteni,toti
											// aceste optiuni sunt doar pentru
											// cel
											// carui ii apartine contul
											// implicit restul au vizibilitatea
											// toti
											// daca este 'eu' doar cel carui ii
											// apartine profilul
											// ii sunt vizibile posturile
											// daca este'prieteni' doar celui
											// carui
											// ii apartine profilul si prietenii
											// lui
											// pot vedea posturile
											// daca este 'toti' oricine poate
											// vedea
											// posturile
											+ " AND (vizibilitate='toti' OR(vizibilitate='eu' AND "
											+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
											+ profil
											+ "')"
											+ "OR(vizibilitate='prieteni' AND( '"
											+ profil
											+ "' IN("
											// profil de la egalitatea de mai
											// jos
											// este alias pentru numele
											// profilului
											// pe care se alfa comentul
											+ "SELECT prieten FROM liste_prieteni WHERE username=profil"
											+ " AND acceptat='DA' ) OR "
											+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
											+ profil + "'" + ")))"
											// pana aici este despre
											// vizibilitatea
											// postului raportata cu cine il
											// aceseaza
											+ " ORDER BY TO_CHAR(c.data_postari,'yyyy-mm-dd:HH24:mi:ss') ASC");
						}
						// aceasi poveste de mai sus
						else {

							rs = stmt
									.executeQuery("SELECT c.id,c.textul,c.autor,c.cuPoza,"
											+ "(SELECT p.poza_profil FROM profiluri p WHERE p.persoana_username="
											+ "c.autor),c.profil,TO_CHAR(c.data_postari,'yyyy-mm-dd:HH24:mi:ss')"
											+ " FROM (("
											+ " SELECT id,textul,autor,cuPoza,data_postari data_postari,"
											+ "'"
											+ profil
											+ "'profil,vizibilitate,profil_id"
											+ " FROM "
											+ "comenturi WHERE "
											+ "profil_id = "
											+ "(SELECT id FROM profiluri WHERE  persoana_username ="
											+ "'"
											+ profil
											+ "') )"
											+ "UNION"
											+ "(SELECT id,textul,autor,cuPoza,data_postari data_postari,"
											+ "(SELECT persoana_username FROM profiluri p1 WHERE p1.id=c1.profil_id) profil,vizibilitate,profil_id"
											+ "  FROM comenturi c1 WHERE profil_id IN"
											+ "(SELECT id FROM profiluri WHERE persoana_username IN ("
											+ "SELECT prieten FROM liste_prieteni WHERE username='"
											+ profil
											+ "' AND acceptat='DA')))) c "

											// am introdus si vizibilitatea
											// comenturilor
											// vizibilitatea poate sa
											// fie:eu,prieteni,toti
											// aceste optiuni sunt doar pentru
											// cel
											// carui ii apartine contul
											// implicit restul au vizibilitatea
											// toti
											// daca este 'eu' doar cel carui ii
											// apartine profilul
											// ii sunt vizibile posturile
											// daca este'prieteni' doar celui
											// carui
											// ii apartine profilul si prietenii
											// lui
											// pot vedea posturile
											// daca este 'toti' oricine poate
											// vedea
											// posturile
											+ " WHERE (vizibilitate='toti' OR(vizibilitate='eu' AND "
											+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
											+ profil
											+ "')"
											+ "OR(vizibilitate='prieteni' AND( '"
											+ profil
											+ "' IN("
											// profil de la egalitatea de mai
											// jos
											// este alias pentru numele
											// profilului
											// pe care se alfa comentul
											+ "SELECT prieten FROM liste_prieteni WHERE username=profil"
											+ " AND acceptat='DA' ) OR "
											+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
											+ profil + "'" + ")))"
											// pana aici este despre
											// vizibilitatea
											// postului raportata cu cine il
											// aceseaza
											+ " ORDER BY TO_CHAR(c.data_postari,'yyyy-mm-dd:HH24:mi:ss') ASC");
						}

						Statement stmt2 = conn.createStatement();
						while (rs.next()) {

							dos.writeInt(rs.getInt(1));
							dos.writeUTF(rs.getString(2));
							dos.writeUTF(rs.getString(3));

							// poza de profil
							int idPozaProfil = rs.getInt(5);
							dos.writeInt(idPozaProfil);
							ResultSet rs2;
							rs2 = stmt2
									.executeQuery("SELECT width,height FROM poze WHERE id="
											+ idPozaProfil);
							while (rs2.next()) {
								// width
								dos.writeInt(rs2.getInt(1));
								// height
								dos.writeInt(rs2.getInt(2));

							}

							rs2.close();
							int idPoza = rs.getInt(4);

							if (idPoza != 0) {
								dos.writeUTF("arePoza");
								dos.writeInt(idPoza);
								rs2 = stmt2
										.executeQuery("SELECT width,height FROM poze WHERE id="
												+ idPoza);

								while (rs2.next()) {
									// width
									dos.writeInt(rs2.getInt(1));
									// height
									dos.writeInt(rs2.getInt(2));
								}
								rs2.close();

							} else
								dos.writeUTF("nuArePoza");
							dos.writeUTF(rs.getString(6));

							// trimite data postului
							dos.writeUTF(rs.getString(7));
						}

					}

					try {
						rs = stmt
								.executeQuery("SELECT id FROM profiluri WHERE persoana_username='"
										+ profil + "'");

						int idProfil = -1;
						while (rs.next()) {
							idProfil = rs.getInt(1);
						}
						// las decat ultima actualizare a pozei de profil , nu
						// le sterg pe toate
						// pt ca poate o vrea si news feed sau comenturi
						stmt.executeUpdate("DELETE FROM update_profiluri_table WHERE profil_id="
								+ idProfil
								+ " AND  TO_CHAR(data_updatarii,'yyyy-mm-dd:HH24:mi:ss')<"
								+ "(SELECT MAX(TO_CHAR(data_updatarii,'yyyy-mm-dd:HH24:mi:ss')) FROM update_profiluri_table"
								+ " WHERE profil_id=" + idProfil + ")");
						// sterg updateurile trecute pentru ca numai am ce face
						// cu ele
						stmt.executeUpdate("DELETE FROM last_update_news WHERE profil_id="
								+ idProfil);
						// zic ca am un update pe pagian de news feed
						stmt.executeUpdate("INSERT INTO last_update_news(id,profil_id,data_updatarii) VALUES"
								+ "(secventa7.NEXTVAL,"
								+ idProfil
								+ ",sysdate)");

						stmt.executeUpdate("COMMIT");

					} catch (Exception e) {
						System.out.println(e);
						stmt.executeUpdate("ROLLBACK");
					}
				}
			}

			if (comanda.equals("startFriendRecommendation")) {

				class prieteni {
					String prieten;
					int prieteniInComun;

					prieteni(String prieten, int prieteniInComun) {
						this.prieten = prieten;
						this.prieteniInComun = prieteniInComun;
					}

				}
				;
				System.out.println("startFriendRecommendation");
				String profil = dis.readUTF();
				ArrayList<String> friendRecommandationsTemp = new ArrayList<>();
				ArrayList<prieteni> friendRecommandations = new ArrayList<>();
				rs = stmt
						.executeQuery(" SELECT prieten FROM liste_prieteni WHERE acceptat='DA' "
								+ "AND username IN( SELECT prieten FROM"
								+ " liste_prieteni WHERE acceptat='DA' AND  username='"
								+ profil
								+ "')"
								+ " AND prieten<>'"
								+ profil
								+ "' AND prieten NOT IN("
								+ " SELECT prieten FROM listE_prieteni "
								+ " WHERE username='"
								+ profil
								+ "') "
								// verific daca utilizatorul a zis ca nu vrea ca
								// recomandare acest prieten
								// daca a zis ca nu il vrea va aparea in acest
								// tabel
								+ "AND NOT EXISTS(SELECT '1' FROM renunta_recomandari_prieteni "
								+ " WHERE persoana_username='"
								+ profil
								+ "' AND recomandare=prieten" + ")" + "");
				while (rs.next()) {
					friendRecommandationsTemp.add(rs.getString(1));
				}

				while (!friendRecommandationsTemp.isEmpty()) {
					int count = 1;
					// iau prima optiune si vad de cate ori apare in vector
					// de fiecare data cand apare in vector inseamna ca aveti un
					// prieten
					// in comun in plus
					String prieten = friendRecommandationsTemp.get(0);
					for (int i = 1; i < friendRecommandationsTemp.size(); i++) {
						if (prieten.equals(friendRecommandationsTemp.get(i))) {
							count++;
						}
					}
					// daca aveti cel putin 3 prieteni in comun atunci el apare
					// de cel putin
					// 3 ori in vector si il recomand ca prieten
					if (count >= 3) {
						friendRecommandations.add(new prieteni(prieten, count));
					}
					// elimin toate aparitiile numele ale prietenului selectat
					for (int i = 0; i < friendRecommandationsTemp.size(); i++) {
						if (prieten.equals(friendRecommandationsTemp.get(i))) {
							friendRecommandationsTemp.remove(i);
							i--;
						}
					}
				}
				// ii sortez descrescator dupa numarul de prieteni in comun
				Collections.sort(friendRecommandations,
						new Comparator<prieteni>() {
							public int compare(prieteni s1, prieteni s2) {
								return s2.prieteniInComun - s1.prieteniInComun;
							}
						});
				// trimit maxim 10 recomandari de prieteni
				if (friendRecommandations.size() > 10)
					dos.writeInt(10);
				else
					// trimit cati prieteni in comun aveti
					dos.writeInt(friendRecommandations.size());

				Statement stmt2 = conn.createStatement();
				Statement stmt3 = conn.createStatement();
				for (int i = 0; i < friendRecommandations.size() && i < 10; i++) {
					ResultSet rs2, rs3;
					String username = friendRecommandations.get(i).prieten;
					int nrPrieteniInComun = friendRecommandations.get(i).prieteniInComun;
					// trimit numele recomandarii
					dos.writeUTF(username);
					// trimit numarul de prieteni in comun
					dos.writeInt(nrPrieteniInComun);
					rs3 = stmt3
							.executeQuery("SELECT poza_profil FROM profiluri WHERE persoana_username='"
									+ username + "'");
					while (rs3.next()) {

						// poza de profil
						int idPozaProfil = rs3.getInt(1);
						dos.writeInt(idPozaProfil);

						rs2 = stmt2
								.executeQuery("SELECT width,height FROM poze WHERE id="
										+ idPozaProfil);
						while (rs2.next()) {
							dos.writeInt(rs2.getInt(1));
							dos.writeInt(rs2.getInt(2));

						}

						rs2.close();
					}
					rs3.close();
				}

			}
			if (comanda.equals("addFriend")) {
				System.out.println("addFriend");
				String cont = dis.readUTF();
				String profil = dis.readUTF();
				synchronized (ods) {
					try {

						// sa fiu sigur ca nu exista deja un rand in tabelul
						// liste_prieteni intre cei 2
						// posibil sa apara daca apasa simultan add friend
						rs = stmt
								.executeQuery("SELECT '1' FROM liste_prieteni WHERE username='"
										+ cont
										+ "' AND prieten='"
										+ profil
										+ "'");

						boolean dejaExista = false;
						while (rs.next()) {
							dejaExista = true;
							System.out.println("da");

						}
						if (dejaExista == false) {

							stmt.executeUpdate("INSERT INTO liste_prieteni(username,prieten,acceptat) VALUES"
									+ "('"
									+ cont
									+ "','"
									+ profil
									+ "','PENDING')");
							stmt.executeUpdate("INSERT INTO liste_prieteni(username,prieten,acceptat) VALUES"
									+ "('" + profil + "','" + cont + "','NU')");
							stmt.executeUpdate("COMMIT");
						}
					} catch (Exception e) {
						stmt.executeUpdate("ROLLBACK");
						System.out.println(e);
					}
				}
			}
			if (comanda.equals("startPaginaMesaje")) {
				System.out.println("startPaginaMesaje");
				String cont = dis.readUTF();
				String partenerConversatie = dis.readUTF();
				synchronized (ods) {
					rs = stmt.executeQuery("SELECT COUNT(*) FROM mesaje WHERE "
							+ "(username_sender='" + cont
							+ "' AND username_receiver='" + partenerConversatie
							+ "') OR  " + "(username_sender='"
							+ partenerConversatie + "' AND username_receiver='"
							+ cont + "')");
					boolean existaMesaje = false;
					while (rs.next()) {
						existaMesaje = true;
						if (rs.getInt(1) > 30)
							dos.writeInt(30);
						else
							dos.writeInt(rs.getInt(1));
					}
					rs = stmt
							.executeQuery("SELECT username_sender,mesaj"
									+ ",id,TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss')"
									+ " FROM mesaje WHERE "
									+ "(username_sender='"
									+ cont
									+ "' AND username_receiver='"
									+ partenerConversatie
									+ "') OR  "
									+ "(username_sender='"
									+ partenerConversatie
									+ "' AND username_receiver='"
									+ cont
									+ "')"
									+ "ORDER BY TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss') DESC");
					int nrRezultate = 0;
					while (rs.next() && nrRezultate < 30) {
						nrRezultate++;
						// id mesaj
						dos.writeInt(rs.getInt(3));
						// nume sender
						dos.writeUTF(rs.getString(1));

						// mesajul
						dos.writeUTF(rs.getString(2));
						// data mesajului
						dos.writeUTF(rs.getString(4));

					}
				}
			}
			if (comanda.equals("addMessagePaginaMesaje")) {
				System.out.println("addMessagePaginaMesaje");
				String cont = dis.readUTF();
				String partenerConversatie = dis.readUTF();
				String mesaj = dis.readUTF();
				synchronized (ods) {
					try {
						stmt.executeUpdate("INSERT INTO mesaje(id,username_sender,username_receiver,mesaj,data_mesaj)"
								+ " VALUES(secventa8.NEXTVAL,'"
								+ cont
								+ "','"
								+ partenerConversatie
								+ "','"
								+ mesaj
								+ "',sysdate)");
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
						stmt.executeUpdate("UPDATE prieteni_temporari SET a_trimis_mesaj='DA' WHERE "
								+ " persoana_username='"
								+ cont
								+ "' AND prieten='" + partenerConversatie + "'");
						stmt.executeUpdate("UPDATE prieteni_temporari SET a_primit_mesaj='DA' WHERE "
								+ " persoana_username='"
								+ partenerConversatie
								+ "' AND prieten='" + cont + "'");
						
						//sa imi dau seama ca a trimis mesaje noi 
					
						stmt.executeUpdate("DELETE FROM mesaje_noi WHERE username_sender='"
								+cont
								+ "' AND username_receiver='"
							+ partenerConversatie
								+ "'");
						stmt.executeUpdate("INSERT INTO mesaje_noi(username_sender,username_receiver) VALUES('"
								+cont
								+ "','"
							+ partenerConversatie
								+ "')");
						stmt.executeUpdate("COMMIT");
					} catch (Exception e) {
						System.out.println(e);
						stmt.executeUpdate("ROLLBACK");
					}
				}
			}
			if (comanda.equals("listaInterese")) {
				System.out.println("listaInterese");
				String profil = dis.readUTF();
				int dim = 0;
				synchronized (ods) {
					rs = stmt
							.executeQuery("SELECT COUNT(*)  FROM interese_persoane "
									+ " WHERE persoana_username="
									+ "'"
									+ profil + "'");

					while (rs.next()) {
						dim = rs.getInt(1);
					}
					dos.writeInt(dim);
					rs = stmt
							.executeQuery("SELECT interes,rating FROM interese_persoane "
									+ " WHERE persoana_username="
									+ "'"
									+ profil + "'");

					while (rs.next()) {

						// trimit interesul
						dos.writeUTF(rs.getString(1));
						// valoarea de rating
						dos.writeInt(rs.getInt(2));

					}
				}
			}
			if (comanda.equals("addInteres")) {
				System.out.println("addInteres");
				String profil = dis.readUTF();
				String interes = dis.readUTF();
				synchronized (ods) {
					try {
						stmt.executeUpdate("INSERT INTO interese_persoane(id,persoana_username,interes,data_adaugarii,rating) VALUES("
								+ "secventa9.NEXTVAL,'"
								+ profil
								+ "','"
								+ interes + "',sysdate,1)");
						stmt.executeUpdate("COMMIT");
					} catch (Exception e) {
						stmt.executeUpdate("ROLLBACK");
						System.out.println(e);
					}
				}
			}
			if (comanda.equals("stergeInteres")) {
				System.out.println("stergeInteres");
				String profil = dis.readUTF();
				String interes = dis.readUTF();
				synchronized (ods) {
					try {
						stmt.executeUpdate("DELETE FROM interese_persoane WHERE persoana_username='"
								+ profil + "' AND interes='" + interes + "'");
						stmt.executeUpdate("COMMIT");
					} catch (Exception e) {
						stmt.executeUpdate("ROLLBACK");
						System.out.println(e);
					}
				}
			}

			if (comanda.equals("createPrieteniTemporari")) {
				System.out.println("createPrieteniTemporari");
				ArrayList<String> toatePersoanele = new ArrayList<>();
				ArrayList<String> persoaneFaraPrieteniTemporari = new ArrayList<>();
				synchronized (ods) {
					try {
						// bag datele anterioare in arhiva
						stmt.executeUpdate("INSERT into arhiva_prieteni_temporari(id,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,data_finalizarii)"
								+ "SELECT secventa11.NEXTVAL,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,sysdate FROM prieteni_temporari");
						// adaug punctele daca cei 2 prieteni temporari au
						// vorbit
						stmt.executeUpdate("UPDATE profiluri p SET p.puncte_prieten_temporar=p.puncte_prieten_temporar+5 WHERE "
								+ " EXISTS("
								+ "SELECT '1' FROM prieteni_temporari pt WHERE pt.persoana_username=p.persoana_username AND "
								+ "a_trimis_mesaj='DA' AND a_primit_mesaj='DA')");

						// golesc tabelelul de prieteni temporari anteriori
						stmt.executeUpdate("TRUNCATE TABLE prieteni_temporari");
						stmt.executeUpdate("COMMIT");

					} catch (Exception e) {
						stmt.executeUpdate("ROLLBACK");
						System.out.println(e);
					}
				}

				// selectezi toti utilizatorii
				rs = stmt.executeQuery("SELECT username FROM persoane");
				while (rs.next()) {
					toatePersoanele.add(rs.getString(1));
				}
				// cat timp nu am incercat sa gasesc prieteni temporari tuturor
				// utilizatorilor
				// selectati nu ma opresc.Cand gasesc pe 'a' sa fie prieten cu
				// 'b' si 'b' este prieten cu 'a'
				// asa ca il scot pe 'b' din vectorul de useri ce le trebuie
				// prieteni
				while (!toatePersoanele.isEmpty()) {
					System.out.println("Cautam prieten pentru "
							+ toatePersoanele.get(0));
					// cautam persoane ce au interese comune posibil ca o
					// persoana
					// sa fie de mai multe ori in lista daca are mai multe
					// interese
					// crecandui sansa de a fii ales prieten
					// Nu lasam persoane sa fie prieten iar cu persoane ce a
					// fost
					// prieten temporar in trecut
					rs = stmt
							.executeQuery("SELECT persoana_username,interes,rating FROM interese_persoane  WHERE persoana_username<>'"
									+ toatePersoanele.get(0)
									+ "' AND("
									+ " persoana_username NOT IN("
									+ "SELECT a.prieten FROM arhiva_prieteni_temporari a WHERE a.persoana_username='"
									// AM NOT IN SAU NOT EXISTS PENTRU 2 CAZURI
									// CAZUL 1 exista persoane in istoric atunci
									// not in va merge si va cauta
									// CAZUL 2 nu exista persoane in istoric
									// atunci not in va da null si not exists
									// va da true
									+ toatePersoanele.get(0)
									+ "') OR NOT EXISTS "
									+ "(SELECT a.prieten FROM arhiva_prieteni_temporari a WHERE a.persoana_username='"
									+ toatePersoanele.get(0)
									+ "'))"
									// sa nu fie deja prieten cu el
									+ " AND persoana_username NOT IN("
									+ "SELECT prieten FROM liste_prieteni WHERE username='"
									+ toatePersoanele.get(0)
									+ "' AND acceptat='DA' "
									+ ")"
									+ " AND UPPER(interes) IN(SELECT UPPER(interes) FROM interese_persoane WHERE persoana_username='"
									+ toatePersoanele.get(0) + "')");
					boolean existaCandidati = false;
					ArrayList<String> listaCandidati = new ArrayList<>();
					ResultSet rs2;
					Statement stmt2 = conn.createStatement();
					while (rs.next()) {
						String interes = rs.getString(2);
						// fiecare utilizator da un rating unui interes ce poate
						// fii de la 1 la 3
						// daca ii place foarte mult acel interes ii da 3 daca
						// ii place mai putin ii da 1
						// daca gasesc 2 persoane ce au dat 3 puncte aceluiasi
						// interes vreau sa le dau sansa
						// mai mare sa fie prieteni temporari deci multiplic
						// ratingurile lor si il bag in lista
						// cu posibil candidati de atatea ori.Daca au mai multe
						// interese in comun cu rating mare
						// o sa creasca sansa si mai mare.
						// Deci trebuie sa iau ratingul primului din lista
						// folosind queryul de mai jos si ratingul
						// candidatului folosind queryul mare de sus
						rs2 = stmt2
								.executeQuery("SELECT rating FROM interese_persoane "
										+ " WHERE persoana_username='"
										+ toatePersoanele.get(0)
										+ "' AND interes='" + interes + "'");
						int rating1 = -1;
						while (rs2.next()) {
							rating1 = rs2.getInt(1);
						}
						int rating2 = rs.getInt(3);
						int nr = rating1 * rating2;
						for (int i = 0; i < nr; i++)
							listaCandidati.add(rs.getString(1));
						existaCandidati = true;
					}

					// daca e false inseamna ca nu sunt persoane cu aceleasi
					// interese
					// trebuie doar 1 sa se potriveasca nu toate cu cat sunt
					// mai multe interese asemanatoare cu atat sansele lor
					// sa devina prieteni cresc pentru ca apare de mai multe ori
					// in lista de candidati
					if (existaCandidati == false) {
						System.out
								.println(toatePersoanele.get(0)
										+ " nu are prieten temporar in functie de interes incerc prieten temporar random");
						persoaneFaraPrieteniTemporari.add(toatePersoanele
								.get(0));
						toatePersoanele.remove(0);
						continue;
					}

					// vrem sa facem perechi unice de 2 persoane daca cineva are
					// ceva in comun cu o persoana dar aceea persoana este deja
					// prieten cu
					// altcineva nu vrem sa ii imperechem . Ne dam seama ca sunt
					// deja
					// prieteni temporari cu cineva daca nu se mai afla in
					// vectorul toatePersoanele
					boolean amGasitCandidatUnic = false;
					int randomCandidat = -1;
					while (amGasitCandidatUnic == false
							&& !(listaCandidati.isEmpty())) {

						randomCandidat = ((int) (Math.random() * 1000))
								% listaCandidati.size();
						boolean seAflaInVector = false;
						// verific daca exista aceasta persoana selectata ca si
						// prieten temporar
						// in lista de useri ce nu au inca prieteni temporari
						// (lista de useri de la inceput)
						for (int i = 0; i < toatePersoanele.size(); i++) {
							if (toatePersoanele.get(i).equals(
									listaCandidati.get(randomCandidat))) {
								seAflaInVector = true;
							}
						}
						if (seAflaInVector == true)
							amGasitCandidatUnic = true;
						else {

							listaCandidati.remove(randomCandidat);

						}
					}

					// daca este false inseamna ca nu iam gasit un prieten
					// temporar
					// asa ca il eliminam din lista uererilor ce trebuie sa le
					// gasim prieten temporar
					if (amGasitCandidatUnic == false) {

						System.out
								.println(toatePersoanele.get(0)
										+ " nu are prieten temporar in functie de interes incerc prieten temporar random");
						persoaneFaraPrieteniTemporari.add(toatePersoanele
								.get(0));
						toatePersoanele.remove(0);
						continue;
					} else {
						System.out.println(toatePersoanele.get(0)
								+ " este prieten cu "
								+ listaCandidati.get(randomCandidat));
					}

					String prietenTemporarAles = listaCandidati
							.get(randomCandidat);

					synchronized (ods) {
						try {
							stmt.executeUpdate("INSERT INTO prieteni_temporari(id,persoana_username,prieten,data_adaugarii) VALUES("
									+ "secventa10.NEXTVAL,'"
									+ toatePersoanele.get(0)
									+ "','"
									+ prietenTemporarAles + "',sysdate)");
							stmt.executeUpdate("INSERT INTO prieteni_temporari(id,persoana_username,prieten,data_adaugarii) VALUES("
									+ "secventa10.NEXTVAL,'"
									+ prietenTemporarAles
									+ "','"
									+ toatePersoanele.get(0) + "',sysdate)");
							stmt.executeUpdate("COMMIT");

						} catch (Exception e) {
							System.out.println(e);
							stmt.executeUpdate("ROLLBACK");
						}
						// sterg persoana pentru ca am cautat prieten temporar
						// din
						// lista de perosana ce au nevoie de un prieten temporar
						// nu sterg dupa commit pentru ca daca ar fii probleme
						// sar executa un ciclu infinit daca problema apare
						// mereu
						toatePersoanele.remove(0);
						// sterg persoana selectata ca si prieten temporar in
						// aceasta
						// runda din lista de persoane ce nu au prieten temporar
						for (int i = 0; i < toatePersoanele.size(); i++) {
							if (toatePersoanele.get(i).equals(
									prietenTemporarAles))
								toatePersoanele.remove(i);
						}
					}
				}
				// incerc sa distribui persoanele ramase prieteniTemporari alesi
				// la intamplare fara nici un criteriu

				while (!persoaneFaraPrieteniTemporari.isEmpty()) {

					// daca mai exista doar o persoana nu am cu cine sa il
					// imperechez asa ca ma opresc aici
					if (persoaneFaraPrieteniTemporari.size() == 1) {
						System.out.println(persoaneFaraPrieteniTemporari.get(0)
								+ " nu are prieten temporar");
						break;
					}

					// pentru persoana caut prieten temporar
					String persoana = persoaneFaraPrieteniTemporari.get(0);

					ArrayList<String> posibiliCandidati = new ArrayList<>();
					// incep de la 1 ca sa nu iau si persoana curenta in calcul
					// pentru prieten temporar.Candatii sunt celelalte persoane
					// fara prieteni temporari
					for (int i = 1; i < persoaneFaraPrieteniTemporari.size(); i++) {
						posibiliCandidati.add(persoaneFaraPrieteniTemporari
								.get(i));
					}
					boolean gasit = false;
					int pozitiePersoanaRandom = -1;
					String persoanaRandom = null;
					// cat timp numai am alti candidati sau pana nu am gasit o
					// potrivire caut prietenTemporar
					while (!posibiliCandidati.isEmpty() && gasit == false) {

						pozitiePersoanaRandom = ((int) (Math.random() * 100000))
								% posibiliCandidati.size();

						persoanaRandom = posibiliCandidati
								.get(pozitiePersoanaRandom);
						rs = stmt
								.executeQuery("SELECT '1' FROM persoane WHERE username='"
										+ persoanaRandom
										+ "' AND "
										+ "username NOT IN("
										+ "SELECT a.prieten FROM arhiva_prieteni_temporari a WHERE a.persoana_username='"
										// AM NOT IN SAU NOT EXISTS PENTRU 2
										// CAZURI
										// CAZUL 1 exista persoane in istoric
										// atunci
										// not in va merge si va cauta
										// CAZUL 2 nu exista persoane in istoric
										// atunci not in va da null si not
										// exists
										// va da true
										+ persoana
										+ "') OR NOT EXISTS "
										+ "(SELECT a.prieten FROM arhiva_prieteni_temporari a WHERE a.persoana_username='"
										+ persoana
										+ "')"
										// sa nu fie deja prieten cu el
										+ " AND username NOT IN("
										+ "SELECT prieten FROM liste_prieteni WHERE username='"
										+ persoana + "')" + "");

						while (rs.next()) {
							gasit = true;
						}
						if (gasit == false) {
							posibiliCandidati.remove(pozitiePersoanaRandom);
						}
					}

					if (gasit) {

						System.out.println("Prieteni temporari random :"
								+ persoana + " cu " + persoanaRandom);

						synchronized (ods) {
							try {
								stmt.executeUpdate("INSERT INTO prieteni_temporari(id,persoana_username,prieten,data_adaugarii) VALUES("
										+ "secventa10.NEXTVAL,'"
										+ persoana
										+ "','" + persoanaRandom + "',sysdate)");
								stmt.executeUpdate("INSERT INTO prieteni_temporari(id,persoana_username,prieten,data_adaugarii) VALUES("
										+ "secventa10.NEXTVAL,'"
										+ persoanaRandom
										+ "','"
										+ persoana
										+ "',sysdate)");
								stmt.executeUpdate("COMMIT");

							} catch (Exception e) {
								System.out.println(e);
								stmt.executeUpdate("ROLLBACK");
							}
						}
						// dupa ce inseram stergem cele 2 persoane din lista de
						// persoane
						// ce vor avea prieteni temporari dati la intamplare
						persoaneFaraPrieteniTemporari.remove(0);
						// dupa ce am sters de pe pozitia 0 se schimba pozitiile
						// deci trebuie
						// sa il cautam
						for (int i = 0; i < persoaneFaraPrieteniTemporari
								.size(); i++) {
							if (persoaneFaraPrieteniTemporari.get(i).equals(
									persoanaRandom))
								persoaneFaraPrieteniTemporari.remove(i);
						}
					} else {
						// daca sunt pe else inseamna ca nu a gasit pe nimeni
						// deci
						// doar il sterg din lista de candidati pt prieteni
						// temporari
						// daca cineva ajunge aici nu are prieteni temporari
						System.out.println(persoaneFaraPrieteniTemporari.get(0)
								+ " nu are prieten temporar");
						persoaneFaraPrieteniTemporari.remove(0);

					}

				}
			}
			if (comanda.equals("startPaginaPrietenTemporar")) {
				System.out.println("startPaginaPrietenTemporar");
				String cont = dis.readUTF();

				ResultSet rs2;
				Statement stmt2 = conn.createStatement();
				// caut prietenul temporar daca exista in tabelul
				// prieteni_temporari
				rs = stmt
						.executeQuery("SELECT persoana_username,poza_profil FROM profiluri WHERE persoana_username=("
								+ "SELECT prieten FROM prieteni_temporari WHERE persoana_username='"
								+ cont + "')");
				boolean gasitPrietenTemporar = false;
				while (rs.next()) {

					// inseana ca sa gasit un prieten temporar
					gasitPrietenTemporar = true;
					dos.writeBoolean(true);

					// iau numarul de interese in comun in momentul acesta poate
					// sa schimbat
					// de cand sau facut combinariile de prieteni
					rs2 = stmt2
							.executeQuery("SELECT COUNT(DISTINCT a.interes) FROM interese_persoane a INNER JOIN interese_persoane B ON "
									+ " a.interes=b.interes WHERE a.persoana_username='"
									+ cont
									+ "' AND b.persoana_username='"
									+ rs.getString(1) + "' ");

					int nrIntereseInComun = 0;
					while (rs2.next()) {
						nrIntereseInComun = rs2.getInt(1);
					}
					rs2.close();
					// trimit numarul lor catre user in caz ca sunt 0 numai am
					// ce sa ii trimit
					dos.writeInt(nrIntereseInComun);

					if (nrIntereseInComun > 0) {
						rs2 = stmt2
								.executeQuery("SELECT DISTINCT a.interes FROM interese_persoane a INNER JOIN interese_persoane b ON "
										+ " a.interes=b.interes WHERE a.persoana_username='"
										+ cont
										+ "' AND b.persoana_username='"
										+ rs.getString(1) + "' ");
						while (rs2.next()) {
							dos.writeUTF(rs2.getString(1));
						}
						rs2.close();
					}
					dos.writeUTF(rs.getString(1));
					int idPoza = rs.getInt(2);

					dos.writeInt(idPoza);
				}

				// inseamna ca nu sa gasit un prieten temporar
				if (gasitPrietenTemporar == false)
					dos.writeBoolean(false);
			}
			if (comanda.equals("startPaginaArhivaPrieteniTemporari")) {
				System.out.println("startPaginaArhivaPrieteniTemporari");
				String cont = dis.readUTF();
				int dim = 0;
				synchronized (ods) {
					rs = stmt
							.executeQuery("SELECT COUNT(*) FROM arhiva_prieteni_temporari WHERE persoana_username='"
									+ cont + "'");
					while (rs.next()) {
						dim = rs.getInt(1);
					}
					// trimit cate 20
					if (dim >20)
						dos.writeInt(20);
					else
						dos.writeInt(dim);
					if (dim == 0) {
						conn.close();
						dis.close();
						dos.close();
						cs.close();
						return;
					}

					rs = stmt
							.executeQuery("SELECT (SELECT p.persoana_username FROM profiluri p WHERE p.persoana_username=a.prieten)"
									+ ",(SELECT p.poza_profil  FROM profiluri p WHERE p.persoana_username=a.prieten),"
									+ "TO_CHAR(data_adaugarii,'yyyy-mm-dd:HH24:mi:ss'),TO_CHAR(data_finalizarii,'yyyy-mm-dd:HH24:mi:ss')"
									+ "  FROM arhiva_prieteni_temporari a WHERE a.persoana_username='"
									+ cont
									+ "'"
									+ " ORDER BY TO_CHAR(a.data_adaugarii,'yyyy-mm-dd:HH24:mi:ss') DESC ");
				}
				int nrRezultate = 0;
				Statement stmt2 = conn.createStatement();
				while (rs.next() && nrRezultate < 20) {
					nrRezultate++;
					// trimit numele
					dos.writeUTF(rs.getString(1));
					// data adaugarii ca prieten temporar
					dos.writeUTF(rs.getString(3));
					// data cand nu au mai fost prieteni temporari;
					dos.writeUTF(rs.getString(4));

					int idPozaProfil = rs.getInt(2);
					dos.writeInt(idPozaProfil);
					ResultSet rs2;
					rs2 = stmt2
							.executeQuery("SELECT width,height FROM poze WHERE id="
									+ idPozaProfil);
					while (rs2.next()) {
						dos.writeInt(rs2.getInt(1));
						dos.writeInt(rs2.getInt(2));
					}
					rs2.close();

				}

			}
			if (comanda.equals("removePrietenTemporar")) {
				System.out.println("removePrietenTemporar");
				String cont = dis.readUTF();
				synchronized (ods) {
					try {
						// inserez prima data datele despre userul curent dupa
						// date despre prietenul lui in arhiva
						stmt.executeUpdate("INSERT into arhiva_prieteni_temporari(id,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,data_finalizarii)"
								+ "SELECT secventa11.NEXTVAL,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,sysdate FROM prieteni_temporari WHERE "
								+ "persoana_username='" + cont + "'");
						stmt.executeUpdate("INSERT into arhiva_prieteni_temporari(id,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,data_finalizarii)"
								+ "SELECT secventa11.NEXTVAL,persoana_username,prieten,data_adaugarii,a_trimis_mesaj,a_primit_mesaj,sysdate FROM prieteni_temporari WHERE "
								+ "prieten='" + cont + "'");
						// sterg din tabelul temporar atat datele userul care a
						// cerut stergerea si datele prietenului temporar
						stmt.executeUpdate("DELETE FROM prieteni_temporari WHERE persoana_username='"
								+ cont + "'");
						stmt.executeUpdate("DELETE FROM prieteni_temporari WHERE prieten='"
								+ cont + "'");

						stmt.executeUpdate("COMMIT");
					} catch (Exception e) {
						stmt.executeUpdate("ROLLBACK");
						System.out.println(e);
					}
				}
			}
			if (comanda.equals("paginaNewsFeedGetMorePosts")) {
				System.out.println("paginaNewsFeedGetMorePosts");
				String profil = dis.readUTF();

				int idUltimPost = dis.readInt();

				synchronized (ods) {
					// selectez toate comenturile de pe profilul meu si toate
					// comenturile de pe
					// profilurile prietenilor care sunt mai vechi ca ultimul
					// coment
					// ce il am pe pagina iau maxim 10 comenturi

					rs = stmt
							.executeQuery("SELECT COUNT(*) FROM (("
									+ " SELECT id,textul,autor,cuPoza,data_postari,"
									+ "'"
									+ profil
									+ "' profil,vizibilitate,profil_id"
									+ " FROM "
									+ "comenturi WHERE "
									+ "profil_id = "
									+ "(SELECT id FROM profiluri WHERE  persoana_username ="
									+ "'"
									+ profil
									+ "') "
									+ " AND TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss')<(SELECT TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss') FROM comenturi WHERE id="
									+ idUltimPost
									+ ")"
									+ " )"
									+ "UNION"
									+ "(SELECT id,textul,autor,cuPoza,data_postari,"
									+ "(SELECT persoana_username FROM profiluri p1 WHERE p1.id=c1.profil_id) profil,vizibilitate,profil_id"
									+ "  FROM comenturi c1 WHERE profil_id IN"
									+ "(SELECT id FROM profiluri WHERE persoana_username IN ("
									+ "SELECT prieten FROM liste_prieteni WHERE username='"
									+ profil
									+ "' AND acceptat='DA'))"
									+ " AND TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss')<(SELECT TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss') FROM comenturi WHERE id="
									+ idUltimPost
									+ ")"
									+ "))"
									+ " WHERE "
									// am introdus si vizibilitatea comenturilor
									// vizibilitatea poate sa
									// fie:eu,prieteni,toti
									// aceste optiuni sunt doar pentru cel carui
									// ii apartine contul
									// implicit restul au vizibilitatea toti
									// daca este 'eu' doar cel carui ii apartine
									// profilul
									// ii sunt vizibile posturile
									// daca este'prieteni' doar celui carui ii
									// apartine profilul si prietenii lui
									// pot vedea posturile
									// daca este 'toti' oricine poate vedea
									// posturile
									+ " (vizibilitate='toti' OR(vizibilitate='eu' AND "
									+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
									+ profil
									+ "')"
									+ "OR(vizibilitate='prieteni' AND( '"
									+ profil
									+ "' IN("
									+ "SELECT prieten FROM liste_prieteni WHERE username=profil"
									+ " AND acceptat='DA' ) OR "
									+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
									+ profil + "'" + ")))"
									// pana aici este despre vizibilitatea
									// postului raportata cu cine il aceseaza
									+ "");
					int nr = 0;
					while (rs.next()) {
						nr = rs.getInt(1);
					}
					if (nr > 10)
						dos.writeInt(10);
					else
						dos.writeInt(nr);
					System.out.println(nr);

					rs = stmt
							.executeQuery("SELECT c.id,c.textul,c.autor,c.cuPoza,"
									+ "(SELECT p.poza_profil FROM profiluri p WHERE p.persoana_username="
									+ "c.autor),c.profil,TO_CHAR(c.data_postari,'yyyy-mm-dd:HH24:mi:ss')"
									+ " FROM (("
									+ " SELECT id,textul,autor,cuPoza,data_postari,"
									+ "'"
									+ profil
									+ "' profil,vizibilitate,profil_id"
									+ " FROM "
									+ "comenturi WHERE "
									+ "profil_id = "
									+ "(SELECT id FROM profiluri WHERE  persoana_username ="
									+ "'"
									+ profil
									+ "')"
									+ " AND TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss')<(SELECT TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss') FROM comenturi WHERE id="
									+ idUltimPost
									+ ")"
									+ " )"
									+ "UNION"
									+ "(SELECT id,textul,autor,cuPoza,data_postari,"
									+ "(SELECT persoana_username FROM profiluri p1 WHERE p1.id=c1.profil_id) profil,vizibilitate,profil_id"
									+ "  FROM comenturi c1 WHERE profil_id IN"
									+ "(SELECT id FROM profiluri WHERE persoana_username IN ("
									+ "SELECT prieten FROM liste_prieteni WHERE username='"
									+ profil
									+ "' AND acceptat='DA'))"
									+ " AND TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss')<(SELECT TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss') FROM comenturi WHERE id="
									+ idUltimPost
									+ ")"
									+ ")) c "
									+ " WHERE "
									// am introdus si vizibilitatea comenturilor
									// vizibilitatea poate sa
									// fie:eu,prieteni,toti
									// aceste optiuni sunt doar pentru cel carui
									// ii apartine contul
									// implicit restul au vizibilitatea toti
									// daca este 'eu' doar cel carui ii apartine
									// profilul
									// ii sunt vizibile posturile
									// daca este'prieteni' doar celui carui ii
									// apartine profilul si prietenii lui
									// pot vedea posturile
									// daca este 'toti' oricine poate vedea
									// posturile
									+ " (vizibilitate='toti' OR(vizibilitate='eu' AND "
									+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
									+ profil
									+ "')"
									+ "OR(vizibilitate='prieteni' AND( '"
									+ profil
									+ "' IN("
									+ "SELECT prieten FROM liste_prieteni WHERE username=profil"
									+ " AND acceptat='DA' ) OR "
									+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
									+ profil
									+ "'"
									+ ")))"
									// pana aici este despre vizibilitatea
									// postului raportata cu cine il aceseaza
									+ "ORDER BY TO_CHAR(c.data_postari,'yyyy-mm-dd:HH24:mi:ss') DESC");

					/*
					 * rs = stmt
					 * .executeQuery("SELECT c.id,c.textul,c.autor,c.cuPoza," +
					 * "(SELECT p.poza_profil FROM profiluri p WHERE p.persoana_username="
					 * + "c.autor)" + " FROM comenturi c WHERE" +
					 * " profil_id = " +
					 * " (SELECT id FROM profiluri WHERE persoana_username = " +
					 * " '" + profil + "')" +
					 * "ORDER BY TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss') DESC"
					 * );
					 */
				}
				// trimit maxim 10
				int contor = 1;
				;
				Statement stmt2 = conn.createStatement();
				while (rs.next() && contor <= 10) {

					contor++;
					dos.writeInt(rs.getInt(1));
					dos.writeUTF(rs.getString(2));
					dos.writeUTF(rs.getString(3));

					// poza de profil
					int idPozaProfil = rs.getInt(5);
					dos.writeInt(idPozaProfil);
					ResultSet rs2;
					rs2 = stmt2
							.executeQuery("SELECT width,height FROM poze WHERE id="
									+ idPozaProfil);
					while (rs2.next()) {
						// width
						dos.writeInt(rs2.getInt(1));
						// height
						dos.writeInt(rs2.getInt(2));
					}

					rs2.close();
					int idPoza = rs.getInt(4);

					if (idPoza != 0) {
						dos.writeUTF("arePoza");
						dos.writeInt(idPoza);
						rs2 = stmt2
								.executeQuery("SELECT width,height FROM poze WHERE id="
										+ idPoza);

						while (rs2.next()) {
							// width
							dos.writeInt(rs2.getInt(1));
							// height
							dos.writeInt(rs2.getInt(2));
						}
						rs2.close();

					} else
						dos.writeUTF("nuArePoza");
					dos.writeUTF(rs.getString(6));
					// trimit data postului
					dos.writeUTF(rs.getString(7));

				}

			}
			if (comanda.equals("paginaProfilGetMorePosts")) {
				System.out.println("paginaProfilGetMorePosts");
				String profil = dis.readUTF();
				String cont = dis.readUTF();
				int idUltimPost = dis.readInt();
				synchronized (ods) {

					rs = stmt
							.executeQuery("SELECT COUNT(*) FROM comenturi WHERE "
									+ "profil_id = "
									+ "(SELECT id FROM profiluri WHERE  persoana_username ="
									+ "'"
									+ profil
									+ "') "
									+ " AND TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss')<(SELECT TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss') FROM comenturi WHERE id="
									+ idUltimPost
									+ ")"
									// am introdus si vizibilitatea comenturilor
									// vizibilitatea poate sa
									// fie:eu,prieteni,toti
									// aceste optiuni sunt doar pentru cel carui
									// ii apartine contul
									// implicit restul au vizibilitatea toti
									// daca este 'eu' doar cel carui ii apartine
									// profilul
									// ii sunt vizibile posturile
									// daca este'prieteni' doar celui carui ii
									// apartine profilul si prietenii lui
									// pot vedea posturile
									// daca este 'toti' oricine poate vedea
									// posturile
									+ " AND(vizibilitate='toti' OR(vizibilitate='eu' AND "
									+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
									+ cont
									+ "')"
									+ "OR(vizibilitate='prieteni' AND( '"
									+ cont
									+ "' IN("
									+ "SELECT prieten FROM liste_prieteni WHERE username='"
									+ profil
									+ "' AND acceptat='DA' ) OR "
									+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
									+ cont + "'" + ")))"
									// pana aici este despre vizibilitatea
									// postului raportata cu cine il aceseaza
									+ "");

					while (rs.next()) {
						if (rs.getInt(1) > 10)
							dos.writeInt(10);
						else
							dos.writeInt(rs.getInt(1));
					}
					// selectez toate comenturile de pe profilul meu s
					// care sunt mai vechi ca ultimul coment
					// ce il am pe pagina iau maxim 10 comenturi
					rs = stmt
							.executeQuery("SELECT c.id,c.textul,c.autor,c.cuPoza,"
									+ "(SELECT p.poza_profil FROM profiluri p WHERE p.persoana_username="
									+ "c.autor),TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss')"
									+ " FROM comenturi c WHERE"
									+ " profil_id = "
									+ " (SELECT id FROM profiluri WHERE persoana_username = "
									+ " '"
									+ profil
									+ "')"
									+ " AND TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss')<(SELECT TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss') FROM comenturi WHERE id="
									+ idUltimPost
									+ ")"
									// am introdus si vizibilitatea comenturilor
									// vizibilitatea poate sa
									// fie:eu,prieteni,toti
									// aceste optiuni sunt doar pentru cel carui
									// ii apartine contul
									// implicit restul au vizibilitatea toti
									// daca este 'eu' doar cel carui ii apartine
									// profilul
									// ii sunt vizibile posturile
									// daca este'prieteni' doar celui carui ii
									// apartine profilul si prietenii lui
									// pot vedea posturile
									// daca este 'toti' oricine poate vedea
									// posturile
									+ " AND(vizibilitate='toti' OR(vizibilitate='eu' AND "
									+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
									+ cont
									+ "')"
									+ "OR(vizibilitate='prieteni' AND( '"
									+ cont
									+ "' IN("
									+ "SELECT prieten FROM liste_prieteni WHERE username='"
									+ profil
									+ "' AND acceptat='DA' ) OR "
									+ "(SELECT persoana_username FROM profiluri  WHERE id=profil_id) ='"
									+ cont
									+ "'"
									+ ")))"
									// pana aici este despre vizibilitatea
									// postului raportata cu cine il aceseaza
									+ "ORDER BY TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss') DESC");
				}
				Statement stmt2 = conn.createStatement();
				// afisez maxim 10
				int contor = 1;
				while (rs.next() && contor <= 10) {
					contor++;
					dos.writeInt(rs.getInt(1));
					dos.writeUTF(rs.getString(2));
					dos.writeUTF(rs.getString(3));

					// poza de profil
					int idPozaProfil = rs.getInt(5);
					dos.writeInt(idPozaProfil);
					ResultSet rs2;
					rs2 = stmt2
							.executeQuery("SELECT width,height FROM poze WHERE id="
									+ idPozaProfil);
					while (rs2.next()) {
						// width
						dos.writeInt(rs2.getInt(1));
						// height
						dos.writeInt(rs2.getInt(2));

					}

					rs2.close();
					int idPoza = rs.getInt(4);

					if (idPoza != 0) {
						dos.writeUTF("arePoza");
						dos.writeInt(idPoza);
						rs2 = stmt2
								.executeQuery("SELECT width,height FROM poze WHERE id="
										+ idPoza);

						while (rs2.next()) {
							// width
							dos.writeInt(rs2.getInt(1));
							// height
							dos.writeInt(rs2.getInt(2));

						}
						rs2.close();

					} else
						dos.writeUTF("nuArePoza");
					// scriu timestampul postului
					dos.writeUTF(rs.getString(6));

				}

			}
			if (comanda.equals("paginaComentGetMoreComents")) {
				System.out.println("paginaComentGetMoreComents");
				int comentId = dis.readInt();
				int idUltimuluiReply = dis.readInt();
				synchronized (ods) {
					rs = stmt
							.executeQuery("Select '1' FROM comenturi  WHERE id="
									+ comentId);
					boolean comentExistent = false;
					while (rs.next()) {
						comentExistent = true;
					}
					dos.writeBoolean(comentExistent);
					if (comentExistent == false) {
						conn.close();
						dis.close();
						dos.close();
						cs.close();
						return;
					}

					rs = stmt
							.executeQuery("SELECT COUNT(*) FROM comenturi WHERE "
									+ "coment_id = "
									+ comentId
									+ " AND TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss')<(SELECT TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss') FROM comenturi WHERE id="
									+ idUltimuluiReply + ")");

					while (rs.next()) {
						if (rs.getInt(1) > 10)
							dos.writeInt(10);
						else
							dos.writeInt(rs.getInt(1));
					}
					rs = stmt
							.executeQuery("SELECT c.id,c.textul,c.autor,c.cuPoza,"
									+ "(SELECT p.poza_profil FROM profiluri p WHERE p.persoana_username="
									+ "c.autor),TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss')"
									+ "FROM comenturi c WHERE"
									+ " coment_id = "
									+ comentId
									+ " AND TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss')<(SELECT TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss') FROM comenturi WHERE id="
									+ idUltimuluiReply
									+ ")"
									+ "ORDER BY TO_CHAR(data_postari,'yyyy-mm-dd:HH24:mi:ss') DESC");
				}
				// trimit maxim 10 comenturi
				int contor = 1;
				Statement stmt2 = conn.createStatement();
				while (rs.next() && contor <= 10) {
					contor++;
					dos.writeInt(rs.getInt(1));
					dos.writeUTF(rs.getString(2));
					dos.writeUTF(rs.getString(3));

					// poza de profil
					int idPozaProfil = rs.getInt(5);
					dos.writeInt(idPozaProfil);
					ResultSet rs2;
					rs2 = stmt2
							.executeQuery("SELECT width,height FROM poze WHERE id="
									+ idPozaProfil);
					while (rs2.next()) {
						// width
						dos.writeInt(rs2.getInt(1));
						// height
						dos.writeInt(rs2.getInt(2));
					}

					rs2.close();

					int idPoza = rs.getInt(4);

					if (idPoza != 0) {
						dos.writeUTF("arePoza");
						dos.writeInt(idPoza);
						rs2 = stmt2
								.executeQuery("SELECT width,height FROM poze WHERE id="
										+ idPoza);

						while (rs2.next()) {
							// width
							dos.writeInt(rs2.getInt(1));
							// height
							dos.writeInt(rs2.getInt(2));

						}
						rs2.close();

					} else
						dos.writeUTF("nuArePoza");
					// scriu timestampul
					dos.writeUTF(rs.getString(6));
				}

			}
			if (comanda.equals("updatePaginaMesaje")) {
				System.out.println("updatePaginaMesaje");
				String cont = dis.readUTF();
				String partenerConversatie = dis.readUTF();
				boolean suntElementeInLista = dis.readBoolean();

				int idUltimMesaj = -1;
				if (suntElementeInLista == true) {
					idUltimMesaj = dis.readInt();
				}
				synchronized (ods) {
					// iau mesajele mai recente decat ce are in acest moment
					if (suntElementeInLista == true) {
						rs = stmt
						// daca sunt elemente in lista compar cu ultimul mesaj
						// daca nu sunt le iau de parca nu le iau pe toate
						// posibil sa modific cate o sa iau in viitor si sa
						// nu modific comenturile
								.executeQuery("SELECT COUNT(*) FROM mesaje m1 WHERE "
										+ "((username_sender='"
										+ cont
										+ "' AND username_receiver='"
										+ partenerConversatie
										+ "') OR  "
										+ "(username_sender='"
										+ partenerConversatie
										+ "' AND username_receiver='"
										+ cont
										+ "'))"
										+ " AND TO_CHAR(m1.data_mesaj,'yyyy-mm-dd:HH24:mi:ss')>(SELECT TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss') FROM mesaje WHERE id="
										+ idUltimMesaj + ")" + "");
					} else {
						rs = stmt
								.executeQuery("SELECT COUNT(*) FROM mesaje WHERE "
										+ "((username_sender='"
										+ cont
										+ "' AND username_receiver='"
										+ partenerConversatie
										+ "') OR  "
										+ "(username_sender='"
										+ partenerConversatie
										+ "' AND username_receiver='"
										+ cont
										+ "'))");
					}

					boolean existaMesaje = false;
					while (rs.next()) {
						existaMesaje = true;
						// trimit maxim 30 de mesaje odata
						if (rs.getInt(1) > 30)
							dos.writeInt(30);
						else
							dos.writeInt(rs.getInt(1));
					}
					if (suntElementeInLista == true) {
						rs = stmt
						// daca e true iau in functie de ultimul mesaj
						// daca e false nu iau in functie de ultimul mesaj
								.executeQuery("SELECT id,username_sender,mesaj,"
										+ "TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss')"
										+ " FROM mesaje WHERE "
										+ "((username_sender='"
										+ cont
										+ "' AND username_receiver='"
										+ partenerConversatie
										+ "') OR  "
										+ "(username_sender='"
										+ partenerConversatie
										+ "' AND username_receiver='"
										+ cont
										+ "'))"
										+ " AND TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss')>(SELECT TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss') FROM mesaje WHERE id="
										+ idUltimMesaj
										+ ")"
										+ "ORDER BY TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss') DESC");
					} else {
						rs = stmt
								.executeQuery("SELECT id,username_sender,mesaj,"
										+ "TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss')"
										+ " FROM mesaje WHERE "
										+ "((username_sender='"
										+ cont
										+ "' AND username_receiver='"
										+ partenerConversatie
										+ "') OR  "
										+ "(username_sender='"
										+ partenerConversatie
										+ "' AND username_receiver='"
										+ cont
										+ "'))"
										+ "ORDER BY TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss') DESC");
					}
					// trimit maxim 30 de mesaje odata
					int nrMesaje = 0;
					while (rs.next() && nrMesaje < 30) {
						nrMesaje++;
						// id mesaj
						dos.writeInt(rs.getInt(1));
						// nume sender
						dos.writeUTF(rs.getString(2));

						// mesajul
						dos.writeUTF(rs.getString(3));

						// data_postarii;
						String data_postarii = rs.getString(4);
						dos.writeUTF(data_postarii);
					}
					synchronized(ods){
						try{
							//zic ca a vazut ultimele mesaje de la partenerul sau de conversatie
							stmt.executeUpdate("DELETE FROM mesaje_noi WHERE username_sender='"
									+ partenerConversatie
									+ "' AND username_receiver='"
								+ cont
									+ "'");
							stmt.executeUpdate("COMMIT");
						}
						catch(Exception e){
							System.out.println(e);
							stmt.executeUpdate("ROLLBACK");
						}
					}
				}
			}
			if (comanda.equals("startPaginaClasamentPrieteniTemporari")) {
				System.out.println("startPaginaClasamentPrieteniTemporari");
				synchronized (ods) {
					rs = stmt
							.executeQuery("SELECT COUNT(*) nr FROM profiluri ");

					while (rs.next()) {

						if (rs.getInt(1) > 15)
							dos.writeInt(15);

						else
							dos.writeInt(rs.getInt(1));

					}
					// SORTEZ descrescator dupa puncte si daca sunt mai multi cu
					// acelasi punctaj
					// sortez crescator dupa numele lor
					rs = stmt
							.executeQuery("SELECT persoana_username,puncte_prieten_temporar,poza_profil FROM profiluri ORDER BY "
									+ "puncte_prieten_temporar DESC,persoana_username ASC");
				}
				Statement stmt2 = conn.createStatement();
				ResultSet rs2;
				// top 10 useri in functie de puncte
				int nr = 1;
				while (rs.next() && nr <= 15) {

					// trimis numele utilizatorului
					dos.writeUTF(rs.getString(1));

					// poza de profil
					int idPozaProfil = rs.getInt(3);
					// trimit idul pozei de profil
					dos.writeInt(idPozaProfil);
					rs2 = stmt2
							.executeQuery("SELECT width,height FROM poze WHERE id="
									+ idPozaProfil);
					while (rs2.next()) {

						dos.writeInt(rs2.getInt(1));
						dos.writeInt(rs2.getInt(2));
					}
					// trimis numarul de puncte
					dos.writeInt(rs.getInt(2));
					nr++;
				}

			}
			if (comanda.equals("addReviewPrietenTemporar")) {
				System.out.println("addReviewPrietenTemporar");
				String cont = dis.readUTF();
				rs = stmt
						.executeQuery("SELECT prieten FROM arhiva_prieteni_temporari WHERE "
								+ " persoana_username='"
								+ cont
								+ "' AND a_trimis_mesaj='DA' AND a_primit_mesaj='DA' AND"
								+ " a_primit_user_rating='NU'");
				boolean trebuieRating = false;
				String numePrieten = null;
				while (rs.next()) {

					trebuieRating = true;
					numePrieten = rs.getString(1);
				}
				dos.writeBoolean(trebuieRating);
				if (trebuieRating == true) {
					dos.writeUTF(numePrieten);
				}
			}
			if (comanda.equals("addReviewPrietenTemporarDaNota")) {
				System.out.println("addReviewPrietenTemporarDaNota");
				String cont = dis.readUTF();
				String prieten = dis.readUTF();
				int rating = dis.readInt();

				rs = stmt
						.executeQuery("SELECT '1' FROM arhiva_prieteni_temporari WHERE "
								+ " persoana_username='"
								+ cont
								+ "' AND prieten='" + prieten + "'");
				boolean adaugaRating = false;
				while (rs.next()) {
					adaugaRating = true;
				}
				// daca e false inseamna ca a ajuns aici printr-un bug altfel
				// nu avea cum sa ajunga pentru ca ratingbarul aparea doar daca
				// aceste conditii erau indeplinite
				if (adaugaRating == false) {
					conn.close();
					dis.close();
					dos.close();
					cs.close();
					return;
				}
				synchronized (ods) {
					try {
						// adaug pucntele din rating prietenului temporar
						stmt.executeUpdate("UPDATE profiluri SET puncte_prieten_temporar=puncte_prieten_temporar+"
								+ rating
								+ " WHERE persoana_username='"
								+ prieten + "'");
						stmt.executeUpdate("UPDATE arhiva_prieteni_temporari SET a_primit_user_rating='DA' WHERE "
								+ " persoana_username='"
								+ cont
								+ "' AND prieten='" + prieten + "'");
						stmt.executeUpdate("COMMIT");
					} catch (Exception e) {
						stmt.executeUpdate("ROLLBACK");
						System.out.println(e);
					}
				}
			}
			if (comanda.equals("dwlImageFromServer")) {
				System.out.println("dwlImageFromServer");
				int idPoza = dis.readInt();

				rs = stmt.executeQuery("SELECT data FROM poze WHERE id="
						+ idPoza);
				while (rs.next()) {
					byte[] temp = rs.getBytes(1);

					dos.writeInt(temp.length);
					dos.write(temp);

				}
			}
			if (comanda.equals("paginaFriendRequestsGetMoreFriendRequests")) {
				System.out.println("paginaFriendRequestsGetMoreFriendRequests");
				String profil = dis.readUTF();
				String numeleUltimuluiFR = dis.readUTF();
				synchronized (ods) {
					rs = stmt
							.executeQuery("SELECT COUNT(*) FROM liste_prieteni WHERE prieten="
									+ "'"
									+ profil
									+ "' AND acceptat='PENDING' AND "
									+ "username >'" + numeleUltimuluiFR + "'");

					boolean trimisDimensiunea = false;
					while (rs.next()) {
						trimisDimensiunea = true;
						int nrRezultate = rs.getInt(1);
						if (nrRezultate > 20)
							dos.writeInt(20);
						else
							dos.writeInt(nrRezultate);
					}

					if (trimisDimensiunea == false) {
						dos.writeInt(0);
						conn.close();
						dis.close();
						dos.close();
						cs.close();
						return;
					}

					rs = stmt
							.executeQuery("SELECT persoana_username,poza_profil FROM profiluri WHERE persoana_username IN ("
									+ "SELECT username FROM liste_prieteni WHERE prieten="
									+ "'"
									+ profil
									+ "' AND acceptat='PENDING'  AND "
									+ "username >'"
									+ numeleUltimuluiFR
									+ "' )"
									+ "ORDER BY persoana_username ASC");
				}
				int nrFriendRequests = 0;
				Statement stmt2 = conn.createStatement();
				// trimit maxim 20 de recomandari de prieteni odata
				while (rs.next() && nrFriendRequests < 20) {
					nrFriendRequests++;
					dos.writeUTF(rs.getString(1));
					// poza de profil
					int idPozaProfil = rs.getInt(2);
					dos.writeInt(idPozaProfil);
					ResultSet rs2;
					rs2 = stmt2
							.executeQuery("SELECT width,height FROM poze WHERE id="
									+ idPozaProfil);
					while (rs2.next()) {
						dos.writeInt(rs2.getInt(1));
						dos.writeInt(rs2.getInt(2));

					}

					rs2.close();
				}
			}
			if (comanda.equals("getMoreMessages")) {
				System.out.println("getMoreMessages");
				String cont = dis.readUTF();
				String partenerConversatie = dis.readUTF();
				int idulPrimuluiMesaj = dis.readInt();
				synchronized (ods) {

					rs = stmt

							.executeQuery("SELECT COUNT(*) FROM mesaje  WHERE "
									+ "((username_sender='"
									+ cont
									+ "' AND username_receiver='"
									+ partenerConversatie
									+ "') OR  "
									+ "(username_sender='"
									+ partenerConversatie
									+ "' AND username_receiver='"
									+ cont
									+ "'))"
									// iau toate mesajele mai vechi decat cel
									// mai vechi mesaj din lista utilizatorului
									// el fiind cel de pe pozitia 0
									+ " AND TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss')<(SELECT TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss') FROM mesaje WHERE id="
									+ idulPrimuluiMesaj + ")" + "");

					boolean existaMesaje = false;
					while (rs.next()) {
						existaMesaje = true;
						// trimit maxim 30 de mesaje odata
						if (rs.getInt(1) > 30)
							dos.writeInt(30);
						else
							dos.writeInt(rs.getInt(1));
					}

					rs = stmt
							.executeQuery("SELECT id,username_sender,mesaj,"
									+ "TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss')"
									+ " FROM mesaje WHERE "
									+ "((username_sender='"
									+ cont
									+ "' AND username_receiver='"
									+ partenerConversatie
									+ "') OR  "
									+ "(username_sender='"
									+ partenerConversatie
									+ "' AND username_receiver='"
									+ cont
									+ "'))"
									+ " AND TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss')<(SELECT TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss') FROM mesaje WHERE id="
									+ idulPrimuluiMesaj + ")"
									// iau toate mesajele mai vechi decat cel
									// mai vechi mesaj din lista utilizatorului
									// el fiind cel de pe pozitia 0
									+ "ORDER BY TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss') DESC");
					// trimit maxim 30 de mesaje
					int nrMesaje = 0;
					while (rs.next() && nrMesaje < 30) {
						nrMesaje++;
						// id mesaj
						dos.writeInt(rs.getInt(1));
						// nume sender
						dos.writeUTF(rs.getString(2));

						// mesajul
						dos.writeUTF(rs.getString(3));

						// data_postarii;
						String data_postarii = rs.getString(4);
						dos.writeUTF(data_postarii);
					}
				}
			}
			if (comanda.equals("PaginaFriendRecommendationDismiss")) {
				System.out.println("PaginaFriendRecommendationDismiss");
				String persoana = dis.readUTF();
				String recomandare = dis.readUTF();
				synchronized (ods) {
					try {
						stmt.executeUpdate("INSERT INTO renunta_recomandari_prieteni(id,persoana_username,recomandare)"
								+ "	VALUES(secventa12.NEXTVAL,'"
								+ persoana
								+ "','" + recomandare + "')");
						stmt.executeUpdate("COMMIT");
					} catch (Exception e) {
						stmt.executeUpdate("ROLLBACK");
						System.out.println(e);
					}
				}
			}
			if (comanda.equals("friendListGetMoreFriends")) {
				System.out.println("friendListGetMoreFriends");
				String profil = dis.readUTF();
				String ultimaPersoana = dis.readUTF();
				synchronized (ods) {
					rs = stmt
							.executeQuery("SELECT COUNT(*) FROM liste_prieteni WHERE username="
									+ "'"
									+ profil
									+ "' AND acceptat='DA' AND "
									+ " prieten>'" + ultimaPersoana + "'");
					boolean trimisDimensiunea = false;
					while (rs.next()) {
						trimisDimensiunea = true;
						int nrRezultate = rs.getInt(1);
						// trimit maxim 10 de rezultate
						if (nrRezultate > 10)
							dos.writeInt(10);
						else
							dos.writeInt(nrRezultate);
					}
					if (trimisDimensiunea == false) {
						dos.writeInt(0);
						conn.close();
						dis.close();
						dos.close();
						cs.close();
						return;
					}
					rs = stmt
							.executeQuery("SELECT persoana_username,poza_profil FROM profiluri WHERE persoana_username IN ("
									+ "SELECT prieten FROM liste_prieteni WHERE username="
									+ "'"
									+ profil
									+ "' AND acceptat='DA' AND "
									+ "prieten>'"
									+ ultimaPersoana
									+ "')"
									+ "ORDER BY persoana_username");
				}
				int nrRezultate = 0;
				Statement stmt2 = conn.createStatement();
				while (rs.next() && nrRezultate < 10) {
					nrRezultate++;
					dos.writeUTF(rs.getString(1));
					// poza de profil
					int idPozaProfil = rs.getInt(2);
					dos.writeInt(idPozaProfil);
					ResultSet rs2;
					rs2 = stmt2
							.executeQuery("SELECT width,height FROM poze WHERE id="
									+ idPozaProfil);
					while (rs2.next()) {
						dos.writeInt(rs2.getInt(1));
						dos.writeInt(rs2.getInt(2));

					}

					rs2.close();
				}
			}
			if (comanda.equals("arhivaPrieteniTemporariGetMore")) {
				System.out.println("arhivaPrieteniTemporariGetMore");
				String cont = dis.readUTF();
				String data_adaugarii_ultimului_prieten = dis.readUTF();
				int dim = 0;
				synchronized (ods) {
					rs = stmt
							.executeQuery("SELECT COUNT(*) FROM arhiva_prieteni_temporari WHERE persoana_username='"
									+ cont
									+ "' "
									+ "AND data_adaugarii< TO_DATE('"
									+ data_adaugarii_ultimului_prieten
									+ "','yyyy-mm-dd:HH24:mi:ss')");
					while (rs.next()) {
						dim = rs.getInt(1);
					}
					// trimi maxim 10
					if (dim > 10)
						dos.writeInt(10);
					else
						dos.writeInt(dim);
					if (dim == 0) {
						conn.close();
						dis.close();
						dos.close();
						cs.close();
						return;
					}

					rs = stmt
							.executeQuery("SELECT (SELECT p.persoana_username FROM profiluri p WHERE p.persoana_username=a.prieten)"
									+ ",(SELECT p.poza_profil  FROM profiluri p WHERE p.persoana_username=a.prieten),"
									+ "TO_CHAR(data_adaugarii,'yyyy-mm-dd:HH24:mi:ss'),TO_CHAR(data_finalizarii,'yyyy-mm-dd:HH24:mi:ss')"
									+ "  FROM arhiva_prieteni_temporari a WHERE a.persoana_username='"
									+ cont
									+ "' "
									+ "AND data_adaugarii< TO_DATE('"
									+ data_adaugarii_ultimului_prieten
									+ "','yyyy-mm-dd:HH24:mi:ss') "
									+ " ORDER BY TO_CHAR(a.data_adaugarii,'yyyy-mm-dd:HH24:mi:ss') DESC ");
				}
				int nrRezultate = 0;
				Statement stmt2 = conn.createStatement();
				while (rs.next() && nrRezultate < 10) {
					nrRezultate++;
					// trimit numele
					dos.writeUTF(rs.getString(1));
					// data adaugarii ca prieten temporar
					dos.writeUTF(rs.getString(3));
					// data cand nu au mai fost prieteni temporari;
					dos.writeUTF(rs.getString(4));

					int idPozaProfil = rs.getInt(2);
					dos.writeInt(idPozaProfil);
					ResultSet rs2;
					rs2 = stmt2
							.executeQuery("SELECT width,height FROM poze WHERE id="
									+ idPozaProfil);
					while (rs2.next()) {
						dos.writeInt(rs2.getInt(1));
						dos.writeInt(rs2.getInt(2));
					}
					rs2.close();

				}

			}
			if (comanda.equals("schimbaRatingInteres")) {
				System.out.println("schimbaRatingInteres");
				String profil = dis.readUTF();
				String interes = dis.readUTF();
				int rating = dis.readInt();

				synchronized (ods) {
					try {
						stmt.executeQuery("UPDATE interese_persoane "
								+ " SET rating=" + rating
								+ " WHERE persoana_username='" + profil
								+ "' AND interes='" + interes + "'");
						stmt.executeUpdate("COMMIT");
					} catch (Exception e) {
						System.out.println(e + "");
						stmt.executeUpdate("ROLLBACK");
					}
				}
			}
			if (comanda.equals("updatePaginaConversatii")) {
				System.out.println("updatePaginaConversatii");
				String cont = dis.readUTF();
				//iau si cate elemente sunt in lista in caz ca a incarcat foarte multe
				//si a dat getmoreConversatii sa nu il duca la primele 20 de rezultate
				//int nrElemente=dis.readInt();
				
					rs = stmt
							.executeQuery("SELECT COUNT(*) FROM("
									+ "(SELECT username_sender FROM mesaje WHERE username_receiver='"
									+ cont
									+ "') UNION"
									+ "(SELECT username_receiver FROM mesaje WHERE username_sender='"
									+ cont + "'" + ")" + ")");
					int nrRezultate=0;
					while(rs.next()){
				
						nrRezultate=rs.getInt(1);
					}
					dos.writeInt(nrRezultate);
					
					rs = stmt
							.executeQuery("SELECT * FROM("
									+ "(SELECT username_sender FROM mesaje WHERE username_receiver='"
									+ cont
									+ "') UNION"
									+ "(SELECT username_receiver FROM mesaje WHERE username_sender='"
									+ cont + "'" + ")"
									+ "" + ")");
					Statement stmt2 = conn.createStatement();
					Statement stmt3 = conn.createStatement();
					while (rs.next()) {
						String usernamePartener = rs.getString(1);
						
						ResultSet rs2;
			
						rs2=stmt2.executeQuery("SELECT TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss'),mesaj,(SELECT poza_profil FROM profiluri WHERE "
								+ "persoana_username='"+usernamePartener+ "'),username_sender "
								+ "FROM mesaje WHERE "
								+ "((username_sender='"+cont+"' AND username_receiver='"+usernamePartener+"') OR "
								+ "(username_sender='"+usernamePartener+"' AND username_receiver='"+cont+"')) AND "
								+ " TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss')=(SELECT MAX(TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss')) FROM mesaje WHERE "
								+ "(username_sender='"+cont+"' AND username_receiver='"+usernamePartener+"') OR "
								+ "(username_sender='"+usernamePartener+"' AND username_receiver='"+cont+"'))"
								+ " ORDER BY TO_CHAR(data_mesaj,'yyyy-mm-dd:HH24:mi:ss') DESC ");
					
					//numele partenerului de conversatie a utilizatorului curent
						dos.writeUTF(usernamePartener);
						
					while(rs2.next()){
						ResultSet rs3;
						//poza de profil
						int idPozaProfil=rs2.getInt(3);
						dos.writeInt(idPozaProfil);
						
						rs3 = stmt3
								.executeQuery("SELECT width,height FROM poze WHERE id="
										+ idPozaProfil);
						while (rs3.next()) {
							// width
							dos.writeInt(rs3.getInt(1));
							// height
							dos.writeInt(rs3.getInt(2));

						}
						rs3.close();
						//data
						dos.writeUTF(rs2.getString(1));
						//mesajul
						dos.writeUTF(rs2.getString(2));
						//cine a trimis ultimul mesaj dintre cei 2
						dos.writeUTF(rs2.getString(4));
						//aflu daca aceasta persoana ia trimis mesaje noi
						rs3=stmt3.executeQuery("SELECT '1' FROM mesaje_noi WHERE username_sender='"
								+usernamePartener
								+ "' AND username_receiver='"
								+cont
								+ "'");
						boolean mesajNou=false;
						while(rs3.next()){
							mesajNou=true;
						}
						if(mesajNou)dos.writeUTF("DA");
						else dos.writeUTF("NU");
						rs3.close();
					}
					rs2.close();
					
					}
				
			}
			conn.close();
			dis.close();
			dos.close();
			cs.close();
		} catch (Exception e) {
			try {

				dis.close();
				dos.close();
				cs.close();
				conn.close();

			} catch (Exception e2) {

				System.out.println(e2);
			}
			System.out.println(e);
		}
	}

	public static void main(String[] args) {
		ServerSocket ss = null;
		Socket cs = null;
		try {
			ss = new ServerSocket(505);

			// Connection conn = DriverManager
			// .getConnection("jdbc:ucanaccess://C:/BackUp/Universitate/Proiect Licenta/Baza de date/Licenta.accdb");

			// Class.forName("oracle.jdbc.driver.OracleDriver");

			OracleDataSource ods = new OracleDataSource();

			// proprietatiile poolului
			Properties prop = new Properties();
			// numarul minim de conexiuni din pool
			prop.setProperty("MinLimit", "2");
			// numarul maxim de conexiuni din pool
			prop.setProperty("MaxLimit", "100");
			// cate secunde poate astepta sa ia o conexiune pana cand ia timeout
			// am pus 0 pentru ca conexiunea o iau sincronizat si daca sta sa
			// astepte
			// restul de threadul asteapta dupa el deci blocheaza tot serverul
			prop.setProperty("ConnectionReserveTimeoutSeconds", "0");
			// numarul de persoane ce pot astepta in acelasi timp
			// este 0 pentru ca daca sta 0 secunde la coada numai are rost sa
			// faca coada.
			prop.setProperty("HighestNumWaiters", "0");

			// proprietatile sursei de date;
			String url = "jdbc:oracle:thin:@localhost:1521:orcl";
			ods.setURL(url);
			ods.setUser("cash");
			ods.setPassword("aristotel1");
			ods.setConnectionCachingEnabled(true); // trebuie sa fie adevarat
			ods.setConnectionCacheProperties(prop);
			ods.setConnectionCacheName("ImplicitCache01"); // numele poolului

			/*
			 * Connection conn = DriverManager.getConnection(
			 * "jdbc:oracle:thin:@localhost:1521:orcl", "cash", "aristotel1");
			 */
			// Statement stmt = conn.createStatement();
			Serverul.getODS(ods);
			while (true) {
				cs = ss.accept();
				//sta maxim 15 secunde sa astepte pt orice citire
				cs.setSoTimeout(15000);
				
				System.out.println("primit");
				DataInputStream is = new DataInputStream(cs.getInputStream());
				DataOutputStream os = new DataOutputStream(cs.getOutputStream());

				new Serverul(is, os, cs).start();

			}
		} catch (Exception e) {
			System.out.println(e);
			System.exit(1);
		}
	}
}
