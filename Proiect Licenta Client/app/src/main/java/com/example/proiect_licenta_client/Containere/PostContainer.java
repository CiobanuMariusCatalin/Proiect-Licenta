package com.example.proiect_licenta_client.Containere;

import android.os.Parcel;
import android.os.Parcelable;

public class PostContainer implements Parcelable {
    public static final Parcelable.Creator<PostContainer> CREATOR = new Parcelable.Creator<PostContainer>() {
        public PostContainer createFromParcel(Parcel in) {
            return new PostContainer(in);
        }

        public PostContainer[] newArray(int size) {
            return new PostContainer[size];
        }
    };
    private int idPost;
    private String autor = "";
    private String text = "";
    private int idPoza=-1;
    private int pozaWidth;
    private int pozaHeight;
    private int idPozaProfil;
    private int pozaProfilWidth;
    private int pozaProfilHeight;
    //il folosesc sa stiu pe ce profil a fost scris mesajul
    private String newsFragmentProfil = null;
    private String data_postarii;

    public PostContainer() {

    }

    public PostContainer(int idPost, String autor, String text, int idPozaProfil,int pozaProfilWidth,int pozaProfilHeight, int idPoza,int pozaWidth,int pozaHeight,String data_postarii) {
        this.idPost = idPost;
        this.autor = autor;
        this.text = text;
        this.idPoza = idPoza;

        this.idPozaProfil = idPozaProfil;
        this.data_postarii=data_postarii;
        this.pozaProfilWidth=pozaProfilWidth;
        this.pozaProfilHeight=pozaProfilHeight;
        this.pozaWidth=pozaWidth;
        this.pozaHeight=pozaHeight;
    }

    public PostContainer(int idPost, String autor, String text,int idPozaProfil,int pozaProfilWidth,int pozaProfilHeight, String newsFragmentProfil, int idPoza,int pozaWidth,int pozaHeight,String data_postarii) {
        this.idPost = idPost;
        this.autor = autor;
        this.text = text;

        this.idPoza = idPoza;
        this.idPozaProfil = idPozaProfil;
        this.newsFragmentProfil = newsFragmentProfil;
        this.data_postarii=data_postarii;
        this.pozaProfilWidth=pozaProfilWidth;
        this.pozaProfilHeight=pozaProfilHeight;
        this.pozaWidth=pozaWidth;
        this.pozaHeight=pozaHeight;
    }

    public PostContainer(int idPost, String autor, String text, int idPozaProfil,int pozaProfilWidth,int pozaProfilHeight,String data_postarii) {
        this.idPost = idPost;
        this.autor = autor;
        this.text = text;

        this.idPozaProfil = idPozaProfil;
        this.data_postarii=data_postarii;
        this.pozaProfilWidth=pozaProfilWidth;
        this.pozaProfilHeight=pozaProfilHeight;
    }

    public PostContainer(int idPost, String autor, String text, int idPozaProfil,int pozaProfilWidth,int pozaProfilHeight, String newsFragmentProfil,String data_postarii) {
        this.idPost = idPost;
        this.autor = autor;
        this.text = text;

        this.idPozaProfil = idPozaProfil;
        this.newsFragmentProfil = newsFragmentProfil;
        this.data_postarii=data_postarii;
        this.pozaProfilWidth=pozaProfilWidth;
        this.pozaProfilHeight=pozaProfilHeight;
    }

    private PostContainer(Parcel in) {
        idPost = in.readInt();
        autor = in.readString();
        text = in.readString();

        String arePoza = in.readString();
        if (arePoza.equals("da")) {
            idPoza = in.readInt();
            pozaWidth=in.readInt();
            pozaHeight=in.readInt();
        }
        idPozaProfil = in.readInt();
        pozaProfilWidth=in.readInt();
        pozaProfilHeight=in.readInt();


        newsFragmentProfil = in.readString();
        data_postarii=in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(idPost);
        out.writeString(autor);
        out.writeString(text);
        if (idPoza == -1) out.writeString("nu");
        else {
            out.writeString("da");
            out.writeInt(idPoza);
            out.writeInt(pozaWidth);
            out.writeInt(pozaHeight);
        }
        out.writeInt(idPozaProfil);
        out.writeInt(pozaProfilWidth);
        out.writeInt(pozaProfilHeight);

        out.writeString(newsFragmentProfil);
        out.writeString(data_postarii);
    }

    //SETARI SI GETERI
    public int getIdPost() {
        return idPost;
    }

    public void setIdPost(int idPost) {
        this.idPost = idPost;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    public int getIdPoza() {
        return idPoza;
    }

    public void setIdPoza(int idPoza) {
        this.idPoza = idPoza;
    }
    public int getIdPozaProfil() {
        return idPozaProfil;
    }

    public void setIdPozaProfil(int idPozaProfil) {
        this.idPozaProfil = idPozaProfil;
    }

    public String getNewsFragmentProfil() {
        return newsFragmentProfil;
    }

    public void setNewsFragmentProfil(String newsFragmentProfil) {
        this.newsFragmentProfil = newsFragmentProfil;
    }
    public String getData_postarii(){return data_postarii;}
    public void setData_postarii(String data_postarii){this.data_postarii=data_postarii;}
    public int getPozaWidth(){return pozaWidth;}
    public void setPozaWidth(int pozaWidth){this.pozaWidth=pozaWidth;}
    public int getPozaHeight(){return pozaHeight;}
    public void setPozaHeight(int pozaHeight){this.pozaHeight=pozaHeight;}
    public int getPozaProfilWidth(){return pozaProfilWidth;}
    public void setPozaProfilWidth(int pozaProfilWidth){this.pozaProfilWidth=pozaProfilWidth;}
    public int getPozaProfilHeight(){return pozaProfilHeight;}
    public void setPozaProfilHeight(int pozaProfilHeight){this.pozaProfilHeight=pozaProfilHeight;}
}
