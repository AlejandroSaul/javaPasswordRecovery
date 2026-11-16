package com.recovery.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ArchiveTestService {

    public static class TestOutcome {
        private final boolean successful;
        private final String diagnostic;

        public TestOutcome(boolean successful, String diagnostic) {
            this.successful = successful;
            this.diagnostic = diagnostic;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public String getDiagnostic() {
            return diagnostic;
        }
    }

    /**
     * Ejecuta "7z t" para comprobar la integridad del RAR.
     *
     * 7-Zip suele devolver código 2 tanto para contraseña incorrecta como
     * para otros errores fatales. Por eso solo clasificamos como contraseña
     * incorrecta cuando la salida contiene un mensaje explícito de contraseña.
     * Los demás errores detienen el proceso para evitar falsos resultados.
     */
    public TestOutcome testPassword(Path sevenZipExe, Path archive, String password)
            throws Exception {

        if (sevenZipExe == null || !Files.isRegularFile(sevenZipExe)) {
            throw new IllegalArgumentException("No se encontró 7z.exe.");
        }
        if (archive == null || !Files.isRegularFile(archive)) {
            throw new IllegalArgumentException("No se encontró el archivo RAR.");
        }

        List<String> command = new ArrayList<String>();
        command.add(sevenZipExe.toAbsolutePath().toString());
        command.add("t");
        command.add("-y");
        command.add("-p" + password);
        command.add(archive.toAbsolutePath().toString());

        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Limitar el diagnóstico guardado; no imprimirlo en consola.
                if (output.length() < 6000) {
                    output.append(line).append(System.lineSeparator());
                }
            }
        }

        boolean finished = process.waitFor(10, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new IllegalStateException("La prueba de 7-Zip excedió el límite de 10 minutos.");
        }

        int exitCode = process.exitValue();
        if (exitCode == 0) {
            return new TestOutcome(true, "La prueba de integridad de 7-Zip terminó correctamente.");
        }

        String normalized = output.toString().toLowerCase(Locale.ROOT);

        if (containsWrongPasswordMessage(normalized)) {
            return new TestOutcome(false, "Contraseña no válida.");
        }

        String diagnostic = "7-Zip terminó con código " + exitCode
                + " y no confirmó explícitamente que el error sea una contraseña incorrecta. "
                + "Se detuvo la recuperación para evitar resultados falsos.";

        throw new IllegalStateException(diagnostic);
    }

    private boolean containsWrongPasswordMessage(String output) {
        return output.contains("wrong password")
                || output.contains("password is incorrect")
                || output.contains("incorrect password")
                || output.contains("contraseña incorrecta")
                || output.contains("contrasena incorrecta");
    }
}
