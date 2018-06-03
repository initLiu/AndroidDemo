package com.lzp.recycerviewdemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lzp.recycerviewdemo.item.Item;
import com.lzp.recycerviewdemo.item.ItemBanner;
import com.lzp.recycerviewdemo.item.ItemContent;
import com.lzp.recycerviewdemo.item.ItemDivider;
import com.lzp.recycerviewdemo.item.ItemHeader;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView mRecyclerView;
    MyAdapter mAdapter;
    LinearLayout mRoot;
    ImageView mImageBg;
    View mCover;
    LinearLayoutManager mManager;
    private int mBannerHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler);
        mAdapter = new MyAdapter();

        mManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mManager);
//        mRecyclerView.addItemDecoration(new RecyclerViewDivider(this));
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setData(initData());

        mImageBg = (ImageView) findViewById(R.id.main_bg);
        mCover = findViewById(R.id.main_cover);
        mCover.setBackgroundColor(Color.BLACK);
        mCover.setAlpha(0);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int pos = mManager.findFirstVisibleItemPosition();
                if (pos == 1) {
                    View banner = mManager.getChildAt(0);
                    mBannerHeight = banner.getHeight();
                    int offseted = Math.abs((int) (banner.getTop() * 0.5));
                    mImageBg.scrollTo(0, offseted);

                    float alpha = (offseted * 1.0f) / mImageBg.getHeight();
                    mCover.setAlpha(alpha);
                } else if (pos > 1) {
                    if (mImageBg.getScrollY() != mBannerHeight / 2) {
                        mImageBg.scrollBy(0, mBannerHeight / 2 - mImageBg.getScrollY());
                    }
                } else {
                    if (mImageBg.getScrollY() != 0) {
                        mImageBg.scrollTo(0, 0);
                    }
                }
            }
        });

        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            int downY, downX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = (int) event.getX();
                        downY = (int) event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int curX = (int) event.getX();
                        int curY = (int) event.getY();
                        int deltaX = curX - downX;
                        int deltaY = curY - downY;


                        downX = curX;
                        downY = curY;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return false;
            }
        });
    }

    private List<Item> initData() {
        List<Item> items = new ArrayList<>();
        items.add(new ItemHeader());
        items.add(new ItemBanner());

        for (int i = 0; i < 500; i++) {
            items.add(new ItemContent("item " + i));
            items.add(new ItemDivider());
        }
        items.remove(items.size() - 1);
        return items;
    }
}
