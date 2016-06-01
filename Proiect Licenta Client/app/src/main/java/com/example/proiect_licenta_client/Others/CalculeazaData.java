package com.example.proiect_licenta_client.Others;


import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalculeazaData {
    public static String getData(String data_postarii){
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss", Locale.ENGLISH);
            Date data = format.parse(data_postarii);



            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();

            c1.setTime(format.parse(format.format(new Date())));
            c2.setTime(data);

            int yearDiff = c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);
            int monthDiff = c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
            int dayDiff = c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);
            int oraDiff=c1.get(Calendar.HOUR_OF_DAY) - c2.get(Calendar.HOUR_OF_DAY);
            int minDiff=  c1.get(Calendar.MINUTE) - c2.get(Calendar.MINUTE);
            int secondDiff= c1.get(Calendar.SECOND) - c2.get(Calendar.SECOND);

           /*  Toast.makeText(context,"timestpare are year"+c1.get(Calendar.YEAR)+
                     "luna"+c1.get(Calendar.MONTH)+"ziua"+c1.get(Calendar.DAY_OF_MONTH)+"minutul"+
                 c1.get(Calendar.MINUTE)+"secunda"+c1.get(Calendar.SECOND),Toast.LENGTH_SHORT).show();*/
         /*   Toast.makeText(context,"data postarii:"+data_postarii.toString()+"timestamp="+ timeStamp.toString()+"y="+yearDiff+"m"+monthDiff+"d"+dayDiff
                    +"min"+minDiff+"s"+secondDiff,Toast.LENGTH_SHORT).show();*/
            //daca sunt ani diferit afisez anul



            String oraRezultat;
            //daca sunt ani diferiti afisez doar anul in care a fost postat
            if( yearDiff!=0){
                oraRezultat=new SimpleDateFormat("dd MMM yyyy").format(c2.getTime());
            }else
                //daca este intr-o luna diferita afisez luna si ziua
                if(monthDiff!=0){
                    oraRezultat=new SimpleDateFormat("MMM dd").format(c2.getTime());
                }else
                    //daca este intr-o zii diferita afisez luna si ziua
                    if(dayDiff!=0){
                        //daca oraDiff <0 inseamna ca de exemplu a postat la 15:30 pe 1 februaria
                        //si vrea sa se afiseze pe 2 februarie la ora 10:00 mesajul daca se scad zilele
                        //va da diferenta de 1 zii dar nu e defapt , si daca scadem orele va fii diferenta negativa
                        //de aici vine conditia oraDiff<0 deci daca este diferenta de 1 zii dar nu 24 ore intre data cand a fost postat
                        //mesajul si cand vrea sa vada mesajul inseamna ca trebuie sa afisez numarul de ore si daca adun diferenta cu inca 23 de ore
                        //capat asta

                        if(dayDiff==1&&oraDiff<0){
                            oraRezultat="acum "+(oraDiff+23)+"ore";
                        }
                        //daca este diferenta mai mult de 2 zile afisez data exacta deci numai trebuie sa calculez nimic
                        else
                            oraRezultat=new SimpleDateFormat("MMM dd").format(c2.getTime());
                    }else
                        //daca au trecut ore de la mesaj afisez numarul de ore trecute de cand a dat mesajul
                        if( oraDiff!=0){
                            //aceasi poveste si cu ore si minute ca la ore si zile.Daca sa zicem a fost postat mesajul la
                            //ora 15:30 si vrea sa se verifice la ora 16:10 daca se scad doare orele va fii oraDiff==1 dar
                            //nu este defapt diferenta de ora , in cazul acesta scazand 10-30 va fii diferenta negativa si
                            //stiu sigur ca nu sa facut inca o ora , daca adun diferenta cu echivalentul a unei ore anume 60 de minute
                            //voi avea adevarata distanta intre timpul cand a fost postat mesajul si cand se vizioneaza el

                            if(oraDiff==1&&minDiff<0){
                                oraRezultat="acum "+(minDiff+60)+" min";
                            }else
                            //daca diferenta este de 1 si minDiff este peste 0 inseamna ca chiar au trecut 60 de minute
                            if(oraDiff==1&&minDiff>=0){
                                oraRezultat="acum o ora";
                                //daca sa zicem avem ora postarii 15:30 si verificam la ora 18:00 din diferenta orele va rezultaa
                                //ca au trecut 3 ore dar defapt au trecut doar 2 jumate si pentru ca rotunjesc ar fii trebuit sa afisez decat 2ore
                                //asa ca la fel ca mai sus daca diferenta minutelor este negativa inseamna ca nu au trecut 60 de minute din ora curenta
                                //asa ca din rezultatul ce mi-ai fii iesit mai scad 1 ora .
                            }else
                            if(oraDiff>1 &&minDiff<0) {
                                if (oraDiff == 2)
                                    oraRezultat = "acum " + (oraDiff - 1) + " ora";
                                else oraRezultat = "acum " + (oraDiff - 1) + " ore";
                            }
                            else{
                                oraRezultat="acum "+oraDiff+" ore";
                            }

                        }else
                            //daca au trecut minute de cand a postat mesajul afisez numarul de minute
                            if(minDiff!=0){
                                //la fel ca mai sus
                                if( minDiff==1&&secondDiff<0) oraRezultat="acum "+(secondDiff+60)+ " s";
                                else
                                if(minDiff>1&&secondDiff<0)oraRezultat="acum "+(minDiff-1)+ " min";
                                    else
                                    oraRezultat="acum "+minDiff+ " min";

                            }else
                            if(secondDiff!=0){
                                //la fel ca mai sus
                                if( secondDiff<0)   oraRezultat="acum "+(secondDiff+60)+" s";
                                else oraRezultat="acum "+secondDiff+" s";
                            }else oraRezultat="acum";
            return oraRezultat;
        }catch(Exception e){

            return "eroare"+e;
        }

    }
}
