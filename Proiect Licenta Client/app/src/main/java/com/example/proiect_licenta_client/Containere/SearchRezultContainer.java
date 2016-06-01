package com.example.proiect_licenta_client.Containere;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchRezultContainer implements Parcelable {

   private String nume;

    private int idPozaProfil;
    private int pozaProfilWidth;
    private int pozaProfilHeight;


   public SearchRezultContainer() {

    }

   public SearchRezultContainer(String nume,int idPozaProfil,int pozaProfilWidth,int pozaProfilHeight) {
        this.nume = nume;

        this.idPozaProfil=idPozaProfil;
       this.pozaProfilWidth=pozaProfilWidth;
       this.pozaProfilHeight=pozaProfilHeight;
    }

    private SearchRezultContainer(Parcel in) {

        nume = in.readString();

        idPozaProfil=in.readInt();
        pozaProfilWidth=in.readInt();
        pozaProfilHeight=in.readInt();

    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {

        out.writeString(nume);

        out.writeInt(idPozaProfil);
        out.writeInt(pozaProfilWidth);
        out.writeInt(pozaProfilHeight);
    }
    public static final Parcelable.Creator<SearchRezultContainer> CREATOR = new Parcelable.Creator<SearchRezultContainer>() {
        public SearchRezultContainer createFromParcel(Parcel in) {
            return new SearchRezultContainer(in);
        }

        public SearchRezultContainer[] newArray(int size) {
            return new SearchRezultContainer[size];
        }
    };

    //SETARI SI GETERI

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
    public int getPozaProfilWidth(){return pozaProfilWidth;}
    public void setPozaProfilWidth(int pozaProfilWidth){this.pozaProfilWidth=pozaProfilWidth;}
    public int getPozaProfilHeight(){return pozaProfilHeight;}
    public void setPozaProfilHeight(int pozaProfilHeight){this.pozaProfilHeight=pozaProfilHeight;}

}

