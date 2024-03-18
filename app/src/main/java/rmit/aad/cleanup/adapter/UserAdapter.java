//package rmit.aad.cleanup.adapter;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.util.List;
//
//import rmit.aad.cleanup.R;
//import rmit.aad.cleanup.model.User;
//
//public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
//    private Context context;
//    private int resource;
//    private List<User> list;
//
//    public UserAdapter(Context context, int resource, List<User> list) {
//        this.context = context;
//        this.resource = resource;
//        this.list = list;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
//        return new ViewHolder(view);
//    }
//
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        User user = list.get(position);
//        holder.name.setText(user.getName());
//        holder.gender.setText(user.getGender().toString());
//    }
//
//    @Override
//    public int getItemCount() {
//        return list.size();
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        private final TextView name, age, gender;
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            name = itemView.findViewById(R.id.uName);
//            age = itemView.findViewById(R.id.uAge);
//            gender = itemView.findViewById(R.id.uGender);
//        }
//    }
//}
