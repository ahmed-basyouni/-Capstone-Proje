package com.ark.android.onlinesourcelib.syncUtils;

import android.accounts.Account;

import com.ark.android.onlinesourcelib.syncadapter.FiveHundredSyncAdapter;
import com.ark.android.onlinesourcelib.Account.FivePxGenericAccountService;

/**
 *
 * Created by ahmed-basyouni on 4/25/17.
 */

public class FivePxSyncUtils extends AbstractSyncUtils{

    private static final String PREF_SETUP_COMPLETE = "fivePx_setup_complete";
    // Value below must match the account type specified in res/xml/fivepx_syncadapteradapter.xml
    public static final String ACCOUNT_TYPE = "com.ark.android.arkwallpaperfivehundred.account";




    @Override
    protected String getSetupCompleteKey() {
        return PREF_SETUP_COMPLETE;
    }

    @Override
    protected Account getAccount() {
        return FivePxGenericAccountService.GetAccount(ACCOUNT_TYPE);
    }

    @Override
    protected String getBundleDownloadKey() {
        return FiveHundredSyncAdapter.CAT_KEY;
    }

    public static FivePxSyncUtils getInstance(){
        return new FivePxSyncUtils();
    }
}
