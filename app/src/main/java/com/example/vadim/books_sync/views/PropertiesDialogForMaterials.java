package com.example.vadim.books_sync.views;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.vadim.books_sync.R;
import com.example.vadim.books_sync.dagger.AppModule;
import com.example.vadim.books_sync.dagger.DaggerAppComponent;
import com.example.vadim.books_sync.dagger.RoomModule;
import com.example.vadim.books_sync.dao.MaterialDao;
import com.example.vadim.books_sync.dao.MaterialFolderJoinDao;
import com.example.vadim.books_sync.presenters.MaterialPresenter;
import com.example.vadim.books_sync.presenters.StateOfDocument;
import com.example.vadim.books_sync.presenters.StateOwnerProperties;
import com.example.vadim.books_sync.presenters.states_of_file.RemovingFile;
import com.example.vadim.books_sync.presenters.states_of_file.RenamingFile;
import com.example.vadim.books_sync.presenters.states_of_file.SharingFile;
import com.example.vadim.books_sync.views.rx.ObserversForNameDocument;

import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observer;
import io.reactivex.functions.Function;

import static android.app.Activity.RESULT_OK;
import static android.widget.Toast.LENGTH_SHORT;

@SuppressLint("ValidFragment")
public class PropertiesDialogForMaterials extends android.support.v4.app.DialogFragment
        implements StateOwnerProperties, DialogView {

    @BindView(R.id.fileName)
    CustomEditText fileNameEditText;

    @BindView(R.id.applyMaterialName)
    ImageButton applyMaterialName;

    @BindView(R.id.cancelMaterialImageButton)
    ImageButton cancelImageButton;

    @BindView(R.id.renameMaterial)
    ImageButton renameMaterial;

    @BindView(R.id.addToFolder)
    ImageButton addToFolderImageButton;

    @BindView(R.id.trash)
    ImageButton trashImageButton;

    @BindView(R.id.share)
    ImageButton shareImageButton;

    @Inject
    MaterialDao materialDao;

    @Inject
    MaterialFolderJoinDao materialFolderJoinDao;

    private MaterialPresenter materialPresenter;

    private InputMethodManager inputMethodManager;

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint({"InflateParams", "CheckResult", "ResourceAsColor", "RtlHardcoded"})
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View viewProperties;
        if (getContext() instanceof MaterialsOfFolderActivity) {
            viewProperties =
                    inflater.inflate(R.layout.materials_dialog_of_folder_properties, null);
        } else {
            viewProperties = inflater.inflate(R.layout.materials_dialog_properties, null);
        }

        final Context context = viewProperties.getContext();
        ButterKnife.bind(this, viewProperties);
        DaggerAppComponent.builder()
                .appModule(new AppModule(
                        Objects.requireNonNull(getActivity())
                                .getApplication()))
                .roomModule(new RoomModule(getActivity().getApplication()))
                .build()
                .injectDialogFragmentForMaterials(this);
        materialPresenter.attachDialog(this);


        fileNameEditText.setText(materialPresenter.getName());
        CallbackPropertiesForMaterialsImpl
                .newCallbacksEditorImpl(this)
                .create();
        drawPropertiesDialog(viewProperties);
        hideEditor();
        validateOfNameDocument();
        inputMethodManager =
                (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        return viewProperties;
    }

    public void setMaterialPresenter(MaterialPresenter materialPresenter) {
        this.materialPresenter = materialPresenter;
    }

    public MaterialPresenter getMaterialPresenter() {
        return materialPresenter;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        materialPresenter.dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        super.onCancel(dialogInterface);
        materialPresenter.dismiss();
    }

    @Override
    public void drawPropertiesDialog(View viewProperties) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        viewProperties.setBackground(ContextCompat.getDrawable(
                viewProperties.getContext(), R.drawable.properties_dialog_bg));
    }

    @Override
    public void hideKeyBoard() {
        inputMethodManager.hideSoftInputFromWindow(
                Objects.requireNonNull(getView()).getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void showKeyBoard() {
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(fileNameEditText,
                    InputMethodManager.SHOW_FORCED);
        }
    }

    @RequiresApi(api=Build.VERSION_CODES.M)
    @Override
    public void removeDocument() {
        final RemovingFile removingFile = new RemovingFile(materialDao,
                materialFolderJoinDao, getActivity());
        removingFile.doStateWithFile(materialPresenter);
        showToast(materialPresenter.getStateOfFile());
    }

    @RequiresApi(api=Build.VERSION_CODES.M)
    @Override
    public void renameDocument() {
        final String newNameMaterial = fileNameEditText.getText().toString();
        final String fullName = getFullNameFile(newNameMaterial);
        fileNameEditText.setText(fullName);
        final RenamingFile renamingFile = new RenamingFile(fullName, materialDao);
        renamingFile.doStateWithFile(materialPresenter);
        showToast(materialPresenter.getStateOfFile());
    }

    @RequiresApi(api=Build.VERSION_CODES.M)
    @Override
    public void shareDocument() {
        final SharingFile sharingFile = new SharingFile();
        sharingFile.doStateWithFile(materialPresenter);
    }

    @Override
    public void addToFolder(String name) {
        final Intent selectorForFoldersIntent =
                new Intent(getContext(), SelectorFolderActivity.class);
        selectorForFoldersIntent.putExtra("material",
                materialPresenter.getMaterial());
        Objects.requireNonNull(getActivity())
                .startActivityForResult(selectorForFoldersIntent, 1);
    }

    @Override
    public void addNewFolder(String name) { }

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {

            }
        }
    }

    @Override
    public void hideEditor() {
        fileNameEditText.setVisibleCloseButton(false);
        fileNameEditText.setEnabled(false);
        applyMaterialName.setVisibility(View.INVISIBLE);
        cancelImageButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showEditor() {
        fileNameEditText.setVisibleCloseButton(true);
        fileNameEditText.setEnabled(true);
        applyMaterialName.setVisibility(View.VISIBLE);
        cancelImageButton.setVisibility(View.VISIBLE);
    }

    String getNameWithoutFormat(MaterialPresenter materialPresenter) {
        String nameMaterial = materialPresenter.getName();
        String format = materialPresenter.getFormat();
        final int lengthName = nameMaterial.length() - (format.length() + 1);
        return nameMaterial.substring(0, lengthName);
    }

    @RequiresApi(api=Build.VERSION_CODES.M)
    @Override
    public void showToast(StateOfDocument stateOfDocument) {
        if (stateOfDocument instanceof StateOfDocument.StateOfFile) {
            final Toast toast = Toast.makeText(getActivity(),
                    stateOfDocument.toString(),
                    LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.show();
        }
    }

    @Override
    public void validateOfNameDocument() {
        final Observer nameDocumentObserver = ObserversForNameDocument
                .getNameDocumentObserver(applyMaterialName, fileNameEditText);
        CustomEditText.getPublishSubject()
                .map((Function<String, Object>) s ->
                        materialDao.findByNameAndWithoutId(getFullNameFile(s),
                                materialPresenter.getId()).isEmpty()
                                && !s.isEmpty()).subscribe(nameDocumentObserver);
    }

    private String getFullNameFile(String name) {
        final String format = materialPresenter.getFormat();
        final String dote = ".";
        return String.valueOf(new StringBuilder(name)
                .append(dote)
                .append(format));
    }

}
