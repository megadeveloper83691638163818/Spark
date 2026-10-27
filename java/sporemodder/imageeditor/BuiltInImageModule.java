package sporemodder.imageeditor;

import sporemodder.imageeditor.filters.Adjustments;
import sporemodder.imageeditor.filters.CategorizedFilter;
import sporemodder.imageeditor.filters.Effects;
import sporemodder.imageeditor.filters.Filters;
import sporemodder.imageeditor.filters.GrayscaleFilter;
import sporemodder.imageeditor.filters.InvertFilter;
import sporemodder.imageeditor.io.JavaImageFormat;
import sporemodder.imageeditor.tools.*;

public class BuiltInImageModule implements ImageEditorModule {
   @Override
   public void register(ImageEditorRegistry registry) {
      registry.addTool(new BrushTool());
      registry.addTool(new PencilTool());
      registry.addTool(new EraserTool());
      registry.addTool(new FillTool());
      registry.addTool(new GradientTool());
      registry.addTool(new PickerTool());
      registry.addTool(new DodgeBurnTool(true));
      registry.addTool(new DodgeBurnTool(false));
      registry.addTool(new BlurBrushTool());
      registry.addTool(new ShapeTool(ShapeTool.Kind.LINE));
      registry.addTool(new ShapeTool(ShapeTool.Kind.RECTANGLE));
      registry.addTool(new ShapeTool(ShapeTool.Kind.ELLIPSE));
      registry.addTool(new MarqueeTool(false));
      registry.addTool(new MarqueeTool(true));
      registry.addTool(new WandTool());

      registry.addFilter(Adjustments.brightnessContrast());
      registry.addFilter(Adjustments.hueSaturation());
      registry.addFilter(Adjustments.levels());
      registry.addFilter(Adjustments.vibrance());
      registry.addFilter(Adjustments.colorBalance());
      registry.addFilter(Adjustments.temperature());
      registry.addFilter(Adjustments.equalize());
      registry.addFilter(new CategorizedFilter("Adjustments", new InvertFilter()));
      registry.addFilter(new CategorizedFilter("Adjustments", new GrayscaleFilter()));
      registry.addFilter(new CategorizedFilter("Adjustments", Filters.sepia()));
      registry.addFilter(new CategorizedFilter("Adjustments", Filters.threshold()));
      registry.addFilter(new CategorizedFilter("Adjustments", Filters.posterize()));
      registry.addFilter(Effects.gaussianBlur());
      registry.addFilter(Effects.motionBlur());
      registry.addFilter(Effects.pixelate());
      registry.addFilter(Effects.addNoise());
      registry.addFilter(Effects.median());
      registry.addFilter(Effects.edgeDetect());
      registry.addFilter(Effects.emboss());
      registry.addFilter(Filters.sharpen());
      registry.addFilter(Filters.flipHorizontal());
      registry.addFilter(Filters.flipVertical());

      registry.addFormat(new JavaImageFormat());
   }
}
