package tweetwallfx.tagcloud;

import java.util.Random;
import java.util.stream.IntStream;
import javafx.animation.AnimationTimer;
import javafx.application.*;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Chapter 12. JavaFX and Leap Motion
 * @author jpereda
 */
public class RippleEffect extends Application {
    
    private int original[], water[];
    private short waterMap[];
    private int width, height, halfWidth, halfHeight, size;
    private int oldInd, newInd, mapInd;
    private final StackPane root = new StackPane();
    private Scene scene;
    
    private AnimationTimer timerEffect;
    private AnimationTimer timerTouch;
    private long lastEffect, lastTouch;
    
    @Override
    public void start(Stage primaryStage) {
        Image img=new Image(getClass().getResourceAsStream("devoxx_nosponsors.jpg")); 
        final ImageView imageView = new ImageView(img);
        width = (int)imageView.getImage().getWidth();
        height = (int)imageView.getImage().getHeight();
        halfWidth=width>>1;
        halfHeight=height>>1;
        size = width * (height+2)*2;
        waterMap = new short[size];
        water = new int[width*height];
        original = new int[width*height];
        oldInd = width;
        newInd = width * (height+3);
        PixelReader pixelReader = imageView.getImage().getPixelReader();
        pixelReader.getPixels(0,0,width,height, 
                WritablePixelFormat.getIntArgbInstance(),original, 0,width);
        
        root.getChildren().add(imageView);
        scene = new Scene(root, width, height);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, t1 -> {
            if(t1.getCode() == KeyCode.ESCAPE){ Platform.exit(); }
        });
        
        lastEffect = System.nanoTime();
        timerEffect = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastEffect + 30_000_000l) {
                    imageView.setImage(applyEffect());
                    lastEffect = now;
                }
            }
        };
       
        lastTouch = System.nanoTime();
        timerTouch = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTouch + 2_000_000_000l) {
                    generateTouch();
                    lastTouch= now;
                }
            }
        };
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        generateTouch();
        
        timerTouch.start();
        
        timerEffect.start();
    }
    
    private void generateTouch(){
        Random r=new Random();
        IntStream.range(0, r.nextInt(10)).boxed().forEach(i->{
            Point2D d=root.sceneToLocal(
                    r.nextInt(width)-scene.getX()-scene.getWindow().getX(),
                    r.nextInt(height)-scene.getY()-scene.getWindow().getY());
            double dx=d.getX(), dy=d.getY(), dz = Math.abs(r.nextInt(200)); 
            int rad=(dz<50)?5:((dz<150)?4:3);
            if(dx>=0d && dx<=root.getWidth() && dy>=0d && dy<=root.getHeight()){
                waterDrop((int)dx,(int)dy,rad);
            }
        });
    }
    
    private void waterDrop(int dx, int dy, int rad) {
        for (int j=dy-rad; j<dy+rad; j++) {
            for (int k=dx-rad; k<dx+rad; k++) {
                if (j>=0 && j<height && k>=0 && k<width) {
                    waterMap[oldInd+(j*width)+k] += 128;            
                } 
            }
        }
    }
    
    private Image applyEffect() {
        int a,b, i=oldInd;
        oldInd=newInd;
        newInd=i;
        i=0;
        mapInd=oldInd;
        for (int y=0;y<height;y++) {
            for (int x=0;x<width;x++) {
                short data = (short)((waterMap[mapInd-width]+waterMap[mapInd+width]+
                                      waterMap[mapInd-1]+waterMap[mapInd+1])>>1);
                data -= waterMap[newInd+i];
                data -= data>>4;
                waterMap[newInd+i]=data;
                data = (short)(1024-data);
                a=((x-halfWidth)*data/1024)+halfWidth;
                if (a>=width){ a=width-1; }
                if (a<0){ a=0; }
                b=((y-halfHeight)*data/1024)+halfHeight;
                if (b>=height){ b=height-1; }
                if (b<0){ b=0; }
                water[i++]=original[a+(b*width)];
                mapInd++;
            }
        }
        WritableImage raster = new WritableImage(width, height);
        PixelWriter pixelWriter = raster.getPixelWriter();
        pixelWriter.setPixels(0,0,width,height, 
                PixelFormat.getIntArgbInstance(),water, 0,width);
        return raster;
    } 
    
    public static void main(String[] args) {
        launch(args);
    }
}