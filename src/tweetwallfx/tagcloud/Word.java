/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetwallfx.tagcloud;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

/**
 *
 * @author sven
 */
class Word implements Comparable<Word> {
    String text;
    double weight;
    private final Text textNode;
    private Point2D center = new Point2D(0, 0);

    public Word(String text, double weight) {
        this.text = text;
        this.weight = weight;
        textNode = new Text(text);
        textNode.setStyle("-fx-font-size: " + (int) (2 * weight));
        textNode.setBoundsType(TextBoundsType.VISUAL);
    }

    @Override
    public int compareTo(Word o) {
        return ((Double) weight).compareTo(o.weight);
    }

    public Text getNode() {
        return textNode;
    }

    public double getWidth() {
        return textNode.getLayoutBounds().getWidth() + 10;
    }

    public double getHeight() {
        return textNode.getLayoutBounds().getHeight() + 10;
    }

    public void setWordleCenter(Point2D center) {
        this.center = center;
    }

    public Point2D getWordleCenter() {
        return this.center;
    }

    public Bounds getBounds() {
        return new BoundingBox(center.getX() - getWidth() / 2d, center.getY() - getHeight() / 2d, getWidth(), getHeight());
    }

    @Override
    public String toString() {
        return "Word{" + "text=" + text + ", center=" + center + ", width=" + getWidth() + ", height=" + getHeight() + '}';
    }
    
}
