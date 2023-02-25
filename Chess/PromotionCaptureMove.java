package Chess;

public class PromotionCaptureMove extends Move {
    private byte startSquare;
    private byte endSquare;
    public int preScore;
    private byte capturePieceType;
    private byte newPieceType;
    private byte oldEnPassant;
    private byte oldCastle;
    private byte indexOfCapturedPiece;
    private byte indexOfPawn;
    public PromotionCaptureMove(byte start, byte end, byte type, byte captureType) {
        startSquare=start;
        endSquare=end;
        preScore=1000*(PreCompute.pieceValues[captureType&7]/10-1+(PreCompute.pieceValues[type&7]/10-1));
        capturePieceType=captureType;
        newPieceType=type;
        if (type<16) {
            whitePieceStartChange=(Main.player.start[newPieceType][endSquare]- Main.player.start[9][startSquare]);
            whitePieceEndChange=(Main.player.end[newPieceType][endSquare]- Main.player.end[9][startSquare]);
            blackPieceStartChange=(-Main.player.start[capturePieceType][endSquare]);
            blackPieceEndChange=(-Main.player.end[capturePieceType][endSquare]);
        } else {
            blackPieceStartChange=(Main.player.start[newPieceType][endSquare]- Main.player.start[17][startSquare]);
            blackPieceEndChange=(Main.player.end[newPieceType][endSquare]- Main.player.end[17][startSquare]);
            whitePieceStartChange=(-Main.player.start[capturePieceType][endSquare]);
            whitePieceEndChange=(-Main.player.end[capturePieceType][endSquare]);
        }
    }
    public int score() {
        return preScore;
    }
    public boolean equals(Object m) {
        return getClass() == m.getClass() && equals((PromotionCaptureMove) m);
    }
    public boolean equals(PromotionCaptureMove m) {
        return (startSquare==m.startSquare && endSquare==m.endSquare && newPieceType==m.newPieceType && capturePieceType==m.capturePieceType);
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
        switch(newPieceType) {
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
        return "Promotion: "+res;
    }
    public void makeMove(Board board) {
        int side = board.whiteToMove?0:1;
        oldEnPassant=board.validEnPassant;
        oldCastle=board.canCastle;
        board.validEnPassant=9;
        board.board[startSquare]=0;
        board.board[endSquare]=newPieceType;

        board.key^=PreCompute.pieceHash[startSquare][(newPieceType&24)+1];
        board.mask[side][1]^=1L<<startSquare;

        board.key^=PreCompute.pieceHash[endSquare][newPieceType];
        board.mask[side][newPieceType&7]^=1L<<endSquare;

        board.key^=PreCompute.pieceHash[endSquare][capturePieceType];
        board.mask[1-side][capturePieceType&7]^=1L<<endSquare;
        if (capturePieceType==12&&endSquare==7) board.canCastle&=14;
        if (capturePieceType==12&&endSquare==0) board.canCastle&=13;
        if (capturePieceType==20&&endSquare==63) board.canCastle&=11;
        if (capturePieceType==20&&endSquare==56) board.canCastle&=7;
        board.key^=PreCompute.turnHash;
        board.key^=PreCompute.enPassantHash[oldEnPassant];
        board.key^=PreCompute.enPassantHash[board.validEnPassant];
        board.key^=PreCompute.castleHash[oldCastle];
        board.key^=PreCompute.castleHash[board.canCastle];
        board.mask[side][6]^=1L<<startSquare;
        board.mask[side][6]^=1L<<endSquare;
        board.mask[1-side][6]^=1L<<endSquare;
        board.numPieces[side][1]--;
        board.numPieces[side][newPieceType&7]++;
        board.numPieces[1-side][capturePieceType&7]--;
        board.whiteToMove=!board.whiteToMove;

        board.numMoves++;
        board.whiteStartBonus+=whitePieceStartChange;
        board.whiteEndBonus+=whitePieceEndChange;
        board.blackStartBonus+=blackPieceStartChange;
        board.blackEndBonus+=blackPieceEndChange;
        board.pieceMask=board.mask[0][6]^board.mask[1][6];
        board.kingCoords[0]=board.bsf(board.mask[0][0]);
        board.kingCoords[1]=board.bsf(board.mask[1][0]);
        board.updateOccupancy();
    }
    public void undoMove(Board board) {
        board.whiteToMove=!board.whiteToMove;
        int side = board.whiteToMove?0:1;
        board.key^=PreCompute.turnHash;
        board.key^=PreCompute.enPassantHash[oldEnPassant];
        board.key^=PreCompute.enPassantHash[board.validEnPassant];
        board.key^=PreCompute.castleHash[oldCastle];
        board.key^=PreCompute.castleHash[board.canCastle];
        board.validEnPassant=oldEnPassant;
        board.board[startSquare]=(byte)(board.whiteToMove?9:17);
        board.board[endSquare]=capturePieceType;
        board.canCastle=oldCastle;

        board.key^=PreCompute.pieceHash[startSquare][(newPieceType&24)+1];
        board.mask[side][1]^=1L<<startSquare;

        board.key^=PreCompute.pieceHash[endSquare][newPieceType];
        board.mask[side][newPieceType&7]^=1L<<endSquare;

        board.key^=PreCompute.pieceHash[endSquare][capturePieceType];
        board.mask[1-side][capturePieceType&7]^=1L<<endSquare;

        board.mask[side][6]^=1L<<startSquare;
        board.mask[side][6]^=1L<<endSquare;
        board.mask[1-side][6]^=1L<<endSquare;
        board.numPieces[side][1]++;
        board.numPieces[side][newPieceType&7]--;
        board.numPieces[1-side][capturePieceType&7]++;

        board.numMoves--;
        board.whiteStartBonus-=whitePieceStartChange;
        board.whiteEndBonus-=whitePieceEndChange;
        board.blackStartBonus-=blackPieceStartChange;
        board.blackEndBonus-=blackPieceEndChange;
        board.pieceMask=board.mask[0][6]^board.mask[1][6];
        board.kingCoords[0]=board.bsf(board.mask[0][0]);
        board.kingCoords[1]=board.bsf(board.mask[1][0]);
        board.updateOccupancy();
    }
    public byte getEnd() {return endSquare;}
    public byte getStart() {return startSquare;}
    public byte getType() {return (byte)(newPieceType>16?17:9);}
    public byte getNewType() {return newPieceType;}
}
