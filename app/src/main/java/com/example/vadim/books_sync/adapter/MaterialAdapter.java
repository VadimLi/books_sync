package com.example.vadim.books_sync.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vadim.books_sync.R;
import com.example.vadim.books_sync.model.Material;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MaterialAdapter extends RecyclerView.Adapter<MaterialAdapter.MyViewHolder> {

    private static final int START_POISTION_OF_MATERIALS  = 0;

    private List<Material> materials = new ArrayList<>();

    private final LayoutInflater layoutInflater;

    private final Context context;

    private View view;

    private MyViewHolder holder;

    public MaterialAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = layoutInflater.inflate(R.layout.material, parent, false);
        holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final Material material = materials.get(position);
        holder.nameMaterial.setText(material.getName());
        holder.formatMaterial.setText(material.getFormat());
    }

    public void setListContent(List<Material> materials){
        this.materials = materials;
        notifyItemRangeChanged(START_POISTION_OF_MATERIALS, materials.size());
    }

    public void removeAt(int position) {
        materials.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(START_POISTION_OF_MATERIALS, materials.size());
    }

    @Override
    public int getItemCount() {
        return materials.size();
    }

    protected class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView nameMaterial;

        private TextView formatMaterial;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            nameMaterial = itemView.findViewById(R.id.nameMaterial);
            formatMaterial = itemView.findViewById(R.id.formatMaterial);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            final Material material = materials.get(position);
            MaterialAdapter.this.openDocumentByPath(material.getPath());
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void openDocumentByPath(String path) {
        final File file = new File(path);
        if (file.exists()) {
            final Intent formatIntent = new Intent(Intent.ACTION_VIEW);
            formatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = FileProvider.getUriForFile(context,
                    context.getApplicationContext()
                            .getPackageName() +
                            ".provider", file);
            formatIntent.setData(uri);
            formatIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(formatIntent);
        } else {
            Log.e("File not exists : ", file.getAbsolutePath());
        }
    }

}
