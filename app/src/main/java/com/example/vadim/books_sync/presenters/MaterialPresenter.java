package com.example.vadim.books_sync.presenters;


import android.os.Build;
import android.support.annotation.RequiresApi;

import com.example.vadim.books_sync.model.Material;

public class MaterialPresenter implements StateOwnerProperties {

    private MaterialListPresenter materialListPresenter;

    private Material material;

    private int materialPosition;

    private FolderPresenter folderPresenter;

    private StateOwnerProperties stateOwnerProperties;

    private StateOfDocument.StateOfFile stateOfFile;

    public FolderPresenter getFolderPresenter() {
        return folderPresenter;
    }

    public void setFolderPresenter(FolderPresenter folderPresenter) {
        this.folderPresenter = folderPresenter;
    }

    public void setStateOfFile(final StateOfDocument.StateOfFile stateOfFile) {
        this.stateOfFile = stateOfFile;
    }

    public StateOfDocument.StateOfFile getStateOfFile() {
        return stateOfFile;
    }

    public void attachDialog(StateOwnerProperties stateOwnerProperties) {
        this.stateOwnerProperties = stateOwnerProperties;
    }

    public StateOwnerProperties getPropertiesDialog() {
        return stateOwnerProperties;
    }

    public MaterialListPresenter getMaterialListPresenter() {
        return materialListPresenter;
    }

    public void setMaterialListPresenter(MaterialListPresenter materialListPresenter) {
        this.materialListPresenter = materialListPresenter;
    }

    public int getMaterialPosition() {
        return materialPosition;
    }

    public void setMaterialPosition(int materialPosition) {
        this.materialPosition = materialPosition;
    }

    public void update(String newName) {
        materialListPresenter.updateAt(this, newName);
    }

    public long getId() {
        return material.getId();
    }

    public String getFormat() {
        return material.getFormat();
    }

    public String getName() {
        return material.getName();
    }

    public String getPath() {
        return material.getPath();
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void removeDocument() {
        materialListPresenter.removeAt(this);
        stateOwnerProperties.removeDocument();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void renameDocument() {
        stateOwnerProperties.renameDocument();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void shareDocument() {
        materialListPresenter.setListContent(materialListPresenter.getMaterials());
        stateOwnerProperties.shareDocument();
    }

    @Override
    public void addToFolder(final String materialName) {
        stateOwnerProperties.addToFolder(materialName);
    }

    @Override
    public void addNewFolder(String name) { }

    @Override
    public void dismiss() {
        stateOwnerProperties = null;
    }

}
