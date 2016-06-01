package com.example.proiect_licenta_client.Pagini;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.example.proiect_licenta_client.Others.ConexiuneLaInternet;
import com.example.proiect_licenta_client.Others.Constante;
import com.example.proiect_licenta_client.Others.IncarcaPoze;
import com.example.proiect_licenta_client.R;

public class PaginaLogin extends Activity {
    private SendTask task;


    //blochez si debloghez butoanele pentru a nu putea deschide de mai multe ori pagina principala si pagian de sign up
    //pentru ca nu ar trebuii sa se intample asta. blochez cand apas un buton  , si deblochez daca sunt date incorect sau nu
    //este acceptat userul altfel deblochez cand se intoarce in activitate anume in onResume
public void blocheazaButonale(){
    Button bt1=(Button) findViewById(R.id.paginaLoginButtonLogin);
    Button bt2=(Button) findViewById(R.id.paginaLoginButtonSignUp);
    bt1.setClickable(false);
    bt2.setClickable(false);
}
    public void deblocheazaButonale(){
        Button bt1=(Button) findViewById(R.id.paginaLoginButtonLogin);
        Button bt2=(Button) findViewById(R.id.paginaLoginButtonSignUp);
        bt1.setClickable(true);
        bt2.setClickable(true);
    }
    public void login(View view) {
        blocheazaButonale();
        EditText et = (EditText) findViewById(R.id.paginaLoginUser);
        String cont = et.getText().toString();
        EditText et2 = (EditText) findViewById(R.id.paginaLoginParola);
        String parola = et2.getText().toString();
        if (!cont.equals("") && !parola.equals("")) {
            if(cont.contains("'")||parola.contains("'")){
                int duration = Toast.LENGTH_LONG;
                String text = "Caracterul ' nu este permis";
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();


                deblocheazaButonale();
            }else {
                task = new SendTask("login", cont, parola);
                task.execute();
            }
        } else {
            deblocheazaButonale();
            int duration = Toast.LENGTH_LONG;
            String text = "Parola si contul trebuie sa aiba cel putin 1 caracter fiecare";
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, text, duration);

            toast.show();
        }
    }

    public void SignUp(View view) {
        blocheazaButonale();
        Intent intent = new Intent(this, PaginaSignUp.class);
        startActivity(intent);
    }

    public void startHomePage() {
        Intent intent = new Intent(this, PaginaPrincipala.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (task != null)
            task.cancel(true);

    }
    @Override
    protected void onResume() {
        super.onResume();
        deblocheazaButonale();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pagina_login);
        setTitle("Pagina pentru autentificare");

        SharedPreferences settings = getSharedPreferences("login", MODE_PRIVATE);

        if(settings.contains("cont")){
            startHomePage();
        }
        //setez contextul pentru clasa ce imi imi incarca poze in memorie si care
        //imi da pozele din memorie
        IncarcaPoze.setContext(getApplicationContext());
/*        //initiez pozele in memorie ram
        if(MemorarePozeInRamCache.mMemoryCache==null){
            MemorarePozeInRamCache.initiere();
        }*/

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

    private class SendTask extends AsyncTask<Void, Void, Void> {
        private DataInputStream dis;
        private DataOutputStream dos;
        private String comanda = "";

        private String mesaj = "";
        private String mesaj2 = "";
        private String raspuns = "";
        private boolean eroare = false;
        private String textEroare = "";
        private Context context = getApplicationContext();
        private CharSequence text;
        private int duration = Toast.LENGTH_LONG;
        private Toast toast;

        SendTask(String comanda, String mesaj, String mesaj2) {
            this.comanda = comanda;
            this.mesaj = mesaj;
            this.mesaj2 = mesaj2;
        }

        protected Void doInBackground(Void... arg0) {
            int port = 505;
            // String adresa2 = "10.0.2.2";
            String adresa = Constante.adresa;
            Socket cs = null;
            if (ConexiuneLaInternet.conexiuneLaInternet(getApplicationContext()) == false) {
                eroare = true;
                textEroare = "Nu exista conexiune la internet";
                return null;
            }
            try {
                cs = new Socket(adresa, port);
                dos = new DataOutputStream(cs.getOutputStream());
                dis = new DataInputStream(cs.getInputStream());
                if (comanda.equals("login")) {
                    dos.writeUTF("login");
                    dos.writeUTF(mesaj);
                    dos.writeUTF(mesaj2);
                    raspuns = dis.readUTF();
                }
              /*  try { Thread.sleep(3000); } catch (InterruptedException e) {

                    e.printStackTrace(); }*/
                dis.close();
                dos.close();
                cs.close();
            } catch (ConnectException ce) {
                eroare = true;
                textEroare = "Nu se poate accesa serverul";
                return null;
            } catch (Exception e) {
                try {
                    dis.close();
                    dos.close();
                    cs.close();
                } catch (Exception e1) {

                }
                //daca iau eroare aici vreau sa o stiu pe asta
                eroare = true;
                textEroare = "A aparut o eroare, va rugam mai incercati odata";
                return null;

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if (eroare == true) {
                text = textEroare;
                toast = Toast.makeText(context, text, duration);
                deblocheazaButonale();
                toast.show();
                return;
            }
            if (comanda.equals("login")) {

                if (raspuns.equals("acceptat")) {
                    EditText et = (EditText) findViewById(R.id.paginaLoginUser);
                    et.setText("");
                    EditText et2 = (EditText) findViewById(R.id.paginaLoginParola);
                    et2.setText("");
                    SharedPreferences settings = getSharedPreferences("login",
                            MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("cont", mesaj);
                    editor.putString("parola", mesaj2);

                    editor.commit();
                    startHomePage();
                } else {
                    if (raspuns.equals("respins")) {
                        deblocheazaButonale();
                        EditText et2 = (EditText) findViewById(R.id.paginaLoginParola);
                        et2.setText("");
                        text = "respins";
                        toast = Toast.makeText(context, text, duration);

                        toast.show();
                    } else {
                        text = "alta eroare";
                        toast = Toast.makeText(context, text, duration);

                        toast.show();
                    }
                }

            }
            super.onPostExecute(result);
        }
    }
}
