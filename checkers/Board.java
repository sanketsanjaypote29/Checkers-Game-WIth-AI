package checkers;
import java.util.ArrayList;
import java.util.Arrays;
class Board {
    private char[][] board;
    private PlayerStats currentTurn;
    private ArrayList<BoardAndScore> successorEvaluations;
    private PlayerStats humanPlayer;
    private PlayerStats aiPlayer;
    static final int TILESIZE = 100;
    static final int COUNTERSIZE = TILESIZE /4;
    private int difficulty;


    Board(int difficulty, PlayerStats humanPlayer) {

        initialiseBoard();
        this.difficulty = difficulty;
        this.humanPlayer = humanPlayer;
        this.aiPlayer = humanPlayer == PlayerStats.b ? PlayerStats.w : PlayerStats.b;
        this.currentTurn = PlayerStats.b;
    }
    private void initialiseBoard() {
        //Initial State
        board = new char[][]{{'X', 'w', 'X', 'w', 'X', 'w', 'X', 'w'},
                             {'w', 'X', 'w', 'X', 'w', 'X', 'w', 'X'},
                             {'X', 'w', 'X', 'w', 'X', 'w', 'X', 'w'},
                             {'O', 'X', 'O', 'X', 'O', 'X', 'O', 'X'},
                             {'X', 'O', 'X', 'O', 'X', 'O', 'X', 'O'},
                             {'b', 'X', 'b', 'X', 'b', 'X', 'b', 'X'},
                             {'X', 'b', 'X', 'b', 'X', 'b', 'X', 'b'},
                             {'b', 'X', 'b', 'X', 'b', 'X', 'b', 'X'}};

    }

    char[][] getAIMove() {
        successorEvaluations = new ArrayList<>();
        minimaxAB(board, 0, aiPlayer, Integer.MIN_VALUE, Integer.MAX_VALUE);
        commandLineAnalyseStates();
        return returnBestMove();
    }

    char[][] getAIMoveMustAttack(boolean specificPiece, boolean isKing, int x, int y) {
        successorEvaluations = new ArrayList<>();
        minimaxAB(board, 0, aiPlayer, Integer.MIN_VALUE, Integer.MAX_VALUE);
        ArrayList<char[][]> validMoves = new ArrayList<>();
        if (specificPiece) {
            validMoves.addAll(getAllAttackableMoves(board,aiPlayer,isKing,x,y));
        }
        else {
            for (int y_ = 0; y_ < 8; y_++) {
                for (int x_ = 0; x_ < 8; x_++) {
                    boolean king = board[y_][x_] == aiPlayer.getKingChar();
                    validMoves.addAll(getAllAttackableMoves(board,aiPlayer,king,x_,y_));
                }
            }
        }
        ArrayList<BoardAndScore> successorEvaluationsClone = new ArrayList<>(successorEvaluations);
        for(BoardAndScore bs : successorEvaluationsClone) {
            if (!contains(validMoves,bs.getState())) {
                successorEvaluations.remove(bs);
            }
        }
        commandLineAnalyseStates();
        return returnBestMove();
    }



    private void commandLineAnalyseStates() {
        System.out.println("My Turn!\nHmmm... Let's have a look here then..\n");
        for (BoardAndScore bs : successorEvaluations) {
            System.out.println("Well, I go can for:\n" + Arrays.deepToString(bs.getState()).replaceAll("],", "]\n") + "\nand I'll score: " + bs.getScore());
            System.out.println("Invulnerable Pieces: " + countCheckersInSafeTile(bs.getState(), getAiPlayer()));
            System.out.println("Vulnerable Pieces: " + countTotalVulnerableCheckers(bs.getState(), getAiPlayer()));
            System.out.println("Opponent Vulnerable Pieces: " + countTotalVulnerableCheckers(bs.getState(), getAiPlayer().getOpponent()));
            System.out.println("Kings On Board: " + countNumberOfKings(bs.getState(), getAiPlayer()));
            System.out.println("Enemy Kings: " + countNumberOfKings(bs.getState(), getAiPlayer().getOpponent()));
            System.out.println("Killed Enemies: " + Math.abs(countNumberOfPawns(bs.getState(), getAiPlayer().getOpponent()) +
                    countNumberOfKings(bs.getState(), getAiPlayer().getOpponent()) - 12));
            System.out.println("\n");
        }
    }
    private char[][] returnBestMove() {
        int max;
        int best = 0;
        if (aiPlayer == PlayerStats.b) {
            max = Integer.MIN_VALUE;
            for (int i = 0; i < successorEvaluations.size(); ++i) {
                if (max < successorEvaluations.get(i).getScore()) {
                    max = successorEvaluations.get(i).getScore();
                    best = i;
                }
            }
        }
        else {
            max = Integer.MAX_VALUE;
            for (int i = 0; i < successorEvaluations.size(); ++i) {
                if (max > successorEvaluations.get(i).getScore()) {
                    max = successorEvaluations.get(i).getScore();
                    best = i;
                }
            }
        }
        return successorEvaluations.isEmpty() ? null : successorEvaluations.get(best).getState();
    }

    private int evaluateNode(char[][] node) {
        int score = 0;
        score = score + countTotalVulnerableCheckers(node, PlayerStats.b.getOpponent())*3;
        score = score - countTotalVulnerableCheckers(node, PlayerStats.w.getOpponent())*3;
        score = score + countNumberOfKings(node, PlayerStats.b)*5;
        score = score - countNumberOfKings(node, PlayerStats.w)*5;
        score = score + Math.abs(countNumberOfPawns(node, PlayerStats.b.getOpponent()) +
                countNumberOfKings(node, PlayerStats.b.getOpponent()) - 12) * 3;
        score = score - Math.abs(countNumberOfPawns(node, PlayerStats.w.getOpponent()) +
                countNumberOfKings(node, PlayerStats.w.getOpponent()) - 12) * 3;

        score = score - countTotalVulnerableCheckers(node, PlayerStats.b);
        score = score + countTotalVulnerableCheckers(node, PlayerStats.w);

        return score;
    }

    private int minimaxAB(char[][] node, int depth, PlayerStats player, int a, int b) {

        if (hasWhiteWon(node)) return  -1000;
        if (hasBlackWon(node)) return 1000;
        if (depth == difficulty) return evaluateNode(node);
        if (player == PlayerStats.b) {
            int bestScore = Integer.MIN_VALUE;
            for (char[][] child: successorFunction(player, node, false)){
                int currentScore = minimaxAB(child, depth+1, PlayerStats.w,a,b);
                bestScore = Math.max(bestScore, currentScore);
                a = Integer.max(a, currentScore);
                if (a >= b)  break;
                if (depth == 0 ) successorEvaluations.add(new BoardAndScore(child,currentScore));
            }
            return bestScore;
        }
        else {
            int bestScore = Integer.MAX_VALUE;
            for (char[][] child: successorFunction(player, node, false)){
                int currentScore = minimaxAB(child, depth +1, PlayerStats.b,a,b);
                bestScore = Integer.min(bestScore, currentScore);
                b = Integer.min(b, currentScore);
                if (a >= b) break;
                if (depth == 0) successorEvaluations.add(new BoardAndScore(child,currentScore));
            }
            return bestScore;
        }
    }

    ArrayList<char[][]> successorFunction(PlayerStats currentPlayer, char[][] currentState, boolean attackOnly) {

        ArrayList<char[][]> possibleStates = new ArrayList<>();
        if (!attackOnly) {
            for (int y=0; y < 8; y++) {
                for (int x=0; x < 8; x++) {
                    if (currentState[y][x] == currentPlayer.getKingChar() || currentState[y][x] == currentPlayer.getPawnChar()) {
                        boolean isKing = (currentState[y][x] == 'B' || currentState[y][x] == 'W');
                        char[][] temp;
                        if (canMoveForwardRight(currentState, currentPlayer, x, y)) {
                            temp = deepClone(currentState);
                            temp[y][x] = 'O';
                         
                            temp[y + currentPlayer.getForwardDirection()][x + 1] = (y + currentPlayer.getForwardDirection() == currentPlayer.getOpponent().getKingsRow())
                                    ? currentPlayer.getKingChar() : currentState[y][x];
                            possibleStates.add(temp);
                        }
                        if (canMoveBackwardRight(currentState, currentPlayer, isKing, x, y)) {
                            temp = deepClone(currentState);
                            temp[y][x] = 'O';

                            temp[y - currentPlayer.getForwardDirection()][x + 1] = (y - currentPlayer.getForwardDirection() == currentPlayer.getOpponent().getKingsRow())
                                    ? currentPlayer.getKingChar() : currentState[y][x];
                            possibleStates.add(temp);
                        }
                        if (canMoveForwardLeft(currentState, currentPlayer, x, y)) {
                            temp = deepClone(currentState);
                            temp[y][x] = 'O';
                            temp[y + currentPlayer.getForwardDirection()][x - 1] = (y + currentPlayer.getForwardDirection() == currentPlayer.getOpponent().getKingsRow())
                                    ? currentPlayer.getKingChar() : currentState[y][x];

                            possibleStates.add(temp);
                        }
                        if (canMoveBackwardLeft(currentState, currentPlayer, isKing, x, y)) {
                            temp = deepClone(currentState);
                            temp[y][x] = 'O';
                            temp[y - currentPlayer.getForwardDirection()][x - 1] = (y - currentPlayer.getForwardDirection() == currentPlayer.getOpponent().getKingsRow())
                                    ? currentPlayer.getKingChar() : currentState[y][x];

                            possibleStates.add(temp);
                        }

                        if (attackableMovePresent(currentState, currentPlayer, isKing, x, y)) {

                            possibleStates.addAll(getAllAttackableMoves(currentState, currentPlayer, isKing, x, y));
                        }
                    }


                }
            }
        }
        else {
            for (int y=0; y < 8; y++) {
                for (int x=0; x < 8; x++) {
                    boolean isKing = currentState[y][x] == currentPlayer.getKingChar();
                    if (currentState[y][x] == currentPlayer.getKingChar() || currentState[y][x] == currentPlayer.getPawnChar()) {
                        possibleStates.addAll(getAllAttackableMoves(currentState, currentPlayer, isKing, x, y));
                    }

                }
            }
        }
        return possibleStates;
    }

    boolean attackableMovePresent(char[][] state, PlayerStats currentPlayer, boolean isKing, int x, int y) {
        return canAttackForwardLeft(state,currentPlayer,x,y) ||canAttackForwardRight(state,currentPlayer,x,y) ||
                canAttackBackwardLeft(state,currentPlayer,isKing,x,y)|| canAttackBackwardRight(state,currentPlayer,isKing,x,y);
    }

    boolean attackableMovePresent(char[][] state, PlayerStats currentPlayer) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                boolean isKing = false;
                if (board[y][x] == currentPlayer.getKingChar()) isKing = true;
                if (board[y][x] == currentPlayer.getKingChar() || board[y][x] == currentPlayer.getPawnChar())
                if (canAttackForwardLeft(state,currentPlayer,x,y) ||canAttackForwardRight(state,currentPlayer,x,y) ||
                        canAttackBackwardLeft(state,currentPlayer,isKing,x,y)|| canAttackBackwardRight(state,currentPlayer,isKing,x,y)) return true;
            }
        }
        return false;
    }

    private boolean canMoveForwardLeft(char[][] state, PlayerStats currentPlayer, int x, int y) {
        if (y + currentPlayer.getForwardDirection() <= 7 && y + currentPlayer.getForwardDirection() >= 0 &&
                x - 1 >= 0)
        return state[y + currentPlayer.getForwardDirection()][x - 1] == 'O';
        return false;
    }
   
    private boolean canMoveForwardRight(char[][] state, PlayerStats currentPlayer, int x, int y){
        try {
        if (y + currentPlayer.getForwardDirection() <= 7 && y + currentPlayer.getForwardDirection() >= 0 &&
                x +1 <=7)
        return state[y + currentPlayer.getForwardDirection()][x + 1] == 'O';

        } catch (IndexOutOfBoundsException ignored) {}
        return false;
    }

    private boolean canMoveBackwardLeft(char[][] state, PlayerStats currentPlayer,boolean isKing, int x, int y){
        try {
            if ((y - currentPlayer.getForwardDirection() <= 7 && y - currentPlayer.getForwardDirection() >= 0 &&
                    x -1 >= 0) && isKing)
            return state[y - currentPlayer.getForwardDirection()][x - 1] == 'O';
        } catch (IndexOutOfBoundsException ignored) {}
        return false;
    }

  
    private boolean canMoveBackwardRight(char[][] state, PlayerStats currentPlayer,boolean isKing, int x, int y){
        try {
            if ((y - currentPlayer.getForwardDirection() <= 7 && y - currentPlayer.getForwardDirection() >= 0 &&
                    x +1 <=7) && isKing)
            return state[y - currentPlayer.getForwardDirection()][x + 1] == 'O';
        } catch (IndexOutOfBoundsException ignored) {}
        return false;
    }

  
    private boolean canAttackForwardLeft(char[][] state, PlayerStats currentPlayer, int x, int y){
        try {
            if (y + currentPlayer.getForwardDirection() * 2 <= 7 && y + currentPlayer.getForwardDirection() * 2 >= 0 &&
                    x - 2 >= 0)
            return ((state[y + currentPlayer.getForwardDirection()][x - 1] == currentPlayer.getOpponent().getPawnChar() ||
                    state[y + currentPlayer.getForwardDirection()][x - 1] == currentPlayer.getOpponent().getKingChar()) &&
                    state[y + currentPlayer.getForwardDirection() * 2][x - 2] == 'O');
        } catch (IndexOutOfBoundsException ignored) {}
        return false;
    }

    private boolean canAttackForwardRight(char[][] state, PlayerStats currentPlayer, int x, int y) {
        try {
            if (y + currentPlayer.getForwardDirection() * 2 <= 7 && y + currentPlayer.getForwardDirection() * 2 >= 0 &&
                    x + 2 <= 7)
            return ((state[y + currentPlayer.getForwardDirection()][x + 1] == currentPlayer.getOpponent().getPawnChar() ||
                    state[y + currentPlayer.getForwardDirection()][x + 1] == currentPlayer.getOpponent().getKingChar()) &&
                    state[y + currentPlayer.getForwardDirection() * 2][x + 2] == 'O');
        } catch (IndexOutOfBoundsException ignored) {}
        return false;
    }

    private boolean canAttackBackwardLeft(char[][] state, PlayerStats currentPlayer, boolean isKing, int x, int y) {
        try {
            if ((y - currentPlayer.getForwardDirection() * 2 <= 7 && y - currentPlayer.getForwardDirection() * 2 >= 0 &&
                    x - 2 >= 0) && isKing)
            return ((state[y - currentPlayer.getForwardDirection()][x - 1] == currentPlayer.getOpponent().getPawnChar() ||
                    state[y - currentPlayer.getForwardDirection()][x - 1] == currentPlayer.getOpponent().getKingChar()) &&
                    state[y - currentPlayer.getForwardDirection() * 2][x - 2] == 'O');
        } catch (IndexOutOfBoundsException ignored) {}
        return false;
    }

    private boolean canAttackBackwardRight(char[][] state, PlayerStats currentPlayer, boolean isKing, int x, int y) {
        try {
            if ((y - currentPlayer.getForwardDirection() * 2 <= 7 && y - currentPlayer.getForwardDirection() * 2 >= 0 &&
                    x + 2 <= 7) && isKing)
            return ((state[y - currentPlayer.getForwardDirection()][x + 1] == currentPlayer.getOpponent().getPawnChar() ||
                    state[y - currentPlayer.getForwardDirection()][x + 1] == currentPlayer.getOpponent().getKingChar()) &&
                    state[y - currentPlayer.getForwardDirection() * 2][x + 2] == 'O');
        } catch (IndexOutOfBoundsException ignored) {}
        return false;
    }

    ArrayList<char[][]> getAllAttackableMoves(char[][] node, PlayerStats currentPlayer, boolean isKing, int x, int y) {
        ArrayList<char[][]> attackMoves = new ArrayList<>();
        if (!attackableMovePresent(node,currentPlayer,isKing,x,y)){
            return new ArrayList<>();
        }
        else {
            char[][] temp;
            try {
                if (canAttackForwardRight(node,currentPlayer,x,y)) {
                    temp = deepClone(node);
                    temp[y][x] = 'O';
                    temp[y + currentPlayer.getForwardDirection()][x + 1] = 'O';
                    temp[y + currentPlayer.getForwardDirection() * 2][x + 2] = (y + currentPlayer.getForwardDirection() * 2 == currentPlayer.getOpponent().getKingsRow())
                            ? currentPlayer.getKingChar() : node[y][x];
                    attackMoves.add(temp);
                }
            } catch (IndexOutOfBoundsException ignored) {}
            try {
                if (isKing) {
                    if ( canAttackBackwardRight(node,currentPlayer,true,x,y)) {
                        temp = deepClone(node);
                        temp[y][x] = 'O';
                        temp[y - currentPlayer.getForwardDirection()][x + 1] = 'O';
                        temp[y - currentPlayer.getForwardDirection() * 2][x + 2] = (y - currentPlayer.getForwardDirection() * 2 == currentPlayer.getOpponent().getKingsRow())
                                ? currentPlayer.getKingChar() : node[y][x];
                        attackMoves.add(temp);
                    }
                }
            } catch (IndexOutOfBoundsException ignored) {}
            try {
                if (canAttackForwardLeft(node,currentPlayer,x,y)) {
                    temp = deepClone(node);
                    temp[y][x] = 'O';
                    temp[y + currentPlayer.getForwardDirection()][x - 1] = 'O';
                    temp[y + currentPlayer.getForwardDirection() * 2][x - 2] = (y + currentPlayer.getForwardDirection() * 2 == currentPlayer.getOpponent().getKingsRow())
                            ? currentPlayer.getKingChar() : node[y][x];
                    attackMoves.add(temp);
                }
            }catch (IndexOutOfBoundsException ignored) {}
            try {
                if (isKing) {
                    if ( canAttackBackwardLeft(node,currentPlayer,true,x,y)) {
                        temp = deepClone(node);
                        temp[y][x] = 'O';
                        temp[y - currentPlayer.getForwardDirection()][x - 1] = 'O';
                        temp[y - currentPlayer.getForwardDirection() * 2][x - 2] = (y - currentPlayer.getForwardDirection() * 2 == currentPlayer.getOpponent().getKingsRow())
                                ? currentPlayer.getKingChar() : node[y][x];
                        attackMoves.add(temp);
                    }
                }
            } catch (IndexOutOfBoundsException ignored) {}
        }
        return attackMoves;
    }

    boolean hasBlackWon(char[][] node) {
        int whiteOnBoard = 0;
        for (int x=0; x<8; x++){
            for (int y=0; y<8;y++) {
                if (node[y][x] == PlayerStats.w.getPawnChar() || node[y][x] == PlayerStats.w.getKingChar()) {
                    whiteOnBoard ++;
                }
            }
        }
        return (whiteOnBoard ==0 || successorFunction(PlayerStats.w,node,false).isEmpty());
    }
  
    boolean hasWhiteWon(char[][] node) {
        int blackOnBoard = 0;
        for (int x=0; x<8; x++){
            for (int y=0; y<8;y++) {
                if (node[y][x] == PlayerStats.b.getPawnChar() || node[y][x] == PlayerStats.b.getKingChar()) {
                    blackOnBoard ++;
                }
            }
        }
        return (blackOnBoard ==0 || successorFunction(PlayerStats.b,node,false).isEmpty());
    }

    private boolean isCheckerVulnerable(char[][] node, PlayerStats player, int x, int y) {
        if (node[y][x] != player.getKingChar() || node[y][x] != player.getPawnChar()) return false;
        try {
            if (node[y - player.getForwardDirection()][x-1] == 'O' &&
                    (node[y+player.getForwardDirection()][x+1] == player.getOpponent().getKingChar() ||
                            node[y+player.getForwardDirection()][x+1] == player.getOpponent().getPawnChar())) {
                return true;
            }
        } catch (Exception ignored) {}

        try {
            if (node[y - player.getForwardDirection()][x+1] == 'O' &&
                    (node[y+player.getForwardDirection()][x-1] == player.getOpponent().getKingChar() ||
                            node[y+player.getForwardDirection()][x-1] == player.getOpponent().getPawnChar())) {
                return true;
            }
        } catch (Exception ignored) {}

        try {
            if (node[y + player.getForwardDirection()][x-1] == 'O' &&
                    node[y - player.getForwardDirection()][x+1] == player.getOpponent().getKingChar()) {
                return true;
            }
        } catch (Exception ignored) {}

        try {
            if (node[y + player.getForwardDirection()][x + 1] == 'O' &&
                    node[y - player.getForwardDirection()][x - 1] == player.getOpponent().getKingChar()) {
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private int countTotalVulnerableCheckers(char[][] node, PlayerStats player) {
        int count = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (isCheckerVulnerable(node,player,x,y)) count++;
            }
        }
        return count;
    }

    private boolean canThisPositionBeAttacked(int x, int y) {
        return (x == 7 || x == 0 || y == 0 || y == 7);
    }

   
    private int countCheckersInSafeTile(char[][] node, PlayerStats player) {
        int count = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (node[y][x] == player.getKingChar() || node[y][x] == player.getPawnChar()){
                    if(canThisPositionBeAttacked(x,y)) count++;
                }
            }
        }
        return count;
    }

    private int countNumberOfKings(char[][] node, PlayerStats player) {
        int count = 0;
        for (int y = 0; y < 8 ; y++) {
            for (int x = 0; x < 8; x++) {
                if (node[y][x] == player.getKingChar()) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countNumberOfPawns(char[][] node, PlayerStats player) {
        int count = 0;
        for (int y = 0; y < 8 ; y++) {
            for (int x = 0; x < 8; x++) {
                if (node[y][x] == player.getPawnChar()) {
                    count++;
                }
            }
        }
        return count;
    }

    void updateBoardForNewKings() {
        for (int x = 0; x < 8; x++) {
            if (board[PlayerStats.w.getKingsRow()][x] == 'b') board[PlayerStats.w.getKingsRow()][x] = 'B';
            if (board[PlayerStats.b.getKingsRow()][x] == 'w') board[PlayerStats.w.getKingsRow()][x] = 'W';
        }
    }

    char[][] returnStateOfMove(int originX, int originY, int destinationX, int destinationY) {
        char[][] newState = deepClone(board);
        char temp = newState[originY][originX];
        newState[originY][originX] = 'O';
        newState[destinationY][destinationX] =
                destinationY == PlayerStats.valueOf(Character.toString(Character.toLowerCase(temp))).getOpponent().getKingsRow()
                        ? Character.toUpperCase(temp) : temp;
        if (Math.abs(originX-destinationX) == 2 && Math.abs(originY-destinationY)==2) {
            if (destinationX - originX > 0 && destinationY - originY > 0) {
                newState[originY+1][originX+1] = 'O';
            }
            else if (destinationX - originX < 0 && destinationY - originY > 0) {
                newState[originY+1][originX-1] = 'O';
            }
            else if (destinationX - originX < 0 && destinationY - originY < 0) {
                newState[originY-1][originX-1] = 'O';
            }
            else if (destinationX - originX > 0 && destinationY - originY < 0) {
                newState[originY-1][originX+1] = 'O';
            }
        }
        return newState;
    }

    boolean gameOver() {
        return hasBlackWon(board) || hasWhiteWon(board);
    }

    ArrayList<int[]> interpretState(ArrayList<char[][]> possibleStates, PlayerStats currentPlayer) {
        ArrayList<int[]> landingStates = new ArrayList<>();
        for (char[][] state : possibleStates) {
            landingStates.add(findCoordsOfResultOfMove(board,state, currentPlayer));
        }
        return landingStates;
    }

    int[] findCoordsOfResultOfMove(char[][] start, char[][] finish, PlayerStats currentPlayer) {
        int[] landingCoordinates = new int[2];

        for (int y = 0; y < 8; y++) {
            for (int x=0; x<8; x++) {
                if (start[y][x] != finish[y][x] && (finish[y][x] == currentPlayer.getPawnChar() || finish[y][x] == currentPlayer.getKingChar())) {
                    landingCoordinates[0] = x;
                    landingCoordinates[1] = y;
                    return landingCoordinates;
                }
            }
        }
        return new int[2];
    }

    static boolean contains(ArrayList<char[][]> a, char[][] b) {
        boolean contains = false;
        for (char[][] states: a) {
            boolean match = true;
            for (int i = 0; i < a.get(0).length; i++) {
                match = Arrays.equals(states[i], b[i]);
                if (!match) break;
            }
            contains = match;
            if (contains) break;
        }
        return contains;
    }

    
    boolean playerMoveValid(char[][] state, int originX, int originY) {

        return state[originY][originX] == humanPlayer.getPawnChar() || state[originY][originX] == humanPlayer.getKingChar();
    }

   
    int countPlayerTokens(char[][] state, PlayerStats player, boolean justPawns, boolean justKings) {
        int count = 0;
        if (justKings && justPawns || (!justKings && !justPawns)) {
            for (int y = 0; y < 8; y ++) {
                for (int x = 0; x < 8; x++) {
                    if (state[y][x] == player.getKingChar() || state[y][x] == player.getPawnChar()) {
                        count++;
                    }
                }
            }
        }
        else if (justPawns) {
            for (int y = 0; y < 8; y ++) {
                for (int x = 0; x < 8; x++) {
                    if (state[y][x] == player.getPawnChar()) {
                        count++;
                    }
                }
            }
        }
        else {
            for (int y = 0; y < 8; y ++) {
                for (int x = 0; x < 8; x++) {
                    if (state[y][x] == player.getKingChar()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    char[][] getBoard() {
        return board;
    }

    PlayerStats getCurrentTurn() {
        return currentTurn;
    }

    ArrayList<BoardAndScore> getSuccessorEvaluations() {
        return successorEvaluations;
    }

    void changeTurn() {
        currentTurn = (currentTurn.equals(humanPlayer)) ? aiPlayer : humanPlayer;
    }

    void updateCurrentState(char[][] state) {
        board = state;
    }


    PlayerStats getAiPlayer() {
        return aiPlayer;
    }

    private char[][] deepClone(char[][] state) {
        char[][] toClone = new char[state.length][];
        for (int i=0; i <state.length; i++) {
            toClone[i] = Arrays.copyOf(state[i], state[i].length);
        }
        return toClone;
    }
}
