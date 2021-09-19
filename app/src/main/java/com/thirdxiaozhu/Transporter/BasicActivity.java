package com.thirdxiaozhu.Transporter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class BasicActivity extends FragmentActivity implements View.OnClickListener {

    public static ConnectionClient connectionClient;
    public MainActivity mainActivity;
    public SettingActivity settingActivity;
    public static Vector<String> send_files;
    public static Vector<String> received_files;
    public static MyListAdapter receiveListAdapter;
    public static MyListAdapter sendListAdapter;

    //声明ViewPager
    private ViewPager viewPager;
    private FragmentPagerAdapter mAdapter;
    private List<Fragment> mFragments;

    //四个Tab对应的布局
    private LinearLayout mTab1;
    private LinearLayout mTab2;
    private LinearLayout mTab3;

    //四个Tab对应的ImageButton
    private ImageButton mImg1;
    private ImageButton mImg2;
    private ImageButton mImg3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_basic);
        received_files = new Vector<>();
        receiveListAdapter = new MyListAdapter(received_files);
        send_files = new Vector<>();
        sendListAdapter = new MyListAdapter(send_files);

        initViews();//初始化控件
        initEvents();//初始化事件
        initDatas();//初始化数据
    }

    private void initDatas() {
        mainActivity = new MainActivity();
        settingActivity = new SettingActivity();

        //将四个Fragment加入集合中
        mFragments = new ArrayList<>();
        mFragments.add(mainActivity);
        mFragments.add(settingActivity);

        //初始化适配器
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {//从集合中获取对应位置的Fragment
                return mFragments.get(position);
            }

            @Override
            public int getCount() {//获取集合中Fragment的总数
                return mFragments.size();
            }
        };
        //不要忘记设置ViewPager的适配器
        viewPager.setAdapter(mAdapter);
        //设置ViewPager的切换监听
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            //页面滚动事件
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            //页面选中事件
            @Override
            public void onPageSelected(int position) {
                //设置position对应的集合中的Fragment
                viewPager.setCurrentItem(position);
                resetImgs();
                selectTab(position);
            }

            @Override
            //页面滚动状态改变事件
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initEvents() {
        //设置四个Tab的点击事件
        mTab1.setOnClickListener(this);
        mTab2.setOnClickListener(this);
        mTab3.setOnClickListener(this);
    }

    //初始化控件
    private void initViews() {
        viewPager = (ViewPager) findViewById(R.id.id_viewpager);

        mTab1 = (LinearLayout) findViewById(R.id.id_tab1);
        mTab2 = (LinearLayout) findViewById(R.id.id_tab2);
        mTab3 = (LinearLayout) findViewById(R.id.id_tab3);

        mImg1 = (ImageButton) findViewById(R.id.id_tab_img1);
        mImg2 = (ImageButton) findViewById(R.id.id_tab_img2);
        mImg3 = (ImageButton) findViewById(R.id.id_tab_img3);
    }

    @Override
    public void onClick(View v) {
        resetImgs();
        switch (v.getId()){
            case R.id.id_tab1:
                selectTab(0);
                break;
            case R.id.id_tab2:
                //selectTab(1);
                onScan();
                break;
            case R.id.id_tab3:
                selectTab(1);
                break;
            default:
                break;
        }

    }

    private void selectTab(int i){
        switch (i){
            case 0:
                mImg1.setImageResource(R.drawable.ic_iconfont_20);
                break;
            case 1:
                mImg3.setImageResource(R.drawable.ic_iconfont_icon_xitong);
                break;
            default:
                break;
        }

        viewPager.setCurrentItem(i);
    }

    private void resetImgs() {
        mImg1.setImageResource(R.drawable.ic_iconfont_20);
        mImg2.setImageResource(R.drawable.ic_iconfont_saomaio);
        mImg3.setImageResource(R.drawable.ic_iconfont_icon_xitong);
    }


    public void onScan(){
        // 创建IntentIntegrator对象
        IntentIntegrator intentIntegrator = new IntentIntegrator(BasicActivity.this);
        // 开始扫描
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.initiateScan();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 获取解析结果
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "取消扫描", Toast.LENGTH_LONG).show();
            } else {
                try {
                    connectPC(result.getContents());
                }catch (Exception e){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("警告");
                    builder.setMessage("无效二维码，请重新扫描");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.out.println("点了确定");
                        }
                    });
                    builder.show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void connectPC(String IP){
        connectionClient = new ConnectionClient(BasicActivity.this, IP);
    }

}