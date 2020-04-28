package solver;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;

import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Function;

public class Controller {

    public TableView<Pair<Double, Double>> pointsTable;
    public TableColumn<Pair<Double, Double>, Double> XColumn;
    public TableColumn<Pair<Double, Double>, Double> YColumn;
    public TextField addX;
    public TextField addY;
    public TextField getX;
    public Label getY;
    public Label errorLabel;
    public HBox chartBox;
    public Label functionLabel;
    public TextField leftLimit;
    public TextField rightLimit;
    public TextField stepField;

    private ObservableList<Pair<Double, Double>> pointsList;
    private double[] xTable;
    private double[][] diffTable;
    private HashMap<String, Function<Double, Double>> functionsMap;
    private Function<Double, Double> currentFunction;
    private String currentFunctionString;

    @FXML
    protected void initialize() {
        currentFunction = null;
        currentFunctionString = null;
        functionsMap = new HashMap<>();
        functionsMap.put("Empty", null);
        functionsMap.put("sin(x)", Math::sin);
        functionsMap.put("x^3", x -> Math.pow(x, 3));
        functionsMap.put("Random", x -> Math.random() * 50 - 25);
        functionsMap.put("x^2", x -> Math.pow(x, 2));
        functionsMap.put("abs(x)", Math::abs);
        pointsList = FXCollections.observableArrayList();
        XColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        YColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        pointsTable.setItems(pointsList);
        errorLabel.setText("");
    }

    public void addPoint() {
        try {
            double x = Double.parseDouble(addX.getText().trim().replaceAll(",", "."));
            errorLabel.setText("");
            if (pointsList.stream().noneMatch(o -> o.getKey().equals(x))) {
                pointsList.add(new Pair<>(x, Double.parseDouble(addY.getText().trim().replaceAll(",", "."))));

                if (pointsList.size() > 1) {
                    updateChart();
                }
                System.out.println("Add point");

            } else errorLabel.setText("Point with x = " + x + " is already exists");

        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid values");
        }
    }

    public void updatePoint() {
        try {
            double x = Double.parseDouble(addX.getText().trim().replaceAll(",", "."));
            errorLabel.setText("");
            Pair<Double, Double> pair = pointsList.stream().filter(o -> o.getKey().equals(x)).findAny().orElse(null);
            if (pair != null) {
                pointsList.remove(pair);
                pointsList.add(new Pair<>(x, Double.parseDouble(addY.getText().trim().replaceAll(",", "."))));

                if (pointsList.size() > 1) {
                    updateChart();
                }
                System.out.println("Update point");

            } else errorLabel.setText("Point with x = " + x + " doesn't exist");

        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid values");
        }
    }

    public void getYByX() {
        try {
            double x = Double.parseDouble(getX.getText());
            getY.setText("Y = " + Interpolation.formatToPrecision(Interpolation.getInterpolationY(x, diffTable, xTable), 5));
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid values");
        }
    }

    private void updateChart() {
        SortedList<Pair<Double, Double>> sortedList = pointsList.sorted(Comparator.comparing(Pair::getKey));
        xTable = new double[sortedList.size()];
        diffTable = new double[sortedList.size()][sortedList.size()];
        for (int i = 0; i < sortedList.size(); i++) {
            xTable[i] = sortedList.get(i).getKey();
            diffTable[i][0] = sortedList.get(i).getValue();
        }

        double step = (Math.ceil(xTable[xTable.length - 1]) - Math.floor(xTable[0])) / 30;
        if (step <= 0) step = 0.5;
        double a = Math.floor(xTable[0] - step);
        double b = Math.ceil(xTable[xTable.length - 1] + step);
        System.out.println(a + " " + b + " " + step);

        Interpolation.getDiffTable(xTable, diffTable);
        LineChart<Number, Number> chart = drawInterpolationPlot(a, b, step);

        if (currentFunction != null) {
            if (currentFunctionString.equals("Random")) drawFunctionPlot(xTable, diffTable, chart);
            else drawFunctionPlot(currentFunction, a, b, chart);
        }

        for (int i = 0; i < xTable.length; i++) {
            addRootToChart(xTable[i], chart, "x" + i);
        }
    }

    private void drawFunctionPlot(double[] x, double[][] y, LineChart<Number, Number> chart) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(currentFunctionString);
        ObservableList<XYChart.Data<Number, Number>> data = series.getData();

        double step = (Math.ceil(x[0]) - Math.floor(x[x.length - 1])) / 30;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (int i = 0; i < x.length; i++) {
            data.add(new XYChart.Data<>(x[i], y[i][0]));
            if (Math.ceil(y[i][0]) > max) max = Math.ceil(y[i][0]);
            if (Math.floor(y[i][0]) < min) min = Math.floor(y[i][0]);
        }

        chart.getData().add(series);

        for (XYChart.Data<Number, Number> dataNode : series.getData()) {
            StackPane stackPane = (StackPane) dataNode.getNode();
            stackPane.setVisible(false);
        }

        System.out.println("Function plot");

    }

    private void drawFunctionPlot(Function<Double, Double> function, double a, double b, LineChart<Number, Number> chart) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(currentFunctionString);
        ObservableList<XYChart.Data<Number, Number>> data = series.getData();

        double step = (Math.ceil(b) - Math.floor(a)) / 30;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (double i = Math.floor(a); i <= Math.ceil(b); i += step) {
            double y = function.apply(i);
            data.add(new XYChart.Data<>(i, function.apply(i)));
            if (Math.ceil(y) > max) max = Math.ceil(y);
            if (Math.floor(y) < min) min = Math.floor(y);
        }

        chart.getData().add(series);

        for (XYChart.Data<Number, Number> dataNode : series.getData()) {
            StackPane stackPane = (StackPane) dataNode.getNode();
            stackPane.setVisible(false);
        }

        System.out.println("Function plot");

    }

    private LineChart<Number, Number> drawInterpolationPlot(double a, double b, double step) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Interpolation plot");
        ObservableList<XYChart.Data<Number, Number>> data = series.getData();

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (double i = a; i <= b; i += step) {
            double y = Interpolation.getInterpolationY(i, diffTable, xTable);
            data.add(new XYChart.Data<>(i, y));
            if (Math.ceil(y) > max) max = Math.ceil(y);
            if (Math.floor(y) < min) min = Math.floor(y);
        }

        NumberAxis yAxis = new NumberAxis(min - (max - min) / 10, max + (max - min) / 10, (max - min) / 10);
        yAxis.setLabel("y");
        NumberAxis xAxis = new NumberAxis(a, b, step * 10);
        xAxis.setLabel("x");
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.getData().add(series);

        for (XYChart.Data<Number, Number> dataNode : series.getData()) {
            StackPane stackPane = (StackPane) dataNode.getNode();
            stackPane.setVisible(false);
        }

        chart.setTitle("Function plot");

        chartBox.getChildren().clear();
        chartBox.getChildren().add(chart);
        System.out.println("Interpolation plot");
        return chart;
    }

    private void addRootToChart(double x, LineChart<Number, Number> chart, String rootName) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>(x, Interpolation.getInterpolationY(x, diffTable, xTable)));
        series.setName(rootName);
        chart.getData().add(series);
    }

    public void clearPointsList() {
        pointsList.clear();
    }

    public void pickFunction(ActionEvent event) {
        String target = ((Button) event.getTarget()).getText();
        currentFunction = functionsMap.get(target);
        currentFunctionString = target;
        functionLabel.setText("Current function: " + target);
        updatePointsListForFunction();
    }

    private void updatePointsListForFunction() {
        try {
            double a = Double.parseDouble(leftLimit.getText().trim().replaceAll(",", "."));
            double b = Double.parseDouble(rightLimit.getText().trim().replaceAll(",", "."));
            double step = Double.parseDouble(stepField.getText().trim().replaceAll(",", "."));
            if (step > (b - a) / 2) throw new NumberFormatException();
            errorLabel.setText("");
            clearPointsList();
            if (currentFunction != null) {
                for (double i = a; i <= b; i += step) {
                    pointsList.add(new Pair<>(i, currentFunction.apply(i)));
                }

                updateChart();
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid generation values");
        }
    }
}
