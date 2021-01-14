package com.example.dlpbgj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public class CustomNotificationAdapter extends ArrayAdapter{

    private final ArrayList<String> notifications;
    private final Context context;

    public CustomNotificationAdapter(Context context, ArrayList<String> notifications) {
        super(context, 0, notifications);
        this.notifications = notifications;
        this.context = context;
    }

    /**
     * Function to use our custom array adapter to show the books of a user.
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.notification_content, parent, false); //Attaches layout from bookcontent to each item inside the ListView
        }
        String notification = notifications.get(position);

        TextView notif = view.findViewById(R.id.textView1);
        notif.setText(notification);



        return view;
    }
}
