package CREARE_BAZA_DE_DATE;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class addInterese {
	public static void main(String[] args) {
		String adresa = "localhost";

		int port = 505;
		Socket cs = null;
		DataOutputStream dos = null;
		DataInputStream dis = null;

		String[] interese = { "Aerobicul", "Animale de companie",
				"Animale exotice", "Animale marine", "Baschetul",
				"Blogging-ul", "Caini", "Calatoritul", "Cantatul",
				"Celebritati", "Cititul", "Colectionarea de obiecte",
				"Comedie stand-up", "Constructii", "Cusutul", "Dansul",
				"Desenatul", "Filmele SF", "Filmele de actiune",
				"Filmele de aventura", "Filmele de comedie",
				"Filmele documentar", "Filmele drama", "Filmele horror",
				"Filmele thriller", "Filmele", "Fitness-ul", "Fotbalul",
				"Fotografia", "Gatitul", "Gradinaritul", "Ingrijirea copiilor",
				"Jocuri", "Jocuri PS4", "Jocuri Pc", "Jocuri Wii",
				"Jocuri Xbox", "Jocuri de noroc", "Jogging-ul", "Jonglarea",
				"Limbi straine", "Magia", "Masini", "Mersul pe bicicleta",
				"Motociclete", "Muzee", "Muzica", "Muzica clasica",
				"Muzica dubstep", "Muzica heavy metal", "Muzica hip hop",
				"Muzica pop", "Muzica rock", "Patinatul", "Pictatul", "Pisici",
				"Plimbatul", "Progamatul", "Puzzle-uri", "Seriale", "Sportul",
				"Sporturi extreme", "Stiri", "Talk Show-uri", "Tehnologia",
				"Telefoane", "Televizorul", "Tenis de camp", "Tenis de masa",
				"Voluntariat", "Yoga" };

		String[] useri = { "Marius", "Ana", "Maria", "Oana", "Cristina",
				"Andreea", "Catalin", "Bogdan", "Vasile", "Mihai", "Cosmin",
				"Andrei", "Razvan", "Victor", "Radu", "Madalina" };

		// bag 3 interese luate la random
		for (int j = 0; j < 6; j++) {
			for (char i = 0; i <= useri.length; i++) {
				try {
					cs = new Socket(adresa, port);
					dos = new DataOutputStream(cs.getOutputStream());
					dis = new DataInputStream(cs.getInputStream());
					dos.writeUTF("addInteres");
					dos.writeUTF(useri[i]);
					// aleg un interes la intamplare
					int interesAles = ((int) (Math.random() * 10000))
							% interese.length;
					dos.writeUTF(interese[interesAles]);

					System.out.println("Userul " + useri[i] + " cu interesul "
							+ interese[interesAles]);
					cs.close();
					dis.close();
					dos.close();

				} catch (Exception e) {
					System.out.println(e + " AICI");

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
}
