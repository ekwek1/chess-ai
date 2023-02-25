package Chess;

public class Move {
    private byte startSquare;
    private byte endSquare;
    public int preScore;
    private byte pieceType;
    private long oldKey;
    private long oldMask;
    private long oldPieceMask;
    private byte oldEnPassant;
    private byte oldCastle;
    public int whitePieceStartChange;
    public int blackPieceStartChange;
    public int whitePieceEndChange;
    public int blackPieceEndChange;
    public byte indexOfPiece;
    public int orderingIdx;
    public int order;
    public Move() {}
    public Move(byte start, byte end, byte type, Board board) {
        startSquare=start;
        endSquare=end;
        pieceType=type;
        preScore=(int)((double)(Main.history[board.whiteToMove?0:1][start][end])/(Main.historyFreq[board.whiteToMove?0:1][start][end]+1)*1000);
        if (preScore<=0) {
            if (preScore==0) preScore=-1000;
            else preScore=-2000;
            if (type<16) {
                preScore+=(board.endgameWeight(true)* Main.player.end[pieceType][endSquare]+(1-board.endgameWeight(true))* Main.player.start[pieceType][endSquare]);
                preScore-=(board.endgameWeight(true)* Main.player.end[pieceType][startSquare]+(1-board.endgameWeight(true))* Main.player.start[pieceType][startSquare]);
            } else {
                preScore+=(board.endgameWeight(false)* Main.player.end[pieceType][endSquare]+(1-board.endgameWeight(false))* Main.player.start[pieceType][endSquare]);
                preScore-=(board.endgameWeight(false)* Main.player.end[pieceType][startSquare]+(1-board.endgameWeight(false))* Main.player.start[pieceType][startSquare]);
            }
        }
        //preScore=Main.history[board.whiteToMove?0:1][start][end]*1000;
        //if ((board.pawnMask((type&8)==0)&(1L<<end))!=0) preScore-=PreCompute.pieceValues[type&7]*1000;
		/*
        if (type<16) {
            preScore+=(board.endgameWeight(true)* Main.player.end[pieceType][endSquare]+(1-board.endgameWeight(true))* Main.player.start[pieceType][endSquare]);
            preScore-=(board.endgameWeight(true)* Main.player.end[pieceType][startSquare]+(1-board.endgameWeight(true))* Main.player.start[pieceType][startSquare]);
        } else {
            preScore+=(board.endgameWeight(false)* Main.player.end[pieceType][endSquare]+(1-board.endgameWeight(false))* Main.player.start[pieceType][endSquare]);
            preScore-=(board.endgameWeight(false)* Main.player.end[pieceType][startSquare]+(1-board.endgameWeight(false))* Main.player.start[pieceType][startSquare]);
        }
		*/
        if (type<16) {
            whitePieceStartChange=(Main.player.start[pieceType][endSquare]- Main.player.start[pieceType][startSquare]);
            whitePieceEndChange=(Main.player.end[pieceType][endSquare]- Main.player.end[pieceType][startSquare]);
        } else {
            blackPieceStartChange=(Main.player.start[pieceType][endSquare]- Main.player.start[pieceType][startSquare]);
            blackPieceEndChange=(Main.player.end[pieceType][endSquare]- Main.player.end[pieceType][startSquare]);
        }
    }
    public int score() {
        return preScore;
    }
    public boolean equals(Object m) {
        return m.getClass() == this.getClass() && startSquare==((Move)m).startSquare && endSquare==((Move)m).endSquare && pieceType==((Move)m).pieceType;
    }
    /*
    public boolean equals(Move m) {
        System.out.println(getClass()+" "+m.getClass());
        return (startSquare==m.startSquare && endSquare==m.endSquare && pieceType==m.pieceType);
    }
    */
    public int hashCode() {
        return 0;
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
        if (orderingIdx!=0) res+=" "+orderingIdx;
        if (order!=0) res+=" "+order;
        return "Move: "+res;
    }
    public void makeMove(Board board) {
        board.board[startSquare]=0;
        board.board[endSquare]=pieceType;
        int side = board.whiteToMove?0:1;
        int idx = pieceType&7;
        oldEnPassant=board.validEnPassant;
        oldCastle=board.canCastle;
        oldKey=board.key;
        oldMask=board.mask[side][6];
        oldPieceMask=board.mask[side][idx];
        board.validEnPassant=(idx==1 && Math.abs(startSquare-endSquare)==16)?(byte)(startSquare&7):9;
        long change = 1L<<startSquare|1L<<endSquare;
        board.mask[side][idx]^=change;
        board.mask[side][6]^=change;
        board.pieceMask^=change;
        if ((pieceType==8)||(pieceType==12&&startSquare==7)) board.canCastle&=14;
        if ((pieceType==8)||(pieceType==12&&startSquare==0)) board.canCastle&=13;
        if ((pieceType==16)||(pieceType==20&&startSquare==63)) board.canCastle&=11;
        if ((pieceType==16)||(pieceType==20&&startSquare==56)) board.canCastle&=7;
        board.key^=PreCompute.pieceHash[startSquare][pieceType]^PreCompute.pieceHash[endSquare][pieceType]^PreCompute.turnHash^PreCompute.enPassantHash[oldEnPassant]^PreCompute.enPassantHash[board.validEnPassant]^PreCompute.castleHash[oldCastle]^PreCompute.castleHash[board.canCastle];
        board.whiteToMove=!board.whiteToMove;
        board.numMoves++;
        board.whiteStartBonus+=whitePieceStartChange;
        board.whiteEndBonus+=whitePieceEndChange;
        board.blackStartBonus+=blackPieceStartChange;
        board.blackEndBonus+=blackPieceEndChange;
        if (idx==0) board.kingCoords[side]=endSquare;
        board.updateOccupancy();
    }
    public void undoMove(Board board) {
        board.whiteToMove=!board.whiteToMove;
        int side = board.whiteToMove?0:1;
        board.canCastle=oldCastle;
        board.validEnPassant=oldEnPassant;
        board.key=oldKey;
        board.mask[side][pieceType&7]=oldPieceMask;
        board.mask[side][6]=oldMask;
        board.board[endSquare]=0;
        board.board[startSquare]=pieceType;

        board.numMoves--;
        board.whiteStartBonus-=whitePieceStartChange;
        board.whiteEndBonus-=whitePieceEndChange;
        board.blackStartBonus-=blackPieceStartChange;
        board.blackEndBonus-=blackPieceEndChange;
        board.pieceMask=board.mask[0][6]|board.mask[1][6];
        if ((pieceType&7)==0) board.kingCoords[side]=startSquare;
        board.updateOccupancy();
    }
    public byte getEnd() {return endSquare;}
    public byte getStart() {return startSquare;}
    public byte getType() {return pieceType;}
}
