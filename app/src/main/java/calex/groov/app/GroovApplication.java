package calex.groov.app;

import android.app.Application;

public class GroovApplication extends Application {

  private GroovApplicationComponent component;

  @Override
  public void onCreate() {
    super.onCreate();
    component = DaggerGroovApplicationComponent.builder()
        .contextModule(new ContextModule(this))
        .build();
  }

  public GroovApplicationComponent getComponent() {
    return component;
  }
}
