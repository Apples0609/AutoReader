package cn.smiles.autoreader.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;


public class MyLinearLayout extends LinearLayout {

    private KeyDownViewListener hideView;

    public MyLinearLayout(Context context) {
        super(context);
    }

    public MyLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (hideView != null) {
                hideView.keyEvent();
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void setKeyEventListener(KeyDownViewListener hideView) {
        this.hideView = hideView;
    }

    public interface KeyDownViewListener {
        void keyEvent();
    }
}
