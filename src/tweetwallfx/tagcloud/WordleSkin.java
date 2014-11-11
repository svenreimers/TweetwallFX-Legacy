/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetwallfx.tagcloud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import tweetwallfx.tagcloud.Wordle.LayoutMode;
import twitter.TweetInfo;

/**
 *
 * @author sven
 */
public class WordleSkin extends SkinBase<Wordle> {

    private final Random rand = new Random();
    private final int dDeg = 10;
    private final double dRadius = 5.0;

    // used for TagCloud
    private final Map<Word, Text> word2TextMap = new HashMap<>();
    // 
    private final List<TweetWordNode> tweetWordList = new ArrayList<>();
    private double max;
    private double min;
    private final Pane pane;
    private List<Word> limitedWords;
    private Set<Word> tweetWords = Collections.emptySet();

    public WordleSkin(Wordle wordle) {
        super(wordle);

        pane = new Pane();
        pane.setStyle("-fx-border-width: 1px; -fx-border-color: black;");
        this.getChildren().add(pane);
        updateCloud();

        wordle.wordsProperty.addListener((obs, oldValue, newValue) -> {
            System.out.println("Words Update: " + newValue.size());
            switch (wordle.layoutModeProperty.get()) {
                case TWEET:
                    break;
                case WORDLE:
                    updateCloud();
                    break;
            }
        });

        pane.widthProperty().addListener(bounds -> {
            switch (wordle.layoutModeProperty.get()) {
                case TWEET:
                    break;
                case WORDLE:
                    updateCloud();
                    break;
            }
        });

        pane.heightProperty().addListener(bounds -> {
            switch (wordle.layoutModeProperty.get()) {
                case TWEET:
                    break;
                case WORDLE:
                    updateCloud();
                    break;
            }
        });

        wordle.layoutModeProperty.addListener((obs, oldVModee, newMode) -> {
            switch (newMode) {
                case TWEET:
//                    addTweetToCloud();

                    cloudToTweet();
                    break;
                case WORDLE:
                    tweetToCloud();
//                    removeTweetFromCloud();
                    break;
            }
        });

        wordle.tweetInfoProperty.addListener((obs, oldValue, newValue) -> {
            String text = newValue.getText();
            tweetWords = pattern.splitAsStream(text)
                    .filter(l -> l.length() > 2)
                    .filter(l -> !l.startsWith("@"))
                    .filter(l -> !l.startsWith("http:"))
                    .filter(l -> !l.startsWith("https:"))
                    .map(l -> l.toLowerCase())
                    .filter(l -> !stopList.contains(l)).map(l -> new Word(l, -2)).collect(Collectors.toSet());

        });
    }

    private final List<String> stopList = new ArrayList<>(
            Arrays.asList("http", "https", "has", "have", "do", "for", "are", "the", "and",
                    "with", "here", "#devoxx", "active", "see", "next", "will", "any", "off", "there", "while", "just", "all", "from", "got", "think", "nice",
                    "ask", "can", "you", "week", "some", "not", "didn", "isn", "per", "how", "show", "out", "but", "last", "your", "one", "should",
                    "now", "also", "done", "will", "become", "did", "what", "when", "let", "that", "this", "always", "where", "our"));

//    private void addTweetToCloud() {
//        System.out.println("Add tweet to cloud");
//        String text = getSkinnable().tweetInfoProperty.get().getText();
//        tweetWords = pattern.splitAsStream(text)
//                .filter(l -> l.length() > 2)
//                .filter(l -> !l.startsWith("@"))
//                .filter(l -> !l.startsWith("http:"))
//                .filter(l -> !l.startsWith("https:"))
//                .map(l -> l.toLowerCase())
//                .filter(l -> !stopList.contains(l)).map(l -> new Word(l, 0)).collect(Collectors.toSet());                
//        List<Word> words = getSkinnable().wordsProperty.get();
//        words.addAll(tweetWords);
//        Platform.runLater(() -> getSkinnable().wordsProperty.set(words));
//    }
    private void removeTweetFromCloud() {
        tweetWords = Collections.emptySet();
        updateCloud();
    }

    private void cloudToTweet() {

        Bounds layoutBounds = pane.getLayoutBounds();

        List<TweetWord> tweetLayout = recalcTweetLayout(getSkinnable().tweetInfoProperty.get());

        ParallelTransition fadeOuts = new ParallelTransition();
        ParallelTransition grows = new ParallelTransition();
        ParallelTransition moves = new ParallelTransition();
        ParallelTransition fadeIns = new ParallelTransition();
        SequentialTransition morph = new SequentialTransition(fadeOuts, grows, moves, fadeIns);

        tweetLayout.stream().forEach(tweetWord -> {
            Word word = new Word(tweetWord.text.trim(), -2);
//            System.out.println("Word: " + word.text + " in " + word2TextMap.keySet());
            if (word2TextMap.containsKey(word)) {
//                System.out.println("MOV TWL: " + word.text);
                Text textNode = word2TextMap.remove(word);
                tweetWordList.add(new TweetWordNode(tweetWord, textNode));
                // fontSizeAdaption(textNode, -1);
                
                FontSizeTransition ft = new FontSizeTransition(Duration.seconds(1.5), textNode);
                ft.setFromSize(textNode.getFont().getSize());
                ft.setToSize(getFontSize(-1));
                grows.getChildren().add(ft);
                
                Bounds bounds = tweetLayout.stream().filter(tw -> tw.text.trim().equals(word.text)).findFirst().get().bounds;

                LocationTransition lt = new LocationTransition(Duration.seconds(1.5), textNode);
                lt.setFromX(textNode.getLayoutX());
                lt.setFromY(textNode.getLayoutY());
                lt.setToX(bounds.getMinX());
                lt.setToY(layoutBounds.getHeight() - bounds.getMaxY());
                moves.getChildren().add(lt);
//                TranslateTransition tt = new TranslateTransition(Duration.seconds(1.5), textNode);
//                tt.setToX(-pane.getWidth()/2d);
//                tt.setToY(-pane.getHeight()/2d);
//                moves.getChildren().add(tt);
            } else {
                Text textNode = createTextNode(word);
//                System.out.println("NEW TWL: " + word.text);

                fontSizeAdaption(textNode, -1);
                tweetWordList.add(new TweetWordNode(tweetWord, textNode));

                Bounds bounds = tweetWord.bounds;

                textNode.setLayoutX(bounds.getMinX());
                textNode.setLayoutY(layoutBounds.getHeight() - bounds.getMaxY());
                textNode.setOpacity(0);
                pane.getChildren().add(textNode);
                FadeTransition ft = new FadeTransition(Duration.seconds(1.5), textNode);
                ft.setToValue(1);
                fadeIns.getChildren().add(ft);
            }
        });

        // kill the remaining words from the cloud
        word2TextMap.entrySet().forEach(entry -> {
//            System.out.println("DEL TWL: " + entry.getKey().text);
            Text textNode = entry.getValue();
            FadeTransition ft = new FadeTransition(Duration.seconds(1.5), textNode);
            ft.setToValue(0);
            fadeOuts.getChildren().add(ft);
            ft.setOnFinished((event) -> {
                pane.getChildren().remove(textNode);
            });
        });
        word2TextMap.clear();
        morph.play();
    }

    private void tweetToCloud() {
        List<Word> sortedWords = new ArrayList<>(getSkinnable().wordsProperty().getValue());

        if (sortedWords.isEmpty()) {
            return;
        }

        limitedWords = sortedWords.stream().limit(MAX_CLOUD_TAGS).collect(Collectors.toList());
//        limitedWords.addAll(tweetWords);
        limitedWords.sort(Comparator.reverseOrder());

        max = limitedWords.get(0).weight;
        min = limitedWords.stream().filter(w -> w.weight > 0).min(Comparator.naturalOrder()).get().weight;

        Map<Word, Bounds> boundsMap = recalcTagLayout(limitedWords);

        ParallelTransition fadeOuts = new ParallelTransition();
        ParallelTransition moves = new ParallelTransition();
        ParallelTransition shrinks = new ParallelTransition();
        ParallelTransition fadeIns = new ParallelTransition();
        SequentialTransition morph = new SequentialTransition(fadeOuts, moves, shrinks, fadeIns);
        
        Bounds layoutBounds = pane.getLayoutBounds();
        
        boundsMap.entrySet().stream().forEach(entry -> {
            Word word = entry.getKey();
            Bounds bounds = entry.getValue();
            Optional<TweetWordNode> optionalTweetWord = tweetWordList.stream().filter(tweetWord -> tweetWord.tweetWord.text.trim().equals(word.text)).findFirst();
            if (optionalTweetWord.isPresent()) {
                System.out.println("MOV TCL: " + entry.getKey().text);
                boolean removed = tweetWordList.remove(optionalTweetWord.get());
                System.out.println("Removed: " + optionalTweetWord.get().tweetWord.text + " " + removed);
                Text textNode = optionalTweetWord.get().textNode;

                word2TextMap.put(word, textNode);
                LocationTransition lt = new LocationTransition(Duration.seconds(1.5), textNode);

                System.out.println("Move Word: " + word.text + " " + bounds);
                
                lt.setFromX(textNode.getLayoutX());
                lt.setFromY(textNode.getLayoutY());
                lt.setToX(bounds.getMinX() + layoutBounds.getWidth() / 2d);
                lt.setToY(bounds.getMinY() + layoutBounds.getHeight() / 2d + bounds.getHeight() / 2d);
                moves.getChildren().add(lt);

                
                FontSizeTransition ft = new FontSizeTransition(Duration.seconds(1.5), textNode);
                ft.setFromSize(textNode.getFont().getSize());
                ft.setToSize(getFontSize(word.weight));
                shrinks.getChildren().add(ft);
                
                
            } else {
                Text textNode = createTextNode(word);
                System.out.println("NEW TCL: " + entry.getKey().text);

                word2TextMap.put(word, textNode);
                System.out.println("Word: " + word.text + " " + bounds);
                textNode.setLayoutX(bounds.getMinX() + layoutBounds.getWidth() / 2d);
                textNode.setLayoutY(bounds.getMinY() + layoutBounds.getHeight() /2d + bounds.getHeight() / 2d);
                textNode.setOpacity(0);
                pane.getChildren().add(textNode);
                FadeTransition ft = new FadeTransition(Duration.seconds(1.5), textNode);
                ft.setToValue(1);
                fadeIns.getChildren().add(ft);
            }
        });

        tweetWordList.forEach(tweetWord -> {
            System.out.println("DEL TCL: " + tweetWord.tweetWord.text);            
            FadeTransition ft = new FadeTransition(Duration.seconds(1.5), tweetWord.textNode);
            ft.setToValue(0);
            fadeOuts.getChildren().add(ft);
            ft.setOnFinished((event) -> {
                pane.getChildren().remove(tweetWord.textNode);
            });
        });

        tweetWordList.clear();

        morph.play();
   }

    private void updateCloud() {
        System.out.println("Update Cloud");
//        pane.setStyle("-fx-border-width: 1px; -fx-border-color: red;");
        List<Word> sortedWords = new ArrayList<>(getSkinnable().wordsProperty().getValue());
        if (sortedWords.isEmpty()) {
            return;
        }

        limitedWords = sortedWords.stream().limit(MAX_CLOUD_TAGS).collect(Collectors.toList());
//        System.out.println("Limited words: " + limitedWords.size());
//        System.out.println("limitedWords: " + limitedWords);
//        limitedWords.addAll(tweetWords);
//        System.out.println("Limited words (incl. tweet words): " + limitedWords.size());
//        System.out.println("tweetWords: " + tweetWords);
        limitedWords.sort(Comparator.reverseOrder());

        max = limitedWords.get(0).weight;
        min = limitedWords.stream().filter(w -> w.weight > 0).min(Comparator.naturalOrder()).get().weight;

//        System.out.println("Max Weight: " + max + ", min Weight: " + min);

        Map<Word, Bounds> boundsMap = recalcTagLayout(limitedWords);
        Bounds layoutBounds = pane.getLayoutBounds();

        List<Word> unusedWords = word2TextMap.keySet().stream().filter(word -> !boundsMap.containsKey(word)).collect(Collectors.toList());
        SequentialTransition morph = new SequentialTransition();


        ParallelTransition fadeOuts = new ParallelTransition();
        unusedWords.forEach(word -> {
//            System.out.println("Fade out: " + word.text);
            Text textNode = word2TextMap.remove(word);

            FadeTransition ft = new FadeTransition(Duration.seconds(1.5), textNode);
            ft.setToValue(0);
            fadeOuts.getChildren().add(ft);
            ft.setOnFinished((event) -> {
                pane.getChildren().remove(textNode);
            });
        });

        morph.getChildren().add(fadeOuts);

        List<Word> existingWords = boundsMap.keySet().stream().filter(word -> word2TextMap.containsKey(word)).collect(Collectors.toList());
        ParallelTransition moves = new ParallelTransition();

        existingWords.forEach(word -> {

//            System.out.println("Move word: " + word.text);
            Text textNode = word2TextMap.get(word);
            fontSizeAdaption(textNode, word.weight);
            Bounds bounds = boundsMap.get(word);

            LocationTransition lt = new LocationTransition(Duration.seconds(1.5), textNode);
            lt.setFromX(textNode.getLayoutX());
            lt.setFromY(textNode.getLayoutY());
            lt.setToX(bounds.getMinX() + layoutBounds.getWidth() / 2d);
            lt.setToY(bounds.getMinY() + + layoutBounds.getHeight() / 2d + bounds.getHeight() / 2d); 
            moves.getChildren().add(lt);
        });

        morph.getChildren().add(moves);

        List<Word> newWords = boundsMap.keySet().stream().filter(word -> !word2TextMap.containsKey(word)).collect(Collectors.toList());
        ParallelTransition fadeIns = new ParallelTransition();

        newWords.forEach(word -> {
            Text textNode = createTextNode(word);
//            System.out.println("new word: " + word.text);
            word2TextMap.put(word, textNode);

            Bounds bounds = boundsMap.get(word);
            textNode.setLayoutX(bounds.getMinX() + layoutBounds.getWidth() / 2d);
            textNode.setLayoutY(bounds.getMinY()  + layoutBounds.getHeight() / 2d + bounds.getHeight() / 2d);
            textNode.setOpacity(0);
            pane.getChildren().add(textNode);
            FadeTransition ft = new FadeTransition(Duration.seconds(1.5), textNode);
            ft.setToValue(1);
            fadeIns.getChildren().add(ft);
        });
        morph.getChildren().add(fadeIns);
        morph.play();
//        reLayout(true);
    }

    private static final int MAX_CLOUD_TAGS = 40;

//    private void reLayout(boolean ignoreMode) {
//        if (ignoreMode) {
//            reLayout();
//        } else if (LayoutMode.WORDLE == getSkinnable().layoutModeProperty.get()) {
//            reLayout();
//        }
//    }
//
//    private void reLayout() {
//        System.out.println("Relayouting");
//        Bounds layoutBounds = pane.getLayoutBounds();
//        word2TextMap.entrySet().forEach(entry -> {
//            Text textNode = entry.getValue();
//            if (null != textNode) {
//                textNode.getTransforms().clear();
//                textNode.getTransforms().add(new Translate(layoutBounds.getWidth() / 2d, layoutBounds.getHeight() / 2d));
//            }
//        });
//        pane.layout();
//    }

    private final Font defaultFont = Font.font("Andalus", FontWeight.BOLD, 18);

    private  double getFontSize(double weight) {
        // maxFont = 48
        // minFont = 18

        double size = defaultFont.getSize();
        if (weight == -1) {
            size = 24;
        } else if (weight == -2) {
            size = 18;            
        } else {
            // linear
            //y = a+bx
//        double size = defaultFont.getSize() + ((48-defaultFont.getSize())/(max-min)) * word.weight;
            // logarithmic
            // y = a * Math.ln(x) + b
            double a = (defaultFont.getSize() - 48) / (Math.log(min / max));
            double b = defaultFont.getSize() - a * Math.log(min);
            size = a * Math.log(weight) + b;
        }
//        System.out.println(word.text + " " + word.weight + " " + " Font: " + size);
        return size;
    }
    
    private void fontSizeAdaption(Text text, double weight) {
        text.setFont(Font.font(defaultFont.getFamily(), getFontSize(weight)));
    }

    private Text createTextNode(Word word) {
        Text textNode = new Text(word.text);
        textNode.setStyle("-fx-fill: white; -fx-padding: 5px");
        fontSizeAdaption(textNode, word.weight);
        return textNode;
    }

    private final Pattern pattern = Pattern.compile("\\s+");

    private List<TweetWord> recalcTweetLayout(TweetInfo info) {
        System.out.println("TweetLayouting");
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(300);
        pattern.splitAsStream(info.getText())
                .forEach(w -> {
                    Text textWord = new Text(w.concat(" "));
                    String color = "#292F33";
                    textWord.setStyle("-fx-fill: " + color + ";");
                    textWord.setFont(Font.font(defaultFont.getFamily(), 24));
                    flow.getChildren().add(textWord);
                });
        flow.requestLayout();
        return flow.getChildren().stream().map(node -> new TweetWord(node.getBoundsInParent(), ((Text) node).getText())).collect(Collectors.toList());
    }

    private Map<Word, Bounds> recalcTagLayout(List<Word> words) {
        List<Bounds> boundsList = new ArrayList<>();
        Text firstNode = createTextNode(words.get(0));
        double firstWidth = firstNode.getLayoutBounds().getWidth();
        double firstHeight = firstNode.getLayoutBounds().getHeight();

        boundsList.add(new BoundingBox(0,
                0, firstWidth, firstHeight));

        for (int i = 1; i < words.size(); ++i) {
            Word word = words.get(i);
            Text textNode = createTextNode(word);
            double width = textNode.getLayoutBounds().getWidth();
            double height = textNode.getLayoutBounds().getHeight();

            Point2D center = new Point2D(0, 0);
            double totalWeight = 0.0;
            for (int prev = 0; prev < i; ++prev) {
                Bounds prevBounds = boundsList.get(prev);
                double weight = words.get(prev).weight;
                center = center.add((prevBounds.getWidth() / 2d) * weight, (prevBounds.getHeight() / 2d) * weight);
                totalWeight += weight;
            }
            center = center.multiply(1d / totalWeight);
            boolean done = false;
            double radius = 0.5 * Math.min(boundsList.get(0).getWidth(), boundsList.get(0).getHeight());
            while (!done) {
                int startDeg = rand.nextInt(360);
                double prev_x = -1;
                double prev_y = -1;
                for (int deg = startDeg; deg < startDeg + 360; deg += dDeg) {
                    double rad = ((double) deg / Math.PI) * 180.0;
                    center = center.add(radius * Math.cos(rad), radius * Math.sin(rad));
                    if (prev_x == center.getX() && prev_y == center.getY()) {
                        continue;
                    }
                    prev_x = center.getX();
                    prev_y = center.getY();
                    Bounds mayBe = new BoundingBox(center.getX() - width / 2d,
                            center.getY() - height / 2d, width, height);
                    boolean useable = true;
                    for (int prev = 0; prev < i; ++prev) {
                        if (mayBe.intersects(boundsList.get(prev))) {
                            useable = false;
                            break;
                        }
                    }
                    if (useable) {
                        done = true;
                        boundsList.add(new BoundingBox(center.getX() - width / 2d,
                                center.getY() - height / 2d, width, height));
                        break;
                    }
                }
                radius += this.dRadius;
            }
        }

        Map<Word, Bounds> boundsMap = new HashMap<>();

        for (int k = 0; k < words.size(); k++) {
            boundsMap.put(words.get(k), boundsList.get(k));
        }
        return boundsMap;
    }

    private static class LocationTransition extends Transition {

        private final Node node;
        private double startX;
        private double startY;
        private double targetY;
        private double targetX;

        public LocationTransition(Duration duration, Node node) {
            setCycleDuration(duration);
            this.node = node;
        }

        public void setFromX(double startX) {
            this.startX = startX;
        }

        public void setFromY(double startY) {
            this.startY = startY;
        }

        public void setToX(double targetX) {
            this.targetX = targetX;
        }

        public void setToY(double targetY) {
            this.targetY = targetY;
        }

        @Override
        protected void interpolate(double frac) {
            if (!Double.isNaN(startX)) {
                node.setLayoutX(startX + frac * (targetX - startX));
            }
            if (!Double.isNaN(startY)) {
                node.setLayoutY(startY + frac * (targetY - startY));
            }
        }

    }
    
    private static class FontSizeTransition extends Transition {

        private final Text node;
        private double startSize;
        private double toSize;

        public FontSizeTransition(Duration duration, Text node) {
            setCycleDuration(duration);
            this.node = node;
        }

        public void setFromSize(double startSize) {
            this.startSize = startSize;
        }

        public void setToSize(double toSize) {
            this.toSize = toSize;
        }

        @Override
        protected void interpolate(double frac) {
            if (!Double.isNaN(startSize)) {
                node.setFont(Font.font(node.getFont().getFamily(), startSize + frac * (toSize - startSize)));
            }
        }

    }
    

    private static class TweetWord {

        Bounds bounds;
        String text;

        public TweetWord(Bounds bounds, String text) {
            this.bounds = bounds;
            this.text = text;
        }

        @Override
        public String toString() {
            return "TweetWord{" + "text=" + text + ", bounds=" + bounds + '}';
        }

    }

    private static class TweetWordNode {

        private TweetWord tweetWord;
        private Text textNode;

        public TweetWordNode(TweetWord tweetWord, Text textNode) {
            this.tweetWord = tweetWord;
            this.textNode = textNode;
        }

    }

}
