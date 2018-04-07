package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;


public class NewEventController {


    @FXML
    private TextField nameOfEventField;
    @FXML
    private TextField dateField;
    @FXML
    private TextField placeField;
    @FXML
    private TextField contactPersonField;

    public static Stage stage3 = new Stage();
    RootLayoutController close= new RootLayoutController();
    public static Event newEvent;




    //Button to open ChoseCategory window
    public void openChoseCategoryWindow() {
        try {
            FXMLLoader fxmlLoader3 = new FXMLLoader(getClass().getResource("ChoseCategory.fxml"));
            Parent root3 = (Parent) fxmlLoader3.load();
            stage3.setTitle("Kategorien Wählen");
            stage3.setScene(new Scene(root3));
            stage3.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Button to create an event
    public void handleGenerateEventButton(ActionEvent event4){
        //save user input to Event object
        //Event newEvent = new Event(nameOfEventField.getText(),dateField.getText().toString(),placeField.getText(),contactPersonField.getText());
        String eventName = nameOfEventField.getText();
        String eventDate = dateField.getText().toString();
        String eventPlace = placeField.getText();
        String contactPerson = contactPersonField.getText();


        //check if user input is valid
        if(eventName.isEmpty() | eventDate.isEmpty() | eventPlace.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Fehlermeldung");
            alert.setHeaderText(null);
            alert.setContentText("Leere Eingabe! Bitte füllen Sie alle Felder aus");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
        }try {
            newEvent = new Event(eventName,eventDate,eventPlace,contactPerson);
            newEvent.parse(eventDate);
            //DatabaseController.insertEvent(newEvent); // muss weg und ans Ende das FilteredChecklistController, stattdessen soll hier das Fenster für die choseCategory aufgehen
            close.closeNewEventWindow();
            openChoseCategoryWindow();

        }catch (RuntimeException  ex){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Fehlermeldung");
            alert.setHeaderText(null);
            alert.setContentText("Falsches Datum! Bitte halten Sie das vorgesehene Format ein");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
        }
    }

    //Button to close NewEventWindow
    public void closeNewEventButton(ActionEvent event5){

    close.closeNewEventWindow();


    }

    public void setEvent(){
        DatabaseController.insertEvent(newEvent);
    }

    //close Window used by ChoseCategoryController
   public void closeWindow(){
        stage3.close();
    }

    public void getNewEvent(Event event){
        this.newEvent = event;
    }
}
