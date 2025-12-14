package stud.g02;

import core.board.Board;
import core.board.PieceColor;
import core.game.Game;
import core.game.Move;

import java.util.Random;

public class AI extends core.player.AI {
    private int steps = 0;
    private Move nextMove;
    private int type = 1; // 1 : alpha-beta, 2 : TBS, 3 : Monte Carlo

    // todo: wlmrh
    @Override
    public Move findNextMove(Move opponentMove) {
        this.board.makeMove(opponentMove);
        if (canWin()){
            this.board.makeMove(nextMove);
            return nextMove;
        } else if (needDefence()) {
            this.board.makeMove(nextMove);
            return nextMove;
        }
        Search();
        this.board.makeMove(nextMove);
        return nextMove;
    }

    // todo: wlmrh
    private boolean canWin(){
        return  true;
    }

    // todo: wlmrh
    private boolean needDefence(){
        return true;
    }

    // todo: hethtina
    // 根据棋盘返回走的两步棋，将结果写到 nextMove
    private void Search(){

    }

    public String name() {
        return "G02";
    }

    @Override
    public void playGame(Game game) {
        super.playGame(game);
        board = new Board();
        steps = 0;
    }
}
