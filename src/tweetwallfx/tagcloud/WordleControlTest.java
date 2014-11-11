/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetwallfx.tagcloud;

import java.util.Arrays;
import java.util.List;
import javafx.application.Application;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tweetwallfx.jdub1581.DevoxxBillboardLogo;

/**
 *
 * @author sven
 */
public class WordleControlTest extends Application {
    
//    private PerspectiveCamera camera;
//    private final CameraTransformer cameraTransform = new CameraTransformer();
    private SubScene subScene ;
    
    
    @Override
    public void start(Stage primaryStage) {
        Group root3D = new Group();
        
        DevoxxBillboardLogo logo = new DevoxxBillboardLogo(1500,700); 
        logo.setFrequency(30.0f);
        logo.setPeriod(30.0f);
        logo.setWaveLength(300.0f);
        logo.setAmplitude(30.0f);
        logo.setTranslateZ(100);
        logo.setTranslateX(960);
        logo.setTranslateY(540);
        logo.setScaleX(1.01);
        logo.setScaleY(1.01);
        root3D.setDepthTest(DepthTest.ENABLE);
        root3D.getChildren().addAll(logo);
                
        subScene = new SubScene(root3D, 1920, 1080,true,SceneAntialiasing.BALANCED);
        
        AnchorPane root = new AnchorPane();
        Wordle wordle = new Wordle();
        
        List<Word> words = Arrays.asList(new Word[] {
                new Word("#Devoxx", 20), 
                new Word("trip", 10),
                new Word("fun", 15),
                new Word("Java", 15),
                new Word("JavaFX", 25),
                new Word("Java EE", 5),
        });
        
        wordle.setWords(words);
        
        BorderPane pane = new BorderPane(wordle);
        root.setStyle("-fx-background-color: transparent;");
        pane.setStyle("-fx-background-color: transparent;");
        AnchorPane.setBottomAnchor(pane, 0d);
        AnchorPane.setTopAnchor(pane, 0d);
        AnchorPane.setRightAnchor(pane, 0d);
        AnchorPane.setLeftAnchor(pane, 0d);
        root.getChildren().add(subScene);
        root.getChildren().add(pane);
        
        Scene scene = new Scene(root, 1920, 1080, true, SceneAntialiasing.BALANCED);
        
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.show();
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
