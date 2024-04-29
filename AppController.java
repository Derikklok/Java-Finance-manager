package org.example.epsilon;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import java.util.Random;


import java.io.IOException;

import java.sql.*;

public class AppController {
    public static final String ACCOUNT_SID = "AC1ddb123a10a3691bea3c5b9ef980c556";
    public static final String AUTH_TOKEN = "b085c8ce98256837b5ef890324fb763b";

    public static int otp;


    @FXML
    private TextField txtotpfield;

    @FXML
    private TextField txtUser,txtPass,uName,uPass;
    @FXML
    private Button backButton;

    private static final String URL      = "jdbc:mysql://localhost:3306/epsilon_database";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "sachin1605";



    @FXML
    protected void onRegisterButtonClick() throws IOException {
        System.out.println("Register Button clicked");
        openRegisterWindow();
    }

    private void openRegisterWindow() {
        System.out.println("Open reg window");
        try {
            // Load the new FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Second-Window-Reg.fxml"));
            Scene scene = new Scene(loader.load());

            // Create a new stage
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Register");
            stage.show();
        } catch (IOException e) {
            System.out.println("Error in loading registration window");
        }
    }

    @FXML
    protected void onRegisterButtonClick2() throws SQLException {
        System.out.println("Button 2 of Register-view has clicked");
        String userName = uName.getText();
        String userPassword = uPass.getText();
        generateTheOtp();
        updateToTable(userName,userPassword,otp);

        //openVerificationWinodow();
    }

    private void generateTheOtp() {
        Random random = new Random();
        otp = random.nextInt(900000) + 100000; // Generates a random number between 100000 and 999999
        System.out.println("Random 6-digit number: " + otp);
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber("+94755264100"),
                new com.twilio.type.PhoneNumber("+13345085814"),
                "Please use verification code : "+otp).create();

        System.out.println("Msg SID : "+message.getSid());
    }

    private void updateToTable(String utName, String ptwd, int otp) throws SQLException {
        Connection connection = null;
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL,USERNAME,PASSWORD);

            if (connection !=null){
                System.out.println("Connection Successfull");

                String sql = "INSERT INTO user_details (USERNAME,PASSWORD,VERIFICATION_NUM) VALUES (?,?,?)";

                PreparedStatement statement = connection.prepareStatement(sql);

                statement.setString(1, utName);
                statement.setString(2, ptwd);
                statement.setInt(3, otp);

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("A new row has been inserted successfully!");
                    openVerificationWinodow();
                }
            }else{
                System.out.println("not connected");
            }
        }catch(ClassNotFoundException | SQLException e){
            System.out.println("Exception");
        }finally{
            connection.close();
        }

    }

    private void openVerificationWinodow() {
        System.out.println("Verifying...");
        try {
            // Load the new FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Verification-Window.fxml"));
            Scene scene = new Scene(loader.load());

            // Create a new stage
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Verification");
            stage.show();
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
                // Close the window
                stage.close();
            }));
            timeline.setCycleCount(1); // Run only once
            timeline.play();
        } catch (IOException e) {
            System.out.println("Error in loading verification window");
        }

    }

    @FXML
    protected void onVerifyOTPBtnClick(){
        System.out.println("OTP page");
        String enteredOtp  = txtotpfield.getText();
        try {
            getOTPRelatedToUsername(txtUser.getText());
        } catch (SQLException e) {
            System.out.println("Error passing values to check");
        }

    }

    private void getOTPRelatedToUsername(String uname) throws SQLException {
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL,USERNAME,PASSWORD);

            if (connection !=null){
                System.out.println("Connection Successfull");

                String sql = "SELECT VERIFICATION_NUM FROM user_details WHERE USERNAME = ?  ";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, txtUser.getText());
//                pstmt.setString(2, userPassword);

                rs = pstmt.executeQuery();

                if (rs.next()) {
                    int count = rs.getInt(1); // Get the count from the result set

                    // Check if the count is greater than 0
                    if (count > 0) {
                        System.out.println("Access Granted!");
                        openSystemView();
                        // openMainSystemWindow();
                    } else {
                        System.out.println("Access Denied!");
                    }
                }
            }else{
                System.out.println("not connected");
            }
        }catch(ClassNotFoundException | SQLException e){
            System.out.println("Exception when accessing database");
        }finally{
            connection.close();
        }
    }

    @FXML
    protected void onLoginBtnClick() throws SQLException {
        System.out.println("System Aceess");
        String userName = txtUser.getText();
        String userPassword = txtPass.getText();
        System.out.println(userName+userPassword);
        userAccessValidation(userName,userPassword);
        //openSystemView();

    }

    private void userAccessValidation(String userName, String userPassword) throws SQLException {
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL,USERNAME,PASSWORD);

            if (connection !=null){
                System.out.println("Connection Successfull");

                String sql = "SELECT COUNT(*) FROM user_details WHERE USERNAME = ? AND PASSWORD = ?";
                pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, userName);
                pstmt.setString(2, userPassword);

                rs = pstmt.executeQuery();

                if (rs.next()) {
                    int count = rs.getInt(1); // Get the count from the result set

                    // Check if the count is greater than 0
                    if (count > 0) {
                        System.out.println("Access Granted!");
                        openSystemView();
                       // openMainSystemWindow();
                    } else {
                        System.out.println("Access Denied!");
                    }
                }
            }else{
                System.out.println("not connected");
            }
        }catch(ClassNotFoundException | SQLException e){
            System.out.println("Exception when accessing database");
        }finally{
            connection.close();
        }
    }

    private void openSystemView() {
        try {
            // Load the new FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("System-view.fxml"));
            Scene scene = new Scene(loader.load());

            // Create a new stage
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("EPSILON");
            stage.show();
        } catch (IOException e) {
            System.out.println("Error in loading registration window");
        }
    }

    @FXML
    protected void onBudgetBtnClick(){
        System.out.println("Budget Btn Clicked");
        openBudgetWindow();
    }
    private void openBudgetWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Budget-Window.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("Budgets");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.out.println("error loading budget window");
        }
    }

    @FXML
    private void onCollectionBtnClick(){
        System.out.println("Create collections btn clicked");
    }

    @FXML
    private void onBackBtn(){
        System.out.println("Back Button clicked");
        // Get the reference to the stage
        Stage stage = (Stage) backButton.getScene().getWindow();
        // Close the stage (window)
        stage.close();
    }
    @FXML
    protected void onsavingsBtnClick(){
        System.out.println("Savings Button clicked");
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Savings-Window.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("Manage Savings");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.out.println("error loading savings window");
        }
    }


}
