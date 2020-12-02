package tft.tfthelper;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.LinkedList;


//Actividad con la funcionalidad principal durante la partida
public class OpponentActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private String username;
    private int resultado;

    //Elementos de la vista
    private BotonJugador[] listaBotonJugador;
    private TextView helpText;
    private Boton botonDeshacer;

    //String con todas las posibles rondas de la partida
    private final String[] rondas = {"0-0", "2-1", "2-2", "2-3", "2-5", "2-6", "3-1", "3-2", "3-3", "3-5", "3-6", "4-1", "4-2", "4-3", "4-5", "4-6", "5-1", "5-2", "5-3", "5-5", "5-6", "6-1", "6-2", "6-3", "6-5", "6-6", "7-1", "7-2", "7-3", "7-5", "7-6"};

    //Game state variables
    private boolean selectedUser;
    private int ronda;
    private int activeOpponents;
    private boolean gameStarted;
    private boolean deleteState;
    private boolean puedeDeshacer;

    //Cola de oponentes
    private LinkedList<Integer> colaOponentes;
    private int colaMaxSize;

    //Old state
    private Estado[] oldEstados;
    private int oldRonda;
    private LinkedList<Integer> oldColaOponentes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opponent);
        Intent intent = getIntent();

        //Inicia variables
        this.username = intent.getStringExtra("username");
        this.ronda = 0;
        this.activeOpponents = 0;
        this.deleteState = false;
        this.puedeDeshacer = false;
        this.selectedUser = false;
        colaOponentes = new LinkedList<>();

        //Inicia elementos de la vista
        int[] listaIdBotonJugador = {R.id.botonPlayer1, R.id.botonPlayer2, R.id.botonPlayer3, R.id.botonPlayer4,
                R.id.botonPlayer5, R.id.botonPlayer6, R.id.botonPlayer7, R.id.botonPlayer8};

        this.listaBotonJugador = new BotonJugador[listaIdBotonJugador.length];

        for (int i = 0; i < listaIdBotonJugador.length; i++) {
            listaBotonJugador[i] = new BotonJugador(OpponentActivity.this, listaIdBotonJugador[i], i + 1);
        }
        this.oldEstados = new Estado[listaBotonJugador.length];

        new Boton(OpponentActivity.this, R.id.botonDelete) {
            @Override
            public void onClickBoton(View v) {
                onClickDelete();
            }
        };

        this.botonDeshacer = new Boton(OpponentActivity.this, R.id.botonBack) {
            @Override
            public void onClickBoton(View v) {
                onClickDeshacer();
            }
        };

        ImageButton botonMenu = findViewById(R.id.botonMenu);
        botonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickMenu(v);
            }
        });

        this.helpText = findViewById(R.id.helpText);
        setHelpText(R.string.seleccionaPosicion);
    }

    //Cambia el mensaje de ayuda mostrado
    public void setHelpText(int stringId) {
        helpText.setText(stringId);
    }

    //Pasa resultado a la main activity
    public void pasaResultado(){
        Intent result = new Intent();
        String resultado = String.valueOf(this.resultado);
        result.putExtra("resultado", resultado);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    //-----------------------------GESTION DE PARTIDA----------------------------------
    //Inicia la partida cuando estan todos los oponentes listos
    public void iniciaPartida() {
        Log.i("NumOponentes: ", activeOpponents + "");
        if (activeOpponents == 7) {
            this.gameStarted = true;
            Log.i("Partida iniciada: ", "true");
            avanzaRonda();
            setHelpText(R.string.partidaIniciada);
            setColaMaxSize(getActiveOpponents() - 3);
        } else {
            setHelpText(R.string.rellenaOponentes);
        }
    }

    //Avanza la ronda y actualiza el texto
    public void avanzaRonda() {
        if(ronda==rondas.length-1){
            rondasAcabadas();
        }else {
            setHelpText(R.string.partidaCurso);
            this.oldRonda = ronda;
            this.ronda++;
            ((TextView) findViewById(R.id.textRound2)).setText(rondas[ronda]);
        }
    }

    //
    public void rondasAcabadas(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.rondasAcabadas);
        builder.setMessage(R.string.noGuarda);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    //Actualiza el estado tras jugar la ronda
    public void rondaJugada(int numJugador) {
        avanzaRonda();
        updateQueue(numJugador);
        activaDeshacer();
    }

    //Aumenta el numero de oponentes iniciados
    public void activaOponente() {
        this.activeOpponents++;
    }

    //Muestra dialog con la posicion al acabar la partida
    public void muestraPuesto() {
        if (resultado >= 1 && resultado <= 8) {
            String texto;
            if (resultado == 1) {
                texto = getString(R.string.hasGanado);
            } else {
                texto = getString(R.string.acabaPuesto, resultado);
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(texto);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    pasaResultado();
                }
            });
            builder.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.puestoNoValido);
            builder.setMessage(R.string.vuelveAIntroducir);

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    preguntaPuesto();
                }
            });
            builder.show();
        }
    }

    //Calcula la posicion en la que acabaste
    public void calculaPuesto() {
        this.resultado = activeOpponents + 1;
    }

    //Pregunta la posicion en la que acabaste
    public void preguntaPuesto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.preguntaPuesto);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        builder.setView(input);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = input.getText().toString();
                resultado = Integer.parseInt(value);
                muestraPuesto();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    //Comprueba si ya no quedan mas oponentes
    public void isGameEnded() {
        if (isGameStarted() && activeOpponents == 0) {
            resultado = 1;
            muestraPuesto();
        }
    }

    //-----------------------------ELIMINAR----------------------------------
    //OnClick de eliminar
    public void onClickDelete() {
        if (isGameStarted()) {
            setDeleteState(true);
            setHelpText(R.string.seleccionaEliminado);
        } else {
            setHelpText(R.string.primeroEmpieza);
        }
    }

    //Elimina al jugador seleccionado
    public void deletePlayer() {
        resetPlayable();
        setDeleteState(false);
        setHelpText(R.string.partidaCurso);
        this.activeOpponents--;
        this.colaMaxSize--;
        this.colaOponentes = new LinkedList<>();
        isGameEnded();
    }

    //Resetea todos los jugadores al estado jugable
    public void resetPlayable() {
        for (int i = 0; i < listaBotonJugador.length; i++) {
            if (listaBotonJugador[i].getEstado() == Estado.NOT_PLAYABLE)
                listaBotonJugador[i].setEstado(Estado.PLAYABLE);
        }
    }

    //-----------------------------DESHACER----------------------------------
    //OnClick de deshacer
    public void onClickDeshacer() {
        if (isPuedeDeshacer()) {
            deshacer();
            setHelpText(R.string.rondaAnterior);
        } else {
            setHelpText(R.string.noVolver);
        }
    }

    //Vuelve al estado de la ronda anterior
    public void deshacer() {
        this.ronda = oldRonda;
        ((TextView) findViewById(R.id.textRound2)).setText(rondas[ronda]);
        Log.i("Cola actual: ", colaOponentes.toString());
        this.colaOponentes = oldColaOponentes;
        Log.i("ColaRecuperada: ", colaOponentes.toString());
        for (int i = 0; i < listaBotonJugador.length; i++) {
            listaBotonJugador[i].setEstado(oldEstados[i]);
        }
        desactivaDeshacer();
    }

    //Activa el boton de deshacer
    public void activaDeshacer() {
        this.puedeDeshacer = true;
        this.botonDeshacer.getBoton().setTextColor(getColor(R.color.white));
    }

    //Desactiva el boton de deshacer
    public void desactivaDeshacer() {
        this.puedeDeshacer = false;
        this.botonDeshacer.getBoton().setTextColor(getColor(R.color.greyButton));
    }

    //-----------------------------COLA----------------------------------
    //Actualiza la cola de oponentes contra los que no se puede jugar
    public void updateQueue(int numJugador) {
        this.oldColaOponentes = (LinkedList) colaOponentes.clone();
        Log.i("OldCola: ", oldColaOponentes.toString());
        colaOponentes.add(numJugador);
        Log.i("ColaSize: ", colaOponentes.size() + "");
        Log.i("ColaMaxSize: ", colaMaxSize + "");
        //Si hay más oponentes de los que caben se saca el primero que entro
        if (colaOponentes.size() == colaMaxSize + 1) {
            Log.i("Cola: ", "Esta llena");
            int opOut = colaOponentes.remove();
            listaBotonJugador[opOut - 1].setEstado(Estado.PLAYABLE);
        }
        Log.i("Cola: ", colaOponentes.toString());
    }

    //Guarda los estados de los botones actuales
    public void guardaEstadosBotones() {
        for (int i = 0; i < listaBotonJugador.length; i++) {
            oldEstados[i] = listaBotonJugador[i].getEstado();
        }
    }

    //-----------------------------MENU----------------------------------
    //OnClick del boton menu
    public void onClickMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.tftmenu);
        popup.show();
    }

    //OnClick de los items del menu
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.resetMenu:
                Log.i("Menu: ", "reset");
                confirmaReset();
                return true;
            case R.id.endMenu:
                Log.i("Menu: ", "terminar");
                confirmaTerminar();
                return true;
            case R.id.abandonMenu:
                Log.i("Menu: ", "abandonar");
                confirmaAbandonar();
                return true;
            default:
                return false;
        }
    }

    //Dialog apra confirmar resetear partida
    public void confirmaReset() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.preguntaReset);
        builder.setMessage(R.string.noGuarda);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetearPartida();
            }
        });
        builder.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    //Resetea la partida a su estado inicial
    public void resetearPartida() {
        Intent i = new Intent(this, OpponentActivity.class);
        startActivity(i);
        finish();
    }

    //Dialog para confirmar termianr partida
    public void confirmaTerminar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.noAcabado);
        builder.setMessage(R.string.preguntaGuardar);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                preguntaPuesto();
            }
        });
        builder.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    //Dialog para confirmar abandonar partida
    public void confirmaAbandonar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.preguntaAbandonar);
        builder.setMessage(R.string.noGuarda);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    //-----------------------------GETTERS AND SETTERS----------------------------------


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(boolean selectedUser) {
        this.selectedUser = selectedUser;
    }

    public int getActiveOpponents() {
        return activeOpponents;
    }

    public void setActiveOpponents(int activeOpponents) {
        this.activeOpponents = activeOpponents;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public boolean isDeleteState() {
        return deleteState;
    }

    public void setDeleteState(boolean deleteState) {
        this.deleteState = deleteState;
    }

    public boolean isPuedeDeshacer() {
        return puedeDeshacer;
    }

    public void setPuedeDeshacer(boolean puedeDeshacer) {
        this.puedeDeshacer = puedeDeshacer;
    }

    public int getColaMaxSize() {
        return colaMaxSize;
    }

    public void setColaMaxSize(int colaMaxSize) {
        this.colaMaxSize = colaMaxSize;
    }


}



