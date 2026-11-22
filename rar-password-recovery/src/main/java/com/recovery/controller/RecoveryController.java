package com.recovery.controller;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.SwingWorker;

import com.recovery.model.PasswordEntry;
import com.recovery.model.RecoveryResult;
import com.recovery.model.RecoveryResult.Status;
import com.recovery.service.ArchiveTestService;
import com.recovery.service.BruteForce;
import com.recovery.service.ExcelPasswordService;
import com.recovery.util.Constants;

public class RecoveryController {

	public interface Listener {
		void onStarted(int total);
		void onProgress(int tested, int total, int excelRow);
		void onLog(String message);
		void onFinished(RecoveryResult result);
	}

	private final ExcelPasswordService excelService;
	private final ArchiveTestService archiveService;
	private SwingWorker<RecoveryResult, String> worker;

	public RecoveryController() {
		this.excelService = new ExcelPasswordService();
		this.archiveService = new ArchiveTestService();
	}

	public boolean isRunning() {
		return worker != null && !worker.isDone();
	}

	public void cancel() {
		if (worker != null) {
			worker.cancel(true);
		}
	}

	public void startByBruteforce(final Path rarPath,
			final Path sevenZipPath, 
			final Listener listener) {

		if (isRunning()) {
			throw new IllegalStateException("Ya hay una recuperación en curso.");
		}

		worker = new SwingWorker<RecoveryResult, String>() {
			@Override
			protected RecoveryResult doInBackground() {
				int tested = 0;
				ExecutorService executor = Executors.newFixedThreadPool(32);
				try {

					publish("Se inicia ataque de fuerza bruta");

					BruteForce generador = new BruteForce(Constants.DATO_ANALIZADO.getConstante());
					int cantidadIntentos = Integer.MAX_VALUE;
					listener.onStarted(cantidadIntentos);
					for (int i = 0; i < cantidadIntentos; i++) {
						String password = generador.next();

						if (isCancelled() || Thread.currentThread().isInterrupted()) {
							return new RecoveryResult(Status.CANCELLED, null, -1, tested,
									"Proceso cancelado por el usuario.");
						}

						tested++;
						publish("Probando combinacion " + password);

						Future<ArchiveTestService.TestOutcome> future =
								executor.submit(() -> archiveService.testPassword(sevenZipPath,rarPath,password));
						ArchiveTestService.TestOutcome outcome = future.get();
						
						listener.onProgress(tested, cantidadIntentos, i);

						if (outcome.isSuccessful()) {
							return new RecoveryResult(Status.FOUND,
									password, i, tested,
									"La prueba de integridad fue exitosa.");
						}

					}
					

					return new RecoveryResult(Status.NOT_FOUND, null, -1, tested,
							"Ninguna contraseña de la lista validó el archivo.");
				} catch (Exception ex) {
					return new RecoveryResult(Status.ERROR, null, -1, tested,
							ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
				}
			}

			@Override
			protected void process(List<String> messages) {
				for (String message : messages) {
					listener.onLog(message);
				}
			}

			@Override
			protected void done() {
				try {
					RecoveryResult result;
					if (isCancelled()) {
						result = new RecoveryResult(Status.CANCELLED, null, -1, 0,
								"Proceso cancelado por el usuario.");
					} else {
						result = get();
					}
					listener.onFinished(result);
				} catch (Exception ex) {
					listener.onFinished(new RecoveryResult(Status.ERROR, null, -1, 0,
							ex.getMessage() == null ? "Error inesperado." : ex.getMessage()));
				}
			}
		};

		worker.execute();
	}


	public void startByExcel(final Path rarPath, final Path excelPath,
			final Path sevenZipPath, final boolean hasHeader,
			final Listener listener) {

		if (isRunning()) {
			throw new IllegalStateException("Ya hay una recuperación en curso.");
		}

		worker = new SwingWorker<RecoveryResult, String>() {
			@Override
			protected RecoveryResult doInBackground() {
				int tested = 0;
				try {
					List<PasswordEntry> entries =
							excelService.readPasswords(excelPath, hasHeader ? 1 : 0);

					if (entries.isEmpty()) {
						return new RecoveryResult(Status.ERROR, null, -1, 0,
								"No se encontraron contraseñas en la primera columna.");
					}

					publish("Se cargaron " + entries.size() + " contraseñas.");
					listener.onStarted(entries.size());

					for (PasswordEntry entry : entries) {
						if (isCancelled() || Thread.currentThread().isInterrupted()) {
							return new RecoveryResult(Status.CANCELLED, null, -1, tested,
									"Proceso cancelado por el usuario.");
						}

						tested++;
						publish("Probando fila " + entry.getExcelRow()
						+ " (" + tested + " de " + entries.size() + ").");

						ArchiveTestService.TestOutcome outcome =
								archiveService.testPassword(sevenZipPath, rarPath,
										entry.getPassword());

						listener.onProgress(tested, entries.size(), entry.getExcelRow());

						if (outcome.isSuccessful()) {
							return new RecoveryResult(Status.FOUND,
									entry.getPassword(), entry.getExcelRow(), tested,
									"La prueba de integridad fue exitosa.");
						}
					}

					return new RecoveryResult(Status.NOT_FOUND, null, -1, tested,
							"Ninguna contraseña de la lista validó el archivo.");
				} catch (Exception ex) {
					return new RecoveryResult(Status.ERROR, null, -1, tested,
							ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
				}
			}

			@Override
			protected void process(List<String> messages) {
				for (String message : messages) {
					listener.onLog(message);
				}
			}

			@Override
			protected void done() {
				try {
					RecoveryResult result;
					if (isCancelled()) {
						result = new RecoveryResult(Status.CANCELLED, null, -1, 0,
								"Proceso cancelado por el usuario.");
					} else {
						result = get();
					}
					listener.onFinished(result);
				} catch (Exception ex) {
					listener.onFinished(new RecoveryResult(Status.ERROR, null, -1, 0,
							ex.getMessage() == null ? "Error inesperado." : ex.getMessage()));
				}
			}
		};

		worker.execute();
	}
}
