/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetwallfx.tagcloud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 *
 * @author sven
 */
public class WordleSkin extends SkinBase<Wordle> {

    private final Random rand = new Random();
    private final int dDeg = 10;
    private final double dRadius = 5.0;

    private final Map<Word, Text> word2TextMap = new HashMap<>();
    private double max;
    private double min;
    private final Pane pane;
    private List<Word> limitedWords;

    public WordleSkin(Wordle wordle) {
//        setStyle("-fx-border-width: 1px; -fx-border-color: black;");
        super(wordle);

        pane = new Pane();
        this.getChildren().add(pane);
        buildCloud();

        wordle.wordsProperty.addListener(newList -> {
            buildCloud();
        });
    }

    private void cloudToTweet() {

    }

    private void tweetToCloud() {

    }

    private void buildCloud() {

        System.out.println(pane.getLayoutBounds());
//        pane.setStyle("-fx-border-width: 1px; -fx-border-color: red;");
        pane.widthProperty().addListener(bounds -> {
            reLayout();
        });

        pane.heightProperty().addListener(bounds -> {
            reLayout();
        });

        List<Word> sortedWords = new ArrayList<>(getSkinnable().wordsProperty().getValue());

        limitedWords = sortedWords.stream().limit(30).collect(Collectors.toList());
        limitedWords.sort(Comparator.reverseOrder());

        max = limitedWords.get(0).weight;
        min = limitedWords.get(limitedWords.size() - 1).weight;

        Map<Word, Bounds> boundsMap = recalcTagLayout(limitedWords);

        List<Word> unusedWords = word2TextMap.keySet().stream().filter(word -> !boundsMap.containsKey(word)).collect(Collectors.toList());
        SequentialTransition morph = new SequentialTransition();
        
        ParallelTransition fadeOuts = new ParallelTransition();
        unusedWords.forEach(word -> {

            Text textNode = word2TextMap.remove(word);

            FadeTransition ft = new FadeTransition(Duration.seconds(1.5), textNode);
            ft.setToValue(0);
            fadeOuts.getChildren().add(ft);
            ft.setOnFinished((event) -> {
                pane.getChildren().remove(textNode);
            });
        });
        
        morph.getChildren().add(fadeOuts);
        
        List<Word> oldWords = boundsMap.keySet().stream().filter(word -> word2TextMap.containsKey(word)).collect(Collectors.toList());
        ParallelTransition moves = new ParallelTransition();

        oldWords.forEach(word -> {

            Text textNode = word2TextMap.get(word);
            fontSizeAdaption(textNode, word);
            Bounds bounds = boundsMap.get(word);

            LocationTransition lt = new LocationTransition(Duration.seconds(1.5), textNode);
            lt.setFromX(textNode.getLayoutX());
            lt.setFromY(textNode.getLayoutY());
            lt.setToX(bounds.getMinX());
            lt.setToY(bounds.getMinY() + bounds.getHeight() / 2d);
            moves.getChildren().add(lt);                    
//            lt.play();

//            textNode.setLayoutX(bounds.getMinX());
//            textNode.setLayoutY(bounds.getMinY()+bounds.getHeight()/2d);
        });

        morph.getChildren().add(moves);

        List<Word> newWords = boundsMap.keySet().stream().filter(word -> !word2TextMap.containsKey(word)).collect(Collectors.toList());
        ParallelTransition fadeIns = new ParallelTransition();

        newWords.forEach(word -> {
            Text textNode = createTextNode(word);

            word2TextMap.put(word, textNode);

            Bounds bounds = boundsMap.get(word);
            textNode.setLayoutX(bounds.getMinX());
            textNode.setLayoutY(bounds.getMinY() + bounds.getHeight() / 2d);
            textNode.setOpacity(0);
            pane.getChildren().add(textNode);
            FadeTransition ft = new FadeTransition(Duration.seconds(1.5), textNode);
            ft.setToValue(1);
            fadeIns.getChildren().add(ft);                        
//            ft.play();
        });
        morph.getChildren().add(fadeIns);
        morph.play();        
        reLayout();
    }

    private void reLayout() {
        Bounds layoutBounds = pane.getLayoutBounds();
        limitedWords.forEach(word -> {

            Text textNode = word2TextMap.get(word);
            textNode.getTransforms().clear();
            textNode.getTransforms().add(new Translate(layoutBounds.getWidth() / 2d, layoutBounds.getHeight() / 2d));
        });
        pane.layout();
    }

    private final Font defaultFont = Font.font("Andalus", FontWeight.BOLD, 18);

    private void fontSizeAdaption(Text text, Word word) {
        // maxFont = 48
        // minFont = 18
        // linear
        //y = a+bx
//        double size = defaultFont.getSize() + ((48-defaultFont.getSize())/(max-min)) * word.weight;
        // logarithmic
        // y = a * Math.ln(x) + b
        double a = (defaultFont.getSize() - 48) / (Math.log(min / max));
        double b = defaultFont.getSize() - a * Math.log(min);

        double size = a * Math.log(word.weight) + b;
//        System.out.println(word.text + " " + word.weight + " " + " Font: " + size);
        text.setFont(Font.font(defaultFont.getFamily(), size));
    }

    private Text createTextNode(Word word) {
        Text textNode = new Text(word.text);
        textNode.setStyle("-fx-fill: white; -fx-padding: 5px");
        fontSizeAdaption(textNode, word);
        return textNode;
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
                node.setLayoutX(startX + frac * (targetX-startX));
            }
            if (!Double.isNaN(startY)) {
                node.setLayoutY(startY + frac * (targetY-startY));
            }
        }

    }

}
