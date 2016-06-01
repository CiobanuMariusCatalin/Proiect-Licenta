package com.example.proiect_licenta_client.Containere;


import android.os.Parcel;
import android.os.Parcelable;

public class FriendRecommendationContainer implements Parcelable {
    public static final Parcelable.Creator<FriendRecommendationContainer> CREATOR = new Parcelable.Creator<FriendRecommendationContainer>() {
        public FriendRecommendationContainer createFromParcel(Parcel in) {
            return new FriendRecommendationContainer(in);
        }

        public FriendRecommendationContainer[] newArray(int size) {
            return new FriendRecommendationContainer[size];
        }
    };
  private  String nume;

    private int idPozaProfil;
    private int pozaProfilWidth;
    private int pozaProfilHeight;
    private int prieteniInComun;
   public FriendRecommendationContainer() {

    }

public    FriendRecommendationContainer(String nume, int idPozaProfil,int pozaProfilWidth,int pozaProfilHeight,int prieteniInComun) {
        this.nume = nume;

        this.idPozaProfil=idPozaProfil;
    this.pozaProfilWidth=pozaProfilWidth;
    this.pozaProfilHeight=pozaProfilHeight;
this.prieteniInComun=prieteniInComun;
}

    private FriendRecommendationContainer(Parcel in) {

        nume = in.readString();
        idPozaProfil=in.readInt();
        pozaProfilWidth=in.readInt();
        pozaProfilHeight=in.readInt();
        prieteniInComun=in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {

        out.writeString(nume);
        out.writeInt(idPozaProfil);
        out.writeInt(pozaProfilWidth);
        out.writeInt(pozaProfilHeight);
        out.writeInt(prieteniInComun);
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
    public int getPrieteniInComun(){return prieteniInComun;}
    public void setPrieteniInComun(int prieteniInComun){this.prieteniInComun=prieteniInComun;}
}