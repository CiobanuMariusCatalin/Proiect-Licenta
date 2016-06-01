package com.example.proiect_licenta_client.Containere;

import android.os.Parcel;
import android.os.Parcelable;

public class FriendListContainer implements Parcelable {

    public static final Parcelable.Creator<FriendListContainer> CREATOR = new Parcelable.Creator<FriendListContainer>() {
        public FriendListContainer createFromParcel(Parcel in) {
            return new FriendListContainer(in);
        }

        public FriendListContainer[] newArray(int size) {
            return new FriendListContainer[size];
        }
    };
    private String nume;

    private int idPozaProfil;
    private int pozaProfilWidth;
    private int pozaProfilHeight;
    public FriendListContainer() {

    }

    public FriendListContainer(String nume, int idPozaProfil,int pozaProfilWidth,int pozaProfilHeight) {
        this.nume = nume;
        this.pozaProfilWidth=pozaProfilWidth;
        this.pozaProfilHeight=pozaProfilHeight;

        this.idPozaProfil = idPozaProfil;
    }

    private FriendListContainer(Parcel in) {

        nume = in.readString();
        idPozaProfil = in.readInt();
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
}
