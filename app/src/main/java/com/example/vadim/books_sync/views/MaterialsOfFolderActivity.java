package com.example.vadim.books_sync.views;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.example.vadim.books_sync.R;
import com.example.vadim.books_sync.adapter.MaterialsRecyclerAdapter;
import com.example.vadim.books_sync.dagger.AppModule;
import com.example.vadim.books_sync.dagger.DaggerAppComponent;
import com.example.vadim.books_sync.dagger.RoomModule;
import com.example.vadim.books_sync.dao.FolderDao;
import com.example.vadim.books_sync.dao.MaterialDao;
import com.example.vadim.books_sync.dao.MaterialFolderJoinDao;
import com.example.vadim.books_sync.model.Folder;
import com.example.vadim.books_sync.model.Material;
import com.example.vadim.books_sync.presenters.FolderPresenter;
import com.example.vadim.books_sync.presenters.MaterialsUpdaterPresenter;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MaterialsOfFolderActivity extends AppCompatActivity implements ActivityView {

    @BindView(R.id.material_list_of_folder)
    RecyclerView recyclerView;

    @BindView(R.id.searchFiles)
    SearchView searchMaterials;

    @Inject
    MaterialDao materialDao;

    @Inject
    FolderDao folderDao;

    @Inject
    MaterialFolderJoinDao materialFolderJoinDao;

    @Inject
    MaterialsUpdaterPresenter materialsUpdaterPresenter;

    private MaterialsRecyclerAdapter materialsRecyclerAdapter;

    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_materials_of_folder);

        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        ButterKnife.bind(this);
        DaggerAppComponent.builder()
                .appModule(new AppModule(getApplication()))
                .roomModule(new RoomModule(getApplication()))
                .build()
                .injectMaterialsOfFolderActivity(this);
        createAdapter();
        final Bundle folderBundle = getIntent().getExtras();
        if (folderBundle != null) {
            final Folder folder = folderBundle.getParcelable("folder");
            final FolderPresenter folderPresenter = new FolderPresenter();
            folderPresenter.setFolder(folder);
            final List<Material> materials = materialFolderJoinDao
                    .findMaterialsForFolders(folder.getId());

            materialsRecyclerAdapter.setFolderPresenter(folderPresenter);
            materialsRecyclerAdapter.setListContent(materials);
            recyclerView.setAdapter(materialsRecyclerAdapter);
        }
        final View actionView = getCustomActionBar();
        final ImageButton moveToFolders = actionView.findViewById(R.id.btnFiles);
        final ImageButton creatorNewFolder = actionView.findViewById(R.id.btnCreatorFolder);
        creatorNewFolder.setVisibility(View.GONE);
        addQueryTextListener();
        moveToFolders.setOnClickListener(v -> {
            final Intent booksIntent = new Intent(this, FoldersActivity.class);
            setResult(RESULT_OK, booksIntent);
            finish();
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("state ", "stop activity");
        materialsUpdaterPresenter.detachView();
    }

    @Override
    public void addQueryTextListener() {
        searchMaterials.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                materialsRecyclerAdapter.getFilter().filter(newText);
                return false;
            }

        });
    }

    @Override
    public void createAdapter() {
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));
        materialsRecyclerAdapter = new MaterialsRecyclerAdapter(this);
        recyclerView.setAdapter(materialsRecyclerAdapter);
    }

    @Override
    public View getCustomActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.folder_or_material_action_bar_layout);
        return actionBar.getCustomView();
    }

}