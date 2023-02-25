package Chess;

import java.io.*;
import java.util.*;

public class Main {
    public static final int CHECKMATE = 1000000000;
    public static final int TIMEOUT = 2100000001;
    public static HashMap<Long,Move> bestMoves = new HashMap<>();
    public static int numPositions = 0;
    public static HashMap<Long,Integer> upperTable = new HashMap<>();
    public static HashMap<Long,Integer> lowerTable = new HashMap<>();
    public static HashMap<Long,Integer> exactTable = new HashMap<>();
    public static int numCapturePositions = 0;
    public static int total;
    public static int predicted;
    public static long startTime;
    public static final int maxtime = 5000;
    public static int maxDepth = 0;
    public static int numHits = 0;
    public static HashMap<Object,Integer> moveTypes = new HashMap<>();
    public static HashMap<Integer,Move[]> killerMap = new HashMap<>();
    public static int[][][] history = new int[2][64][64];
    public static int[][][] historyFreq = new int[2][64][64];
    public static Move[][] counterMove = new Move[6][64];
    public static AIPlayer player = new AIPlayer(true);
    public static HashMap<Integer,Integer> bestIdxMap = new HashMap<>();
    public static int tempCounter = 0;
    public static void main(String[] args) throws IOException {
        //FastScanner input = new FastScanner(true);
        //searchFile = new PrintWriter("Search.txt");
        new PreCompute();
        moveTypes.put(Move.class,0);
        moveTypes.put(CaptureMove.class,0);
        moveTypes.put(CastleMove.class,0);
        moveTypes.put(EnPassantMove.class,0);
        moveTypes.put(PromotionCaptureMove.class,0);
        moveTypes.put(PromotionMove.class,0);
        byte x = 0;
        long t = System.currentTimeMillis();
        if (x==0) {
            Scanner input = new Scanner(System.in);
            System.out.print("FEN: ");
            String fen = input.nextLine();
            Board board;
            if (fen.equals("")) board=new Board();
            else board = new Board(fen);
            for (int i = 0; i<1000; i++) {
                killerMap.put(i,new Move[3]);
                Arrays.fill(killerMap.get(i),new Move());
            }
            for (int i = 0; i<100; i++) {
                bestIdxMap.put(i,0);
            }
            for (int i = 0; i<6; i++) {
                for (int j = 0; j<64; j++) {
                    counterMove[i][j]=new Move();
                }
            }
            System.out.print("W or B: ");
            boolean whiteToPlay = input.nextLine().equals("W");
            if (!whiteToPlay) {
                while (true) {
                    try {
                        System.out.print("Opponent Move: ");
                        Move oppMove = PreCompute.tokenToMove(input.nextLine(), board);
                        System.out.println("Opp Move: " + oppMove);
                        oppMove.makeMove(board);
                        board.moves.add(oppMove);
                        board.previousMoves.add(board.getKey());
                        System.out.println();
                        clear();
                        break;
                    } catch (Exception e) {
                        System.out.println("INVALID MOVE");
                    }
                }
            }
            while (true) {
                if (board.numMoves>10 || !bookMove(board)) {
                    iterativeDeepening(board);
                }
                while (true) {
                    try {
                        System.out.print("Opponent Move: ");
                        String token;
                        while (true) {
                            token = input.nextLine();
                            if (token.length()>=4 && token.equals("undo")) {
                                board.previousMoves.remove(board.getKey());
                                board.moves.get(board.moves.size() - 1).undoMove(board);
                                board.moves.remove(board.moves.size() - 1);
                                board.previousMoves.remove(board.getKey());
                                board.moves.get(board.moves.size() - 1).undoMove(board);
                                board.moves.remove(board.moves.size() - 1);
                            } else if (token.length()>=4 && token.substring(0, 4).equals("redo")) {
                                board.previousMoves.remove(board.getKey());
                                board.moves.get(board.moves.size() - 1).undoMove(board);
                                board.moves.remove(board.moves.size() - 1);
                                Move m = tokenToMove(token.substring(5, token.length()), board);
                                m.makeMove(board);
                                board.moves.add(m);
                                board.previousMoves.add(board.getKey());
                            } else if (token.length()>=5 && token.substring(0,5).equals("print")) {
                                System.out.println(board);
                            } else if (token.length()>=5 && token.substring(0,5).equals("flush")) {
                                lowerTable=new HashMap<>();
                                upperTable=new HashMap<>();
                                exactTable=new HashMap<>();
                                System.out.println("Hash tables cleared");
                            } else if (token.length()>=5 && token.substring(0,5).equals("score")) {
                                System.out.println("TOTAL SCORE: "+-board.score(true));
                            } else break;
                            System.out.print("Opponent Move: ");
                        }
                        Move oppMove = PreCompute.tokenToMove(token, board);
                        clear();
                        System.out.println("Opp Move: " + oppMove);
                        oppMove.makeMove(board);
                        board.moves.add(oppMove);
                        board.previousMoves.add(board.getKey());
                        System.out.println();
                        break;
                    } catch (Exception e) {
                        System.out.println("INVALID MOVE");
                    }
                }
            }
        } else if (x==1) {
            FastScanner input = new FastScanner(true);
            Board board;
            for (int i = 0; i<23; i++) {
                String line = input.nextLine();
                String[] tokens = line.split(",");
                tokens[0]=tokens[0].substring(2,tokens[0].length()-1);
                tokens[1]=tokens[1].substring(2,tokens[1].length()-1);
                tokens[2]=tokens[2].substring(1,tokens[2].length()-1);
                //System.out.println(Arrays.toString(tokens));
                board = new Board(tokens[0]);
                System.out.println((i+1)+" "+tokens[2]+"    "+board.SEE(f(tokens[1].substring(0,2)),f(tokens[1].substring(2,4))));
            }
        } else if (x==2) {
            for (int i = 0; i<100; i++) {
                killerMap.put(i,new Move[3]);
                Arrays.fill(killerMap.get(i),new Move());
            }
            for (int i = 0; i<100; i++) {
                bestIdxMap.put(i,0);
            }
            for (int i = 0; i<6; i++) {
                for (int j = 0; j<64; j++) {
                    counterMove[i][j]=new Move();
                }
            }

            Board board = new Board("r1bqk1nr/pppp1ppp/2n5/4p3/1bB1P3/5N2/PPPP1PPP/RNBQ1RK1 b kq - 5 4");
            //board = new Board("r6r/pp2knpp/8/2p5/1n6/5N1P/PPP2PP1/R2Q2KR w - - 0 1");
            iterativeDeepening(board,8);
            System.out.println();

        }
        System.out.println(System.currentTimeMillis()-t+" ms");
    }
    public static boolean bookMove(Board board) {
        HashMap<Move,Integer> map = PreCompute.book.get(board.getKey());
        System.out.println(map);
        if (map==null) return false;
        int total = map.get(new Move());
        map.remove(new Move());
        int random = (int)(Math.random()*total);
        Move res = new Move();
        for (Move m : map.keySet()) {
            random-=map.get(m);
            if (random<0) {
                res=m;
                break;
            }
        }
        map.put(new Move(),total);
        System.out.println(PreCompute.moveToToken(res,board));
        System.out.println("BOOK MOVE");
        res.makeMove(board);
        return true;

    }
    public static void iterativeDeepening(Board board,int maxDepth) {
        if (maxDepth==0) {
            System.out.println(board);
            System.out.println(searchCaptures(board,-2*CHECKMATE,2*CHECKMATE,0,false));
            return;
        }
        int score = 0;
        int iter=0;
        startTime=System.currentTimeMillis();
        int positionsSearched = 0;
        int capturesSearched = 0;
        double accuracy = 0;
        Move best = new Move();
        search(0,board,-2*CHECKMATE,2*CHECKMATE,true,new Move(),false);
        while (iter<=maxDepth-1 && score<CHECKMATE) {
            clear();
            int temp = search(++iter,board,-2*CHECKMATE,2*CHECKMATE,true,new Move(),false);
            //searchFile.flush();
            if (temp==TIMEOUT) break;
            score=temp;
            positionsSearched=numPositions;
            capturesSearched=numCapturePositions;
            accuracy = (double)predicted/total;
            //best=bestMoves.get(board.getKey());
            best=bestMoves.get(board.getKey()^PreCompute.depthHash[iter]);
            System.out.println(iter+" "+score+" "+PreCompute.moveToToken(best,board)+" "+(positionsSearched+capturesSearched));
            //searchFile.println("NEW ITERATION");
        }
        System.out.println("ITERATIONS: "+(iter-1));
        System.out.println("SCORE: "+score);
        System.out.println("MOVE: "+ PreCompute.moveToToken(best,board));
        System.out.println("SEARCHED: "+positionsSearched);
        System.out.println("CAPTURES SEARCHED: "+capturesSearched);
        System.out.println("MOVES HASHED: "+bestMoves.size());
        System.out.println("ACC: "+accuracy);
        System.out.println("ELAPSED: "+(System.currentTimeMillis()-startTime)+" ms");
        //System.out.println(tableElapsed+" ms spent on captures");
        System.out.println("Max Capture Depth: "+Main.maxDepth);
        System.out.println("Table sizes: "+lowerTable.size()+" "+exactTable.size()+" "+upperTable.size());
        System.out.println("AV Best Idx: "+(double)numHits/total);
        System.out.println(tempCounter);
        System.out.println(moveTypes);
        System.out.println();
        System.out.println(bestIdxMap);

        best.makeMove(board);
        board.previousMoves.add(board.getKey());
        board.moves.add(best);
		/*
		for (int i = 0; i<6; i++) {
			for (byte j = 0; j<64; j++) {
				if (!counterMove[i][j].equals(new Move())) System.out.println(new String[]{"K","","N","B","R","Q"}[i]+h(j)+" "+counterMove[i][j]);
			}
		}

		for (int i = 0; i<30; i++) {
			System.out.println(Arrays.toString(killerMap.get(i)));
		}
		for (byte i = 0; i<64; i++) {
			for (byte j = 0; j<64; j++) {
				if (history[1][i][j]!=0) {
					System.out.println(h(i)+" "+h(j)+" "+(double)history[1][i][j]/(historyFreq[1][i][j]+1));
				}
			}
		}
		System.out.println();
		for (byte i = 0; i<64; i++) {
			for (byte j = 0; j<64; j++) {
				if (history[0][i][j]!=0) {
					System.out.println(h(i)+" "+h(j)+" "+(double)history[0][i][j]/(historyFreq[0][i][j]+1));
				}
			}
		}
		*/
        //searchFile.flush();
    }
    public static void iterativeDeepening(Board board) {
        iterativeDeepening(board,50);
    }
    public static int search(int depth, Board board, int alpha, int beta, boolean first, Move previousMove, boolean addToFile) {
        //System.out.println(board.moves);
        /*
        String fileLine = "";
        for (int i = 9; i>depth; i--) {
            fileLine+=" ";
        }
        if (addToFile) searchFile.println(fileLine+previousMove+" "+depth);
        */
        //System.out.println(fileLine+first);
        //System.out.println(fileLine+board.previousMoves);
        if (System.currentTimeMillis()>startTime+maxtime) return TIMEOUT;
        numPositions++;
        int side = board.whiteToMove?0:1;
        if (!first && board.squareControlled(board.kingCoords[1-side],board.whiteToMove)) {
            //System.out.println(board);
            //if (addToFile) searchFile.println(fileLine+"CHECKMATE");
            return CHECKMATE+depth;
        }
        //if (board.squareControlled(board.kingCoords[side],!board.whiteToMove)) depth++;
        boolean IID = true;
        for (int d = 0; d<=depth; d++) {
            if (bestMoves.containsKey(board.getKey()^PreCompute.depthHash[d])) {
                IID=false;
                break;
            }
        }
        //boolean IID = !bestMoves.containsKey(board.getKey());
        if (!first && exactTable.containsKey(PreCompute.depthHash[depth]^board.getKey())) {
            //if (addToFile) searchFile.println(fileLine+"TABLE 1: "+exactTable.get(PreCompute.depthHash[depth]^board.getKey()));
            return exactTable.get(PreCompute.depthHash[depth]^board.getKey());
        }
        else if (!first && upperTable.containsKey(PreCompute.depthHash[depth]^board.getKey())) { // Upper bound
            beta=Math.min(beta, upperTable.get(PreCompute.depthHash[depth]^board.getKey()));
            IID=false;
        } else if (!first && lowerTable.containsKey(PreCompute.depthHash[depth]^board.getKey())) { // Lower bound
            alpha=Math.max(alpha, lowerTable.get(PreCompute.depthHash[depth]^board.getKey()));
            IID=false;
        }
        if (alpha>=beta) {
            //if (addToFile) searchFile.println(fileLine+"TABLE 2: "+alpha+" "+beta+" "+lowerTable.get(PreCompute.depthHash[depth]^board.getKey())+" "+upperTable.get(PreCompute.depthHash[depth]^board.getKey()));
            return beta;
        }
        if (depth==0) {
            int res = searchCaptures(board,alpha,beta,0,false);
            //if (addToFile) searchFile.println(fileLine+"QSEARCH: "+res);
            return res;
        }
        byte oldEnPassant = board.makeNullMove();
        int nullEval = -search(Math.max(0,depth-3),board,-beta,-alpha,false,new Move(),false);
        //if (addToFile) searchFile.println(nullEval+" "+alpha+" "+beta);
        board.undoNullMove(oldEnPassant);
        if (nullEval>=beta) {
            depth-=4;
            if (depth<=0) {
                //if (addToFile) searchFile.println(fileLine+"QSEARCH NULL: "+nullEval);
                //nmrElapsed +=System.currentTimeMillis()-t;
                int res = searchCaptures(board,alpha,beta,0,false);
                return res;
            }
        }
        //nmrElapsed +=System.currentTimeMillis()-t;

        if (IID && depth>4) search(depth/4,board,alpha,beta,true,previousMove,false);

        boolean improvedAlpha = false;
        ArrayList<Move> initMoves = board.getMoveList();
        //count++;
        ArrayList<Move> moves = getSortedMoves(initMoves,board,previousMove,depth);
        Move bestMove = new Move();
        int bestScore=-2*CHECKMATE;
        int bestIdx = 0;
        byte idx = 1;
        boolean inCheck = board.squareControlled(board.kingCoords[side],!board.whiteToMove);
        for (Move m : moves) {
            //if (first) System.out.println(m.score()+" "+m);
            m.makeMove(board);
            board.moves.add(m);
            //System.out.println(board.previousMoves+" "+depth);
            if (board.previousMoves.contains(board.getKey())) {
                board.moves.remove(m);
                m.undoMove(board);
                //System.out.println(fileLine+"REPETITION");
                return 0;
            }
            int eval;
            if (board.previousMoves.contains(board.getKey())) {
                eval = 0;
            } else {
                boolean reducedDepth = true;
                if (idx<=3 || depth<3 || inCheck || board.squareControlled(board.kingCoords[1-side],!board.whiteToMove) || !(m.getClass()==Move.class || m.getClass()==CastleMove.class)) {
                    reducedDepth=false;
                }
                board.previousMoves.add(board.getKey());
                eval = -search(depth-1-(reducedDepth?1:0),board,-beta,-alpha,false,m,addToFile);
                if (reducedDepth && eval>alpha) eval = -search(depth-1,board,-beta,-alpha,false,m,addToFile);
                board.previousMoves.remove(board.getKey());
                if (eval==CHECKMATE+depth-2 && !((!board.whiteToMove && board.squareControlled(board.kingCoords[1],true)) || (board.whiteToMove && board.squareControlled(board.kingCoords[0],false)))) eval = 0;
            }
            //if (first) System.out.println(alpha+" "+beta+" "+m+" "+m.score()+" "+-search(depth-1,board,-2*CHECKMATE,2*CHECKMATE,false,m));
            board.moves.remove(m);
            m.undoMove(board);
            if (eval==-TIMEOUT) return TIMEOUT;
            if (eval>=CHECKMATE) {
                if (first) {
                    //bestMoves.put(board.getKey(),m);
                    bestMoves.put(board.getKey()^PreCompute.depthHash[depth],m);
                }
                //if (addToFile) searchFile.println(fileLine+"CHECKMATE");
                return eval;
            }
            if (eval>=beta) {
                bestIdx=idx;
                lowerTable.put(PreCompute.depthHash[depth]^board.getKey(),beta);
                total++;
                if (m.equals(moves.get(0))) predicted++;
                else {
                    //bestMoves.put(board.getKey(),m);
                    bestMoves.put(board.getKey()^PreCompute.depthHash[depth],m);
                }
                if (first) {
                    //bestMoves.put(board.getKey(),m);
                    bestMoves.put(board.getKey()^PreCompute.depthHash[depth],m);
                }
                if (m.getClass()==Move.class || m.getClass()==CastleMove.class) {
                    m.orderingIdx=eval;
                    m.order=idx;
                    Move[] killer = killerMap.get(board.numMoves);
                    if (idx!=1&&!m.equals(killer[0])&&!m.equals(killer[1])&&!m.equals(killer[2])) {
                        if (idx>killer[2].orderingIdx) {
                            if (idx<killer[1].orderingIdx) {
                                killer[2]=m;
                            } else if (idx<killer[0].orderingIdx) {
                                killer[2]=killer[1];
                                killer[1]=m;
                            } else {
                                killer[2]=killer[1];
                                killer[1]=killer[0];
                                killer[0]=m;
                            }
                        }
                    }
                    history[board.whiteToMove?0:1][m.getStart()][m.getEnd()]+=depth*depth;
                    for (Move m2 : moves) {
                        if (m2.equals(m)) break;
                        history[board.whiteToMove?0:1][m2.getStart()][m2.getEnd()]-=depth*depth;
                        historyFreq[board.whiteToMove?0:1][m2.getStart()][m2.getEnd()]+=depth*depth;
                    }
                    if (depth>=3) counterMove[previousMove.getType()&7][previousMove.getEnd()]=m;
                }
                numHits+=bestIdx;
                bestIdxMap.put(bestIdx,bestIdxMap.get(bestIdx)+1);
                //if (addToFile) searchFile.println(fileLine+"BETA: "+m+ " "+eval+" "+depth);
                return eval;
            }
            if (eval>alpha) {
                improvedAlpha=true;
                alpha=eval;
            }
            if (eval>bestScore) {
                bestMove=m;
                bestScore=eval;
                bestIdx=idx;
            }
            idx++;
        }
        if (moves.size()==0) {
            exactTable.put(PreCompute.depthHash[depth]^board.getKey(),0);
            //if (addToFile) searchFile.println(fileLine+"STALEMATE");
            return 0;
        }
        // Upper bound
        if (improvedAlpha) exactTable.put(PreCompute.depthHash[depth]^board.getKey(),bestScore);
        else upperTable.put(PreCompute.depthHash[depth]^board.getKey(),bestScore);
        total++;
        if (bestMove.equals(moves.get(0))) predicted++;
        else {
            //bestMoves.put(board.getKey(),bestMove);
            bestMoves.put(board.getKey()^PreCompute.depthHash[depth],bestMove);
        }
        if (first) {
            //bestMoves.put(board.getKey(),bestMove);
            bestMoves.put(board.getKey()^PreCompute.depthHash[depth],bestMove);
        }
		/*
		if (bestIdx==2) {
			System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
			System.out.println(bestMove+" "+bestMove.preScore);
			System.out.println(moves.get(0)+" "+moves.get(0).preScore);
		}
		*/
        moveTypes.put(moves.get(0).getClass(),moveTypes.get(moves.get(0).getClass())+1);
        numHits+=bestIdx;
        bestIdxMap.put(bestIdx,bestIdxMap.get(bestIdx)+1);
        //if (addToFile) searchFile.println(fileLine+"END SEARCH: "+previousMove+ " "+bestScore+" "+depth);
        //System.out.println(moves+" "+bestMove);
        return bestScore;
    }
    public static int searchCaptures(Board board, int alpha, int beta, int depth, boolean lastMoveNull) {
        maxDepth=Math.max(maxDepth,depth);
        // Qmate
        long t = System.currentTimeMillis();
        if ((board.whiteToMove && board.squareControlled(board.kingCoords[1],true)) || (!board.whiteToMove && board.squareControlled(board.kingCoords[0],false))) {
            return CHECKMATE;
        }
        numCapturePositions++;
        ArrayList<Move> moves;
        //Qsort
        if ((board.whiteToMove && board.squareControlled(board.kingCoords[0],false)) || (!board.whiteToMove && board.squareControlled(board.kingCoords[1],true))) {
            t=System.currentTimeMillis();
            alpha=Math.max(alpha,board.score(false));
            //System.out.println(board.score(false));
            t=System.currentTimeMillis();
            ArrayList<Move> initMoves = board.getMoveList();
            t=System.currentTimeMillis();
            moves=getSortedMoves(initMoves,board,new Move(),0);
            //count++;
        } else {
            t=System.currentTimeMillis();
            int eval = board.score(false);
            if (eval>=beta) return beta;
            alpha=Math.max(alpha,eval);
            t=System.currentTimeMillis();
            ArrayList<Move> initMoves = board.getCaptureMoveList();
            t=System.currentTimeMillis();
            moves=getSortedCaptureMoves(initMoves,board);
            // NMR
            if (moves.size()==0&!lastMoveNull) {
                t=System.currentTimeMillis();
                byte oldEnPassant = board.makeNullMove();
                int res = -searchCaptures(board,-beta,-alpha,depth+1,true);
                board.undoNullMove(oldEnPassant);
                if (res>=beta) {
                    return res;
                }
                alpha=Math.max(alpha,res);
            } else if (moves.size()==0) {
                return eval;
            }
        }
        // Qmove
        for (Move m : moves) {
            if (m.score()<0) break;
            t=System.currentTimeMillis();
            m.makeMove(board);
            board.moves.add(m);
            int eval = -searchCaptures(board,-beta,-alpha,depth+1,false);
            //System.out.println(m+" "+eval);
            t=System.currentTimeMillis();
            m.undoMove(board);
            board.moves.remove(m);
            if (eval>=beta) return eval;
            alpha=Math.max(alpha,eval);
        }
		/*
		if (depth<5) {
			for (Move m : board.getCheckMoveList()) {
				//System.out.println(m);
				m.makeMove(board);
				int eval = -searchCaptures(board,-beta,-alpha,depth+1,false);
				m.undoMove(board);
				if (eval>=beta) return eval;
				alpha=Math.max(alpha,eval);
			}
		}
		*/
        return alpha;
    }
    public static ArrayList<Move> getSortedMoves(ArrayList<Move> moves, Board board, Move previousMove, int depth) {
        Move[] killer = killerMap.get(board.numMoves);
        //int d = depth;
        //for (;d>0&&!bestMoves.containsKey(board.getKey()^PreCompute.depthHash[d]); d--);

        for (Move m : moves) {

            for (int d = depth; d>=0; d--) {
                if (m.equals(bestMoves.getOrDefault(board.getKey()^PreCompute.depthHash[d],new Move()))) {
                    m.preScore=1000000000+d;
                    break;
                }
            }
            if (m.preScore>=1000000000) continue;
            if (m.equals(killer[0])) m.preScore-=100000000;
            else if (m.equals(killer[1])) m.preScore+=90000000;
            else if (m.equals(killer[2])) m.preScore+=80000000;
            else if (m.equals(counterMove[previousMove.getType()&7][previousMove.getEnd()])) m.preScore+=9000000;
        }
        moves.sort(Comparator.comparingInt(o->((-o.score()))));


        //moves.sort(Comparator.comparingInt(o->(-o.score()-(o.equals(killer[0])?100000000:0)-(o.equals(killer[1])?90000000:0)-(o.equals(killer[2])?80000000:0)-(o.equals(counterMove[previousMove.getType()&7][previousMove.getEnd()])?9000000:0)-(o.equals(bestMoves.getOrDefault(board.getKey(),new Move()))?1000000000:0))));
        return moves;
    }
    //public static ArrayList<Move> getSortedCheckEvasionMoves(ArrayList<Move> moves, Board board, )
    public static ArrayList<Move> getSortedCaptureMoves(ArrayList<Move> moves, Board board) {
        moves.sort(Comparator.comparingInt(o->((-o.score()))));
        return moves;
    }
    public static byte f(String pos) {
        byte res = 0;
        switch(pos.charAt(0)) {
            case 'a': res=0;break;
            case 'b': res=1;break;
            case 'c': res=2;break;
            case 'd': res=3;break;
            case 'e': res=4;break;
            case 'f': res=5;break;
            case 'g': res=6;break;
            case 'h': res=7;break;
        }
        switch(pos.charAt(1)) {
            case '1': res+=0;break;
            case '2': res+=8;break;
            case '3': res+=16;break;
            case '4': res+=24;break;
            case '5': res+=32;break;
            case '6': res+=40;break;
            case '7': res+=48;break;
            case '8': res+=56;break;
        }
        return res;
    }
    public static byte g(String piece) {
        byte res = 0;
        switch (piece.charAt(0)) {
            case 'W': res=8;break;
            case 'B': res=16;break;
        }
        switch (piece.charAt(1)) {
            case 'K':break;
            case 'P':res+=1;break;
            case 'N':res+=2;break;
            case 'B':res+=3;break;
            case 'R':res+=4;break;
            case 'Q':res+=5;break;
        }
        return res;
    }
    public static String h(byte square) {
        String res = "";
        switch(square&7) {
            case 0: res+="a";break;
            case 1: res+="b";break;
            case 2: res+="c";break;
            case 3: res+="d";break;
            case 4: res+="e";break;
            case 5: res+="f";break;
            case 6: res+="g";break;
            case 7: res+="h";break;
        }
        switch(square>>3) {
            case 0: res+="1";break;
            case 1: res+="2";break;
            case 2: res+="3";break;
            case 3: res+="4";break;
            case 4: res+="5";break;
            case 5: res+="6";break;
            case 6: res+="7";break;
            case 7: res+="8";break;
        }
        return res;
    }
    public static int count(int depth, Board board) {
        if (board.whiteToMove) {
            if (board.squareControlled(board.kingCoords[1],true)) {
                return 0;
            }
        } else {
            if (board.squareControlled(board.kingCoords[0],false)) {
                return 0;
            }
        }
        if (depth==0) {
            return 1;
        }
        int res = 0;
        long t = System.currentTimeMillis();
        ArrayList<Move> moves = board.getMoveList();
        for (Move m : moves) {
            m.makeMove(board);
            res+=count(depth-1,board);
            m.undoMove(board);
        }
        return res;
    }
    public static void clear() {
        //lowerTable=new HashMap<>();
        //upperTable=new HashMap<>();
        //exactTable=new HashMap<>();
        numPositions=0;
        numCapturePositions=0;
        //predicted=total=0;
    }
    public static Move tokenToMove(String token,Board board) {
        String[] tokens = token.split(" ");
        if (tokens.length==1) {
            // Castle
            if (tokens[0].length()==3) {
                return new CastleMove((byte)(board.whiteToMove?0:2),board);
            } else {
                return new CastleMove((byte)(board.whiteToMove?1:3),board);
            }
        }
        byte startSquare = f(tokens[0]);
        byte endSquare = f(tokens[1]);
        byte pieceType = board.board[startSquare];
        byte rank = (byte)((startSquare>>3)+1);
        byte file = (byte)(startSquare&7);
        if ((rank==5 && board.whiteToMove && pieceType==9) || (rank==4 && !board.whiteToMove && pieceType==17) && board.validEnPassant!=9 && Math.abs(file-board.validEnPassant)==1) {
            // En Passant
            byte captureSquare = (byte)(board.validEnPassant+(board.whiteToMove?32:24));
            return new EnPassantMove(startSquare,captureSquare,endSquare,pieceType,board);
        } else if (board.board[endSquare]==0) {
            if (tokens.length==2) {
                // Move
                return new Move(startSquare,endSquare,pieceType,board);
            } else {
                // Promotion
                return new PromotionMove(startSquare,endSquare,g(tokens[2]));
            }
        } else {
            if (tokens.length==2) {
                // Capture
                return new CaptureMove(startSquare,endSquare,pieceType,board.board[endSquare],board);
            } else {
                // Promotion Capture
                return new PromotionCaptureMove(startSquare,endSquare,g(tokens[2]),board.board[endSquare]);
            }
        }
    }
    private static class FastScanner {
        final private int BUFFER_SIZE = 1 << 16;
        private DataInputStream din;
        private byte[] buffer;
        private int bufferPointer, bytesRead;
        private FastScanner(boolean usingFile) throws IOException {
            if (usingFile) din=new DataInputStream(new FileInputStream("SEETest.txt"));
            else din = new DataInputStream(System.in);
            buffer = new byte[BUFFER_SIZE];
            bufferPointer = bytesRead = 0;
        }
        private int nextInt() throws IOException {
            int ret = 0;
            byte c = read();
            while (c <= ' ') c = read();
            boolean neg = (c == '-');
            if (neg) c = read();
            do ret = ret * 10 + c - '0';
            while ((c = read()) >= '0' && c <= '9');
            if (neg) return -ret;
            return ret;
        }
        public long nextLong() throws IOException {
            long ret = 0;
            byte c = read();
            while (c <= ' ') c = read();
            boolean neg = (c == '-');
            if (neg) c = read();
            do ret = ret * 10 + c - '0';
            while ((c = read()) >= '0' && c <= '9');
            if (neg) return -ret;
            return ret;
        }
        private String nextLine() throws IOException {
            StringBuilder ret = new StringBuilder();
            byte c = read();
            do {
                ret.append((char)c);
            } while ((c = read())!='\n');
            return ret.toString();
        }
        private char nextChar() throws IOException {
            byte c = read();
            while (c <= ' ') c = read();
            return (char)c;
        }
        private String nextString() throws IOException {
            StringBuilder ret = new StringBuilder();
            byte c = read();
            while (c <=' ') c = read();
            do {
                ret.append((char)c);
            } while ((c = read())>' ');
            return ret.toString();
        }
        private void fillBuffer() throws IOException {
            bytesRead = din.read(buffer, bufferPointer = 0, BUFFER_SIZE);
            if (bytesRead == -1)  buffer[0] = -1;
        }
        private byte read() throws IOException {
            if (bufferPointer == bytesRead) fillBuffer();
            return buffer[bufferPointer++];
        }
    }

}
