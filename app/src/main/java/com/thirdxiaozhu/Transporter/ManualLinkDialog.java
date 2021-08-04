package com.thirdxiaozhu.Transporter;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;

public class ManualLinkDialog extends DialogFragment {
    MainActivity mainActivity;

    public ManualLinkDialog(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,  Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_manual_link_dialog, container);
        Button button = (Button)view.findViewById(R.id.link);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ip = (EditText)view.findViewById(R.id.ipAddress);
                mainActivity.manualLink(ip.getText().toString());
                dismiss();
            }
        });

        ImageButton dismiss = (ImageButton)view.findViewById(R.id.dismiss);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Window window = getDialog().getWindow();
        if(window != null){
            window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));


            WindowManager.LayoutParams lp = window.getAttributes();
            lp.dimAmount = 0.3f; //背景灰度
            lp.gravity = Gravity.BOTTOM; //靠主窗口下方显示
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.horizontalMargin = 20;
            window.setAttributes(lp);
        }
    }

}
