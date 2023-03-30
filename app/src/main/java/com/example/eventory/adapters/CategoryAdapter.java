package com.example.eventory.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventory.ContainerActivity;
import com.example.eventory.EventPageActivity;
import com.example.eventory.R;
import com.example.eventory.ViewAllActivity;
import com.example.eventory.models.CardModel;
import com.example.eventory.models.CategoryModel;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    private List<CategoryModel> categoryModelList;
    private Context context;

    public CategoryAdapter(List<CategoryModel> categoryModelList) {
        this.categoryModelList = categoryModelList;
    }

    public List<CategoryModel> getLayoutModelList() {
        return categoryModelList;
    }

    public void setLayoutModelList(List<CategoryModel> categoryModelList) {
        this.categoryModelList = categoryModelList;
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        context = parent.getContext();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {


        CategoryModel categoryModel = categoryModelList.get(position);
        holder.category.setText(categoryModel.getCategory());
        if(categoryModel.getCategory().equals("Tomsarkgh"))
            holder.category.setText("All events");


        LinearLayoutManager layoutManager = new LinearLayoutManager(
                holder.recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false);

        layoutManager.setInitialPrefetchItemCount(categoryModel.getCardModelList().size());

        CardAdapter cardAdapter = new CardAdapter(context, categoryModel.getCardModelList());

        holder.recyclerView.setLayoutManager(layoutManager);
        holder.recyclerView.setAdapter(cardAdapter);
        holder.recyclerView.setRecycledViewPool(viewPool);
        holder.recyclerView.setHasFixedSize(true);
        cardAdapter.notifyDataSetChanged();

        holder.view_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ViewAllActivity.class);
                i.putExtra("category", categoryModel.getCategory());
                context.startActivity(i);
            }
        });


        //FIXME see if it works Tomsarkgh is from here
        Gson gson = new Gson();
        String json = gson.toJson(categoryModel.getCardModelList());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(categoryModel.getCategory(), json);
        editor.apply();

    }

    @Override
    public int getItemCount() {
        return categoryModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView category;
        private RecyclerView recyclerView;
        private TextView view_all;

        public ViewHolder(@NonNull View view) {
            super(view);

            category = view.findViewById(R.id.category_name);
            recyclerView = view.findViewById(R.id.recycle_view);
            view_all = view.findViewById(R.id.category_view_all);

        }
    }
}
