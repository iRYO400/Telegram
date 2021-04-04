package org.telegram.ui.Animation.editor;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Animation.AnimationParams;
import org.telegram.ui.Animation.AnimationType;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SizeNotifierFrameLayout;

import java.util.ArrayList;
import java.util.List;

import static org.telegram.messenger.AndroidUtilities.dp;

public class ChatAnimationActivity extends BaseFragment {

    public final static String KEY_ANIMATIONS_CHANGED = "AnimationSettingsChanged";

    private SizeNotifierFrameLayout rootView;
    private HorizontalScrollView horizontalScrollView;
    private LinearLayout contentView;
    private NonSwipeableViewPager viewPager;

    private Context context;
    private ActionBarMenuItem otherItem;

    private final static int share_parameters = 1;
    private final static int import_parameters = 2;
    private final static int restore_to_default = 3;
    private final static int view_pager = 4;

    private ArrayList<AnimationType> availableSettings = new ArrayList<>();
    private VooDooPagerAdapter adapter;

    @Override
    public View createView(Context context) {
        this.context = context;
        fragmentView = new SizeNotifierFrameLayout(context, parentLayout);
        rootView = (SizeNotifierFrameLayout) fragmentView;

        initActionBar(context);

        contentView = new LinearLayout(context);
        contentView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        contentView.setOrientation(LinearLayout.VERTICAL);
        rootView.addView(contentView);

        //TODO fill here before loading
        availableSettings.add(AnimationType.SMALL_MESSAGE);
        availableSettings.add(AnimationType.BIG_MESSAGE);
        availableSettings.add(AnimationType.SINGLE_EMOJI);
        List<AnimationSettingsParams> animationSettingsParams = loadData(availableSettings);

        horizontalScrollView = new HorizontalScrollView(context);
        horizontalScrollView.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        horizontalScrollView.setHorizontalScrollBarEnabled(false);
        fillTabs(animationSettingsParams);
        contentView.addView(horizontalScrollView);

        viewPager = new NonSwipeableViewPager(context);
        viewPager.setId(view_pager);
        viewPager.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Log.d("Bootya", "PagePosition " + position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        adapter = new VooDooPagerAdapter(animationSettingsParams);
        viewPager.setAdapter(adapter);

        contentView.addView(viewPager);
        return fragmentView;
    }

    private void initActionBar(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("Animation Settings");

        ActionBarMenu barMenu = actionBar.createMenu();
        otherItem = barMenu.addItem(0, R.drawable.ic_ab_other);
        otherItem.setContentDescription(LocaleController.getString("AccDescrMoreOptions", R.string.AccDescrMoreOptions));

        otherItem.addSubItem(share_parameters, "Share Parameters");
        otherItem.addSubItem(import_parameters, "Import Parameters");
        otherItem.addSubItem(restore_to_default, "Restore to Default");

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (getParentActivity() == null)
                    return;
                if (id == share_parameters) {
                    Toast.makeText(context, "Share Parameters", Toast.LENGTH_SHORT).show();
                } else if (id == import_parameters) {
                    Toast.makeText(context, "Import Parameters", Toast.LENGTH_SHORT).show();
                } else if (id == restore_to_default) {
                    Toast.makeText(context, "Restore to Default", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fillTabs(List<AnimationSettingsParams> animationSettingsParamsList) {
        LinearLayout tabs = new LinearLayout(context);
        tabs.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < animationSettingsParamsList.size(); i++) {
            AnimationSettingsParams animationSettingsParams = animationSettingsParamsList.get(i);
            TextView title = new TextView(context);
            title.setPadding(dp(16), dp(8), dp(16), dp(8));
            title.setTextColor(Theme.getColor(Theme.key_actionBarTabUnactiveText));
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            title.setText(animationSettingsParams.getAnimationType().getName());
            title.setOnClickListener(v -> {
                if (viewPager == null)
                    return;
                if (tabs.getChildCount() <= 0)
                    return;

                switch (animationSettingsParams.getAnimationType()) {
                    case SMALL_MESSAGE:
                        viewPager.setCurrentItem(0);
                        TextView textView = (TextView) tabs.getChildAt(0);
                        textView.setTextColor(Theme.getColor(Theme.key_actionBarTabActiveText));
                        textView = (TextView) tabs.getChildAt(1);
                        textView.setTextColor(Theme.getColor(Theme.key_actionBarTabUnactiveText));
                        textView = (TextView) tabs.getChildAt(2);
                        textView.setTextColor(Theme.getColor(Theme.key_actionBarTabUnactiveText));
                        horizontalScrollView.smoothScrollBy(Integer.MIN_VALUE, 0);
                        break;
                    case BIG_MESSAGE:
                        viewPager.setCurrentItem(1);
                        textView = (TextView) tabs.getChildAt(0);
                        textView.setTextColor(Theme.getColor(Theme.key_actionBarTabUnactiveText));
                        textView = (TextView) tabs.getChildAt(1);
                        textView.setTextColor(Theme.getColor(Theme.key_actionBarTabActiveText));
                        textView = (TextView) tabs.getChildAt(2);
                        textView.setTextColor(Theme.getColor(Theme.key_actionBarTabUnactiveText));
                        break;
                    case SINGLE_EMOJI:
                        viewPager.setCurrentItem(2);
                        textView = (TextView) tabs.getChildAt(0);
                        textView.setTextColor(Theme.getColor(Theme.key_actionBarTabUnactiveText));
                        textView = (TextView) tabs.getChildAt(1);
                        textView.setTextColor(Theme.getColor(Theme.key_actionBarTabUnactiveText));
                        textView = (TextView) tabs.getChildAt(2);
                        textView.setTextColor(Theme.getColor(Theme.key_actionBarTabActiveText));
                        horizontalScrollView.smoothScrollBy(Integer.MAX_VALUE, 0);
                        break;
                }
            });
            tabs.addView(title);
        }

        horizontalScrollView.addView(tabs);
    }

    private ArrayList<AnimationSettingsParams> loadData(ArrayList<AnimationType> availableSettings) {
        ArrayList<AnimationSettingsParams> loadedParameters = new ArrayList<>();
        SharedPreferences preferences = MessagesController.getGlobalMainSettings();

        for (AnimationType animationType : availableSettings) {
            ArrayList<AnimationParams> animationParamsList = new ArrayList<>();
            String durationKey = animationType.getName() + "_duration";
            long maxDuration = preferences.getLong(durationKey, 500);

            List<AnimationParams> paramsList = animationType.getConfigWrappersForSettings();
            for (AnimationParams params : paramsList) {
                String prefixParamsKey = animationType.getName() + params.getName();
                float startDuration = preferences.getFloat(prefixParamsKey + "_startDuration", -1);
                float endDuration = preferences.getFloat(prefixParamsKey + "_endDuration", -1);
                float cp1 = preferences.getFloat(prefixParamsKey + "_cp1", -1);
                float cp2 = preferences.getFloat(prefixParamsKey + "_cp2", -1);
                animationParamsList.add(
                        new AnimationParams(params.type, maxDuration, startDuration, endDuration, cp1, cp2)
                );
            }

            loadedParameters.add(new AnimationSettingsParams(
                    animationType,
                    maxDuration,
                    animationParamsList
            ));
        }
        return loadedParameters;
    }

    public static class Duration {
        public String text;
        public long value;

        public Duration(String text, long value) {
            this.text = text;
            this.value = value;
        }

        public static List<Duration> getList() {
            ArrayList<Duration> durations = new ArrayList<>();
            durations.add(new Duration("200ms", 200));
            durations.add(new Duration("300ms", 300));
            durations.add(new Duration("400ms", 400));
            durations.add(new Duration("500ms", 500));
            durations.add(new Duration("600ms", 600));
            durations.add(new Duration("700ms", 700));
            durations.add(new Duration("800ms", 800));
            durations.add(new Duration("900ms", 900));
            durations.add(new Duration("1000ms", 1000));
            durations.add(new Duration("1500ms", 1500));
            durations.add(new Duration("2000ms", 2000));
            durations.add(new Duration("3000ms", 3000));
            return durations;
        }
    }

    public static class VooDooPagerAdapter extends PagerAdapter {

        private final SparseArray<ViewGroup> sparseArray = new SparseArray<>();

        public final List<AnimationSettingsParams> animationSettingsParamsList;

        private ActionBarPopupWindow sendPopupWindow;
        private ActionBarPopupWindow.ActionBarPopupWindowLayout sendPopupLayout;

        public long maxDuration;

        public VooDooPagerAdapter(List<AnimationSettingsParams> animationSettingsParams) {
            this.animationSettingsParamsList = animationSettingsParams;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            AnimationEditorListView rootView = new AnimationEditorListView(container.getContext());
            rootView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
            setCustomId(rootView, position);

            AnimationSettingsParams animationSettingsParams = animationSettingsParamsList.get(position);

            this.maxDuration = animationSettingsParams.getMaxDuration();

            TextSettingsCell cell = new TextSettingsCell(container.getContext());
            cell.setTextAndValue("Duration", animationSettingsParams.getMaxDurationText(), false);
            cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            cell.setOnClickListener(v -> {
                if (sendPopupLayout == null) {
                    sendPopupLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(container.getContext());
                    sendPopupLayout.setAnimationEnabled(false);
                    sendPopupLayout.setOnTouchListener(new View.OnTouchListener() {

                        private android.graphics.Rect popupRect = new android.graphics.Rect();

                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                                if (sendPopupWindow != null && sendPopupWindow.isShowing()) {
                                    v.getHitRect(popupRect);
                                    if (!popupRect.contains((int) event.getX(), (int) event.getY())) {
                                        sendPopupWindow.dismiss();
                                    }
                                }
                            }
                            return false;
                        }
                    });
                    sendPopupLayout.setDispatchKeyEventListener(keyEvent -> {
                        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0 && sendPopupWindow != null && sendPopupWindow.isShowing()) {
                            sendPopupWindow.dismiss();
                        }
                    });
                    sendPopupLayout.setShowedFromBotton(false);

                    for (int i = 0; i < Duration.getList().size(); i++) {
                        Duration duration = Duration.getList().get(i);
                        ActionBarMenuSubItem actionBarMenuSubItem = new ActionBarMenuSubItem(container.getContext(), false, false);
                        actionBarMenuSubItem.setText(duration.text);
                        sendPopupLayout.addView(actionBarMenuSubItem, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 48));
                        actionBarMenuSubItem.setOnClickListener(v1 -> {
                            cell.setTextAndValue("Duration", duration.text, false);
                            maxDuration = duration.value;
                            Toast.makeText(cell.getContext(), "Please, close and open settings again :)", Toast.LENGTH_SHORT).show();
                            sendPopupWindow.dismiss();
                        });
                    }
                    sendPopupLayout.setupRadialSelectors(Theme.getColor(Theme.key_dialogButtonSelector));

                    sendPopupWindow = new ActionBarPopupWindow(sendPopupLayout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
                    sendPopupWindow.setAnimationEnabled(false);
                    sendPopupWindow.setAnimationStyle(R.style.PopupContextAnimation2);
                    sendPopupWindow.setOutsideTouchable(true);
                    sendPopupWindow.setClippingEnabled(true);
                    sendPopupWindow.setInputMethodMode(ActionBarPopupWindow.INPUT_METHOD_NOT_NEEDED);
                    sendPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED);
                    sendPopupWindow.getContentView().setFocusableInTouchMode(true);
                }

                sendPopupLayout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(1000), View.MeasureSpec.AT_MOST));
                sendPopupWindow.setFocusable(true);
                sendPopupWindow.showAtLocation(cell, Gravity.START | Gravity.TOP, cell.getLeft(), cell.getTop());
            });
            rootView.addView(cell);

            Space space = new Space(container.getContext());
            space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(24)));
            rootView.addView(space);

            for (AnimationParams animationParams : animationSettingsParams.getAnimationParamsList()) {
                TextView title = new TextView(container.getContext());
                title.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
                title.setPadding(dp(16), dp(16), dp(16), dp(16));
                title.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteValueText));
                title.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                title.setText(animationParams.type.getName());
                rootView.addView(title);

                ChatAnimationEditor editor = new ChatAnimationEditor(container.getContext(), animationParams);
                rootView.addView(editor);
            }

            container.addView(rootView);
            sparseArray.put(position, rootView);
            return rootView;
        }

        private void setCustomId(ViewGroup root, int position) {
            switch (position) {
                case 0:
                case 1:
                case 2:
                    root.setId(position + 100);
                    break;
            }
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            AnimationEditorListView view = (AnimationEditorListView) object;
            container.removeView(view);
            sparseArray.remove(position);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return object.equals(view);
        }

        @Override
        public int getCount() {
            return animationSettingsParamsList.size();
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return animationSettingsParamsList.get(position).getAnimationType().getName();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveAllData();
    }

    private void saveAllData() {
        if (adapter != null) {
            SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
            List<AnimationSettingsParams> animationSettingsParamsList = adapter.animationSettingsParamsList;
            for (AnimationSettingsParams animationSettingsParams : animationSettingsParamsList) {
                String durationKey = animationSettingsParams.getAnimationType().getName() + "_duration";
                editor.putLong(durationKey, adapter.maxDuration);
                for (AnimationParams animationParams : animationSettingsParams.getAnimationParamsList()) {
                    String prefixParamsKey = animationSettingsParams.getAnimationType().getName() + animationParams.getName();
                    editor.putFloat(prefixParamsKey + "_startDuration", animationParams.startDuration);
                    editor.putFloat(prefixParamsKey + "_endDuration", animationParams.endDuration);
                    editor.putFloat(prefixParamsKey + "_cp1", animationParams.cp1);
                    editor.putFloat(prefixParamsKey + "_cp2", animationParams.cp2);
                }
            }
            editor.putBoolean(KEY_ANIMATIONS_CHANGED, true);
            editor.apply();
        }
    }

    @Override
    public boolean isSwipeBackEnabled(MotionEvent event) {
        return false; //TODO later
    }
}
