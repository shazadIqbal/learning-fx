module com.mls.learningfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.mls.learningfx to javafx.fxml;
    exports com.mls.learningfx;
    exports com.mls.learningfx.controller;
    opens com.mls.learningfx.controller to javafx.fxml;
}