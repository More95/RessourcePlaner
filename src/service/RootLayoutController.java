package service;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RootLayoutController {

    public static Stage stage1 = new Stage();

    //Open newEventWindow
    public void handleNewEventButton() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("view/NewEvent.fxml"));
            Parent root1 = fxmlLoader.load();
            stage1.setTitle("Neues Event");
            stage1.setScene(new Scene(root1));
            stage1.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


