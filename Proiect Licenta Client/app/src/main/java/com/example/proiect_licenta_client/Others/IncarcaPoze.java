package com.example.proiect_licenta_client.Others;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class IncarcaPoze {
    private static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context con) {
        context = con;
    }

    public static File getSavePath() {
        File cale;
        if (hasSDCard()) { // SD card
            cale = new File(context.getExternalCacheDir() + "/LicentaCache/");
            cale.mkdir();
        } else {
            cale = Environment.getDataDirectory();
        }
        return cale;
    }

    public static String getCacheFilename(String nume) {
        File f = getSavePath();
        return f.getAbsolutePath() + "/" + nume + ".png";
    }

    public static Bitmap loadFromFile(String filename) {
        try {
            File f = new File(filename);
            if (!f.exists()) {
                return null;
            }
            Bitmap tmp = BitmapFactory.decodeFile(filename);
            return tmp;
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap loadFromCacheFile(String nume) {
        return loadFromFile(getCacheFilename(nume));
    }

    public static void saveToCacheFile(Bitmap bmp, String nume) {
        saveToFile(getCacheFilename(nume), bmp);
    }

    public static void saveToFile(String filename, Bitmap bmp) {
        try {
            FileOutputStream out = new FileOutputStream(filename);
            //100 inseamna ca pastreaza calitatea imaginii 100%
            bmp.compress(CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
        }
    }

    //daca are sd
    public static boolean hasSDCard() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    // imi da pathul catre sd sa pun poze in el.Nu cel de cache
    public static String getSDCardPath() {
        File path = Environment.getExternalStorageDirectory();
        return path.getAbsolutePath();
    }

    //e statica ca sa pot face o clasa de acest tip si din afara fara sa am nevoie de un obiect
    public static class LoadImage extends AsyncTask<Void, Void, Bitmap> {
        private DataInputStream dis;
        private DataOutputStream dos;
        private ImageView imv;
        private String idPoza;
        private boolean incarcat = false;
        private int new_width;
        private int new_height;
        private Context context;
        private static double idContor;
        private double idCurent;
        private String comanda = null;
        private ArrayList<LoadImage> pozeDeIncarcat;


        public LoadImage(ImageView imv, Context context, String idPoza, int new_width, int new_height, String comanda,ArrayList<LoadImage> pozeDeIncarcat) {
            this.imv = imv;
            this.idPoza = idPoza;
            this.new_width = new_width;
            this.new_height = new_height;
            this.idPoza = idPoza;
            this.comanda = comanda;
            this.context = context;
            this.pozeDeIncarcat=pozeDeIncarcat;
        }
        @Override
        protected void onPreExecute() {
            //in caz ca ajung la valoarea maxima o resetez
            if(idContor==Double.MAX_VALUE) {
                idContor=0;
                idCurent = 0;
            }
            else
            idCurent=idContor++;

            pozeDeIncarcat.add(this);

        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap bitmap = null;

            if (comanda.equals("getPozaById")) {

                try {
                    bitmap = IncarcaPoze.loadFromCacheFile(idPoza + "");
                    if (bitmap == null) {

                    } else incarcat = true;
                } catch (Exception e) {
                    System.out.println(e);

                }

            }
            if (comanda.equals("dwlImageFromServer")) {
                String adresa = Constante.adresa;
                Socket cs = null;
                int port = 505;
                //PozaProfil
                try {
                    //mai verific odata si aici ca poate a fost incarcata poza pana cand am ajuns aici
                    //inca exista o sansa sa se incarce poza de 2 ori dar nu e nici o problema
                    //pentru ca va fii suprascrisa
                    bitmap = IncarcaPoze.loadFromCacheFile(idPoza + "");
                    if (bitmap == null) {
                        cs = new Socket(adresa, port);
                        dos = new DataOutputStream(cs.getOutputStream());
                        dis = new DataInputStream(cs.getInputStream());

                        dos.writeUTF("dwlImageFromServer");


                        if (idPoza.charAt(0) == 'P') {
                            //al doilea caracter este o adica este "PozaProfil"
                            if(idPoza.charAt(1)=='o')
                                //PozaProfil
                            dos.writeInt(Integer.parseInt(idPoza.substring(10)));
                                //PaginaProfil
                            //folosesc Pozele cu id Paginaprofil sa le pun
                            else dos.writeInt(Integer.parseInt(idPoza.substring(12)));
                        } else dos.writeInt(Integer.parseInt(idPoza));

                        int dim = dis.readInt();
                        byte[] temp = new byte[dim];

                        dis.readFully(temp, 0, dim);

                        bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(temp, 0, temp.length), new_width, new_height, false);
                        IncarcaPoze.saveToCacheFile(bitmap, idPoza + "");

                    }
                } catch (Exception e) {

                    return null;
                }

            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            for (int i = 0; i < pozeDeIncarcat.size(); i++) {
                if (pozeDeIncarcat.get(i).idCurent == this.idCurent) {
                  /* Toast.makeText(context, "Sters "+idCurent, Toast.LENGTH_SHORT)
                            .show();*/
                    pozeDeIncarcat.remove(i);
                }
            }
            if (comanda.equals("getPozaById")) {

                if (incarcat == false) {
                    new LoadImage(imv, context, idPoza, new_width, new_height, "dwlImageFromServer",pozeDeIncarcat).execute();
                } else {
                  if(imv!=null)  imv.setImageBitmap(result);
                }
            }
            if (comanda.equals("dwlImageFromServer")) {
                if(imv!=null)imv.setImageBitmap(result);
            }

        }

    }

}
