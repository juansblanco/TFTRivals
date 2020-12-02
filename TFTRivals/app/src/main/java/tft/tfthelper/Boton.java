package tft.tfthelper;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

//Clase abstracta de los botones a utilizar
public abstract class Boton {

    private Button boton;
    private Activity activity;

    public Boton(Activity activity, int id){
        this.boton = activity.findViewById(id);
        this.boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickBoton(v);
            }
        });
        this.activity=activity;
    }

    public Button getBoton() {
        return boton;
    }

    public void setBoton(Button boton) {
        this.boton = boton;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public abstract void onClickBoton(View v);

}
