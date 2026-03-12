module org.ghost.chat_examen {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires java.sql;
    requires java.desktop;
    requires java.persistence;
    requires org.hibernate.orm.core;
    requires net.bytebuddy;
    requires java.naming;
    requires java.xml.bind;
    requires com.jfoenix;
    requires jbcrypt;
    requires atlantafx.base;
    requires static lombok;

    opens org.ghost.chat_examen to javafx.fxml;
    opens org.ghost.chat_examen.entities to org.hibernate.orm.core, javafx.base;
    opens org.ghost.chat_examen.components;
    opens org.ghost.chat_examen.enums;
    opens org.ghost.chat_examen.dao to org.hibernate.orm.core;

    opens org.ghost.chat_examen.controllers to javafx.fxml;
    opens org.ghost.chat_examen.client to javafx.fxml;

    exports org.ghost.chat_examen;

}