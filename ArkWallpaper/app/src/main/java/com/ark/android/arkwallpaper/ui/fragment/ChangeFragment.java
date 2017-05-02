package com.ark.android.arkwallpaper.ui.fragment;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ark.android.arkwallpaper.Constants;
import com.ark.android.arkwallpaper.R;
import com.ark.android.arkwallpaper.WallpaperApp;
import com.ark.android.arkwallpaper.presenter.contract.HomeContract;
import com.ark.android.arkwallpaper.utils.WallPaperUtils;
import com.ark.android.arkwallpaper.utils.uiutils.GlideContentProviderLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 * Created by ahmed-basyouni on 4/17/17.
 */

public class ChangeFragment extends Fragment implements HomeContract.OnHomePagerChange
        , CompoundButton.OnCheckedChangeListener
        , TextWatcher, AdapterView.OnItemSelectedListener,SharedPreferences.OnSharedPreferenceChangeListener {

    @BindView(R.id.lastWallpaperContainer)
    CardView lastWallpaperContainer;
    @BindView(R.id.lastSetImage)
    ImageView lastChosenWallpaper;
    @BindView(R.id.changeEveryCheckBox)
    CheckBox changeEveryCheckBox;
    @BindView(R.id.changeEveryField)
    EditText changeEveryField;
    @BindView(R.id.changeEveryUnit)
    Spinner changeEveryUnit;
    @BindView(R.id.changeWithUnlock)
    CheckBox changeWithUnlockCheckBox;
    @BindView(R.id.changeWithDoubleTap)
    CheckBox changeWithDoubleTap;

    @Override
    public void onFragmentSelected() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_change, container, false);

        ButterKnife.bind(this, rootView);
        checkWallpaper();
        checkCheckBoxesState();
        setCheckBoxesListener();
        setChangeUnitSpinner();
        changeEveryField.addTextChangedListener(this);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        return rootView;
    }

    private void checkCheckBoxesState() {
        changeEveryCheckBox.setChecked(WallPaperUtils.isChangeWithInterval());
        changeWithDoubleTap.setChecked(WallPaperUtils.isChangedWithDoubleTap());
        changeWithUnlockCheckBox.setChecked(WallPaperUtils.isChangeWithUnlock());
        changeEveryField.setText(String.valueOf(WallPaperUtils.getChangeWallpaperInterval()));
    }

    private void setChangeUnitSpinner() {

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.change_unit, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        changeEveryUnit.setAdapter(adapter);

        changeEveryUnit.setSelection(WallPaperUtils.getChangeWallpaperUnit());

        changeEveryUnit.setOnItemSelectedListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setCheckBoxesListener() {
        changeEveryCheckBox.setOnCheckedChangeListener(this);
        changeWithUnlockCheckBox.setOnCheckedChangeListener(this);
        changeWithDoubleTap.setOnCheckedChangeListener(this);
    }

    private void checkWallpaper() {
        if (WallPaperUtils.getCurrentWallpaper() != null) {

            lastWallpaperContainer.setVisibility(View.VISIBLE);
            Glide.with(getActivity())
                    .using(new GlideContentProviderLoader(getActivity()))
                    .load(Uri.parse(WallPaperUtils.getCurrentWallpaper()))
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(lastChosenWallpaper);
        } else {
            lastWallpaperContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.changeEveryCheckBox:
                if (changeEveryField.getText().toString().isEmpty()) {
                    fieldIsEmpty();
                    changeEveryCheckBox.setChecked(false);
                } else
                    WallPaperUtils.setChangeInterval(Integer.parseInt(changeEveryField.getText().toString())
                            , Constants.INTERVAL_MODE.values()[changeEveryUnit.getSelectedItemPosition()]);

                break;

            case R.id.changeWithDoubleTap:
                WallPaperUtils.setChangeWithDoubleTap(isChecked);
                break;

            case R.id.changeWithUnlock:
                WallPaperUtils.setChangeWithUnlock(isChecked);
                break;
        }
    }

    private void fieldIsEmpty() {
        Toast.makeText(getActivity(), getString(R.string.changeValueIsEmpty), Toast.LENGTH_SHORT).show();
    }

    int check = 0;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(++check > 1) {
            if (changeEveryCheckBox.isChecked())
                if (changeEveryField.getText().toString().isEmpty()) {
                    fieldIsEmpty();
                    changeEveryCheckBox.setChecked(false);
                } else
                    WallPaperUtils.setChangeInterval(Integer.parseInt(changeEveryField.getText().toString())
                            , Constants.INTERVAL_MODE.values()[position]);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key != null && key.equals(Constants.CURRENT_WALLPAPER_KEY))
            checkWallpaper();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(changeEveryCheckBox.isChecked() && !changeEveryField.getText().toString().isEmpty())
            WallPaperUtils.setChangeInterval(Integer.parseInt(changeEveryField.getText().toString())
                    , Constants.INTERVAL_MODE.values()[changeEveryUnit.getSelectedItemPosition()]);
    }
}
