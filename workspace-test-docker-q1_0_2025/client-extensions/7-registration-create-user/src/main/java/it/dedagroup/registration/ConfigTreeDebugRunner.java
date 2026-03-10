package it.dedagroup.registration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;

@Component
public class ConfigTreeDebugRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        String cePath = System.getenv("LIFERAY_ROUTES_CLIENT_EXTENSION");
        String dxpPath = System.getenv("LIFERAY_ROUTES_DXP");

        System.out.println("LIFERAY_ROUTES_CLIENT_EXTENSION = " + cePath);
        System.out.println("LIFERAY_ROUTES_DXP = " + dxpPath);

        _printFiles("CLIENT_EXTENSION", cePath);
        _printFiles("DXP", dxpPath);
    }

    private void _printFiles(String label, String path) throws Exception {
        if (path == null || path.isBlank()) {
            System.out.println(label + ": path assente");
            return;
        }

        File dir = new File(path);

        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println(label + ": directory non trovata -> " + path);
            return;
        }

        System.out.println(label + ": contenuto di " + path);

        File[] files = dir.listFiles();

        if (files == null) {
            System.out.println(label + ": nessun file");
            return;
        }

        for (File file : files) {
            System.out.println(" - " + file.getName());

            if (file.isFile()) {
                String value = java.nio.file.Files.readString(file.toPath());
                System.out.println("   value = " + value);
            }
        }
    }
}