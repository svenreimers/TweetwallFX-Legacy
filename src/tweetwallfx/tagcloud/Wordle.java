/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetwallfx.tagcloud;

import java.util.List;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;

/**
 *
 * @author sven
 */
class Wordle extends Pane {

    private Random rand = new Random();
    private int dDeg = 10;
    private double dRadius = 5.0;
    ObservableList<Word> words = FXCollections.<Word>observableArrayList();
    List<Word> sortedWords = new SortedList<>(words);

    void addWord(String word, double weight) {
        Word w = new Word(word, weight);
        words.add(w);
        getChildren().add(w.getNode());
    }

    @Override
    protected void layoutChildren() {
        for (int i = 1; i < sortedWords.size(); ++i) {
            Word word = sortedWords.get(i);
            Point2D center = new Point2D(0, 0);
            double totalWeight = 0.0;
            if (i != 0) {
                for (int prev = 0; prev < i; ++prev) {
                    Word wPrev = sortedWords.get(prev);
                    center = center.add((wPrev.getWidth() / 2d) * wPrev.weight, (wPrev.getHeight() / 2d) * wPrev.weight);
                    totalWeight += wPrev.weight;
                }
                center = center.multiply(1 / totalWeight);
            }
            boolean done = false;
            double radius = 0.5 * Math.min(sortedWords.get(0).getWidth(), sortedWords.get(0).getHeight());
            while (!done) {
                int startDeg = rand.nextInt(360);
                double prev_x = -1;
                double prev_y = -1;
                for (int deg = startDeg; deg < startDeg + 360; deg += dDeg) {
//                        System.out.println("DEG: " + deg);
                    double rad = ((double) deg / Math.PI) * 180.0;
                    center = center.add(radius * Math.cos(rad), radius * Math.sin(rad));
                    if (prev_x == center.getX() && prev_y == center.getY()) {
                        continue;
                    }
                    prev_x = center.getX();
                    prev_y = center.getY();
                    Bounds mayBe = new BoundingBox(center.getX() - word.getWidth() / 2d, center.getY() - word.getHeight() / 2d, word.getWidth(), word.getHeight());
                    int prev = 0;
                    boolean useable = true;
                    for (prev = 0; prev < i; ++prev) {
//                            System.out.println("MayBe: " + mayBe + " ? " + mayBe.isEmpty());
//                            System.out.println("intersects: " + prev + " " + words.get(prev).getBounds() + " ? " + words.get(prev).getBounds().isEmpty());
                        if (mayBe.intersects(words.get(prev).getBounds())) {
                            useable = false;
                            break;
                        }
                    }
                    if (useable) {
                        done = true;
                        word.setWordleCenter(center);
//                            System.out.println("Done " + word + " " + word.getBounds());
                        break;
                    }
                }
                radius += this.dRadius;
            }
        }
        Bounds ownBounds = getLayoutBounds();
        double shiftXCenter = ownBounds.getWidth() / 2d;
        double shiftYCenter = ownBounds.getHeight() / 2d;
        words.forEach((tweetwallfx.tagcloud.Word word) -> {
            word.getNode().setLayoutX(word.getWordleCenter().getX() - word.getWidth() / 2d + shiftXCenter);
            word.getNode().setLayoutY(word.getWordleCenter().getY() - word.getHeight() / 2d + shiftYCenter);
        });
    }

}
