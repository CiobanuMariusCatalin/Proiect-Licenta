package com.example.proiect_licenta_client.Containere;

import android.os.Parcel;
import android.os.Parcelable;

public class IntereseContainer implements Parcelable {
    public static final Parcelable.Creator<IntereseContainer> CREATOR = new Parcelable.Creator<IntereseContainer>() {
        public IntereseContainer createFromParcel(Parcel in) {
            return new IntereseContainer(in);
        }

        public IntereseContainer[] newArray(int size) {
            return new IntereseContainer[size];
        }
    };
    private String nume;
    //lam pus string in loc de boolean pt ca parcable nu avea metoda sa scrie doar un boolean
    private String bifat;
    private int rating;
    public IntereseContainer(String nume,String bifat,int rating){
        this.nume=nume;
        this.bifat=bifat;
        this.rating=rating;
    }
    public IntereseContainer(String nume){
        this.nume=nume;
        bifat="NU";
        rating=0;
    }
    public IntereseContainer(){

    }
    private IntereseContainer(Parcel in) {

        nume = in.readString();
       bifat=in.readString();
        rating=in.readInt();

    }
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {

        out.writeString(nume);
      out.writeString(bifat);
        out.writeInt(rating);
    }
    //SETARI SI GETERI

    public String getNume(){
        return nume;
    }
    public void setNume(String nume){
        this.nume=nume;
    }
    public String getBifat(){return bifat;}
    public void setBifat(String bifat){this.bifat=bifat;}
    public int getRating(){return rating;}
    public void setRating(int rating){this.rating=rating;}
}
