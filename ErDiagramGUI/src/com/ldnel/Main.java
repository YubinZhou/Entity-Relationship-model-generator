package com.ldnel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class Main extends Application {
	MenuBar menubar = new MenuBar();
	Menu fileMenu = new Menu("File");

	//GUI list, text fields, labels, and buttons
	Label wordLabel = new Label("  FD:");
	TextField wordTextField = new TextField();
	Button enterButton = new Button("Add FD");
	Button deleteButton = new Button("Delete");
	Button lineButton = new Button("Add Line");
	Button normalizeButton = new Button("Normalize&Create Table");
	ArrayList<String> nodeList = new ArrayList<String>();
	double startX=0.0;
	double startY=0.0;
	double endX=0.0;
	double endY=0.0;
	Rectangle tmpRect;
	Polygon tmpPoly;
	int flag=0;
	public static int deleteFlag=0;
	int lineFlag=0;
	String line1="";
	String line2="";
	int lineChoose=0;
	private Point2D dragAnchor;
	private double initX;
	private double initY;
	HBox wordEntryBox = new HBox(); //horizontal layout box
	Group root = new Group();
	String command = "";
	double[][] linePoint = new double[2][2];
	int linePointIndex = 0;
	String[] objectType = new String[2];

	Map<Rectangle,Integer> entityMapgetIndexByEntity = new HashMap<Rectangle, Integer>();
	Map<Integer,Rectangle> entityMapgetEntityByIndex = new HashMap<Integer,Rectangle>();
	Map<Rectangle,Integer> ovalMapgetIndexByEntity = new HashMap<Rectangle, Integer>();
	Map<Integer,Rectangle> ovalMapgetEntityByIndex = new HashMap<Integer,Rectangle>();
	Map<Polygon,Integer> polygonMapgetIndexByEntity = new HashMap<Polygon, Integer>();
	Map<Integer,Polygon> polygonMapgetEntityByIndex = new HashMap<Integer,Polygon>();

	Map<Rectangle, List<Rectangle>> entityToAttributeMap = new HashMap<Rectangle, List<Rectangle>>();
	Map<Rectangle, List<Rectangle>> attributeToEntityMap = new HashMap<Rectangle, List<Rectangle>>();
	Map<Rectangle, List<Polygon>> entityToRelationMap = new HashMap<Rectangle, List<Polygon>>();
	Map<Polygon, List<Rectangle>> relationToEntityMap = new HashMap<Polygon, List<Rectangle>>();

	Map<String, Line> lineMap = new HashMap<String, Line>();
	Map<Rectangle, TextField> textFieldMap = new HashMap<Rectangle, TextField>();
	Map<Polygon, TextField> textFiledRelationMap = new HashMap<Polygon, TextField>();
	private ObservableList<FunctionalDependency> functionalDependencies = FXCollections.observableArrayList();
	private ObservableList<Attribute> attributes = FXCollections.observableArrayList();

	int objectIndex = 0;
	int[] lastTwoObjectIndex = new int[2];
	String sql = "";

	@Override
	public void start(Stage primaryStage) {
		try {
			Scene scene = new Scene(root, 1300, 600);
			primaryStage.setTitle("ErDiagram Editor");
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			menubar.getMenus().add(fileMenu);

			scene.setOnMouseClicked(new EventHandler<MouseEvent>(){
				public void handle(MouseEvent me) {
					double x = me.getSceneX();
					double y = me.getSceneY();

					if(x >= 120.0){
						if(!"line".equals(command)){
							if("entity".equals(command)){
								final Rectangle rect = createRect("entity", Color.CADETBLUE, 70, 40, 0, 0, (int)x, (int)y, false, false);				
								root.getChildren().addAll(rect);
								entityMapgetIndexByEntity.put(rect, objectIndex);
								entityMapgetEntityByIndex.put(objectIndex++, rect);						
							} else if("plo".equals(command)) {
								final Polygon plo = createDiam("", Color.CADETBLUE, false, (int)x, (int)y);
								plo.setTranslateX((int)x);
								plo.setTranslateY((int)y);
								root.getChildren().addAll(plo);
								polygonMapgetEntityByIndex.put(objectIndex, plo);
								polygonMapgetIndexByEntity.put(plo, objectIndex++);
							} else if("oval".equals(command)){
								final Rectangle oval = createRect("",Color.CADETBLUE,60,30,60,30,(int)x,(int)y, false, true);
								oval.setTranslateX((int)x);
								oval.setTranslateY((int)y);
								root.getChildren().addAll(oval);
								ovalMapgetEntityByIndex.put(objectIndex, oval);
								ovalMapgetIndexByEntity.put(oval, objectIndex++);
							}
							command = "";
						} else {
							if(linePointIndex == 1){
								if(objectType[0].equals("entity") && objectType[1].equals("oval") 
										|| objectType[0].equals("oval") && objectType[1].equals("entity")
										|| objectType[0].equals("entity") && objectType[1].equals("plo")
										|| objectType[0].equals("plo") && objectType[1].equals("entity")) {
									linePoint[linePointIndex][0] = x;
									linePoint[linePointIndex][1] = y;
									linePointIndex=0;
									final Line line = new Line();
									line.setStartX(linePoint[0][0]);
									line.setStartY(linePoint[0][1]);
									line.setEndX(linePoint[1][0]);
									line.setEndY(linePoint[1][1]);

									line.setFill(Color.CADETBLUE);
									line.setStroke(Color.CADETBLUE);
									root.getChildren().addAll(line);

									line.setOnMouseClicked(new EventHandler<MouseEvent>(){
										public void handle(MouseEvent me) {
											if(deleteFlag == 1) {
												root.getChildren().remove(line);
											}
										}	
									});

									System.out.println(lastTwoObjectIndex[0] + "_                                                                        " + lastTwoObjectIndex[1]);
									System.out.println(lastTwoObjectIndex[1] + "_" + lastTwoObjectIndex[0]);

									lineMap.put(lastTwoObjectIndex[0] + "_" + lastTwoObjectIndex[1], line);
									lineMap.put(lastTwoObjectIndex[1] + "_" + lastTwoObjectIndex[0], line);

									if(objectType[0].equals("entity") && objectType[1].equals("oval")){
										entityMapgetEntityByIndex.get(lastTwoObjectIndex[0]);
										ovalMapgetEntityByIndex.get(lastTwoObjectIndex[1]);

										if(!entityToAttributeMap.containsKey(entityMapgetEntityByIndex.get(lastTwoObjectIndex[0]))) {
											List<Rectangle> list = new ArrayList<Rectangle>();
											list.add(ovalMapgetEntityByIndex.get(lastTwoObjectIndex[1]));
											entityToAttributeMap.put(entityMapgetEntityByIndex.get(lastTwoObjectIndex[0]), list);
										} else {
											entityToAttributeMap.get(entityMapgetEntityByIndex.get(lastTwoObjectIndex[0])).add(ovalMapgetEntityByIndex.get(lastTwoObjectIndex[1]));
										}

										if(!attributeToEntityMap.containsKey(ovalMapgetEntityByIndex.get(lastTwoObjectIndex[1]))){
											List<Rectangle> list = new ArrayList<Rectangle>();
											list.add(entityMapgetEntityByIndex.get(lastTwoObjectIndex[0]));
											attributeToEntityMap.put(ovalMapgetEntityByIndex.get(lastTwoObjectIndex[1]), list);
										} else {
											attributeToEntityMap.get(ovalMapgetEntityByIndex.get(lastTwoObjectIndex[1])).add(ovalMapgetEntityByIndex.get(lastTwoObjectIndex[0]));
										}
									} else if(objectType[0].equals("oval") && objectType[1].equals("entity")){
										ovalMapgetEntityByIndex.get(lastTwoObjectIndex[0]);
										entityMapgetEntityByIndex.get(lastTwoObjectIndex[1]);

										if(!entityToAttributeMap.containsKey(entityMapgetEntityByIndex.get(lastTwoObjectIndex[1]))) {
											List<Rectangle> list = new ArrayList<Rectangle>();
											list.add(ovalMapgetEntityByIndex.get(lastTwoObjectIndex[0]));
											entityToAttributeMap.put(entityMapgetEntityByIndex.get(lastTwoObjectIndex[1]), list);
										} else {
											entityToAttributeMap.get(entityMapgetEntityByIndex.get(lastTwoObjectIndex[1])).add(ovalMapgetEntityByIndex.get(lastTwoObjectIndex[0]));
										}

										if(!attributeToEntityMap.containsKey(ovalMapgetEntityByIndex.get(lastTwoObjectIndex[0]))){
											List<Rectangle> list = new ArrayList<Rectangle>();
											list.add(entityMapgetEntityByIndex.get(lastTwoObjectIndex[1]));
											attributeToEntityMap.put(ovalMapgetEntityByIndex.get(lastTwoObjectIndex[0]), list);
										} else {
											attributeToEntityMap.get(ovalMapgetEntityByIndex.get(lastTwoObjectIndex[0])).add(ovalMapgetEntityByIndex.get(lastTwoObjectIndex[1]));
										}
									} else if(objectType[0].equals("entity") && objectType[1].equals("plo")){
										entityMapgetEntityByIndex.get(lastTwoObjectIndex[0]);
										polygonMapgetEntityByIndex.get(lastTwoObjectIndex[1]);

										if(!entityToRelationMap.containsKey(entityMapgetEntityByIndex.get(lastTwoObjectIndex[0]))) {
											List<Polygon> list = new ArrayList<Polygon>();
											list.add(polygonMapgetEntityByIndex.get(lastTwoObjectIndex[1]));
											entityToRelationMap.put(entityMapgetEntityByIndex.get(lastTwoObjectIndex[0]), list);
										} else {
											entityToRelationMap.get(entityMapgetEntityByIndex.get(lastTwoObjectIndex[0])).add(polygonMapgetEntityByIndex.get(lastTwoObjectIndex[1]));
										}

										if(!relationToEntityMap.containsKey(polygonMapgetEntityByIndex.get(lastTwoObjectIndex[1]))) {
											List<Rectangle> list = new ArrayList<Rectangle>();
											list.add(entityMapgetEntityByIndex.get(lastTwoObjectIndex[0]));
											relationToEntityMap.put(polygonMapgetEntityByIndex.get(lastTwoObjectIndex[1]), list);
										} else {
											relationToEntityMap.get(polygonMapgetEntityByIndex.get(lastTwoObjectIndex[1])).add(entityMapgetEntityByIndex.get(lastTwoObjectIndex[0]));
										}
									} else if(objectType[0].equals("plo") && objectType[1].equals("entity")){
										polygonMapgetEntityByIndex.get(lastTwoObjectIndex[0]);
										entityMapgetEntityByIndex.get(lastTwoObjectIndex[1]);

										if(!entityToRelationMap.containsKey(entityMapgetEntityByIndex.get(lastTwoObjectIndex[1]))) {
											List<Polygon> list = new ArrayList<Polygon>();
											list.add(polygonMapgetEntityByIndex.get(lastTwoObjectIndex[0]));
											entityToRelationMap.put(entityMapgetEntityByIndex.get(lastTwoObjectIndex[1]), list);
										} else {
											entityToRelationMap.get(entityMapgetEntityByIndex.get(lastTwoObjectIndex[1])).add(polygonMapgetEntityByIndex.get(lastTwoObjectIndex[0]));
										}

										if(!relationToEntityMap.containsKey(polygonMapgetEntityByIndex.get(lastTwoObjectIndex[0]))) {
											List<Rectangle> list = new ArrayList<Rectangle>();
											list.add(entityMapgetEntityByIndex.get(lastTwoObjectIndex[1]));
											relationToEntityMap.put(polygonMapgetEntityByIndex.get(lastTwoObjectIndex[0]), list);
										} else {
											relationToEntityMap.get(polygonMapgetEntityByIndex.get(lastTwoObjectIndex[0])).add(entityMapgetEntityByIndex.get(lastTwoObjectIndex[1]));
										}
									}
								}

								linePointIndex = 0;
								command = "";
							} else {
								linePoint[linePointIndex][0] = x;
								linePoint[linePointIndex][1] = y;
								linePointIndex++;
							}

						}
					} 
				}
			});

			//add menu bar object to application scene root
			root.getChildren().add(menubar); //add menubar to GUI
			buildMenus(primaryStage); //add menu items to menus
			primaryStage.show();

			wordEntryBox.setSpacing(20); //space elements
			wordEntryBox.setAlignment(Pos.TOP_LEFT);
			wordTextField.setPrefWidth(450);
			wordEntryBox.setTranslateY(30);
			wordEntryBox.getChildren().addAll(wordLabel, wordTextField, enterButton, deleteButton,lineButton,normalizeButton);
			root.getChildren().addAll(wordEntryBox);

			Rectangle rec = createRect("",Color.CADETBLUE, 70,40,0,0,10,100,true, false);
			root.getChildren().addAll(rec);

			Polygon plo = createDiam("", Color.CADETBLUE,true, 0, 0);
			plo.setTranslateX(10);
			plo.setTranslateY(190);
			root.getChildren().addAll(plo);

			Rectangle test = createRect("",Color.CADETBLUE,60,30,60,30,300,150, true, true);
			test.setTranslateX(10);
			test.setTranslateY(230);
			root.getChildren().addAll(test);
			Line line = new Line();
			line.setStartX(100);
			line.setStartY(52);
			line.setEndX(100);
			line.setEndY(600);

			line.setFill(Color.CADETBLUE);
			line.setStroke(Color.CADETBLUE);
			root.getChildren().addAll(line);


			Line lineRow = new Line();
			lineRow.setStartX(0);
			lineRow.setStartY(52);
			lineRow.setEndX(100);
			lineRow.setEndY(52);

			lineRow.setFill(Color.CADETBLUE);
			lineRow.setStroke(Color.CADETBLUE);
			root.getChildren().addAll(lineRow);

			enterButton.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent e) {
					String anFDString = wordTextField.getText().trim();
					if (anFDString != null && anFDString.length() > 0) {
						FunctionalDependency fd = Normalizer.parseFDString(anFDString);
						if (fd != null) addFD(fd);
					}
					wordTextField.clear(); //clear the text field
				}
			});

			deleteButton.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent e) {
					if(deleteFlag==1){
						deleteFlag=0;
						root.setCursor(Cursor.DEFAULT);
					}
					else{
						deleteFlag=1;
						root.setCursor(Cursor.CROSSHAIR);
					}
				}

			});

			normalizeButton.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent e) {
					performNormalization();
				}
			});

			lineButton.setOnAction(new EventHandler<ActionEvent>() {
				public void handle(ActionEvent e) {
					command="line";
				}

			});
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	private void addFD(FunctionalDependency anFD){
		functionalDependencies.addAll(anFD);
		rebuiltAttributesList();
	}
	private void rebuiltAttributesList(){
		//Rebuild the attributes list
		attributes.clear();
		DependencySet FDs = new DependencySet();
		//Gather all the functional dependencies
		for(FunctionalDependency fd : functionalDependencies){FDs.add(fd);}
		AttributeSet allAttributes = FDs.getAllAttributes();
		Collections.sort(allAttributes.getElements());
		for (Attribute a : allAttributes.getElements()) attributes.add(a);
	}

	public static void main(String[] args) {
		launch(args);
	}

	private void buildMenus(final Stage theStage){
		//build the menus for the menu bar

		//Build File menu items
		MenuItem aboutMenuItem = new MenuItem("About This App");
		fileMenu.getItems().addAll(aboutMenuItem);
		aboutMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				Alert alert = new Alert(Alert.AlertType.INFORMATION);
				alert.setTitle("ErEditor GUI");
				alert.setHeaderText(null);
				alert.setContentText("Ver 1.0 \u00A9 L.D. Nel 2015\nldnel@scs.carleton.ca");
				alert.showAndWait();
			}
		});

		fileMenu.getItems().addAll(new SeparatorMenuItem());

		MenuItem newMenuItem = new MenuItem("New");
		fileMenu.getItems().addAll(newMenuItem);
		newMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				root.getChildren().clear();
				nodeList.clear();
				flag=0;
				deleteFlag=0;
				lineFlag=0;
				lineChoose=0;
				root.getChildren().addAll(wordEntryBox);
				root.getChildren().addAll(menubar);
			}
		});


		MenuItem saveMenuItem = new MenuItem("Save As");
		MenuItem saveMenuItemSql = new MenuItem("Save As sql script");
		fileMenu.getItems().addAll(saveMenuItem);
		fileMenu.getItems().add(saveMenuItemSql);
		saveMenuItem.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {

				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Save As");
				String currentDirectoryProperty = System.getProperty("user.dir");
				File currentDirectory = new File(currentDirectoryProperty);
				fileChooser.setInitialDirectory(currentDirectory);
				File selectedFile = fileChooser.showSaveDialog(theStage);


				System.out.println("save to file: " + selectedFile);
				saveFile(selectedFile);

			}
		});
		saveMenuItemSql.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {

				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Save As");
				String currentDirectoryProperty = System.getProperty("user.dir");
				File currentDirectory = new File(currentDirectoryProperty);
				fileChooser.setInitialDirectory(currentDirectory);
				File selectedFile = fileChooser.showSaveDialog(theStage);


				System.out.println("save to file: " + selectedFile);
				saveFileSql(selectedFile);

			}
		});

		fileMenu.getItems().addAll(new SeparatorMenuItem());
	}

	private void saveFile(File aFile){
		System.out.println("saveFile()");

		if(aFile == null) return;
		//save the chartModel to disk
		PrintWriter outputFileStream = null;

		try{
			outputFileStream = new PrintWriter(new FileWriter(aFile));
			for(FunctionalDependency fd : functionalDependencies){
				outputFileStream.println(fd);
			}
			outputFileStream.close();

		} catch (FileNotFoundException e) {
			System.out.println("Error: Cannot open file" + outputFileStream + " for writing.");

		} catch (IOException e) {
			System.out.println("Error: Cannot write to file: " + outputFileStream);

		}
	}

	private void saveFileSql(File aFile){
		System.out.println("saveFileSql()");

		if(aFile == null) return;
		//save the chartModel to disk
		PrintWriter outputFileStream = null;

		try{
			System.out.println(sql);
			outputFileStream = new PrintWriter(new FileWriter(aFile));
			outputFileStream.println(sql);
			outputFileStream.close();

		} catch (FileNotFoundException e) {
			System.out.println("Error: Cannot open file" + outputFileStream + " for writing.");

		} catch (IOException e) {
			System.out.println("Error: Cannot write to file: " + outputFileStream);

		}
	}

	private Rectangle createRect(final String name, final Color color, int x,int y,int ax,int ay,int tx,int ty, final boolean isMenu, final boolean isOval) {
		final Rectangle rect = new Rectangle();
		final TextField text = new TextField();
		text.setPrefWidth(70);
		text.setMaxWidth(100);
		text.setText(name);
		text.setTranslateX(tx);
		text.setTranslateY(ty - 20);

		if(!isMenu){
			root.getChildren().addAll(text);
		}

		rect.setTranslateX(tx);
		rect.setTranslateY(ty);
		rect.setWidth(x);
		rect.setHeight(y);
		rect.setFill(color);
		rect.setCursor(Cursor.HAND);
		rect.setArcHeight(ay);
		rect.setArcWidth(ax);
		textFieldMap.put(rect, text);

		if(!isMenu) {
			rect.setOnMouseDragged(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent me) {
					double dragX = me.getSceneX() - dragAnchor.getX();
					double dragY = me.getSceneY() - dragAnchor.getY();
					double newXPosition = initX + dragX;
					double newYPosition = initY + dragY;
					//if new position do not exceeds borders of the rectangle, translate to this position

					//if ((newXPosition>=rect.getWidth()/2) && (newXPosition<=950-rect.getWidth()/2)) {
					rect.setTranslateX(newXPosition);
					text.setTranslateX(newXPosition);
					//}

					//if ((newYPosition>=rect.getHeight()/2) && (newYPosition<=500-rect.getHeight()/2)){
					rect.setTranslateY(newYPosition);
					text.setTranslateY(newYPosition - 20);

					if(entityMapgetIndexByEntity.containsKey(rect)){
						int rectIndex = entityMapgetIndexByEntity.get(rect);
						if(entityToAttributeMap.containsKey(rect)){
							List<Rectangle> attributeList = entityToAttributeMap.get(rect);
							for(Iterator<Rectangle> it = attributeList.iterator(); it.hasNext();) {
								Rectangle rect = it.next();
								int attributeIndex = ovalMapgetIndexByEntity.get(rect);
								Line line = lineMap.get(rectIndex + "_" + attributeIndex);
								line.setStartX(rect.getTranslateX() + rect.getWidth() / 2);
								line.setStartY(rect.getTranslateY() + rect.getHeight() / 2);

								line.setEndX(newXPosition + 20);
								line.setEndY(newYPosition);
							}
						}
						if(entityToRelationMap.containsKey(rect)) {
							List<Polygon> polygonList = entityToRelationMap.get(rect);
							for(Iterator<Polygon> it = polygonList.iterator();it.hasNext();) {
								Polygon pologon = it.next();
								int pologonIndex = polygonMapgetIndexByEntity.get(pologon);
								Line line = lineMap.get(rectIndex + "_" + pologonIndex);

								line.setStartX(pologon.getTranslateX());
								line.setStartY(pologon.getTranslateY());

								line.setEndX(newXPosition + 20);
								line.setEndY(newYPosition);
							}
						}
					} else if(ovalMapgetIndexByEntity.containsKey(rect)) {
						int ovalIndex = ovalMapgetIndexByEntity.get(rect);
						if(attributeToEntityMap.containsKey(rect)){
							List<Rectangle> entityList = attributeToEntityMap.get(rect);
							for(Iterator<Rectangle> it = entityList.iterator(); it.hasNext();) {
								Rectangle rect = it.next();
								int rectIndex = entityMapgetIndexByEntity.get(rect);
								Line line = lineMap.get(rectIndex + "_" + ovalIndex);
								line.setStartX(rect.getTranslateX() + rect.getWidth() / 2);
								line.setStartY(rect.getTranslateY() + rect.getHeight() / 2);

								line.setEndX(newXPosition + 20);
								line.setEndY(newYPosition);
							}
						}
					}
					System.out.println(name + " was dragged (x:" + newXPosition + ", y:" + newYPosition +")"); }
			});

		}

		rect.setOnMouseEntered(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				//rect.toFront();
				System.out.println("Mouse entered " + name);
			}
		});
		rect.setOnMouseExited(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				System.out.println("Mouse exited " +name);
			}
		});
		rect.setOnMousePressed(new EventHandler<MouseEvent>() { public void handle(MouseEvent me) {
			//when mouse is pressed, store initial position
			initX = rect.getTranslateX();
			initY = rect.getTranslateY();
			dragAnchor = new Point2D(me.getSceneX(), me.getSceneY());
			System.out.println("Mouse pressed above " + name);
		}
		});
		rect.setOnMouseReleased(new EventHandler<MouseEvent>() { public void handle(MouseEvent me) {
			System.out.println("Mouse released above " +name);
		}
		});
		rect.setOnMouseClicked(new EventHandler<MouseEvent>(){public void handle(MouseEvent me) {
			if(isMenu){
				if(isOval)
					command="oval";
				else {
					command = "entity";
				}
			}else {
				if(isOval) {
					objectType[linePointIndex] = "oval";
					lastTwoObjectIndex[linePointIndex] = ovalMapgetIndexByEntity.get(rect);
					System.out.println(ovalMapgetIndexByEntity.get(rect));
				}
				else { 
					objectType[linePointIndex] = "entity";
					lastTwoObjectIndex[linePointIndex] = entityMapgetIndexByEntity.get(rect);
					System.out.println(entityMapgetIndexByEntity.get(rect));
				}

				if(deleteFlag==1){
					root.getChildren().removeAll(rect);
					root.getChildren().removeAll(text);
					if(isOval){
						if(attributeToEntityMap.containsKey(rect)){
							List<Rectangle> entityList = attributeToEntityMap.get(rect);
							for(Iterator<Rectangle> it = entityList.iterator();it.hasNext();){
								Rectangle rec = it.next();
								root.getChildren().remove(lineMap.get(entityMapgetIndexByEntity.get(rec) + "_" + ovalMapgetIndexByEntity.get(rect)));
								lineMap.remove(entityMapgetIndexByEntity.get(rec) + "_" + ovalMapgetIndexByEntity.get(rect));

							}
						}
					} else {
						if(entityToAttributeMap.containsKey(rect)){
							List<Rectangle> entityList = entityToAttributeMap.get(rect);
							for(Iterator<Rectangle> it = entityList.iterator();it.hasNext();){
								Rectangle rec = it.next();
								System.out.println(ovalMapgetIndexByEntity.get(rec) + "_" + entityMapgetIndexByEntity.get(rect));
								root.getChildren().remove(lineMap.get(ovalMapgetIndexByEntity.get(rec) + "_" + entityMapgetIndexByEntity.get(rect)));
								lineMap.remove(ovalMapgetIndexByEntity.get(rec) + "_" + entityMapgetIndexByEntity.get(rect));

							}
						}

						if(entityToRelationMap.containsKey(rect)){
							List<Polygon> polygonList = entityToRelationMap.get(rect);
							for(Iterator<Polygon> it = polygonList.iterator(); it.hasNext();){
								Polygon plo = it.next();
								root.getChildren().remove(lineMap.get(polygonMapgetIndexByEntity.get(plo) + "_" + entityMapgetIndexByEntity.get(rect)));
								lineMap.remove(polygonMapgetIndexByEntity.get(plo) + "_" + entityMapgetIndexByEntity.get(rect));
							}
						}
					}
					deleteFlag=0;
					int deleteNum=0;
					root.setCursor(Cursor.DEFAULT);
				}
				else{
					if(lineFlag==1){
						if(rect.getFill()==Color.CADETBLUE){
							if(lineChoose==1){
								line2=name;
								if(flag==1){
									tmpRect.setFill(Color.CADETBLUE);
								}
								else{
									tmpPoly.setFill(Color.CADETBLUE);
								}
								boolean lineExist = false;
								lineChoose=0;
								lineFlag=0;
								root.setCursor(Cursor.DEFAULT);
							}
							else{
								lineChoose=1;
								line1=name;
								tmpRect=rect;
								flag=1;
								startX=initX+30;
								startY=initY+15;
								rect.setFill(Color.CHARTREUSE);
							}
						}
						else{
							lineChoose=0;
							rect.setFill(Color.CADETBLUE);
						}
					}
				}
			}

		}
		});
		return rect;
	}

	private void performNormalization() {
		DependencySet FDs = new DependencySet();
		//Gather all the functional dependencies
		for(FunctionalDependency fd : functionalDependencies){FDs.add(fd);}
		//print all the attributes
		AttributeSet allAttributes = FDs.getAllAttributes();
		Collections.sort(allAttributes.getElements());
		//allAttributes.printToSystemOut();

		//print all the attributes
		System.out.println("ATTRIBUTES:");
		for(Attribute att : allAttributes.getElements())
			System.out.println(att.toString());
		System.out.println("==================================================");


		//print all the functional dependencies created from data file
		System.out.println("FUNCTIONAL DEPENDENCIES:");
		System.out.println(FDs);
		System.out.println("==================================================");

		DependencySet minCover = FDs.minCover();

		System.out.println("==================================================");
		System.out.println("Minimal Cover:");
		System.out.println(minCover);
		DependencySet toMerge = minCover.copy();
		DependencySet newMinCover = new DependencySet();

		while(!toMerge.isEmpty()){
			FunctionalDependency fd = toMerge.getElements().get(0);
			toMerge.remove(fd);
			minCover.remove(fd);
			newMinCover.add(fd);
			for(FunctionalDependency fd2 : toMerge.getElements()){
				if(fd.getLHS().equals(fd2.getLHS())) {
					fd.getRHS().addAll(fd2.getRHS());
					minCover.remove(fd2);
				}
			}
			toMerge = minCover.copy();
		}

		minCover = newMinCover;

		//Minimal Cover with LHS's merged
		System.out.println("============================");
		System.out.println("MINIMAL COVER: MERGED LHS");

		System.out.println(minCover);
		//check that minimal cover and original FD's are in fact equivalent
		/*
        if(FDs.equals(minCover))
            System.out.println("FD Sets are Equivalent");
        else
            System.out.println("FD Sets are NOT Equivalent");
		 */

		//find all the candidate keys of a table consisting of all
		//the attributes with respect to the functional dependencies
		System.out.println("\n-------------------------------------------------------------");
		System.out.println("CANDIDATE KEY FOR ALL ATTRIBUTES:");
		AttributeSet candidateKey = allAttributes.findCandidateKey(minCover);
		System.out.println(candidateKey.toString());

		System.out.println("\n-------------------------------------------------------------");
		System.out.println("ALL CANDIDATE KEYS (FOR SMALL EXAMPLES ONLY):");

		SetOfAttributeSets candidateKeys = allAttributes.allCandidateKeys(minCover);
		if(candidateKeys != null)
			for (AttributeSet aKey : candidateKeys.getElements())
				System.out.println(aKey.toString());

		//Create Dependency Preserving 3NF tables
		/*
		 * This is the 3 step algorithm 16.4 presented in
		 * Elmasri and Navathe 6th ed. Which decomposes a set of attributes
		 * (universal relation) with respect to functional dependencies F.
		 *
		 *  Step 1) Find a minimal cover Fm of F
		 *
		 *  Step 2) For each left hand side X of FD in Fm
		 *  create with columns X U A1 U A2 U ...An where
		 *  X->A1, X->A2,... X->An are all the dependencies in Fm
		 *  with left hand side X
		 *
		 *  Step 3) Place any attributes not found in F in a table
		 *  of their own. (Note, that does not happen in this code since
		 *  the attributes are obtained from the functional dependencies on input.
		 */
		System.out.println("\n=======================================================");
		System.out.println("Dependency Preserving, 3NF tables");

		//Step 1: already done above
		//Step 2:
		ArrayList<Relation> database_3nf_dep_preserving = new ArrayList<Relation>();
		for(FunctionalDependency fd : minCover.getElements()){
			Relation table = new Relation(fd);
			database_3nf_dep_preserving.add(table);
		}
		//Step 3:
		AttributeSet minCoverAttributes = minCover.getAllAttributes();
		AttributeSet leftOverAttributes = new AttributeSet();
		for(Attribute a : allAttributes.getElements())
			if(!minCoverAttributes.contains(a)) leftOverAttributes.add(a);
		if(!leftOverAttributes.isEmpty()){
			Relation tableOfLeftOverAttributes = new Relation(leftOverAttributes,leftOverAttributes);
			database_3nf_dep_preserving.add(tableOfLeftOverAttributes);
		}

		for(Relation r : database_3nf_dep_preserving)
			System.out.println(r.toString());

		System.out.println("\n=======================================================");
		System.out.println("Lossless-Join, Dependency Preserving, 3NF tables");

		//Create Lossless-Join, Dependency Preserving 3NF tables
		/*
		 * This is based on the 4 step algorithm 16.6 presented in
		 * Elmasri and Navathe 6th ed. Which decomposes a set of attributes
		 * (universal relation) with respect to functional dependencies F.
		 *
		 *  Step 1) Find a minimal cover Fm of F
		 *
		 *  Step 2) For each left hand side X of FD in Fm
		 *  create with columns X U A1 U A2 U ...An where
		 *  X->A1, X->A2,... X->An are all the dependencies in Fm
		 *  with left hand side X
		 *
		 *  Step 3) If none of the tables created in Step 2 contains a
		 *  candidate key for the universal relation consisting of all the
		 *  attributes, then create a table consisting of a candidate key
		 *
		 *  Step 4) Remove redundant tables. If any table is a projection of another (has all its columns
		 *  appearing in another tables, then remove that table from the decomposition
		 */


		//Step 1 & 2
		ArrayList<Relation> database_3nf_lossless_join_dep_preserving = new ArrayList<Relation>();
		for(FunctionalDependency fd : minCover.getElements()) {
			Relation table = new Relation(fd);
			database_3nf_lossless_join_dep_preserving.add(table);
		}

		//Step 3: Ensure decomposition contains a key for an imaginary table
		//        consisting of all the attributes
		boolean keyFound = false;
		for (Relation table : database_3nf_lossless_join_dep_preserving){
			AttributeSet columns = table.getAttributes();
			if(columns.containsAll(candidateKey)) {
				keyFound = true;
				break;
			}

		}
		if(!keyFound)
			database_3nf_lossless_join_dep_preserving.add(new Relation(candidateKey,candidateKey));

		//Step 4: Remove any redundant tables
		//A table is redundant if all of its attributes appears in some other table.

		Relation redunantTable = null;
		while((redunantTable = Normalizer.findRedunantTable(database_3nf_lossless_join_dep_preserving)) != null){
			database_3nf_lossless_join_dep_preserving.remove(redunantTable);
			System.out.println("\nRemoving Redundant table: " + redunantTable);
		}

		for(Relation r : database_3nf_lossless_join_dep_preserving)
			System.out.println(r.toString());


		Map<String, String> primaryKeyMap = new HashMap<String, String>();
		Map<String, String> relationShipMap = new HashMap<String, String>();
		Map<String, Boolean> isTableExist = new HashMap<String, Boolean>();

		for(Iterator<Entry<Rectangle, List<Rectangle>>> it = entityToAttributeMap.entrySet().iterator(); it.hasNext(); ){
			Entry<Rectangle, List<Rectangle>> entry = it.next();
			Rectangle entityOne = entry.getKey();
			List<Rectangle> attributeOneList = entry.getValue();

			String[] entityOneInfos = textFieldMap.get(entityOne).getText().split(":");
			String tableName = entityOneInfos[entityOneInfos.length - 1];
			for(Iterator<Rectangle> itAttribute = attributeOneList.iterator(); itAttribute.hasNext();) {
				String[] attributeInfos = textFieldMap.get(itAttribute.next()).getText().split(":");
				if(attributeInfos[0].equalsIgnoreCase("p")){
					primaryKeyMap.put(tableName, attributeInfos[1]);
				}
			}
		}

		for(Iterator<Entry<Rectangle, List<Rectangle>>> it = entityToAttributeMap.entrySet().iterator(); it.hasNext(); ){
			Entry<Rectangle, List<Rectangle>> entry = it.next();
			Rectangle entityOne = entry.getKey();
			String[] entityOneInfos = textFieldMap.get(entityOne).getText().split(":");
			int lengthOne = entityOneInfos.length;
			List<Rectangle> attributeOneList = entry.getValue();
			List<Polygon> relationShipList = entityToRelationMap.get(entityOne);

			StringBuffer sbTableOne = new StringBuffer();
			sbTableOne.append("create table " + entityOneInfos[lengthOne - 1] + "(\n");
			for(Iterator<Rectangle> ite = attributeOneList.iterator(); ite.hasNext();) {
				String[] attributeInfos = textFieldMap.get(ite.next()).getText().split(":");
				if(attributeInfos.length == 1)
					sbTableOne.append("\t" + attributeInfos[0] + " varchar(20),\n");
				else {
					if(attributeInfos[0].equalsIgnoreCase("p")){
						sbTableOne.append("\t" + attributeInfos[1] + " varchar(20) primary key,\n");
					}
				} 
			}
			String sqlTemp = sbTableOne.substring(0,sbTableOne.length() - 2);
			sqlTemp += "\n);\n";
			sql += sqlTemp;
			DBHelper dbOne = new DBHelper(sqlTemp);
			dbOne.close();

			for(Iterator<Polygon> itPoly = relationShipList.iterator(); itPoly.hasNext();) {
				Polygon relation = itPoly.next();
				List<Rectangle> entityList = relationToEntityMap.get(relation);
				for (Iterator<Rectangle> itEntity = entityList.iterator(); itEntity.hasNext();) {
					Rectangle entityTwo = itEntity.next();
					List<Rectangle> attributeTwoList = entityToAttributeMap.get(entityTwo);
					String[] entityTwoInfos = textFieldMap.get(entityTwo).getText().split(":");
					String[] relationInfos = textFiledRelationMap.get(relation).getText().split(":");

					double entityOneX = entityOne.getTranslateX();
					double entityTwoX = entityTwo.getTranslateX();
					int lengthTwo = entityTwoInfos.length;

					if(!entityOneInfos[lengthOne - 1].equals(entityTwoInfos[lengthTwo - 1])) {
						if(relationInfos[0].equalsIgnoreCase("n") && relationInfos[1].equalsIgnoreCase("n")) {
							relationShipMap.put(entityOneInfos[lengthOne - 1] + "_" + entityTwoInfos[lengthTwo - 1], "n_n");
						} else if(relationInfos[0].equalsIgnoreCase("n") && relationInfos[1].equalsIgnoreCase("1")) {
							if(entityOneX < entityTwoX) {
								relationShipMap.put(entityOneInfos[lengthOne - 1] + "_" + entityTwoInfos[lengthTwo - 1], "n_1");
							} else if(entityOneX > entityTwoX) {
								relationShipMap.put(entityOneInfos[lengthOne - 1] + "_" + entityTwoInfos[lengthTwo - 1], "1_n");
							}
						} else if(relationInfos[0].equalsIgnoreCase("1") && relationInfos[1].equalsIgnoreCase("n")) {
							if(entityOneX < entityTwoX) {
								relationShipMap.put(entityOneInfos[lengthOne - 1] + "_" + entityTwoInfos[lengthTwo - 1], "1_n");
							} else if(entityOneX > entityTwoX) {
								relationShipMap.put(entityOneInfos[lengthOne - 1] + "_" + entityTwoInfos[lengthTwo - 1], "n_1");
							}
						} 
					}
				}
			}
		}

		for(Iterator<Entry<String, String>> ite = relationShipMap.entrySet().iterator(); ite.hasNext();) {
			Entry<String, String> entryRelationStr = ite.next();
			String key = entryRelationStr.getKey();
			String value = entryRelationStr.getValue();

			String[] keys = key.split("_");
			String[] values = value.split("_");

			if(!isTableExist.containsKey(keys[0] + "_" + keys[1]) && !isTableExist.containsKey(keys[1] + "_" + keys[0])) {
				if(values[0].equalsIgnoreCase("n") && values[1].equalsIgnoreCase("n")) {
					String primaryKeyOne = primaryKeyMap.get(keys[0]);
					String primaryKeyTwo = primaryKeyMap.get(keys[1]);
					StringBuffer sbTwo = new StringBuffer();
					sbTwo.append("create table " + key + "(\n");
					sbTwo.append("\t" + keys[0] + "_" + primaryKeyOne + " varchar(20),\n");
					sbTwo.append("\t" + keys[1] + "_" + primaryKeyTwo + " varchar(20),\n");
					String sqlTemp;
					sqlTemp = sbTwo.substring(0,sbTwo.length() - 2);
					sqlTemp += "\n);\n";
					sql += sqlTemp;
					DBHelper dbTwo = new DBHelper(sqlTemp);
					dbTwo.close();
					isTableExist.put(key, true);
				} else if(values[0].equalsIgnoreCase("n") && values[1].equalsIgnoreCase("1")){
						String primaryKeyTwo = primaryKeyMap.get(keys[1]);
						StringBuffer sbTwo = new StringBuffer();
						sbTwo.append("alter table " + keys[0] +" add column " + keys[1] + "_" + primaryKeyTwo + " varchar(20)");
						sql += sbTwo.toString() + "\n";
						DBHelper dbTwo = new DBHelper(sbTwo.toString());
						dbTwo.close();
						
						/*String sqlTemp = "alter table " + keys[0] + " add foreign key " + keys[1] + "_" + primaryKeyTwo + " references " + keys[1] + "('"+  primaryKeyTwo +"')";
						//alter table key[0] add foreign key locstock_ibfk2(stockid) references product(stockid)
						System.out.println(sqlTemp);
						DBHelper dbThree = new DBHelper(sqlTemp);
						dbThree.close();*/
						isTableExist.put(keys[0] + "_"  + keys[1], true);
				
				} else if(values[0].equalsIgnoreCase("1") && values[1].equalsIgnoreCase("n")){
						String primaryKeyOne = primaryKeyMap.get(keys[0]);
						StringBuffer sbTwo = new StringBuffer();
						sbTwo.append("alter table " + keys[1] +" add column " + keys[0] + "_" + primaryKeyOne + " varchar(20)");
						DBHelper dbTwo = new DBHelper(sbTwo.toString());
						sql += sbTwo.toString() + "\n";
						dbTwo.close();
						isTableExist.put(keys[0] + "_"  + keys[1], true);
				}
			}
		} 
	}

	private Polygon createDiam(String name, final Color color,final boolean isMenu, int tx,int ty) {
		final Polygon poly = new Polygon();
		final TextField text = new TextField();
		text.setText(name);
		text.setTranslateX(tx);
		text.setTranslateY(ty-40);
		text.setPrefWidth(60);

		if(!isMenu){
			root.getChildren().addAll(text);
		}

		textFiledRelationMap.put(poly, text);
		poly.getPoints().addAll(new Double[]{
				0.0, 0.0,
				30.0, 15.0,
				60.0, 0.0,
				30.0, -15.0});
		poly.setFill(color);
		poly.setCursor(Cursor.HAND);

		if(!isMenu){
			poly.setOnMouseDragged(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent me) {

					double dragX = me.getSceneX() - dragAnchor.getX();
					double dragY = me.getSceneY() - dragAnchor.getY();
					double newXPosition = initX + dragX;
					double newYPosition = initY + dragY;
					//if new position do not exceeds borders of the rectangle, translate to this position
					//if ((newXPosition>=60/2) && (newXPosition<=950-60/2)) {
					poly.setTranslateX(newXPosition);
					text.setTranslateX(newXPosition);
					//}

					//if ((newYPosition>=30/2) && (newYPosition<=500-30/2)){
					poly.setTranslateY(newYPosition);
					text.setTranslateY(newYPosition - 40);
					//}
					int polyIndex = polygonMapgetIndexByEntity.get(poly);
					if(relationToEntityMap.containsKey(poly)){
						List<Rectangle> entityList = relationToEntityMap.get(poly);
						for(Iterator<Rectangle> it = entityList.iterator(); it.hasNext();) {
							Rectangle rect = it.next();
							int rectIndex = entityMapgetIndexByEntity.get(rect);
							Line line = lineMap.get(rectIndex + "_" + polyIndex);
							line.setStartX(rect.getTranslateX() + rect.getWidth() / 2);
							line.setStartY(rect.getTranslateY() + rect.getHeight() / 2);
							line.setEndX(newXPosition + 20);
							line.setEndY(newYPosition);
						}
					}
				}
			});
		}

		poly.setOnMouseEntered(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				//poly.toFront();
				System.out.println("Mouse entered");
			}
		});
		poly.setOnMouseExited(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent me) {
				System.out.println("Mouse exited ");
			}
		});
		poly.setOnMousePressed(new EventHandler<MouseEvent>() { public void handle(MouseEvent me) {
			//when mouse is pressed, store initial position
			initX = poly.getTranslateX();
			initY = poly.getTranslateY();
			dragAnchor = new Point2D(me.getSceneX(), me.getSceneY());
			System.out.println("Mouse pressed above ");
		}
		});
		poly.setOnMouseReleased(new EventHandler<MouseEvent>() { public void handle(MouseEvent me) {
			System.out.println("Mouse released above ");
		}
		});
		poly.setOnMouseClicked(new EventHandler<MouseEvent>(){public void handle(MouseEvent me) {
			if(isMenu){
				command = "plo";
			}else{
				objectType[linePointIndex] = "plo";
				lastTwoObjectIndex[linePointIndex] = polygonMapgetIndexByEntity.get(poly);
				System.out.println(polygonMapgetIndexByEntity.get(poly));

				if(deleteFlag==1){
					root.getChildren().removeAll(poly);
					deleteFlag=0;
					List<Rectangle> entityList = relationToEntityMap.get(poly);
					for(Iterator<Rectangle> it = entityList.iterator(); it.hasNext();){
						Rectangle rect = it.next();
						root.getChildren().remove(lineMap.get(polygonMapgetIndexByEntity.get(poly) + "_" + entityMapgetIndexByEntity.get(rect)));
						lineMap.remove(polygonMapgetIndexByEntity.get(poly) + "_" + entityMapgetIndexByEntity.get(rect));
					}
					root.setCursor(Cursor.DEFAULT);
				}
				else{
					if(lineFlag==1){
						if(poly.getFill()==Color.CADETBLUE){
							if(lineChoose==1){
								if(flag==1){
									tmpRect.setFill(Color.CADETBLUE);
								}
								else{
									tmpPoly.setFill(Color.CADETBLUE);
								}
								boolean lineExist = false;
								lineChoose=0;
								lineFlag=0;
								root.setCursor(Cursor.DEFAULT);
							}
							else{
								lineChoose=1;
								tmpPoly=poly;
								startX=initX+30;
								startY=initY;
								flag=2;
								poly.setFill(Color.CHARTREUSE);
							}
						}
						else{
							lineChoose=0;
							poly.setFill(Color.CADETBLUE);
						}
					}
				}
			}

		}
		});
		return poly;
	}
}
