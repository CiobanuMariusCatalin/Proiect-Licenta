package com.example.proiect_licenta_client.Pagini;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.proiect_licenta_client.Others.ConexiuneLaInternet;
import com.example.proiect_licenta_client.Others.Constante;
import com.example.proiect_licenta_client.R;

public class PaginaSignUp extends Activity {
    private SendTask task;



    public void SignUp(View view) {


        EditText et = (EditText) findViewById(R.id.paginaSignUpUser);
        String cont = et.getText().toString();
        EditText et2 = (EditText) findViewById(R.id.paginaSignUpParola);
        String parola = et2.getText().toString();
        EditText et3 = (EditText) findViewById(R.id.paginaSignUpParolaConfirmare);
        String confirmareParola = et3.getText().toString();

        if (cont.equals("") || parola.equals("") || confirmareParola.equals("")) {
            int duration = Toast.LENGTH_LONG;
            String text = "Parola si contul trebuie sa aiba cel putin 1 caracter fiecare";
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, text, duration);

            toast.show();
            return;
        }


        if (!parola.equals(confirmareParola)) {
            TextView tv = (TextView) findViewById(R.id.PaginaSignUpEroareParola);
            tv.setText("Parolele nu se potrivesc");
            et2.setText("");
            et3.setText("");

        } else {
            if(cont.contains("'")||parola.contains("'")||confirmareParola.contains("'")) {
                int duration = Toast.LENGTH_LONG;
                String text = "Caracterul ' nu este permis";
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }else {
                task = new SendTask("signUp", cont, parola);
                task.execute();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (task != null)
            task.cancel(true);

    }

    /*
        <SeekBar android:id="@+id/seekBar1"
        android:max="100"
        android:layout_weight="1"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
 ></SeekBar>
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pagina_sign_up);
        setTitle("Pagina pentru inregistrare");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu2, menu);
        return true;
    }

    ;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private  class SendTask extends AsyncTask<Void, Void, Void> {
        private DataInputStream dis;
        private DataOutputStream dos;
        private String comanda = "";
        private int n;
        private boolean eroare = false;
        private String textEroare;
        private String mesaj;
        private String mesaj2;
        private String raspuns;
        private Context context = getApplicationContext();
        private CharSequence text;
        private int duration = Toast.LENGTH_LONG;
        private Toast toast;

        SendTask(String comanda, String cont, String parola) {
            this.comanda = comanda;
            mesaj = cont;
            mesaj2 = parola;
        }

        protected Void doInBackground(Void... arg0) {
            int port = 505;
            String adresa = Constante.adresa;
            Socket cs = null;
            context = getApplicationContext();
            duration = Toast.LENGTH_SHORT;
            if (ConexiuneLaInternet.conexiuneLaInternet(getApplicationContext()) == false) {
                eroare = true;
                textEroare = "Nu exista conexiune la internet";
                return null;
            }
            try {
                cs = new Socket(adresa, port);
                dos = new DataOutputStream(cs.getOutputStream());
                dis = new DataInputStream(cs.getInputStream());
                if (comanda.equals("signUp")) {
                    dos.writeUTF("signUp");
                    dos.writeUTF(mesaj);
                    dos.writeUTF(mesaj2);
                    raspuns = dis.readUTF();
                }
              /*  try { Thread.sleep(3000); } catch (InterruptedException e) {

                    e.printStackTrace();}*/
                dis.close();
                dos.close();
                cs.close();
            } catch (ConnectException ce) {
                eroare = true;
                textEroare = "A aparut o eroare, va rugam mai incercati odata";
                return null;
            } catch (Exception e) {
                eroare = true;
                textEroare = e.toString();
                return null;

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (eroare == true) {
                text = textEroare;
                toast = Toast.makeText(context, text, duration);

                toast.show();
                //cand luam eroare sa putem apasa iar pe buton
                Button bt = (Button) findViewById(R.id.paginaSignUpButtonSignUp);
                bt.setClickable(true);
                return;
            }
            if (comanda.equals("signUp")) {
                if (raspuns.equals("succes")) {
                    // contextul al aplicatiei nu doar activitatea curenta
                    text = "Cont creat";
                    toast = Toast.makeText(context, text, duration);

                    toast.show();


                    finish();
                } else if (raspuns.equals("esec")) {
                    text = "Numele este deja in folosinta";
                    toast = Toast.makeText(context, text, duration);

                    toast.show();

                } else {
                    text = "other";
                    toast = Toast.makeText(context, text, duration);

                    toast.show();
                }

            }

            super.onPostExecute(result);
        }
    }

}
