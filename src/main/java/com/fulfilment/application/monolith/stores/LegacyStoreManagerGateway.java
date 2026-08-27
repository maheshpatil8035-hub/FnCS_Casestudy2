package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import org.jboss.logging.Logger;

@ApplicationScoped
public class LegacyStoreManagerGateway {

  private static final Logger LOGGER = Logger.getLogger(LegacyStoreManagerGateway.class);
  private static final String LEGACY_FILE_DIRECTORY = ".fulfilment/legacy-store-sync";

  public void createStoreOnLegacySystem(Store store) {
    // just to emulate as this would send this to a legacy system, let's write a temp file with the
    writeToFile(store);
  }

  public void updateStoreOnLegacySystem(Store store) {
    // just to emulate as this would send this to a legacy system, let's write a temp file with the
    writeToFile(store);
  }

  private void writeToFile(Store store) {
    try {
      Path tempDirectory = Files.createDirectories(Path.of(System.getProperty("user.home"), LEGACY_FILE_DIRECTORY));
      setDirectoryPermissionsIfSupported(tempDirectory);

      Path tempFile = Files.createTempFile(tempDirectory, "store-", ".txt");

      LOGGER.infof("Temporary file created at: %s", tempFile);

      String content =
          "Store created. [ name ="
              + store.name
              + " ] [ items on stock ="
              + store.quantityProductsInStock
              + "]";
      Files.writeString(tempFile, content);
      LOGGER.info("Data written to temporary file.");

      String readContent = Files.readString(tempFile);
      LOGGER.infof("Data read from temporary file: %s", readContent);

      Files.delete(tempFile);
      LOGGER.info("Temporary file deleted.");

    } catch (Exception e) {
      LOGGER.error("Failed to write store payload to legacy system temp file", e);
      throw new IllegalStateException("Failed to write store payload to legacy system temp file", e);
    }
  }

  private void setDirectoryPermissionsIfSupported(Path directory) {
    try {
      Files.setPosixFilePermissions(
          directory,
          Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE));
    } catch (UnsupportedOperationException ignored) {
      // On Windows, POSIX permissions are not supported; the app-specific home directory is still
      // private to the user and avoids the shared public temp area.
    } catch (IOException e) {
      LOGGER.debugf("Unable to set POSIX permissions for %s: %s", directory, e.getMessage());
    }
  }
}
