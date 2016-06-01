package com.example.proiect_licenta_client.Containere;


import android.os.Parcel;
import android.os.Parcelable;

public class MesajeContainer implements Parcelable {

    public static final Parcelable.Creator<MesajeContainer> CREATOR = new Parcelable.Creator<MesajeContainer>() {
        public MesajeContainer createFromParcel(Parcel in) {
            return new MesajeContainer(in);
        }

        public MesajeContainer[] newArray(int size) {
            return new MesajeContainer[size];
        }
    };
  private  int idMesaj;;
  private  String nume = "";
  private  String text = "";

  private String data_postarii;

   public MesajeContainer() {

    }

   public MesajeContainer(String nume, String text,int idMesaj,String data_postarii) {
        this.idMesaj=idMesaj;
        this.nume = nume;
        this.text = text;


       this.data_postarii=data_postarii;
    }


    private MesajeContainer(Parcel in) {
        idMesaj=in.readInt();
        nume = in.readString();
        text = in.readString();


        data_postarii=in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(idMesaj);
        out.writeString(nume);
        out.writeString(text);

        out.writeString(data_postarii);

    }

    //SETARI SI GETERI
    public int getIdMesaj(){
        return idMesaj;
    }
    public void setIdMesaj(int idMesaj){
        this.idMesaj=idMesaj;
    }
    public String getNume(){
        return nume;
    }
    public void setNume(String nume){
        this.nume=nume;
    }
    public String getText(){
        return text;
    }
    public void setText(String text){
        this.text=text;
    }

    public String getData_postarii(){return data_postarii;}
    public void setData_postarii(String data_postarii){this.data_postarii=data_postarii;}
}
