package service;

import db.ArticleRepository;
import db.EventArticleRepository;
import db.EventRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.util.converter.IntegerStringConverter;
import model.Article;
import model.Event;
import model.EventArticle;
import runner.Main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class FilteredChecklistController {

    @FXML
    private TableColumn articleNameColumn;
    @FXML
    private TableColumn articleAmountColumn;
    @FXML
    private TableView<Article> articles;
    private Event event;

    //set column values and make the TableView editable
    @FXML
    private void initialize() {
        articles.setEditable(true);
        articleNameColumn.setCellValueFactory(new PropertyValueFactory<Article, String>("name"));
        articleAmountColumn.setCellValueFactory(new PropertyValueFactory<Article, Integer>("amount"));
        articleAmountColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        articleAmountColumn.setOnEditCommit(
                (EventHandler<TableColumn.CellEditEvent<Article, Integer>>) editEvent -> editEvent.getTableView().getItems().get(editEvent.getTablePosition().getRow()).setAmount(editEvent.getNewValue()));
    }

    // set events into cells
    public void setItems(ChoseCategoryController controller) {
        articles.setItems(FXCollections.observableArrayList(ArticleRepository.readCategory(controller.getSelectedCategories())));
        event = controller.getEvent();
    }

    //check how much articles are already used at the given events day. If there is enough articles available insert the event and the connection betwen Article ID and Event ID.
    //If there are not enough articles available show alert window and don't insert the event and the Article ID - Event ID connection as well
    @FXML
    private void save() {
        ObservableList<Article> uiArticles = articles.getItems();
        Collection<Event> todaysEvents = EventRepository.searchTodaysEvents(event.getDate());
        Collection<EventArticle> alreadyBlockedArticlesForEvents = EventArticleRepository.loadBy(todaysEvents.stream().map(Event::getId).collect(Collectors.toList()));
        Collection<Article> alreadyUsedArticles = ArticleRepository.findBy(alreadyBlockedArticlesForEvents.stream().map(EventArticle::getArticleId).collect(Collectors.toList()));
        Collection<Article> dbArticlesForGivenArticleNrs = ArticleRepository.getBy(uiArticles.stream().map(Article::getArticleNr).collect(Collectors.toList()));
        Collection<EventArticle> eventArticles = new ArrayList<>();
        Collection<FailResult> failResults = new ArrayList<>();

        for (Article uiArticle : uiArticles) {
            if (uiArticle.getAmount() > 0) {
                long countAllArticlesForArticleNr = dbArticlesForGivenArticleNrs.stream().filter(article -> article.getArticleNr().equals(uiArticle.getArticleNr())).count();
                long countAlreadyUsedArticlesForArticleNr = alreadyUsedArticles.stream().filter(article -> article.getArticleNr().equals(uiArticle.getArticleNr())).count();
                long availableArticleAmount = countAllArticlesForArticleNr - countAlreadyUsedArticlesForArticleNr;
                boolean moreArticlesAvailableThanPicked = countAllArticlesForArticleNr - countAlreadyUsedArticlesForArticleNr - uiArticle.getAmount() >= 0;

                if (moreArticlesAvailableThanPicked) {
                    dbArticlesForGivenArticleNrs.stream()
                            .filter(article -> article.getArticleNr().equals(uiArticle.getArticleNr()))
                            .limit(uiArticle.getAmount())
                            .forEach(art -> eventArticles.add(new EventArticle(art.getId(), event.getId())));
                } else {
                    failResults.add(new FailResult(uiArticle.getName(), availableArticleAmount, uiArticle.getAmount()));
                }
            }
        }
        if (failResults.isEmpty()) {
            EventRepository.insertEvent(event);
            EventArticleRepository.insertConnectionToEventArticleTable(eventArticles);
            successfullyDoneWindow();
            ChoseCategoryController.stage3.close();
            NewEventController.stage2.close();
            RootLayoutController.stage1.close();
            Main.refresh();
        } else {
            showAlert(failResults);
        }
    }

    //if close button was clicked close the stage
    public void closeFilteredCategoryButton() {
        ChoseCategoryController.stage3.close();
    }

    //opens alert window with the articles which are not available
    private void showAlert(Collection<FailResult> failResults) {
        String printMessage = failResults.stream().map(FailResult::toString).collect(Collectors.joining(",\n"));
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setResizable(true);
        alert.setTitle("Fehler beim Aktualisieren");
        alert.setHeaderText("Die Aktualisierung der Datenbank ist fehlgeschlagen.");
        alert.setContentText("Die Datenbank konnte nicht aktualisiert werden, " +
                "da mehr Artikel ausgewählt wurden als vorhanden sind: \n\n" + printMessage);
        alert.showAndWait();
    }

    private void successfullyDoneWindow() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Event angelegt!");
        alert.setHeaderText(null);
        alert.setContentText("Das Event wurde Erfolgreich angelegt!");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
    }
}



