package sachovnice.calculations.figureDataTypes;

import sachovnice.calculations.types.Positions;

public class InsertedFigure {
    public Positions position;
    public int figureNumber;
    public String typ;

    public InsertedFigure(Positions position, int figureNumber, String typ) {
        this.position = position;
        this.figureNumber = figureNumber;
        this.typ = typ;
    }
}
