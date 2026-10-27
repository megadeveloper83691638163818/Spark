package sporemodder.imageeditor.io;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import sporemodder.imageeditor.model.ImageDocument;

public interface ImageFormat {
   Set<String> getExtensions();

   ImageDocument read(File var1) throws IOException;

   void write(ImageDocument var1, File var2) throws IOException;
}
