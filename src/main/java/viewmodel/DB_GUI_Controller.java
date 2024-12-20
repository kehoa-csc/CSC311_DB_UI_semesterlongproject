package viewmodel;

import com.azure.storage.blob.BlobClient;
import dao.DbConnectivityClass;
import dao.StorageUploader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Person;
import service.MyLogger;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;

import static dao.DbConnectivityClass.status;

enum majors {
    Computer_Science,
    CPIS,
    Dentistry,
    English,
    Economics,
    Electrical_Engineering,
    Horticulture,
    Nursing,
    Sports_Management
}

public class DB_GUI_Controller implements Initializable {

    StorageUploader store= new StorageUploader();

    @FXML
    TextField first_name, last_name, department, email, imageURL;
    @FXML
    MenuItem newItem, editItem, deleteItem;
    @FXML
    ComboBox<String> major_drop;
    @FXML
    ImageView img_view;
    @FXML
    MenuBar menuBar;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_fn, tv_ln, tv_department, tv_major, tv_email;
    private final DbConnectivityClass cnUtil = new DbConnectivityClass();
    private final ObservableList<Person> data = cnUtil.getData();
    @FXML
    Button editButton;
    @FXML
    Button deleteButton;
    @FXML
    Button addButton;
    @FXML
    ProgressBar progressBar;
    @FXML
    TextField statusText;

    boolean canModify = false;
    boolean canAdd = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
            tv_fn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
            tv_ln.setCellValueFactory(new PropertyValueFactory<>("lastName"));
            tv_department.setCellValueFactory(new PropertyValueFactory<>("department"));
            tv_major.setCellValueFactory(new PropertyValueFactory<>("major"));
            tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
            tv.setItems(data);

            String curr;
            for(majors m: majors.values()) {
                curr = m.toString();
                curr = curr.replace('_',' ');
                major_drop.getItems().add(curr);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        textBoxCheck();
    }

    @FXML
    protected void textBoxCheck() {
        canModify =
                !(first_name.getText().isEmpty() ||
                        last_name.getText().isEmpty() ||
                        department.getText().isEmpty() ||
                        major_drop.getValue().isBlank() ||
                        email.getText().isEmpty());
        canAdd =
                (!first_name.getText().isEmpty() &&
                        !last_name.getText().isEmpty() &&
                        !department.getText().isEmpty() &&
                        !major_drop.getValue().isBlank() &&
                        !email.getText().isEmpty());

        editButton.setDisable(!canModify);
        deleteButton.setDisable(!canModify);
        editItem.setDisable(!canModify);
        deleteItem.setDisable(!canModify);
        newItem.setDisable(!canAdd);
        addButton.setDisable(!canAdd);
        newItem.setDisable(!canAdd);
        editItem.setDisable(!canModify);
        deleteItem.setDisable(!canModify);
    }

    @FXML
    protected void addNewRecord() {

            Person p = new Person(first_name.getText(), last_name.getText(), department.getText(),
                    major_drop.getValue(), email.getText(), imageURL.getText());
            cnUtil.insertUser(p);
            cnUtil.retrieveId(p);
            p.setId(cnUtil.retrieveId(p));
            data.add(p);
            clearForm();

            statusText.setText(status);
    }

    @FXML
    protected void clearForm() {
        first_name.setText("");
        last_name.setText("");
        department.setText("");
        major_drop.setValue("");
        email.setText("");
        imageURL.setText("");
        textBoxCheck();
        status = "Cleared details.";
        statusText.setText(status);
    }

    @FXML
    protected void logOut(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
            Scene scene = new Scene(root, 920, 620);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").getFile());
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    @FXML
    protected void displayAbout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/about.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void editRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        Person p2 = new Person(index + 1, first_name.getText(), last_name.getText(), department.getText(),
                major_drop.getValue(), email.getText(),  imageURL.getText());
        cnUtil.editUser(p.getId(), p2);
        data.remove(p);
        data.add(index, p2);
        tv.getSelectionModel().select(index);
    }

    @FXML
    protected void deleteRecord() {
        Person p = tv.getSelectionModel().getSelectedItem();
        int index = data.indexOf(p);
        cnUtil.deleteRecord(p);
        data.remove(index);
        tv.getSelectionModel().select(index);
    }

    @FXML
    protected void showImage() {
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            img_view.setImage(new Image(file.toURI().toString()));

            Task<Void> uploadTask = createUploadTask(file, progressBar);
            progressBar.progressProperty().bind(uploadTask.progressProperty());
            new Thread(uploadTask).start();
        }


    }

    @FXML
    protected void addRecord() {
        showSomeone();
    }

    @FXML
    protected void selectedItemTV(MouseEvent mouseEvent) {
        Person p = tv.getSelectionModel().getSelectedItem();
        first_name.setText(p.getFirstName());
        last_name.setText(p.getLastName());
        department.setText(p.getDepartment());
        major_drop.setValue(p.getMajor());
        email.setText(p.getEmail());
        imageURL.setText(p.getImageURL());
        textBoxCheck();
    }

    public void lightTheme(ActionEvent actionEvent) {
        try {
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.getScene().getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            System.out.println("light " + scene.getStylesheets());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void darkTheme(ActionEvent actionEvent) {
        try {
            Stage stage = (Stage) menuBar.getScene().getWindow();
            Scene scene = stage.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/css/ak-darkmode.css").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showSomeone() {
        Dialog<Results> dialog = new Dialog<>();
        dialog.setTitle("New User");
        dialog.setHeaderText("Please specify…");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField textField1 = new TextField("Name");
        TextField textField2 = new TextField("Last Name");
        TextField textField3 = new TextField("Email ");
        ObservableList<Major> options =
                FXCollections.observableArrayList(Major.values());
        ComboBox<Major> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().selectFirst();
        dialogPane.setContent(new VBox(8, textField1, textField2,textField3, comboBox));
        Platform.runLater(textField1::requestFocus);
        dialog.setResultConverter((ButtonType button) -> {
            if (button == ButtonType.OK) {
                return new Results(textField1.getText(),
                        textField2.getText(), comboBox.getValue());
            }
            return null;
        });
        Optional<Results> optionalResult = dialog.showAndWait();
        optionalResult.ifPresent((Results results) -> {
            MyLogger.makeLog(
                    results.fname + " " + results.lname + " " + results.major);
        });
    }

    private static enum Major {Business, CSC, CPIS}

    private static class Results {

        String fname;
        String lname;
        Major major;

        public Results(String name, String date, Major venue) {
            this.fname = name;
            this.lname = date;
            this.major = venue;
        }
    }

    private Task<Void> createUploadTask(File file, ProgressBar progressBar) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                BlobClient blobClient = store.getContainerClient().getBlobClient(file.getName());
                long fileSize = Files.size(file.toPath());
                long uploadedBytes = 0;

                try (FileInputStream fileInputStream = new FileInputStream(file);
                     OutputStream blobOutputStream = blobClient.getBlockBlobClient().getBlobOutputStream()) {

                    byte[] buffer = new byte[1024 * 1024]; // 1 MB buffer size
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        blobOutputStream.write(buffer, 0, bytesRead);
                        uploadedBytes += bytesRead;

                        // Calculate and update progress as a percentage
                        int progress = (int) ((double) uploadedBytes / fileSize * 100);
                        updateProgress(progress, 100);
                    }
                }

                return null;
            }
        };
    }

    @FXML
    protected void importCsv() throws FileNotFoundException {
        System.out.println("importCsv");
        File file = (new FileChooser()).showOpenDialog(img_view.getScene().getWindow());
        //Scanner sc = new Scanner(new File("src/main/resources/csvtest.csv"));
        Scanner sc = new Scanner(file);
        sc.nextLine();
        try {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!line.isEmpty()) {
                    String[] parts = line.split(",");
                    cnUtil.insertUser(new Person(parts[0], parts[1], parts[2], parts[3], parts[4], ""));
                }
            }
            status = "Imported CSV successfully.";

        } catch (Exception e) {
            status = "Error importing CSV. Do you have duplicate entries?";
        }
        statusText.setText(status);
        sc.close();
        tv.setItems(cnUtil.getData());
    }

    @FXML
    protected void exportCsv() throws IOException {
        System.out.println("exportCsv");
        FileWriter fw = new FileWriter("src/main/resources/export.csv");
        File file = new File("src/main/resources/export.csv");
        file.createNewFile();

        fw.write("firstname,lastname,department,major,email\n");
        fw.write(cnUtil.stringAllUsers());

        status = "Exported to " + file.getAbsolutePath();
        statusText.setText(status);

        fw.close();
    }

    @FXML
    protected void formatDoc() throws IOException {
        File htmlFile = new File("docs/csv-guide.html");
        Desktop.getDesktop().browse(htmlFile.toURI());
    }

    @FXML
    protected void helpDoc() throws IOException {
        File htmlFile = new File("docs/index.html");
        Desktop.getDesktop().browse(htmlFile.toURI());
    }

    @FXML
    protected void copyEntry() {
        Person p = tv.getSelectionModel().getSelectedItem();
        tv.getSelectionModel().clearSelection();
        p.setFirstName(first_name.getText()+"_copy");
        p.setLastName(last_name.getText()+"_copy");
        p.setEmail(email.getText()+"_copy");
        cnUtil.insertUser(p);

        status = "Copied user successfully.";
        statusText.setText(status);
        tv.refresh();
        //tv.setItems(cnUtil.getData());
    }
}