package Sachovnice.calculations;

import Sachovnice.calculations.calculations.SettingFigure;
import Sachovnice.calculations.calculations.VirtualChessField;
import Sachovnice.calculations.calculations.AvailableFigureMoves;
import Sachovnice.calculations.figureDataTypes.Figure;
import Sachovnice.calculations.figureDataTypes.FigureInField;
import Sachovnice.calculations.figureDataTypes.FigureWithField;
import Sachovnice.calculations.figureDataTypes.InsertedFigure;
import Sachovnice.calculations.types.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CalculateFigurePathing extends AvailableFigureMoves {
    protected InsertedFigure figura1;
    protected InsertedFigure figura2;

    private final Sizing size;

    private final Result result;

    protected ArrayList<ArrayList<Field>> fields;

    protected VisitedFields visitedFields;

    public CalculateFigurePathing(int height, int width, InsertedFigure figure1, InsertedFigure figure2) {
        if(width <= 0)
            throw new Error("Špatná šířka");

        if(height <= 0)
            throw new Error("Špatná šířka");

        this.size = new Sizing(width, height);

        this.fields = VirtualChessField.generateVirtualChessboard(this.size.width, this.size.height);

        this.visitedFields = new VisitedFields(new ArrayList<Field>(), new ArrayList<Field>());

        this.result = new Result(0, false, null, new ArrayList<>(), new ArrayList<>());

        this.figura1 = figure1;
        this.figura2 = figure2;
        new SettingFigure(figure1, this.fields, this.visitedFields);
        new SettingFigure(figure2, this.fields, this.visitedFields);
    }

    /**
     * Calculates number of played moves.
     * Repeat until finds solution or there is any fields where can figures go.
     *
     * @return Result with count of moves and way of first and second figure to cross field
     */
    public Result countFigureMoves() {
        if (this.figura1 == null)
            throw new Error("Není zadaná figura 1");

        if (this.figura2 == null)
            throw new Error("Není zadaná figura 2");

        while (this.visitedFields.figure1.size() != 0 && this.visitedFields.figure2.size() != 0 && !this.result.isCrossed) {
            this.result.pocetTahu++;
            goThrowVisitedFields(this.visitedFields.figure1, 1);

            if (!this.result.isCrossed)
                goThrowVisitedFields(this.visitedFields.figure2, 2);
        }

        if (this.result.isCrossed)
            return this.result;

        return null;
    }

    /**
     * Go throw all visited fields
     *
     * @param visitedFields - List of visited fields in last
     * @param numberOfFigure - Number of figure
     */
    private void goThrowVisitedFields(ArrayList<Field> visitedFields, int numberOfFigure) {
        if (numberOfFigure == 1) {
            this.visitedFields.figure1 = new ArrayList<>();
        } else if (numberOfFigure == 2) {
            this.visitedFields.figure2 = new ArrayList<>();
        }

        for (int i = 0; i < visitedFields.size() && !this.result.isCrossed; i++) {
            calculateAvailableFieldsForFigure(
                new Figure(visitedFields.get(i).x, visitedFields.get(i).y, numberOfFigure)
            );
        }
    }

    /**
     * Calculates all available fields from field where figure stand at this time.
     *
     * @param data Information about figure - position - x - Figures x
     *                                                 - y - Figures y
     *                                      - figureNumber - Number of figure {1 | 2}
     */
    private void calculateAvailableFieldsForFigure(Figure data) {
        HashMap<String, InsertedFigure> aktualniFigura = new HashMap<>();
        aktualniFigura.put("figure1", this.figura1);
        aktualniFigura.put("figure2", this.figura2);
        List<Positions> availableFields = calculateFigureAvailableFields(
            aktualniFigura.get("figure" + data.figureNumber).typ,
            new Positions(data.x, data.y),
            this.size,
            data.figureNumber,
            this.result.pocetTahu
        );

        for (int i = 0; availableFields.size() > i && !this.result.isCrossed; i++) {
            int x = data.x + availableFields.get(i).x;
            int y = data.y + availableFields.get(i).y;

            if (x >= 0 && x < this.size.width && y >= 0 && y < this.size.height) {
                Field evaluatedPlayField = this.foundPlayField(x, y);

                HashMap<Object, FigureInField> evaluatedPlayFieldFigureHash = new HashMap<>();
                evaluatedPlayFieldFigureHash.put("figure1", evaluatedPlayField.figure1);
                evaluatedPlayFieldFigureHash.put("figure2", evaluatedPlayField.figure2);

                FigureInField figure = evaluatedPlayFieldFigureHash.get("figure" + data.figureNumber);

                if (figure.predchoziPole.equals("x1") && !figure.aktualniPoziceFigury) {
                    markFieldFigureEntered(
                        new FigureWithField(data.x, data.y, data.figureNumber, evaluatedPlayField),
                        !(aktualniFigura.get("figure" + data.figureNumber).typ.equals("pawn") && availableFields.get(i).y != 0)
                    );
                }
            }
        }
    }

    /**
     * Marks field where figure entered.
     * At field is marked which figure it was and from which field she came.
     *
     * @param field - Information about field on which is decided figures cross
     *                  - {Field} field - Field where is cross evaluated
     *                  - predchoziPole - Field from where figure came
     *                  - figureNumber - Figure number {1 | 2}
     * @param mark - If field will be marked (Only used for pawns)
     */
    private void markFieldFigureEntered(FigureWithField field, boolean mark) {
        evaluateFigureWaysCross(field);

        if (!this.result.isCrossed && mark) {
            HashMap<Object, FigureInField> calculatedFieldHash = new HashMap<>();
            calculatedFieldHash.put("poleFigure1", field.field.figure1);
            calculatedFieldHash.put("poleFigure2", field.field.figure2);

            HashMap<Object, ArrayList<Field>> navstivena = new HashMap<>();
            navstivena.put("visitedFields1", this.visitedFields.figure1);
            navstivena.put("visitedFields2", this.visitedFields.figure2);

            FigureInField fieldFigure = calculatedFieldHash.get("poleFigure" + field.figureNumber);
            fieldFigure.visited = true;
            navstivena.get("visitedFields" + field.figureNumber).add(field.field);

            if (!fieldFigure.aktualniPoziceFigury && fieldFigure.predchoziPole.equals("x1")) {
                fieldFigure.predchoziPole = field.x + "-" + field.y;
                fieldFigure.cisloTahu = this.result.pocetTahu;
            }
        }
    }

    /**
     * Evaluates if figures crossed each other on the field
     *
     * @param field - Information about field on which is decided figures cross
     *                  - {Field} field - Field where is cross evaluated
     *                  - predchoziPole - Field from where figure came
     *                  - figureNumber - Figure number {1 | 2}
     */
    private void evaluateFigureWaysCross(FigureWithField field) {
        HashMap<Object, FigureInField> aktualniPolickoHash = new HashMap<>();
        aktualniPolickoHash.put("figure1", field.field.figure1);
        aktualniPolickoHash.put("figure2", field.field.figure2);

        if (aktualniPolickoHash.get("figure" + (field.figureNumber == 1 ? 2 : 1)).visited &&
            !aktualniPolickoHash.get("figure" + field.figureNumber).aktualniPoziceFigury
        ) {
            if (field.figureNumber == 1) {
                this.visitedFields.figure1 = new ArrayList<>();
            } else if (field.figureNumber == 2) {
                this.visitedFields.figure2 = new ArrayList<>();
            }
            FigureInField aktualniPolickoNastaveniFigura = aktualniPolickoHash.get("figure" + field.figureNumber);
            aktualniPolickoNastaveniFigura.predchoziPole = field.x + "-" + field.y;
            this.result.crossField = field.field;
            calculateFigurePath();
            this.result.isCrossed = true;
        }
    }

    /**
     * Calculates route of first and second figure
     */
    private void calculateFigurePath() {
        HashMap<String, String> crossFieldBeforeField = new HashMap<>();
        crossFieldBeforeField.put("figure1", this.result.crossField.figure1.predchoziPole);
        crossFieldBeforeField.put("figure2", this.result.crossField.figure2.predchoziPole);

        for (int i = 1; i <= 2; i++) {
            String beforeFieldId = crossFieldBeforeField.get("figure" + i);
            while (beforeFieldId != null && !beforeFieldId.equals("x1")) {
                String[] fieldBeforeId = beforeFieldId.split("-");
                int x = Integer.parseInt(fieldBeforeId[0]);
                int y = Integer.parseInt(fieldBeforeId[1]);

                Field pole = this.fields.get(y).get(x);
                HashMap<String, FigureInField> aktualniPolickoHash = new HashMap<>();
                aktualniPolickoHash.put("figure1", pole.figure1);
                aktualniPolickoHash.put("figure2", pole.figure2);

                if (!aktualniPolickoHash.get("figure" + i).aktualniPoziceFigury) {
                    beforeFieldId = aktualniPolickoHash.get("figure" + i).predchoziPole;
                    if (i == 1) {
                        this.result.figure1Way.add(pole);
                    } else {
                        this.result.figure2Way.add(pole);
                    }
                } else {
                    beforeFieldId = null;
                }
            }
        }
    }

    /**
     * Fiends field in virtual chessBoard
     * @param x - Fields x
     * @param y - Fields y
     * @return Field from virtual chessBoard
     */
    private Field foundPlayField(int x, int y) {
        return this.fields.get(y).get(x);
    }
}