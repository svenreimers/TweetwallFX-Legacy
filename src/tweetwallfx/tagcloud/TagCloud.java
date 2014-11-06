/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetwallfx.tagcloud;

import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
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
    private final String hashtag = "#Google";
    private TagTweets tweetsTask;
    
    @Override
    public void start(Stage primaryStage) {
        
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 800, 600);
            
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
                tweetsTask= new TagTweets(conf, hashtag, root);
                tweetsTask.start();
            }
        });
        
        primaryStage.setTitle("Tag Cloud Example!");
        primaryStage.setScene(scene);
        primaryStage.show();
        
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
