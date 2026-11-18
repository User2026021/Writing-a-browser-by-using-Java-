import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Main extends Application {
    private WebEngine engine;
    private TextField addressBar;
    private Label statusLabel;

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        engine = webView.getEngine();

        addressBar = new TextField("https://example.com");
        addressBar.setPromptText("Введите адрес сайта или поисковый запрос");

        Button backButton = new Button("←");
        Button forwardButton = new Button("→");
        Button reloadButton = new Button("↻");
        Button goButton = new Button("Перейти");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(120);
        progressBar.progressProperty().bind(
                engine.getLoadWorker().progressProperty()
        );

        statusLabel = new Label("Готово");

        backButton.setOnAction(event -> goBack());
        forwardButton.setOnAction(event -> goForward());
        reloadButton.setOnAction(event -> engine.reload());
        goButton.setOnAction(event -> openAddress());

        // Нажатие Enter в адресной строке тоже открывает страницу.
        addressBar.setOnAction(event -> openAddress());

        // Показываем фактический адрес загруженной страницы.
        engine.locationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isBlank()) {
                addressBar.setText(newValue);
            }
        });

        // Показываем название сайта в заголовке окна.
        engine.titleProperty().addListener((observable, oldTitle, newTitle) -> {
            if (newTitle == null || newTitle.isBlank()) {
                stage.setTitle("Мой браузер");
            } else {
                stage.setTitle(newTitle + " — Мой браузер");
            }
        });

        // Отображаем состояние загрузки страницы.
        engine.getLoadWorker().stateProperty().addListener(
                (observable, oldState, newState) -> {
                    if (newState == Worker.State.SCHEDULED
                            || newState == Worker.State.RUNNING) {
                        statusLabel.setText("Загрузка…");
                    } else if (newState == Worker.State.SUCCEEDED) {
                        statusLabel.setText("Страница загружена");
                    } else if (newState == Worker.State.FAILED) {
                        statusLabel.setText("Не удалось загрузить страницу");
                    } else if (newState == Worker.State.CANCELLED) {
                        statusLabel.setText("Загрузка отменена");
                    }
                }
        );

        HBox toolbar = new HBox(
                8,
                backButton,
                forwardButton,
                reloadButton,
                addressBar,
                goButton
        );
        toolbar.setPadding(new Insets(10));

        // Адресная строка занимает свободное место на панели.
        HBox.setHgrow(addressBar, javafx.scene.layout.Priority.ALWAYS);

        HBox bottomBar = new HBox(12, progressBar, statusLabel);
        bottomBar.setPadding(new Insets(8));

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(webView);
        root.setBottom(bottomBar);

        stage.setTitle("Мой браузер");
        stage.setScene(new Scene(root, 1100, 750));
        stage.show();

        openAddress();
    }

    private void openAddress() {
        String input = addressBar.getText().trim();

        if (input.isEmpty()) {
            statusLabel.setText("Введите адрес или поисковый запрос");
            return;
        }

        String address;

        if (input.startsWith("https://") || input.startsWith("http://")) {
            address !input.contains(".")) {
            // Если введён текст, а не адрес, ищем его в Google.
            String query = URLEncoder.encode(input, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            address = "https://www.google.com/search?q=" + query;
        } else {
            // Например, example.com превращается в https://example.com.
            address = "https://" + input;
        }

        engine.load(address);
    }

    private void goBack() {
        var history = engine.getHistory();

        if (history.getCurrentIndex() > 0) {
            history.go(-1);
        }
    }

    private void goForward() {
        var history = engine.getHistory();

        if (history.getCurrentIndex() < history.getEntries().size() - 1) {
            history.go(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}