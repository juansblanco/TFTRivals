package tft.tfthelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

//Clase con la funcionalidad de los botones de jugadores
public class BotonJugador extends Boton{

    //La activity en la que existen los botones
    private OpponentActivity opAct;

    //Estado del boton
    private Estado estado;
    private boolean iniciado;

    //Informacion del jugador asociado al boton
    private int numJugador;
    private String nomJugador;

    public BotonJugador(OpponentActivity activity, int id, int num){
        super(activity, id);
        this.opAct = activity;
        this.estado = Estado.ELIMINATED;
        this.iniciado = false;
        this.numJugador = num;
        this.nomJugador = getBoton().getText().toString();
    }

    @Override
    public void onClickBoton(View v) {
        Log.i("Click: ", "boton " + numJugador);
        //El primer boton elegido es el usuario
        if(!opAct.isSelectedUser()){
            selectAsMe();
            opAct.setHelpText(R.string.introduceNombres);
        }else{
            //Se inician los jugadores dandoles un nombre
            if(!iniciado) {
                inputName();
            }else{
                //Si se ha pulsado eliminar y se va a eliminar un jugador
                if(opAct.isDeleteState()){
                    Log.i("Delete: ", "Borro jugador");
                    confirmDeletePlayer();
                }
                //Si la partida esta iniciada
                else if(opAct.isGameStarted()) {
                    Log.i("Click: ", "Juego vs oponente");
                    if(estado != Estado.ME) {
                        //Cambia el estado del jugador tras enfrentarse a el
                        if (estado == Estado.PLAYABLE) {
                            opAct.guardaEstadosBotones();
                            setEstado(Estado.NOT_PLAYABLE);
                            opAct.rondaJugada(numJugador);
                        }
                        else {
                            opAct.setHelpText(R.string.noPuedeJugar);
                        }
                    }else{
                        Log.i("Click: ", "Jugador usuario");
                        opAct.setHelpText(R.string.contraUsuario);
                    }
                }
                else{
                    opAct.setHelpText(R.string.rellenaTodos);
                }
            }
        }
    }

    //El boton pasa a ser el del jugador usuario
    public void selectAsMe(){
        setEstado(Estado.ME);
        opAct.setSelectedUser(true);
        getBoton().setText(opAct.getUsername());
        this.iniciado = true;
    }

    //Cambia el estado del jugador y actualiza el color del boton
    public void setEstado(Estado estado) {
        this.estado = estado;
        cambiaColor();
    }

    //Cambia el color del boton en funcion de su estado
    public void cambiaColor(){
        switch(this.estado){
            case ME:
                getBoton().setBackgroundResource(R.drawable.boton_usuario);
                break;
            case PLAYABLE:
                getBoton().setBackgroundResource(R.drawable.boton_jugable);
                break;
            case NOT_PLAYABLE:
                getBoton().setBackgroundResource(R.drawable.boton_no_jugable);
                break;
            case ELIMINATED:
                getBoton().setBackgroundResource(R.drawable.boton_eliminado);
                break;
            default:
                Log.e("Error", "Falla cambiaColor");
        }
    }

    //Muestra el dialog para introducir el nombre de los rivales
    public void inputName(){
        AlertDialog.Builder builder = new AlertDialog.Builder(opAct);
        builder.setTitle(opAct.getString(R.string.nombreOponente, numJugador));

        final EditText input = new EditText(opAct);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(input);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                nomJugador = input.getText().toString();
                getBoton().setText(nomJugador);
                iniciaEstado();
                opAct.iniciaPartida();
            }
        });
        builder.show();
    }

    //Inicia los oponentes
    public void iniciaEstado(){
        setEstado(Estado.PLAYABLE);
        this.iniciado = true;
        opAct.activaOponente();
    }

    //
    public void confirmDeletePlayer(){
        String titulo;
        String mensaje;
        String boton;
        if(estado == Estado.ME){
            titulo = opAct.getString(R.string.eliminarte);
            mensaje = opAct.getString(R.string.preguntaTerminar);
            boton = opAct.getString(R.string.terminar);
        }else{
            titulo = opAct.getString(R.string.preguntaEliminar, nomJugador);
            mensaje = null;
            boton = opAct.getString(R.string.eliminar);
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(opAct);
        final AlertDialog dialog = builder.setTitle(titulo)
                .setMessage(mensaje)
        .setPositiveButton(boton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(estado == Estado.ME){
                    opAct.calculaPuesto();
                    opAct.muestraPuesto();
                }
                setEstado(Estado.ELIMINATED);
                opAct.deletePlayer();
                opAct.desactivaDeshacer();
            }
        }).setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                opAct.setDeleteState(false);
            }
        }).setCancelable(false).create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(opAct.getResources().getColor(R.color.rojoJugador));
            }
        });

        dialog.show();
    }

    //Getters and setters
    public Estado getEstado() {
        return estado;
    }
}

//Enum con los posibles estados del boton
enum Estado{
    ME(),
    PLAYABLE,
    NOT_PLAYABLE,
    ELIMINATED
}
