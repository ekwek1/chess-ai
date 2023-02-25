package Chess;

public class EnPassantMove extends Move {
    private byte startSquare;
    private byte endSquare;
    private byte capturedSquare;
    public int preScore;
    private byte pieceType;
    private byte oldEnPassant;
    private byte indexOfCapturedPiece;
    private byte indexOfPiece;
    public EnPassantMove(byte start, byte captureSquare, byte newSquare, byte type, Board board) {
        startSquare=start;
        capturedSquare=captureSquare;
        endSquare=newSquare;
        pieceType=type;
        preScore=board.SEE(start,newSquare)*1000;
        if (type<16) {
            whitePieceStartChange=(Main.player.start[pieceType][endSquare]- Main.player.start[pieceType][startSquare]);
            whitePieceEndChange=(Main.player.end[pieceType][endSquare]- Main.player.end[pieceType][startSquare]);
            blackPieceStartChange=(-Main.player.start[17][capturedSquare]);
            blackPieceEndChange=(-Main.player.end[17][capturedSquare]);
        } else {
            blackPieceStartChange=(Main.player.start[pieceType][endSquare]- Main.player.start[pieceType][startSquare]);
            blackPieceEndChange=(Main.player.end[pieceType][endSquare]- Main.player.end[pieceType][startSquare]);
            whitePieceStartChange=(-Main.player.start[9][capturedSquare]);
            whitePieceEndChange=(-Main.player.end[9][capturedSquare]);
        }
    }
    public int score() {
        return preScore;
    }
    public boolean equals(Object m) {
        return getClass() == m.getClass() && equals((EnPassantMove) m);
    }
    public boolean equals(EnPassantMove m) {
        return (startSquare==m.startSquare && endSquare==m.endSquare && pieceType==m.pieceType && capturedSquare==m.capturedSquare);
    }
    public String toString() {
        return "En passant: "+startSquare+" "+endSquare;
    }
    public void makeMove(Board board) {
        int side = board.whiteToMove?0:1;
        oldEnPassant=board.validEnPassant;
        board.validEnPassant=9;
        board.key^=PreCompute.pieceHash[startSquare][pieceType];
        board.key^=PreCompute.pieceHash[endSquare][pieceType];
        board.key^=PreCompute.pieceHash[capturedSquare][pieceType^24];
        board.mask[side][6]^=1L<<startSquare;
        board.mask[side][6]^=1L<<endSquare;
        board.mask[1-side][6]^=1L<<capturedSquare;
        board.mask[side][1]^=1L<<startSquare;
        board.mask[side][1]^=1L<<endSquare;
        board.mask[1-side][1]^=1L<<capturedSquare;
        board.numPieces[1-side][1]--;
        board.board[startSquare]=0;
        board.board[capturedSquare]=0;
        board.board[endSquare]=pieceType;
        board.key^=PreCompute.turnHash;
        board.key^=PreCompute.enPassantHash[oldEnPassant];
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
        board.validEnPassant=oldEnPassant;
        board.board[endSquare]=0;
        board.board[startSquare]=pieceType;
        board.board[capturedSquare]=(byte)(pieceType^24);
        board.key^=PreCompute.pieceHash[startSquare][pieceType];
        board.key^=PreCompute.pieceHash[endSquare][pieceType];
        board.key^=PreCompute.pieceHash[capturedSquare][pieceType^24];
        board.numPieces[1-side][1]++;
        board.mask[side][6]^=1L<<startSquare;
        board.mask[side][6]^=1L<<endSquare;
        board.mask[1-side][6]^=1L<<capturedSquare;
        board.mask[side][1]^=1L<<startSquare;
        board.mask[side][1]^=1L<<endSquare;
        board.mask[1-side][1]^=1L<<capturedSquare;

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
    public byte getType() {return pieceType;}
}
