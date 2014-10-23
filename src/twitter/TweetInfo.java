package twitter;

import java.util.Date;
import twitter4j.Status;

/**
 * TweetWallFX - Devoxx 2014
 * @johanvos @SvenNB @SeanMiPhillips @jdub1581 @JPeredaDnr
 *
 * @author jpereda
 */
public class TweetInfo {
    
    private final Status status;
    
    public TweetInfo(Status status){
        this.status=status;
    }

    public String getName(){
        return status.getUser().getName();
    }
    
    public String getText(){
        return status.getText();
    }
    
    public String getImageURL(){
        return status.getUser().getProfileImageURL();
    }
    
    public String getHandle(){
        return status.getUser().getScreenName();
    }
    
    public Date getDate(){
        return status.getCreatedAt();
    }
}
