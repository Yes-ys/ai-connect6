// wlhrh's first version AI, ref e2376524daab2c9bbd698ca83ea39c00c8abe92a
package stud.g02;

import core.board.Board;
import core.board.PieceColor;
import core.game.Game;
import core.game.Move;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AI extends core.player.AI {
    private int steps = 0;
    private int[] nextStep = new int[]{0, 0};
    private int method = 1; // 1 : alpha-beta, 2 : TBS, 3 : Monte Carlo

    // todo: wlmrh
    @Override
    public Move findNextMove(Move opponentMove) {
        this.board.makeMove(opponentMove);
        Move nextMove;
        if (canWin()){
            nextMove = createMoveFromIndices(nextStep[0], nextStep[1]);
            this.board.makeMove(nextMove);
            return nextMove;
        } else if (needDefence()) {
            nextMove = createMoveFromIndices(nextStep[0], nextStep[1]);
            this.board.makeMove(nextMove);
            return nextMove;
        }

        if (Search(2) != 2){
            System.out.println("Find next move failed.\n");
            return createMoveFromIndices(0, 0);
        }

        nextMove = createMoveFromIndices(nextStep[0], nextStep[1]);
        this.board.makeMove(nextMove);
        return nextMove;
    }

    // ���ص�ǰ������Ƿ���ֱ�ӻ�ʤ
    // ������ԣ���ѡ�������λ������д�� nextMove ��
    private boolean canWin() {
        PieceColor myColor = this.getBoard().whoseMove(); // ��ǰ����
        PieceColor[] cells = board.get_board(); // ��ǰ����
        int[][] directions = Board.FORWARD; // ���п��ܵ���������

        for (int i = 0; i < 361; i++) {
            int col = i % 19;
            int row = i / 19;

            for (int[] dir : directions) {
                List<Integer> emptyIndices = new ArrayList<>();
                int sameColor = 0;
                boolean valid = true;

                for (int k = 0; k < 6; k++) {
                    int c = col + dir[0] * k;
                    int r = row + dir[1] * k;
                    if (c < 0 || c >= 19 || r < 0 || r >= 19) {
                        valid = false;
                        break;
                    }
                    int idx = r * 19 + c;
                    PieceColor p = cells[idx];

                    if (p == myColor) {
                        sameColor++;
                    } else if (p == PieceColor.EMPTY) {
                        emptyIndices.add(idx);
                    } else {
                        valid = false;
                        break;
                    }
                }

                if (valid) {
                    if (sameColor >= 4) {
                        nextStep[0] = emptyIndices.get(0);
                        // ����γ�һ�������������������ؼ��㣻����γ�һ���������ǰ�ؼ��������һ��������
                        nextStep[1] = (emptyIndices.size() > 1) ? emptyIndices.get(1) : getAnyEmpty(nextStep[0]);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * �ж��Ƿ���Ҫ���ء�
     * ���ԣ�
     * 1. ɨ��ȫ����в��
     * 2. �������в -> return false��
     * 3. ���������ȫ��ס -> nextMove[0]=���ص�, nextMove[1]=Search(1) -> return true��
     * 4. ������ӷ���ס -> ��������������ӷ������ -> nextMove[0/1]=��ѵ�� -> return true��
     */
    private boolean needDefence() {
        List<List<Integer>> allThreats = getAllThreatLines();

        if (allThreats.isEmpty()) {
            return false;
        }

        // Ѱ���ܸ��������в�ĵ���
        int bestSinglePoint = findBestSinglePoint(allThreats);

        // ���������Ƿ񸲸���������в
        if (coversAllThreats(bestSinglePoint, allThreats)) {
            // һ������ܷ���ס
            nextStep[0] = bestSinglePoint;

            if (Search(1) == 0) {
                nextStep[1] = getAnyEmpty(nextStep[0]);
            }

        } else {
            // ����������ȫ�����ڷ���
            // ���ñ���ö�٣�Ѱ�Ҹ�������ߵķ������
            int[] pair = getBestPairBruteForce(allThreats);
            nextStep[0] = pair[0];
            nextStep[1] = pair[1];
        }
        return true;
    }

    // ��������
    private int[] getBestPairBruteForce(List<List<Integer>> threats) {
        // ��������в����ȡ�� candidates
        Set<Integer> candidates = new HashSet<>();
        for (List<Integer> line : threats) {
            candidates.addAll(line);
        }
        List<Integer> candidateList = new ArrayList<>(candidates);

        if (candidateList.size() < 2) {
            System.out.println("Can't defend with one step, but only have one candidate.\n");
            return new int[]{candidateList.get(0), getAnyEmpty(candidateList.get(0))};
        }

        int[] bestPair = new int[2];
        int maxCovered = -1;

        for (int i = 0; i < candidateList.size(); i++) {
            for (int j = i + 1; j < candidateList.size(); j++) {
                int p1 = candidateList.get(i);
                int p2 = candidateList.get(j);

                int covered = countCovered(threats, p1, p2);

                if (covered > maxCovered) {
                    maxCovered = covered;
                    bestPair[0] = p1;
                    bestPair[1] = p2;

                    if (maxCovered == threats.size()) {
                        return bestPair;
                    }
                }
            }
        }
        return bestPair;
    }

    private List<List<Integer>> getAllThreatLines() {
        List<List<Integer>> lines = new ArrayList<>();
        PieceColor oppColor = this.getBoard().whoseMove().opposite();
        PieceColor[] cells = board.get_board();
        int[][] directions = Board.FORWARD;

        for (int i = 0; i < 361; i++) {
            int col = i % 19;
            int row = i / 19;
            for (int[] dir : directions) {
                List<Integer> emptyIndices = new ArrayList<>();
                int oppCount = 0;
                boolean valid = true;

                for (int k = 0; k < 6; k++) {
                    int c = col + dir[0] * k;
                    int r = row + dir[1] * k;
                    if (c < 0 || c >= 19 || r < 0 || r >= 19) {
                        valid = false; break;
                    }
                    int idx = r * 19 + c;
                    if (cells[idx] == oppColor) oppCount++;
                    else if (cells[idx] == PieceColor.EMPTY) emptyIndices.add(idx);
                    else { valid = false; break; }
                }

                if (valid && oppCount >= 4) {
                    lines.add(emptyIndices);
                }
            }
        }
        return lines;
    }

    private int findBestSinglePoint(List<List<Integer>> threats) {
        int[] scores = new int[361];
        int bestIdx = -1;
        int maxScore = -1;

        for (List<Integer> line : threats) {
            // ����ʱ����Ȩ�ص���Ϊ��������ȴ���
            int weight = (line.size() == 1) ? 100 : 1;
            for (int idx : line) {
                scores[idx] += weight;
                if (scores[idx] > maxScore) {
                    maxScore = scores[idx];
                    bestIdx = idx;
                }
            }
        }
        if (bestIdx == -1 && !threats.isEmpty()) return threats.get(0).get(0);
        return bestIdx;
    }

    private boolean coversAllThreats(int p1, List<List<Integer>> threats) {
        for (List<Integer> line : threats) {
            if (!line.contains(p1)) return false;
        }
        return true;
    }

    private int countCovered(List<List<Integer>> threats, int p1, int p2) {
        int count = 0;
        for (List<Integer> line : threats) {
            if (line.contains(p1) || line.contains(p2)) {
                count++;
            }
        }
        return count;
    }

    // ���ص�ǰ�����ϳ��� avoidIdx �����������һ�� EMTPY λ������
    // ���û���ҵ������� -1
    private int getAnyEmpty(int avoidIdx) {
        PieceColor[] cells = board.get_board();
        // ������ avoidIdx ��Χ�Ŀ�λ
        int[] nearby = {-1, 1, -19, 19, -20, 20, -18, 18};
        for (int offset : nearby) {
            int target = avoidIdx + offset;
            if (target >= 0 && target < 361 && cells[target] == PieceColor.EMPTY) {
                return target;
            }
        }

        for (int i = 0; i < 361; i++) {
            if (i != avoidIdx && cells[i] == PieceColor.EMPTY) return i;
        }
        return -1;
    }

    // ������ǰ������ѡ���λ�����������ض�Ӧ�� Move ����
    private Move createMoveFromIndices(int idx1, int idx2) {
        char c1 = indexToCol(idx1);
        char r1 = indexToRow(idx1);
        char c2 = indexToCol(idx2);
        char r2 = indexToRow(idx2);
        return new Move(c1, r1, c2, r2);
    }

    private char indexToCol(int index) {
        int val = index % 19;
        return (char) ('A' + val + (val >= 8 ? 1 : 0));
    }

    private char indexToRow(int index) {
        int val = index / 19;
        return (char) ('A' + val + (val >= 8 ? 1 : 0));
    }

    // todo: hethtina
    // type ��ʾ Search ��Ҫ���ɵĲ���(1 / 2)
    // ��ѡ��λ�õ�����д�� nextMove ������
    // type = 1ʱ������д�� nextMove[1] ��
    // ���سɹ����ɵĲ���
    private int Search(int type){
        return 1;
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
