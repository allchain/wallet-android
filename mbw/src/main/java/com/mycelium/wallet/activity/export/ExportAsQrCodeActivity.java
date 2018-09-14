/*
 * Copyright 2013, 2014 Megion Research and Development GmbH
 *
 * Licensed under the Microsoft Reference Source License (MS-RSL)
 *
 * This license governs use of the accompanying software. If you use the software, you accept this license.
 * If you do not accept the license, do not use the software.
 *
 * 1. Definitions
 * The terms "reproduce," "reproduction," and "distribution" have the same meaning here as under U.S. copyright law.
 * "You" means the licensee of the software.
 * "Your company" means the company you worked for when you downloaded the software.
 * "Reference use" means use of the software within your company as a reference, in read only form, for the sole purposes
 * of debugging your products, maintaining your products, or enhancing the interoperability of your products with the
 * software, and specifically excludes the right to distribute the software outside of your company.
 * "Licensed patents" means any Licensor patent claims which read directly on the software as distributed by the Licensor
 * under this license.
 *
 * 2. Grant of Rights
 * (A) Copyright Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free copyright license to reproduce the software for reference use.
 * (B) Patent Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free patent license under licensed patents for reference use.
 *
 * 3. Limitations
 * (A) No Trademark License- This license does not grant you any rights to use the Licensor’s name, logo, or trademarks.
 * (B) If you begin patent litigation against the Licensor over patents that you think may apply to the software
 * (including a cross-claim or counterclaim in a lawsuit), your license to the software ends automatically.
 * (C) The software is licensed "as-is." You bear the risk of using it. The Licensor gives no express warranties,
 * guarantees or conditions. You may have additional consumer rights under your local laws which this license cannot
 * change. To the extent permitted under your local laws, the Licensor excludes the implied warranties of merchantability,
 * fitness for a particular purpose and non-infringement.
 */

package com.mycelium.wallet.activity.export;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mycelium.wallet.MbwManager;
import com.mycelium.wallet.R;
import com.mycelium.wallet.Utils;
import com.mycelium.wallet.activity.util.QrImageView;
import com.mycelium.wapi.wallet.ExportableAccount;

public class ExportAsQrCodeActivity extends Activity {
   public static final String ACCOUNT = "account";
   private MbwManager _mbwManager;
   private ExportableAccount.Data accountData;
   private SwitchCompat swSelectData;
   private boolean hasWarningAccepted = false;
   private RadioGroup rgKeyTypes;
   private final String[] allKeyTypes =
           {"xpub", "ypub", "zpub",
           "xprv", "yprv", "zprv"};

   public static Intent getIntent(Activity activity, ExportableAccount.Data accountData) {
      Intent intent = new Intent(activity, ExportAsQrCodeActivity.class);
      intent.putExtra(ACCOUNT, accountData);
      return intent;
   }

   /**
    * Called when the activity is first created.
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
      super.onCreate(savedInstanceState);
      setContentView(R.layout.export_as_qr_code_activity);
      // Prevent the OS from taking screenshots of this activity
      Utils.preventScreenshots(this);

      // Get base58 encoded private key
      _mbwManager = MbwManager.getInstance(getApplication());
      accountData = (ExportableAccount.Data) getIntent().getSerializableExtra(ACCOUNT);
      if (accountData == null ||
            (!accountData.publicData.isPresent() && !accountData.privateData.isPresent())
            ) {
         finish();
         return;
      }


      // hide the priv/pub switch, if this is a watch-only accountData
      swSelectData = (SwitchCompat) findViewById(R.id.swSelectData);
      TextView privateText = (TextView) findViewById(R.id.prv_key);
      TextView publicText = (TextView) findViewById(R.id.pub_key);
      rgKeyTypes = findViewById(R.id.rg_key_types);
      if (accountData.privateData.isPresent()) {
         swSelectData.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
               updateData();
            }
         });
      } else {
         swSelectData.setVisibility(View.GONE);
         privateText.setVisibility(View.GONE);
         publicText.setVisibility(View.GONE);
      }

      // listener for exporting xpub/ypub/zpub  /  xprv/yprv/zprv
      rgKeyTypes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
               case R.id.rb_key_x:
                  if (isPrivateDataSelected()) {/* todo xpriv*/} else {/* todo xpub*/}
                  break;
               case R.id.rb_key_y:
                  if (isPrivateDataSelected()) {/* todo ypriv*/} else {/* todo ypub*/}
                  break;
               case R.id.rb_key_z:
                  if (isPrivateDataSelected()) {/* todo zpriv*/} else {/* todo zpub*/}
            }
         }
      });

      findViewById(R.id.llPrivKeyWarning).setOnLongClickListener(new View.OnLongClickListener() {
         @Override
         public boolean onLongClick(View view) {
            hasWarningAccepted = true;
            updateData();
            return true;
         }
      });
      updateData();
   }

   private void updateData() {
      if (isPrivateDataSelected()) {
         showPrivateData();
         updateKeyTypes(true);
      } else {
         showPublicData();
         updateKeyTypes(false);
      }

      ((RadioButton) rgKeyTypes.getChildAt(0)).setChecked(true);
   }

   /**
    * This method updates the texts for the radio buttons. Change the allKeyTypes array for your need
    * @param forPrivate key or for public
    */
   private void updateKeyTypes(boolean forPrivate)
   {
      if(forPrivate)
      {
         for (int i = 0; i < rgKeyTypes.getChildCount(); i++)
         {
            ((RadioButton) rgKeyTypes.getChildAt(i)).setText(allKeyTypes[3+i]);
         }
      }
      else
      {
         for (int i = 0; i < rgKeyTypes.getChildCount(); i++)
         {
            ((RadioButton) rgKeyTypes.getChildAt(i)).setText(allKeyTypes[i]);
         }
      }
   }

   private boolean isPrivateDataSelected() {
      if (accountData.privateData.isPresent()) {
         return swSelectData.isChecked();
      } else {
         return false;
      }
   }

   private void setWarningVisibility(boolean showWarning) {
      if (showWarning) {
         findViewById(R.id.llPrivKeyWarning).setVisibility(View.VISIBLE);
         findViewById(R.id.ivQrCode).setVisibility(View.GONE);
         findViewById(R.id.tvShowData).setVisibility(View.GONE);
         findViewById(R.id.btCopyToClipboard).setVisibility(View.GONE);
         findViewById(R.id.btShare).setVisibility(View.GONE);
         findViewById(R.id.tvQrTapHint).setVisibility(View.GONE);
      } else {
         findViewById(R.id.llPrivKeyWarning).setVisibility(View.GONE);
         findViewById(R.id.ivQrCode).setVisibility(View.VISIBLE);
         findViewById(R.id.tvShowData).setVisibility(View.VISIBLE);
         findViewById(R.id.btCopyToClipboard).setVisibility(View.VISIBLE);
         findViewById(R.id.btShare).setVisibility(View.VISIBLE);
         findViewById(R.id.tvQrTapHint).setVisibility(View.VISIBLE);
      }
   }

   private void showPrivateData() {

      if (hasWarningAccepted) {
         setWarningVisibility(false);
         String privateData = accountData.privateData.get();
         showData(privateData);
      } else {
         setWarningVisibility(true);
      }
      ((TextView) findViewById(R.id.tvWarning)).setText(this.getString(R.string.export_warning_privkey));

   }

   private void showPublicData() {
      setWarningVisibility(false);
      String publicData = accountData.publicData.get();
      showData(publicData);
      ((TextView) findViewById(R.id.tvWarning)).setText(this.getString(R.string.export_warning_pubkey));

   }

   private void showData(final String data) {
      // Set QR code
      QrImageView iv = (QrImageView) findViewById(R.id.ivQrCode);
      iv.setQrCode(data);

      // split the date in fragments with 8chars and a newline after three parts
      StringBuilder builder = new StringBuilder();
      int cnt = 0;
      for (String part : Utils.stringChopper(data, 8)) {
         cnt++;
         builder.append(part);
         builder.append(cnt % 3 == 0 ? "\n" : " ");
      }
      String fragmentedData = builder.toString();

      ((TextView) findViewById(R.id.tvShowData)).setText(fragmentedData);
      findViewById(R.id.btCopyToClipboard).setOnClickListener(new OnClickListener() {

         @Override
         public void onClick(View arg0) {
            exportToClipboard(data);
         }
      });
      findViewById(R.id.btShare).setOnClickListener(new OnClickListener() {

         @Override
         public void onClick(View arg0) {
            shareData(data);
         }
      });
   }

   @Override
   protected void onPause() {
      // This way we finish the activity when home is pressed, so you are forced
      // to reenter the PIN to see the QR-code again
      finish();
      super.onPause();
   }

   private void exportToClipboard(final String data) {
      if (isPrivateDataSelected()) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.export_to_clipboard_warning).setCancelable(false)
               .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                     Utils.setClipboardString(data, ExportAsQrCodeActivity.this);
                     Toast.makeText(ExportAsQrCodeActivity.this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
                     dialog.dismiss();
                  }
               }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
         });
         AlertDialog alertDialog = builder.create();
         alertDialog.show();
      } else {
         Utils.setClipboardString(data, ExportAsQrCodeActivity.this);
         Toast.makeText(ExportAsQrCodeActivity.this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
      }
   }

   public void shareData(final String data) {
      if (isPrivateDataSelected()) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.export_share_warning).setCancelable(false)
               .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                     Intent s = new Intent(android.content.Intent.ACTION_SEND);
                     s.setType("text/plain");
                     s.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.xpriv_title));
                     s.putExtra(Intent.EXTRA_TEXT, data);
                     startActivity(Intent.createChooser(s, getResources().getString(R.string.share_xpriv)));
                     dialog.dismiss();
                  }
               }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
         });
         AlertDialog alertDialog = builder.create();
         alertDialog.show();
      } else {
         Intent s = new Intent(android.content.Intent.ACTION_SEND);
         s.setType("text/plain");
         s.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.xpub_title));
         s.putExtra(Intent.EXTRA_TEXT, data);
         startActivity(Intent.createChooser(s, getResources().getString(R.string.share_xpub)));
      }

   }
}
