package Chess;

public class CaptureMove extends Move {
    private byte startSquare;
    private byte endSquare;
    //public int preScore;
    private byte pieceType;
    private byte capturePieceType;
    private long oldKey;
    private long oldMask;
    private long oldPieceMask;
    private byte oldEnPassant;
    private byte oldCastle;
    private byte indexOfCapturedPiece;
    public byte indexOfPiece;
    public CaptureMove(byte start, byte end, byte type, byte captureType, Board board) {
        startSquare=start;
        endSquare=end;
		/*
        preScore=1000*PreCompute.pieceValues[captureType&7];
		if (board.squareControlled(endSquare,type>16)) {
			preScore-=1000*PreCompute.pieceValues[type&7];
		}
		*/
        preScore=board.SEE(start,end)*1000;
        //preScore+=50000;
        pieceType=type;
        capturePieceType=captureType;
        if (type<16) {
            whitePieceStartChange=(Main.player.start[pieceType][endSquare]- Main.player.start[pieceType][startSquare]);
            whitePieceEndChange=(Main.player.end[pieceType][endSquare]- Main.player.end[pieceType][startSquare]);
            blackPieceStartChange=(-Main.player.start[capturePieceType][endSquare]);
            blackPieceEndChange=(-Main.player.end[capturePieceType][endSquare]);
        } else {
            blackPieceStartChange=(Main.player.start[pieceType][endSquare]- Main.player.start[pieceType][startSquare]);
            blackPieceEndChange=(Main.player.end[pieceType][endSquare]- Main.player.end[pieceType][startSquare]);
            whitePieceStartChange=(-Main.player.start[capturePieceType][endSquare]);
            whitePieceEndChange=(-Main.player.end[capturePieceType][endSquare]);
        }
    }
    public CaptureMove(byte start, byte end, byte type, byte captureType, Board board, int preScore) {
        startSquare=start;
        endSquare=end;
        this.preScore=preScore*1000;
        pieceType=type;
        capturePieceType=captureType;
        if (type<16) {
            whitePieceStartChange=(Main.player.start[pieceType][endSquare]- Main.player.start[pieceType][startSquare]);
            whitePieceEndChange=(Main.player.end[pieceType][endSquare]- Main.player.end[pieceType][startSquare]);
            blackPieceStartChange=(-Main.player.start[capturePieceType][endSquare]);
            blackPieceEndChange=(-Main.player.end[capturePieceType][endSquare]);
        } else {
            blackPieceStartChange=(Main.player.start[pieceType][endSquare]- Main.player.start[pieceType][startSquare]);
            blackPieceEndChange=(Main.player.end[pieceType][endSquare]- Main.player.end[pieceType][startSquare]);
            whitePieceStartChange=(-Main.player.start[capturePieceType][endSquare]);
            whitePieceEndChange=(-Main.player.end[capturePieceType][endSquare]);
        }
    }
    public int score() {
        return preScore;
    }
    public boolean equals(Object m) {
        return getClass() == m.getClass() && equals((CaptureMove) m);
    }
    public boolean equals(CaptureMove m) {
        return (startSquare==m.startSquare && endSquare==m.endSquare && pieceType==m.pieceType && capturePieceType==m.capturePieceType);
    }
    public String toString() {
        String res = "";
        switch(startSquare&7) {
            case 0: res+="a";break;
            case 1: res+="b";break;
            case 2: res+="c";break;
            case 3: res+="d";break;
            case 4: res+="e";break;
            case 5: res+="f";break;
            case 6: res+="g";break;
            case 7: res+="h";break;
        }
        switch(startSquare>>3) {
            case 0: res+="1";break;
            case 1: res+="2";break;
            case 2: res+="3";break;
            case 3: res+="4";break;
            case 4: res+="5";break;
            case 5: res+="6";break;
            case 6: res+="7";break;
            case 7: res+="8";break;
        }
        res+=" ";
        switch(endSquare&7) {
            case 0: res+="a";break;
            case 1: res+="b";break;
            case 2: res+="c";break;
            case 3: res+="d";break;
            case 4: res+="e";break;
            case 5: res+="f";break;
            case 6: res+="g";break;
            case 7: res+="h";break;
        }
        switch(endSquare>>3) {
            case 0: res+="1";break;
            case 1: res+="2";break;
            case 2: res+="3";break;
            case 3: res+="4";break;
            case 4: res+="5";break;
            case 5: res+="6";break;
            case 6: res+="7";break;
            case 7: res+="8";break;
        }
        res+=" ";
        switch(pieceType) {
            case 8:  res+="WK";break;
            case 9:  res+="WP";break;
            case 10: res+="WN";break;
            case 11: res+="WB";break;
            case 12: res+="WR";break;
            case 13: res+="WQ";break;
            case 16: res+="BK";break;
            case 17: res+="BP";break;
            case 18: res+="BN";break;
            case 19: res+="BB";break;
            case 20: res+="BR";break;
            case 21: res+="BQ";break;
        }
        res+="x";
        switch(capturePieceType) {
            case 8:  res+="WK";break;
            case 9:  res+="WP";break;
            case 10: res+="WN";break;
            case 11: res+="WB";break;
            case 12: res+="WR";break;
            case 13: res+="WQ";break;
            case 16: res+="BK";break;
            case 17: res+="BP";break;
            case 18: res+="BN";break;
            case 19: res+="BB";break;
            case 20: res+="BR";break;
            case 21: res+="BQ";break;
        }
        return "Capture: "+res;
    }
    public void makeMove(Board board) {
        //long t = System.currentTimeMillis();
        int side = board.whiteToMove?0:1;
        int opp = 1-side;
        oldEnPassant=board.validEnPassant;
        oldCastle = board.canCastle;
        oldKey=board.key;
        oldMask=board.mask[side][6];
        int piece = pieceType&7;
        int capturePiece=capturePieceType&7;
        oldPieceMask=board.mask[side][piece];
        board.validEnPassant=9;
        long combined = 1L<<startSquare|1L<<endSquare;
        long end = 1L<<endSquare;
        board.mask[side][piece]^=combined;
        board.mask[side][6]^=combined;
        board.mask[opp][6]^=end;
        board.mask[opp][capturePiece]^=end;
        board.canCastle&=((pieceType==8)||(pieceType==12&&startSquare==7))||(capturePieceType==12&&endSquare==7)?14:15;
        board.canCastle&=((pieceType==8)||(pieceType==12&&startSquare==0))||(capturePieceType==12&&endSquare==0)?13:15;
        board.canCastle&=((pieceType==16)||(pieceType==20&&startSquare==63))||(capturePieceType==20&&endSquare==63)?11:15;
        board.canCastle&=((pieceType==16)||(pieceType==20&&startSquare==56))||(capturePieceType==20&&endSquare==56)?7:15;
        board.key^=PreCompute.turnHash^PreCompute.enPassantHash[oldEnPassant]^PreCompute.pieceHash[startSquare][pieceType]^PreCompute.pieceHash[endSquare][pieceType]^PreCompute.pieceHash[endSquare][capturePieceType];
        board.key^=PreCompute.castleHash[board.canCastle];
        board.key^=PreCompute.castleHash[oldCastle];
        board.board[endSquare]=pieceType;
        board.board[startSquare]=0;
        board.numPieces[opp][capturePiece]--;
        board.whiteToMove=!board.whiteToMove;

        //board.moves.add(this);
        board.numMoves++;
        board.whiteStartBonus+=whitePieceStartChange;
        board.whiteEndBonus+=whitePieceEndChange;
        board.blackStartBonus+=blackPieceStartChange;
        board.blackEndBonus+=blackPieceEndChange;
        board.pieceMask=board.mask[0][6]^board.mask[1][6];
        board.kingCoords[0]=board.bsf(board.mask[0][0]);
        board.kingCoords[1]=board.bsf(board.mask[1][0]);
        //Board.elapsed3+=System.currentTimeMillis()-t;
        board.updateOccupancy();
    }
    public void undoMove(Board board) {
        //long t = System.currentTimeMillis();
        board.whiteToMove=!board.whiteToMove;
        int side = board.whiteToMove?0:1;
        int opp = 1-side;
        board.key=oldKey;
        board.validEnPassant=oldEnPassant;
        board.canCastle=oldCastle;
        int piece = capturePieceType&7;
        board.mask[side][pieceType&7]=oldPieceMask;
        board.mask[side][6]=oldMask;
        board.mask[opp][piece]^=1L<<endSquare;
        board.mask[opp][6]^=1L<<endSquare;
        board.board[startSquare]=pieceType;
        board.board[endSquare]=capturePieceType;
        board.numPieces[opp][piece]++;

        //board.moves.remove(board.moves.size()-1);
        board.numMoves--;
        board.whiteStartBonus-=whitePieceStartChange;
        board.whiteEndBonus-=whitePieceEndChange;
        board.blackStartBonus-=blackPieceStartChange;
        board.blackEndBonus-=blackPieceEndChange;
        board.pieceMask=board.mask[0][6]^board.mask[1][6];
        board.kingCoords[0]=board.bsf(board.mask[0][0]);
        board.kingCoords[1]=board.bsf(board.mask[1][0]);
        //Board.elapsed3+=System.currentTimeMillis()-t;
        board.updateOccupancy();
    }
    public byte getEnd() {return endSquare;}
    public byte getStart() {return startSquare;}
    public byte getType() {return pieceType;}
}
