package com.ark.android.arkwallpaper.ui.fragment;

import android.accounts.Account;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.data.model.AlbumObject;
import com.ark.android.arkwallpaper.presenter.contract.AlbumFragmentContract;
import com.ark.android.arkwallpaper.presenter.contract.HomeContract;
import com.ark.android.arkwallpaper.presenter.presenterImp.AlbumsPresenter;
import com.ark.android.gallerylib.data.GallaryDataBaseContract;
import com.ark.android.onlinesourcelib.FiveHundredSyncAdapter;
import com.ark.android.onlinesourcelib.FivePxGenericAccountService;
import com.ark.android.onlinesourcelib.FivePxSyncUtils;
import com.ark.android.onlinesourcelib.TumblrGenericAccountService;
import com.ark.android.onlinesourcelib.TumblrManager;
import com.ark.android.onlinesourcelib.TumblrSyncAdapter;
import com.ark.android.onlinesourcelib.TumblrSyncUtils;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action1;

/**
 * Created by ahmed-basyouni on 4/22/17.
 */

public class AlbumsFragment extends Fragment implements
        HomeContract.OnHomePagerChange, AlbumFragmentContract.IAlbumsView, LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    @BindView(R.id.albumsList)
    RecyclerView albumList;
    @BindView(R.id.addAlbum)
    FloatingActionButton addAlbum;
    @BindView(R.id.floatingMenu)
    FloatingActionButton floatingMenu;
    @BindView(R.id.addTumblr)
    FloatingActionButton addtumblr;
    @BindView(R.id.addFive)
    FloatingActionButton add500px;
    @BindView(R.id.addFiveContainer)
    LinearLayout addFiveContainer;
    @BindView(R.id.addTumblrContainer)
    LinearLayout addTumblrContainer;
    @BindView(R.id.addAlbumContainer)
    LinearLayout addAlbumContainer;
    @BindView(R.id.addtumblrText)
    TextView addTumblrText;
    @BindView(R.id.addFiveText)
    TextView addFiveText;

    private AlbumsPresenter albumsPresenter;
    int count = 0;
    private static final int LOADER_ID = 214;
    private Animation fabOpen;
    private Animation fabContainerOpen;
    private Animation fabClose;
    private Animation rotateForward;
    private Animation rotateBackward;
    private boolean isFabOpen;
    private Dialog albumNameDialog;
    private Dialog fivePxDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.albums_fragment, container, false);
        ButterKnife.bind(this, rootView);

        albumsPresenter = new AlbumsPresenter(this);

        getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        fabOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_open);
        fabContainerOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_container_open);
        fabClose = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_backward);

        floatingMenu.setOnClickListener(this);
        addAlbum.setOnClickListener(this);
        addtumblr.setOnClickListener(this);
        add500px.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void showAddAlbumDialog(final boolean isTumblr, String editFieldText, final Action1<String> action1) {
        albumNameDialog = new Dialog(getActivity());
        albumNameDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        albumNameDialog.setContentView(R.layout.add_album_dialog);

        final EditText albumNameField = (EditText) albumNameDialog.findViewById(R.id.albumNameField);
        if (isTumblr)
            albumNameField.setHint(getString(R.string.blogName));
        else
            albumNameField.setHint(getString(R.string.albumName));

        if (editFieldText != null)
            albumNameField.setText(editFieldText);

        Button okButton = (Button) albumNameDialog.findViewById(R.id.addAlbum);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!albumNameField.getText().toString().isEmpty()) {
                    final String albumName = albumNameField.getText().toString();
                    if (isTumblr) {
                        TumblrManager.getInstance().checkBlogInfo(albumNameField.getText().toString(), action1);
                        albumNameDialog.dismiss();
                    } else {
                        albumsPresenter.addAlbum(albumNameField.getText().toString(), GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_GALLERY, null, albumName);
                        albumNameDialog.dismiss();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.no_name_provided), Toast.LENGTH_SHORT).show();
                }
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(albumNameDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        albumNameDialog.getWindow().setAttributes(lp);

        albumNameDialog.show();
    }

    @Override
    public void showAdd500PxDialog(int index, final Action1<String> action1) {
        fivePxDialog = new Dialog(getActivity());
        fivePxDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        fivePxDialog.setContentView(R.layout.add_fivepx_dialog);

        final Spinner spinner = (Spinner) fivePxDialog.findViewById(R.id.catSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.fivePx_Cat, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setSelection(index);

        Button okButton = (Button) fivePxDialog.findViewById(R.id.addAlbum);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(action1 == null)
                    add500PxAlbum(spinner.getItemAtPosition(spinner.getSelectedItemPosition()).toString());
                else
                    action1.call(spinner.getItemAtPosition(spinner.getSelectedItemPosition()).toString());
                fivePxDialog.dismiss();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(fivePxDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        fivePxDialog.getWindow().setAttributes(lp);

        fivePxDialog.show();
    }

    @Override
    public void onFragmentSelected() {

    }

    @Override
    public RecyclerView getAlbumList() {
        return albumList;
    }

    @Override
    public Activity getActivityContext() {
        return getActivity();
    }

    @Override
    public void showSnackWithMsg(String msg) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), GallaryDataBaseContract.AlbumsTable.CONTENT_URI, new String[]{BaseColumns._ID, GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_NAME,
                GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_IMAGE_URI
                , GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_ENABLED, GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_TYPE, GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_COUNT
                , GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_TUMBLR_BLOG_NAME, GallaryDataBaseContract.AlbumsTable.COLUMN_ALBUM_Five_PX_CATEGORY}, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.getCount() > 0) {
            albumList.setVisibility(View.VISIBLE);
            albumsPresenter.onAlbumsLoaded(data);
        } else {
            albumList.setVisibility(View.GONE);
            albumsPresenter.clearAlbums();
            if (albumsPresenter.getAlbums() != null)
                checkFloatingAlbumState();
        }
    }

    @Override
    public void checkFloatingAlbumState() {
        boolean tumblrExist = false;
        boolean fivePxExist = false;
        for (AlbumObject albumObject : albumsPresenter.getAlbums()) {
            if (albumObject.getAlbumName().equals(TumblrSyncAdapter.ALBUM_NAME)) {
                addtumblr.setEnabled(false);
                addTumblrText.setText(getString(R.string.tumblrAlreadyExist));
                tumblrExist = true;
            }
            if (albumObject.getAlbumName().equals(FiveHundredSyncAdapter.ALBUM_NAME)) {
                add500px.setEnabled(false);
                addFiveText.setText(getString(R.string.fivePxAlreadyExist));
                fivePxExist = true;
            }
        }

        if (!fivePxExist) {
            add500px.setEnabled(true);
            addFiveText.setText(getString(R.string.addfivePxAlbum));
        }
        if (!tumblrExist) {
            addtumblr.setEnabled(true);
            addTumblrText.setText(getString(R.string.addTumblrAlbum));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.floatingMenu:
                animateFAB();
                break;
            case R.id.addAlbum:
                showAddAlbumDialog(false, null, null);
                animateFAB();
                break;
            case R.id.addTumblr:
                showAddAlbumDialog(true, null, getTumblrAction());
                animateFAB();
                break;
            case R.id.addFive:
                showAdd500PxDialog(0, null);
                break;
        }
    }

    Action1<String> getTumblrAction() {
        return new Action1<String>() {
            @Override
            public void call(String albumName) {
                if (albumName != null) {
                    Bundle bundle = new Bundle();
                    bundle.putString("albumName", albumName);
                    bundle.putBoolean("isPer", true);
                    albumsPresenter.addAlbum(TumblrSyncAdapter.ALBUM_NAME, GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_TUMBLR, null, albumName);
                    if (!TumblrSyncUtils.CreateSyncAccount(getActivity(), bundle)) {
                        TumblrSyncUtils.TriggerRefresh(bundle);
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.blogNotFound), Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void add500PxAlbum(String s) {
        Bundle bundle = new Bundle();
        bundle.putString(FiveHundredSyncAdapter.CAT_KEY, s);
        bundle.putBoolean("isPer", true);
        albumsPresenter.addAlbum(FiveHundredSyncAdapter.ALBUM_NAME, GallaryDataBaseContract.AlbumsTable.ALBUM_TYPE_PX, s, null);
        if (!FivePxSyncUtils.CreateSyncAccount(getActivity(), bundle)) {
            FivePxSyncUtils.TriggerRefresh(bundle);
        }
    }

    private void animateFAB() {

        if (isFabOpen) {
            floatingMenu.startAnimation(rotateBackward);
            addFiveContainer.startAnimation(fabClose);
            addAlbumContainer.startAnimation(fabClose);
            addTumblrContainer.startAnimation(fabClose);
            add500px.startAnimation(fabClose);
            addAlbum.startAnimation(fabClose);
            addtumblr.startAnimation(fabClose);
            addAlbum.setClickable(false);
            addtumblr.setClickable(false);
            add500px.setClickable(false);
            isFabOpen = false;
        } else {
            floatingMenu.startAnimation(rotateForward);
            addFiveContainer.startAnimation(fabContainerOpen);
            addTumblrContainer.startAnimation(fabContainerOpen);
            addAlbumContainer.startAnimation(fabContainerOpen);
            add500px.startAnimation(fabOpen);
            addAlbum.startAnimation(fabOpen);
            addtumblr.startAnimation(fabOpen);
            addAlbum.setClickable(true);
            addtumblr.setClickable(true);
            add500px.setClickable(true);
            isFabOpen = true;
        }
    }

    @Override
    public void onEmptyAlbums() {

    }

    @Override
    public void registerObservers(boolean shouldAdd500PxObserver, boolean shouldAddTumblrObserver) {

        if (shouldAdd500PxObserver) {
            Account account = FivePxGenericAccountService.GetAccount(FivePxSyncUtils.ACCOUNT_TYPE);

            boolean syncActive = ContentResolver.isSyncActive(
                    account, GallaryDataBaseContract.GALLERY_AUTHORITY);
            boolean syncPending = ContentResolver.isSyncPending(
                    account, GallaryDataBaseContract.GALLERY_AUTHORITY);

            if (!syncActive && !syncPending && albumsPresenter.get500PxAlbum().getCount() == 0) {
                Bundle bundle = new Bundle();
                bundle.putString(FiveHundredSyncAdapter.CAT_KEY, albumsPresenter.get500PxAlbum().getFivePxCategoryName());
                bundle.putBoolean("isPer", true);
                FivePxSyncUtils.TriggerRefresh(bundle);
            }

        }

        if (shouldAddTumblrObserver) {

            Account account = TumblrGenericAccountService.GetAccount(TumblrSyncUtils.ACCOUNT_TYPE);

            boolean syncActive = ContentResolver.isSyncActive(
                    account, GallaryDataBaseContract.GALLERY_AUTHORITY);
            boolean syncPending = ContentResolver.isSyncPending(
                    account, GallaryDataBaseContract.GALLERY_AUTHORITY);

            if (!syncActive && !syncPending && albumsPresenter.getTumblrAlbum().getCount() == 0) {
                Bundle bundle = new Bundle();
                bundle.putString("albumName", albumsPresenter.getTumblrAlbum().getTumblrBlogName());
                bundle.putBoolean("isPer", true);
                TumblrSyncUtils.TriggerRefresh(bundle);
            }

        }
    }

}
