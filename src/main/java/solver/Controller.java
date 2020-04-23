package solver;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Pair;
import org.mariuszgromada.math.mxparser.Expression;

import java.util.ArrayList;
import java.util.List;

public class Controller {

    public TableView<Pair<Double, Double>> pointsTable;
    public TableColumn<Pair<Double, Double>, Integer> numberColumn;
    public TableColumn<Pair<Double, Double>, Double> XColumn;
    public TableColumn<Pair<Double, Double>, Double> YColumn;
    public TextField addX;
    public TextField addY;
    public TextField getX;
    public Label getY;
    public Label errorLabel;
    public LineChart<Number, Number> functionChart;

    private ObservableList<Pair<Double, Double>> pointsList;

    @FXML
    protected void initialize() {
        pointsList = FXCollections.observableArrayList();
        XColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        YColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        pointsTable.setItems(pointsList);
        errorLabel.setText("");
    }

    public void addPoint() {
        try {
            double x = Double.parseDouble(addX.getText());
            errorLabel.setText("");
            if (pointsList.stream().noneMatch(o -> o.getKey().equals(x))) {
                pointsList.add(new Pair<>(x, Double.parseDouble(addY.getText())));
                System.out.println("Add point");
            } else errorLabel.setText("Point with x = " + x + " already exists");
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid values");
        }
    }

    public void getYByX() {
        try {
            double x = Double.parseDouble(getX.getText());
            getY.setText("Y = " + x * 2);
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid values");
        }
    }

    private LineChart<Number, Number> drawPlot(Expression f, double a, double b) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(f.getExpressionString());
        ObservableList<XYChart.Data<Number, Number>> data = series.getData();

        double step = (Math.ceil(b) - Math.floor(a)) / 30;
        String arg = f.getArgument(0).getArgumentName();
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double fx;

        for (double i = Math.floor(a); i <= Math.ceil(b); i += step) {
            f.setArgumentValue(arg, i);
            fx = f.calculate();
            data.add(new XYChart.Data<>(i, fx));
            if (Math.ceil(fx) > max) max = Math.ceil(fx);
            if (Math.floor(fx) < min) min = Math.floor(fx);
        }

        NumberAxis yAxis = new NumberAxis(min - (max - min) / 10, max + (max - min) / 10, (max - min) / 10);
        yAxis.setLabel("y");
        NumberAxis xAxis = new NumberAxis(Math.floor(a), Math.ceil(b), step * 10);
        xAxis.setLabel("x");
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.getData().add(series);

        for (XYChart.Data<Number, Number> dataNode : series.getData()) {
            StackPane stackPane = (StackPane) dataNode.getNode();
            stackPane.setVisible(false);
        }

        chart.setTitle("Function plot");

        functionChart = chart;
        System.out.println("Chart!!!!");
        return chart;
    }

    private void addRootToChart(Expression f, Double root, String arg, LineChart<Number, Number> chart, String rootName) {
        if (root != null && !Double.isNaN(root)) {
            f.setArgumentValue(arg, root);
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.getData().add(new XYChart.Data<>(root, f.calculate()));
            series.setName(rootName);
            chart.getData().add(series);
        }
    }

    public void clearPointsList() {
        pointsList.clear();
    }
}
