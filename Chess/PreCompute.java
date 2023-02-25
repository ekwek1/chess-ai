package Chess;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;


public class PreCompute {
    // N,S,W,E,NW,SE,NE,SW
    public static byte[] directionOffsets = {8,-8,-1,1,7,-7,9,-9};
    public static byte[] knightOffsets = {-17,-15,-10,-6,6,10,15,17};
    public static byte[][] knightDirections = {{1,2},{1,3},{2,1},{3,1},{2,0},{3,0},{0,2},{0,3}};
    public static byte[][] distToEdge = new byte[8][64];
    public static byte[][] vectorToIdx = {{7,1,5},{2,-1,3},{4,0,6}};
    public static byte[] deltaToIdx = {7,1,5,-1,-1,-1,-1,-1,2,-1,3,-1,-1,-1,-1,-1,4,0,6};
    public static short[] pieceValues = {9000,100,325,325,500,975};
    public static short[] passedValuesMG = {0,4,7,6,24,66,108,0};
    public static short[] passedValuesEG = {0,11,13,16,28,69,102,0};
    public static long[] fileMask = new long[8];
    public static long[] rankMask = new long[8];
    public static long[] knightNeighborMask = new long[64];
    public static long[] kingNeighborMask = new long[64];
    public static long[] kingZoneWhite = new long[64];
    public static long[] kingZoneBlack = new long[64];
    public static long[][] ray = new long[8][64];
    public static long[][] rayWStart = new long[8][64];
    public static long whiteKingsideKing = 0b1110000011100000;
    public static long whiteKingsidePawn = 0b111000001110000000000000;
    public static long whiteQueensideKing = 0b11100000111;
    public static long whiteQueensidePawn = 0b1110000011100000000;
    public static long blackKingsideKing = Long.reverse(whiteQueensideKing);
    public static long blackKingsidePawn = Long.reverse(whiteQueensidePawn);
    public static long blackQueensideKing = Long.reverse(whiteKingsideKing);
    public static long blackQueensidePawn = Long.reverse(whiteKingsidePawn);
    public static long whiteSide = 0b0000000000000000000000000000000000111100001111000000000000000000;
    public static long blackSide = 0b0000000000000000001111000011110000000000000000000000000000000000L;
    public static byte[][] dist = new byte[64][64];
    public static double[][] mobilitymg;
    public static double[][] mobilityeg;
    public static long[][] pieceHash = new long[64][22];
    public static long turnHash;
    public static long[] castleHash = new long[16];
    public static long[] enPassantHash = new long[10];
    public static long[] depthHash = new long[1000];
    public static HashMap<Long,HashMap<Move,Integer>> book = new HashMap<>();
    public PreCompute() throws IOException {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                byte N = (byte) (7 - j);
                byte S = (byte) j;
                byte W = (byte) i;
                byte E = (byte) (7 - i);
                byte idx = (byte) ((j << 3) + i);
                distToEdge[0][idx] = N;
                distToEdge[1][idx] = S;
                distToEdge[2][idx] = W;
                distToEdge[3][idx] = E;
                distToEdge[4][idx] = (byte) Math.min(N, W);
                distToEdge[5][idx] = (byte) Math.min(S, E);
                distToEdge[6][idx] = (byte) Math.min(N, E);
                distToEdge[7][idx] = (byte) Math.min(S, W);
            }
        }
        for (int i = 0; i<8; i++) {
            long res = 0;
            for (int j = 0; j<8; j++) {
                res^=1L<<(i+(j<<3));
            }
            fileMask[i]=res;
        }
        for (int j = 0; j<8; j++) {
            long res = 0;
            for (int i = 0; i<8; i++) {
                res^=1L<<(i+(j<<3));
            }
            rankMask[j]=res;
        }
        for (byte i = 0; i<64; i++) {
            for (byte dir = 0; dir<8; dir++) {
                if (distToEdge[dir][i]>=1) kingNeighborMask[i]|=1L<<(i+directionOffsets[dir]);

                byte longDirection = PreCompute.knightDirections[dir][0];
                byte shortDirection = PreCompute.knightDirections[dir][1];
                if (PreCompute.distToEdge[longDirection][i]<2 || PreCompute.distToEdge[shortDirection][i]<1) continue;
                knightNeighborMask[i]|=1L<<(i+knightOffsets[dir]);
            }
        }
        for (byte i = 0; i<64; i++) {
            kingZoneWhite[i]=kingNeighborMask[i]|(i<56?kingNeighborMask[i+8]:0);
            kingZoneBlack[i]=kingNeighborMask[i]|(i>=8?kingNeighborMask[i-8]:0);
        }
        for (int i = 0; i<8; i++) {
            for (byte j = 0; j<64; j++) {
                long res = 0;
                byte pos = j;
                for (int k = 0; k<distToEdge[i][j]; k++) {
                    pos+=directionOffsets[i];
                    res|=1L<<pos;
                }
                ray[i][j]=res;
                rayWStart[i][j]=res|1L<<j;
            }
        }
        for (int a = 0; a<8; a++) {
            for (int b = 0; b<8; b++) {
                for (int c = 0; c<8; c++) {
                    for (int d = 0; d<8; d++) {
                        dist[(a<<3)+b][(c<<3)+d]=(byte)(14-Math.abs(c-a)-Math.abs(d-b));
                    }
                }
            }
        }
        mobilitymg = new double[][]{
                {-62,-53,-12,-4,3,13,22,28,33},
                {-48,-20,16,26,38,51,55,63,63,68,81,81,91,98},
                {-60,-20,2,3,3,11,22,31,40,40,41,48,57,57,62},
                {-30,-12,-8,-9,20,23,23,35,38,53,64,65,65,66,67,67,72,72,77,79,93,108,108,108,110,114,114,116}
        };
        mobilityeg = new double[][]{
                {-81,-56,-31,-16,5,11,17,20,25},
                {-59,-23,-3,13,24,42,54,57,65,73,78,86,88,97},
                {-78,-17,23,39,70,99,103,121,134,139,158,164,168,169,172},
                {-48,-30,-7,19,40,55,59,75,78,96,96,100,121,127,131,133,136,141,147,150,151,168,168,171,182,182,192,219}
        };
        for (int i = 0; i<4; i++) {
            for (int j = 0; j<mobilitymg[i].length; j++) {
                mobilitymg[i][j]*=100;
                mobilitymg[i][j]/=256;
                mobilityeg[i][j]*=100;
                mobilityeg[i][j]/=256;
            }
        }
        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 22; j++) {
                pieceHash[i][j] = (long) (Long.MAX_VALUE * Math.random());
            }
        }
        turnHash = (long) (Long.MAX_VALUE * Math.random());
        for (int i = 0; i < 16; i++) {
            castleHash[i] = (long) (Long.MAX_VALUE * Math.random());
        }
        for (int i = 0; i < 8; i++) {
            enPassantHash[i] = (long) (Long.MAX_VALUE * Math.random());
        }
        for (int i = 0; i < 1000; i++) {
            depthHash[i] = (long) (Long.MAX_VALUE * Math.random());
        }
        //loadBook();
        System.out.println("LOADING COMPLETE");
    }
    public static void loadBook() throws IOException {
        FastScanner input = new FastScanner(true);

        for (int i = 0; i<7756; i++) {
            //System.out.println(i);
            String[] tokens = input.nextLine().split(" ");
            Board board = new Board();
            for (int j = 0; j<=Math.min(10,tokens.length-1); j++) {
                String token = tokens[j];
                Move move = tokenToMove(token,board);
                Move m = new Move();
                if (!book.containsKey(board.getKey())) {
                    book.put(board.getKey(),new HashMap<>());
                    book.get(board.getKey()).put(m,0);
                }
                book.get(board.getKey()).put(m,book.get(board.getKey()).get(m)+1);
                book.get(board.getKey()).put(move,book.get(board.getKey()).getOrDefault(move,0)+1);
                move.makeMove(board);
            }
        }
    }
    public static Move tokenToMove(String token, Board board) {
        int side = board.whiteToMove?0:1;
        if (token.charAt(token.length()-1)=='+') token=token.substring(0,token.length()-1);
        if (Character.isLowerCase(token.charAt(0))) token="P"+token;
        int N = token.length();
        int newPieceType = 0;
        if (token.contains("=")) {
            newPieceType = (byte)(8*side+8);
            switch (token.charAt(N-1)) {
                case 'Q': newPieceType++;
                case 'R': newPieceType++;
                case 'B': newPieceType++;
                case 'N': newPieceType++;
                case 'P': newPieceType++;
            }
            token=token.substring(0,N-2);
            N-=2;
        }
        if (token.equals("O-O")) return new CastleMove((byte)(board.whiteToMove?0:2),board);
        if (token.equals("O-O-O")) return new CastleMove((byte)(board.whiteToMove?1:3),board);
        byte newCoords = Main.f(token.substring(N-2,N));
        String prefix = token.substring(0,N-2);
        char ambiguity = prefix.length()>=2 && prefix.charAt(1)!='x'?prefix.charAt(1):' ';
        long ambiguityMask = 0b1111111111111111111111111111111111111111111111111111111111111111L;
        if (ambiguity!=' ') {
            if (Character.isDigit(ambiguity)) ambiguityMask=rankMask[ambiguity-'1'];
            else ambiguityMask=fileMask[ambiguity-'a'];
        }
        byte pieceType = (byte)(8*side+8);
        if (Character.isUpperCase(token.charAt(0))) {
            switch (token.charAt(0)) {
                case 'Q': pieceType++;
                case 'R': pieceType++;
                case 'B': pieceType++;
                case 'N': pieceType++;
                case 'P': pieceType++;
            }
        } else pieceType++;
        ArrayList<Move> moves = board.getLegalMoveList();
        ArrayList<Move> res = new ArrayList<>();
        for (Move m : moves) {
            if (newCoords==m.getEnd() && pieceType==m.getType() && (ambiguityMask&(1L<<m.getStart()))!=0) {
                res.add(m);
            }
        }
        if (res.size()==1) return res.get(0);
        for (Move m : res) {
            if (m.getClass()!=PromotionMove.class && m.getClass()!=PromotionCaptureMove.class) {
                System.out.println("ERROR 1");
                return new Move();
            }
        }
        System.out.println(newPieceType);
        for (Move m : res) {
            if (m.getClass()==PromotionMove.class && ((PromotionMove)m).getNewType()==newPieceType) return m;
            if (m.getClass()==PromotionCaptureMove.class && ((PromotionCaptureMove)m).getNewType()==newPieceType) return m;
        }
        System.out.println("ERROR 2");
        return new Move();
    }
    public static String simpleMoveToToken(Move m, Board board) {
        m.makeMove(board);
        String check = board.squareControlled(board.kingCoords[board.whiteToMove?0:1],!board.whiteToMove)?"+":"";
        m.undoMove(board);
        if (m.getClass()==CastleMove.class) {
            if ((((CastleMove)m).castleType()&1)==0) return "O-O";
            else return "O-O-O";
        } else if (m.getClass()==PromotionMove.class) {
            return Main.h(m.getEnd())+"="+new String[]{"K","","N","B","R","Q"}[((PromotionMove)m).getNewType()&7]+check;
        } else if (m.getClass()==PromotionCaptureMove.class) {
            return Main.h(m.getStart()).charAt(0)+"x"+ Main.h(m.getEnd())+"="+new String[]{"K","","N","B","R","Q"}[((PromotionCaptureMove)m).getNewType()&7]+check;
        }
        String pieceType = new String[]{"K","","N","B","R","Q"}[m.getType()&7];
        String endSquare = Main.h(m.getEnd());
        String capture = "";
        if (m.getClass()==CaptureMove.class || m.getClass()==EnPassantMove.class || m.getClass()==PromotionCaptureMove.class) {
            capture = "x";
            if (pieceType.equals("")) capture=Character.toString((Main.h(m.getStart()).charAt(0)))+"x";
        }
        return pieceType+capture+endSquare+check;
    }
    public static String moveToToken(Move m, Board board) {
        if (m==null) return null;
        if (m.getClass()==CastleMove.class) {
            if ((((CastleMove)m).castleType()&1)==0) return "O-O";
            else return "O-O-O";
        }
        String target = simpleMoveToToken(m,board);
        int total = 0;
        int fileCount = 0;
        int rankCount = 0;
        ArrayList<Move> moves = board.getLegalMoveList();
        for (Move move : moves) {
            if (simpleMoveToToken(move,board).equals(target)) {
                total++;
                if (Main.h(m.getStart()).charAt(0)== Main.h(move.getStart()).charAt(0)) fileCount++;
                if (Main.h(m.getStart()).charAt(1)== Main.h(move.getStart()).charAt(1)) rankCount++;
            }
        }
        if (total==1) return target;
        if (fileCount==1) return Character.toString(target.charAt(0))+ Main.h(m.getStart()).charAt(0)+target.substring(1);
        if (rankCount==1) return Character.toString(target.charAt(0))+ Main.h(m.getStart()).charAt(1)+target.substring(1);
        return target;
    }
    private static class FastScanner {
        final private int BUFFER_SIZE = 1 << 16;
        private DataInputStream din;
        private byte[] buffer;
        private int bufferPointer, bytesRead;
        private FastScanner(boolean usingFile) throws IOException {
            if (usingFile) din=new DataInputStream(new FileInputStream("openingbook.txt"));
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
