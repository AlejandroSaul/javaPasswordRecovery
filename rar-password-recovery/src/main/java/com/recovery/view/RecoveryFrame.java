package com.recovery.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.recovery.controller.RecoveryController;
import com.recovery.controller.RecoveryController.Listener;
import com.recovery.model.RecoveryResult;
import com.recovery.model.RecoveryResult.Status;
import com.recovery.util.Constants;
import com.recovery.util.SevenZipUtil;

public class RecoveryFrame extends JFrame {

	private final JTextField rarField = new JTextField();
	private final JTextField excelField = new JTextField();
	private final JTextField sevenZipField = new JTextField();
	private final JTextField initialValueField = new JTextField(15);


	private final JButton rarBrowseButton = new JButton(Constants.EXAMINAR.getConstante());
	private final JButton excelBrowseButton = new JButton(Constants.EXAMINAR.getConstante());
	private final JButton sevenZipBrowseButton = new JButton(Constants.EXAMINAR.getConstante());

	private final JButton startButton = new JButton("Iniciar");
	private final JButton cancelButton = new JButton("Cancelar");
	private final JButton saveButton = new JButton("Guardar resultado");

	private final JCheckBox headerCheckBox =
			new JCheckBox("La primera fila contiene encabezados");

	private final JCheckBox excelCheckBox =
			new JCheckBox("¿Usar Excel?");

	private final JProgressBar progressBar = new JProgressBar();
	private final JLabel statusLabel = new JLabel("Selecciona los archivos para comenzar.");
	private final JLabel countLabel = new JLabel("0 / 0");
	private final JTextArea logArea = new JTextArea(12, 60);

	private final RecoveryController recoveryController = new RecoveryController();
	private RecoveryResult lastResult;

	public RecoveryFrame() {
		super("RAR Password Recovery - Java 8");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(720, 540));
		setLocationRelativeTo(null);

		buildUi();
		wireEvents();

		Path default7z = SevenZipUtil.findDefaultPath();
		if (default7z != null) {
			sevenZipField.setText(default7z.toString());
		}

		setBusy(false);
	}

	private void buildUi() {
		JPanel root = new JPanel(new BorderLayout(10, 10));
		root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		setContentPane(root);

		JPanel filesPanel = new JPanel(new GridBagLayout());
		filesPanel.setBorder(BorderFactory.createTitledBorder("Archivos de entrada"));
		

		addFileRow(filesPanel, 0, "Archivo RAR:", rarField, rarBrowseButton);
		addFileRow(filesPanel, 1, "Excel (.xlsx):", excelField, excelBrowseButton);
		addFileRow(filesPanel, 2, "7-Zip (7z.exe):", sevenZipField, sevenZipBrowseButton);

		GridBagConstraints check = new GridBagConstraints();
		check.gridx = 0;
		check.gridy = 3;
		check.gridwidth = 3;
		check.anchor = GridBagConstraints.WEST;
		check.insets = new Insets(8, 5, 5, 5);
		filesPanel.add(headerCheckBox, check);
		
		GridBagConstraints checkExc = new GridBagConstraints();
		checkExc.gridx = 0;
		checkExc.gridy = 4;
		checkExc.gridwidth = 3;
		checkExc.anchor = GridBagConstraints.WEST;
		checkExc.insets = new Insets(8, 5, 5, 5);
		filesPanel.add(excelCheckBox, checkExc);

		GridBagConstraints initial = new GridBagConstraints();

		initial.gridx = 0;
		initial.gridy = 5;
		initial.anchor = GridBagConstraints.WEST;
		initial.insets = new Insets(8, 5, 5, 5);

		filesPanel.add(new JLabel("Valor inicial:"), initial);

		initial = new GridBagConstraints();
		initial.gridx = 1;
		initial.gridy = 5;
		initial.gridwidth = 2;
		initial.weightx = 1.0;
		initial.fill = GridBagConstraints.HORIZONTAL;
		initial.insets = new Insets(8, 5, 5, 5);

		filesPanel.add(initialValueField, initial);

		JPanel progressPanel = new JPanel(new BorderLayout(8, 8));
		progressPanel.setBorder(BorderFactory.createTitledBorder("Progreso"));
		JPanel progressTop = new JPanel(new BorderLayout());
		progressTop.add(statusLabel, BorderLayout.WEST);
		progressTop.add(countLabel, BorderLayout.EAST);
		progressPanel.add(progressTop, BorderLayout.NORTH);
		progressPanel.add(progressBar, BorderLayout.CENTER);

		logArea.setEditable(false);
		logArea.setLineWrap(true);
		logArea.setWrapStyleWord(true);
		JScrollPane logScroll = new JScrollPane(logArea);
		logScroll.setBorder(BorderFactory.createTitledBorder("Registro de actividad"));

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttons.add(saveButton);
		buttons.add(cancelButton);
		buttons.add(startButton);

		JPanel center = new JPanel(new BorderLayout(8, 8));
		center.add(progressPanel, BorderLayout.NORTH);
		center.add(logScroll, BorderLayout.CENTER);

		root.add(filesPanel, BorderLayout.NORTH);
		root.add(center, BorderLayout.CENTER);
		root.add(buttons, BorderLayout.SOUTH);
	}

	private void addFileRow(JPanel panel, int row, String label,
			JTextField field, JButton button) {
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		c.gridy = row;

		c.gridx = 0;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel(label), c);

		c.gridx = 1;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		field.setEditable(false);
		panel.add(field, c);

		c.gridx = 2;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		panel.add(button, c);
	}

	private void wireEvents() {
		rarBrowseButton.addActionListener(e -> chooseFile(
				rarField, "Seleccionar archivo RAR",
				new FileNameExtensionFilter("Archivos RAR (*.rar)", "rar")));

		excelBrowseButton.addActionListener(e -> chooseFile(
				excelField, "Seleccionar Excel",
				new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx")));

		sevenZipBrowseButton.addActionListener(e -> chooseFile(
				sevenZipField, "Seleccionar 7z.exe", null));

		startButton.addActionListener(e -> startRecovery());
		cancelButton.addActionListener(e -> recoveryController.cancel());
		saveButton.addActionListener(e -> saveResult());
	}

	private void chooseFile(JTextField target, String title,
			FileNameExtensionFilter filter) {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);
		if (filter != null) {
			chooser.setFileFilter(filter);
		}
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			target.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	private void startRecovery() {
		if(excelCheckBox.isSelected()) {
			startRecoveryByExcel();
		} else {
			startRecoveryByBruteforce();
		}

	}

	private void startRecoveryByExcel() {

		final Path rar = pathFromField(rarField);
		final Path excel = pathFromField(excelField);
		final Path sevenZip = pathFromField(sevenZipField);

		if (!Files.isRegularFile(rar)) {
			showError("Selecciona un archivo RAR válido.");
			return;
		}
		if (!Files.isRegularFile(excel)) {
			showError("Selecciona un archivo Excel .xlsx válido.");
			return;
		}
		if (!Files.isRegularFile(sevenZip)) {
			showError("Selecciona 7z.exe. Normalmente está en la carpeta de instalación de 7-Zip.");
			return;
		}

		lastResult = null;
		saveButton.setEnabled(false);
		logArea.setText("");
		progressBar.setValue(0);
		countLabel.setText("0 / 0");
		statusLabel.setText("Preparando...");
		setBusy(true);

		recoveryController.startByExcel(rar, excel, sevenZip, headerCheckBox.isSelected(),
				new Listener() {
			@Override
			public void onStarted(int total) {
				SwingUtilities.invokeLater(() -> {
					progressBar.setMaximum(Math.max(total, 1));
					countLabel.setText("0 / " + total);
					statusLabel.setText("Probando contraseñas...");
				});
			}

			@Override
			public void onProgress(int tested, int total, int excelRow) {
				SwingUtilities.invokeLater(() -> {
					progressBar.setMaximum(Math.max(total, 1));
					progressBar.setValue(tested);
					countLabel.setText(tested + " / " + total);
				});
			}

			@Override
			public void onLog(String message) {
				SwingUtilities.invokeLater(() -> appendLog(message));
			}

			@Override
			public void onFinished(RecoveryResult result) {
				SwingUtilities.invokeLater(() -> finishRecovery(result));
			}
		});

	}

	private void startRecoveryByBruteforce() {
		final Path rar = pathFromField(rarField);
		final Path sevenZip = pathFromField(sevenZipField);

		if (!Files.isRegularFile(rar)) {
			showError("Selecciona un archivo RAR válido.");
			return;
		}
		if (!Files.isRegularFile(sevenZip)) {
			showError("Selecciona 7z.exe. Normalmente está en la carpeta de instalación de 7-Zip.");
			return;
		}

		lastResult = null;
		saveButton.setEnabled(false);
		logArea.setText("");
		progressBar.setValue(0);
		countLabel.setText("0 / 0");
		statusLabel.setText("Preparando...");
		setBusy(true);

		recoveryController.startByBruteforce(rar, sevenZip,
				new Listener() {
			@Override
			public void onStarted(int total) {
				SwingUtilities.invokeLater(() -> {
					progressBar.setMaximum(Math.max(total, 1));
					countLabel.setText("0 / " + total);
					statusLabel.setText("Probando contraseñas...");
				});
			}

			@Override
			public void onProgress(int tested, int total, int excelRow) {
				SwingUtilities.invokeLater(() -> {
					progressBar.setMaximum(Math.max(total, 1));
					progressBar.setValue(tested);
					countLabel.setText(tested + " / " + total);
				});
			}

			@Override
			public void onLog(String message) {
				SwingUtilities.invokeLater(() -> appendLog(message));
			}

			@Override
			public void onFinished(RecoveryResult result) {
				SwingUtilities.invokeLater(() -> finishRecovery(result));
			}
		},initialValueField );
	}

	private Path pathFromField(JTextField field) {
		String value = field.getText().trim();
		return value.isEmpty() ? null : Paths.get(value);
	}

	private void finishRecovery(RecoveryResult result) {
		lastResult = result;
		setBusy(false);
		statusLabel.setText(result.getMessage());
		appendLog(result.getMessage());

		if (result.getStatus() == Status.FOUND) {
			appendLog("Contraseña encontrada en la fila " + result.getExcelRow() + ".");
			JOptionPane.showMessageDialog(this,
					"Contraseña encontrada:\n" + result.getPassword()
					+ "\nFila de Excel: " + result.getExcelRow(),
					"Recuperación exitosa", JOptionPane.INFORMATION_MESSAGE);
			saveButton.setEnabled(true);
		} else if (result.getStatus() == Status.NOT_FOUND) {
			JOptionPane.showMessageDialog(this, result.getMessage(),
					"Sin coincidencias", JOptionPane.INFORMATION_MESSAGE);
		} else if (result.getStatus() == Status.CANCELLED) {
			JOptionPane.showMessageDialog(this, result.getMessage(),
					"Proceso cancelado", JOptionPane.WARNING_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this, result.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void saveResult() {
		if (lastResult == null || lastResult.getStatus() != Status.FOUND) {
			return;
		}

		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Guardar resultado");
		chooser.setSelectedFile(new java.io.File("resultado-recuperacion.txt"));

		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String content = "Archivo RAR: " + rarField.getText()
			+ System.lineSeparator()
			+ "Fila de Excel: " + lastResult.getExcelRow()
			+ System.lineSeparator()
			+ "Contraseña: " + lastResult.getPassword()
			+ System.lineSeparator()
			+ "Resultado: " + lastResult.getMessage()
			+ System.lineSeparator();

			try {
				Files.write(chooser.getSelectedFile().toPath(),
						content.getBytes(StandardCharsets.UTF_8));
				JOptionPane.showMessageDialog(this,
						"Resultado guardado correctamente.");
			} catch (Exception ex) {
				showError("No se pudo guardar el resultado: " + ex.getMessage());
			}
		}
	}

	private void appendLog(String message) {
		logArea.append(message + System.lineSeparator());
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	private void setBusy(boolean busy) {
		startButton.setEnabled(!busy);
		cancelButton.setEnabled(busy);
		rarBrowseButton.setEnabled(!busy);
		excelBrowseButton.setEnabled(!busy);
		sevenZipBrowseButton.setEnabled(!busy);
		headerCheckBox.setEnabled(!busy);
		saveButton.setEnabled(!busy && lastResult != null
				&& lastResult.getStatus() == Status.FOUND);
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message,
				"Validación", JOptionPane.ERROR_MESSAGE);
	}
}
