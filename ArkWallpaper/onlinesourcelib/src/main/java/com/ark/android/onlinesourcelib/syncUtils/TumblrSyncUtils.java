package com.ark.android.onlinesourcelib.syncUtils;

import android.accounts.Account;

import com.ark.android.onlinesourcelib.Account.TumblrGenericAccountService;

/**
 * Created by ahmed-basyouni on 4/25/17.
 */

public class TumblrSyncUtils extends AbstractSyncUtils{

    private static final String PREF_SETUP_COMPLETE = "tumblr_setup_complete";
    // Value below must match the account type specified in res/xml/syncadapter.xml
    public static final String ACCOUNT_TYPE = "com.ark.android.arkwallpapertumblr.account";



    public static TumblrSyncUtils getInstance(){
        return new TumblrSyncUtils();
    }



    @Override
    protected String getSetupCompleteKey() {
        return PREF_SETUP_COMPLETE;
    }

    @Override
    protected Account getAccount() {
        return TumblrGenericAccountService.GetAccount(ACCOUNT_TYPE);
    }

    @Override
    protected String getBundleDownloadKey() {
        return "albumName";
    }
}
