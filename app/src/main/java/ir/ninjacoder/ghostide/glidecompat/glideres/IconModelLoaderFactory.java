package ir.ninjacoder.ghostide.glidecompat.glideres;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

public class IconModelLoaderFactory implements ModelLoaderFactory<IconRes, Drawable> {

  private final Context context;

  public IconModelLoaderFactory(Context context) {
    this.context = context;
  }

  @NonNull
  @Override
  public ModelLoader<IconRes, Drawable> build(@NonNull MultiModelLoaderFactory multiFactory) {
    return new IconModelLoader(context);
  }

  @Override
  public void teardown() {}
}
