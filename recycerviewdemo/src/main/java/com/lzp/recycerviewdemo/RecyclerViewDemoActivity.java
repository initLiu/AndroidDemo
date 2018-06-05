package com.lzp.recycerviewdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.lzp.recycerviewdemo.item.Item;
import com.lzp.recycerviewdemo.item.ItemBanner;
import com.lzp.recycerviewdemo.item.ItemContent;
import com.lzp.recycerviewdemo.item.ItemDivider;
import com.lzp.recycerviewdemo.refreshable.RefreshHeaderLayout;
import com.lzp.recycerviewdemo.refreshable.RefreshableRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewDemoActivity extends AppCompatActivity {
    RefreshableRecyclerView mRecyclerView;
    MyAdapter mAdapter;
    LinearLayoutManager mManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclerdemo);

        mRecyclerView = (RefreshableRecyclerView) findViewById(R.id.recyclerdemo_recycler);
        mAdapter = new MyAdapter();

        mManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mManager);
        mRecyclerView.setAdapter(mAdapter);
        View view = LayoutInflater.from(RecyclerViewDemoActivity.this).inflate(R.layout.item_header, null);
        mRecyclerView.setRefreshHeader(view);
        mRecyclerView.enableRefreshHeader(true);
        mAdapter.setData(initData());

        findViewById(R.id.recyclerdemo_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.stopRefresh();
            }
        });
    }

    private List<Item> initData() {
        List<Item> items = new ArrayList<>();
        items.add(new ItemBanner());

        for (int i = 0; i < 500; i++) {
            items.add(new ItemContent("item " + i));
            items.add(new ItemDivider());
        }
        items.remove(items.size() - 1);
        return items;
    }
}
