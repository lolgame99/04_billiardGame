package at.fhv.sysarch.lab4.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import at.fhv.sysarch.lab4.physics.Physics;
import at.fhv.sysarch.lab4.rendering.Renderer;
import javafx.scene.input.MouseEvent;
import org.dyn4j.dynamics.RaycastResult;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;

public class Game {
    private final Renderer renderer;
    private Physics physics;

    public Game(Renderer renderer, Physics physics) {
        this.renderer = renderer;
        this.physics = physics;
        this.initWorld();
    }

    public void onMousePressed(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        double pX = this.renderer.screenToPhysicsX(x);
        double pY = this.renderer.screenToPhysicsY(y);

        Cue cue = new Cue(x,y);
        this.renderer.setCue(Optional.of(cue));
    }

    public void onMouseReleased(MouseEvent e) {
        Cue cue = this.renderer.getCue().get();


        Vector2 start = new Vector2(
                this.renderer.screenToPhysicsX(cue.getStartX()),
                this.renderer.screenToPhysicsY(cue.getStartY())
        );
        Vector2 end = new Vector2(
                this.renderer.screenToPhysicsX(cue.getEndX()),
                this.renderer.screenToPhysicsY(cue.getEndY())
        );

        Ray ray = new Ray(start, end);
        this.renderer.addDebugVector(ray.getStart(), ray.getDirectionVector());

        ArrayList<RaycastResult> results = new ArrayList<>();
        boolean result = this.physics.getWorld().raycast(ray, 1.0,false,false,results);
        if (result && results.get(0).getBody().getUserData() instanceof Ball){
            RaycastResult hit = results.get(0);
            System.out.println(((Ball) hit.getBody().getUserData()).getColor() + " Ball hit");
            hit.getBody().applyForce(start.subtract(end).multiply(500));
        }
        this.renderer.setCue(Optional.empty());
    }

    public void setOnMouseDragged(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();

        this.renderer.getCue().get().setEnd(x,y);

        double pX = renderer.screenToPhysicsX(x);
        double pY = renderer.screenToPhysicsY(y);
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
}