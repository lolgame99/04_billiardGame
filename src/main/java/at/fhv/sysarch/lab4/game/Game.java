package at.fhv.sysarch.lab4.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import at.fhv.sysarch.lab4.CoordinateConverter;
import at.fhv.sysarch.lab4.physics.BallPocketedListener;
import at.fhv.sysarch.lab4.physics.ObjectsRestListener;
import at.fhv.sysarch.lab4.physics.Physics;
import at.fhv.sysarch.lab4.rendering.Renderer;
import javafx.scene.input.MouseEvent;
import org.dyn4j.dynamics.RaycastResult;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Rotation;
import org.dyn4j.geometry.Vector2;

import static at.fhv.sysarch.lab4.rendering.Renderer.SCALE;

public class Game implements BallPocketedListener, ObjectsRestListener {
    private final Renderer renderer;
    private Physics physics;
    private CoordinateConverter converter;

    private boolean ballsMoving = false;

    //foul flags
    private boolean foulWhiteBallPocketed;

    //player
    private boolean hasPlayed = false;

    public Game(Renderer renderer, Physics physics) {
        this.renderer = renderer;
        this.physics = physics;
        this.physics.setBallPocketedListener(this);
        this.physics.setObjectsRestListener(this);
        this.initWorld();
        this.converter = CoordinateConverter.getInstance();
    }

    public void onMousePressed(MouseEvent e) {
        if (ballsMoving)
            return;

        double x = e.getX();
        double y = e.getY();

        double pX = this.converter.screenToPhysicsX(x);
        double pY = this.converter.screenToPhysicsY(y);

        Cue cue = new Cue(x,y);
        this.renderer.setCue(Optional.of(cue));
    }

    public void onMouseReleased(MouseEvent e) {
        if (ballsMoving)
            return;

        Cue cue = this.renderer.getCue().get();
        Optional<Ray> ray = cue.getShotRay();
        if(ray.isPresent()){
            ArrayList<RaycastResult> results = new ArrayList<>();
            boolean result = this.physics.getWorld().raycast(ray.get(), 0.1,false,false,results);
            if (result && results.get(0).getBody().getUserData() instanceof Ball){
                RaycastResult hit = results.get(0);
                hit.getBody().applyForce(cue.getShotForce().multiply(SCALE));

            }
        }
        hasPlayed = true;
        this.renderer.setCue(Optional.empty());
    }

    public void setOnMouseDragged(MouseEvent e) {
        if (ballsMoving)
            return;

        double x = e.getX();
        double y = e.getY();

        this.renderer.getCue().get().setEnd(x,y);
    }

    private void placeBalls(List<Ball> balls) {
        Collections.shuffle(balls);

        // positioning the billard balls IN WORLD COORDINATES: meters
        int row = 0;
        int col = 0;
        int colSize = 5;

        double y0 = -2*Ball.Constants.RADIUS*2;
        double x0 = -Table.Constants.WIDTH * 0.25 - Ball.Constants.RADIUS;

        for (Ball b : balls) {
            double y = y0 + (2 * Ball.Constants.RADIUS * row) + (col * Ball.Constants.RADIUS);
            double x = x0 + (2 * Ball.Constants.RADIUS * col);

            b.setPosition(x, y);
            b.getBody().setLinearVelocity(0, 0);
            renderer.addBall(b);

            row++;

            if (row == colSize) {
                row = 0;
                col++;
                colSize--;
            }
        }
    }

    private void initWorld() {
        List<Ball> balls = new ArrayList<>();
        
        for (Ball b : Ball.values()) {
            if (b == Ball.WHITE)
                continue;

            balls.add(b);
            physics.getWorld().addBody(b.getBody());
        }
       
        this.placeBalls(balls);

        Ball.WHITE.setPosition(Table.Constants.WIDTH * 0.25, 0);
        physics.getWorld().addBody(Ball.WHITE.getBody());
        renderer.addBall(Ball.WHITE);
        
        Table table = new Table();
        physics.getWorld().addBody(table.getBody());
        renderer.setTable(table);
    }

    @Override
    public void onBallPocketed(Ball b) {
        b.getBody().setLinearVelocity(0, 0);

        if (b == Ball.WHITE) {
            foulWhiteBallPocketed = true;
        } else {
            renderer.removeBall(b);
            physics.getWorld().removeBody(b.getBody());
        }
    }

    @Override
    public void onEndAllObjectsRest() {
        ballsMoving = true;
    }

    @Override
    public void onStartAllObjectsRest() {
        if (hasPlayed){
            if (foulWhiteBallPocketed){

                renderer.setFoulMessage("Foul Play! White ball pocketed");
                Ball.WHITE.setPosition(Table.Constants.WIDTH * 0.25, 0);
            }

            ballsMoving = false;
        }
    }
}