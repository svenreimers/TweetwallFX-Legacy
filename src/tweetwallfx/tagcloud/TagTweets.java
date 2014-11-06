package tweetwallfx.tagcloud;

import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import tweetwallfx.tagcloud.Wordle;
import twitter.TweetInfo;
import twitter4j.FilterQuery;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;

/**
 * TweetWallFX - Devoxx 2014
 * {@literal @}johanvos {@literal @}SvenNB {@literal @}SeanMiPhillips {@literal @}jdub1581 {@literal @}JPeredaDnr
 * 
 * Tasks to perform a search on Twitter for some hashtag, create an HBox with each
 * tweets, crate a snapshot and then load the image as diffuseMap of a segmented torus
 * Tasks and blockingQueues take care of this complex process
 * @author José Pereda
 */
public class TagTweets {
    
    private final static int MIN_WEIGHT = 4;
    private final static int NUM_MAX_WORDS = 30;
    
    private final List<String> stopList=new ArrayList<>(
            Arrays.asList("http","https","has","have","do","for","are","the","and","with"));
    
    private final Pattern pattern = Pattern.compile("\\s+");
    
    private final ExecutorService showTweetsExecutor = createExecutor("ShowTweets");
    private final ShowTweetsTask showTweetsTask;
    
    private final Wordle wordle = new Wordle();
    
    private TreeMap<String,Long> tree;
    
    private final String searchText;
    private final Configuration conf;
    private final StackPane root;
    private final HBox hBottom = new HBox();
    
    
    public TagTweets(Configuration conf, String searchText, StackPane root){
        this.conf=conf;
        this.searchText=searchText;
        this.root=root;
        this.showTweetsTask=new ShowTweetsTask(conf, searchText);
    }
    
    public void start(){
        try {
            System.out.println("** 1. Creating Tag Cloud for "+searchText);
            Query query = new Query(searchText);
            query.setCount(100);
            Twitter twitter = new TwitterFactory(conf).getInstance();
            QueryResult result = twitter.search(query);
            
            System.out.print("TAGS: ");
            tree = buildTagCloud(result.getTweets());            
            
            tree.entrySet().stream()
//                .filter(entry -> entry.getValue() > MIN_WEIGHT)
                .sorted(Map.Entry.comparingByValue(Long::compareTo))
                .skip(tree.size()-NUM_MAX_WORDS)
//                    .peek(e->System.out.print(e+" "))
                .forEach(entry -> wordle.addWord(entry.getKey(), entry.getValue()));
//            System.out.println("");
            
            Platform.runLater(()->{
            
                HBox hWordle = new HBox(wordle);
                hWordle.setAlignment(Pos.CENTER);
                hWordle.setPadding(new Insets(20));
                VBox.setVgrow(hWordle,Priority.ALWAYS);
                
                hBottom.setMinHeight(150);
                hBottom.setPrefHeight(150);
                VBox vbox = new VBox(hWordle, hBottom);
                
                root.getChildren().setAll(vbox);
               
                wordle.requestLayout();
            });
            
        } catch (TwitterException ex) {
            System.out.println("Error Twitter: "+ex);
            return;
        }
        
        System.out.println("** 2. Starting new Tweets search for "+searchText);
        showTweetsExecutor.execute(showTweetsTask);
    }
    
    public void stop() {
        showTweetsExecutor.shutdown();
        try {
            showTweetsExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
        }
    }
    
    private class TweetsCreationTask extends Task<Void> {
        private final String searchText;
        private TwitterStream stream;
        private final Configuration conf;
        private final BlockingQueue<Parent> tweets;
    
        public TweetsCreationTask(Configuration conf, String searchText, BlockingQueue<Parent> tweets) {
            this.conf=conf;
            this.searchText = searchText;
            this.tweets = tweets;
        }

        @Override protected Void call() throws Exception {
            FilterQuery query = new FilterQuery();
            query.track(new String[]{searchText});
            if(conf!=null){
                stream=new TwitterStreamFactory(conf).getInstance();
                addListener(s->{
                    try {
                        TweetInfo info = new TweetInfo(s);
                        List<String> words=checkTags(info);
                        if(words.size()>0){
                            System.out.println("Tw: "+info.getText());
                            tweets.put(createTweetInfoBox(info,words));
                        } else {
                            System.out.println("No valid tags");
                        }
                    } catch (InterruptedException ex) {
                        System.out.println("Error: "+ex);
                    }
                });
                stream.filter(query);
            }
            return null;

        }

        private void addListener(Consumer<Status> consumer){
            stream.addListener(new StatusAdapter(){
                @Override
                public void onStatus(Status status){
                   consumer.accept(status);
                }
            });
        }


        private Parent createTweetInfoBox(TweetInfo info, List<String> words) {
            HBox hbox = new HBox(20);
            hbox.setStyle("-fx-padding: 20px;");
            hbox.setPrefHeight(150);
            
            HBox hImage = new HBox();
            hImage.setPadding(new Insets(10));
            Image image = new Image(info.getImageURL(), 48, 48, true, false);
            ImageView imageView = new ImageView(image);
            Rectangle clip = new Rectangle(48, 48);
            clip.setArcWidth(10);
            clip.setArcHeight(10);
            imageView.setClip(clip);
            hImage.getChildren().add(imageView);

            HBox hName = new HBox(20);
            Label name = new Label(info.getName());
            name.setStyle("-fx-font: 24px \"Andalus\"; -fx-text-fill: #292F33; -fx-font-weight: bold;");
            DateFormat df = new SimpleDateFormat("HH:mm:ss"); 
            Label handle = new Label("@"+info.getHandle()+" · "+df.format(info.getDate()));
            handle.setStyle("-fx-font: 22px \"Andalus\"; -fx-text-fill: #8899A6;");
            hName.getChildren().addAll(name,handle);
        
            System.out.print("WORDS: ");
            words.forEach(w->System.out.print(w+", "));
            System.out.println("");
//            wordle.formatWords(words);

            String status=info.getText().replaceAll("[^\\dA-Za-z ]", " ");
        
            List<String> collect = pattern.splitAsStream(status).collect(Collectors.toList());
            
            TextFlow flow = new TextFlow();
            collect.forEach(w->{
                Text textWord = new Text(w.concat(" "));
                textWord.setStyle("-fx-font: 18px \"Andalus\"; -fx-fill: "+(words.contains(w.toLowerCase())?"red;":"#292F33;"));
                flow.getChildren().add(textWord);
            });
            VBox vbox = new VBox(10);
            vbox.getChildren().addAll(hName, flow);
            hbox.getChildren().addAll(hImage, vbox);
            
            return hbox;
        }

        private List<String> checkTags(TweetInfo info) {
            
            String status=info.getText().replaceAll("[^\\dA-Za-z ]", " ");
        
            List<String> collect = pattern.splitAsStream(status).map(s->s.toLowerCase())
                    .collect(Collectors.toList());
            
            List<String> words = tree.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Long::compareTo))
                    .skip(tree.size()-NUM_MAX_WORDS)
                    .filter(entry->collect.contains(entry.getKey()))
//                    .peek(System.out::println)
                    .map(entry->entry.getKey())
                    .collect(Collectors.toList());
            return words;
        }
    }
  
    private class TweetsSnapshotTask extends Task<Void> {
        private final BlockingQueue<Parent> tweets;

        TweetsSnapshotTask(BlockingQueue<Parent> tweets) {
            this.tweets = tweets;
        }

        @Override protected Void call() throws Exception {
            while(true) {
                if(isCancelled()){
                    break;
                }
                snapshotTweet(tweets.take());
            }
            return null;
        }

        private void snapshotTweet(final Parent tweetContainer) throws InterruptedException {
            final CountDownLatch latch = new CountDownLatch(1);
            // render the chart in an offscreen scene (scene is used to allow css processing) and snapshot it to an image.
            // the snapshot is done in runlater as it must occur on the javafx application thread.
            Platform.runLater(() -> {
                System.out.println("** Loading new Tweet & updating Wall");
                hBottom.getChildren().setAll(tweetContainer);
                
                wordle.requestLayout();
                
                latch.countDown();
            });

            latch.await();
        }
    }
  
    private class ShowTweetsTask<Void> extends Task {
        private final BlockingQueue<Parent>        tweets         = new ArrayBlockingQueue(5);
        private final BlockingQueue<BufferedImage> bufferedImages = new ArrayBlockingQueue(5);
        private final ExecutorService    tweetsCreationExecutor   = createExecutor("CreateTweets");
        private final ExecutorService    tweetsSnapshotExecutor   = createExecutor("TakeSnapshots");
        private final TweetsCreationTask tweetsCreationTask;
        private final TweetsSnapshotTask tweetsSnapshotTask;

        ShowTweetsTask(final Configuration conf, final String textSearch) {
            tweetsCreationTask = new TweetsCreationTask(conf, textSearch, tweets);
            tweetsSnapshotTask = new TweetsSnapshotTask(tweets);
            
            setOnCancelled(e -> {
                tweetsCreationTask.cancel();
                tweetsSnapshotTask.cancel();
            });

        }

        @Override protected Void call() throws Exception {
            tweetsCreationExecutor.execute(tweetsCreationTask);
            tweetsSnapshotExecutor.execute(tweetsSnapshotTask);
            
            tweetsCreationExecutor.shutdown();
            tweetsSnapshotExecutor.shutdown();

            try {
                tweetsSnapshotExecutor.awaitTermination(1, TimeUnit.DAYS);
            } catch (InterruptedException e) {} 

            return null;
        }
    } 
  
    private ExecutorService createExecutor(final String name) {       
        ThreadFactory factory = r -> {
            Thread t = new Thread(r);
            t.setName(name);
            t.setDaemon(true);
            return t;
        };
        return Executors.newSingleThreadExecutor(factory);
    }  

    private TreeMap<String, Long> buildTagCloud(List<Status> tweets) {
        Stream<String> map = tweets.stream()
                .map(t -> t.getText().replaceAll("[^\\dA-Za-z ]", " "));
        TreeMap<String, Long> answer = map
                .flatMap(c -> pattern.splitAsStream(c))
                .filter(l -> l.length() > 2)
                .filter(l->!stopList.contains(l.toLowerCase()))
                .collect(Collectors.groupingBy(String::toLowerCase, TreeMap::new, Collectors.counting()));
        System.out.println("#######33words: "+answer.size());

//        answer.forEach((t,u) -> { if (u > 1)
//            System.out.println(t+ " --> "+u);
//        });
        return answer;
    }
}
