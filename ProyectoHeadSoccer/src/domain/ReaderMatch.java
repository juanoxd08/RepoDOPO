package domain;

public interface ReaderMatch {
    Player getPlayer1();
    Player getPlayer2();
    Field getField();
    Ball getBall();
    ScoreBoard getScoreBoard();
    GameStatus getState();
}
