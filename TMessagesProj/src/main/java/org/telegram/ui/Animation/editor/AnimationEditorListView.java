package org.telegram.ui.Animation.editor;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

public class AnimationEditorListView extends RecyclerListView {

    private final AnimationEditorAdapter adapter = new AnimationEditorAdapter();

    private final int actionY = AndroidUtilities.dp(30);
    private float downY;

    private IChatAnimationEditor animationEditor;

    public AnimationEditorListView(Context context) {
        super(context);
        setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        setLayoutManager(layoutManager);
        setBackgroundColor(Theme.getColor(Theme.key_chat_outBubble));
    }

    @Override
    public void addView(View view) {
        adapter.views.add(view);
    }

    @Override
    public void removeView(View view) {
        adapter.views.remove(view);
    }

    @Override
    public void removeAllViews() {
        adapter.views.clear();
    }

    public View getItemAt(int index) {
        return adapter.views.get(index);
    }

    public int getItemCount() {
        return adapter.views.size();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = e.getY();
                View view = getChildAt(e.getX(), e.getY());
                if (view instanceof IChatAnimationEditor) {
                    float x = e.getX();
                    float y = e.getY();
                    e.setLocation(x - view.getLeft(), y - view.getTop());
                    view.onTouchEvent(e);
                    e.setLocation(x, y);
                    if (((IChatAnimationEditor) view).isEditing())
                        animationEditor = (IChatAnimationEditor) view;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (animationEditor != null && Math.abs(e.getY() - downY) < actionY)
                    return false;
                break;
            case MotionEvent.ACTION_UP:
                animationEditor = null;
                break;
        }
        return super.onInterceptTouchEvent(e);
    }

    private View getChildAt(float x, float y) {
        for (int i = 0; i < getItemCount(); i++) {
            View view = getItemAt(i);
            if (view.getTop() <= y && view.getBottom() >= y)
                return view;
        }
        return null;
    }

    private static class AnimationEditorAdapter extends SelectionAdapter {

        private final ArrayList<View> views = new ArrayList<>();

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(views.get(viewType));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return views.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public boolean isEnabled(ViewHolder holder) {
            return true;
        }
    }
}
