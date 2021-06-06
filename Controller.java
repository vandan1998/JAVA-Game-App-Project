package com.internshala.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {
	private static final int Column = 7;
	private static final int Row = 6;
	private static final int Circle_Diameter = 80;
	private static final String discColour1 = "#24303E";
	private static final String discColour2 = "#4CAA88";

    private boolean isPlayerOneTurn = true;
    private Disc[][] insertedDiscsArray = new Disc[Row][Column]; //Structural changes:for developer

	@FXML
	public GridPane rootGridPane;
	@FXML
	public Pane insertedDiscPane;
	@FXML
	public Label playernameLabel;
	@FXML
	public Button setNamesButton;
	@FXML
	public TextField playerOneTextField , playerTwoTextField;

	private boolean isAllowedToInsert = true; // flqg to avoid same colour discs being added
	private String Player_One;
	private String Player_Two;

	public void CreatePlayground()
	{
		Shape rectangleWithHoles = gameStructuralGrid();
		rootGridPane.add(rectangleWithHoles,0,1);

		List<Rectangle> rectangleList = createClickAbleColumn();
		for (Rectangle rectangle: rectangleList) {
			rootGridPane.add(rectangle,0,1);
		}
		setNamesButton.setOnAction(event -> {
			setNames();
		});
	}

	private void setNames() {
		Player_One  = playerOneTextField.getText();
		Player_Two  = playerTwoTextField.getText();
	}

	private Shape gameStructuralGrid()
	{
		Shape rectangleWithHoles = new Rectangle((Column+1)*Circle_Diameter,(Row+1)*Circle_Diameter);
		for (int row= 0; row<Row; row++) {
			for(int col=0; col<Column; col++){
				Circle circle = new Circle();
				circle.setRadius(Circle_Diameter/2);
				circle.setCenterX(Circle_Diameter/2);
				circle.setCenterY(Circle_Diameter/2);
				circle.setSmooth(true); //for smooth edges of the cicular discs

				circle.setTranslateX(col * (Circle_Diameter + 5)+ Circle_Diameter/4);
				circle.setTranslateY(row * (Circle_Diameter + 5)+ Circle_Diameter/4);
				rectangleWithHoles = Shape.subtract(rectangleWithHoles,circle);
			}
		}
		rectangleWithHoles.setFill(Color.WHITE);
		return rectangleWithHoles;
	}
	private List<Rectangle> createClickAbleColumn()
	{
		List<Rectangle> rectangleList = new ArrayList<>();
		for (int col = 0; col <Column ; col++) {
			Rectangle rectangle = new Rectangle(Circle_Diameter,(Row+1)*Circle_Diameter);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col * (Circle_Diameter + 5) + Circle_Diameter/4);

			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

			final int column = col;
			rectangle.setOnMouseClicked(event ->{
				if(isAllowedToInsert) {
					isAllowedToInsert = false; //when disc is being dropped then no more disc will be inserted
					insertDisc(new Disc(isPlayerOneTurn), column);
				}
				});
            rectangleList.add(rectangle);
		}
		return  rectangleList;
	}

	private void insertDisc(Disc disc,int column)
	{
		int row = Row-1;
		while (row >= 0)
		{
			if(getDiscIfPresent(row,column) == null)
				break;
			row--;
		}
		if(row<0)   //if it is full,we can't insert anymore disc
			return;

		insertedDiscsArray[row][column] = disc;
		insertedDiscPane.getChildren().add(disc);
		disc.setTranslateX(column * (Circle_Diameter + 5) + Circle_Diameter/4);

		int currentRow = row;
		TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5),disc);
		translateTransition.setToY(row * (Circle_Diameter + 5)+ Circle_Diameter/4);
		translateTransition.setOnFinished(event -> {
			isAllowedToInsert = true;//finally,when disc is dropped then allow next player to drop disc
			if(gameEnded(currentRow,column))
			{
				gameOver();
				return;
			}

			isPlayerOneTurn = !isPlayerOneTurn;
			playernameLabel.setText(isPlayerOneTurn? Player_One:Player_Two);
		});
		translateTransition.play();
	}
	private void gameOver() {
		String Winner = isPlayerOneTurn? Player_One  : Player_Two;
		System.out.println("Winner is: "+Winner);
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("Winner is: " + Winner);
		alert.setContentText("Want To Play Again?? ");

		ButtonType yesBtn = new ButtonType("Yes");
		ButtonType noBtn = new ButtonType("No");
		alert.getButtonTypes().setAll(yesBtn,noBtn);

		Platform.runLater(()->{
			Optional<ButtonType> buttonClicked = alert.showAndWait();
			if(buttonClicked.isPresent() && buttonClicked.get() == yesBtn)
			{
				resetGame();
			}else
			{
				Platform.exit();
				System.exit(0);
			}
		});
	}

	public void resetGame() {
		insertedDiscPane.getChildren().clear(); // Remove all Inserted disc from Pane
		for (int row = 0; row < insertedDiscsArray.length ; row++) {
			for (int col = 0; col < insertedDiscsArray[row].length ; col++) {
				insertedDiscsArray[row][col] = null;
			}
		}
		isPlayerOneTurn = true; // let player one start the game
		playernameLabel.setText(Player_One);
		CreatePlayground(); // prepare a fresh background
	}

	private boolean gameEnded(int row,int column)
	{
		//Verticalpoints
     List<Point2D> veritcalPoints =IntStream.rangeClosed(row-3,row+3) // range of row values = 0,1,2,3,4,5
		                           .mapToObj(r ->new Point2D(r,column)) //0,3 1,3 2,3 3,3 4,3 5,3 -> Point2D
		                           .collect(Collectors.toList());
      //Horizontal points
		List<Point2D> horizontalPoints = IntStream.rangeClosed(column-3,column+3)
				                         .mapToObj(col-> new Point2D(row,col))
				                         .collect(Collectors.toList());
		Point2D startpoint1 = new Point2D(row-3,column+3);
		List<Point2D> diagonalpoint1 = IntStream.rangeClosed(0,6)
				                       .mapToObj(i->startpoint1.add(i,-i))
				                       .collect(Collectors.toList());
		Point2D startpoint2 = new Point2D(row-3,column-3);
		List<Point2D> diagonalpoint2 = IntStream.rangeClosed(0,6)
				.mapToObj(i->startpoint2.add(i,i))
				.collect(Collectors.toList());


		boolean isEnded = CheckCombinations(veritcalPoints)
				          || CheckCombinations(horizontalPoints)
				          ||CheckCombinations(diagonalpoint1)
				          ||CheckCombinations(diagonalpoint2);
		return isEnded;
	}

	private boolean CheckCombinations(List<Point2D> Points) {
		int chain=0;
		for (Point2D point2D : Points)
		{
			int rowIndexArray = (int) point2D.getX();
			int columnIndexArray = (int) point2D.getY();
			Disc disc = getDiscIfPresent(rowIndexArray,columnIndexArray);
			if(disc != null && disc.isPlayerOneMove == isPlayerOneTurn) //if the last inserted disc belongs to current player
			{
				chain++;
				if (chain == 4)
					return true;
			}
			else
				chain = 0;
		}
		return  false;
	}
	private Disc getDiscIfPresent(int row, int column)  // To Prevent ArrayIndexOutOfBound Exception
	{
		if(row>= Row || row<0 || column>=Column|| column<0) //If Row or Column is Invalid
			return  null;
		return insertedDiscsArray[row][column];
	}

	private static class Disc extends Circle
	{
		private final boolean isPlayerOneMove;
		public Disc(boolean isPlayerOneMove)
		{
			this.isPlayerOneMove = isPlayerOneMove;
			setRadius(Circle_Diameter/2);
			setCenterX(Circle_Diameter/2);
			setCenterY(Circle_Diameter/2);
			setFill(isPlayerOneMove? Color.valueOf(discColour1):Color.valueOf(discColour2));
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}
