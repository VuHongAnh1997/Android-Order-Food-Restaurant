package com.example.finalapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.finalapp.Common.Common;
import com.example.finalapp.Database.Database;
import com.example.finalapp.Interface.ItemClickListener;
import com.example.finalapp.Model.Category;
import com.example.finalapp.Model.Food;
import com.example.finalapp.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    Query query;
    Query query2;
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    FirebaseRecyclerOptions<Food> options;
    FirebaseRecyclerOptions<Food> options2;
    String categoryId="";

    //Search Funtionality
    FirebaseRecyclerAdapter<Food,FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;

    //Favorites
    Database localDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        categoryId = getIntent().getStringExtra("CategoryId");

        //Local DB
        localDB = new Database(this);

        //Get Intent here
        if(getIntent()!=null){
            categoryId = getIntent().getStringExtra("CategoryId");
        }
        if(!categoryId.isEmpty()&&categoryId!=null){
            query = database.getReference("Foods").orderByChild("menuId").equalTo(categoryId);
            options = new FirebaseRecyclerOptions.Builder<Food>().setQuery(query,Food.class).build();
            if(Common.isConnectedToInternet(getBaseContext()))
                loadListFood();
            else{
                Toast.makeText(FoodList.this, "Please check your connection !!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        recyclerView = (RecyclerView)findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Search
        materialSearchBar = (MaterialSearchBar)findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter your food");
        loadSuggest();//Write function to load Suggest from Firebase
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> suggest = new ArrayList<String>();
                for (String search: suggestList){
                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                //When Search Bar is close
                //Restore original suggest adapter
                if(!enabled){
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                //When Search finish
                //Show result of search adapter
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });
    }

    private void startSearch(CharSequence text){
        query2 = database.getReference("Foods").orderByChild("name").equalTo(text.toString());
        options2 = new FirebaseRecyclerOptions.Builder<Food>().setQuery(query2,Food.class).build();
        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options2) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {
                holder.food_name.setText(model.getName());
                Picasso.get().load(model.getImage()).into(holder.food_image);
                final Food clickItem= model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail = new Intent(FoodList.this,FoodDetail.class);
                        foodDetail.putExtra("FoodId",searchAdapter.getRef(position).getKey());
                        startActivity(foodDetail);
                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.food_item,viewGroup,false);
                return new FoodViewHolder(view);
            }
        };
        recyclerView.setAdapter(searchAdapter);
        searchAdapter.startListening();
    }

    private void loadSuggest() {
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                    Food item = postSnapShot.getValue(Food.class);
                    suggestList.add(item.getName());//Add name of food to suggest list
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadListFood() {
        adapter = new FirebaseRecyclerAdapter<Food,FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder holder, final int position, @NonNull final Food model) {
                holder.food_name.setText(model.getName());
                Picasso.get().load(model.getImage()).into(holder.food_image);

                //Add Favorites
                if(localDB.isFavorite(adapter.getRef(position).getKey()))
                    holder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);

                //Click to change state of Favorites
                holder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!localDB.isFavorite(adapter.getRef(position).getKey())){
                            localDB.addToFavorites(adapter.getRef(position).getKey());
                            holder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(FoodList.this,model.getName()+" was added to Favorites",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey());
                            holder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(FoodList.this,model.getName()+" was removed from Favorites",Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                final Food clickItem= model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail = new Intent(FoodList.this,FoodDetail.class);
                        foodDetail.putExtra("FoodId",adapter.getRef(position).getKey());
                        startActivity(foodDetail);
                    }
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.food_item,viewGroup,false);
                return new FoodViewHolder(view);
            }
        };
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
