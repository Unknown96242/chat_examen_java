package org.ghost.chat_examen.components;

import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbarLayout;
import javafx.util.Duration;

public class Toast {
    private final JFXSnackbar snackbar;
    private int duration = 3000;

    public Toast(JFXSnackbar snackbar){
        this.snackbar = snackbar;

    }

    public Toast(JFXSnackbar snackbar, int duration){
        this.snackbar = snackbar;
        this.duration = duration;

    }


    //Toast pour msg general
    public void toast(String s, String type){
        JFXSnackbarLayout content = new JFXSnackbarLayout(s);
        content.getStyleClass().add("snackbar-"+type);
        snackbar.enqueue(new JFXSnackbar.SnackbarEvent(content, Duration.millis(duration)));
    }


    //Toast pour msg d'erreur
    public void error(String s){
        JFXSnackbarLayout content = new JFXSnackbarLayout(s);
        content.getStyleClass().add("snackbar-error");
        snackbar.enqueue(new JFXSnackbar.SnackbarEvent(content, Duration.millis(duration)));
    }

    //Toast pour msg succes
    public void success(String s){
        JFXSnackbarLayout content = new JFXSnackbarLayout(s);
        content.getStyleClass().add("snackbar-success");
        snackbar.enqueue(new JFXSnackbar.SnackbarEvent(content, Duration.millis(duration)));
    }

    //Toast pour msg warning
    public void warning(String s){
        JFXSnackbarLayout content = new JFXSnackbarLayout(s);
        content.getStyleClass().add("snackbar-warning");
        snackbar.enqueue(new JFXSnackbar.SnackbarEvent(content, Duration.millis(duration)));
    }
}
