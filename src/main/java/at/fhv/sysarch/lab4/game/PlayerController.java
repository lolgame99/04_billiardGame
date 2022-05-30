package at.fhv.sysarch.lab4.game;

import at.fhv.sysarch.lab4.rendering.Renderer;

public class PlayerController {
    private Renderer renderer;
    private int currentPlayer = 1;
    private int[] score;

    public PlayerController(Renderer renderer) {
        this.renderer = renderer;
        this.score = new int[]{0,0};
    }

    public void switchPlayers() {
        if (currentPlayer == 1)
            currentPlayer = 2;
        else
            currentPlayer = 1;

        renderer.setActionMessage("Switched players: Player " + currentPlayer + " is now playing!");
    }

    public void increasePlayerScoreByAmount(int amount) {
        if (currentPlayer == 1){
            score[0] += amount;
            renderer.setPlayer1Score(score[0]);
        } else{
            score[1] += amount;
            renderer.setPlayer2Score(score[1]);
        }

        renderer.setStrikeMessage("Player " + currentPlayer + " scored " + amount + " point(s)!");
    }

    public void decreasePlayerScoreByAmount(int amount) {
        if (currentPlayer == 1){
            score[0] -= amount;
            renderer.setPlayer1Score(score[0]);
        } else{
            score[1] -= amount;
            renderer.setPlayer2Score(score[1]);
        }

        renderer.setStrikeMessage("Player " + currentPlayer + " lost " + amount + " point(s)!");
    }
}
