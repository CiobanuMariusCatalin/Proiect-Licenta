package com.example.proiect_licenta_client.Containere;

import android.os.Parcel;
import android.os.Parcelable;



public class ArhivaPrieteniTemporariContainer implements Parcelable {
   private String nume;


    private int idPozaProfil;
    private int pozaProfilWidth;
    private int pozaProfilHeight;
    private String data_adaugarii;
    private String data_terminarii;
    public ArhivaPrieteniTemporariContainer() {

    }

    public ArhivaPrieteniTemporariContainer(String nume,int idPozaProfil,int pozaProfilWidth,int pozaProfilHeight,String data_adaugarii,String data_terminarii) {
        this.nume = nume;
        this.pozaProfilWidth=pozaProfilWidth;
        this.pozaProfilHeight=pozaProfilHeight;

        this.idPozaProfil=idPozaProfil;
        this.data_adaugarii=data_adaugarii;
        this.data_terminarii=data_terminarii;
    }

    private ArhivaPrieteniTemporariContainer(Parcel in) {

        nume = in.readString();
        idPozaProfil=in.readInt();
        pozaProfilWidth=in.readInt();
        pozaProfilHeight=in.readInt();
        data_adaugarii=in.readString();
        data_terminarii=in.readString();

    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {

        out.writeString(nume);
        out.writeInt(idPozaProfil);
        out.writeInt(pozaProfilWidth);
        out.writeInt(pozaProfilHeight);
        out.writeString(data_adaugarii);
        out.writeString(data_terminarii);
    }
    public static final Parcelable.Creator<ArhivaPrieteniTemporariContainer> CREATOR = new Parcelable.Creator<ArhivaPrieteniTemporariContainer>() {
        public ArhivaPrieteniTemporariContainer createFromParcel(Parcel in) {
            return new ArhivaPrieteniTemporariContainer(in);
        }

        public ArhivaPrieteniTemporariContainer[] newArray(int size) {
            return new ArhivaPrieteniTemporariContainer[size];
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
    public String getData_adaugarii(){return data_adaugarii;}
    public void setData_adaugarii(String data_adaugarii){this.data_adaugarii=data_adaugarii;}
    public String getData_terminarii(){return data_terminarii;}
    public void setData_terminarii(String data_terminarii){this.data_terminarii=data_terminarii;}
}
