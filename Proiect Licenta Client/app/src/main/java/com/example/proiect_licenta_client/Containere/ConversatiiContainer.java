package com.example.proiect_licenta_client.Containere;


import android.os.Parcel;
import android.os.Parcelable;

public class ConversatiiContainer implements Parcelable {


    private String nume;
    private String cineATrimisUltimulMesaj;
    private int idPozaProfil;
    private int pozaProfilWidth;
    private int pozaProfilHeight;
    private String dataUltimuluiMesaj;
    private String textul;
    private String mesajNou;
    public ConversatiiContainer() {

    }

    public ConversatiiContainer(String nume, int idPozaProfil,int pozaProfilWidth,int pozaProfilHeight,String dataUltimuluiMesaj,String textul,String cineATrimisUltimulMesaj,String mesajNou) {
        this.nume = nume;
        this.idPozaProfil = idPozaProfil;
        this.pozaProfilWidth=pozaProfilWidth;
        this.pozaProfilHeight=pozaProfilHeight;
        this.dataUltimuluiMesaj=dataUltimuluiMesaj;
        this.textul=textul;
        this.cineATrimisUltimulMesaj=cineATrimisUltimulMesaj;
        this.mesajNou=mesajNou;
    }



    private ConversatiiContainer(Parcel in) {

        nume = in.readString();
        idPozaProfil = in.readInt();
        pozaProfilWidth=in.readInt();
        pozaProfilHeight=in.readInt();
        dataUltimuluiMesaj=in.readString();
        textul=in.readString();
        cineATrimisUltimulMesaj=in.readString();
        mesajNou=in.readString();
    }
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {

        out.writeString(nume);
        out.writeInt(idPozaProfil);
        out.writeInt(pozaProfilWidth);
        out.writeInt(pozaProfilHeight);
        out.writeString(dataUltimuluiMesaj);
        out.writeString(textul);
        out.writeString(cineATrimisUltimulMesaj);
        out.writeString(mesajNou);
    }



    public static final Parcelable.Creator< ConversatiiContainer> CREATOR = new Parcelable.Creator< ConversatiiContainer>() {
        public  ConversatiiContainer createFromParcel(Parcel in) {
            return new  ConversatiiContainer(in);
        }

        public  ConversatiiContainer[] newArray(int size) {
            return new  ConversatiiContainer[size];
        }
    };

    //SETARI SI GETERI

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }


    public int getIdPozaProfil() {
        return idPozaProfil;
    }

    public void setIdPozaProfil(int idPozaProfil) {
        this.idPozaProfil = idPozaProfil;
    }
    public int getPozaProfilWidth(){return pozaProfilWidth;}
    public void setPozaProfilWidth(int pozaProfilWidth){this.pozaProfilWidth=pozaProfilWidth;}
    public int getPozaProfilHeight(){return pozaProfilHeight;}
    public void setPozaProfilHeight(int pozaProfilHeight){this.pozaProfilHeight=pozaProfilHeight;}
    public String getDataUltimuluiMesaj(){return dataUltimuluiMesaj;}
    public void setDataUltimuluiMesaj(String dataUltimuluiMesaj){this.dataUltimuluiMesaj=dataUltimuluiMesaj;}
    public String getTextul(){return  textul;}
    public void setTextul(String textul){this.textul=textul;}
    public String getcineATrimisUltimulMesaj(){return  cineATrimisUltimulMesaj;}
    public void setcineATrimisUltimulMesaj(String cineATrimisUltimulMesaj){this.cineATrimisUltimulMesaj=cineATrimisUltimulMesaj;}

    public String getMesajNou() {
        return mesajNou;
    }

    public void setMesajNou(String mesajNou) {
        this.mesajNou = mesajNou;
    }
}
