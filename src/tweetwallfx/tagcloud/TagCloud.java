/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetwallfx.tagcloud;

import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tweetwallfx.jdub1581.DevoxxBillboardLogo;
import twitter.CLogOut;
import twitter.TwitterOAuth;
import twitter4j.conf.Configuration;

/**
 *
 * @author sven
 */
public class TagCloud extends Application {

    private Configuration conf;
    private CLogOut log;
    private final String hashtag = "#devoxx";
    private TagTweets tweetsTask;
    
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
                
        SubScene subScene = new SubScene(root3D, 1920, 1080,true,SceneAntialiasing.BALANCED);
        
        AnchorPane root = new AnchorPane();
        BorderPane borderPane = new BorderPane();
        root.setStyle("-fx-background-color: transparent;");
        borderPane.setStyle("-fx-background-color: transparent;");
        AnchorPane.setBottomAnchor(borderPane, 0d);
        AnchorPane.setTopAnchor(borderPane, 0d);
        AnchorPane.setRightAnchor(borderPane, 0d);
        AnchorPane.setLeftAnchor(borderPane, 0d);
        root.getChildren().add(subScene);
        root.getChildren().add(borderPane);
        
        Scene scene = new Scene(root, 1920, 1080, true, SceneAntialiasing.BALANCED);
        
            /* TWITTER */
//        log=CLogOut.getInstance();
//        log.getMessages().addListener((ov,s,s1)->System.out.println(s1));
            
            final Service service=new Service<Void>(){
                @Override protected Task<Void> createTask() {   
                    Task<Void> task = new Task<Void>(){
                        @Override protected Void call() throws Exception {
                            conf = TwitterOAuth.getInstance().readOAuth();
                            return null;
                        }
                    };
                    return task;
                }
            };
            
            service.setOnSucceeded(e->{
                if(!hashtag.isEmpty() && conf!=null){
                    tweetsTask= new TagTweets(conf, hashtag, borderPane);
                    tweetsTask.start();
                }
            });
            
            primaryStage.setTitle("The JavaFX Tweetwall for Devoxx!");
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint("");
            service.start();
    }
    
    @Override
    public void stop() {
        System.out.println("closing...");
        if(tweetsTask!=null){
            tweetsTask.stop();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
