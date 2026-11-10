module com.mls.learningfx {

    // JavaFX Modules
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Core Java Modules
    requires java.sql;
    requires java.desktop;

    // External Libraries
    requires static lombok;
    requires com.github.librepdf.openpdf;
    requires javafx.base;

    // Reflection Openings for JavaFX
    opens com.mls.learningfx to javafx.fxml;
    opens com.mls.learningfx.controller to javafx.fxml;
    opens com.mls.learningfx.model to javafx.base;

    // Exported Packages
    exports com.mls.learningfx;
    exports com.mls.learningfx.controller;
}