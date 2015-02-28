package calv1n.datetime.dependency.wheelview;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;


import java.util.LinkedList;
import java.util.List;

/**
 * 自定义滚动视图
 * Created by duchengzhen on 2014/7/19.
 */
public class WheelView extends View {

	/**
	 * 顶部底部阴影色
	 */
	private int[] SHADOWS_COLORS = new int[]{0xFF111111, 0x00AAAAAA, 0x00AAAAAA};

	/**
	 * 顶部和底部偏移量 (to hide that)
	 */
	private static final int ITEM_OFFSET_PERCENT = 0;

	/**
	 * 左右padding值
	 */
	private static final int PADDING = 10;

	/**
	 * 默认可见条目5
	 */
	private static final int DEF_VISIBLE_ITEMS = 5;

	// Wheel当前条目
	private int currentItem = 0;

	//可见条目数量
	private int visibleItems = DEF_VISIBLE_ITEMS;

	//条目高
	private int itemHeight = 0;

	//中心线
	private Drawable centerDrawable;

	// Wheel 图片资源
	private int wheelBackground = 0;

	//阴影资源
	private GradientDrawable topShadow;
	private GradientDrawable bottomShadow;

	//是否画出阴影
	private boolean drawShadows = false;
	//是否画出前景色
	private boolean drawForground = false;

	// Scrolling
	private WheelScroller scroller;
	private boolean isScrollingPerformed;
	private int scrollingOffset;

	// 是否开启循环
	boolean isCyclic = false;

	//条目布局
	private LinearLayout itemsLayout;

	//不居中第一项的值
	private int firstItem;

	// View适配器
	private WheelViewAdapter viewAdapter;

	//滚动循环
	private WheelRecycle recycle = new WheelRecycle(this);

	//监听器
	private List<OnWheelChangedListener> changingListeners = new LinkedList<OnWheelChangedListener>();
	private List<OnWheelScrollListener> scrollingListeners = new LinkedList<OnWheelScrollListener>();
	private List<OnWheelClickedListener> clickingListeners = new LinkedList<OnWheelClickedListener>();

	/**
	 * 构造器
	 */
	public WheelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initData(context);
	}

	/**
	 * 构造器
	 */
	public WheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initData(context);
	}

	/**
	 * 构造器
	 */
	public WheelView(Context context) {
		super(context);
		initData(context);
	}

	/**
	 * 初始化类数据
	 *
	 * @param context the mContext
	 */
	private void initData(Context context) {
		scroller = new WheelScroller(getContext(), scrollingListener);
	}

	// Scrolling listener
	WheelScroller.ScrollingListener scrollingListener = new WheelScroller.ScrollingListener() {
		@Override
		public void onStarted() {
			isScrollingPerformed = true;
			notifyScrollingListenersAboutStart();
		}

		@Override
		public void onScroll(int distance) {
			doScroll(distance);

			int height = getHeight();
			if (scrollingOffset > height) {
				scrollingOffset = height;
				scroller.stopScrolling();
			} else if (scrollingOffset < -height) {
				scrollingOffset = -height;
				scroller.stopScrolling();
			}
		}

		@Override
		public void onFinished() {
			if (isScrollingPerformed) {
				notifyScrollingListenersAboutEnd();
				isScrollingPerformed = false;
			}

			scrollingOffset = 0;
			invalidate();
		}

		@Override
		public void onJustify() {
			if (Math.abs(scrollingOffset) > WheelScroller.MIN_DELTA_FOR_SCROLLING) {
				scroller.scroll(scrollingOffset, 0);
			}
		}
	};

	/**
	 * Set the the specified scrolling interpolator
	 *
	 * @param interpolator the interpolator
	 */
	public void setInterpolator(Interpolator interpolator) {
		scroller.setInterpolator(interpolator);
	}

	/**
	 * Gets count of visible items
	 *
	 * @return the count of visible items
	 */
	public int getVisibleItems() {
		return visibleItems;
	}

	/**
	 * Sets the desired count of visible items.
	 * Actual amount of visible items depends on wheel layout parameters.
	 * To apply changes and rebuild view call measure().
	 *
	 * @param count the desired count for visible items
	 */
	public void setVisibleItems(int count) {
		visibleItems = count;
	}

	/**
	 * Gets view adapter
	 *
	 * @return the view adapter
	 */
	public WheelViewAdapter getViewAdapter() {
		return viewAdapter;
	}

	// Adapter listener
	private DataSetObserver dataObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			invalidateWheel(false);
		}

		@Override
		public void onInvalidated() {
			invalidateWheel(true);
		}
	};

	/**
	 * Sets view adapter. Usually new adapters contain different views, so
	 * it needs to rebuild view by calling measure().
	 *
	 * @param viewAdapter the view adapter
	 */
	public void setViewAdapter(WheelViewAdapter viewAdapter) {
		if (this.viewAdapter != null) {
			this.viewAdapter.unregisterDataSetObserver(dataObserver);
		}
		this.viewAdapter = viewAdapter;
		if (this.viewAdapter != null) {
			this.viewAdapter.registerDataSetObserver(dataObserver);
		}

		invalidateWheel(true);
	}

	/**
	 * Adds wheel changing listener
	 *
	 * @param listener the listener
	 */
	public void addChangingListener(OnWheelChangedListener listener) {
		changingListeners.add(listener);
	}

	/**
	 * Removes wheel changing listener
	 *
	 * @param listener the listener
	 */
	public void removeChangingListener(OnWheelChangedListener listener) {
		changingListeners.remove(listener);
	}

	/**
	 * Notifies changing listeners
	 *
	 * @param oldValue the old wheel value
	 * @param newValue the new wheel value
	 */
	protected void notifyChangingListeners(int oldValue, int newValue) {
		for (OnWheelChangedListener listener : changingListeners) {
			listener.onChanged(this, oldValue, newValue);
		}
	}

	/**
	 * Adds wheel scrolling listener
	 *
	 * @param listener the listener
	 */
	public void addScrollingListener(OnWheelScrollListener listener) {
		scrollingListeners.add(listener);
	}

	/**
	 * Removes wheel scrolling listener
	 *
	 * @param listener the listener
	 */
	public void removeScrollingListener(OnWheelScrollListener listener) {
		scrollingListeners.remove(listener);
	}

	/**
	 * Notifies listeners about starting scrolling
	 */
	protected void notifyScrollingListenersAboutStart() {
		for (OnWheelScrollListener listener : scrollingListeners) {
			listener.onScrollingStarted(this);
		}
	}

	/**
	 * Notifies listeners about ending scrolling
	 */
	protected void notifyScrollingListenersAboutEnd() {
		for (OnWheelScrollListener listener : scrollingListeners) {
			listener.onScrollingFinished(this);
		}
	}

	/**
	 * Adds wheel clicking listener
	 *
	 * @param listener the listener
	 */
	public void addClickingListener(OnWheelClickedListener listener) {
		clickingListeners.add(listener);
	}

	/**
	 * Removes wheel clicking listener
	 *
	 * @param listener the listener
	 */
	public void removeClickingListener(OnWheelClickedListener listener) {
		clickingListeners.remove(listener);
	}

	/**
	 * Notifies listeners about clicking
	 */
	protected void notifyClickListenersAboutClick(int item) {
		for (OnWheelClickedListener listener : clickingListeners) {
			listener.onItemClicked(this, item);
		}
	}

	/**
	 * Gets current value
	 *
	 * @return the current value
	 */
	public int getCurrentItem() {
		return currentItem;
	}

	/**
	 * Sets the current item. Does nothing when index is wrong.
	 *
	 * @param index    the item index
	 * @param animated the animation flag
	 */
	public void setCurrentItem(int index, boolean animated) {
		if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
			return; // throw?
		}

		int itemCount = viewAdapter.getItemsCount();
		if (index < 0 || index >= itemCount) {
			if (isCyclic) {
				while (index < 0) {
					index += itemCount;
				}
				index %= itemCount;
			} else {
				return; // throw?
			}
		}
		if (index != currentItem) {
			if (animated) {
				int itemsToScroll = index - currentItem;
				if (isCyclic) {
					int scroll = itemCount + Math.min(index, currentItem) - Math.max(index, currentItem);
					if (scroll < Math.abs(itemsToScroll)) {
						itemsToScroll = itemsToScroll < 0 ? scroll : -scroll;
					}
				}
				scroll(itemsToScroll, 0);
			} else {
				scrollingOffset = 0;

				int old = currentItem;
				currentItem = index;

				notifyChangingListeners(old, currentItem);

				invalidate();
			}
		}
	}

	/**
	 * Sets the current item w/o animation. Does nothing when index is wrong.
	 *
	 * @param index the item index
	 */
	public void setCurrentItem(int index) {
		setCurrentItem(index, false);
	}

	/**
	 * Tests if wheel is cyclic. That means before the 1st item there is shown the last one
	 *
	 * @return true if wheel is cyclic
	 */
	public boolean isCyclic() {
		return isCyclic;
	}

	/**
	 * Set wheel cyclic flag
	 *
	 * @param isCyclic the flag to saveConfig
	 */
	public void setCyclic(boolean isCyclic) {
		this.isCyclic = isCyclic;
		invalidateWheel(false);
	}

	/**
	 * Determine whether shadows are drawn
	 *
	 * @return true is shadows are drawn
	 */
	public boolean drawShadows() {
		return drawShadows;
	}

	/**
	 * Set whether shadows should be drawn
	 *
	 * @param drawShadows flag as true or false
	 */
	public void setDrawShadows(boolean drawShadows) {
		this.drawShadows = drawShadows;
	}

	public void setDrawForground(boolean drawForground) {
		this.drawForground = drawForground;
	}

	/**
	 * 设置阴影渐变色
	 *
	 * @param start  开始色
	 * @param middle 中间色
	 * @param end    结束色
	 */
	public void setShadowColor(int start, int middle, int end) {
		SHADOWS_COLORS = new int[]{start, middle, end};
	}

	/**
	 * 设置wheel背景色
	 *
	 * @param resId
	 */
	public void setWheelBackground(int resId) {
		wheelBackground = resId;
	}

	/**
	 * 设置wheel前景色
	 *
	 * @param resId
	 */
	public void setWheelCenter(int resId) {
		centerDrawable = getContext().getResources().getDrawable(resId);
	}

	/**
	 * Invalidates wheel
	 *
	 * @param clearCaches if true then cached views will be clear
	 */
	public void invalidateWheel(boolean clearCaches) {
		if (clearCaches) {
			recycle.clearAll();
			if (itemsLayout != null) {
				itemsLayout.removeAllViews();
			}
			scrollingOffset = 0;
		} else if (itemsLayout != null) {
			// cache all items
			recycle.recycleItems(itemsLayout, firstItem, new ItemsRange());
		}

		invalidate();
	}

	/**
	 * Initializes resources
	 */
	private void initResourcesIfNecessary() {
		if (centerDrawable == null) {
			centerDrawable = getContext().getResources().getDrawable(R.drawable.wheel_default_forground);
		}

		if (topShadow == null) {
			topShadow = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, SHADOWS_COLORS);
		}

		if (bottomShadow == null) {
			bottomShadow = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, SHADOWS_COLORS);
		}

		if (wheelBackground != 0) {
			setBackgroundResource(wheelBackground);
		}
	}

	/**
	 * Calculates desired height for layout
	 *
	 * @param layout the source layout
	 * @return the desired layout height
	 */
	private int getDesiredHeight(LinearLayout layout) {
		if (layout != null && layout.getChildAt(0) != null) {
			itemHeight = layout.getChildAt(0).getMeasuredHeight();
		}

		int desired = itemHeight * visibleItems - itemHeight * ITEM_OFFSET_PERCENT / 50;

		return Math.max(desired, getSuggestedMinimumHeight());
	}

	/**
	 * Returns height of wheel item
	 *
	 * @return the item height
	 */
	private int getItemHeight() {
		//todo 修改这里
		if (itemHeight != 0) {
			return itemHeight;
		}

		if (itemsLayout != null && itemsLayout.getChildAt(0) != null) {
			itemHeight = itemsLayout.getChildAt(0).getHeight();
			return itemHeight;
		}
		return getHeight() / visibleItems;

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		// clear all items
		if (itemsLayout != null) {
			recycle.recycleItems(itemsLayout, firstItem, new ItemsRange());
		} else {
			createItemsLayout();
		}

		//Builds view for measuring,add views
		int addItems = visibleItems / 2;
		for (int i = currentItem + addItems; i >= currentItem - addItems; i--) {
			if (addViewItem(i, true)) {
				firstItem = i;
			}
		}
		//控件的宽度
		int width = calculateLayoutWidth(widthSize, widthMode);
		//控件的高度
		int height;
		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else {
			height = getDesiredHeight(itemsLayout);
			if (heightMode == MeasureSpec.AT_MOST) {
				height = Math.min(height, heightSize);
			}
		}

		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		layout(r - l, b - t);
	}

	/**
	 * Sets layouts width and height
	 *
	 * @param width  the layout width
	 * @param height the layout height
	 */
	private void layout(int width, int height) {
		int itemsWidth = width - 2 * PADDING;

		itemsLayout.layout(0, 0, itemsWidth, height);
	}

	/**
	 * Calculates control width and creates text layouts
	 *
	 * @param widthSize the input layout width
	 * @param mode      the layout mode
	 * @return the calculated control width
	 */
	private int calculateLayoutWidth(int widthSize, int mode) {
		initResourcesIfNecessary();

		// TODO: make it static
		itemsLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		itemsLayout.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		int width = itemsLayout.getMeasuredWidth();

		if (mode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else {
			width += 2 * PADDING;

			// Check against our minimum width
			width = Math.max(width, getSuggestedMinimumWidth());

			if (mode == MeasureSpec.AT_MOST && widthSize < width) {
				width = widthSize;
			}
		}

		itemsLayout.measure(MeasureSpec.makeMeasureSpec(width - 2 * PADDING, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		return width;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (viewAdapter != null && viewAdapter.getItemsCount() > 0) {
			if (rebuildItems()) {
				calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);
				layout(getWidth(), getHeight());
			}

			//画条目
			canvas.save();
			int top = (currentItem - firstItem) * getItemHeight() + (getItemHeight() - getHeight()) / 2;
			canvas.translate(PADDING, -top + scrollingOffset);
			itemsLayout.draw(canvas);
			canvas.restore();

			//在当前选中条目上画出选中条目效果的半透明块
			if (drawForground) {
				drawCenterRect(canvas);
			}
		}

		if (drawShadows) drawShadows(canvas);
	}

	/**
	 * Draws shadows on top and bottom of control
	 *
	 * @param canvas the canvas for drawing
	 */
	private void drawShadows(Canvas canvas) {
		int height = (int) (1.5 * getItemHeight());
		topShadow.setBounds(0, 0, getWidth(), height);
		topShadow.draw(canvas);

		bottomShadow.setBounds(0, getHeight() - height, getWidth(), getHeight());
		bottomShadow.draw(canvas);
	}

	/**
	 * 在当前选中条目上画出选中条目效果的半透明块
	 *
	 * @param canvas the canvas for drawing
	 */
	private void drawCenterRect(Canvas canvas) {
		int center = getHeight() / 2;
		int offset = (int) (getItemHeight() / 2 * 1.2);
		centerDrawable.setBounds(0, center - offset, getWidth(), center + offset);
		centerDrawable.draw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled() || getViewAdapter() == null) {
			return true;
		}

		switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE:
				if (getParent() != null) {
					getParent().requestDisallowInterceptTouchEvent(true);
				}
				break;

			case MotionEvent.ACTION_UP:
				if (!isScrollingPerformed) {
					int distance = (int) event.getY() - getHeight() / 2;
					if (distance > 0) {
						distance += getItemHeight() / 2;
					} else {
						distance -= getItemHeight() / 2;
					}
					int items = distance / getItemHeight();
					if (items != 0 && isValidItemIndex(currentItem + items)) {
						notifyClickListenersAboutClick(currentItem + items);
					}
				}
				break;
		}

		return scroller.onTouchEvent(event);
	}

	/**
	 * Scrolls the wheel
	 *
	 * @param delta the scrolling value
	 */
	private void doScroll(int delta) {
		scrollingOffset += delta;

		int itemHeight = getItemHeight();
		int count = scrollingOffset / itemHeight;

		int pos = currentItem - count;
		int itemCount = viewAdapter.getItemsCount();

		int fixPos = scrollingOffset % itemHeight;
		if (Math.abs(fixPos) <= itemHeight / 2) {
			fixPos = 0;
		}
		if (isCyclic && itemCount > 0) {
			if (fixPos > 0) {
				pos--;
				count++;
			} else if (fixPos < 0) {
				pos++;
				count--;
			}
			// fix position by rotating
			while (pos < 0) {
				pos += itemCount;
			}
			pos %= itemCount;
		} else {
			//
			if (pos < 0) {
				count = currentItem;
				pos = 0;
			} else if (pos >= itemCount) {
				count = currentItem - itemCount + 1;
				pos = itemCount - 1;
			} else if (pos > 0 && fixPos > 0) {
				pos--;
				count++;
			} else if (pos < itemCount - 1 && fixPos < 0) {
				pos++;
				count--;
			}
		}

		int offset = scrollingOffset;
		if (pos != currentItem) {
			setCurrentItem(pos, false);
		} else {
			invalidate();
		}

		// update offset
		scrollingOffset = offset - count * itemHeight;
		if (scrollingOffset > getHeight()) {
			scrollingOffset = scrollingOffset % getHeight() + getHeight();
		}
	}

	/**
	 * Scroll the wheel
	 *
	 * @param itemsToScroll items to scroll
	 * @param time          scrolling duration
	 */
	public void scroll(int itemsToScroll, int time) {
		int distance = itemsToScroll * getItemHeight() - scrollingOffset;
		scroller.scroll(distance, time);
	}

	/**
	 * Calculates range for wheel items
	 *
	 * @return the items range
	 */
	private ItemsRange getItemsRange() {
		if (getItemHeight() == 0) {
			return null;
		}

		int first = currentItem;
		int count = 1;

		while (count * getItemHeight() < getHeight()) {
			first--;
			count += 2; // top + bottom items
		}

		if (scrollingOffset != 0) {
			if (scrollingOffset > 0) {
				first--;
			}
			count++;

			// process empty items above the first or below the second
			int emptyItems = scrollingOffset / getItemHeight();
			first -= emptyItems;
			count += Math.asin(emptyItems);
		}
		return new ItemsRange(first, count);
	}

	/**
	 * Rebuilds wheel items if necessary. Caches all unused items.
	 *
	 * @return true if items are rebuilt
	 */
	private boolean rebuildItems() {
		boolean updated = false;
		ItemsRange range = getItemsRange();
		if (itemsLayout != null) {
			int first = recycle.recycleItems(itemsLayout, firstItem, range);
			updated = firstItem != first;
			firstItem = first;
		} else {
			createItemsLayout();
			updated = true;
		}

		if (!updated) {
			updated = firstItem != range.getFirst() || itemsLayout.getChildCount() != range.getCount();
		}

		if (firstItem > range.getFirst() && firstItem <= range.getLast()) {
			for (int i = firstItem - 1; i >= range.getFirst(); i--) {
				if (!addViewItem(i, true)) {
					break;
				}
				firstItem = i;
			}
		} else {
			firstItem = range.getFirst();
		}

		int first = firstItem;
		for (int i = itemsLayout.getChildCount(); i < range.getCount(); i++) {
			if (!addViewItem(firstItem + i, false) && itemsLayout.getChildCount() == 0) {
				first++;
			}
		}
		firstItem = first;

		return updated;
	}

	/**
	 * Updates view. Rebuilds items and label if necessary, recalculate items sizes.
	 */
	private void updateView() {
		if (rebuildItems()) {
			calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);
			layout(getWidth(), getHeight());
		}
	}

	/**
	 * Creates item layouts if necessary如果需要创建项目布局
	 */
	private void createItemsLayout() {
		if (itemsLayout == null) {
			itemsLayout = new LinearLayout(getContext());
			itemsLayout.setOrientation(LinearLayout.VERTICAL);
			//todo 尝试修改这里
			//itemsLayout.setGravity(Gravity.CENTER);
		}
	}

	/**
	 * Builds view for measuring
	 */
	private void buildViewForMeasuring() {
		// clear all items
		if (itemsLayout != null) {
			recycle.recycleItems(itemsLayout, firstItem, new ItemsRange());
		} else {
			createItemsLayout();
		}

		// add views
		int addItems = visibleItems / 2;
		for (int i = currentItem + addItems; i >= currentItem - addItems; i--) {
			if (addViewItem(i, true)) {
				firstItem = i;
			}
		}
	}

	/**
	 * Adds view for item to items layout
	 *
	 * @param index the item index
	 * @param first the flag indicates if view should be first
	 * @return true if corresponding item exists and is added
	 */
	private boolean addViewItem(int index, boolean first) {
		View view = getItemView(index);
		if (view != null) {
			if (first) {
				itemsLayout.addView(view, 0);
			} else {
				itemsLayout.addView(view);
			}

			return true;
		}

		return false;
	}

	/**
	 * Checks whether intem index is valid
	 *
	 * @param index the item index
	 * @return true if item index is not out of bounds or the wheel is cyclic
	 */
	private boolean isValidItemIndex(int index) {
		return viewAdapter != null && viewAdapter.getItemsCount() > 0 &&
				(isCyclic || index >= 0 && index < viewAdapter.getItemsCount());
	}

	/**
	 * Returns view for specified item
	 *
	 * @param index the item index
	 * @return item view or empty view if index is out of bounds
	 */
	private View getItemView(int index) {
		if (viewAdapter == null || viewAdapter.getItemsCount() == 0) {
			return null;
		}
		int count = viewAdapter.getItemsCount();
		if (!isValidItemIndex(index)) {
			return viewAdapter.getEmptyItem(recycle.getEmptyItem(), itemsLayout);
		} else {
			while (index < 0) {
				index = count + index;
			}
		}

		index %= count;
		return viewAdapter.getItem(index, recycle.getItem(), itemsLayout);
	}

	/**
	 * Stops scrolling
	 */
	public void stopScrolling() {
		scroller.stopScrolling();
	}

	/**
	 * Wheel changed listener interface.
	 * <p>The onChanged() method is called whenever current wheel positions is changed:
	 * <li> New Wheel position is saveConfig
	 * <li> Wheel view is scrolled
	 */
	public interface OnWheelChangedListener {
		/**
		 * Callback method to be invoked when current item changed
		 *
		 * @param wheel    the wheel view whose state has changed
		 * @param oldValue the old value of current item
		 * @param newValue the new value of current item
		 */
		void onChanged(WheelView wheel, int oldValue, int newValue);
	}

	/**
	 * Wheel scrolled listener interface.
	 */
	public interface OnWheelScrollListener {
		/**
		 * Callback method to be invoked when scrolling started.
		 *
		 * @param wheel the wheel view whose state has changed.
		 */
		void onScrollingStarted(WheelView wheel);

		/**
		 * Callback method to be invoked when scrolling ended.
		 *
		 * @param wheel the wheel view whose state has changed.
		 */
		void onScrollingFinished(WheelView wheel);
	}

	/**
	 * Wheel clicked listener interface.
	 * <p>The onItemClicked() method is called whenever a wheel item is clicked
	 * <li> New Wheel position is saveConfig
	 * <li> Wheel view is scrolled
	 */
	public interface OnWheelClickedListener {
		/**
		 * Callback method to be invoked when current item clicked
		 *
		 * @param wheel     the wheel view
		 * @param itemIndex the index of clicked item
		 */
		void onItemClicked(WheelView wheel, int itemIndex);
	}
}
