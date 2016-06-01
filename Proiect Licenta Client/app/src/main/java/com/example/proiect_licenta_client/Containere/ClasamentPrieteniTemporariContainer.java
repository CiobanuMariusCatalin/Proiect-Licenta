package com.example.proiect_licenta_client.Containere;


import android.os.Parcel;
import android.os.Parcelable;

public class ClasamentPrieteniTemporariContainer implements Parcelable {
    private String nume;
    private int pozaProfilWidth;
    private int pozaProfilHeight;
    private int idPozaProfil;
    private int puncte;

    public ClasamentPrieteniTemporariContainer(String nume,int idPozaProfil,int pozaProfilWidth,int pozaProfilHeight,int puncte) {
        this.nume = nume;

        this.pozaProfilWidth=pozaProfilWidth;
        this.pozaProfilHeight=pozaProfilHeight;
        this.idPozaProfil=idPozaProfil;
        this.puncte=puncte;
    }
    public ClasamentPrieteniTemporariContainer(){

    }
    public int describeContents() {
        return 0;
    }
    private ClasamentPrieteniTemporariContainer(Parcel in) {

        nume = in.readString();
        idPozaProfil=in.readInt();
        pozaProfilWidth=in.readInt();
        pozaProfilHeight=in.readInt();
        puncte=in.readInt();

    }
    public void writeToParcel(Parcel out, int flags) {

        out.writeString(nume);
        out.writeInt(idPozaProfil);
        out.writeInt(pozaProfilWidth);
        out.writeInt(pozaProfilHeight);
        out.writeInt(puncte);
    }
    public static final Parcelable.Creator<ClasamentPrieteniTemporariContainer> CREATOR = new Parcelable.Creator<ClasamentPrieteniTemporariContainer>() {
        public ClasamentPrieteniTemporariContainer createFromParcel(Parcel in) {
            return new ClasamentPrieteniTemporariContainer(in);
        }

        public ClasamentPrieteniTemporariContainer[] newArray(int size) {
            return new ClasamentPrieteniTemporariContainer[size];
        }
    };
    public String getNume(){
        return nume;
    }
    public void setNume(String nume){
        this.nume=nume;
    }

    public int getIdPozaProfil(){
        return idPozaProfil;
    }
    public void setIdPozaProfil(int idPozaProfil){
        this.idPozaProfil=idPozaProfil;
    }
    public int getPuncte(){return puncte;}
    public void setPuncte(int puncte){this.puncte=puncte;}
    public int getPozaProfilWidth(){return pozaProfilWidth;}
    public void setPozaProfilWidth(int pozaProfilWidth){this.pozaProfilWidth=pozaProfilWidth;}
    public int getPozaProfilHeight(){return pozaProfilHeight;}
    public void setPozaProfilHeight(int pozaProfilHeight){this.pozaProfilHeight=pozaProfilHeight;}
}
