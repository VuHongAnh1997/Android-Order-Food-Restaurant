package com.example.androideatitserver;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.androideatitserver.Common.Common;
import com.example.androideatitserver.Interface.ItemClickListener;
import com.example.androideatitserver.Model.Order;
import com.example.androideatitserver.Model.Request;
import com.example.androideatitserver.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrderStatus extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    FirebaseDatabase db;
    Query requests;
    FirebaseRecyclerOptions options;

    //Spinner
    Spinner spinner;
    String[] arr = {"Placed","On my way","Shipped"};
    List<String> list ;
    ArrayAdapter<String> adapter1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        list = Arrays.asList(arr);
        //Firebase
        db = FirebaseDatabase.getInstance();
        requests = db.getReference("Requests");
        options = new FirebaseRecyclerOptions.Builder<Request>().setQuery(requests,Request.class).build();

        //Init
        recyclerView = (RecyclerView)findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        loadOrders();//load all Orders
    }

    private void loadOrders() {

        adapter = new FirebaseRecyclerAdapter<Request,OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, int position, @NonNull final Request model) {
                holder.txtOrderId.setText(adapter.getRef(position).getKey());
                holder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                holder.txtOrderPhone.setText(model.getPhone());
                holder.txtOrderAddress.setText(model.getAddress());

                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //just implement it to fix Crash when click to this item
                        if(!isLongClick){
                            Intent trackingOrder = new Intent(OrderStatus.this, TrackingOrder.class);
                            Common.currentRequest = model;
                            startActivity(trackingOrder);
                        }
                        else {
                            Intent orderDetail = new Intent(OrderStatus.this, OrderDetail.class);
                            Common.currentRequest = model;
                            orderDetail.putExtra("OrderId",adapter.getRef(position).getKey());
                            startActivity(orderDetail);

                        }
                    }
                });
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_layout,viewGroup,false);
                return new OrderViewHolder(view);
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId()==0){
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else if(item.getItemId()==1){
            deleteOrder(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteOrder(String key) {
        db.getReference("Requests").child(key).removeValue();
    }

    private void showUpdateDialog(String key, final Request item) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please choose status");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.update_order_layout,null);
        spinner = (Spinner)view.findViewById(R.id.statusSpinner);
        adapter1 = new ArrayAdapter(this, android.R.layout.simple_spinner_item,list);
        spinner.setAdapter(adapter1);

        alertDialog.setView(view);
        final String localKey = key;
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
                item.setStatus(String.valueOf(spinner.getSelectedItemId()));

                db.getReference("Requests").child(localKey).setValue(item);
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }


}
