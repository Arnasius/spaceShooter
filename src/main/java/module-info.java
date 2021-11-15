module com.spaceinvader.spaceinvaders {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.spaceinvader.spaceinvaders to javafx.fxml;
    exports com.spaceinvader.spaceinvaders;
}