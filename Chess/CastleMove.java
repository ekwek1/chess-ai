package Chess;

public class CastleMove extends Move {
    public int preScore;
    private byte type;
    private byte oldEnPassant;
    private byte oldCastle;
    public CastleMove(byte type, Board board) {
        this.type=type; // (0 - WK, 1 - WQ, 2 - BK, 3 - BQ)
		/*
        switch(type&1) {
            case 0: preScore=49;break;
            case 1: preScore=40;break;
        }
		*/
        switch(type) {
            case 0:
                whitePieceStartChange=49;
                whitePieceEndChange=11;
                preScore+=(int)((double)Main.history[board.whiteToMove?0:1][4][6]/(Main.historyFreq[board.whiteToMove?0:1][4][6]+1)*1000);
                //preScore+=Main.history[board.whiteToMove?0:1][4][6]*1000;
                break;
            case 1:
                whitePieceStartChange=40;
                whitePieceEndChange=15;
                preScore+=(int)((double)Main.history[board.whiteToMove?0:1][4][2]/(Main.historyFreq[board.whiteToMove?0:1][4][2]+1)*1000);
                //preScore+=Main.history[board.whiteToMove?0:1][4][2]*1000;
                break;
            case 2:
                blackPieceStartChange=49;
                blackPieceEndChange=11;
                preScore+=(int)((double)Main.history[board.whiteToMove?0:1][60][62]/(Main.historyFreq[board.whiteToMove?0:1][60][62]+1)*1000);
                //preScore+=Main.history[board.whiteToMove?0:1][60][62]*1000;
                break;
            case 3:
                blackPieceStartChange=40;
                blackPieceEndChange=15;
                preScore+=(int)((double)Main.history[board.whiteToMove?0:1][60][58]/(Main.historyFreq[board.whiteToMove?0:1][60][58]+1)*1000);
                //preScore+=Main.history[board.whiteToMove?0:1][60][58]*1000;
                break;
        }
    }
    public int score() {
        return preScore;
    }
    public boolean equals(Object m) {
        return getClass() == m.getClass() && equals((CastleMove) m);
    }
    public boolean equals(CastleMove m) {
        return (type==m.type);
    }
    public String toString() {
        String res = "";
        switch(type&2) {
            case 0: res="W ";break;
            case 2: res="B ";break;
        }
        switch(type&1) {
            case 0: res+="0-0";break;
            case 1: res+="0-0-0";break;
        }
        if (orderingIdx!=0) res+=" "+orderingIdx;
        if (order!=0) res+=" "+order;
        return "Castle: "+res;
    }
    public void makeMove(Board board) {
        byte indexOfRook = 0;
        int side = board.whiteToMove?0:1;
        oldEnPassant=board.validEnPassant;
        oldCastle=board.canCastle;
        switch (type) {
            case 0:
                board.board[4]=0;
                board.board[6]=8;
                board.board[7]=0;
                board.board[5]=12;
                board.key^=PreCompute.pieceHash[4][8];
                board.key^=PreCompute.pieceHash[6][8];
                board.key^=PreCompute.pieceHash[7][12];
                board.key^=PreCompute.pieceHash[5][12];
                board.mask[0][6]^=1L<<4;
                board.mask[0][6]^=1L<<5;
                board.mask[0][6]^=1L<<6;
                board.mask[0][6]^=1L<<7;
                board.mask[0][0]^=1L<<4;
                board.mask[0][4]^=1L<<5;
                board.mask[0][0]^=1L<<6;
                board.mask[0][4]^=1L<<7;
                break;
            case 1:
                board.board[4]=0;
                board.board[2]=8;
                board.board[0]=0;
                board.board[3]=12;
                board.key^=PreCompute.pieceHash[4][8];
                board.key^=PreCompute.pieceHash[2][8];
                board.key^=PreCompute.pieceHash[0][12];
                board.key^=PreCompute.pieceHash[3][12];
                board.mask[0][6]^=1L;
                board.mask[0][6]^=1L<<2;
                board.mask[0][6]^=1L<<3;
                board.mask[0][6]^=1L<<4;
                board.mask[0][4]^=1L;
                board.mask[0][0]^=1L<<2;
                board.mask[0][4]^=1L<<3;
                board.mask[0][0]^=1L<<4;
                break;
            case 2:
                board.board[60]=0;
                board.board[62]=16;
                board.board[63]=0;
                board.board[61]=20;
                board.key^=PreCompute.pieceHash[60][16];
                board.key^=PreCompute.pieceHash[62][16];
                board.key^=PreCompute.pieceHash[63][20];
                board.key^=PreCompute.pieceHash[61][20];
                board.mask[1][6]^=1L<<60;
                board.mask[1][6]^=1L<<61;
                board.mask[1][6]^=1L<<62;
                board.mask[1][6]^=1L<<63;
                board.mask[1][0]^=1L<<60;
                board.mask[1][4]^=1L<<61;
                board.mask[1][0]^=1L<<62;
                board.mask[1][4]^=1L<<63;
                break;
            case 3:
                board.board[60]=0;
                board.board[58]=16;
                board.board[56]=0;
                board.board[59]=20;
                board.key^=PreCompute.pieceHash[60][16];
                board.key^=PreCompute.pieceHash[58][16];
                board.key^=PreCompute.pieceHash[56][20];
                board.key^=PreCompute.pieceHash[59][20];
                board.mask[1][6]^=1L<<56;
                board.mask[1][6]^=1L<<58;
                board.mask[1][6]^=1L<<59;
                board.mask[1][6]^=1L<<60;
                board.mask[1][4]^=1L<<56;
                board.mask[1][0]^=1L<<58;
                board.mask[1][4]^=1L<<59;
                board.mask[1][0]^=1L<<60;
                break;
        }
        board.validEnPassant=9;
        board.canCastle&=(board.whiteToMove?12:3);
        board.key^=PreCompute.turnHash;
        board.key^=PreCompute.enPassantHash[oldEnPassant];
        board.key^=PreCompute.castleHash[oldCastle];
        board.key^=PreCompute.castleHash[board.canCastle];
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
        board.key^=PreCompute.castleHash[oldCastle];
        board.key^=PreCompute.castleHash[board.canCastle];
        board.canCastle=oldCastle;
        board.validEnPassant=oldEnPassant;
        byte indexOfRook = 0;
        switch (type) {
            case 0:
                board.board[6]=0;
                board.board[4]=8;
                board.board[5]=0;
                board.board[7]=12;
                board.key^=PreCompute.pieceHash[4][8];
                board.key^=PreCompute.pieceHash[6][8];
                board.key^=PreCompute.pieceHash[7][12];
                board.key^=PreCompute.pieceHash[5][12];
                board.mask[0][6]^=1L<<4;
                board.mask[0][6]^=1L<<5;
                board.mask[0][6]^=1L<<6;
                board.mask[0][6]^=1L<<7;
                board.mask[0][0]^=1L<<4;
                board.mask[0][4]^=1L<<5;
                board.mask[0][0]^=1L<<6;
                board.mask[0][4]^=1L<<7;
                break;
            case 1:
                board.board[2]=0;
                board.board[4]=8;
                board.board[3]=0;
                board.board[0]=12;
                board.key^=PreCompute.pieceHash[4][8];
                board.key^=PreCompute.pieceHash[2][8];
                board.key^=PreCompute.pieceHash[0][12];
                board.key^=PreCompute.pieceHash[3][12];
                board.mask[0][6]^=1L;
                board.mask[0][6]^=1L<<2;
                board.mask[0][6]^=1L<<3;
                board.mask[0][6]^=1L<<4;
                board.mask[0][4]^=1L;
                board.mask[0][0]^=1L<<2;
                board.mask[0][4]^=1L<<3;
                board.mask[0][0]^=1L<<4;
                break;
            case 2:
                board.board[62]=0;
                board.board[60]=16;
                board.board[61]=0;
                board.board[63]=20;
                board.key^=PreCompute.pieceHash[60][16];
                board.key^=PreCompute.pieceHash[62][16];
                board.key^=PreCompute.pieceHash[63][20];
                board.key^=PreCompute.pieceHash[61][20];
                board.mask[1][6]^=1L<<60;
                board.mask[1][6]^=1L<<61;
                board.mask[1][6]^=1L<<62;
                board.mask[1][6]^=1L<<63;
                board.mask[1][0]^=1L<<60;
                board.mask[1][4]^=1L<<61;
                board.mask[1][0]^=1L<<62;
                board.mask[1][4]^=1L<<63;
                break;
            case 3:
                board.board[58]=0;
                board.board[60]=16;
                board.board[59]=0;
                board.board[56]=20;
                board.key^=PreCompute.pieceHash[60][16];
                board.key^=PreCompute.pieceHash[58][16];
                board.key^=PreCompute.pieceHash[56][20];
                board.key^=PreCompute.pieceHash[59][20];
                board.mask[1][6]^=1L<<56;
                board.mask[1][6]^=1L<<58;
                board.mask[1][6]^=1L<<59;
                board.mask[1][6]^=1L<<60;
                board.mask[1][4]^=1L<<56;
                board.mask[1][0]^=1L<<58;
                board.mask[1][4]^=1L<<59;
                board.mask[1][0]^=1L<<60;
                break;
        }

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
    public int castleType() {return type;}
    public byte getEnd() {
        switch (type) {
            case 0: return 6;
            case 1: return 2;
            case 2: return 62;
            case 3: return 58;
        }
        return -1;
    }
    public byte getStart() {
        switch (type) {
            case 0: return 4;
            case 1: return 4;
            case 2: return 60;
            case 3: return 60;
        }
        return -1;
    }
}
