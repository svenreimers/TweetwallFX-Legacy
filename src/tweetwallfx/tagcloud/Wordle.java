/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetwallfx.tagcloud;

import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import twitter4j.Status;

/**
 *
 * @author sven
 */
public class Wordle extends Control {

    ObjectProperty<List<Word>> wordsProperty = new SimpleObjectProperty<>();
    ObjectProperty<Status> statusProperty = new SimpleObjectProperty<>();

    public Wordle() {
    }

    public void setTweet(Status status) {
        statusProperty.set(status);
    }

    public ObjectProperty<Status> statusProperty() {
        return statusProperty;
    }
    
    public void setWords(List<Word> words) {
        wordsProperty.set(words);
    }

    public ObjectProperty<List<Word>> wordsProperty() {
        return wordsProperty;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new WordleSkin(this);
    }

}
