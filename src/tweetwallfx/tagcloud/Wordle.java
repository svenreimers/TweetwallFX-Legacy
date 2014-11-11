/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetwallfx.tagcloud;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import twitter.TweetInfo;

/**
 *
 * @author sven
 */
public class Wordle extends Control {

    enum LayoutMode { WORDLE, TWEET }
    
    ObjectProperty<List<Word>> wordsProperty = new SimpleObjectProperty<>(new ArrayList<>());
    ObjectProperty<TweetInfo> tweetInfoProperty = new SimpleObjectProperty<>();

    ObjectProperty<LayoutMode> layoutModeProperty = new SimpleObjectProperty<>(LayoutMode.WORDLE);

    public Wordle() {
    }

    public void setTweet(TweetInfo status) {
        tweetInfoProperty.set(status);
    }    
    
    public ObjectProperty<TweetInfo> tweetInfoProperty() {
        return tweetInfoProperty;
    }
    
    public void setWords(List<Word> words) {
        wordsProperty.set(words);
    }

    public ObjectProperty<List<Word>> wordsProperty() {
        return wordsProperty;
    }

    public void setLayoutMode(LayoutMode layoutMode) {
        layoutModeProperty.set(layoutMode);
    }

    public ObjectProperty<LayoutMode> layoutModeProperty() {
        return layoutModeProperty;
    }
    
    @Override
    protected Skin<?> createDefaultSkin() {
        return new WordleSkin(this);
    }

}
