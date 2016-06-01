package TestBazaDeDate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;



public class Main {
	static Statement s;
	static void  adaugareInBDTest1(int i){
		try {
			s.executeUpdate("INSERT INTO test1 VALUES(secventaTest1.NEXTVAL,"+i+",\'VASILE\',\'ION\')");

		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void main(String[] args) {

		try {


			 Connection conn = DriverManager
			 .getConnection("jdbc:oracle:thin:@localhost:1521:orcl","cash","aristotel1");
		
			
			
			Main.s = conn.createStatement();
			int i=1;
			while(i<Integer.MAX_VALUE){
				Main.adaugareInBDTest1(i);
				System.out.println(i);
				i++;
			}
			
		} catch (Exception e) {
			System.out.println(e);
		}

	}
}
