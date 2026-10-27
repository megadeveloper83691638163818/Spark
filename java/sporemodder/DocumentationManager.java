package sporemodder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

public class DocumentationManager extends AbstractManager {
   private final Map<String, Properties> loadedFiles = new HashMap<>();
   private final Map<String, List<DocumentationManager.DocumentationLinkCategory>> docLinks = new HashMap<>();

   public static DocumentationManager get() {
      return MainApp.get().getDocumentationManager();
   }

   private static final String DOCS_URL = "https://raw.githubusercontent.com/emd4600/SporeModder-FX/master/smfx_docs.json";

   @Override
   public void initialize(Properties var1) {
      File CACHE = new File(PathManager.get().getProgramFile("Documentation"), "smfx_docs.json");
      String TEXT = null;
      // web first, with short timeouts
      try {
         java.net.URLConnection CONN = new java.net.URL(DOCS_URL).openConnection();
         CONN.setConnectTimeout(4000);
         CONN.setReadTimeout(4000);
         try (java.io.InputStream IN = CONN.getInputStream()) {
            TEXT = new String(IN.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
         }
         new JSONObject(TEXT);
         CACHE.getParentFile().mkdirs();
         java.nio.file.Files.write(CACHE.toPath(), TEXT.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      } catch (Exception E) {
         System.err.println("docs links: " + E);
         TEXT = null;
      }
      // then the last downloaded copy
      if (TEXT == null && CACHE.isFile()) {
         try {
            TEXT = new String(java.nio.file.Files.readAllBytes(CACHE.toPath()), java.nio.charset.StandardCharsets.UTF_8);
            new JSONObject(TEXT);
         } catch (Exception E) {
            TEXT = null;
         }
      }
      // and the copy inside the jar as a last resort
      if (TEXT == null) {
         try (java.io.InputStream IN = DocumentationManager.class.getResourceAsStream("resources/smfx_docs.json")) {
            if (IN != null) {
               TEXT = new String(IN.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
         } catch (Exception E) {
            E.printStackTrace();
         }
      }
      if (TEXT != null) {
         try {
            this.loadDocLinks(new JSONObject(TEXT));
         } catch (Exception E) {
            E.printStackTrace();
         }
      }
   }

   public String getDocumentation(String var1) {
      return this.getDocumentation(var1.split("\\.")[0], var1);
   }

   public String getDocumentation(String var1, String var2) {
      Properties var3 = this.loadFile(var1);
      if (var3 == null) {
         return var2;
      } else {
         String var4 = var3.getProperty(var2);
         if (var4 == null && var2.endsWith(".")) {
            var4 = var3.getProperty(var2.substring(0, var2.length() - 1));
         }

         if (var4 == null && !var2.endsWith(".")) {
            var4 = var3.getProperty(var2 + ".");
         }

         return var4 != null && !var4.trim().isEmpty() ? var4 : var2;
      }
   }

   public List<DocumentationManager.DocumentationLinkCategory> getDocumentationLinks(String var1) {
      return this.docLinks.get(var1);
   }

   public Pane createDocumentationPane(String var1) {
      List<DocumentationManager.DocumentationLinkCategory> var2 = this.getDocumentationLinks(var1);
      if (var2 == null) {
         return null;
      } else {
         VBox var3 = new VBox();
         var3.setPadding(new Insets(5.0, 0.0, 0.0, 0.0));

         for (DocumentationManager.DocumentationLinkCategory var5 : var2) {
            if (!var5.name.isEmpty()) {
               Label var6 = new Label(var5.name);
               var6.getStyleClass().add("inspector-docs-title");
               var3.getChildren().add(var6);
            }

            for (DocumentationManager.DocumentationLink var7 : var5.links) {
               Hyperlink var8 = new Hyperlink(var7.title);
               var8.setWrapText(true);
               var8.getStyleClass().add("inspector-docs-link");
               if (var7.url.isEmpty()) {
                  var8.setDisable(true);
               } else {
                  var8.setTooltip(new Tooltip(var7.url));
                  var8.setOnAction(var1x -> MainApp.get().getHostServices().showDocument(var7.url));
               }

               var3.getChildren().add(var8);
            }
         }

         return var3;
      }
   }

   private Properties loadFile(String var1) {
      Properties var2 = this.loadedFiles.get(var1);
      if (var2 == null) {
         File var3 = new File(PathManager.get().getProgramFile("Documentation"), var1 + ".txt");
         var2 = new Properties();

         try (FileReader var4 = new FileReader(var3)) {
            var2.load(var4);
         } catch (IOException var9) {
            var9.printStackTrace();
            return null;
         }

         this.loadedFiles.put(var1, var2);
      }

      return var2;
   }

   private void loadDocLinks(JSONObject var1) {
      JSONObject var2 = var1.getJSONObject("entries");

      for (String var5 : var2.keySet()) {
         ArrayList<DocumentationManager.DocumentationLinkCategory> var6 = new ArrayList<>();
         JSONArray var7 = var2.getJSONArray(var5);

         for (int var8 = 0; var8 < var7.length(); var8++) {
            JSONObject var9 = var7.getJSONObject(var8);
            DocumentationManager.DocumentationLinkCategory var10 = new DocumentationManager.DocumentationLinkCategory();
            var10.name = var9.getString("name");
            JSONArray var11 = var9.getJSONArray("items");

            for (int var12 = 0; var12 < var11.length(); var12++) {
               JSONObject var13 = var11.getJSONObject(var12);
               DocumentationManager.DocumentationLink var14 = new DocumentationManager.DocumentationLink();
               var14.title = var13.getString("title");
               var14.url = var13.getString("url");
               var10.links.add(var14);
            }

            var6.add(var10);
         }

         this.docLinks.put(var5, var6);
      }

      var2 = var1.getJSONObject("redirections");

      for (String var18 : var2.keySet()) {
         this.docLinks.put(var18, this.docLinks.get(var2.getString(var18)));
      }
   }

   public static class DocumentationLink {
      public String title;
      public String url;
   }

   public static class DocumentationLinkCategory {
      public String name;
      public final List<DocumentationManager.DocumentationLink> links = new ArrayList<>();
   }
}
