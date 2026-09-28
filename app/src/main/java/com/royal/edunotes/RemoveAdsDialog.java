package com.royal.edunotes;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

/**
 * Dialog to present Remove Ads / Premium Subscription to the user.
 */
public class RemoveAdsDialog {

    public interface OnDismissListener {
        void onDismissed(boolean isPurchased);
    }

    public static void show(final Activity activity) {
        show(activity, null);
    }

    public static void show(final Activity activity, final OnDismissListener listener) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;

        final BillingManager billingManager = BillingManager.getInstance(activity);
        final boolean alreadyPremium = BillingManager.isAdsRemoved(activity);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_remove_ads, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        TextView tvTitle = dialogView.findViewById(R.id.tv_premium_title);
        TextView tvSubtitle = dialogView.findViewById(R.id.tv_premium_subtitle);
        TextView tvPrice = dialogView.findViewById(R.id.tv_price);
        View layoutPrice = dialogView.findViewById(R.id.layout_price);
        TextView tvAlreadyPremium = dialogView.findViewById(R.id.tv_already_premium);
        Button btnSubscribe = dialogView.findViewById(R.id.btn_subscribe);
        TextView tvRestorePurchase = dialogView.findViewById(R.id.tv_restore_purchase);
        TextView tvCancel = dialogView.findViewById(R.id.tv_cancel);

        // Fetch formatted price if available from Google Play
        String priceText = billingManager.getFormattedPrice(BillingManager.PRODUCT_REMOVE_ADS_YEARLY, "₹200 / Year");
        tvPrice.setText(priceText);

        if (alreadyPremium) {
            tvTitle.setText("Premium Active 👑");
            tvSubtitle.setText("You are enjoying an ad-free learning experience!");
            layoutPrice.setVisibility(View.GONE);
            tvAlreadyPremium.setVisibility(View.VISIBLE);
            btnSubscribe.setText("Close");
            tvRestorePurchase.setVisibility(View.GONE);
            tvCancel.setVisibility(View.GONE);

            btnSubscribe.setOnClickListener(v -> dialog.dismiss());
        } else {
            btnSubscribe.setText("Upgrade Now");

            btnSubscribe.setOnClickListener(v -> {
                dialog.dismiss();
                billingManager.launchPurchaseFlow(activity, BillingManager.PRODUCT_REMOVE_ADS_YEARLY);
            });

            tvRestorePurchase.setOnClickListener(v -> {
                Toast.makeText(activity, "Checking previous purchases...", Toast.LENGTH_SHORT).show();
                billingManager.queryPurchases();
                dialog.dismiss();
            });

            tvCancel.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.setOnDismissListener(d -> {
            if (listener != null) {
                listener.onDismissed(BillingManager.isAdsRemoved(activity));
            }
        });

        dialog.show();
    }
}
