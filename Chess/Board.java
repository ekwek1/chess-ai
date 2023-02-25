package Chess;

import java.util.*;

public class Board {
    public byte[] board = new byte[64];
    /*
     Empty: 0
     White King: 8
     White Pawn: 9
     White Knight: 10
     White Bishop: 11
     White Rook: 12
     White Queen: 13
     Black King: 16
     Black Pawn: 17
     Black Knight: 18
     Black Bishop: 19
     Black Rook: 20
     Black Queen: 21
    */
    public byte validEnPassant = 9;
    public long[][] occupancy = new long[2][7];
    public byte[][] numPieces = new byte[2][6];
    public byte canCastle;// King castle, Queen castle
    public boolean whiteToMove = true;
    public long key = 0;
    public int numMoves = 0;
    public ArrayList<Move> moves = new ArrayList<>();
    public HashSet<Long> previousMoves = new HashSet<>();
    public short whiteStartBonus;
    public short whiteEndBonus;
    public short blackStartBonus;
    public short blackEndBonus;
    public static int numTimes = 0;
    public static long elapsed = 0;
    public static long elapsed2 = 0;
    public static long elapsed3 = 0;
    public byte[] kingCoords = new byte[2];
    public long[][] mask = new long[2][7];
    public long pieceMask;
    public int maxGuarding;
    public Board() {
        this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }
    public Board(String fenString) {
        canCastle = 0;
        String[] tokens = fenString.split(" ");
        if (tokens[3].equals("-")) {
            validEnPassant=9;
        } else {
            switch (tokens[3].charAt(0)) {
                case 'a': validEnPassant=0;break;
                case 'b': validEnPassant=1;break;
                case 'c': validEnPassant=2;break;
                case 'd': validEnPassant=3;break;
                case 'e': validEnPassant=4;break;
                case 'f': validEnPassant=5;break;
                case 'g': validEnPassant=6;break;
                case 'h': validEnPassant=7;break;
            }
        }
        for (char c : tokens[2].toCharArray()) {
            switch (c) {
                case 'K': canCastle|=1;break;
                case 'Q': canCastle|=2;break;
                case 'k': canCastle|=4;break;
                case 'q': canCastle|=8;break;
            }
        }
        whiteToMove=tokens[1].equals("w");
        String[] boardEnc = tokens[0].split("/");

        for (int i = 0; i<8; i++) {
            int cur = 0;
            for (int j = 0; j<boardEnc[7-i].length(); j++) {
                char c = boardEnc[7-i].charAt(j);
                byte square = (byte)((i<<3)+cur);
                if (Character.isDigit(c)) {
                    cur+=c-'1';
                } else if (Character.toLowerCase(c)==c) { // black
                    switch (c) {
                        case 'k':
                            board[square]=16;
                            mask[1][0]|=1L<<square;
                            numPieces[1][0]++;
                            break;
                        case 'p':
                            board[square]=17;
                            mask[1][1]|=1L<<square;
                            numPieces[1][1]++;
                            break;
                        case 'n':
                            board[square]=18;
                            mask[1][2]|=1L<<square;
                            numPieces[1][2]++;
                            break;
                        case 'b':
                            board[square]=19;
                            mask[1][3]|=1L<<square;
                            numPieces[1][3]++;
                            break;
                        case 'r':
                            board[square]=20;
                            mask[1][4]|=1L<<square;
                            numPieces[1][4]++;
                            break;
                        case 'q':
                            board[square]=21;
                            mask[1][5]|=1L<<square;
                            numPieces[1][5]++;
                            break;
                    }
                    mask[1][6]|=1L<<square;
                } else { // white
                    switch (Character.toLowerCase(c)) {
                        case 'k':
                            board[square]=8;
                            mask[0][0]|=1L<<square;
                            numPieces[0][0]++;
                            break;
                        case 'p':
                            board[square]=9;
                            mask[0][1]|=1L<<square;
                            numPieces[0][1]++;
                            break;
                        case 'n':
                            board[square]=10;
                            mask[0][2]|=1L<<square;
                            numPieces[0][2]++;
                            break;
                        case 'b':
                            board[square]=11;
                            mask[0][3]|=1L<<square;
                            numPieces[0][3]++;
                            break;
                        case 'r':
                            board[square]=12;
                            mask[0][4]|=1L<<square;
                            numPieces[0][4]++;
                            break;
                        case 'q':
                            board[square]=13;
                            mask[0][5]|=1L<<square;
                            numPieces[0][5]++;
                            break;
                    }
                    mask[0][6]|=1L<<square;
                }
                cur++;

            }
        }
        numMoves=whiteToMove?2*Integer.parseInt(tokens[5])-2:2*Integer.parseInt(tokens[5])-1;
        for (byte piece = 0; piece<6; piece++) {
            long whiteMask = mask[0][piece];
            while (whiteMask!=0) {
                byte pos = bsf(whiteMask);
                whiteStartBonus+= Main.player.start[8+piece][pos];
                whiteEndBonus+= Main.player.end[8+piece][pos];
                whiteMask&=whiteMask-1;
            }
            long blackMask=mask[1][piece];
            while (blackMask!=0) {
                byte pos = bsf(blackMask);
                blackStartBonus+= Main.player.start[16+piece][pos];
                blackEndBonus+= Main.player.end[16+piece][pos];
                blackMask&=blackMask-1;
            }
        }
        pieceMask=mask[0][6]^mask[1][6];
        kingCoords[0]=bsf(mask[0][0]);
        kingCoords[1]=bsf(mask[1][0]);
        generateKey();
        updateOccupancy();
        previousMoves.add(getKey());
    }
    public String toString() {
        String res = "BOARD\n";
        for (int i = 7; i>=0; i--) {
            for (int j = 0; j<8; j++) {
                res+=board[(i<<3)+j]+"\t";
            }
            res+="\n";
        }
        return res;
    }
    public boolean works() {
        for (int i = 0; i<64; i++) {
            if (board[i]==0) continue;
            if ((mask[(board[i]-8)/8][board[i]%8]&1L<<i)==0) return false;
        }
        return true;
    }
    private void addSlidingPieceMoves(ArrayList<Move> moveList, byte coords, byte type, boolean captureOnly) {
        byte i = 0;
        byte j = 8;
        if (type==4) j=4;
        else if (type==3) i=4;
        byte pieceType = (byte)(type+(whiteToMove?8:16));
        for (;i<j; i++) {
            byte newCoords=coords;
            byte delta = PreCompute.directionOffsets[i];
            for (byte k = 0; k<PreCompute.distToEdge[i][coords]; k++) {
                newCoords+=delta;
                if (board[newCoords]==0) {
                    if (!captureOnly) moveList.add(new Move(coords,newCoords,pieceType,this));
                } else if ((board[newCoords]&16)!=0 ^ whiteToMove) break; // Blocked by friendly
                else if ((board[newCoords]&8)!=0 ^ whiteToMove) { // Blocked by enemy
                    moveList.add(new CaptureMove(coords, newCoords, pieceType, board[newCoords], this));
                    break;
                }
            }
        }
    }
    public long rookMask(byte coords) {
        long m = 0;
        for (int i = 0; i<4; i+=3) {
            long cur = PreCompute.ray[i][coords];
            byte blockerPos=bsf(pieceMask&cur);
            if (blockerPos!=-1) {
                cur^=PreCompute.ray[i][blockerPos];
            }
            m|=cur;
        }
        for (int i = 1; i<=2; i++) {
            long cur = PreCompute.ray[i][coords];
            byte blockerPos=bsr(pieceMask&cur);
            if (blockerPos!=-1) {
                cur^=PreCompute.ray[i][blockerPos];
            }
            m|=cur;
        }
        return m;
    }
    public long bishopMask(byte coords) {
        long m = 0;
        for (int i = 4; i<8; i+=2) {
            long cur = PreCompute.ray[i][coords];
            byte blockerPos=bsf(pieceMask&cur);
            if (blockerPos!=-1) {
                cur^=PreCompute.ray[i][blockerPos];
            }
            m|=cur;
        }
        for (int i = 5; i<8; i+=2) {
            long cur = PreCompute.ray[i][coords];
            byte blockerPos=bsr(pieceMask&cur);
            if (blockerPos!=-1) {
                cur^=PreCompute.ray[i][blockerPos];
            }
            m|=cur;
        }
        return m;
    }
    public long queenMask(byte coords) {
        return rookMask(coords)|bishopMask(coords);
    }
    public long knightMask(byte coords) {
        return PreCompute.knightNeighborMask[coords];
    }
    public long kingMask(byte coords) {
        return PreCompute.kingNeighborMask[coords];
    }
    public long totalPawnMask(boolean white) {
        if (white) return ((mask[0][1]&~PreCompute.fileMask[0])<<7)|((mask[0][1]&~PreCompute.fileMask[7])<<9);
        return ((mask[1][1]&~PreCompute.fileMask[0])>>9)|((mask[1][1]&~PreCompute.fileMask[7])>>7);
    }
    public long pawnMask(byte coords, boolean white) {
        long pawnMask = 1L<<coords;
        if (white) return ((pawnMask&~PreCompute.fileMask[0])<<7)|((pawnMask&~PreCompute.fileMask[7])<<9);
        return ((pawnMask&~PreCompute.fileMask[0])>>9)|((pawnMask&~PreCompute.fileMask[7])>>7);
    }
    public long rayMask(byte coords, byte dir) {
        long cur = PreCompute.ray[dir][coords];
        byte blockerPos=PreCompute.directionOffsets[dir]>0?bsf(pieceMask&cur):bsr(pieceMask&cur);
        if (blockerPos!=-1) {
            cur^=PreCompute.ray[dir][blockerPos];
        }
        return cur;
    }
    public void addRookMovesExperimental(ArrayList<Move> moveList, byte coords, boolean captureOnly) {
        long m = 0;
        for (int i = 0; i<4; i+=3) {
            long cur = PreCompute.ray[i][coords];
            byte blockerPos=bsf(pieceMask&cur);
            if (blockerPos!=-1) {
                cur^=PreCompute.rayWStart[i][blockerPos];
            }
            m|=cur;
            if (blockerPos!=-1 && (board[blockerPos]&8)!=(board[coords]&8)) moveList.add(new CaptureMove(coords,blockerPos,board[coords],board[blockerPos],this));
        }
        for (int i = 1; i<=2; i++) {
            long cur = PreCompute.ray[i][coords];
            byte blockerPos=bsr(pieceMask&cur);
            if (blockerPos!=-1) {
                cur^=PreCompute.rayWStart[i][blockerPos];
            }
            m|=cur;
            if (blockerPos!=-1 && (board[blockerPos]&8)!=(board[coords]&8)) moveList.add(new CaptureMove(coords,blockerPos,board[coords],board[blockerPos],this));
        }
        if (!captureOnly) {
            while (m!=0) {
                byte pos = bsf(m);
                m&=m-1;
                moveList.add(new Move(coords,pos,board[coords],this));
            }
        }
    }
    public void addBishopMovesExperimental(ArrayList<Move> moveList, byte coords, boolean captureOnly) {
        long m = 0;
        for (int i = 4; i<8; i++) {
            long cur = PreCompute.ray[i][coords];
            byte blockerPos=PreCompute.directionOffsets[i]>0?bsf(pieceMask&cur):bsr(pieceMask&cur);
            if (blockerPos!=-1) {
                cur^=PreCompute.rayWStart[i][blockerPos];
                if ((board[blockerPos]&8)!=(board[coords]&8)) moveList.add(new CaptureMove(coords,blockerPos,board[coords],board[blockerPos],this));
            }
            m|=cur;
        }
        if (!captureOnly) {
            while (m!=0) {
                byte pos = bsf(m);
                moveList.add(new Move(coords,pos,board[coords],this));
                m&=m-1;
            }
        }
    }
    private void addKnightMovesExperimental(ArrayList<Move> moveList, byte coords, boolean captureOnly) {
        int side = whiteToMove?0:1;
        byte type = (byte)((side<<3)+10);
        long captureMoves = PreCompute.knightNeighborMask[coords]&mask[1-side][6];
        if (!captureOnly) {
            long regMoves = PreCompute.knightNeighborMask[coords]&(~pieceMask);
            while (regMoves!=0) {
                byte pos = bsf(regMoves);
                moveList.add(new Move(coords,pos,type,this));
                regMoves&=regMoves-1;
            }
            while (captureMoves!=0) {
                byte pos = bsf(captureMoves);
                moveList.add(new CaptureMove(coords,pos,type,board[pos],this));
                captureMoves&=captureMoves-1;
            }
        } else {
            while (captureMoves!=0) {
                byte pos = (byte)Long.numberOfTrailingZeros(captureMoves);
                int preScore = SEE(coords,pos);
                if (preScore>=0) {
                    moveList.add(new CaptureMove(coords,pos,type,board[pos],this,preScore));
                }
                captureMoves&=captureMoves-1;
            }
        }
    }
    public void addPawnMovesExperimental(ArrayList<Move> moveList, boolean white, boolean captureOnly) {
        long nonPromote,promote,captureLeft,captureRight,forward1,forward2,promoteCaptureLeft,promoteCaptureRight,promoteForward;
        byte type,cLeft,cRight,cForward;
        if (white) {
            nonPromote = mask[0][1]&(~PreCompute.rankMask[6]);
            promote = mask[0][1]&(PreCompute.rankMask[6]);
            captureLeft = ((nonPromote&(~PreCompute.fileMask[0]))<<7)&mask[1][6];
            captureRight = ((nonPromote&(~PreCompute.fileMask[7]))<<9)&mask[1][6];
            forward1 = (nonPromote<<8)&(~pieceMask);
            forward2 = ((forward1&(PreCompute.rankMask[2]))<<8)&(~pieceMask);
            promoteCaptureLeft=((promote&(~PreCompute.fileMask[0]))<<7)&mask[1][6];
            promoteCaptureRight = ((promote&(~PreCompute.fileMask[7]))<<9)&mask[1][6];
            promoteForward = (promote<<8)&(~pieceMask);
            type=9;
            cLeft=7;
            cRight=9;
            cForward=8;
        } else {
            nonPromote = mask[1][1]&(~PreCompute.rankMask[1]);
            promote = mask[1][1]&(PreCompute.rankMask[1]);
            captureLeft = ((nonPromote&(~PreCompute.fileMask[0]))>>9)&mask[0][6];
            captureRight = ((nonPromote&(~PreCompute.fileMask[7]))>>7)&mask[0][6];
            forward1 = (nonPromote>>8)&(~pieceMask);
            forward2 = ((forward1&(PreCompute.rankMask[5]))>>8)&(~pieceMask);
            promoteCaptureLeft=((promote&(~PreCompute.fileMask[0]))>>9)&mask[0][6];
            promoteCaptureRight = ((promote&(~PreCompute.fileMask[7]))>>7)&mask[0][6];
            promoteForward = (promote>>8)&(~pieceMask);
            type=17;
            cLeft=-9;
            cRight=-7;
            cForward=-8;
        }
        //System.out.println(Long.toBinaryString(whitetotalPawnMask));
        //System.out.println(Long.toBinaryString(forward1));
        if (!captureOnly) {
            while (forward1!=0) {
                byte pos = bsf(forward1);
                moveList.add(new Move((byte)(pos-cForward),pos,type,this));
                forward1&=forward1-1;
            }
            while (forward2!=0) {
                byte pos = bsf(forward2);
                moveList.add(new Move((byte)(pos-2*cForward),pos,type,this));
                forward2&=forward2-1;
            }
            while (promoteForward!=0) {
                byte pos = bsf(promoteForward);
                for (byte i = 1; i<=4; i++) moveList.add(new PromotionMove((byte)(pos-cForward),pos,(byte)(type+i)));
                promoteForward&=promoteForward-1;
            }
        }
        while (captureLeft!=0) {
            byte pos = bsf(captureLeft);
            moveList.add(new CaptureMove((byte)(pos-cLeft),pos,type,board[pos],this));
            captureLeft&=captureLeft-1;
        }
        while (captureRight!=0) {
            byte pos = bsf(captureRight);
            moveList.add(new CaptureMove((byte)(pos-cRight),pos,type,board[pos],this));
            captureRight&=captureRight-1;
        }
        while (promoteCaptureLeft!=0) {
            byte pos = bsf(promoteCaptureLeft);
            for (byte i = 1; i<=4; i++) moveList.add(new PromotionCaptureMove((byte)(pos-cLeft),pos,(byte)(type+i),board[pos]));
            promoteCaptureLeft&=promoteCaptureLeft-1;
        }
        while (promoteCaptureRight!=0) {
            byte pos = bsf(promoteCaptureRight);
            for (byte i = 1; i<=4; i++) moveList.add(new PromotionCaptureMove((byte)(pos-cRight),pos,(byte)(type+i),board[pos]));
            promoteCaptureRight&=promoteCaptureRight-1;
        }
    }
    private void addKingMovesExperimental(ArrayList<Move> moveList, byte coords, boolean captureOnly) {
        int side = whiteToMove?0:1;
        byte type = (byte)((side<<3)+8);
        long captureMoves = (PreCompute.kingNeighborMask[coords]&mask[1-side][6])&(~occupancy[1-side][6]);
        if (!captureOnly) {
            long regMoves = (PreCompute.kingNeighborMask[coords]&(~pieceMask))&(~occupancy[1-side][6]);
            while (regMoves!=0) {
                byte pos = bsf(regMoves);
                moveList.add(new Move(coords,pos,type,this));
                regMoves&=regMoves-1;
            }
        }
        while (captureMoves!=0) {
            byte pos = bsf(captureMoves);
            moveList.add(new CaptureMove(coords,pos,type,board[pos],this));
            captureMoves&=captureMoves-1;
        }
    }
    private void addCastlingMoves(ArrayList<Move> moveList) {
        if (whiteToMove) {
            if ((canCastle&1)!=0 && board[5]==0 && board[6]==0 && !squareControlled((byte)4,false) && !squareControlled((byte)5,false) && !squareControlled((byte)6,false)) moveList.add(new CastleMove((byte)0,this));
            if ((canCastle&2)!=0 && board[3]==0 && board[2]==0 && board[1]==0 && !squareControlled((byte)4,false) && !squareControlled((byte)3,false) && !squareControlled((byte)2,false)) moveList.add(new CastleMove((byte)1,this));
        } else {
            if ((canCastle&4)!=0 && board[61]==0 && board[62]==0 && !squareControlled((byte)60,true) && !squareControlled((byte)61,true) && !squareControlled((byte)62,true)) moveList.add(new CastleMove((byte)2,this));
            if ((canCastle&8)!=0 && board[59]==0 && board[58]==0 && board[57]==0 && !squareControlled((byte)60,true) && !squareControlled((byte)59,true) && !squareControlled((byte)58,true)) moveList.add(new CastleMove((byte)3,this));
        }
    }
    private void addEnPassantMoves(ArrayList<Move> moveList, byte coords) {
        if (validEnPassant==9) return;
        byte pieceType = (byte)((whiteToMove?9:17));
        byte rank = (byte)((coords>>3)+1);
        byte file = (byte)(coords&7);
        if ((rank==5 && whiteToMove) || (rank==4 && !whiteToMove)) {
            if (Math.abs(file-validEnPassant)==1) {
                byte captureSquare = (byte)(validEnPassant+(whiteToMove?32:24));
                byte newSquare = (byte)(captureSquare+(whiteToMove?8:-8));
                moveList.add(new EnPassantMove(coords,captureSquare,newSquare,pieceType,this));
            }
        }
    }
    public ArrayList<Move> getMoveList() {
        long t = System.currentTimeMillis();
        elapsed3++;
        ArrayList<Move> res = new ArrayList<>();
        int side = whiteToMove?0:1;
        addKingMovesExperimental(res,kingCoords[side],false);
        addCastlingMoves(res);
        addPawnMovesExperimental(res,whiteToMove,false);
        if (validEnPassant!=9) {
            long m = mask[side][1];
            while (m!=0) {
                addEnPassantMoves(res,bsf(m));
                m&=m-1;
            }
        }
        long m = mask[side][2];
        while (m!=0) {
            addKnightMovesExperimental(res,bsf(m),false);
            m&=m-1;
        }
        m = mask[side][3];
        while (m!=0) {
            addBishopMovesExperimental(res,bsf(m),false);
            m&=m-1;
        }
        m = mask[side][4];
        while (m!=0) {
            addRookMovesExperimental(res,bsf(m),false);
            m&=m-1;
        }
        m = mask[side][5];
        while (m!=0) {
            byte pos = bsf(m);
            addBishopMovesExperimental(res,pos,false);
            addRookMovesExperimental(res,pos,false);
            m&=m-1;
        }
        elapsed+=System.currentTimeMillis()-t;
        return res;
    }
    public ArrayList<Move> getLegalMoveList() {
        ArrayList<Move> res = new ArrayList<>();
        int side = whiteToMove?0:1;
        for (Move m : getMoveList()) {
            m.makeMove(this);
            if (!squareControlled(kingCoords[side],whiteToMove)) res.add(m);
            m.undoMove(this);
        }
        return res;
    }
    public ArrayList<Move> getCaptureMoveList() {
        ArrayList<Move> res = new ArrayList<>();
        int side = whiteToMove?0:1;
        addKingMovesExperimental(res,kingCoords[side],true);
        addPawnMovesExperimental(res,whiteToMove,true);
        if (validEnPassant!=9) {
            long m = mask[side][1];
            while (m!=0) {
                addEnPassantMoves(res,bsf(m));
                m&=m-1;
            }
        }
        long m=mask[side][2];
        while (m!=0) {
            addKnightMovesExperimental(res,bsf(m),true);
            m&=m-1;
        }
        m=mask[side][3];
        while (m!=0) {
            addBishopMovesExperimental(res,bsf(m),true);
            m&=m-1;
        }
        m=mask[side][4];
        while (m!=0) {
            addRookMovesExperimental(res,bsf(m),true);
            m&=m-1;
        }
        m=mask[side][5];
        while (m!=0) {
            byte pos = bsf(m);
            addBishopMovesExperimental(res,pos,true);
            addRookMovesExperimental(res,pos,true);
            m&=m-1;
        }
        return res;
    }
    public ArrayList<Move> getCheckMoveList() {
        ArrayList<Move> res = new ArrayList<>();
        int side = whiteToMove?0:1;
        // Knight
        byte pieceType = (byte)(8*side+10);
        long kingKnightMask = knightMask(kingCoords[1-side]);
        long tempMask = mask[side][2];
        while (tempMask!=0) {
            byte startSquare = bsf(tempMask);
            long curKnightMask = knightMask(startSquare)&kingKnightMask;
            while (curKnightMask!=0) {
                byte endSquare = bsf(curKnightMask);
                if (board[endSquare]==0) {
                    res.add(new Move(startSquare,endSquare,pieceType,this));
                } else if (board[endSquare]>>3!=side+1) {
                    res.add(new CaptureMove(startSquare,endSquare,pieceType,board[endSquare],this));
                }
                curKnightMask&=curKnightMask-1;
            }
            tempMask&=tempMask-1;
        }
        // Bishop
        pieceType = (byte)(8*side+11);
        long kingBishopMask = bishopMask(kingCoords[1-side]);
        tempMask = mask[side][3];
        while (tempMask!=0) {
            byte startSquare = bsf(tempMask);
            long curBishopMask = bishopMask(startSquare)&kingBishopMask;
            while (curBishopMask!=0) {
                byte endSquare = bsf(curBishopMask);
                if (board[endSquare]==0) {
                    res.add(new Move(startSquare,endSquare,pieceType,this));
                } else if (board[endSquare]>>3!=side+1) {
                    res.add(new CaptureMove(startSquare,endSquare,pieceType,board[endSquare],this));
                }
                curBishopMask&=curBishopMask-1;
            }
            tempMask&=tempMask-1;
        }
        // Rook
        pieceType = (byte)(8*side+12);
        long kingRookMask = rookMask(kingCoords[1-side]);
        tempMask = mask[side][4];
        while (tempMask!=0) {
            byte startSquare = bsf(tempMask);
            long curRookMask = rookMask(startSquare)&kingRookMask;
            while (curRookMask!=0) {
                byte endSquare = bsf(curRookMask);
                if (board[endSquare]==0) {
                    res.add(new Move(startSquare,endSquare,pieceType,this));
                } else if (board[endSquare]>>3!=side+1) {
                    res.add(new CaptureMove(startSquare,endSquare,pieceType,board[endSquare],this));
                }
                curRookMask&=curRookMask-1;
            }
            tempMask&=tempMask-1;
        }
        // Queen
        pieceType = (byte)(8*side+13);
        long kingQueenMask = queenMask(kingCoords[1-side]);
        tempMask = mask[side][5];
        while (tempMask!=0) {
            byte startSquare = bsf(tempMask);
            long curQueenMask = queenMask(startSquare)&kingQueenMask;
            while (curQueenMask!=0) {
                byte endSquare = bsf(curQueenMask);
                if (board[endSquare]==0) {
                    res.add(new Move(startSquare,endSquare,pieceType,this));
                } else if (board[endSquare]>>3!=side+1) {
                    res.add(new CaptureMove(startSquare,endSquare,pieceType,board[endSquare],this));
                }
                curQueenMask&=curQueenMask-1;
            }
            tempMask&=tempMask-1;
        }
        return res;
    }
    public int score(boolean verbose) {
        //numTimes++;
        if (whiteToMove && squareControlled(kingCoords[1],true)) return Integer.MIN_VALUE;
        if (!whiteToMove && squareControlled(kingCoords[0],false)) return Integer.MIN_VALUE;
        double whiteWeight = endgameWeight(true);
        double blackWeight = endgameWeight(false);
        int score = 0;
        score+=pieceSquareBonus(whiteWeight,blackWeight,verbose);
        int whiteEG = endgameScore(whiteWeight,true,score);
        int blackEG = endgameScore(blackWeight,false,-score);
        if (verbose) System.out.println("Endgame: "+whiteEG+" "+blackEG+" "+(whiteEG-blackEG));
        score+=whiteEG-blackEG;
        score+=material(verbose);
        score+=pawnStructure(verbose);
        score+=kingSafety(whiteWeight,blackWeight,verbose);
        score+=space(verbose);
        score+=mobility(whiteWeight,blackWeight,verbose);
        score+=passedPawns(whiteWeight,blackWeight,verbose);
        if (!whiteToMove) return -score;
        return score;
    }
    public double endgameWeight(boolean white) {
        short score;
        int side = white?0:1;
        score=(short)(numPieces[side][2]*3+numPieces[side][3]*3+numPieces[side][4]*5+numPieces[side][5]*9);
        return Math.max(0,1-(double)score/24);
    }
    public int pieceSquareBonus(double whiteEGWeight, double blackEGWeight, boolean verbose) {
        int whiteScore=(int)(whiteEndBonus*whiteEGWeight+whiteStartBonus*(1-whiteEGWeight));
        int blackScore=(int)(blackEndBonus*blackEGWeight+blackStartBonus*(1-blackEGWeight));
        if (verbose) System.out.println("Position: "+whiteScore+" "+blackScore+" "+(whiteScore-blackScore));
        return whiteScore-blackScore;
    }
    public int material(boolean verbose) {
        int whiteMaterial = numPieces[0][3]>=2?50:0;
        int blackMaterial = numPieces[1][3]>=2?50:0;
        whiteMaterial+=(numPieces[0][1]-5)*numPieces[0][2]*100/16;
        whiteMaterial-=(numPieces[0][1]-5)*numPieces[0][4]*100/8;
        blackMaterial+=(numPieces[1][1]-5)*numPieces[1][2]*100/16;
        blackMaterial-=(numPieces[1][1]-5)*numPieces[1][4]*100/8;
        for (int piece = 0; piece<6; piece++) {
            whiteMaterial+=numPieces[0][piece]*PreCompute.pieceValues[piece];
            blackMaterial+=numPieces[1][piece]*PreCompute.pieceValues[piece];
        }
        if (verbose) System.out.println("Material: "+whiteMaterial+" "+blackMaterial+" "+(whiteMaterial-blackMaterial));
        return whiteMaterial-blackMaterial;
    }
    public int endgameScore(double weight, boolean white, int scoreDiff) {
        if (scoreDiff<200) return 0;
        byte opponentRank,opponentFile,friendlyRank,friendlyFile;
        int side = white?0:1;
        opponentRank = (byte)(kingCoords[1-side]>>3);
        opponentFile = (byte)(kingCoords[1-side]&7);
        friendlyRank = (byte)(kingCoords[side]>>3);
        friendlyFile = (byte)(kingCoords[side]&7);
        return (int)weight*(10*(Math.max(3-opponentRank,opponentRank-4)+Math.max(3-opponentFile,opponentFile-4))+4*(Math.abs(opponentRank-friendlyRank)+Math.abs(opponentFile-friendlyFile)));
    }
    public int pawnStructure(boolean verbose) {
        int whiteStructure = 0;
        int blackStructure = 0;
        long m = mask[0][1];
        long wFrontSpan = northFill(mask[0][1]);
        while (m!=0) {
            byte pos = bsf(m);
            byte file = (byte)(pos&7);
            if (Long.bitCount(mask[0][1]&PreCompute.fileMask[file])>1) whiteStructure-=11;
            if (file==0) {
                if ((mask[0][1]&PreCompute.fileMask[1])==0 || ((mask[0][1]&((1L<<pos)>>7))==0 && (mask[0][1]&((1L<<pos)<<1))==0)) whiteStructure-=5;
                else whiteStructure+=10;
                if ((wFrontSpan&1L<<(pos+1))==0 && (occupancy[1][1]&1L<<pos)!=0) whiteStructure-=9;
            } else if (file==7) {
                if ((mask[0][1]&PreCompute.fileMask[6])==0 || ((mask[0][1]&(1L<<pos>>9))==0 && (mask[0][1]&(1L<<pos>>1))==0)) whiteStructure-=5;
                else whiteStructure+=10;
                if ((wFrontSpan&1L<<(pos-1))==0 && (occupancy[1][1]&1L<<pos)!=0) whiteStructure-=9;
            } else {
                if (((mask[0][1]&PreCompute.fileMask[file-1])==0 && (mask[0][1]&PreCompute.fileMask[file+1])==0) || ((mask[0][1]&(1L<<pos>>7))==0 && (mask[0][1]&(1L<<pos>>9))==0 && (mask[0][1]&(1L<<pos<<1))==0 && (mask[0][1]&(1L<<pos>>1))==0)) whiteStructure-=5;
                else whiteStructure+=10;
                if ((wFrontSpan&1L<<(pos-1))==0 && (wFrontSpan&1L<<(pos+1))==0 && (occupancy[1][1]&1L<<pos)!=0) whiteStructure-=9;
            }
            m&=m-1;
        }
        m = mask[1][1];
        long bFrontSpan = southFill(mask[1][1]);
        while (m!=0) {
            byte pos = bsf(m);
            byte file = (byte)(pos&7);
            if (Long.bitCount(mask[1][1]&PreCompute.fileMask[file])>1) blackStructure-=11;
            if (file==0) {
                if ((mask[1][1]&PreCompute.fileMask[1])==0 || ((mask[1][1]&(1L<<pos<<9))==0 && (mask[1][1]&(1L<<pos<<1))==0)) blackStructure-=5;
                else blackStructure+=10;
                if ((bFrontSpan&1L<<(pos+1))==0 && (occupancy[1][1]&1L<<pos)!=0) blackStructure-=9;
            }
            else if (file==7) {
                if ((mask[1][1]&PreCompute.fileMask[6])==0 || ((mask[1][1]&(1L<<pos<<7))==0 && (mask[1][1]&(1L<<pos>>1))==0)) blackStructure-=5;
                else blackStructure+=10;
                if ((bFrontSpan&1L<<(pos-1))==0 && (occupancy[1][1]&1L<<pos)!=0) blackStructure-=9;
            } else {
                if (((mask[1][1]&PreCompute.fileMask[file-1])==0 && (mask[1][1]&PreCompute.fileMask[file+1])==0) || ((mask[1][1]&(1L<<pos<<7))==0 && (mask[1][1]&(1L<<pos<<9))==0 && (mask[1][1]&(1L<<pos<<1))==0 && (mask[1][1]&(1L<<pos>>1))==0)) blackStructure-=5;
                else blackStructure+=10;
                if ((bFrontSpan&1L<<(pos-1))==0 && (bFrontSpan&1L<<(pos+1))==0 && (occupancy[1][1]&1L<<pos)!=0) blackStructure-=9;
            }
            m&=m-1;
        }
        if (verbose) System.out.println("Pawns: "+whiteStructure+" "+blackStructure+" "+(whiteStructure-blackStructure));
        return whiteStructure-blackStructure;
    }
    public int kingSafety(double whiteEGWeight, double blackEGWeight, boolean verbose) {
        int whiteSafety = 0;
        int blackSafety = 0;
        if ((PreCompute.whiteKingsideKing&1L<<kingCoords[0])!=0) {
            whiteSafety=10*Math.min(3,Long.bitCount(mask[0][1]&PreCompute.whiteKingsidePawn));
        } else if ((PreCompute.whiteQueensideKing&1L<<kingCoords[0])!=0) {
            whiteSafety=10*Math.min(3,Long.bitCount(mask[0][1]&PreCompute.whiteQueensidePawn));
        }
        byte file = (byte)(kingCoords[0]&7);
        if (file!=0 && (mask[0][1]&PreCompute.fileMask[file-1])==0) {
            whiteSafety-=20;
            if ((mask[1][1]&PreCompute.fileMask[file-1])==0) whiteSafety-=20;
        }
        if ((mask[0][1]&PreCompute.fileMask[file])==0) {
            whiteSafety-=20;
            if ((mask[1][1]&PreCompute.fileMask[file])==0) whiteSafety-=20;
        }
        if (file!=7 && (mask[0][1]&PreCompute.fileMask[file+1])==0) {
            whiteSafety-=20;
            if ((mask[1][1]&PreCompute.fileMask[file+1])==0) whiteSafety-=20;
        }
        long tempMask = mask[1][2];
        while (tempMask!=0) {
            if ((knightMask(bsf(tempMask))&kingMask(kingCoords[0]))!=0) whiteSafety-=32;
            tempMask&=tempMask-1;
        }
        tempMask = mask[1][3];
        while (tempMask!=0) {
            if ((bishopMask(bsf(tempMask))&kingMask(kingCoords[0]))!=0) whiteSafety-=20;
            tempMask&=tempMask-1;
        }
        tempMask = mask[1][4];
        while (tempMask!=0) {
            if ((rookMask(bsf(tempMask))&kingMask(kingCoords[0]))!=0) whiteSafety-=17;
            tempMask&=tempMask-1;
        }
        tempMask = mask[1][5];
        while (tempMask!=0) {
            if ((queenMask(bsf(tempMask))&kingMask(kingCoords[0]))!=0) whiteSafety-=4;
            tempMask&=tempMask-1;
        }

        if ((PreCompute.blackKingsideKing&1L<<kingCoords[1])!=0) {
            blackSafety=10*Math.min(3,Long.bitCount(mask[1][1]&PreCompute.blackKingsidePawn));
        } else if ((PreCompute.blackQueensideKing&1L<<kingCoords[1])!=0) {
            blackSafety=10*Math.min(3,Long.bitCount(mask[1][1]&PreCompute.blackQueensidePawn));
        }
        file = (byte)(kingCoords[1]&7);
        if (file!=0 && (mask[1][1]&PreCompute.fileMask[file-1])==0) {
            blackSafety-=20;
            if ((mask[0][1]&PreCompute.fileMask[file-1])==0) blackSafety-=20;
        }
        if ((mask[1][1]&PreCompute.fileMask[file])==0) {
            blackSafety-=20;
            if ((mask[0][1]&PreCompute.fileMask[file])==0) blackSafety-=20;
        }
        if (file!=7 && (mask[1][1]&PreCompute.fileMask[file+1])==0) {
            blackSafety-=20;
            if ((mask[0][1]&PreCompute.fileMask[file+1])==0) blackSafety-=20;
        }
        tempMask = mask[0][2];
        while (tempMask!=0) {
            if ((knightMask(bsf(tempMask))&kingMask(kingCoords[1]))!=0) blackSafety-=32;
            tempMask&=tempMask-1;
        }
        tempMask = mask[0][3];
        while (tempMask!=0) {
            if ((bishopMask(bsf(tempMask))&kingMask(kingCoords[1]))!=0) blackSafety-=20;
            tempMask&=tempMask-1;
        }
        tempMask = mask[0][4];
        while (tempMask!=0) {
            if ((rookMask(bsf(tempMask))&kingMask(kingCoords[1]))!=0) blackSafety-=17;
            tempMask&=tempMask-1;
        }
        tempMask = mask[0][5];
        while (tempMask!=0) {
            if ((queenMask(bsf(tempMask))&kingMask(kingCoords[1]))!=0) blackSafety-=4;
            tempMask&=tempMask-1;
        }
        whiteSafety*=(1-blackEGWeight);
        blackSafety*=(1-whiteEGWeight);
        if (verbose) {
            System.out.println("Safety: "+whiteSafety+" "+blackSafety+" "+(whiteSafety-blackSafety));
        }
        return whiteSafety-blackSafety;
    }
    public int mobility(double whiteEGWeight, double blackEGWeight, boolean verbose) {

        double whiteMobility = 0;
        // Move to PreCompute
        long m = mask[0][2];
        while (m!=0) {
            int rawMobility = Long.bitCount(knightMask(bsf(m)));
            whiteMobility+=PreCompute.mobilitymg[0][rawMobility]*(1-whiteEGWeight)+PreCompute.mobilityeg[0][rawMobility]*whiteEGWeight;
            m&=m-1;
        }
        m = mask[0][3];
        while (m!=0) {
            int rawMobility = Long.bitCount(bishopMask(bsf(m)));
            whiteMobility+=PreCompute.mobilitymg[1][rawMobility]*(1-whiteEGWeight)+PreCompute.mobilityeg[1][rawMobility]*whiteEGWeight;
            m&=m-1;
        }
        m = mask[0][4];
        while (m!=0) {
            int rawMobility = Long.bitCount(rookMask(bsf(m)));
            whiteMobility+=PreCompute.mobilitymg[2][rawMobility]*(1-whiteEGWeight)+PreCompute.mobilityeg[2][rawMobility]*whiteEGWeight;
            m&=m-1;
        }
        m = mask[0][5];
        while (m!=0) {
            int rawMobility = Long.bitCount(queenMask(bsf(m)));
            whiteMobility+=PreCompute.mobilitymg[3][rawMobility]*(1-whiteEGWeight)+PreCompute.mobilityeg[3][rawMobility]*whiteEGWeight;
            m&=m-1;
        }
        double blackMobility = 0;
        m = mask[1][2];
        while (m!=0) {
            int rawMobility = Long.bitCount(knightMask(bsf(m)));
            blackMobility+=PreCompute.mobilitymg[0][rawMobility]*(1-blackEGWeight)+PreCompute.mobilityeg[0][rawMobility]*blackEGWeight;
            m&=m-1;
        }
        m = mask[1][3];
        while (m!=0) {
            int rawMobility = Long.bitCount(bishopMask(bsf(m)));
            blackMobility+=PreCompute.mobilitymg[1][rawMobility]*(1-blackEGWeight)+PreCompute.mobilityeg[1][rawMobility]*blackEGWeight;
            m&=m-1;
        }
        m = mask[1][4];
        while (m!=0) {
            int rawMobility = Long.bitCount(rookMask(bsf(m)));
            blackMobility+=PreCompute.mobilitymg[2][rawMobility]*(1-blackEGWeight)+PreCompute.mobilityeg[2][rawMobility]*blackEGWeight;
            m&=m-1;
        }
        m = mask[1][5];
        while (m!=0) {
            int rawMobility = Long.bitCount(queenMask(bsf(m)));
            blackMobility+=PreCompute.mobilitymg[3][rawMobility]*(1-blackEGWeight)+PreCompute.mobilityeg[3][rawMobility]*blackEGWeight;
            m&=m-1;
        }
        if (verbose) System.out.println("Mobility: "+(int)whiteMobility+" "+(int)blackMobility+" "+(int)(whiteMobility-blackMobility));
        return (int)(whiteMobility-blackMobility);
    }
    public int space(boolean verbose) {
        long whiteSpaceMask = (occupancy[0][6]&PreCompute.whiteSide&~occupancy[1][1]);
        long blackSpaceMask = (occupancy[1][6]&PreCompute.blackSide&~occupancy[0][1]);
        int whiteSpace = (Long.bitCount(whiteSpaceMask)+Long.bitCount(whiteSpaceMask&southFill(mask[0][1])))*(numPieces[0][2]+numPieces[0][3]+numPieces[0][4]+numPieces[0][5]);
        int blackSpace = (Long.bitCount(blackSpaceMask)+Long.bitCount(blackSpaceMask&northFill(mask[1][1])))*(numPieces[1][2]+numPieces[1][3]+numPieces[1][4]+numPieces[1][5]);
        if (verbose) System.out.println("Space: "+whiteSpace+" "+blackSpace+" "+(whiteSpace-blackSpace));
        return whiteSpace-blackSpace;
    }
    public int passedPawns(double whiteEGWeight, double blackEGWeight, boolean verbose) {
        int whitePassed = 0;
        int blackPassed = 0;
        long whitePassedBounds = northFill(mask[0][1]);
        long blackPassedBounds = southFill(mask[1][1]);
        whitePassedBounds|=((whitePassedBounds&~PreCompute.fileMask[0])<<7)|((whitePassedBounds&~PreCompute.fileMask[7])<<9);
        blackPassedBounds|=((blackPassedBounds&~PreCompute.fileMask[0])>>9)|((blackPassedBounds&~PreCompute.fileMask[7])>>7);
        long whitePassedPawns = mask[0][1]&~blackPassedBounds;
        long blackPassedPawns = mask[1][1]&~whitePassedBounds;
        for (byte i = 0; i<8; i++) {
            int whiteNum = Long.bitCount(whitePassedPawns&PreCompute.rankMask[i]);
            whitePassed+=PreCompute.passedValuesMG[i]*whiteNum*(1-whiteEGWeight)+PreCompute.passedValuesEG[i]*whiteNum*whiteEGWeight;
            int blackNum = Long.bitCount(blackPassedPawns&PreCompute.rankMask[i]);
            blackPassed+=PreCompute.passedValuesMG[7-i]*blackNum*(1-blackEGWeight)+PreCompute.passedValuesEG[7-i]*blackNum*blackEGWeight;
        }
        if (verbose) System.out.println("Passed: "+whitePassed+" "+blackPassed+" "+(whitePassed-blackPassed));
        return whitePassed-blackPassed;
    }
    public long getKey() {
        return key;
    }
    public long generateKey() {
        key=0;
        for (int side = 0; side<2; side++) {
            for (int piece = 0; piece<6; piece++) {
                long m = mask[side][piece];
                while (m!=0) {
                    key^=PreCompute.pieceHash[bsf(m)][piece+6*side];
                    m&=m-1;
                }
            }
        }
        for (int i = 0; i<4; i++) {
            if ((canCastle&(1<<i))!=0) key^=PreCompute.castleHash[i];
        }
        if (validEnPassant!=9) key^=PreCompute.enPassantHash[validEnPassant];
        return key;
    }

    public boolean squareControlled(byte square, boolean checkWhite) {
        return (occupancy[checkWhite?0:1][6]&(1L<<square))!=0;
    }

    public byte bsf(long bitboard) {
        return (byte)(bitboard==0?-1:Long.numberOfTrailingZeros(bitboard));
    }
    public byte bsr(long bitboard) {
        return (byte)(bitboard==0?-1:Long.numberOfLeadingZeros(bitboard)^63);
    }
    public long northFill(long bitboard) {
        bitboard|=bitboard<<8;
        bitboard|=bitboard<<16;
        bitboard|=bitboard<<32;
        return bitboard;
    }
    public long southFill(long bitboard) {
        bitboard|=bitboard>>8;
        bitboard|=bitboard>>16;
        bitboard|=bitboard>>32;
        return bitboard;
    }
    public int SEE(byte startSquare, byte endSquare) {
        byte pieceType = board[startSquare];
        byte capturePieceType = board[endSquare];
        int side = pieceType<16?0:1;
        long oldPieceMask = pieceMask;
        long[][] pieceAttackMask = new long[2][6];
        pieceMask^=1L<<startSquare;
        if ((pieceType&7)==1 && capturePieceType==0) {
            byte captureSquare = (byte)(((startSquare>>3)<<3)+(endSquare&7));
            capturePieceType = board[captureSquare];
            if ((capturePieceType&7)==1) {
                pieceMask^=1L<<captureSquare;
                pieceMask^=1L<<endSquare;
            }
        }
        long b = knightMask(endSquare)&(pieceMask&(mask[0][2]|mask[1][2]));
        while (b!=0) {
            byte pos = bsf(b);
            pieceAttackMask[(board[pos]>>3)-1][board[pos]&7]|=1L<<pos;
            b&=b-1;
        }
        b = bishopMask(endSquare)&(pieceMask&(mask[0][3]|mask[1][3]|mask[0][5]|mask[1][5]));
        while (b!=0) {
            byte pos = bsf(b);
            pieceAttackMask[(board[pos]>>3)-1][board[pos]&7]|=1L<<pos;
            b&=b-1;
        }
        b = rookMask(endSquare)&(pieceMask&(mask[0][4]|mask[1][4]|mask[0][5]|mask[1][5]));
        while (b!=0) {
            byte pos = bsf(b);
            pieceAttackMask[(board[pos]>>3)-1][board[pos]&7]|=1L<<pos;
            b&=b-1;
        }
        b = pawnMask(endSquare,true)&(pieceMask&(mask[1][1]));
        while (b!=0) {
            byte pos = bsf(b);
            pieceAttackMask[(board[pos]>>3)-1][board[pos]&7]|=1L<<pos;
            b&=b-1;
        }
        b = pawnMask(endSquare,false)&(pieceMask&(mask[0][1]));
        while (b!=0) {
            byte pos = bsf(b);
            pieceAttackMask[(board[pos]>>3)-1][board[pos]&7]|=1L<<pos;
            b&=b-1;
        }
        b=kingMask(endSquare)&(pieceMask&(mask[0][0]|mask[1][0]));
        while (b!=0) {
            byte pos = bsf(b);
            pieceAttackMask[(board[pos]>>3)-1][board[pos]&7]|=1L<<pos;
            b&=b-1;
        }
        short[] material = new short[15];
        material[0] = PreCompute.pieceValues[capturePieceType&7];
        material[1]=(short)(-material[0]+PreCompute.pieceValues[pieceType&7]);
        int iteration = 2;
        side=1-side;
        while (true) {
            byte pos = -1;
            byte piece = -1;
            for (byte p = 1; p<=6; p++) {
                if (p==6) p=0;
                if (pieceAttackMask[side][p]!=0) {
                    pos =bsf(pieceAttackMask[side][p]);
                    piece = p;
                    break;
                }
                if (p==0) p=6;
            }
            if (piece==-1) break;
            pieceAttackMask[side][piece&7]^=1L<<pos;
            material[iteration++]=(short)(-material[iteration-2]+PreCompute.pieceValues[piece]);
            switch(piece) {
                case 1:
                    pieceMask^=1L<<pos;
                    byte newPiece = bsf(rayMask(endSquare,PreCompute.deltaToIdx[pos-endSquare+9])&pieceMask);
                    if (newPiece==-1) {
                        side=1-side;
                        continue;
                    }
                    byte newPieceType=(byte)(board[newPiece]&7);
                    if (newPieceType==3 || newPieceType==5) {
                        pieceAttackMask[side][newPieceType]|=1L<<newPiece;
                    }
                    // Pawn
                    break;
                case 3:
                    byte dir = -1;
                    for (byte i = 4; i<8; i++) {
                        if ((rayMask(endSquare,i)&(1L<<pos))!=0) {
                            dir=i;
                            break;
                        }
                    }
                    pieceMask^=1L<<pos;
                    newPiece = bsf(rayMask(endSquare,dir)&pieceMask);
                    if (newPiece==-1) {
                        side=1-side;
                        continue;
                    }
                    newPieceType=(byte)(board[newPiece]&7);
                    if (newPieceType==3 || newPieceType==5) {
                        pieceAttackMask[side][newPieceType]|=1L<<newPiece;
                    }
                    // Bishop
                    break;
                case 4:
                    dir = -1;
                    for (byte i = 0; i<4; i++) {
                        if ((rayMask(endSquare,i)&(1L<<pos))!=0) {
                            dir=i;
                            break;
                        }
                    }
                    pieceMask^=1L<<pos;
                    newPiece = bsf(rayMask(endSquare,dir)&pieceMask);
                    if (newPiece==-1) {
                        side=1-side;
                        continue;
                    }
                    newPieceType=(byte)(board[newPiece]&7);
                    if (newPieceType==4 || newPieceType==5) {
                        pieceAttackMask[side][newPieceType]|=1L<<newPiece;
                    }
                    // Rook
                    break;
                case 5:
                    dir = -1;
                    for (byte i = 0; i<8; i++) {
                        if ((rayMask(endSquare,i)&(1L<<pos))!=0) {
                            dir=i;
                            break;
                        }
                    }
                    pieceMask^=1L<<pos;
                    newPiece = bsf(rayMask(endSquare,dir)&pieceMask);
                    if (newPiece==-1) {
                        side=1-side;
                        continue;
                    }
                    newPieceType=(byte)(board[newPiece]&7);
                    if (dir<4) {
                        if ((newPieceType)==4 || (newPieceType)==5) {
                            pieceAttackMask[side][newPieceType]|=1L<<newPiece;
                        }
                    } else {
                        if ((newPieceType)==3 || (newPieceType)==5) {
                            pieceAttackMask[side][newPieceType]|=1L<<newPiece;
                        }
                    }
                    // Queen
                    break;
            }
            side=1-side;
        }
        maxGuarding=Math.max(maxGuarding,iteration-2);
        for (int i = iteration-3; i>=0; i--) {
            material[i]=(short)(-Math.max(-material[i],material[i+1]));
        }
        pieceMask=oldPieceMask;
        return (int)material[0];
    }

    public void updateOccupancy() {
        occupancy = new long[2][7];
        occupancy[0][0]=kingMask(kingCoords[0]);
        occupancy[0][1]=totalPawnMask(true);
        long m = mask[0][2];
        while (m!=0) {
            occupancy[0][2]|=knightMask(bsf(m));
            m&=m-1;
        }
        m = mask[0][3];
        while (m!=0) {
            occupancy[0][3]|=bishopMask(bsf(m));
            m&=m-1;
        }
        m = mask[0][4];
        while (m!=0) {
            occupancy[0][4]|=rookMask(bsf(m));
            m&=m-1;
        }
        m = mask[0][5];
        while (m!=0) {
            occupancy[0][5]|=queenMask(bsf(m));
            m&=m-1;
        }
        occupancy[1][0]=kingMask(kingCoords[1]);
        occupancy[1][1]=totalPawnMask(false);
        m = mask[1][2];
        while (m!=0) {
            occupancy[1][2]|=knightMask(bsf(m));
            m&=m-1;
        }
        m = mask[1][3];
        while (m!=0) {
            occupancy[1][3]|=bishopMask(bsf(m));
            m&=m-1;
        }
        m = mask[1][4];
        while (m!=0) {
            occupancy[1][4]|=rookMask(bsf(m));
            m&=m-1;
        }
        m = mask[1][5];
        while (m!=0) {
            occupancy[1][5]|=queenMask(bsf(m));
            m&=m-1;
        }
        occupancy[0][6]=occupancy[0][0]|occupancy[0][1]|occupancy[0][2]|occupancy[0][3]|occupancy[0][4]|occupancy[0][5];
        occupancy[1][6]=occupancy[1][0]|occupancy[1][1]|occupancy[1][2]|occupancy[1][3]|occupancy[1][4]|occupancy[1][5];
    }
    public byte makeNullMove() {
        whiteToMove=!whiteToMove;
        byte oldEnPassant = validEnPassant;
        key^=PreCompute.turnHash^PreCompute.enPassantHash[oldEnPassant]^PreCompute.enPassantHash[9];
        validEnPassant=9;
        numMoves++;
        return oldEnPassant;
    }
    public void undoNullMove(byte oldEnPassant) {
        whiteToMove=!whiteToMove;
        key^=PreCompute.turnHash^PreCompute.enPassantHash[oldEnPassant]^PreCompute.enPassantHash[9];
        validEnPassant=oldEnPassant;
        numMoves--;
    }
}
