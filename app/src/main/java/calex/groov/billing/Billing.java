package calex.groov.billing;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Billing {

  private static final String SKU = "ads_free";

  private final BillingClient billingClient;
  private final MutableLiveData<Boolean> adsFree;
  private boolean connecting;

  @Inject
  public Billing(Context context) {
    adsFree = new MutableLiveData<>();
    billingClient = BillingClient.newBuilder(context)
        .setListener(
            (responseCode, purchases) ->
                adsFree.setValue(purchases != null && !purchases.isEmpty()))
        .build();
    runWhenConnected(this::queryPurchases);
  }

  public LiveData<Boolean> adsFree() {
    runWhenConnected(this::queryPurchases);
    return adsFree;
  }

  public void launchAdsFreePurchaseFlow(Activity activity) {
    runWhenConnected(() -> {
      if (!billingClient.isReady()) {
        return;
      }

      billingClient.launchBillingFlow(
          activity,
          BillingFlowParams.newBuilder()
              .setSku(SKU)
              .setType(BillingClient.SkuType.INAPP)
              .build());
    });
  }

  private void runWhenConnected(Runnable runnable) {
    if (billingClient.isReady()) {
      runnable.run();
      return;
    }

    if (!billingClient.isReady() && !connecting) {
      synchronized (billingClient) {
        if (!billingClient.isReady() && !connecting) {
          connecting = true;
          billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(int responseCode) {
              connecting = false;
              if (responseCode == BillingClient.BillingResponse.OK) {
                runnable.run();
              }
            }

            @Override
            public void onBillingServiceDisconnected() {
              connecting = false;
            }
          });
        }
      }
    }
  }

  private void queryPurchases() {
    if (!billingClient.isReady()) {
      return;
    }

    billingClient.queryPurchaseHistoryAsync(
        BillingClient.SkuType.INAPP, (responseCode1, purchasesList) -> {
          if (responseCode1 == BillingClient.BillingResponse.OK) {
            adsFree.setValue(purchasesList != null && !purchasesList.isEmpty());
          }
        });
  }
}
