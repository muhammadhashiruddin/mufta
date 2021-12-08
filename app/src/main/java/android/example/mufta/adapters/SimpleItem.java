package android.example.mufta.adapters;

import android.example.mufta.R;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class SimpleItem extends DrawerItem<SimpleItem.ViewHolder> {

    private int selectedIconTint;
    private int unSelectedIconTint;

    private int selectedTitleTint;
    private int unSelectedTitleTint;

    private Drawable icon;
    private String title;

    public SimpleItem(Drawable icon, String title){
        this.icon = icon;
        this.title = title;
    }

    @Override
    public ViewHolder createViewHolder(ViewGroup parent) {
        LayoutInflater inflater =  LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_of_recycler,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void bindViewHolder(ViewHolder holder) {
        holder.title.setText(title);
        holder.icon.setImageDrawable(icon);

        holder.title.setTextColor(isChecked ? selectedTitleTint : unSelectedTitleTint);
        holder.icon.setColorFilter(isChecked ? selectedIconTint : unSelectedIconTint);
        holder.title.setTypeface(null, isChecked ? Typeface.BOLD : Typeface.NORMAL);
    }

    public SimpleItem withSelectedIconTint(int selectedIconTint){
        this.selectedIconTint = selectedIconTint;
        return this;
    }

    public SimpleItem withSelectedTitleTint(int selectedTitleTint){
        this.selectedTitleTint = selectedTitleTint;
        return this;
    }

    public SimpleItem withIconTint(int unSelectedIconTint){
        this.unSelectedIconTint = unSelectedIconTint;
        return this;
    }

    public SimpleItem withTitleTint(int unSelectedTitleTint){
        this.unSelectedTitleTint = unSelectedTitleTint;
        return this;
    }

    static class ViewHolder extends DrawerAdapter.ViewHolder{
        private ImageView icon;
        private TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.item_recycler_image);
            title = itemView.findViewById(R.id.item_recycler_image_txt);
        }
    }
}
