package Others;

import java.util.ArrayList;
import java.util.Collections;

public class OrdonareaIntereselor {

	public static void main(String[] args){
		String[] interese= {"Aerobicul","Animale de companie","Animale exotice","Animale marine","Baschetul","Blogging-ul",
				"Caini","Calatoritul","Cantatul","Celebritati","Cititul","Colectionarea de obiecte","Comedie stand-up","Constructii",
				"Cusutul","Dansul","Desenatul","Filmele SF","Filmele de actiune","Filmele de aventura","Filmele de comedie","Filmele documentar",
				"Filmele drama","Filmele horror","Filmele thriller","Filmele","Fitness-ul","Fotbalul","Fotografia","Gatitul","Gradinaritul",
				"Ingrijirea copiilor","Jocuri","Jocuri PS4","Jocuri Pc","Jocuri Wii","Jocuri Xbox","Jocuri de noroc","Jogging-ul","Jonglarea",
				"Limbi straine","Magia","Masini","Mersul pe bicicleta","Motociclete","Muzee","Muzica","Muzica clasica","Muzica dubstep",
				"Muzica heavy metal","Muzica hip hop","Muzica pop","Muzica rock","Patinatul","Pictatul","Pisici","Plimbatul","Progamatul",
				"Puzzle-uri","Seriale","Sportul","Sporturi extreme","Stiri","Talk Show-uri","Tehnologia","Telefoane","Televizorul","Tenis de camp",
				"Tenis de masa","Voluntariat","Yoga"};

		ArrayList<String> test=new ArrayList<>();
		for(int i=0;i<interese.length;i++){
			test.add(interese[i]);
		}
		Collections.sort(test);
		System.out.print("{");
		for(int i=0;i<test.size();i++){
			if(i<test.size()-1)System.out.print("\""+test.get(i)+"\",");
			else System.out.print("\""+test.get(i)+"\"}");
		}
	}
}
