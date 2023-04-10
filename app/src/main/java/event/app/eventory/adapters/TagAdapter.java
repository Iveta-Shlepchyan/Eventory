package event.app.eventory.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import event.app.eventory.Logic.Convertor;
import event.app.eventory.R;
import event.app.eventory.ViewAllActivity;

import java.util.ArrayList;
import java.util.Collection;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> tags = new ArrayList<String>();
    public ArrayList<String> selected_tags = new ArrayList<String>();
    private boolean selectable = true;
    private boolean isMapFragment = false;
    private int item = R.layout.item_tag;

    public TagAdapter(Context context, Collection<String> tags, boolean selectable){
        this.context = context;
        this.tags.addAll(tags);
        this.selectable = selectable;
    }


    public void select_all_map_tags(){
        selected_tags.clear();
        selected_tags.addAll(tags);
        isMapFragment = true;
        item = R.layout.item_tag_map;
    }

    public boolean isMapFragment(){
        return isMapFragment;
    }

    public ArrayList<String> getTags() {
        return tags;
    }


    public interface onItemClickListener{
        void onClick(int position, String tag, boolean selected);
    }

    TagAdapter.onItemClickListener onItemClickListner;

    public void setOnItemClickListener(TagAdapter.onItemClickListener
                                               onItemClickListner)
    {
        this.onItemClickListner = onItemClickListner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TagAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {


        if(isMapFragment){
            holder.tag.getBackground().setAlpha(240);
            if(selected_tags.contains(tags.get(position))){
                /*holder.tag.getBackground().setTint(context.getResources().getColor(Convertor.categories.get(tags.get(position))));
                holder.tag.setTextColor(context.getResources().getColor(R.color.white));*/
                holder.tag.getBackground().setTint(context.getResources().getColor(R.color.white));
                holder.tag.setTextColor(context.getResources().getColor(Convertor.categories.get(tags.get(position))));
            }
            else{
                holder.tag.getBackground().setTint(context.getResources().getColor(R.color.white));
                holder.tag.setTextColor(context.getResources().getColor(R.color.searchHint));
            }
        }


        holder.tag.setText("#"+tags.get(position));
        if(selectable) holder.tag.setSelected(selected_tags.contains(tags.get(position)));
        holder.tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if from EventPageActivity go to ViewAllActivity and select tag
                if(!selectable){
                    Intent i = new Intent(context, ViewAllActivity.class);
                    i.putExtra("category", tags.get(position));
                    context.startActivity(i);
                }
                else {
                    Log.e("TagAdapter", "before "+selected_tags);
                    if(selected_tags.contains(tags.get(holder.getPosition()))){
                        selected_tags.remove(tags.get(holder.getPosition()));
                        notifyItemChanged(holder.getPosition());

                        onItemClickListner.onClick(holder.getPosition(), tags.get(holder.getPosition()), false);
                    }
                    else{
                        selected_tags.add(tags.get(holder.getPosition()));
                        notifyItemChanged(holder.getPosition());

                        onItemClickListner.onClick(holder.getPosition(), tags.get(holder.getPosition()), true);
                        }
                    Log.e("TagAdapter", "after "+selected_tags);


                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tag = itemView.findViewById(R.id.tag);
        }
    }


}
