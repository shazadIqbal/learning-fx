module com.mls.learningfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires static lombok;
    requires javafx.graphics;

    // PDF report
    requires com.github.librepdf.openpdf;
    requires java.desktop;   // OpenPDF uses java.awt.Color


    opens com.mls.learningfx to javafx.fxml;
    exports com.mls.learningfx;
    exports com.mls.learningfx.controller;
    opens com.mls.learningfx.controller to javafx.fxml;

    opens com.mls.learningfx.model to javafx.base;
}