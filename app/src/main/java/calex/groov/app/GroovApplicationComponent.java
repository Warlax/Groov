package calex.groov.app;

import javax.inject.Singleton;

import calex.groov.activity.GroovActivity;
import calex.groov.activity.HistoryActivity;
import calex.groov.data.GroovDatabaseModule;
import calex.groov.model.GroovViewModel;
import calex.groov.model.HistoryViewModel;
import calex.groov.receiver.DateChangedReceiver;
import calex.groov.service.GroovTileService;
import calex.groov.service.RecordDefaultSetService;
import calex.groov.service.UpdateAppWidgetService;
import calex.groov.worker.DeleteMostRecentSetWorker;
import calex.groov.worker.ExportWorker;
import calex.groov.worker.ImportWorker;
import calex.groov.worker.RecordSetWorker;
import calex.groov.worker.ReminderWorker;
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
  void inject(HistoryActivity activity);

  void inject(RecordDefaultSetService service);
  void inject(UpdateAppWidgetService service);
  void inject(GroovTileService service);

  void inject(DateChangedReceiver receiver);

  void inject(GroovViewModel viewModel);
  void inject(HistoryViewModel viewModel);

  void inject(RecordSetWorker worker);
  void inject(ExportWorker worker);
  void inject(ImportWorker worker);
  void inject(DeleteMostRecentSetWorker worker);
  void inject(ReminderWorker worker);
}

