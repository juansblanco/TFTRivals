package tft.tfthelper;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

//Actividad del menu principal
public class MainActivity extends AppCompatActivity {

    private String username;
    private String resultado;
    private String file;
    private String file2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.file = "file.txt";
        this.file2 = "file2.txt";
        this.username = "";
        this.resultado = "";


        //Si ya hay un nombre de usuario guardado lo pone
        leerUsuario();
        ((TextView) findViewById(R.id.textoUsuario)).setText(username);
        if(username.equals("")){
            ((TextView) findViewById(R.id.textoUsuario)).setText(R.string.usuarioVacio);
        }

        //Si hay un resultado guardado lo pone
        leerResultado();
        ((TextView) findViewById(R.id.textoResultado)).setText(getString(R.string.ultimoResultado, resultado));
        if(resultado.equals("")){
            ((TextView) findViewById(R.id.textoResultado)).setText(getString(R.string.ultimoResultado, ""));
        }

        new Boton(MainActivity.this, R.id.botonStart) {
            @Override
            public void onClickBoton(View v) {
                lanzaPartida();
            }
        };

        ImageButton botonEditar = findViewById(R.id.botonEditar);
        botonEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickEditar();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            resultado = data.getStringExtra("resultado");
        }
        ((TextView) findViewById(R.id.textoResultado)).setText(getString(R.string.ultimoResultado, resultado));
        Log.i("Resultado: ", resultado+"");
        guardaResultado();
    }

    //Lanza la activity de la partida
    public void lanzaPartida(){
        Intent i = new Intent(getApplicationContext(), OpponentActivity.class);
        i.putExtra("username", username);
        startActivityForResult(i, 0);
    }

    //OnClick del boton editar
    public void onClickEditar(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pideUsername);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(input);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                username = input.getText().toString();
                ((TextView) findViewById(R.id.textoUsuario)).setText(username);
                guardaUsuario();
            }
        });
        builder.show();
    }

    public void guardaUsuario(){
        String text = username;
        FileOutputStream fOS;
        try{
            fOS = openFileOutput(file, Context.MODE_PRIVATE);
            fOS.write(text.getBytes());
            fOS.close();
        }catch(FileNotFoundException e){
            Log.e("File write: ", "Fichero no encontrado");
        }catch (IOException e){
            Log.e("File write: ", "Error al abrir el fichero");
        }
    }

    public void leerUsuario(){
        InputStream fIS;
        try{
            fIS = openFileInput(file);
            BufferedReader bR = new BufferedReader(new InputStreamReader(fIS));
            this.username = bR.readLine();
            if(this.username == null){
                Log.i("File: ", "Empty");
                this.username = "";
            }
            Log.i("Username: ", username);
            fIS.close();
        }catch(FileNotFoundException e){
            Log.e("File read: ", "Fichero no encontrado");
        }catch (IOException e){
            Log.e("File read: ", "Error al abrir el fichero");
        }
    }

    public void guardaResultado(){
        String text = String.valueOf(resultado);
        FileOutputStream fOS;
        try{
            fOS = openFileOutput(file2, Context.MODE_PRIVATE);
            fOS.write(text.getBytes());
            fOS.close();
        }catch(FileNotFoundException e){
            Log.e("File2 write: ", "Fichero no encontrado");
        }catch (IOException e){
            Log.e("File2 write: ", "Error al abrir el fichero");
        }
    }

    public void leerResultado(){
        InputStream fIS;
        try{
            fIS = openFileInput(file2);
            BufferedReader bR = new BufferedReader(new InputStreamReader(fIS));
            resultado = bR.readLine();
            fIS.close();
        }catch(FileNotFoundException e){
            Log.e("File2 read: ", "Fichero no encontrado");
        }catch (IOException e){
            Log.e("File2 read: ", "Error al abrir el fichero");
        }
    }

}