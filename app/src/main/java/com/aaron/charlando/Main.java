package com.aaron.charlando;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

public class Main extends Activity implements TextToSpeech.OnInitListener{

    private boolean reproductor=false;
    private final int HABLA = 1;
    private final int ESCUCHA = 2;
    private TextToSpeech tts;
    private TextView texto;
    private Spinner idioma;
    private ChatterBot bot1;
    private ChatterBotFactory factory;
    private HiloFacil hf=null;
    private Button bHabla;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initcomponents();
    }

    public void initcomponents(){
        Intent intent=new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, HABLA);
        texto =(TextView)findViewById(R.id.texto);
        idioma= (Spinner)findViewById(R.id.spinner);
        factory = new ChatterBotFactory();
        bHabla=(Button)findViewById(R.id.bHabla);
        try {
            bot1 = factory.create(ChatterBotType.CLEVERBOT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == HABLA) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, this);
            } else {
                Intent intent = new Intent();
                intent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(intent);
            }
        }
        if (requestCode == ESCUCHA) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> textos = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String respuesta=textos.get(0);
                texto.setText(texto.getText().toString()+"Yo: "+respuesta+"\n");
                //reproducirTexto(respuesta);
                hf = new HiloFacil();
                hf.execute(new String[]{respuesta});

            } else {
                bHabla.setEnabled(true);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onInit(int i) {
        if(i==TextToSpeech.SUCCESS){
            reproductor=true;
        }
        else{}
    }

    @Override
    protected void onStop() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        super.onStop();
    }

    public void escuchando(View v){
        bHabla.setEnabled(false);
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        if(idioma.getSelectedItemPosition()==0) {
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "es-ES");
        }else{
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-GB");
        }
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,R.string.hablaahora);
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,3000);
        startActivityForResult(i, ESCUCHA);

    }

    class HiloFacil extends AsyncTask<String, Integer, String> {

        private ProgressDialog dialogo;

        //Lanzamos un dialogo que nos indica que está en carga
        @Override
        protected void onPreExecute() {
            dialogo = new ProgressDialog(Main.this);
            dialogo.setMessage(getString(R.string.pensando));
            dialogo.setCancelable(false);
            dialogo.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... objects) {
            String respuesta = objects[0];
            try {
                ChatterBotSession bot1session = bot1.createSession();
                try {
                    respuesta=bot1session.think(respuesta);
                } catch (Exception ex) {}
                return respuesta;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String r) {
            super.onPostExecute(r);
            tts.speak(r, TextToSpeech.QUEUE_ADD, null);
            texto.setText(texto.getText().toString() + "IA: " + r + "\n");
            dialogo.dismiss();
            bHabla.setEnabled(true);
        }
    }
}
