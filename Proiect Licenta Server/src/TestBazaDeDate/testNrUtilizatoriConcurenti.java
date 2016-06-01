package TestBazaDeDate;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;

public class testNrUtilizatoriConcurenti extends Thread {
	static Object o = new Object();

	public void run() {
		PrintWriter print = null;
		try {

			String adresa = "localhost";
			int port = 505;
			Socket cs = null;
			DataOutputStream dos = null;
			DataInputStream dis = null;
			cs = new Socket(adresa, port);
			dos = new DataOutputStream(cs.getOutputStream());
			dis = new DataInputStream(cs.getInputStream());
			/*
			 * // login dos.writeUTF("login"); dos.writeUTF("a");
			 * dos.writeUTF("a"); dis.readUTF();
			 */
			// startPaginaProfil

			dos.writeUTF("startPaginaProfil");
			dos.writeUTF("a");
			dos.writeUTF("a");

			boolean profilExistent = dis.readBoolean();
			if (profilExistent) {
				int n = dis.readInt();
				for (int i = 0; i < n; i++) {
					dis.readInt();
					dis.readUTF();
					dis.readUTF();
					dis.readInt();
					int dimPozaProfil = dis.readInt();
					byte[] temp = new byte[dimPozaProfil];
					dis.readFully(temp, 0, dimPozaProfil);
					String temp2 = dis.readUTF();
					if (temp2.equals("arePoza")) {
						dis.readInt();
						int dim = dis.readInt();
						temp = new byte[dim];
						dis.readFully(temp, 0, dim);
					}
				}
			}
			synchronized (o) {

				print = new PrintWriter(new BufferedWriter(new FileWriter(
						"output.txt", true)));
				print.println("succes");
				print.close();

			}
		} catch (Exception e) {
			synchronized (o) {
				System.out.println(e);
				if (print != null) {
					print.println(e);
					print.close();
				} else {
					try {
						print = new PrintWriter(new BufferedWriter(
								new FileWriter("output.txt", true)));
					} catch (IOException e1) {

						e1.printStackTrace();
					}
					print.println(e);
					print.close();
				}
			}

		}
	}

	public static void main(String[] args) {

		int i = 0;
		while (i <100) {
			testNrUtilizatoriConcurenti thread = new testNrUtilizatoriConcurenti();
			thread.start();
			i++;
		}

	}
}
