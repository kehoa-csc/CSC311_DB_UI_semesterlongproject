package viewmodel;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;


public class LoginController {

    @FXML
    TextField usernameTextField;
    @FXML
    PasswordField passwordField;

    boolean valid = false;
    FileReader fr;
    Scanner sc;
    FileWriter fw;

    int attempts = 0;
    public static String currUser;

    @FXML
    private GridPane rootpane;

    @FXML
    private Label topLabel;

    public void initialize() {
        try {
             fr = new FileReader("src/main/resources/logins.txt");
             sc = new Scanner(fr);
        } catch (FileNotFoundException e) {
            System.out.println("Logins file not found. Cannot proceed with login.");
        }

        rootpane.setBackground(new Background(
                        createImage("https://edencoding.com/wp-content/uploads/2021/03/layer_06_1920x1080.png"),
                        null,
                        null,
                        null,
                        null,
                        null
                )
        );

        rootpane.setOpacity(0);
        FadeTransition fadeOut2 = new FadeTransition(Duration.seconds(7), rootpane);
        fadeOut2.setFromValue(0);
        fadeOut2.setToValue(1);
        fadeOut2.play();

    }
    private static BackgroundImage createImage(String url) {
        return new BackgroundImage(
                new Image(url),
                BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT,
                new BackgroundPosition(Side.LEFT, 0, true, Side.BOTTOM, 0, true),
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, false, true));
    }
    @FXML
    public void login(ActionEvent actionEvent) throws FileNotFoundException {
        valid = false;
        //Stop accepting after 10 attempts
        if (attempts >= 10) {
            topLabel.setText("Too many attempts. Please try again later.");
            return;
        } else {attempts++;}

        System.out.println("Checking login...");
        topLabel.setText("Checking login...");
        //Check if fields are blank, if so break and inform user
        if (usernameTextField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            topLabel.setText("Username or password is empty. Please try again.");
            return;
        }
        String[] login = new String[2];
        String[] loginScan = new String[2];
        login[0] = usernameTextField.getText();
        login[1] = passwordField.getText();
        //Reading from file to see if login is there
        fr = new FileReader("src/main/resources/logins.txt");
        sc = new Scanner(fr);
        while(sc.hasNextLine()) {
            System.out.println("checking attempt " + attempts);
            loginScan[0] = sc.nextLine();
            loginScan[1] = sc.nextLine();

            if(login[0].equals(loginScan[0]) && login[1].equals(loginScan[1])) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            topLabel.setText("Invalid username or password. (Attempt "+attempts+")");
            System.out.println("Invalid username or password. (Attempt "+attempts+")");
        } else { //If valid
            topLabel.setText("Valid login! Loading...");
            System.out.println("Valid login! Loading...");
            currUser = login[0];
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/view/db_interface_gui.fxml"));
                System.out.println("Loading FXML...");
                Scene scene = new Scene(root, 920, 620);
                System.out.println("Making scene...");
                scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
                System.out.println("Loading CSS...");
                Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                System.out.println("Setting scene...");
                window.setScene(scene);
                System.out.println("Showing window...");
                window.show();
                window.toFront();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    public void signUp(ActionEvent actionEvent) throws IOException {

        System.out.println("signup");
        try {
            fw = new FileWriter("src/main/resources/logins.txt",true);
        } catch (IOException e) {
            System.out.println("Logins file not found. Cannot proceed with signup.");
        }

        if (usernameTextField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            topLabel.setText("Provide signup details.");
        } else {
            fw.write(usernameTextField.getText() + "\n");
            fw.write(passwordField.getText() + "\n");
            fw.close();
            topLabel.setText("Signup successful! You can now use this login.");
        }
        /*try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/signUp.fxml"));
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/lightTheme.css").toExternalForm());
            Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            window.setScene(scene);
            window.show();
        } catch (Exception e) {
            e.printStackTrace();
        }*/


    }


}
