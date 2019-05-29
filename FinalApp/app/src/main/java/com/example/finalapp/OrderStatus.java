package com.example.finalapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.finalapp.Common.Common;
import com.example.finalapp.Model.Food;
import com.example.finalapp.Model.Request;
import com.example.finalapp.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class OrderStatus extends AppCompatActivity {

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;
    FirebaseRecyclerOptions<Request> options;
    Query query;

    FirebaseDatabase database;
    DatabaseReference requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //Firebase
        database = FirebaseDatabase.getInstance();
        if(getIntent()==null){
            query = database.getReference("Requests").orderByChild("phone").equalTo(Common.currentUser.getPhone());
        }else {
            query = database.getReference("Requests").orderByChild("phone").equalTo(getIntent().getStringExtra("userPhone"));
        }

        options = new FirebaseRecyclerOptions.Builder<Request>().setQuery(query,Request.class).build();
        loadOrders();
        recyclerView = (RecyclerView)findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void loadOrders() {
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, int position, @NonNull Request model) {
                holder.txtOrderId.setText(adapter.getRef(position).getKey());
                holder.txtOrderStatus.setText(convertCodeToStatus(model.getStatus()));
                holder.txtOrderPhone.setText(model.getPhone());
                holder.txtOrderAddress.setText(model.getAddress());
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_layout,viewGroup,false);
                return new OrderViewHolder(view);
            }
        };
    }

    private String convertCodeToStatus(String status) {
        if(status.equals("0")){
            return "Placed";
        }
        else if(status.equals("1")){
            return "On my way";
        }
        else {
            return "Shipped";
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
