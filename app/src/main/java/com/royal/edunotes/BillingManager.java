package com.royal.edunotes;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages Google Play Billing for In-App Purchases and Subscriptions.
 * Handles purchasing "Remove Ads" (e.g. ₹200 for 1 Year subscription or lifetime),
 * verifying purchase receipts, handling renewals, and caching status.
 */
public class BillingManager implements PurchasesUpdatedListener {

    private static final String TAG = "BillingManager";
    private static final String PREF_NAME = "billing_prefs";
    private static final String KEY_IS_ADS_REMOVED = "is_ads_removed";
    private static final String KEY_PURCHASE_TIME = "purchase_time";

    // Subscription & In-App Product IDs
    // Set up this product ID in Google Play Console -> Monetize -> Subscriptions
    public static final String PRODUCT_REMOVE_ADS_YEARLY = "remove_ads_yearly";
    // Optional fallback / one-time product ID (if set up in Play Console -> In-app products)
    public static final String PRODUCT_REMOVE_ADS_LIFETIME = "remove_ads_lifetime";

    private static volatile BillingManager instance;
    private final Context appContext;
    private final SharedPreferences prefs;
    private final Handler mainHandler;

    private BillingClient billingClient;
    private boolean isServiceConnected = false;
    private final Map<String, ProductDetails> productDetailsMap = new HashMap<>();

    public interface BillingCallback {
        void onPurchaseSuccess();
        void onPurchaseFailure(int responseCode, String message);
        void onProductsLoaded();
    }

    private final List<BillingCallback> callbacks = new ArrayList<>();

    public static BillingManager getInstance(Context context) {
        if (instance == null) {
            synchronized (BillingManager.class) {
                if (instance == null) {
                    instance = new BillingManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private BillingManager(Context context) {
        this.appContext = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.mainHandler = new Handler(Looper.getMainLooper());
        initBillingClient();
    }

    /**
     * Helper to check if ads should be suppressed for this user.
     */
    public static boolean isAdsRemoved(Context context) {
        if (context == null) return false;
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_IS_ADS_REMOVED, false);
    }

    public void setAdsRemoved(boolean removed) {
        prefs.edit().putBoolean(KEY_IS_ADS_REMOVED, removed).apply();
    }

    public void registerCallback(BillingCallback callback) {
        if (callback != null && !callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    public void unregisterCallback(BillingCallback callback) {
        callbacks.remove(callback);
    }

    private void initBillingClient() {
        PendingPurchasesParams pendingPurchasesParams = PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build();

        billingClient = BillingClient.newBuilder(appContext)
                .setListener(this)
                .enablePendingPurchases(pendingPurchasesParams)
                .build();

        startConnection(null);
    }

    public void startConnection(@Nullable final Runnable onConnectedRunnable) {
        if (billingClient == null) return;
        if (billingClient.isReady()) {
            isServiceConnected = true;
            if (onConnectedRunnable != null) onConnectedRunnable.run();
            return;
        }

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing service connected successfully");
                    isServiceConnected = true;
                    // Query current purchases to refresh status
                    queryPurchases();
                    // Query product details so we have pricing info ready
                    queryAvailableProducts();
                    if (onConnectedRunnable != null) {
                        mainHandler.post(onConnectedRunnable);
                    }
                } else {
                    Log.w(TAG, "Billing setup failed: " + billingResult.getDebugMessage());
                    isServiceConnected = false;
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected");
                isServiceConnected = false;
            }
        });
    }

    public void queryAvailableProducts() {
        if (!isServiceConnected) {
            startConnection(this::queryAvailableProducts);
            return;
        }

        // Query Subscription product
        List<QueryProductDetailsParams.Product> subProducts = Collections.singletonList(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_REMOVE_ADS_YEARLY)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
        );

        QueryProductDetailsParams subParams = QueryProductDetailsParams.newBuilder()
                .setProductList(subProducts)
                .build();

        billingClient.queryProductDetailsAsync(subParams, (billingResult, productDetailsResult) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsResult != null && productDetailsResult.getProductDetailsList() != null) {
                for (ProductDetails details : productDetailsResult.getProductDetailsList()) {
                    productDetailsMap.put(details.getProductId(), details);
                    Log.d(TAG, "Found product: " + details.getProductId() + " -> " + details.getName());
                }
                notifyProductsLoaded();
            }
        });

        // Also query In-App product (in case user configured lifetime remove ads as in-app)
        List<QueryProductDetailsParams.Product> inAppProducts = Collections.singletonList(
                QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_REMOVE_ADS_LIFETIME)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
        );

        QueryProductDetailsParams inAppParams = QueryProductDetailsParams.newBuilder()
                .setProductList(inAppProducts)
                .build();

        billingClient.queryProductDetailsAsync(inAppParams, (billingResult, productDetailsResult) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsResult != null && productDetailsResult.getProductDetailsList() != null) {
                for (ProductDetails details : productDetailsResult.getProductDetailsList()) {
                    productDetailsMap.put(details.getProductId(), details);
                }
                notifyProductsLoaded();
            }
        });
    }

    public ProductDetails getProductDetails(String productId) {
        return productDetailsMap.get(productId);
    }

    public String getFormattedPrice(String productId, String fallbackPrice) {
        ProductDetails details = productDetailsMap.get(productId);
        if (details != null) {
            if (BillingClient.ProductType.SUBS.equals(details.getProductType())) {
                List<ProductDetails.SubscriptionOfferDetails> offers = details.getSubscriptionOfferDetails();
                if (offers != null && !offers.isEmpty()) {
                    List<ProductDetails.PricingPhase> phases = offers.get(0).getPricingPhases().getPricingPhaseList();
                    if (phases != null && !phases.isEmpty()) {
                        return phases.get(0).getFormattedPrice();
                    }
                }
            } else if (details.getOneTimePurchaseOfferDetails() != null) {
                return details.getOneTimePurchaseOfferDetails().getFormattedPrice();
            }
        }
        return fallbackPrice;
    }

    public void launchPurchaseFlow(Activity activity, String productId) {
        if (!isServiceConnected) {
            startConnection(() -> launchPurchaseFlow(activity, productId));
            return;
        }

        ProductDetails details = productDetailsMap.get(productId);
        if (details == null) {
            Toast.makeText(activity, "Product info is loading, please try again in a moment.", Toast.LENGTH_SHORT).show();
            queryAvailableProducts();
            return;
        }

        BillingFlowParams.ProductDetailsParams.Builder productDetailsParamsBuilder =
                BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details);

        if (BillingClient.ProductType.SUBS.equals(details.getProductType())) {
            List<ProductDetails.SubscriptionOfferDetails> offers = details.getSubscriptionOfferDetails();
            if (offers != null && !offers.isEmpty()) {
                productDetailsParamsBuilder.setOfferToken(offers.get(0).getOfferToken());
            } else {
                Toast.makeText(activity, "Subscription plan unavailable.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(Collections.singletonList(productDetailsParamsBuilder.build()))
                .build();

        BillingResult result = billingClient.launchBillingFlow(activity, billingFlowParams);
        if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Failed to launch billing flow: " + result.getDebugMessage());
        }
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        int responseCode = billingResult.getResponseCode();
        if (responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User cancelled purchase flow");
        } else {
            Log.w(TAG, "Purchase error: " + billingResult.getDebugMessage() + " (Code: " + responseCode + ")");
            notifyPurchaseFailure(responseCode, billingResult.getDebugMessage());
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            boolean matchesOurProduct = false;
            for (String product : purchase.getProducts()) {
                if (PRODUCT_REMOVE_ADS_YEARLY.equals(product) || PRODUCT_REMOVE_ADS_LIFETIME.equals(product)) {
                    matchesOurProduct = true;
                    break;
                }
            }

            if (matchesOurProduct) {
                // Grant entitlement
                setAdsRemoved(true);
                prefs.edit().putLong(KEY_PURCHASE_TIME, purchase.getPurchaseTime()).apply();
                Log.d(TAG, "Ads successfully removed! Entitlement granted.");

                // Acknowledge the purchase if not already acknowledged
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.getPurchaseToken())
                                    .build();

                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            Log.d(TAG, "Purchase acknowledged successfully");
                        } else {
                            Log.e(TAG, "Failed to acknowledge purchase: " + billingResult.getDebugMessage());
                        }
                    });
                }

                notifyPurchaseSuccess();
            }
        }
    }

    public void queryPurchases() {
        if (!isServiceConnected) {
            startConnection(this::queryPurchases);
            return;
        }

        // Query active subscriptions
        QueryPurchasesParams subParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build();

        billingClient.queryPurchasesAsync(subParams, (billingResult, list) -> {
            boolean hasActiveSub = false;
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                for (Purchase purchase : list) {
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                        for (String prod : purchase.getProducts()) {
                            if (PRODUCT_REMOVE_ADS_YEARLY.equals(prod)) {
                                hasActiveSub = true;
                                handlePurchase(purchase);
                                break;
                            }
                        }
                    }
                }
            }

            // Also check one-time in-app purchases
            final boolean activeSubFound = hasActiveSub;
            QueryPurchasesParams inAppParams = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build();

            billingClient.queryPurchasesAsync(inAppParams, (result, inAppList) -> {
                boolean hasActiveInApp = false;
                if (result.getResponseCode() == BillingClient.BillingResponseCode.OK && inAppList != null) {
                    for (Purchase purchase : inAppList) {
                        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                            for (String prod : purchase.getProducts()) {
                                if (PRODUCT_REMOVE_ADS_LIFETIME.equals(prod)) {
                                    hasActiveInApp = true;
                                    handlePurchase(purchase);
                                    break;
                                }
                            }
                        }
                    }
                }

                // If neither active subscription nor lifetime in-app purchase was returned,
                // and the user was previously marked as ads removed, revoke entitlement
                // (subscription expired or was refunded).
                if (!activeSubFound && !hasActiveInApp) {
                    setAdsRemoved(false);
                    Log.d(TAG, "No active subscription or purchase found. Ads enabled.");
                }
            });
        });
    }

    private void notifyPurchaseSuccess() {
        mainHandler.post(() -> {
            for (BillingCallback cb : callbacks) {
                cb.onPurchaseSuccess();
            }
        });
    }

    private void notifyPurchaseFailure(int code, String message) {
        mainHandler.post(() -> {
            for (BillingCallback cb : callbacks) {
                cb.onPurchaseFailure(code, message);
            }
        });
    }

    private void notifyProductsLoaded() {
        mainHandler.post(() -> {
            for (BillingCallback cb : callbacks) {
                cb.onProductsLoaded();
            }
        });
    }
}
