package sporemodder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javafx.scene.Scene;
import sporemodder.view.SceneTranslator;

public class LanguageManager extends AbstractManager {
   public static final String FOLDER_NAME = "Languages";
   private static final String PROPERTY_language = "language";
   private static final String BUILTIN_PATH = "resources/lang/";
   private static final String NAME_PREFIX = "#name=";
   private final Map<String, Language> languages = new LinkedHashMap<>();
   private Language language = Language.ENGLISH;
   private Map<String, String> dictionary = new HashMap<>();

   public static LanguageManager get() {
      return MainApp.get().getLanguageManager();
   }

   public static String tr(String var0) {
      MainApp var1 = MainApp.get();
      return var1 != null && var1.getLanguageManager() != null ? var1.getLanguageManager().translate(var0) : var0;
   }

   public void initialize(Properties var1) {
      this.discoverLanguages();
      this.language = this.findLanguage(var1.getProperty("language", Language.ENGLISH.getId()));
      this.dictionary = this.loadDictionary(this.language);
   }

   public void saveSettings(Properties var1) {
      var1.setProperty("language", this.language.getId());
   }

   public Collection<Language> getLanguages() {
      return Collections.unmodifiableCollection(this.languages.values());
   }

   public Language getLanguage() {
      return this.language;
   }

   public Language findLanguage(String var1) {
      Language var2 = this.languages.get(var1);
      return var2 == null ? Language.ENGLISH : var2;
   }

   public void setLanguage(Language var1) {
      if (var1 != null && var1 != this.language) {
         this.language = var1;
         this.dictionary = this.loadDictionary(var1);
         this.translateAll();
      }
   }

   private Map<String, String> templatesSource;
   private java.util.List<Object[]> templates = new java.util.ArrayList<>();
   private final Map<String, String> templateCache = new HashMap<>();

   private void rebuildTemplates() {
      this.templates = new java.util.ArrayList<>();
      this.templateCache.clear();
      this.templatesSource = this.dictionary;

      for (Map.Entry<String, String> var1 : this.dictionary.entrySet()) {
         String var2 = var1.getKey();
         if (var2.contains("{}")) {
            String[] var3 = var2.split("\\{\\}", -1);
            StringBuilder var4 = new StringBuilder();

            for (int var5 = 0; var5 < var3.length; var5++) {
               if (var5 > 0) {
                  var4.append("(.*?)");
               }

               var4.append(java.util.regex.Pattern.quote(var3[var5]));
            }

            this.templates.add(new Object[]{java.util.regex.Pattern.compile(var4.toString(), 32), var1.getValue()});
         }
      }
   }

   public String translate(String var1) {
      if (var1 == null) {
         return null;
      } else {
         String var2 = this.dictionary.get(var1);
         if (var2 != null) {
            return var2;
         } else {
            if (this.templatesSource != this.dictionary) {
               this.rebuildTemplates();
            }

            String var10 = this.templateCache.get(var1);
            if (var10 != null) {
               return var10;
            }

            if (this.templateCache.size() > 20000) {
               this.templateCache.clear();
            }

            for (Object[] var3 : this.templates) {
               java.util.regex.Matcher var4 = ((java.util.regex.Pattern)var3[0]).matcher(var1);
               if (var4.matches()) {
                  String var5 = (String)var3[1];
                  StringBuilder var6 = new StringBuilder();
                  int var7 = 1;
                  int var8 = 0;

                  while (true) {
                     int var9 = var5.indexOf("{}", var8);
                     if (var9 < 0 || var7 > var4.groupCount()) {
                        var6.append(var5.substring(var8));
                        this.templateCache.put(var1, var6.toString());
                        return var6.toString();
                     }

                     var6.append(var5, var8, var9).append(var4.group(var7++));
                     var8 = var9 + 2;
                  }
               }
            }

            this.templateCache.put(var1, var1);
            return var1;
         }
      }
   }

   public void translateAll() {
      Scene var1 = UIManager.get().getScene();
      if (var1 != null) {
         SceneTranslator.translate(var1.getRoot());
      }

      SceneTranslator.translateRibbon();
      UIManager.get().notifyUIUpdate(false);
   }

   private File getExternalFolder() {
      return MainApp.get().getPathManager().getProgramFile("Languages");
   }

   private File getExternalFile(Language var1) {
      return new File(this.getExternalFolder(), var1.getId() + ".tsv");
   }

   private void discoverLanguages() {
      this.languages.clear();
      this.languages.put(Language.ENGLISH.getId(), Language.ENGLISH);

      try (InputStream var1 = LanguageManager.class.getResourceAsStream("resources/lang/languages.txt")) {
         if (var1 != null) {
            BufferedReader var2 = new BufferedReader(new InputStreamReader(var1, StandardCharsets.UTF_8));

            String var3;
            while ((var3 = var2.readLine()) != null) {
               var3 = var3.trim();
               int var4 = var3.indexOf(61);
               if (!var3.isEmpty() && !var3.startsWith("#") && var4 > 0) {
                  String var5 = var3.substring(0, var4).trim();
                  this.languages.putIfAbsent(var5, new Language(var5, var3.substring(var4 + 1).trim()));
               }
            }
         }
      } catch (IOException var11) {
         var11.printStackTrace();
      }

      File[] var12 = this.getExternalFolder().listFiles((var0, var1x) -> var1x.toLowerCase().endsWith(".tsv"));
      if (var12 != null) {
         Arrays.sort((Object[])var12);

         for (File var17 : var12) {
            String var6 = var17.getName().substring(0, var17.getName().length() - ".tsv".length());
            if (!var6.equals(Language.ENGLISH.getId())) {
               String var7 = readName(var17);
               Language var8 = this.languages.get(var6);
               this.languages.put(var6, new Language(var6, var7 != null ? var7 : (var8 != null ? var8.getDisplayName() : var6)));
            }
         }
      }
   }

   private static String readName(File var0) {
      String var2;
      try (BufferedReader var1 = Files.newBufferedReader(var0.toPath(), StandardCharsets.UTF_8)) {
         while ((var2 = var1.readLine()) != null) {
            if (var2.startsWith("#name=")) {
               return var2.substring("#name=".length()).trim();
            }
         }
      } catch (IOException var6) {
         var6.printStackTrace();
      }

      return null;
   }

   private Map<String, String> loadDictionary(Language var1) {
      HashMap var2 = new HashMap();
      if (var1 == Language.ENGLISH) {
         return var2;
      } else {
         try (InputStream var3 = LanguageManager.class.getResourceAsStream("resources/lang/" + var1.getId() + ".tsv")) {
            if (var3 != null) {
               var2.putAll(parseDictionary(new BufferedReader(new InputStreamReader(var3, StandardCharsets.UTF_8))));
            }
         } catch (IOException var12) {
            var12.printStackTrace();
         }

         File var13 = this.getExternalFile(var1);
         if (var13.isFile()) {
            try (BufferedReader var4 = Files.newBufferedReader(var13.toPath(), StandardCharsets.UTF_8)) {
               var2.putAll(parseDictionary(var4));
            } catch (IOException var10) {
               var10.printStackTrace();
            }
         }

         return var2;
      }
   }

   static Map<String, String> parseDictionary(BufferedReader var0) throws IOException {
      HashMap var1 = new HashMap();

      String var2;
      while ((var2 = var0.readLine()) != null) {
         int var3 = var2.indexOf(9);
         if (!var2.isEmpty() && !var2.startsWith("#") && var3 > 0) {
            var1.put(unescape(var2.substring(0, var3)), unescape(var2.substring(var3 + 1)));
         }
      }

      return var1;
   }

   static String unescape(String var0) {
      if (var0.indexOf(92) < 0) {
         return var0;
      } else {
         StringBuilder var1 = new StringBuilder();

         for (int var2 = 0; var2 < var0.length(); var2++) {
            char var3 = var0.charAt(var2);
            if (var3 == '\\' && var2 + 1 < var0.length()) {
               char var4 = var0.charAt(++var2);
               var1.append(var4 == 'n' ? '\n' : (var4 == 't' ? '\t' : var4));
            } else {
               var1.append(var3);
            }
         }

         return var1.toString();
      }
   }
}
