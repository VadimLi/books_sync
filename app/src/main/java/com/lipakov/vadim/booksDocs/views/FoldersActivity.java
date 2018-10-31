package com.lipakov.vadim.booksDocs.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.lipakov.vadim.booksDocs.R;
import com.lipakov.vadim.booksDocs.adapter.FoldersRecyclerAdapter;
import com.lipakov.vadim.booksDocs.dagger.AppModule;
import com.lipakov.vadim.booksDocs.dagger.DaggerAppComponent;
import com.lipakov.vadim.booksDocs.dagger.RoomModule;
import com.lipakov.vadim.booksDocs.dao.FolderDao;
import com.lipakov.vadim.booksDocs.dao.MaterialDao;
import com.lipakov.vadim.booksDocs.model.Folder;
import com.lipakov.vadim.booksDocs.presenters.FolderPresenter;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FoldersActivity extends AppCompatActivity
        implements ActivityView, ActivityView.ActivityViewForFolders {

    @BindView(R.id.searchFolders)
    SearchView searchFolders;

    @BindView(R.id.folder_list)
    RecyclerView recyclerView;

    @Inject
    FolderDao folderDao;

    @Inject
    MaterialDao materialDao;

    private FoldersRecyclerAdapter foldersRecyclerAdapter;

    private View actionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folders);
        ButterKnife.bind(this);

        DaggerAppComponent.builder()
                .appModule(new AppModule(getApplication()))
                .roomModule(new RoomModule(getApplication()))
                .build()
                .injectSelectorFolderActivity(this);
        createAdapter();
        final List<Folder> folders = folderDao.findAll();
        foldersRecyclerAdapter.setListContent(folders);
        recyclerView.setAdapter(foldersRecyclerAdapter);

        actionView = getCustomActionBar();
        final ImageButton imageButtonFiles = actionView.findViewById(R.id.btnFiles);
        imageButtonFiles.setOnClickListener(v -> {
            final Intent booksIntent = new Intent(this, MainActivity.class);
            setResult(RESULT_OK, booksIntent);
            finish();
        });

        final TextView folderName = actionView.findViewById(R.id.titleFolder);
        folderName.setVisibility(View.GONE);

        addClickListenerForCreatorFolder();
        addQueryTextListener();
    }

    @Override
    public void createAdapter() {
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this));
        foldersRecyclerAdapter = new FoldersRecyclerAdapter(this);
        recyclerView.setAdapter(foldersRecyclerAdapter);
    }

    @Override
    public View getCustomActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.folder_or_material_action_bar_layout);
        return actionBar.getCustomView();
    }

    @Override
    public void addClickListenerForCreatorFolder() {
        final ImageButton imageButtonCreatorFolder =
                actionView.findViewById(R.id.btnCreatorFolder);
        imageButtonCreatorFolder.setOnClickListener(v -> {
            final AddingFolderDialog addingFolderDialog =
                    new AddingFolderDialog();
            final FolderPresenter folderPresenter = new FolderPresenter();
            addingFolderDialog.setFolderPresenter(folderPresenter);
            folderPresenter.setFolderListPresenter(foldersRecyclerAdapter.getFolderListPresenter());
            final FragmentManager fragmentManager = getSupportFragmentManager();
            addingFolderDialog.show(fragmentManager, "dialog for adding folder");
        });
    }

    @Override
    public void addQueryTextListener() {
        searchFolders.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                foldersRecyclerAdapter.getFilter().filter(newText);
                return false;
            }

        });
    }

}