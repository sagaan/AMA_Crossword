package com.ama.crossword;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.InputFilter;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String[] columnas = {"a","b","c","d","e","f","g","h","i","j","k"};
    private EditText[][] casillas;

    private Button btnReset, btnFinish;

//    private Vibrator v;
//    private boolean longClick;

    private TextView timer;
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    int Seconds, Minutes, MilliSeconds ;

    private TextView txtHorizontal, txtPista1, txtVertical, txtPista2;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtHorizontal = findViewById(R.id.txtHorizontal);
        txtVertical = findViewById(R.id.txtVertical);
        txtPista1 = findViewById(R.id.txtPista1);
        txtPista2 = findViewById(R.id.txtPista2);

        casillas = new EditText[11][11];
        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                //Genera los diferentes ID mediante los valores del for
                String etxId = "etx"+x+columnas[y];
                int id = getResources().getIdentifier(etxId, "id", getPackageName());
                casillas[x][y] = findViewById(id);
                casillas[x][y].setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        onTouchCasilla(v);
                        return false;
                    }
                });
                casillas[x][y].setBackgroundColor(getResources().getColor(R.color.colorBlack));
                casillas[x][y].setFilters(new InputFilter[]{new InputFilter.LengthFilter(0)});
            }
        }

        btnReset = findViewById(R.id.btnReiniciar);
        btnReset.setOnClickListener(this);

        btnFinish = findViewById(R.id.btnSubmit);
        btnFinish.setOnClickListener(this);

        timer = findViewById(R.id.txtTimer);
        iniciar();

//        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//        longClick = false;
    }

    public void iniciar(){
        boolean valido = true;

        List<Palabra> palabras = read();
        //Revisa que haya por lo menos 2 palabras en la lista
        if(palabras.size() < 2){
            valido = false;
            Toast.makeText(this, "No hay palabras/pistas validas suficientes para jugar", Toast.LENGTH_SHORT).show();
            btnReset.setEnabled(false);
        }

        //Reinicia el texto de todas las casillas
        for (int x=0; x<11; x++){
            for (int y = 0; y < 11; y++) {
                casillas[x][y].setText("");
                casillas[x][y].setBackgroundColor(getResources().getColor(R.color.colorBlack));
                casillas[x][y].setTag(null);
            }
        }

        valido = ordenar(palabras);
        if(!valido)
            Toast.makeText(this, "No hay intersecciones entre palabras", Toast.LENGTH_SHORT).show();


        //Habilita/Deshabilita los botones dependiendo si hay suficientes palabras
        btnFinish.setEnabled(valido);

        //Esconder textviews
        txtHorizontal.setVisibility(View.INVISIBLE);
        txtVertical.setVisibility(View.INVISIBLE);
        txtPista1.setText("");
        txtPista1.setVisibility(View.INVISIBLE);
        txtPista2.setText("");
        txtPista2.setVisibility(View.INVISIBLE);

        //Reinicia el timer
        MillisecondTime = 0L ;
        StartTime = SystemClock.uptimeMillis();
        TimeBuff = 0L ;
        UpdateTime = 0L ;
        Seconds = 0 ;
        Minutes = 0 ;
        MilliSeconds = 0 ;
        timer.setText("00:00:00");
        if(valido)
            handler.postDelayed(timerRunnable, 100);

    }

    //Obtiene las palabras de strings.xml
    //Verifica que esten en pares validos de palabra y pista
    public List<Palabra> read(){
        List<Palabra> palabras = new ArrayList<>();

        for (int i = 1; i < 11; i++) {
            String palabraPre = "palabra"+i;
            String pistaPre = "pista"+i;
            int palabraId = getResources().getIdentifier(palabraPre, "string", getPackageName());
            int pistaId = getResources().getIdentifier(pistaPre, "string", getPackageName());
            String palabra = getResources().getString(palabraId);
            String pista = getResources().getString(pistaId);

            if(palabra.length() <= 0 || palabra.length() > 10 || pista.length() <= 0)
                System.err.println("String invalido, verifique que la palabra y pista existan. " +
                        "Ademas, la palabra debe ser menor igual a 10 caracteres");
            else {
                palabras.add(new Palabra(palabra, pista));
                System.err.println("Palabra: " + palabra + "\nPista: " + pista);
            }
        }

        return palabras;
    }

    public boolean ordenar(List<Palabra> palabras){
        boolean valido = true;
        int validCounter = 1;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            palabras.sort(new Comparator<Palabra>() {
                @Override
                public int compare(Palabra o1, Palabra o2) {
                    return (o2.getPalabra().length() - o1.getPalabra().length());
                }
            });
        }

        //Primer palabra
        Random random = new Random();
        boolean acomodo;
        do {
            acomodo = false;
            int firstX = random.nextInt(11);
            int firstY = random.nextInt(11);
            Palabra palabra = palabras.get(0);
            int length = palabra.getPalabra().length();
            HashMap<String, Palabra> tag = new HashMap<>();
            if ((firstX + (length - 1)) < 11) {
                palabra.setDireccion(true);
                tag.put("horizontal", palabra);
                for (int i = 0; i < length; i++) {
                    casillas[firstX + i][firstY].setTag(R.string.tag1,tag);
                    casillas[firstX + i][firstY].setTag(R.string.tag2,palabra.getPalabra().charAt(i));
                    casillas[firstX + i][firstY].setBackgroundColor(getResources().getColor(R.color.colorWhite));
                    casillas[firstX + i][firstY].setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                }
                acomodo = true;
            } else if ((firstY + (length - 1)) < 11) {
                palabra.setDireccion(false);
                tag.put("vertical", palabra);
                for (int i = 0; i < length; i++) {
                    casillas[firstX][firstY + i].setTag(R.string.tag1,tag);
                    casillas[firstX][firstY + i].setTag(R.string.tag2,palabra.getPalabra().charAt(i));
                    casillas[firstX][firstY+ i].setBackgroundColor(getResources().getColor(R.color.colorWhite));
                    casillas[firstX][firstY + i].setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                }
                acomodo = true;
            }
        }while(!acomodo);

        List<Palabra> otrasPalabras = palabras.subList(1, palabras.size());
        for (Palabra palabra: otrasPalabras) {
            if(gridCheck(palabra))
                validCounter++;
        }

        if(validCounter < 2)
            valido = false;

        return valido;
    }

    public boolean gridCheck(Palabra palabra){
        boolean valid = false;
        boolean breakout = false;

        for (int x = 0; x < 11; x++) {
            for (int y = 0; y < 11; y++) {
                String caract;
                try {
                    caract = casillas[x][y].getTag(R.string.tag2).toString();
                }catch (NullPointerException npe){
                    caract = null;
                }

                if(caract != null){
                    for (int l = 0; l < palabra.getPalabra().length(); l++){
                        if(palabra.getPalabra().toUpperCase().charAt(l) == caract.toUpperCase().charAt(0)){
                            HashMap<String, Palabra> palabraGrid = (HashMap<String,Palabra>) casillas[x][y].getTag(R.string.tag1);
                            if(palabraGrid.containsKey("horizontal")){
                                if(y - l + (palabra.getPalabra().length()-1) < 11 && y - l > 0 ){
                                    for (int y2 = 0; y2 < palabra.getPalabra().length(); y2++) {
                                        HashMap<String, Palabra> tag = new HashMap<>();
                                        tag.put("vertical", palabra);
                                        casillas[x][(y-l)+y2].setTag(R.string.tag1,tag);
                                        casillas[x][(y-l)+y2].setTag(R.string.tag2,palabra.getPalabra().charAt(y2));
                                        casillas[x][(y-l)+y2].setBackgroundColor(getResources().getColor(R.color.colorWhite));
                                        casillas[x][(y-l)+y2].setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                                    }
                                    valid = true;
                                }
                                breakout = true;
                                break;
                            }else{
                                if(x - l + (palabra.getPalabra().length()-1) < 11 && x - l > 0 ){
                                    for (int x2 = 0; x2 < palabra.getPalabra().length(); x2++) {
                                        HashMap<String, Palabra> tag = new HashMap<>();
                                        tag.put("horizontal", palabra);
                                        casillas[(x-l)+x2][y].setTag(R.string.tag1,tag);
                                        casillas[(x-l)+x2][y].setTag(R.string.tag2,palabra.getPalabra().charAt(x2));
                                        casillas[(x-l)+x2][y].setBackgroundColor(getResources().getColor(R.color.colorWhite));
                                        casillas[(x-l)+x2][y].setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
                                    }
                                    valid = true;
                                }
                                breakout = true;
                                break;
                            }
                        }
                    }
                }
                if(breakout)
                    break;
            }
            if(breakout)
                break;
        }

        return valid;
    }
//    Runnable longClickRunnable = new Runnable() {
//        public void run() {
//            Toast.makeText(MainActivity.this, "Ya le picaste mucho", Toast.LENGTH_SHORT).show();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
//            }else{
//                v.vibrate(200);
//            }
//            longClick = true;
//        }
//    };

    final Handler handler = new Handler();
    public Runnable timerRunnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

            timer.setText("" + Minutes + ":"
                    + String.format("%02d", Seconds) + ":"
                    + String.format("%03d", MilliSeconds));

            handler.postDelayed(this, 0);
        }

    };

    @Override
    public void onClick(View v) {
        handler.removeCallbacks(timerRunnable);
        if(v.getId() == R.id.btnReiniciar)
            iniciar();
    }

    public void onTouchCasilla(View v){
        HashMap<String,Palabra> palabras = (HashMap<String, Palabra>)v.getTag(R.string.tag1);
        if(palabras == null){
            Toast.makeText(MainActivity.this, "Casilla invalida", Toast.LENGTH_SHORT).show();
            txtHorizontal.setVisibility(View.INVISIBLE);
            txtVertical.setVisibility(View.INVISIBLE);
            txtPista1.setText("");
            txtPista1.setVisibility(View.INVISIBLE);
            txtPista2.setText("");
            txtPista2.setVisibility(View.INVISIBLE);
        }else {
            if (palabras.containsKey("horizontal") && !palabras.containsKey("vertical")) {
                txtHorizontal.setVisibility(View.VISIBLE);
                txtPista1.setVisibility(View.VISIBLE);
                txtVertical.setVisibility(View.INVISIBLE);
                txtPista2.setVisibility(View.INVISIBLE);
                txtPista1.setText(palabras.get("horizontal").getPista());
            }

            if (palabras.containsKey("vertical") && !palabras.containsKey("horizontal")) {
                txtHorizontal.setVisibility(View.INVISIBLE);
                txtPista1.setVisibility(View.INVISIBLE);
                txtVertical.setVisibility(View.VISIBLE);
                txtPista2.setVisibility(View.VISIBLE);
                txtPista2.setText(palabras.get("vertical").getPista());
            }

            if (palabras.containsKey("horizontal") && palabras.containsKey("vertical")) {
                txtHorizontal.setVisibility(View.VISIBLE);
                txtPista1.setVisibility(View.VISIBLE);
                txtVertical.setVisibility(View.VISIBLE);
                txtPista2.setVisibility(View.VISIBLE);
                txtPista1.setText(palabras.get("horizontal").getPista());
                txtPista2.setText(palabras.get("vertical").getPista());
            }
        }
    }
}
