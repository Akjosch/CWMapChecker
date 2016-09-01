package de.vernideas.cwmapchecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import de.vernideas.cwmapchecker.data.Province;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.FileChooser;

public final class MainWindow implements Initializable {
	@FXML private MenuBar menuBar;
	@FXML private ComboBox<Province> provSelector;
	@FXML private TextField textId;
	@FXML private TextField textName;
	@FXML private Label labelColor;
	@FXML private TextField textRed;
	@FXML private TextField textGreen;
	@FXML private TextField textBlue;
	@FXML private TextField textSize;
	@FXML private TextField textAvgX;
	@FXML private TextField textAvgY;
	@FXML private ScrollPane mapImagePane;
	@FXML private ImageView mapImage;
	@FXML private Label statusLabel;
	
	private FileChooser fileChooser;

	private File mapDir;
	private List<Province> provinces = new ArrayList<>();
	private Map<Color, Province> provinceColors = new HashMap<>();
	private List<Province> emptyProvinces = new ArrayList<>();
	private Map<Color, Province> emptyProvinceColors = new HashMap<>();
	private boolean preventSelectionScroll;

	@FXML private void handleOpen(final ActionEvent event) {
		File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
		if(null != file) {
			provinces.clear();
			provinceColors.clear();
			emptyProvinces.clear();
			emptyProvinceColors.clear();
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("Windows-1252")))) {
				String line = null;
				while(null != (line = reader.readLine())) {
					try {
						Province prov = new Province(line);
						if(prov.getId() != -1 && !prov.getName().isEmpty()) {
							provinces.add(prov);
							provinceColors.put(prov.getColor(), prov);
						} else if(prov.getName().isEmpty()) {
							// Record for later
							emptyProvinces.add(prov);
							emptyProvinceColors.put(prov.getColor(), prov);
						}
					} catch(IllegalArgumentException iaex) {
						// Ignore stuff?
						iaex.printStackTrace();
					}
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
			
			provinces.sort(Province.NAME_COMPARATOR);
			provSelector.getItems().clear();
			provSelector.getItems().addAll(provinces);
			
			mapDir = file.getParentFile();
			
			try(InputStream imageStream = new FileInputStream(new File(mapDir, "provinces.bmp"))) {
				final Image img = new Image(imageStream);
				if(img.isBackgroundLoading()) {
					img.progressProperty().addListener(new ChangeListener<Number>() {
						@Override public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
							if((Double) newValue >= 1.0 && !img.isError()) {
								analyzeImage(img);
							}
						}
					});
				} else {
					analyzeImage(img);
				}
				mapImage.setImage(img);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@FXML private void handleSaveErrors(final ActionEvent event) {
	}
	
	@FXML private void handleQuit(final ActionEvent event) {
	}
	
	@FXML private void handleProvinceList(final ActionEvent event) {
	}
	
	@FXML private void handleErrorList(final ActionEvent event) {
	}
	
	@FXML private void handleAbout(final ActionEvent event) {
	}

	@FXML private void handleKeyInput(final InputEvent event) {
		if( event instanceof KeyEvent ) {
			// final KeyEvent keyEvent = (KeyEvent) event;
			// TODO: Do we need it?
		}
	}
	
	private void analyzeImage(Image img) {
		PixelReader pixelReader = img.getPixelReader();
		boolean provincesNeedSorting = false;
		for(int x = (int) (img.getWidth() - 1); x >= 0; -- x) {
			for(int y = (int) (img.getHeight() - 1); y >= 0; -- y) {
				Color color = pixelReader.getColor(x, y);
				if(provinceColors.containsKey(color)) {
					provinceColors.get(color).addPixel(x, y);
				} else if(emptyProvinceColors.containsKey(color)) {
					// Record and move back to the "real" list
					Province prov = emptyProvinceColors.get(color);
					emptyProvinceColors.remove(color);
					emptyProvinces.remove(prov);
					provinces.add(prov);
					provinceColors.put(prov.getColor(), prov);
					prov.addPixel(x, y);
					provincesNeedSorting = true;
				} else if(color.equals(Color.BLACK) || color.equals(Color.WHITE)) {
					// Terra incognita and wasteland; ignore
				} else {
					System.err.println(String.format("Unknown color %.0f;%.0f;%.0f at %d,%d",
						color.getRed() * 255, color.getGreen() * 255, color.getBlue() * 255, x, y));
				}
			}
		}
		if(provincesNeedSorting) {
			provinces.sort(Province.NAME_COMPARATOR);
			provSelector.getItems().clear();
			provSelector.getItems().addAll(provinces);
		}
		for(Province prov : provinces) {
			if(prov.getSize() < 25) {
				System.out.println(String.format("Warning: Province %s is very small; %d pixel(s) around %d,%d",
					prov, prov.getSize(), prov.getX(), prov.getY()));
			}
		}
	}
	
	@FXML private void handleProvSelection(final ActionEvent event) {
		Province prov = provSelector.getValue();
		if(null != prov) {
			if(!preventSelectionScroll) {
				mapImagePane.setVvalue(1.0 * prov.getY() / mapImage.getImage().getHeight());
				mapImagePane.setHvalue(1.0 * prov.getX() / mapImage.getImage().getWidth());
			}
			textId.setText(Integer.toString(prov.getId()));
			textName.setText(prov.getName());
			labelColor.setBackground(new Background(new BackgroundFill(Paint.valueOf(prov.getColor().toString()), null, null)));
			textRed.setText(Integer.toString(prov.getRed()));
			textGreen.setText(Integer.toString(prov.getGreen()));
			textBlue.setText(Integer.toString(prov.getBlue()));
			textSize.setText(Integer.toString(prov.getSize()));
			textAvgX.setText(Integer.toString(prov.getX()));
			textAvgY.setText(Integer.toString(prov.getY()));
		} else {
			labelColor.setBackground(Background.EMPTY);
		}
	}
	
	private double currentScale = 1.0;
	
	@FXML private void handleImageMouseClick(final MouseEvent event) {
		if(event.isStillSincePress() && event.getButton().equals(MouseButton.PRIMARY)) {
			int x = (int) (event.getX() / currentScale + 0.5);
			int y = (int) (event.getY() / currentScale + 0.5);
			Color c = mapImage.getImage().getPixelReader().getColor(x, y);
			if(provinceColors.containsKey(c)) {
				Province prov = provinceColors.get(c);
				preventSelectionScroll = true;
				provSelector.setValue(prov);
				preventSelectionScroll = false;
			}
		}
	}
	
	@FXML private void handleImageScroll(final ScrollEvent event) {
		currentScale *= Math.pow(1.001, event.getDeltaY());

        mapImage.setFitWidth(mapImage.getImage().getWidth() * currentScale);
        mapImage.setFitHeight(mapImage.getImage().getHeight() * currentScale);
        mapImagePane.setContent(null);
        mapImagePane.setContent(mapImage);
        event.consume();
	}

	@Override public void initialize(URL location, ResourceBundle resources) {
		menuBar.setFocusTraversable(true);
		fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().clear();
		fileChooser.getExtensionFilters().addAll(
			new FileChooser.ExtensionFilter("Clausewitz map definition file", "definition.csv"),
			new FileChooser.ExtensionFilter("All files", "*.*"));
	}
}
