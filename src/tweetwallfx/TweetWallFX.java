package tweetwallfx;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.fxyz.cameras.CameraTransformer;
import org.fxyz.extras.Skybox;
import org.fxyz.tests.SkyBoxTest;

/**
 * TweetWallFX - Devoxx 2014
 * @johanvos @SvenNB @SeanMiPhillips @JPeredaDnr
 * 
 * JavaFX 3D Application that renders an SkyBox cube from F(x)yz library 
 * Inside the box there are several tori with rotating banners containing
 * snapshot of tweets related to @devoxx
 * 
 * TODO
 * -...
 */
public class TweetWallFX extends Application {
    
    private Group root;
    private Skybox skyBox;
    final Rotate rotateY = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
    private PerspectiveCamera camera;
    private final double cameraDistance = 5000;
    private final CameraTransformer cameraTransform = new CameraTransformer();
    
    private final Image 
        top = new Image(SkyBoxTest.class.getResource("res/top.png").toExternalForm()),
        bottom = new Image(SkyBoxTest.class.getResource("res/bottom.png").toExternalForm()),
        left = new Image(SkyBoxTest.class.getResource("res/left.png").toExternalForm()),
        right = new Image(SkyBoxTest.class.getResource("res/right.png").toExternalForm()),
        front = new Image(SkyBoxTest.class.getResource("res/front.png").toExternalForm()),
        back = new Image(SkyBoxTest.class.getResource("res/back.png").toExternalForm());
    
    @Override
    public void start(Stage primaryStage) {
        root = new Group();
        
        camera = new PerspectiveCamera(true);
        cameraTransform.setTranslate(0, 0, 0);
        cameraTransform.getChildren().addAll(camera);
        camera.setNearClip(0.1);
        camera.setFarClip(1000000.0);
        camera.setFieldOfView(42);
        camera.setTranslateZ(-cameraDistance);
        cameraTransform.rx.setAngle(15.0);
        
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(camera.getTranslateX());
        light.setTranslateY(camera.getTranslateY());
        light.setTranslateZ(camera.getTranslateZ());
        cameraTransform.getChildren().add(light);
                
        root.getChildren().add(cameraTransform);
        root.setDepthTest(DepthTest.ENABLE);
        
        // Load Skybox AFTER camera is initialized
        double size = 100000D;
        skyBox = new Skybox(top, bottom, left, right, front, back,
                            size, camera);
        
        Group toriGroup = new Group();
        toriGroup.setDepthTest(DepthTest.ENABLE);
        toriGroup.getChildren().add(cameraTransform);
        
        double tam = 2048;
        List<Point3D> fixPos=FXCollections.observableArrayList(new Point3D(tam/2, tam/2, tam/2),
                new Point3D(-tam/2, tam/2, -tam/2), new Point3D(tam/2, -tam/2, tam/2),
                new Point3D(-3*tam/4, -3*tam/4, 3*tam/4), new Point3D(0, 0, 0));
        
        IntStream.range(0,5).boxed().forEach(i->{
            Random r = new Random();
            float randomRadius = (float) ((r.nextFloat() * 100) + 550);
            float randomTubeRadius = (float) ((r.nextFloat() * 100) + 300);
            Color randomColor = new Color(r.nextDouble(), r.nextDouble(), r.nextDouble(), r.nextDouble());

            SegmentedTorus torus = new SegmentedTorus(56, 50, 0, randomRadius, randomTubeRadius-0, randomColor);
            SegmentedTorus twTorus = new SegmentedTorus(56, 50, 14, randomRadius, randomTubeRadius, randomColor.brighter().brighter());
            twTorus.setDiffuseMap(new Image(getClass().getResourceAsStream("tweet2.jpg"))); 
            
            Translate translate = new Translate(fixPos.get(i).getX(), fixPos.get(i).getY(), fixPos.get(i).getZ());
            Rotate rotateX = new Rotate(Math.random() * 45, Rotate.X_AXIS);
            Rotate rotateY0 = new Rotate(10, Rotate.Y_AXIS);
            Rotate rotateZ = new Rotate(Math.random() * 45, Rotate.Z_AXIS);

            torus.getTransforms().addAll(translate, rotateX, rotateY0, rotateZ, rotateY); 
            twTorus.getTransforms().addAll(translate, rotateX, rotateY0, rotateZ, rotateY);
            toriGroup.getChildren().addAll(torus,twTorus);
        });
        
        Scene scene = new Scene(new Group(root), 1024, 720, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.TRANSPARENT);
        scene.setCamera(camera);
        
        primaryStage.setTitle("TweetWallFX Test - Devoxx 2014");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        root.getChildren().addAll(skyBox, toriGroup);
        root.setAutoSizeChildren(true);
        
        /* ANIMATIONS */
        
        final Timeline timeTori = new Timeline();
        timeTori.setCycleCount(Timeline.INDEFINITE);
        final KeyValue kv1 = new KeyValue(rotateY.angleProperty(), 360);
        final KeyFrame kf1 = new KeyFrame(Duration.millis(20000), kv1);
        timeTori.getKeyFrames().addAll(kf1);
        timeTori.play();
        
        final Timeline timeSky = new Timeline();
        timeSky.setCycleCount(Timeline.INDEFINITE);
        final KeyValue kv2 = new KeyValue(skyBox.rotateProperty(), 360);
        final KeyFrame kf2 = new KeyFrame(Duration.millis(100000), kv2);
        timeSky.getKeyFrames().addAll(kf2);
        timeSky.play();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
