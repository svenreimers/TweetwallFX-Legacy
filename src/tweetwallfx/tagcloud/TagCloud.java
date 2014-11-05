/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetwallfx.tagcloud;

import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import twitter.TwitterOAuth;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.conf.Configuration;

/**
 *
 * @author sven
 */
public class TagCloud extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            
            Configuration readOAuth = TwitterOAuth.getInstance().readOAuth();
            
            Query query = new Query("#devoxx");
            query.setCount(100);
            Twitter twitter = new TwitterFactory(readOAuth).getInstance();
            QueryResult result = twitter.search(query);
            
            Wordle wordle = new Wordle();
            buildTagCloud(result.getTweets()).entrySet().stream().filter(entry -> entry.getValue() > 10).
                    sorted(Comparator.comparing(entry -> entry.getValue())).limit(20).forEach(entry -> wordle.addWord(entry.getKey(), entry.getValue()));
//            wordle.addWord("JavaEE", 3);
//            wordle.addWord("Java", 4);
//            wordle.addWord("JavaScript", 2);
//            wordle.addWord("AngularJS", 1);
//            wordle.addWord("HTML5", 1);
//            wordle.addWord("JavaFX", 1);
//            wordle.addWord("#keynote", 3);
//            wordle.addWord("@stephan007", 2);
            
            Scene scene = new Scene(wordle, 300, 250);
            
            primaryStage.setTitle("Tag Cloud Example!");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (TwitterException ex) {
            Logger.getLogger(TagCloud.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private TreeMap<String, Long> buildTagCloud(List<Status> tweets) {
        Stream<String> map = tweets.stream()
                .map(t -> t.getText().replaceAll("[^\\dA-Za-z ]", " "));
        Pattern pattern = Pattern.compile("\\s+");
        TreeMap<String, Long> answer = map
                .flatMap(c -> pattern.splitAsStream(c))
                .filter(l -> l.length() > 2)
                .collect(Collectors.groupingBy(String::toLowerCase, TreeMap::new, Collectors.counting()));
        System.out.println("#######33words: "+answer.size());

        answer.forEach((t,u) -> { if (u > 1)
            System.out.println(t+ " --> "+u);
        });
        return answer;
    }       

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
