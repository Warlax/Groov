package calex.groov.app;

import javax.inject.Singleton;

import calex.groov.activity.GroovActivity;
import calex.groov.data.GroovDatabaseModule;
import calex.groov.model.GroovViewModel;
import calex.groov.service.RecordDefaultSetService;
import calex.groov.service.UpdateAppWidgetService;
import calex.groov.worker.RecordSetWorker;
import dagger.Component;

@Singleton
@Component(modules = {
    ContextModule.class,
    GroovBindingModule.class,
    GroovDatabaseModule.class,
    SharedPreferencesModule.class,
    WorkManagerModule.class,
})
@GroovApplicationScope
public interface GroovApplicationComponent {
  void inject(GroovActivity activity);
  void inject(RecordSetWorker worker);
  void inject(GroovViewModel viewModel);
  void inject(RecordDefaultSetService service);
  void inject(UpdateAppWidgetService service);
}

