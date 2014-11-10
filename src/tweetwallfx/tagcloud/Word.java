/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetwallfx.tagcloud;

import javafx.geometry.Point2D;

/**
 *
 * @author sven
 */
class Word implements Comparable<Word> {
    String text;
    double weight;

    public Word(String text, double weight) {
        this.text = text;
        this.weight = weight;
    }

    @Override
    public int compareTo(Word o) {
        return ((Double) weight).compareTo(o.weight);
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Word{" + "text=" + text + ", weight=" + weight + '}';
    }
    
}
