package sporemodder.view.ribbons.util;

import java.awt.Desktop;
import java.io.File;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import sporemodder.PathManager;
import sporemodder.UIManager;
import sporemodder.file.dbpf.PackageVersionConverter;
import sporemodder.view.Controller;

/** Ribbon group that converts DBPF 3.0 packages to the DBPF 2.0 format of the 2008 prototype. */
public class PackageConverterUI implements Controller {

	public static final String OUTPUT_FOLDER = "Converted packages";

	@FXML
	private Node mainNode;
	@FXML
	private Button btnConvert;
	@FXML
	private Button btnOpenOutput;

	private File lastFolder;

	@Override
	public Node getMainNode() {
		return mainNode;
	}

	private File getOutputFolder() {
		File folder = PathManager.get().getProgramFile(OUTPUT_FOLDER);
		folder.mkdirs();
		return folder;
	}

	@FXML
	protected void initialize() {
		btnConvert.setOnAction(event -> convert());
		btnOpenOutput.setOnAction(event -> UIManager.get().tryAction(
				() -> Desktop.getDesktop().open(getOutputFolder()), "Cannot open the output folder."));
	}

	private void convert() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Select the package to convert");
		chooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Spore package (*.package)", "*.package"),
				new FileChooser.ExtensionFilter("All files", "*.*"));
		if (lastFolder != null && lastFolder.isDirectory()) chooser.setInitialDirectory(lastFolder);

		File input = chooser.showOpenDialog(UIManager.get().getPrimaryStage());
		if (input == null) return;
		lastFolder = input.getParentFile();

		File output = PackageVersionConverter.defaultOutputFile(input, getOutputFolder());

		Task<PackageVersionConverter.Result> task = new Task<PackageVersionConverter.Result>() {
			@Override
			protected PackageVersionConverter.Result call() throws Exception {
				return PackageVersionConverter.convert(input, output, p -> updateProgress(p, 1.0));
			}
		};
		btnConvert.disableProperty().bind(task.runningProperty());
		task.setOnSucceeded(e -> {
			btnConvert.disableProperty().unbind();
			btnConvert.setDisable(false);
			Alert alert = new Alert(AlertType.INFORMATION, "Converted " + task.getValue().itemCount
					+ " items:\n" + output.getAbsolutePath(), ButtonType.OK);
			alert.setTitle("Package converted");
			UIManager.get().showDialog(alert);
		});
		task.setOnFailed(e -> {
			btnConvert.disableProperty().unbind();
			btnConvert.setDisable(false);
			UIManager.get().showErrorDialog(task.getException(), "Cannot convert the package.", true);
		});
		Thread t = new Thread(task, "PackageVersionConverter");
		t.setDaemon(true);
		t.start();
	}
}
