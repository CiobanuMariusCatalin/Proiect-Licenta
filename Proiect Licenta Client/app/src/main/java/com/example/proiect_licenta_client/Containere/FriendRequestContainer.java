package com.example.proiect_licenta_client.Containere;


import android.os.Parcel;
import android.os.Parcelable;

public class FriendRequestContainer implements Parcelable {
    public static final Parcelable.Creator<FriendRequestContainer> CREATOR = new Parcelable.Creator<FriendRequestContainer>() {
        public FriendRequestContainer createFromParcel(Parcel in) {
            return new FriendRequestContainer(in);
        }

        public FriendRequestContainer[] newArray(int size) {
            return new FriendRequestContainer[size];
        }
    };
   private String nume;

    private  int idPozaProfil;
    private int pozaProfilWidth;
    private int pozaProfilHeight;
  public  FriendRequestContainer() {

    }

    public FriendRequestContainer(String nume, int idPozaProfil,int pozaProfilWidth,int pozaProfilHeight) {
        this.nume = nume;

        this.idPozaProfil=idPozaProfil;
        this.pozaProfilWidth=pozaProfilWidth;
        this.pozaProfilHeight=pozaProfilHeight;
    }

    private FriendRequestContainer(Parcel in) {

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

